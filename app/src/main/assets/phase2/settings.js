"use strict";

(() => {
  const KEY = "chemistry_phase2_settings";
  const defaults = {
    theme: "auto",
    font: "medium",
    motion: "on",
    digits: "ar"
  };

  let saved = {};
  try {
    saved = JSON.parse(localStorage.getItem(KEY) || "{}") || {};
  } catch (_) {}

  const settings = { ...defaults, ...saved };

  const allowed = {
    theme: ["auto", "light", "dark"],
    font: ["small", "medium", "large"],
    motion: ["on", "off"],
    digits: ["ar", "en"]
  };

  for (const key of Object.keys(defaults)) {
    if (!allowed[key].includes(settings[key])) {
      settings[key] = defaults[key];
    }
  }

  const systemDark = window.matchMedia(
    "(prefers-color-scheme: dark)"
  );

  function apply() {
    const dark = settings.theme === "dark" ||
      (settings.theme === "auto" && systemDark.matches);

    document.documentElement.dataset.p2Theme =
      dark ? "dark" : "light";
    document.documentElement.dataset.p2Font = settings.font;
    document.documentElement.dataset.p2Motion = settings.motion;
    document.documentElement.dataset.p2Digits = settings.digits;

    document.documentElement.style.colorScheme =
      dark ? "dark" : "light";
  }

  function save() {
    try {
      localStorage.setItem(KEY, JSON.stringify(settings));
    } catch (_) {}
    apply();
  }

  function show() {
    const app = document.getElementById("app");
    if (!app) return;

    app.innerHTML = `
      <section class="phase2-menu p2-settings" dir="rtl">
        <button id="settings-back" class="phase2-card">
          ↩ العودة إلى المختبر
        </button>
        <h2 class="phase2-title">⚙️ إعدادات التطبيق</h2>

        <div class="phase2-card">
          <label for="setting-theme">🌙 المظهر</label>
          <select id="setting-theme">
            <option value="auto">تلقائي حسب الهاتف</option>
            <option value="light">نهاري</option>
            <option value="dark">ليلي</option>
          </select>

          <label for="setting-font">🔠 حجم الخط</label>
          <select id="setting-font">
            <option value="small">صغير</option>
            <option value="medium">متوسط</option>
            <option value="large">كبير</option>
          </select>

          <label for="setting-motion">✨ المؤثرات الحركية</label>
          <select id="setting-motion">
            <option value="on">تشغيل</option>
            <option value="off">تقليل الحركة</option>
          </select>

          <label for="setting-digits">🔢 عرض الأرقام</label>
          <select id="setting-digits">
            <option value="ar">عربية ١٢٣</option>
            <option value="en">إنجليزية 123</option>
          </select>

          <button id="settings-reset" class="phase2-card">
            🔄 استعادة الإعدادات الافتراضية
          </button>

          <p id="settings-status" aria-live="polite">
            يتم حفظ اختياراتك تلقائيًا.
          </p>
        </div>
      </section>
    `;

    const fields = {
      theme: "setting-theme",
      font: "setting-font",
      motion: "setting-motion",
      digits: "setting-digits"
    };

    for (const [key, id] of Object.entries(fields)) {
      const element = document.getElementById(id);
      element.value = settings[key];

      element.addEventListener("change", () => {
        settings[key] = element.value;
        save();
        document.getElementById("settings-status").textContent =
          "✅ تم حفظ الإعدادات.";
      });
    }

    document.getElementById("settings-reset").onclick = () => {
      Object.assign(settings, defaults);
      save();
      show();
    };

    document.getElementById("settings-back").onclick = () => {
      document.getElementById("phase2-open").click();
    };
  }

  function addButton() {
    if (document.getElementById("phase2-settings-open")) return;

    const open = document.getElementById("phase2-open");
    if (!open) return;

    const button = document.createElement("button");
    button.id = "phase2-settings-open";
    button.type = "button";
    button.className = "phase2-card";
    button.textContent = "⚙️ الإعدادات";
    button.style.cssText = "width:100%;margin:8px 0";

    open.insertAdjacentElement("afterend", button);
    button.addEventListener("click", show);
  }

  apply();

  if (systemDark.addEventListener) {
    systemDark.addEventListener("change", apply);
  } else if (systemDark.addListener) {
    systemDark.addListener(apply);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", addButton);
  } else {
    addButton();
  }

  window.Phase2Settings = { show, apply, settings };
})();
