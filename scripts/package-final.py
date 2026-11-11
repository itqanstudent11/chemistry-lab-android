#!/usr/bin/env python3
"""Create one self-contained delivery ZIP. Run only after verification completes."""
import argparse
import hashlib
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
NAME = 'chemistry-lab-4.0.0'
SKIP = {'.git', '.gradle', '.kotlin', '.android', 'build', 'dist', '.idea', '.work', '__pycache__', 'screenshots'}


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--apk', type=Path, default=ROOT / 'app/build/outputs/apk/debug/app-debug.apk')
    parser.add_argument('--output', type=Path, default=ROOT / 'dist' / f'{NAME}-final.zip')
    args = parser.parse_args()
    if not args.apk.is_file():
        parser.error(f'APK missing: {args.apk}')
    report = ROOT / 'docs/VERIFICATION.md'
    if not report.is_file():
        parser.error('docs/VERIFICATION.md must record the final checks and limitations first.')
    report_text = report.read_text(encoding='utf-8')
    if any(pending in report_text for pending in ('قيد التحقق على محاكي', 'يجب استبدال هذه الفقرة')):
        parser.error('Verification report still contains a pending Android test placeholder.')
    with tempfile.TemporaryDirectory(prefix='chemistry-package-') as temporary:
        package = Path(temporary) / NAME
        source = package / 'source'
        source.mkdir(parents=True)
        for file in sorted(ROOT.rglob('*')):
            rel = file.relative_to(ROOT)
            if not file.is_file() or file.is_symlink() or any(p in SKIP for p in rel.parts):
                continue
            if file.name in {'local.properties', '.DS_Store'} or file.suffix.lower() in {'.jks', '.keystore', '.apk', '.zip', '.log', '.pyc'}:
                continue
            target = source / rel
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(file, target)
        shutil.copy2(args.apk, package / f'{NAME}.apk')
        shutil.copy2(ROOT / 'scripts/install-termux.sh', package / 'install-termux.sh')
        shutil.copy2(ROOT / 'README.md', package / 'README.md')
        shutil.copytree(source / 'docs', package / 'docs')
        shutil.copy2(report, package / 'VERIFICATION.md')
        files = sorted(p for p in package.rglob('*') if p.is_file())
        (package / 'SHA256SUMS').write_text(''.join(f'{sha256(p)}  {p.relative_to(package).as_posix()}\n' for p in files), encoding='utf-8')
        subprocess.run(['bash', str(package / 'install-termux.sh')], check=True, env={**__import__('os').environ, 'CHEMISTRY_VERIFY_ONLY': '1'})
        args.output.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(args.output, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
            for file in sorted(package.rglob('*')):
                if file.is_file():
                    archive.write(file, file.relative_to(package.parent))
        print(args.output.resolve())
        print(f'SHA256: {sha256(args.output)}')


if __name__ == '__main__':
    main()
