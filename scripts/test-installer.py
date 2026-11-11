#!/usr/bin/env python3
"""Verify the installer refuses tampered artifacts and handles paths with spaces."""
import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import tempfile

script = Path(__file__).with_name('install-termux.sh')
with tempfile.TemporaryDirectory(prefix='chemistry installer ') as temp:
    root = Path(temp) / 'package with spaces'
    root.mkdir()
    shutil.copy2(script, root / 'install-termux.sh')
    apk = root / 'chemistry-lab-4.0.0.apk'
    apk.write_bytes(b'installer test fixture, not an APK')
    checksum = hashlib.sha256(apk.read_bytes()).hexdigest()
    manifest = root / 'SHA256SUMS'
    manifest.write_text(f'{checksum}  {apk.name}\n', encoding='utf-8')
    env = {**os.environ, 'CHEMISTRY_VERIFY_ONLY': '1'}
    def run(expected):
        result = subprocess.run(['bash', str(root / 'install-termux.sh')], env=env, capture_output=True, text=True)
        assert (result.returncode == 0) == expected, result.stdout + result.stderr
    run(True)
    apk.write_bytes(b'tampered')
    run(False)
    apk.write_bytes(b'installer test fixture, not an APK')
    manifest.write_text(f'{checksum}  {apk.name}\n{checksum}  {apk.name}\n', encoding='utf-8')
    run(False)
    manifest.write_text(f'{checksum}  ../outside.apk\n', encoding='utf-8')
    run(False)
    manifest.unlink()
    run(False)
print('Installer verification passed: valid file, spaces, tamper, duplicate, path mismatch, missing manifest.')
