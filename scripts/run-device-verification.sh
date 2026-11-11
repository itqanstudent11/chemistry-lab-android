#!/usr/bin/env bash
# One failure in device execution or APK validation must fail the whole CI step.
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")/.."
chemistry_gradle="${CHEMISTRY_GRADLE_BIN:-./gradlew}"
"$chemistry_gradle" --no-daemon connectedDebugAndroidTest
chemistry_sdk="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
[[ -n "$chemistry_sdk" ]] || { echo 'Android SDK location is required.' >&2; exit 1; }
chemistry_tools="$chemistry_sdk/build-tools/35.0.0"
mkdir -p app/build/reports
"$chemistry_tools/apksigner" verify --verbose --print-certs app/build/outputs/apk/debug/app-debug.apk > app/build/reports/apk-signature.txt
"$chemistry_tools/zipalign" -c -P 16 4 app/build/outputs/apk/debug/app-debug.apk >> app/build/reports/apk-signature.txt
"$chemistry_tools/aapt" dump badging app/build/outputs/apk/debug/app-debug.apk > app/build/reports/apk-metadata.txt
