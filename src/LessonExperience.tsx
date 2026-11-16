import { useState } from 'react';
import type { CSSProperties } from 'react';
import { ArrowLeftRight, Atom, Bookmark, Check, Clock3, FlaskConical, Network, Sparkles, Target } from 'lucide-react';
import type { Lesson, LessonText, Locale } from './lessonTypes';
import { lessonVisuals } from './lessonVisuals';
import { copy } from './i18n';

type Props = { c: typeof copy.ar | typeof copy.en; locale: Locale; lesson: Lesson; text: LessonText; done: boolean; saved: boolean; onComplete: () => void; onSave: () => void };
const isAr = (locale: Locale) => locale === 'ar';

export function LessonExperience(props: Props) {
  switch (props.lesson.kind) {
    case 'atomic-charge': return <AtomExperience {...props} />;
    case 'energy': return <MatterExperience {...props} />;
    case 'ph': return <AcidExperience {...props} />;
    case 'equilibrium': return <EquilibriumExperience {...props} />;
    case 'mole': return <MoleExperience {...props} />;
    case 'bond': return <BondExperience {...props} />;
  }
  return null;
}

function ExperienceActions({ c, locale, done, saved, onComplete, onSave }: Props) {
  const ar = isAr(locale);
  const jump = (id: string) => document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
  return <div className="experience-actions">
    <button className={`button ${done ? 'button-complete' : 'button-primary'}`} onClick={onComplete}>{done ? <Check size={17} /> : <Target size={17} />}{done ? c.common.completed : c.common.markComplete}</button>
    <button className={`button button-quiet ${saved ? 'is-saved' : ''}`} onClick={onSave}><Bookmark size={17} fill={saved ? 'currentColor' : 'none'} />{saved ? c.common.saved : c.common.save}</button>
    <button className="experience-jump" onClick={() => jump('map')}><Network size={15} />{ar ? 'خريطة الأفكار' : 'Concept map'}</button>
    <button className="experience-jump" onClick={() => jump('simulation')}><FlaskConical size={15} />{ar ? 'مختبر الدرس' : 'Lesson lab'}</button>
  </div>;
}

function ExperienceMeta({ locale, text }: { locale: Locale; text: LessonText }) {
  const ar = isAr(locale);
  return <div className="experience-meta"><span>{text.subject}</span><i /><span>{text.level}</span><i /><span><Clock3 size={14} />{text.duration}</span><small>{ar ? 'درس مستقل · عربي/إنجليزي' : 'Dedicated lesson · AR/EN'}</small></div>;
}

function ExperienceImage({ lesson, locale, className = '' }: { lesson: Lesson; locale: Locale; className?: string }) {
  const visual = lessonVisuals[lesson.id];
  return <figure className={`experience-image ${className}`}>
    <img src={visual.cover} alt={locale === 'ar' ? visual.altAr : visual.altEn} width="2304" height="1536" loading="eager" decoding="async" />
    <span className="experience-image-tag"><Sparkles size={14} />{locale === 'ar' ? visual.badgeAr : visual.badgeEn}</span>
    <figcaption>{locale === 'ar' ? visual.focusAr : visual.focusEn}</figcaption>
  </figure>;
}

function AtomExperience(props: Props) {
  const ar = isAr(props.locale);
  return <section className="lesson-hero lesson-experience experience-atom">
    <div className="experience-copy"><span className="experience-eyebrow"><Atom size={15} />{ar ? 'مرصد الذرة · استكشف البنية' : 'Atomic observatory · Explore the structure'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <div className="atom-fact-ribbon" dir="ltr"><span><b>Z</b><strong>6</strong><small>{ar ? 'كربون · عدد ذري' : 'Carbon · atomic no.'}</small></span><i /><span><b>p⁺</b><strong>6</strong><small>{ar ? 'بروتونات ثابتة' : 'protons stay fixed'}</small></span><i /><span><b>Nₑ</b><strong>{ar ? 'متغير' : 'varies'}</strong><small>{ar ? 'عدد الإلكترونات في الأيون' : 'ion electron count'}</small></span></div>
      <ExperienceActions {...props} />
    </div>
    <div className="atom-visual-column"><ExperienceImage lesson={props.lesson} locale={props.locale} className="atom-cover" /><div className="atom-identity-note"><span className="atom-note-mark">Z</span><div><strong>{ar ? 'هوية العنصر ثابتة' : 'Element identity stays fixed'}</strong><p>{ar ? 'عدد البروتونات هو بطاقة الهوية؛ الإلكترونات تفسر الشحنة.' : 'Proton count is the identity; electrons explain the charge.'}</p></div></div></div>
  </section>;
}

const phases = [
  { ar: 'صلب', en: 'Solid', subAr: 'جسيمات متقاربة وتهتز', subEn: 'Close-packed, vibrating particles' },
  { ar: 'انصهار', en: 'Melting', subAr: 'يتغير الترتيب مع امتصاص الطاقة', subEn: 'Arrangement changes as energy is absorbed' },
  { ar: 'سائل', en: 'Liquid', subAr: 'متقارب ويتدفق', subEn: 'Close together and flowing' },
  { ar: 'غليان', en: 'Boiling', subAr: 'تحول بين السائل والغاز', subEn: 'Liquid-to-gas transition' },
  { ar: 'غاز', en: 'Gas', subAr: 'جسيمات متباعدة تتحرك بحرية', subEn: 'Separated particles moving freely' },
];

function MatterExperience(props: Props) {
  const ar = isAr(props.locale);
  const [phase, setPhase] = useState(0);
  const current = phases[phase];
  return <section className="lesson-hero lesson-experience experience-matter">
    <div className="matter-cover-band"><ExperienceImage lesson={props.lesson} locale={props.locale} className="matter-cover" /><span className="matter-temperature-chip"><span>H₂O</span><i />{ar ? 'رحلة تغيّر الحالة' : 'A journey through states'}</span></div>
    <div className="matter-story"><span className="experience-eyebrow"><Sparkles size={15} />{ar ? 'استوديو المادة · تتبّع التحول' : 'Matter studio · Trace the change'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <div className="phase-journey" role="group" aria-label={ar ? 'مراحل تحولات الماء' : 'Water state stages'}>{phases.map((item, index) => <button key={item.en} className={phase === index ? 'active' : phase < index ? 'upcoming' : 'passed'} aria-pressed={phase === index} onClick={() => setPhase(index)}><span>{String(index + 1).padStart(2, '0')}</span><strong>{ar ? item.ar : item.en}</strong><i /></button>)}</div>
      <div className="phase-insight"><span className={`phase-symbol phase-symbol-${phase}`}><i /><i /><i /><i /></span><div><small>{ar ? 'الحالة المختارة' : 'Selected state'}</small><strong>{ar ? current.ar : current.en}</strong><p>{ar ? current.subAr : current.subEn}</p></div><b>{phase % 2 === 0 ? 'H₂O' : 'Q ↔'}</b></div>
      <ExperienceActions {...props} />
    </div>
  </section>;
}

function AcidExperience(props: Props) {
  const ar = isAr(props.locale);
  const [ph, setPh] = useState(7);
  const h = Math.pow(10, -ph);
  const category = ph < 7 ? (ar ? 'حمضي' : 'Acidic') : ph > 7 ? (ar ? 'قاعدي' : 'Basic') : (ar ? 'متعادل تقريباً' : 'Approximately neutral');
  const color = `hsl(${Math.max(0, Math.min(275, (14 - ph) * 19))} 70% 54%)`;
  return <section className="lesson-hero lesson-experience experience-acid">
    <div className="acid-spectrum-rail"><ExperienceImage lesson={props.lesson} locale={props.locale} className="acid-cover" /><div className="acid-spectrum-art"><span>0</span><i style={{ left: `${ph / 14 * 100}%`, background: color }} /><b>7</b><span>14</span></div><div className="acid-concentration"><small>{ar ? 'تركيز الهيدرونيوم المثالي' : 'Ideal hydronium concentration'}</small><strong dir="ltr">{h.toExponential(2)} mol/L</strong></div></div>
    <div className="acid-story"><span className="experience-eyebrow"><FlaskConical size={15} />{ar ? 'استوديو المحاليل · مقياس لوغاريتمي' : 'Solution studio · Logarithmic scale'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <div className="acid-control-card"><div><label htmlFor="hero-ph">{ar ? 'استكشف مقياس pH' : 'Explore the pH scale'}</label><output htmlFor="hero-ph" style={{ color }}>{ph.toFixed(1)} <small>pH</small></output></div><input id="hero-ph" type="range" min="0" max="14" step="0.1" value={ph} onChange={(event) => setPh(Number(event.target.value))} aria-valuetext={`${ph.toFixed(1)} pH`} /><div className="acid-end-labels"><span>{ar ? 'أكثر حمضية' : 'More acidic'}</span><strong style={{ color }}>{category}</strong><span>{ar ? 'أكثر قاعدية' : 'More basic'}</span></div><p>{ar ? 'الانتقال وحدة واحدة على مقياس pH يعني تغيراً يقارب عشرة أضعاف في التركيز المثالي.' : 'A one-unit pH change corresponds to about a tenfold change in idealized concentration.'}</p></div>
      <ExperienceActions {...props} />
    </div>
  </section>;
}

function EquilibriumExperience(props: Props) {
  const ar = isAr(props.locale);
  const [disturbance, setDisturbance] = useState<'none' | 'add-a' | 'add-b'>('none');
  const a = disturbance === 'add-a' ? 8 : 6;
  const b = disturbance === 'add-b' ? 8 : 6;
  const message = disturbance === 'add-a' ? (ar ? 'استجابة صافية نحو تكوين B' : 'Net response toward forming B') : disturbance === 'add-b' ? (ar ? 'استجابة صافية نحو تكوين A' : 'Net response toward forming A') : (ar ? 'المعدلان متساويان؛ التفاعل ديناميكي' : 'Rates are equal; the system is dynamic');
  return <section className="lesson-hero lesson-experience experience-equilibrium">
    <div className="equilibrium-story"><span className="experience-eyebrow"><ArrowLeftRight size={15} />{ar ? 'غرفة الاتزان · راقب الاتجاهين' : 'Equilibrium room · Observe both directions'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <div className="equilibrium-controls-v2" role="group" aria-label={ar ? 'غيّر حالة النموذج' : 'Change model state'}><button className={disturbance === 'none' ? 'active' : ''} aria-pressed={disturbance === 'none'} onClick={() => setDisturbance('none')}>{ar ? 'اتزان أولي' : 'Initial equilibrium'}</button><button className={disturbance === 'add-a' ? 'active' : ''} aria-pressed={disturbance === 'add-a'} onClick={() => setDisturbance('add-a')}>{ar ? 'أضف A' : 'Add A'}</button><button className={disturbance === 'add-b' ? 'active' : ''} aria-pressed={disturbance === 'add-b'} onClick={() => setDisturbance('add-b')}>{ar ? 'أضف B' : 'Add B'}</button></div>
      <div className="equilibrium-equation-v2" dir="ltr"><span>A</span><i>⇌</i><span>B</span><small dir={ar ? 'rtl' : 'ltr'}>{disturbance === 'none' ? (ar ? 'المعدل الأمامي = المعدل العكسي' : 'Forward rate = reverse rate') : disturbance === 'add-a' ? (ar ? 'إضافة A · استجابة نحو B' : 'A added · response → B') : (ar ? 'إضافة B · استجابة نحو A' : 'B added · response → A')}</small></div>
      <ExperienceActions {...props} />
    </div>
    <div className="equilibrium-visual-v2"><ExperienceImage lesson={props.lesson} locale={props.locale} className="equilibrium-cover" /><div className="equilibrium-particle-chamber"><div><strong>A · {a}</strong><span>{Array.from({ length: a }, (_, i) => <i key={`a-${i}`} style={{ '--p': i } as CSSProperties} />)}</span></div><ArrowLeftRight /><div><strong>B · {b}</strong><span>{Array.from({ length: b }, (_, i) => <i key={`b-${i}`} style={{ '--p': i } as CSSProperties} />)}</span></div></div><p className="equilibrium-live-caption" aria-live="polite">{message}</p></div>
  </section>;
}

function MoleExperience(props: Props) {
  const ar = isAr(props.locale);
  const [grams, setGrams] = useState(44.01);
  const moles = grams / 44.01;
  const particles = moles * 6.02214076e23;
  return <section className="lesson-hero lesson-experience experience-mole">
    <div className="mole-story"><span className="experience-eyebrow"><Atom size={15} />{ar ? 'محطة الكميات · من الكتلة إلى الجسيمات' : 'Quantity studio · From mass to particles'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <label className="mole-input-v2"><span>{ar ? 'كتلة عينة CO₂ (غ)' : 'CO₂ sample mass (g)'}</span><input type="number" min="0" max="100000" step="0.01" inputMode="decimal" value={grams} onChange={(event) => setGrams(Math.max(0, Math.min(100000, Number(event.target.value) || 0)))} /></label>
      <div className="mole-flow-v2" dir="ltr"><div><small>{ar ? 'الكتلة' : 'mass'}</small><strong>{grams.toFixed(2)} g</strong></div><ArrowLeftRight /><div><small>{ar ? 'كمية المادة' : 'amount'}</small><strong>{moles.toPrecision(4)} mol</strong></div><ArrowLeftRight /><div><small>{ar ? 'الجسيمات' : 'particles'}</small><strong>{particles.toExponential(2)}</strong></div></div>
      <p className="mole-method-note"><span>{ar ? 'لثاني أكسيد الكربون النقي:' : 'For pure carbon dioxide:'}</span><b dir="ltr">n = m / M · N = n × Nₐ · M(CO₂) = 44.01 g/mol</b></p><ExperienceActions {...props} />
    </div>
    <div className="mole-visual-v2"><ExperienceImage lesson={props.lesson} locale={props.locale} className="mole-cover" /><div className="mole-number-chip"><span>Nₐ</span><strong>6.022 × 10²³</strong><small>{ar ? 'جسيم لكل مول' : 'particles per mole'}</small></div><div className="mole-steps-v2"><span>01 · {ar ? 'قِس الكتلة' : 'Measure mass'}</span><i /><span>02 · {ar ? 'حوّل إلى مولات' : 'Convert to moles'}</span><i /><span>03 · {ar ? 'احسب الجسيمات' : 'Count particles'}</span></div></div>
  </section>;
}

function BondExperience(props: Props) {
  const ar = isAr(props.locale);
  const [deltaChi, setDeltaChi] = useState(1.2);
  const [side, setSide] = useState<'left' | 'right'>('right');
  const polarity = Math.min(1, Math.max(0, deltaChi / 3.5));
  const shift = side === 'right' ? polarity : -polarity;
  const shiftText = deltaChi < 0.15 ? (ar ? 'تقاسم شبه متساوٍ في هذا التصور' : 'Nearly even sharing in this model') : side === 'right' ? (ar ? 'كثافة أكبر قرب الذرة اليمنى' : 'Greater density near the right atom') : (ar ? 'كثافة أكبر قرب الذرة اليسرى' : 'Greater density near the left atom');
  return <section className="lesson-hero lesson-experience experience-bond">
    <div className="bond-visual-v2" style={{ '--electron-shift': `${shift * 1.7}rem`, '--polarity': polarity } as CSSProperties}><ExperienceImage lesson={props.lesson} locale={props.locale} className="bond-cover" /><div className="bond-model-v2"><span className="bond-atom left"><b>{side === 'left' && deltaChi > .15 ? 'δ−' : deltaChi > .15 ? 'δ+' : 'A'}</b><small>A</small></span><i className="bond-cloud-v2"><i /><i /><i /><i /></i><span className="bond-atom right"><b>{side === 'right' && deltaChi > .15 ? 'δ−' : deltaChi > .15 ? 'δ+' : 'B'}</b><small>B</small></span></div><p className="bond-shift-caption" aria-live="polite">{shiftText}</p></div>
    <div className="bond-story"><span className="experience-eyebrow"><Sparkles size={15} />{ar ? 'استوديو الروابط · تتبّع الكثافة' : 'Bond studio · Track electron density'}</span><h1>{props.text.title}</h1><p>{props.text.summary}</p><ExperienceMeta locale={props.locale} text={props.text} />
      <div className="bond-control-v2"><label htmlFor="hero-bond-delta">{ar ? 'فرق السالبية التقريبي Δχ' : 'Approximate electronegativity difference Δχ'}<output dir="ltr">{deltaChi.toFixed(1)}</output></label><input id="hero-bond-delta" type="range" min="0" max="3.5" step="0.1" value={deltaChi} onChange={(event) => setDeltaChi(Number(event.target.value))} /><div className="bond-side-control" role="group" aria-label={ar ? 'الذرة الأعلى سالبية في النموذج' : 'More electronegative atom in the model'}><button aria-pressed={side === 'left'} className={side === 'left' ? 'active' : ''} onClick={() => setSide('left')}>{ar ? 'A · اليسار' : 'A · left'}</button><button aria-pressed={side === 'right'} className={side === 'right' ? 'active' : ''} onClick={() => setSide('right')}>{ar ? 'B · اليمين' : 'B · right'}</button></div><small>{ar ? 'الاستقطاب طيف متدرج؛ الشحنات الموضحة جزئية وليست كاملة.' : 'Polarity is a continuum; the displayed charges are partial, not full.'}</small></div>
      <ExperienceActions {...props} />
    </div>
  </section>;
}
