import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
assets = root / "app/src/main/assets"

def s(x, k):
    return str(x.get(k, "") or "")

def enrich(x, english=False):
    title = s(x, "title")
    blob = (title + " " + s(x, "intro") + " " + s(x, "detail")).lower()
    gold = "gold" in s(x, "section").lower() or "ذهب" in blob
    if english:
        return {
            "whyMatters": f'This lesson turns “{title}” into a connected explanation that can be checked and applied safely.',
            "keyIdeas": ["Separate observation, measurement, model and interpretation.", "Track variables, units, assumptions and evidence.", "Connect the idea to a previous lesson before memorizing it."],
            "commonMistakes": ["Changing a chemical formula instead of a coefficient.", "Generalizing from one example without checking conditions.", "Treating a visual clue as complete proof."],
            "safePractice": "Study on paper or with safe simulations. Real experiments require a licensed laboratory, supervision and protective equipment.",
            "reflection": ["What evidence would support this idea without being the only evidence?", "Which variable should be measured or controlled?", "What does the model explain and what still requires data?"],
            "connections": ["Connect symbols to the periodic table.", "Use the glossary for new terms.", "Review prerequisites before the quiz."],
            "enrichmentVersion": 1
        }
    if gold:
        why = f"يرتبط درس «{title}» بفهم الذهب داخل سياقه الجيولوجي والتحليلي قبل أي قرار معالجة."
        ideas = ["وجود الكوارتز أو اللون الأصفر لا يثبت وحده وجود خام اقتصادي.", "التوصيف والتمثيل والتحليل المخبري يسبق اختيار أي مسار.", "الفصل الفيزيائي والكيميائي عمليات صناعية تحتاج ترخيصاً وإدارة مخلفات."]
        mistakes = ["الحكم على العينة من اختبار ميداني واحد.", "طحن أو تسخين أو خلط خام مجهول في المنزل.", "اعتبار وجود الذهب دليلاً على الجدوى الاقتصادية."]
    elif any(k in blob for k in ["حمض", "قاعدة", "pH", "التعادل"]):
        why = f"يساعدك درس «{title}» على فهم المحاليل والتعادل والقياس دون حفظ القواعد منفصلة."
        ideas = ["قوة الحمض وتركيزه مفهومان مختلفان.", "الأس الهيدروجيني مقياس لوغاريتمي.", "المؤشر قرينة تعليمية والقياس المهني يحتاج معايرة."]
        mistakes = ["الخلط بين القوة والتركيز.", "اعتبار تغير اللون دليلاً كافياً.", "خلط الأحماض والقواعد خارج مختبر مرخص."]
    elif any(k in blob for k in ["تفاعل", "معادلة", "مول", "متفاعل"]):
        why = f"يحوّل درس «{title}» المعادلات إلى علاقات كمية تحفظ الذرات والشحنة."
        ideas = ["وازن المعادلة قبل الحساب.", "حوّل الكتل والحجوم إلى مولات عند المقارنة.", "ميّز بين المتفاعل المحدد والناتج النظري والفعلي."]
        mistakes = ["تغيير الرقم السفلي في الصيغة.", "إهمال الوحدات.", "اعتبار الناتج النظري كمية مضمونة."]
    else:
        why = f"يشرح درس «{title}» فكرة يمكن ربطها بالملاحظة والقياس والنموذج العلمي."
        ideas = ["افصل بين الملاحظة والتفسير.", "حدد المتغيرات والوحدات والافتراضات.", "اربط المفهوم بدرس سابق ومثال واقعي آمن."]
        mistakes = ["حفظ المصطلح دون فهم العلاقة.", "تعميم مثال واحد على كل الحالات.", "تجاهل شروط التجربة ومصدر البيانات."]
    return {
        "whyMatters": why,
        "keyIdeas": ideas,
        "commonMistakes": mistakes,
        "safePractice": "تعلّم بالمحاكاة أو على الورق. لا تخلط مواد أو تسخّن عينات أو تطحن خامات مجهولة؛ التجربة الفعلية تحتاج مختبراً مرخصاً وإشرافاً ومعدات حماية.",
        "reflection": ["ما الدليل الذي يدعم الفكرة دون أن يكون الدليل الوحيد؟", "ما المتغير الذي يجب قياسه أو تثبيته؟", "ما حدود النموذج أو التفسير؟"],
        "connections": ["اربط الرموز بالجدول الدوري.", "استخدم المعجم للمصطلحات الجديدة.", "راجع المتطلبات السابقة قبل الاختبار."],
        "enrichmentVersion": 1
    }

for name, english in [("lessons.json", False), ("lessons_en.json", True)]:
    p = assets / name
    data = json.loads(p.read_text(encoding="utf-8"))
    for item in data:
        item.update(enrich(item, english))
    p.write_text(json.dumps(data, ensure_ascii=not english, indent=2) + "\n", encoding="utf-8")
    print(name, len(data), "lessons enriched")
