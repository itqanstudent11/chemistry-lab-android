#!/data/data/com.termux/files/usr/bin/bash
# Run with bash. Android will ask the user to confirm installation.
set -euo pipefail

fail() { printf '%s\n' "$*" >&2; exit 1; }
bundle_dir="$(CDPATH= cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
if [[ $# -gt 1 ]]; then fail 'Usage: bash install-termux.sh [extracted-package-directory]'; fi
if [[ $# -eq 1 ]]; then bundle_dir="$(CDPATH= cd -- "$1" && pwd)"; fi
[[ -f "$bundle_dir/chemistry-lab-4.0.0.apk" ]] || fail 'APK missing. Extract the complete final ZIP first.'
[[ -f "$bundle_dir/SHA256SUMS" ]] || fail 'SHA256SUMS missing. Use the complete final ZIP.'
command -v sha256sum >/dev/null || fail 'Install coreutils in Termux: pkg install coreutils'
cd -- "$bundle_dir"
# Require exactly one checksum for the expected APK; do not trust arbitrary paths.
checksum_line=''
while IFS= read -r entry || [[ -n "$entry" ]]; do
  if [[ "$entry" == *'  chemistry-lab-4.0.0.apk' ]]; then
    [[ -z "$checksum_line" ]] || fail 'Duplicate APK checksum entry.'
    checksum_line="$entry"
  fi
done < SHA256SUMS
[[ "$checksum_line" =~ ^[0-9a-f]{64}[[:space:]][[:space:]]chemistry-lab-4\.0\.0\.apk$ ]] || fail 'Invalid or duplicate APK checksum entry.'
printf '%s\n' "$checksum_line" | sha256sum --check --status - || fail 'APK checksum mismatch. Installation cancelled.'
if [[ "${CHEMISTRY_VERIFY_ONLY:-0}" == 1 ]]; then printf '%s\n' 'APK SHA-256 verified.'; exit 0; fi
command -v termux-open >/dev/null || fail 'APK verified. Install termux-tools (pkg install termux-tools), then run this command again.'
printf '%s\n' 'APK SHA-256 verified. Confirm installation in the Android installer.'
printf '%s\n' 'If Android blocks it, allow Termux to install unknown apps in Android Settings.'
termux-open --view --content-type application/vnd.android.package-archive "$bundle_dir/chemistry-lab-4.0.0.apk"
