"use strict";

window.Phase2Balance = {
  show() {
    const app = document.getElementById("app");

    app.innerHTML = `
      <section class="phase2-menu" dir="rtl">
        <button id="balance-back" class="phase2-card">
          ↩ العودة إلى الأقسام
        </button>

        <h2 class="phase2-title">⚖️ الميزان الكيميائي الرقمي</h2>

        <div class="phase2-card">
          <p>أدخل كتلة المادة بالجرام:</p>
          <input id="balance-mass" type="number"
            min="0" step="any" value="10"
            style="width:100%;padding:14px;
            border-radius:12px;font-size:20px">

          <p>الكتلة المولية (جرام/مول):</p>
          <input id="balance-molar" type="number"
            min="0.000001" step="any" value="18.015"
            style="width:100%;padding:14px;
            border-radius:12px;font-size:20px">

          <h3>📊 نتائج القياس</h3>
          <p id="balance-grams"></p>
          <p id="balance-mg"></p>
          <p id="balance-kg"></p>
          <p id="balance-moles"></p>
          <p id="balance-error" role="status"></p>
        </div>
      </section>
    `;

    document.getElementById("balance-back").onclick = () => {
      document.getElementById("phase2-open").click();
    };

    const massInput = document.getElementById("balance-mass");
    const molarInput = document.getElementById("balance-molar");

    const calculate = () => {
      const mass = Number(massInput.value);
      const molar = Number(molarInput.value);

      if (
        massInput.value.trim() === "" ||
        molarInput.value.trim() === "" ||
        !Number.isFinite(mass) ||
        !Number.isFinite(molar) ||
        mass < 0 ||
        molar <= 0
      ) {
        document.getElementById("balance-error").textContent =
          "⚠️ أدخل كتلة غير سالبة وكتلة مولية أكبر من الصفر.";

        ["balance-grams", "balance-mg",
         "balance-kg", "balance-moles"].forEach(id => {
          document.getElementById(id).textContent = "";
        });
        return;
      }

      document.getElementById("balance-error").textContent = "";

      const fmt = value => value.toLocaleString("ar", {
        maximumSignificantDigits: 8
      });

      document.getElementById("balance-grams").textContent =
        "الكتلة: " + fmt(mass) + " جرام";

      document.getElementById("balance-mg").textContent =
        "المليجرام: " + fmt(mass * 1000) + " mg";

      document.getElementById("balance-kg").textContent =
        "الكيلوجرام: " + fmt(mass / 1000) + " kg";

      document.getElementById("balance-moles").textContent =
        "عدد المولات: " + fmt(mass / molar) + " mol";
    };

    massInput.addEventListener("input", calculate);
    molarInput.addEventListener("input", calculate);
    calculate();
  }
};
