# البناء والتسليم

المشروع يستخدم Kotlin 2.0.21 وAndroid Gradle Plugin 8.6.1 وGradle 8.10.2، مع Compose BOM 2024.12.01. يتطلب JDK 17 وAndroid SDK Platform 35 وBuild Tools 35.0.0. الحد الأدنى Android 7.0 (API 24)، والهدف API 35. لا يحتاج التطبيق إلى اتصال بالإنترنت لتصفح المحتوى.

## البناء من المصدر

عرّف `JAVA_HOME` و`ANDROID_HOME` إلى تثبيتي JDK وSDK المحليين، ثم شغّل من جذر `source`:

```sh
./gradlew --no-daemon testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
python3 scripts/test-installer.py
```

يحمل Gradle Wrapper التوزيع المثبّت بالإصدار 8.10.2 ويتحقق من SHA-256 المنشور رسميًا:

`31c55713e40233a8303827ceb42ca48a47267a0ad4bab9177123121e71524c26`

تم أيضًا مطابقة `gradle-wrapper.jar` مع بصمته الرسمية: `2db75c40782f5e8ba1fc278a5574bab070adccb2d21ca5a6e5ed840888448046`. مصادر التحقق: [توزيع Gradle](https://services.gradle.org/distributions/gradle-8.10.2-bin.zip.sha256)، [Wrapper](https://services.gradle.org/distributions/gradle-8.10.2-wrapper.jar.sha256).

يحتاج البناء الأول إلى تنزيل الاعتمادات من Google Maven وMaven Central وGradle؛ تثبيت الحزمة الجاهزة في الهاتف لا يبني المشروع ولا يحتاج إلى JDK أو SDK في Termux. بناء تطبيق Android الأصلي داخل Termux نفسه ليس جزءًا من أمر التثبيت.

## حزمة واحدة

بعد تنفيذ جولة التحقق وتوليد دليلها الحالي كما في [VERIFICATION_PIPELINE.md](VERIFICATION_PIPELINE.md)، مع إبقاء `VERIFICATION_RUN_ID` نفسه:

```sh
python3 scripts/package-final.py
```

ينتج `dist/chemistry-lab-4.0.0-final.zip` ويضم APK المثبت، نسخة المصدر دون أدوات البيئة أو مخازن المفاتيح، تقرير التحقق، `SHA256SUMS` وأداة التثبيت. يتحقق `scripts/install-termux.sh` من بصمة APK ويرفض الملف المتغير أو سجل البصمة المكرر أو المفقود، ثم يفتح مثبّت Android. Android يحتاج إلى موافقة المستخدم ومنح Termux إذن تثبيت التطبيقات؛ لا يستطيع الأمر تجاوز ذلك. بصمة SHA-256 تكشف تغير الملفات وليست توقيع ناشر مستقلًا.

## التوقيع والتحديث

APK التسليم مبني بنوع `debug` وموقّع بمفتاح بيئة البناء، وليس مفتاح نشر محفوظًا للمشروع. لا تتضمن الحزمة المفتاح الخاص. معرّف التطبيق `org.arabicchemistry.lab.nativeedition`، والإصدار 4.0.0 ورقم البناء 2. إذا كان إصدار سابق من هذا المعرف موقّعًا بمفتاح مختلف، سيرفض Android التحديث. إزالة التطبيق تحذف تقدّم الدروس والمفضلة والإعدادات المحلية؛ لا تتضمن هذه النسخة وظيفة تصديرها. للاحتفاظ بالبيانات عند التحديث، أعد بناء التطبيق بنفس مفتاح النسخة المثبّتة الذي تملكه. إصدارات GitHub Actions الجديدة لا تضمن نفس مفتاح debug؛ التوزيع الإنتاجي القابل للتحديث يحتاج إدارة مفتاح إصدار ثابت خارج المستودع.

لا تُعد نتيجة تجميع اختبارات Android تشغيلًا لها على جهاز. راجع تقرير التحقق لمعرفة الاختبارات التي شُغلت فعلًا وأي قيود أجهزة أو محاكيات.

---

Build requirements: JDK 17, Android SDK 35 / Build Tools 35.0.0, Gradle 8.10.2 (verified wrapper). Run the commands above from `source`. The final ZIP includes the signed debug APK, sources, checksums, installer, and actual verification record. Termux opens Android's package installer after checksum verification; the user must confirm installation. Production release signing and durable upgrade keys remain deployment responsibilities.

تشغيل GitHub Actions بعد رفع هذا التغيير سيستخدم محاكي API 24 مع KVM، ويرفع تقارير الاختبارات حتى عند الإخفاق. لم يُدّع تشغيل هذه النسخة من workflow في GitHub خلال التحقق المحلي. لإرفاق نسخة Git احتياطية كاملة بالحزمة: `python3 scripts/package-final.py --git-bundle /path/to/checkpoint.bundle`.

فشل تجهيز الحزمة عند غياب `build/verification/verification.json` أو اختلاف الالتزام/المصدر/APK/معرّف الجولة مقصود. لا تحذف شرط التقرير المعلّق؛ شغّل مراحل توليد الدليل. التقرير داخل الحزمة يولد من XML الجولة الحالية، ولا تستخدم نتائج السجل المحلي التاريخي مكانها.
