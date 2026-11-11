# بيانات الجدول الدوري / Periodic-table data

يتضمن `app/src/main/assets/elements.json` العناصر الـ118، مرتبة بالعدد الذري. لكل عنصر اسم عربي وإنجليزي، رمز، دورة ومجموعة، عائلة لونية، قطاع، حالة فيزيائية ووصف موجز باللغتين، ومراجع.

## الكتل والأوزان

- الأوزان الذرية المعيارية المختصرة وأوجه عدم اليقين مأخوذة من **CIAAW Abridged Standard Atomic Weights 2024**، بما فيها تحديثات الزركونيوم والغادولينيوم واللوتيتيوم. جرى الاطلاع على الجدول في 11 نوفمبر 2026: https://ciaaw.org/abridged-atomic-weights.htm
- `massKind: "standard"`: `mass` قيمة الوزن الذري النسبي المعياري المختصر **دون وحدة**، و`massUncertainty` قيمة عدم اليقين المنشورة. هذه ليست كتلة جزيء، ولا وزنًا ثابتًا لكل عينة؛ قد يختلف التركيب النظائري. القيم مختصرة لأغراض التعليم، وليست بديلًا عن بيانات مرجعية لتحليل عالي الدقة.
- `massKind: "isotope"`: لا يملك العنصر وزنًا ذريًا معياريًا. القيمة بين الأقواس المربعة، مثل `[97]` للتكنيشيوم، **عدد كتلي لنظير مختار** من جدول IUPAC المؤرخ 4 مايو 2022، وليست كتلة ذرية مقاسة ولا متوسطًا نظائريًا. لا ينبغي إلحاق `u` بهذه القيمة. المرجع: https://iupac.org/wp-content/uploads/2022/05/IUPAC_Periodic_Table-04May22.pdf
- لا تعني القيمة المربعة أنها نظير مستقر؛ فهي لقطة مرجعية من إصدار 2022، وقد تتغير معلومات الأعمار النصفية والنظائر مستقبلًا. تتبع البيانات `[262]` للورنسيوم كما في ذلك الإصدار، دون مزجها بقيم من جداول أخرى.
- `massSourceUrl` يحدد مصدر قيمة الكتلة في كل سجل. `sourceUrl` رابط صفحة RSC للعنصر للمراجعة العلمية العامة. استخرجت الروابط الـ118 من فهرس RSC الحالي، لأن الروابط القديمة على `www.rsc.org/periodic-table/element/...` تعيد التوجيه إلى الصفحة الرئيسية.

## ترتيب الجدول وتصنيفاته

- الدورات 1–7 والمجموعات الرئيسية 1–18.
- تعرض السلسلتان 57–71 و89–103 في صفين منفصلين، مع موضع بديل في العمود الثالث للدورتين السادسة والسابعة. `group: 3` لجميع سجلات السلسلتين **قيمة تخطيطية داخل هذا التطبيق**، وليست إقرارًا بأن جميع هذه العناصر أعضاء في المجموعة 3. ينبغي عرض السلسلة بدل رقم المجموعة لهذه السجلات.
- `block: "f"` لهذه السلاسل اصطلاح عرض تعليمي؛ لا يصف التوزيع الإلكتروني الأرضي لكل ذرة، ولا يحسم خلافات موضع المجموعة 3. لم تُدرج توزيعات إلكترونية تخمينية.
- العائلات اللونية تصنيف تعليمي؛ حدود أشباه الفلزات و«الفلزات بعد الانتقالية» تختلف بين المراجع. صُنّف الزنك والكادميوم والزئبق والكوبرنيسيوم مع الفلزات بعد الانتقالية تجنبًا لمساواة جميع عناصر القطاع d بالتعريف الإلكتروني الدقيق للعنصر الانتقالي. تصنيف العناصر فائقة الثقل يعتمد موضعها الدوري ولا يضمن مشابهة خواصها للعناصر الأخف.

## الحالة الفيزيائية وحدود المحتوى

- الحالة المشار إليها عند **20 °C وضغط يقارب 1 atm**. حالتا البروم والزئبق سائلتان، والغاليوم صلب في هذه الدرجة مع انصهاره قرب درجة حرارة الغرفة.
- لم تُحوّل تنبؤات الحالة الفيزيائية للأستاتين والفرانسيوم والعناصر 100–118 إلى حقائق تجريبية؛ يعرض السجل أن الحالة لم تُحدد تجريبيًا في الظروف القياسية. هذا اختيار محافظ بسبب غياب العينات الكتلية أو محدوديتها، ولا ينفي وجود تنبؤات نظرية.
- الأوصاف نصوص تعليمية أصلية موجزة، لا إجراءات تشغيل أو معالجة أو استخلاص. روجعت الحقائق المعروفة مع إطار صفحات Royal Society of Chemistry: https://periodic-table.rsc.org/
- لا توجد ادعاءات عن مواقع وجود أي عنصر في اليمن بلا مسح جيولوجي أولي موثق. لا تدل خواص العنصر الحر على خواص جميع مركباته، ولا يكفي لون الخام أو لمعانه لإثبات مكوناته.
- لم تدرج بيانات كثافة أو درجات انصهار أو كهروسالبية أو حالات أكسدة كاملة لكل عنصر؛ ينبغي إضافة هذه الحقول مستقبلًا فقط مع تعريف شروط القياس والمصدر وعدم اليقين.

## التحقق

نفذ من جذر المشروع:

```sh
python3 tools/validate_elements.py
```

يفحص العدد والتسلسل والرموز الفريدة، الحقول الثنائية اللغة، حدود الدورات والمجموعات، أنواع الكتل وعدم اليقين، ومواقع عناصر مرجعية وحالاتها. هذا فحص بنيوي مع اختبارات دقة مرجعية محددة، ولا يحل محل مراجعة علمية مستقلة لجميع النصوص.

## English data notes

The asset contains all 118 elements with original Arabic/English educational descriptions. Abridged standard atomic weights and uncertainties follow CIAAW 2024; bracketed isotope mass numbers follow the IUPAC 4 May 2022 table. Standard atomic weights are dimensionless; bracketed values are isotope mass numbers, not atomic masses in `u`.

Group 3 and block `f` for the detached lanthanide/actinide rows are layout conventions, not complete ground-state electronic configurations or a resolution of group-3 placement. Display the series name rather than the group number for those entries. Family colours are teaching conventions. Physical states refer to 20 °C and approximately 1 atm; unestablished bulk states are explicitly marked. No unsupported element-occurrence claims are made for Yemen. Properties of free elements must not be applied indiscriminately to their compounds.

Numerical factual data are attributed above. The copyrighted IUPAC PDF is linked, not bundled or reproduced. No third-party periodic-table dataset or its prose was copied.
