# Verification evidence for each execution

Actions run #10 built commit `c053d9b`, whose checked-in report still contained an explicit Android-test placeholder. Packaging correctly rejected it. The additional root cause was that the workflow had no producer for a current-run report: removing the placeholder would still let old local test counts, APK hashes and signer details ship as CI evidence.

The workflow now executes this sequence:

1. `verification.py begin` records a unique run ID, HEAD commit, a digest of application/test/build inputs and the UTC start time. It clears only generated JUnit/Lint evidence, preserving application sources, APKs and historical evidence. Build caching is disabled for the build check so restored old XML cannot substitute for this execution.
2. Content/installer checks and verification regression tests run. Gradle builds the APK/test APK, runs JVM tests and performs Lint.
3. The API 24 emulator runs all Android tests. A Bash helper propagates any Gradle/runner/signature/alignment failure; signer and package metadata are captured after device execution, which may repackage the APK.
4. `verification.py report` runs with `if: always()`, reading actual JUnit testcase entries, not prose or a Gradle assembly result. It records the original command outcomes. Missing, malformed, failed, skipped, zero-case or partial results produce an explicit `not_verified` report. Android success is never inferred from `assembleDebugAndroidTest`.
5. Diagnostics are uploaded with `if: always()`. Final packaging remains gated on a successful job AND complete validated evidence. There is no continue-on-error path that manufactures a successful final bundle.

`build/verification/` contains `context.json`, `verification.json`, `VERIFICATION.md` and raw `evidence/` files. XML testcase identities must exactly match the current Kotlin `@Test fun` methods in the app's test source sets (currently 12 JVM and 6 Android cases). Failure/error/skip counts, declared XML totals, duplicate cases, wrong Lint XML roots and missing reports block final packaging. The current source layout uses one top-level test class per Kotlin file; changes to this layout require updating the discovery parser and its regression tests.

`package-final.py` retains the explicit placeholder guard. It additionally checks run ID, source commit/input digest, APK SHA-256, report SHA-256 and raw evidence hashes, then reparses the XML and coverage. Old reports cannot qualify a changed APK or another CI execution. The primary report and evidence inside the ZIP come from the current run; original source documentation remains under `source/` as historical context.

## Local execution

Prepare JDK 17, SDK platform/build tools 35.0.0 and a working authorized device/emulator (API 24 is used in CI). With `ANDROID_HOME` set:

```sh
export VERIFICATION_RUN_ID="local-$(date -u +%Y%m%dT%H%M%SZ)"
python3 scripts/verification.py begin --run-id "$VERIFICATION_RUN_ID"
python3 scripts/test-verification.py
python3 scripts/test-installer.py
python3 tools/validate_content.py
./gradlew --no-daemon --no-build-cache testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
bash scripts/run-device-verification.sh
python3 scripts/verification.py report --run-id "$VERIFICATION_RUN_ID" --build-outcome success --android-outcome success --device "Record actual Android API/ABI here"
python3 scripts/package-final.py
```

Only pass `success` for commands that actually returned zero. If a command fails, use its real `failure`/`skipped` outcome in the report command to retain diagnostics. Packaging will reject that incomplete run. A report created before source commits change must be regenerated through a new verification execution; changing the prose or editing hashes is not a valid fix.

No test framework proves all devices or scientific accreditation. Reports explicitly limit claims to the recorded environment. API 35 and physical-device coverage must be listed as absent when they were not successfully executed. A successful local execution is not proof of a successful GitHub Actions run; record the actual remote run URL/conclusion separately.
