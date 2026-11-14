"use strict";

window.Phase2 = {
  sections: [
    {id:"basics",title:"أساسيات الكيمياء",icon:"⚛️"},
    {id:"reactions",title:"التفاعلات الكيميائية",icon:"🧪"},
    {id:"balance",title:"الوزن والحسابات",icon:"⚖️"},
    {id:"minerals",title:"علم المعادن",icon:"💎"},
    {id:"gold",title:"مختبر الذهب",icon:"🟡"},
    {id:"simulation",title:"المحاكاة التفاعلية",icon:"🔬"}
  ],

  renderMenu() {
    return `
      <section class="phase2-menu" dir="rtl">
        <h2 class="phase2-title">المختبر المتقدم</h2>
        <div class="phase2-grid">
          ${this.sections.map(s => `
            <button class="phase2-card"
              data-section="${s.id}">
              <span>${s.icon}</span>
              <strong>${s.title}</strong>
            </button>
          `).join("")}
        </div>
      </section>
    `;
  }
};

document.getElementById("phase2-open")?.addEventListener("click", () => {
  const app = document.getElementById("app");
  if (!app) return;

  app.innerHTML = `
    <div dir="rtl">
      <button id="phase2-back" class="phase2-card"
        style="width:100%;margin-bottom:16px">
        ↩ العودة إلى المختبر الأساسي
      </button>
      ${window.Phase2.renderMenu()}
    </div>
  `;

  document.getElementById("phase2-back")
    .addEventListener("click", () => {
      render("home");
    });

  app.querySelectorAll("[data-section]").forEach(button => {
    button.addEventListener("click", () => {
      const section = window.Phase2.sections.find(
        item => item.id === button.dataset.section
      );

      if (!section) return;

      const handlers = {
        basics: () => window.Phase2Modules.basics(),
        balance: () => window.Phase2Balance.show(),
        minerals: () => window.Phase2Modules.minerals(),
        gold: () => window.Phase2Modules.gold(),
        simulation: () => window.Phase2Modules.simulation()
      };
      if (handlers[section.id]) {
        handlers[section.id]();
        return;
      }


      if (section.id === "reactions") {
        window.Phase2Reactions.show();
        return;
      }


      app.innerHTML = `
        <section class="phase2-card" dir="rtl">
          <button id="phase2-menu-back" type="button">
            ↩ العودة إلى الأقسام
          </button>
          <h2 class="phase2-title">
            ${section.icon} ${section.title}
          </h2>
          <p>سيتم إضافة الدروس والمحاكاة التفاعلية هنا.</p>
        </section>
      `;

      document.getElementById("phase2-menu-back")
        .addEventListener("click", () => {
          document.getElementById("phase2-open").click();
        });
    });
  });
});
