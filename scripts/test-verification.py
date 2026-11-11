#!/usr/bin/env python3
"""Regression checks for current-run evidence and fail-closed final packaging.

Uses temporary Git repositories and synthetic JUnit XML. This never runs Android,
Gradle, or changes the application's source/assets.
"""
import json
import os
from pathlib import Path
import subprocess
import shutil
import tempfile
import unittest

import verification


def write(path, text):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding='utf-8')
    return path


def suite(cases, name='arbitrary.suite', declared=None):
    """cases are (classname, method, passed|failure|error|skipped)."""
    counts = {key: sum(status == key for _, _, status in cases)
              for key in ('failure', 'error', 'skipped')}
    number = len(cases) if declared is None else declared
    children = []
    for classname, method, status in cases:
        child = '' if status == 'passed' else f'<{status} message="fixture">fixture</{status}>'
        children.append(f'<testcase classname="{classname}" name="{method}" time="0.01">{child}</testcase>')
    return (f'<testsuite name="{name}" tests="{number}" failures="{counts["failure"]}" '
            f'errors="{counts["error"]}" skipped="{counts["skipped"]}">'
            + ''.join(children) + '</testsuite>')


class JunitCollectionTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix='chemistry junit fixture ')
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)

    def xml(self, name, text):
        return write(self.root / name, text)

    def test_counts_actual_cases_and_statuses(self):
        rows = [('example.Checks', name, status) for name, status in
                [('ok', 'passed'), ('bad', 'failure'), ('broken', 'error'), ('omitted', 'skipped')]]
        result = verification.collect_junit([self.xml('mixed.xml', suite(rows))])
        self.assertEqual((4, 1, 1, 1, 1), tuple(result[k] for k in ('tests', 'passed', 'failures', 'errors', 'skipped')))
        self.assertEqual({'passed', 'failure', 'error', 'skipped'}, {c['status'] for c in result['cases']})

    def test_missing_and_malformed_xml_are_not_success(self):
        for path in [self.root / 'missing.xml', self.xml('malformed.xml', '<testsuite><testcase>')]:
            with self.subTest(path=path.name):
                result = verification.collect_junit([path])
                self.assertTrue(result['problems'])
                self.assertEqual(0, result['passed'])

    def test_empty_suite_cannot_create_passed_cases(self):
        result = verification.collect_junit([self.xml('empty.xml', suite([]))])
        self.assertEqual(0, result['tests'])
        self.assertEqual(0, result['passed'])
        self.assertEqual([], result['cases'])

    def test_declared_test_count_mismatch_is_reported(self):
        result = verification.collect_junit([self.xml('mismatch.xml', suite([('example.Checks', 'ok', 'passed')], declared=9))])
        self.assertEqual(1, result['tests'])
        self.assertTrue(result['problems'])

    def test_duplicate_case_in_two_files_is_reported(self):
        document = suite([('example.Checks', 'same', 'passed')])
        result = verification.collect_junit([self.xml('one.xml', document), self.xml('two.xml', document)])
        self.assertTrue(result['problems'])

    def test_nested_aggregate_does_not_double_count(self):
        a = suite([('example.UnitChecks', 'one', 'passed')])
        b = suite([('example.DeviceChecks', 'two', 'passed')])
        document = '<testsuites tests="2" failures="0" errors="0" skipped="0">' + a + b + '</testsuites>'
        result = verification.collect_junit([self.xml('nested.xml', document)])
        self.assertEqual(2, result['tests'])
        self.assertEqual(2, result['passed'])
        self.assertFalse(result['problems'])

    def test_testcase_classnames_win_over_suite_name(self):
        cases = [('example.UiChecks', 'screen', 'passed'), ('example.PersistenceChecks', 'restore', 'passed')]
        result = verification.collect_junit([self.xml('device.xml', suite(cases, name='example.UiChecks'))])
        self.assertEqual({('example.UiChecks', 'screen'), ('example.PersistenceChecks', 'restore')},
                         {(c['classname'], c['name']) for c in result['cases']})
        self.assertFalse(result['problems'])


class ProvenanceTests(unittest.TestCase):
    run_id = 'local-regression-123'

    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory(prefix='chemistry verification fixture ')
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.unit_source = write(self.root / 'app/src/test/java/example/UnitChecks.kt', '''package example
import org.junit.Test
class UnitChecks {
    @Test fun conservesAtoms() {}
    fun helper() {}
}
''')
        self.android_source = write(self.root / 'app/src/androidTest/java/example/DeviceChecks.kt', '''package example
import org.junit.Test
class DeviceChecks {
    @Test
    fun opensLesson() {}
    @Test fun restoresProgress() {}
}
''')
        for name in ('build.gradle.kts', 'settings.gradle.kts', 'app/build.gradle.kts', 'gradle.properties'):
            write(self.root / name, '// regression build input\n')
        self.git('init', '--quiet')
        self.git('add', 'app/src', 'build.gradle.kts', 'settings.gradle.kts', 'app/build.gradle.kts', 'gradle.properties')
        self.git('commit', '--quiet', '-m', 'Fixture source')
        self.apk = self.root / 'app/build/outputs/apk/debug/app-debug.apk'
        self.unit_xml = self.root / 'app/build/test-results/testDebugUnitTest/TEST-unit.xml'
        self.android_xml = self.root / 'app/build/outputs/androidTest-results/connected/debug/TEST-device.xml'
        self.lint_xml = self.root / 'app/build/reports/lint-results-debug.xml'
        self.evidence = self.root / 'build/verification'
        verification.begin(self.root, self.run_id)
        self.apk.parent.mkdir(parents=True, exist_ok=True)
        self.apk.write_bytes(b'verification fixture bytes, not a real Android APK')
        self.write_results()

    def git(self, *args):
        return subprocess.run(['git', '-c', 'user.name=Verification Regression',
                               '-c', 'user.email=verification@example.invalid',
                               '-c', 'commit.gpgsign=false', *args], cwd=self.root,
                              capture_output=True, text=True, check=True).stdout.strip()

    def write_results(self, android_status='passed'):
        write(self.unit_xml, suite([('example.UnitChecks', 'conservesAtoms', 'passed')]))
        write(self.android_xml, suite([('example.DeviceChecks', 'opensLesson', android_status),
                                       ('example.DeviceChecks', 'restoresProgress', 'passed')]))
        write(self.lint_xml, '<?xml version="1.0"?><issues format="6"/>')

    def generate(self, build='success', android='success'):
        return verification.generate(self.root, self.run_id, build, android, 'Android API 24 regression fixture')

    def problems(self, run_id=None):
        return verification.validate(self.root, self.evidence, self.apk, run_id or self.run_id)

    def test_expected_tests_use_real_annotated_methods(self):
        self.assertEqual({('example.UnitChecks', 'conservesAtoms')}, verification.expected_tests(self.root, 'jvm'))
        self.assertEqual({('example.DeviceChecks', 'opensLesson'), ('example.DeviceChecks', 'restoresProgress')},
                         verification.expected_tests(self.root, 'android'))
        with tempfile.TemporaryDirectory() as empty:
            self.assertEqual(set(), verification.expected_tests(Path(empty), 'android'))

    def test_source_digest_tracks_code_and_build_inputs_not_report_output(self):
        original = verification.source_digest(self.root)
        write(self.root / 'docs/VERIFICATION.md', 'Generated report must not invalidate tested code.\n')
        self.assertEqual(original, verification.source_digest(self.root))
        self.unit_source.write_text(self.unit_source.read_text() + '\n// source modification\n')
        self.assertNotEqual(original, verification.source_digest(self.root))
        source_digest = verification.source_digest(self.root)
        write(self.root / 'app/build.gradle.kts', '// changed build configuration\n')
        self.assertNotEqual(source_digest, verification.source_digest(self.root))

    def test_begin_clears_generated_results_but_preserves_historical_evidence(self):
        history = write(self.root / 'docs/evidence/historical.xml', 'retained historical evidence')
        verification.begin(self.root, 'new-run')
        self.assertFalse(self.unit_xml.exists())
        self.assertFalse(self.android_xml.exists())
        self.assertFalse(self.lint_xml.exists())
        self.assertTrue(self.apk.exists(), "Beginning a run must not delete unrelated build artifacts")
        self.assertEqual('retained historical evidence', history.read_text())
        self.assertTrue((self.evidence / 'context.json').is_file())

    def test_complete_current_run_generates_valid_report(self):
        self.generate()
        self.assertTrue((self.evidence / 'verification.json').is_file())
        self.assertTrue((self.evidence / 'VERIFICATION.md').is_file())
        self.assertFalse(self.problems(), self.problems())

    def test_missing_android_results_are_not_inferred_from_success_exit(self):
        self.android_xml.unlink()
        self.generate()
        self.assertTrue(self.problems())

    def test_zero_android_cases_are_not_a_pass(self):
        write(self.android_xml, suite([]))
        self.generate()
        self.assertTrue(self.problems())

    def test_partial_android_coverage_is_not_complete_verification(self):
        write(self.android_xml, suite([('example.DeviceChecks', 'opensLesson', 'passed')]))
        self.generate()
        self.assertTrue(self.problems())

    def test_failed_errored_or_skipped_android_cases_block_final_packaging(self):
        for status in ('failure', 'error', 'skipped'):
            with self.subTest(status=status):
                self.write_results(status)
                self.generate()
                self.assertTrue(self.problems())

    def test_failed_process_outcome_cannot_be_overridden_by_green_xml(self):
        for build, android in [('failure', 'success'), ('success', 'failure'), ('success', 'skipped')]:
            with self.subTest(build=build, android=android):
                self.generate(build, android)
                self.assertTrue(self.problems())

    def test_source_change_after_execution_invalidates_evidence(self):
        self.generate()
        self.unit_source.write_text(self.unit_source.read_text() + '\n// changed after run\n')
        self.assertTrue(self.problems())

    def test_apk_change_after_execution_invalidates_evidence(self):
        self.generate()
        self.apk.write_bytes(b'different APK')
        self.assertTrue(self.problems())

    def test_wrong_run_id_is_rejected(self):
        self.generate()
        self.assertTrue(self.problems('another-run'))

    def test_new_git_commit_invalidates_previous_commit_provenance(self):
        self.generate()
        write(self.root / 'README.md', 'A new source revision.\n')
        self.git('add', 'README.md')
        self.git('commit', '--quiet', '-m', 'New revision')
        self.assertTrue(self.problems())

    def test_tampered_report_and_xml_evidence_are_rejected(self):
        self.generate()
        report = self.evidence / 'VERIFICATION.md'
        report.write_text(report.read_text() + '\nUnrecorded report edit.\n')
        self.assertTrue(self.problems())
        self.generate()
        xmls = list((self.evidence / 'evidence').rglob('*.xml'))
        self.assertTrue(xmls, 'Generator must retain raw XML evidence')
        xmls[0].write_text(xmls[0].read_text() + '\n<!-- unrecorded edit -->\n')
        self.assertTrue(self.problems())

    def test_missing_lint_evidence_blocks_final_packaging(self):
        self.lint_xml.unlink()
        self.generate()
        self.assertTrue(self.problems())

    def test_unrelated_well_formed_xml_is_not_a_clean_lint_report(self):
        write(self.lint_xml, '<not-a-lint-report/>')
        self.generate()
        self.assertTrue(self.problems())

    def test_wrong_json_record_shapes_fail_closed_with_diagnostics(self):
        self.generate()
        for filename in ('verification.json', 'context.json'):
            path = self.evidence / filename
            original = path.read_text()
            for value in (None, [], 7, 'unexpected scalar'):
                with self.subTest(filename=filename, value=value):
                    path.write_text(json.dumps(value))
                    try:
                        self.assertTrue(self.problems())
                    finally:
                        path.write_text(original)

    def test_empty_test_sources_cannot_define_successful_zero_coverage(self):
        self.android_source.unlink()
        self.generate()
        self.assertTrue(self.problems())


class DeviceHelperTests(unittest.TestCase):
    """Exercise the CI shell boundary without invoking Gradle, SDK tools or Android."""
    def run_helper(self, gradle=0, signer=0, explicit_gradle=True):
        with tempfile.TemporaryDirectory(prefix='chemistry helper fixture ') as temporary:
            root = Path(temporary) / 'project with spaces'
            helper = root / 'scripts/run-device-verification.sh'
            helper.parent.mkdir(parents=True)
            shutil.copy2(Path(__file__).with_name('run-device-verification.sh'), helper)
            sdk = Path(temporary) / 'fake sdk'
            tools = sdk / 'build-tools/35.0.0'
            tools.mkdir(parents=True)
            calls = Path(temporary) / 'calls.txt'
            write(root / 'app/build/outputs/apk/debug/app-debug.apk', 'fixture, not a real APK')

            def executable(path, label, status):
                write(path, '#!/usr/bin/env bash\nset -eu\n'
                      '[[ "$PWD" == "$CHEMISTRY_HELPER_PROJECT" ]] || exit 97\n'
                      'printf "%s\\n" "' + label + '" >> "$CHEMISTRY_HELPER_CALL_LOG"\n'
                      'printf "%s\\n" "synthetic ' + label + ' output"\n'
                      'exit ' + str(status) + '\n')
                path.chmod(0o755)

            executable(root / 'gradlew', 'gradle', gradle)
            executable(tools / 'apksigner', 'apksigner', signer)
            executable(tools / 'zipalign', 'zipalign', 0)
            executable(tools / 'aapt', 'aapt', 0)
            environment = {**os.environ, 'ANDROID_HOME': str(sdk),
                           'CHEMISTRY_HELPER_PROJECT': str(root),
                           'CHEMISTRY_HELPER_CALL_LOG': str(calls)}
            environment.pop('CHEMISTRY_GRADLE_BIN', None)
            if explicit_gradle:
                environment['CHEMISTRY_GRADLE_BIN'] = str(root / 'gradlew')
            result = subprocess.run(['bash', str(helper)], cwd=temporary, env=environment,
                                    capture_output=True, text=True)
            called = calls.read_text().splitlines() if calls.exists() else []
            metadata = root / 'app/build/reports/apk-metadata.txt'
            return result.returncode, called, metadata.exists()

    def test_gradle_failure_stops_before_signer_even_when_sdk_tools_would_succeed(self):
        code, called, metadata = self.run_helper(gradle=23)
        self.assertEqual(23, code)
        self.assertEqual(['gradle'], called)
        self.assertFalse(metadata)

    def test_signer_failure_stops_alignment_and_metadata(self):
        code, called, metadata = self.run_helper(signer=19)
        self.assertEqual(19, code)
        self.assertEqual(['gradle', 'apksigner'], called)
        self.assertFalse(metadata)

    def test_successful_default_wrapper_invokes_every_check_in_order(self):
        code, called, metadata = self.run_helper(explicit_gradle=False)
        self.assertEqual(0, code)
        self.assertEqual(['gradle', 'apksigner', 'zipalign', 'aapt'], called)
        self.assertTrue(metadata)


if __name__ == '__main__':
    unittest.main(verbosity=2)
