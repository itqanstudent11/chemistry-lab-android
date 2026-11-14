"use strict";

window.Phase2Modules = {
  mount(title, content) {
    const app = document.getElementById("app");
    app.innerHTML = `
      <section class="phase2-menu" dir="rtl">
        <button class="phase2-card" id="module-back">
          ↩ العودة إلى الأقسام
        </button>
        <h2 class="phase2-title">${title}</h2>
        ${content}
      </section>
    `;
    document.getElementById("module-back").onclick = () => {
      document.getElementById("phase2-open").click();
    };
  },

  basics() {
    this.mount("⚛️ أساسيات الكيمياء", `
      <div class="phase2-card">
        <h3>الدرس الأول: المادة والذرة</h3>
        <p>المادة هي كل ما له كتلة ويشغل حيزًا من الفراغ.</p>
        <p>تتكون الذرة من نواة تحتوي على بروتونات ونيوترونات،
        وتحيط بها إلكترونات.</p>
        <h3>الدرس الثاني: العناصر والمركبات</h3>
        <p>العنصر يتكون من نوع واحد من الذرات،
        أما المركب فيتكون من عنصرين مختلفين أو أكثر
        مرتبطين كيميائيًا.</p>
        <h3>اختبر معلوماتك</h3>
        <p>ما الجسيم الذي يحمل شحنة سالبة؟</p>
        <button class="phase2-card" data-answer="0">البروتون</button>
        <button class="phase2-card" data-answer="1">الإلكترون</button>
        <button class="phase2-card" data-answer="0">النيوترون</button>
        <p id="basic-result" aria-live="polite"></p>
      </div>
    `);

    document.querySelectorAll("[data-answer]").forEach(btn => {
      btn.onclick = () => {
        document.getElementById("basic-result").textContent =
          btn.dataset.answer === "1"
          ? "✅ إجابة صحيحة! الإلكترون سالب الشحنة."
          : "❌ حاول مرة أخرى.";
      };
    });
  },

  minerals() {
    const minerals = [
      ["الكوارتز", "SiO₂", "7", "صلادة 7، بريق زجاجي"],
      ["الكالسيت", "CaCO₃", "3", "يتفاعل مع الأحماض المخففة"],
      ["البيريت", "FeS₂", "6–6.5", "لون أصفر نحاسي وبريق معدني"],
      ["الذهب", "Au", "2.5–3", "كثافة مرتفعة وقابل للطرق"]
    ];

    this.mount("💎 علم المعادن", `
      <div class="phase2-grid">
        ${minerals.map(m => `
          <article class="phase2-card">
            <h3>${m[0]}</h3>
            <p>الصيغة: ${m[1]}</p>
            <p>صلادة موهس: ${m[2]}</p>
            <p>${m[3]}</p>
          </article>
        `).join("")}
      </div>
      <p>ملاحظة: التعرف النهائي على الخام قد يتطلب
      فحوصًا مخبرية متخصصة.</p>
    `);
  },

  gold() {
    this.mount("🟡 مختبر تحليل الذهب", `
      <div class="phase2-card">
        <h3>حاسبة محتوى الذهب والاسترجاع</h3>
        <label>وزن الخام بالكيلوجرام</label>
        <input id="ore-mass" type="number" value="100"
          min="0" step="any">
        <label>تركيز الذهب في الخام (جرام/طن)</label>
        <input id="ore-grade" type="number" value="5"
          min="0" step="any">
        <label>الذهب المسترجع بالجرام</label>
        <input id="gold-recovered" type="number" value="0.2"
          min="0" step="any">
        <div id="gold-result" aria-live="polite"></div>
      </div>
      <p>هذه حاسبة تعليمية للميزان الكتلي،
      وليست تعليمات لاستخدام مواد استخلاص سامة.</p>
    `);

    const inputs = ["ore-mass", "ore-grade", "gold-recovered"]
      .map(id => document.getElementById(id));

    const update = () => {
      const values = inputs.map(el => Number(el.value));

      if (inputs.some(el => el.value.trim() === "") ||
          values.some(v => !Number.isFinite(v) || v < 0)) {
        document.getElementById("gold-result").textContent =
          "⚠️ أدخل أرقامًا غير سالبة وصحيحة.";
        return;
      }

      const [kg, grade, recovered] = values;
      const theoretical = kg * grade / 1000;
      const recovery = theoretical > 0
        ? recovered / theoretical * 100 : 0;

      document.getElementById("gold-result").innerHTML = `
        <h3>النتائج</h3>
        <p>الذهب النظري: ${theoretical.toFixed(4)} جرام</p>
        <p>نسبة الاسترجاع: ${recovery.toFixed(2)}%</p>
        <p>${recovered > theoretical
          ? "⚠️ المسترجع أكبر من المحتوى النظري؛ راجع البيانات."
          : "✅ الحساب مكتمل."}</p>
      `;
    };

    inputs.forEach(el => el.addEventListener("input", update));
    update();
  },

  simulation() {
    this.mount("🔬 المحاكاة التفاعلية", `
      <div class="phase2-card">
        <h3>محاكاة تخفيف محلول</h3>
        <p>تعتمد على العلاقة C₁V₁ = C₂V₂.</p>
        <label>التركيز الابتدائي (مول/لتر)</label>
        <input id="sim-c1" type="number" value="1"
          min="0" step="any">
        <label>الحجم الابتدائي (مل)</label>
        <input id="sim-v1" type="number" value="100"
          min="0" step="any">
        <label>الحجم النهائي (مل)</label>
        <input id="sim-v2" type="number" value="500"
          min="0" step="any">
        <div id="sim-result" aria-live="polite"></div>
      </div>
    `);

    const inputs = ["sim-c1", "sim-v1", "sim-v2"]
      .map(id => document.getElementById(id));

    const update = () => {
      const [c1, v1, v2] = inputs.map(el => Number(el.value));
      const result = document.getElementById("sim-result");

      if (inputs.some(el => el.value.trim() === "") ||
          [c1, v1, v2].some(v => !Number.isFinite(v)) ||
          c1 < 0 || v1 <= 0 || v2 < v1) {
        result.textContent =
          "⚠️ يجب أن يكون الحجم النهائي أكبر من أو يساوي الابتدائي.";
        return;
      }

      const c2 = c1 * v1 / v2;
      result.innerHTML = `
        <h3>التركيز النهائي: ${c2.toFixed(4)} مول/لتر</h3>
        <p>المحلول أصبح أكثر تخفيفًا أو بقي كما هو.</p>
        <div style="height:160px;display:flex;
          align-items:end;justify-content:center">
          <div style="width:90px;height:140px;
            border:3px solid #38bdf8;border-top:0;
            border-radius:0 0 16px 16px;
            display:flex;align-items:end;overflow:hidden">
            <div style="width:100%;
              height:${Math.max(5, Math.min(100, c1 ? c2 / c1 * 100 : 5))}%;
              background:linear-gradient(#67e8f9,#0284c7);
              transition:height .4s"></div>
          </div>
        </div>
      `;
    };

    inputs.forEach(el => el.addEventListener("input", update));
    update();
  }
};
