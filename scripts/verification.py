#!/usr/bin/env python3
"""Produce and validate verification evidence for one actual build/test execution."""
import argparse
from datetime import datetime, timezone
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
APK = Path('app/build/outputs/apk/debug/app-debug.apk')
JVM = Path('app/build/test-results/testDebugUnitTest')
ANDROID = Path('app/build/outputs/androidTest-results/connected')
LINT = Path('app/build/reports/lint-results-debug.xml')
PENDING = ('قيد التحقق على محاكي', 'يجب استبدال هذه الفقرة')


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def current_commit(root):
    return subprocess.run(['git', 'rev-parse', 'HEAD'], cwd=root, capture_output=True, text=True, check=True).stdout.strip()


def source_digest(root):
    root = Path(root)
    files = list((root / 'app/src').rglob('*'))
    for name in ('build.gradle.kts', 'settings.gradle.kts', 'gradle.properties', 'app/build.gradle.kts',
                 'gradle/wrapper/gradle-wrapper.properties', 'gradle/wrapper/gradle-wrapper.jar'):
        files.append(root / name)
    digest = hashlib.sha256()
    for p in sorted(set(p for p in files if p.is_file())):
        digest.update(p.relative_to(root).as_posix().encode() + b'\0' + p.read_bytes() + b'\0')
    return digest.hexdigest()


def expected_tests(root, kind):
    folder = 'androidTest' if kind == 'android' else 'test'
    result = set()
    for path in (Path(root) / 'app/src' / folder).rglob('*.kt'):
        text = re.sub(r'/\*.*?\*/|//[^\n]*', '', path.read_text(), flags=re.S)
        package = re.search(r'^\s*package\s+([\w.]+)', text, re.M)
        klass = re.search(r'\bclass\s+(\w+)', text)
        if package and klass:
            for method in re.findall(r'@Test(?:\([^)]*\))?\s+fun\s+(\w+)\s*\(', text):
                result.add((package[1] + '.' + klass[1], method))
    return result


def collect_junit(paths):
    result = dict(tests=0, passed=0, failures=0, errors=0, skipped=0, cases=[], problems=[])
    seen = set()
    if not paths:
        result['problems'].append('No JUnit XML reports found')
    for path in paths:
        try:
            document = ET.parse(path).getroot()
            if document.tag not in ('testsuite', 'testsuites'):
                raise ValueError('Not a JUnit testsuite/testsuites document')
            for suite in (node for node in document.iter() if node.tag in ('testsuite', 'testsuites')):
                # Aggregate suites may repeat totals; inspect every testcase exactly once below.
                children = list(suite.iter('testcase'))
                if 'tests' in suite.attrib and int(suite.get('tests')) != len(children):
                    result['problems'].append(f'{Path(path).name}: declared tests disagree with testcase entries')
                for attr, tag in (('failures', 'failure'), ('errors', 'error'), ('skipped', 'skipped')):
                    actual = sum(c.find(tag) is not None for c in children)
                    if attr in suite.attrib and int(suite.get(attr)) != actual:
                        result['problems'].append(f'{Path(path).name}: declared {attr} disagree with testcase entries')
            for case in document.iter('testcase'):
                identity = (case.get('classname', ''), case.get('name', ''))
                if not all(identity) or identity in seen:
                    result['problems'].append(f'Invalid or duplicate testcase: {identity}')
                seen.add(identity)
                flags = [tag for tag in ('failure', 'error', 'skipped') if case.find(tag) is not None]
                if len(flags) > 1:
                    result['problems'].append(f'Conflicting testcase outcomes: {identity}')
                status = flags[0] if flags else 'passed'
                result['cases'].append(dict(classname=identity[0], name=identity[1], status=status))
                result['tests'] += 1
                result[{'failure': 'failures', 'error': 'errors'}.get(status, status)] += 1
        except (ET.ParseError, OSError, ValueError, TypeError) as exc:
            result['problems'].append(f'{Path(path).name}: {exc}')
    if not result['tests']:
        result['problems'].append('Zero executed testcase entries')
    return result


def begin(root, run_id):
    root = Path(root)
    if not run_id.strip():
        raise ValueError('An explicit execution ID is required')
    out = root / 'build/verification'
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True)
    # Clear only generated evidence. Gradle must create new results for this execution.
    for relative in (JVM, ANDROID, LINT, Path('app/build/reports/lint-results-debug.txt'),
                     Path('app/build/reports/lint-results-debug.html'), Path('app/build/reports/apk-signature.txt'),
                     Path('app/build/reports/apk-metadata.txt')):
        p = root / relative
        if p.is_dir():
            shutil.rmtree(p)
        elif p.exists():
            p.unlink()
    context = dict(schema=1, run_id=run_id, started_at=datetime.now(timezone.utc).isoformat(),
                   source_commit=current_commit(root), source_digest=source_digest(root))
    (out / 'context.json').write_text(json.dumps(context, indent=2) + '\n')
    return context


def lint_result(path):
    try:
        document = ET.parse(path).getroot()
        if document.tag != 'issues':
            raise ValueError('Not an Android lint issues report')
        issues = list(document.iter('issue'))
        return dict(errors=sum(i.get('severity', '').lower() in ('error', 'fatal') for i in issues),
                    warnings=sum(i.get('severity', '').lower() == 'warning' for i in issues), problems=[])
    except (OSError, ET.ParseError, ValueError) as exc:
        return dict(errors=0, warnings=0, problems=[f'Lint evidence unavailable: {exc}'])


def suite_problems(root, kind, suite):
    problems = list(suite['problems'])
    expected = expected_tests(root, kind)
    observed = {(c['classname'], c['name']) for c in suite['cases']}
    if not expected:
        problems.append(f'No expected {kind} test methods could be discovered from source')
    if observed != expected:
        problems.append(f'{kind} coverage mismatch: missing {sorted(expected-observed)}, unexpected {sorted(observed-expected)}')
    if suite['failures'] or suite['errors'] or suite['skipped']:
        problems.append(f'{kind} tests include failures, errors or skipped cases')
    return problems


def generate(root, run_id, build_outcome, android_outcome, device):
    root = Path(root)
    out = root / 'build/verification'
    context = json.loads((out / 'context.json').read_text())
    problems = []
    if context['run_id'] != run_id:
        problems.append('Execution ID does not match begin marker')
    if context['source_commit'] != current_commit(root) or context['source_digest'] != source_digest(root):
        problems.append('Source changed during verification')
    evidence = out / 'evidence'
    if evidence.exists():
        shutil.rmtree(evidence)
    evidence.mkdir()
    files = []
    started = datetime.fromisoformat(context['started_at']).timestamp()
    for kind, relative in (('jvm', JVM), ('android', ANDROID)):
        originals = sorted((root / relative).rglob('TEST-*.xml'))
        for index, p in enumerate(originals):
            if p.stat().st_mtime < started - 2:
                problems.append(f'Stale {kind} XML predates this execution: {p.name}')
            dest = evidence / kind / f'{index}-{p.name}'
            dest.parent.mkdir(exist_ok=True)
            shutil.copy2(p, dest)
            files.append(dict(kind=kind, path=dest.relative_to(out).as_posix(), sha256=sha(dest)))
    p = root / LINT
    if p.exists():
        if p.stat().st_mtime < started - 2:
            problems.append('Stale lint XML predates this execution')
        dest = evidence / 'lint.xml'
        shutil.copy2(p, dest)
        files.append(dict(kind='lint', path='evidence/lint.xml', sha256=sha(dest)))
    for name in ('apk-signature.txt', 'apk-metadata.txt'):
        original = root / 'app/build/reports' / name
        if original.is_file():
            dest = evidence / name
            shutil.copy2(original, dest)
            files.append(dict(kind='apk_metadata', path='evidence/' + name, sha256=sha(dest)))
    jvm = collect_junit([out / f['path'] for f in files if f['kind'] == 'jvm'])
    android = collect_junit([out / f['path'] for f in files if f['kind'] == 'android'])
    lint = lint_result(evidence / 'lint.xml')
    problems += suite_problems(root, 'jvm', jvm) + suite_problems(root, 'android', android) + lint['problems']
    if build_outcome != 'success':
        problems.append(f'Build/JVM/lint command outcome: {build_outcome}')
    if android_outcome != 'success':
        problems.append(f'Android command outcome: {android_outcome}; success is not established')
    if lint['errors']:
        problems.append('Lint contains errors')
    apk = root / APK
    if not apk.is_file():
        problems.append('APK missing')
    record = dict(**context, completed_at=datetime.now(timezone.utc).isoformat(),
                  build_outcome=build_outcome, android_outcome=android_outcome,
                  device=device, apk_sha256=sha(apk) if apk.is_file() else None,
                  jvm=jvm, android=android, lint=lint, files=files,
                  status='passed' if not problems else 'not_verified', problems=problems)
    record['github_run_url'] = (f"https://github.com/{os.environ['GITHUB_REPOSITORY']}/actions/runs/{os.environ['GITHUB_RUN_ID']}"
                                if os.environ.get('GITHUB_RUN_ID') and os.environ.get('GITHUB_REPOSITORY') else None)
    status_ar = 'اجتازت الجولة شروط التحقق.' if not problems else 'لم يثبت اجتياز الجولة؛ لا يجوز اعتماد حزمة نهائية ناجحة.'
    def totals(name, data):
        return f"- {name}: {data['tests']} حالات مسجلة، {data['passed']} ناجحة، {data['failures']} إخفاقات، {data['errors']} أخطاء، {data['skipped']} متخطاة."
    lines = ['# تقرير التحقق المولد من نتائج التنفيذ', '', status_ar, '',
             f"- معرّف التنفيذ: `{run_id}`", f"- الالتزام: `{context['source_commit']}`",
             f"- بداية التنفيذ UTC: {context['started_at']}", f"- اكتمال التقرير UTC: {record['completed_at']}",
             f"- بيئة Android المطلوبة: {device}", f"- نتيجة أمر البناء: {build_outcome}",
             f"- نتيجة أمر Android: {android_outcome}", totals('JVM', jvm), totals('Android', android),
             f"- Lint: {lint['errors']} أخطاء، {lint['warnings']} تحذيرات.",
             f"- SHA-256 لملف APK: `{record['apk_sha256']}`", '',
             'تعتمد الأعداد على عناصر testcase الفعلية، مع مطابقة جميع اختبارات المصدر. تجميع اختبارات Android وحده لا يعد تشغيلًا لها.', '',
             '## حالات الاختبار الموثقة', '']
    for kind, suite in (('JVM', jvm), ('Android', android)):
        lines += [f"- {kind}: `{c['classname']}.{c['name']}` — {c['status']}" for c in suite['cases']]
    lines += ['', '## حدود هذه الجولة', '',
              'تثبت النتائج البيئة المذكورة فقط؛ لا تثبت اختبارات أجهزة فعلية أو API 35 أو TalkBack أو جميع أحجام الشاشات. توقيع APK تطويري، وليس مفتاح نشر إنتاجي ثابتًا.',
              'تبقى حدود المحتوى العلمي والبيانات المحلية والتراخيص موثقة في REQUIREMENTS.md وSOURCES.md. لا تنشئ هذه الجولة بيانات عينات يمنية أو اعتمادًا مهنيًا.', '',
              '## العوائق وأسباب عدم الاجتياز', '']
    lines += ['- ' + p for p in problems] if problems else ['لم تسجل عوائق تمنع اجتياز اختبارات هذه الجولة.']
    if record['github_run_url']:
        lines += ['', f"رابط Actions: {record['github_run_url']}"]
    report = out / 'VERIFICATION.md'
    report.write_text('\n'.join(lines) + '\n')
    record['report_sha256'] = sha(report)
    (out / 'verification.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n')
    return record


def validate(root, verification_dir, apk, run_id):
    root, out, apk = Path(root), Path(verification_dir), Path(apk)
    problems = []
    try:
        record = json.loads((out / 'verification.json').read_text())
        context = json.loads((out / 'context.json').read_text())
        if not isinstance(record, dict) or not isinstance(context, dict):
            raise ValueError('Verification record/context must be JSON objects')
        if any(record.get(k) != context.get(k) for k in ('run_id', 'source_commit', 'source_digest', 'started_at')):
            problems.append('Verification record does not match execution begin marker')
        report = out / 'VERIFICATION.md'
        if any(p in report.read_text() for p in PENDING):
            problems.append('Verification report still contains a pending Android test placeholder')
        if record.get('schema') != 1 or record.get('status') != 'passed' or record.get('problems'):
            problems.append('Verification record does not establish a successful complete run')
        if not run_id or record.get('run_id') != run_id:
            problems.append('Verification execution ID mismatch')
        if record.get('source_commit') != current_commit(root) or record.get('source_digest') != source_digest(root):
            problems.append('Verification source revision/input digest mismatch')
        if record.get('apk_sha256') != sha(apk):
            problems.append('Verification APK SHA256 mismatch')
        if record.get('report_sha256') != sha(report):
            problems.append('Verification Markdown digest mismatch')
        if record.get('build_outcome') != 'success' or record.get('android_outcome') != 'success':
            problems.append('Build or Android command was not successful')
        checked = []
        for file in record.get('files', []):
            p = out / file['path']
            if p.is_symlink() or not p.resolve().is_relative_to(out.resolve()) or file['sha256'] != sha(p):
                problems.append('Evidence path/hash mismatch: ' + file['path'])
            checked.append((file['kind'], p))
        for kind in ('jvm', 'android'):
            parsed = collect_junit([p for k, p in checked if k == kind])
            problems += suite_problems(root, kind, parsed)
            if parsed != record.get(kind):
                problems.append(f'{kind} evidence does not match verification summary')
        lint_files = [p for k, p in checked if k == 'lint']
        if len(lint_files) != 1:
            problems.append('Exactly one lint XML is required')
        else:
            lint = lint_result(lint_files[0])
            if lint != record.get('lint') or lint['errors'] or lint['problems']:
                problems.append('Lint evidence invalid or inconsistent')
    except (OSError, ValueError, KeyError, TypeError, subprocess.CalledProcessError) as exc:
        problems.append('Missing or invalid current-run verification evidence: ' + str(exc))
    return problems


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['begin', 'report'])
    parser.add_argument('--run-id', required=True)
    parser.add_argument('--build-outcome', default='unknown')
    parser.add_argument('--android-outcome', default='unknown')
    parser.add_argument('--device', default='Not recorded')
    args = parser.parse_args()
    if args.action == 'begin':
        begin(ROOT, args.run_id)
        print('Started verification execution:', args.run_id)
    else:
        record = generate(ROOT, args.run_id, args.build_outcome, args.android_outcome, args.device)
        print('Verification status:', record['status'])
        for problem in record['problems']:
            print('UNVERIFIED:', problem)
        # Preserve honest diagnostics even after a failed/skipped test step. Packaging enforces status.
    return 0


if __name__ == '__main__':
    raise SystemExit(main())
