"use strict";

window.Phase2Reactions = {
  questions: [
    {
      equation: "H₂ + O₂ → H₂O",
      answer: [2, 1, 2],
      names: ["H₂", "O₂", "H₂O"]
    },
    {
      equation: "N₂ + H₂ → NH₃",
      answer: [1, 3, 2],
      names: ["N₂", "H₂", "NH₃"]
    },
    {
      equation: "Fe + O₂ → Fe₂O₃",
      answer: [4, 3, 2],
      names: ["Fe", "O₂", "Fe₂O₃"]
    }
  ],

  show(index = 0) {
    const q = this.questions[index];
    const app = document.getElementById("app");

    app.innerHTML = `
      <section class="phase2-menu" dir="rtl">
        <button class="phase2-card" id="reaction-back">
          ↩ العودة إلى الأقسام
        </button>

        <h2 class="phase2-title">🧪 موازنة المعادلات</h2>
        <p>التجربة ${index + 1} من ${this.questions.length}</p>

        <div class="phase2-card">
          <h3>${q.equation}</h3>
          <p>أدخل المعامل أمام كل مادة:</p>

          <div class="phase2-grid">
            ${q.names.map((name, i) => `
              <label>
                <strong>${name}</strong>
                <input type="number"
                  min="1"
                  max="99"
                  value="1"
                  class="reaction-input"
                  data-index="${i}"
                  style="width:80px;padding:12px;
                  border-radius:12px;font-size:20px">
              </label>
            `).join("")}
          </div>

          <button id="reaction-check" class="phase2-card">
            ✅ تحقق من الإجابة
          </button>

          <p id="reaction-result" aria-live="polite"></p>

          <button id="reaction-next" class="phase2-card"
            style="display:none">
            التجربة التالية ←
          </button>
        </div>
      </section>
    `;

    document.getElementById("reaction-back").onclick = () => {
      document.getElementById("phase2-open").click();
    };

    document.getElementById("reaction-check").onclick = () => {
      const values = [...document.querySelectorAll(".reaction-input")]
        .map(input => Number(input.value));

      const correct = values.every(
        (value, i) => value === q.answer[i]
      );

      const result = document.getElementById("reaction-result");

      result.textContent = correct
        ? "🎉 ممتاز! المعادلة موزونة بشكل صحيح."
        : "❌ حاول مجددًا. يجب أن يتساوى عدد ذرات كل عنصر.";

      result.style.color = correct ? "#16a34a" : "#ef4444";

      if (correct) {
        document.getElementById("reaction-next").style.display =
          "block";
      }
    };

    document.getElementById("reaction-next").onclick = () => {
      this.show((index + 1) % this.questions.length);
    };
  }
};
