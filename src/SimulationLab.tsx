import { useEffect, useMemo, useState } from 'react';
import type { CSSProperties } from 'react';
import { Atom, ArrowLeftRight, Beaker, Lightbulb, RotateCcw, ShieldCheck } from 'lucide-react';
import type { Locale, SimulationKind } from './lessonTypes';

type Props = { locale: Locale; kind: SimulationKind };
type Mode = 'heating' | 'cooling';
type BalanceState = 'initial' | 'add-a' | 'add-b';
type MoleMode = 'mass' | 'particles';
const avogadro = 6.02214076e23;
const isAr = (locale: Locale) => locale === 'ar';
const chargeText = (charge: number, locale: Locale) => charge === 0 ? (isAr(locale) ? 'متعادل' : 'Neutral') : isAr(locale) ? `شحنة ${Math.abs(charge)}${charge > 0 ? '+' : '−'}` : `Charge ${Math.abs(charge)}${charge > 0 ? '+' : '−'}`;

function sliderConfig(kind: SimulationKind) {
  if (kind === 'atomic-charge') return { min: 4, max: 8, step: 1, initial: 6, labelAr: 'عدد الإلكترونات', labelEn: 'Electron count', unitAr: 'إلكترون', unitEn: 'electrons' };
  if (kind === 'energy') return { min: 0, max: 100, step: 1, initial: 0, labelAr: 'التقدم على مسار التحول الحراري', labelEn: 'Position on the thermal-change path', unitAr: '%', unitEn: '%' };
  if (kind === 'ph') return { min: 0, max: 14, step: 0.1, initial: 7, labelAr: 'درجة الحموضة pH', labelEn: 'pH', unitAr: 'pH', unitEn: 'pH' };
  if (kind === 'bond') return { min: 0, max: 3.5, step: 0.1, initial: 1, labelAr: 'فرق السالبية الكهربائية التقريبي Δχ', labelEn: 'Approximate electronegativity difference Δχ', unitAr: 'Δχ', unitEn: 'Δχ' };
  if (kind === 'mole') return { min: 0, max: 1000, step: 0.01, initial: 44.01, labelAr: 'قيمة CO₂', labelEn: 'CO₂ amount', unitAr: 'غ', unitEn: 'g' };
  return { min: 0, max: 0, step: 1, initial: 0, labelAr: '', labelEn: '', unitAr: '', unitEn: '' };
}

export function SimulationLab({ locale, kind }: Props) {
  const ar = isAr(locale);
  const config = useMemo(() => sliderConfig(kind), [kind]);
  const [value, setValue] = useState(config.initial);
  const [mode, setMode] = useState<Mode>('heating');
  const [showEquation, setShowEquation] = useState(true);
  const [volume, setVolume] = useState(1);
  const [balanceState, setBalanceState] = useState<BalanceState>('initial');
  const [moleMode, setMoleMode] = useState<MoleMode>('mass');
  const [bondSide, setBondSide] = useState<'left' | 'right'>('right');
  useEffect(() => {
    setValue(config.initial); setMode('heating'); setShowEquation(true); setVolume(1);
    setBalanceState('initial'); setMoleMode('mass'); setBondSide('right');
  }, [kind, config.initial]);

  const pathProgress = kind === 'energy' && mode === 'cooling' ? 100 - value : value;
  const thermal = thermalState(pathProgress);
  const carbonCharge = kind === 'atomic-charge' ? 6 - Math.round(value) : 0;
  const hydronium = kind === 'ph' ? Math.pow(10, -value) : 0;
  const hydroxide = kind === 'ph' ? Math.pow(10, -(14 - value)) : 0;
  const amount = moleMode === 'mass' ? value / 44.01 : value;
  const moleculeCount = amount * avogadro;

  const output = kind === 'atomic-charge' ? atomOutput(value, locale)
    : kind === 'energy' ? energyOutput(thermal, locale)
      : kind === 'ph' ? phOutput(value, locale)
        : kind === 'equilibrium' ? equilibriumOutput(balanceState, locale)
          : kind === 'mole' ? moleOutput(value, moleMode, amount, moleculeCount, locale)
            : bondOutput(value, bondSide, locale);

  const title = kind === 'atomic-charge' ? (ar ? 'استكشف مكوّنات الذرة' : 'Explore the atom')
    : kind === 'energy' ? (ar ? 'رحلة الطاقة بين الحالات' : 'Energy path between states')
      : kind === 'ph' ? (ar ? 'مقياس pH والتركيز' : 'pH and concentration')
        : kind === 'equilibrium' ? (ar ? 'نظام عكسي يتحرك باستمرار' : 'A reversible system in motion')
          : kind === 'mole' ? (ar ? 'حوّل بين الكتلة والجسيمات' : 'Convert mass and particles')
            : (ar ? 'شاهد استقطاب الرابطة' : 'Explore bond polarity');

  return <div className={`simulation-panel sim-${kind}`}>
    <div className="simulation-head"><div><span className="live-pill"><i />{ar ? 'مختبر بصري آمن' : 'Safe visual lab'}</span><h2>{title}</h2><p>{ar ? 'غيّر المتغير ولاحظ ما يتغير في النموذج، ثم اقرأ تفسيره وحدوده العلمية.' : 'Change a variable, observe the model, then read its scientific interpretation and limits.'}</p></div><span className="simulation-symbol">{kind === 'ph' ? <Beaker /> : kind === 'equilibrium' ? <ArrowLeftRight /> : <Atom />}</span></div>

    <SimulationArt locale={locale} kind={kind} value={value} thermal={thermal} volume={volume} balanceState={balanceState} moleMode={moleMode} amount={amount} bondSide={bondSide} />

    {kind === 'atomic-charge' && <>
      <RangeControl id="atom-electrons" config={config} value={value} locale={locale} onChange={setValue} display={String(Math.round(value))} />
      <label className="equation-toggle"><input type="checkbox" checked={showEquation} onChange={(event) => setShowEquation(event.target.checked)} /><span>{ar ? 'أظهر علاقة الشحنة' : 'Show the charge relationship'}</span></label>
      {showEquation && <div className="formula-ribbon" dir="ltr">q/e = Z − Nₑ = 6 − {Math.round(value)} = {carbonCharge > 0 ? `${carbonCharge}+` : carbonCharge < 0 ? `${Math.abs(carbonCharge)}−` : '0'}</div>}
    </>}

    {kind === 'energy' && <>
      <div className="segment-control" role="group" aria-label={ar ? 'اتجاه المسار الحراري' : 'Thermal path direction'}>
        <button aria-pressed={mode === 'heating'} className={mode === 'heating' ? 'selected' : ''} onClick={() => setMode('heating')}>{ar ? 'تسخين' : 'Heating'}</button>
        <button aria-pressed={mode === 'cooling'} className={mode === 'cooling' ? 'selected' : ''} onClick={() => setMode('cooling')}>{ar ? 'تبريد' : 'Cooling'}</button>
      </div>
      <RangeControl id="energy-progress" config={config} value={value} locale={locale} onChange={setValue} display={`${Math.round(value)}%`} />
    </>}

    {kind === 'ph' && <>
      <RangeControl id="ph-value" config={config} value={value} locale={locale} onChange={setValue} display={value.toFixed(1)} />
      <div className="ph-concentration-grid">
        <ConcentrationCard label="[H₃O⁺]" value={hydronium} amount={hydronium * volume} dominant={value < 7} locale={locale} />
        <ConcentrationCard label="[OH⁻]" value={hydroxide} amount={hydroxide * volume} dominant={value > 7} locale={locale} />
      </div>
      <RangeControl id="ph-volume" config={{ min: 0.5, max: 2, step: 0.5, initial: 1, labelAr: 'حجم عينة افتراضي متجانس', labelEn: 'Idealized homogeneous sample volume', unitAr: 'لتر', unitEn: 'L' }} value={volume} locale={locale} onChange={setVolume} display={volume.toFixed(1)} />
      <div className="formula-ribbon" dir="ltr">pH = −log₁₀[H₃O⁺] · pH + pOH = 14 · n = cV</div>
    </>}

    {kind === 'equilibrium' && <div className="equilibrium-controls" role="group" aria-label={ar ? 'اضطرابات نموذج الاتزان' : 'Equilibrium model disturbances'}>
      <button onClick={() => setBalanceState('add-a')} className={balanceState === 'add-a' ? 'selected' : ''}>{ar ? 'أضف كمية افتراضية من A' : 'Add a conceptual amount of A'}</button>
      <button onClick={() => setBalanceState('add-b')} className={balanceState === 'add-b' ? 'selected' : ''}>{ar ? 'أضف كمية افتراضية من B' : 'Add a conceptual amount of B'}</button>
      <button className="reset-control" onClick={() => setBalanceState('initial')}><RotateCcw size={15} />{ar ? 'إعادة الضبط' : 'Reset'}</button>
    </div>}

    {kind === 'mole' && <>
      <div className="segment-control" role="group" aria-label={ar ? 'نوع التحويل' : 'Conversion type'}>
        <button aria-pressed={moleMode === 'mass'} className={moleMode === 'mass' ? 'selected' : ''} onClick={() => { setMoleMode('mass'); setValue(44.01); }}>{ar ? 'كتلة ← مولات' : 'Mass → moles'}</button>
        <button aria-pressed={moleMode === 'particles'} className={moleMode === 'particles' ? 'selected' : ''} onClick={() => { setMoleMode('particles'); setValue(1); }}>{ar ? 'مولات ← جسيمات' : 'Moles → particles'}</button>
      </div>
      <label className="numeric-control"><span>{ar ? `قيمة CO₂ (${moleMode === 'mass' ? 'غ' : 'mol'})` : `CO₂ value (${moleMode === 'mass' ? 'g' : 'mol'})`}</span><input type="number" min="0" max="1000000" step="0.01" value={value} onChange={(event) => setValue(Math.max(0, Math.min(1000000, Number(event.target.value) || 0)))} inputMode="decimal" /></label>
      <div className="mole-result-card"><span>{ar ? 'النتيجة' : 'Result'}</span><strong dir="ltr">{moleMode === 'mass' ? `${amount.toPrecision(4)} mol` : `${moleculeCount.toExponential(4)} molecules`}</strong><p dir="ltr">{moleMode === 'mass' ? `n = m / M = ${value.toPrecision(4)} g / 44.01 g mol⁻¹` : `N = n × Nₐ = ${value.toPrecision(4)} mol × 6.02214076 × 10²³ mol⁻¹`}</p></div>
    </>}

    {kind === 'bond' && <>
      <RangeControl id="bond-delta" config={config} value={value} locale={locale} onChange={setValue} display={value.toFixed(1)} />
      <div className="segment-control bond-direction" role="group" aria-label={ar ? 'جهة السالبية الأعلى في النموذج' : 'More electronegative side in the model'}>
        <button className={bondSide === 'left' ? 'selected' : ''} aria-pressed={bondSide === 'left'} onClick={() => setBondSide('left')}>{ar ? 'الذرة اليسرى' : 'Left atom'}</button>
        <button className={bondSide === 'right' ? 'selected' : ''} aria-pressed={bondSide === 'right'} onClick={() => setBondSide('right')}>{ar ? 'الذرة اليمنى' : 'Right atom'}</button>
      </div>
    </>}

    <div className="simulation-result" aria-live="polite" aria-atomic="true"><span>{ar ? 'ما الذي نلاحظه؟' : 'What do we notice?'}</span><strong>{output.title}</strong><p>{output.body}</p></div>
    <p className="simulation-footnote"><Lightbulb size={15} />{modelLimit(kind, locale)}</p>
    <p className="simulation-safe-note"><ShieldCheck size={15} />{ar ? 'هذا نموذج تعليمي على الشاشة فقط، ولا يتطلب مواد أو تجربة مخبرية.' : 'This is a screen-based learning model; it requires no substances or laboratory experiment.'}</p>
  </div>;
}

function RangeControl({ id, config, value, locale, onChange, display }: { id: string; config: ReturnType<typeof sliderConfig> | { min: number; max: number; step: number; initial: number; labelAr: string; labelEn: string; unitAr: string; unitEn: string }; value: number; locale: Locale; onChange: (value: number) => void; display: string }) {
  const ar = isAr(locale);
  return <div className="simulation-control"><div className="simulation-value"><label htmlFor={id}>{ar ? config.labelAr : config.labelEn}</label><strong dir="ltr">{display} <small>{ar ? config.unitAr : config.unitEn}</small></strong></div><input id={id} type="range" min={config.min} max={config.max} step={config.step} value={value} onChange={(event) => onChange(Number(event.target.value))} aria-valuetext={display} /><div className="range-ends" dir="ltr"><span>{config.min}</span><span>{config.max}</span></div></div>;
}

function ConcentrationCard({ label, value, amount, dominant, locale }: { label: string; value: number; amount: number; dominant: boolean; locale: Locale }) {
  const ar = isAr(locale);
  return <article className={`concentration-card ${dominant ? 'dominant' : ''}`}><span dir="ltr">{label}</span><strong dir="ltr">{value.toExponential(2)} mol/L</strong><div className="log-bar"><i style={{ width: `${Math.max(4, Math.min(100, ((14 + Math.log10(Math.max(value, 1e-14))) / 14) * 100))}%` }} /></div><small dir="ltr">n = cV = {amount.toExponential(2)} mol</small><em>{ar ? 'مقياس لوغاريتمي' : 'Logarithmic scale'}</em></article>;
}

function SimulationArt({ locale, kind, value, thermal, volume, balanceState, moleMode, amount, bondSide }: { locale: Locale; kind: SimulationKind; value: number; thermal: ReturnType<typeof thermalState>; volume: number; balanceState: BalanceState; moleMode: MoleMode; amount: number; bondSide: 'left' | 'right' }) {
  const ar = isAr(locale);
  if (kind === 'atomic-charge') {
    const electrons = Math.round(value);
    const positions = [[21,34],[31,17],[50,12],[69,20],[79,39],[72,67],[51,80],[29,70]];
    const charge = 6 - electrons;
    return <div className="simulation-art atom-cloud-art" role="img" aria-label={ar ? `نواة كربون ثابتة فيها 6 بروتونات و6 نيوترونات، وحولها ${electrons} إلكترونات؛ ${chargeText(charge,locale)}` : `Fixed carbon nucleus with 6 protons and 6 neutrons, surrounded by ${electrons} electrons; ${chargeText(charge,locale)}`}>
      <div className="electron-probability-cloud" />
      <div className="atom-nucleus" aria-hidden="true">{Array.from({ length: 12 }, (_, index) => <i key={index} className={index % 2 === 0 ? 'proton' : 'neutron'} />)}</div>
      {positions.slice(0,electrons).map(([x,y],index) => <i key={index} className="cloud-electron" style={{ left:`${x}%`,top:`${y}%` }} />)}
      <div className="atom-fixed-label"><strong>6p⁺</strong><span>6n⁰</span></div>
      <div className={`atom-charge-chip ${charge === 0 ? 'neutral' : charge > 0 ? 'positive' : 'negative'}`}>{chargeText(charge,locale)}</div>
      <div className="simulation-legend"><span><i className="legend-proton" />{ar ? 'بروتون' : 'Proton'}</span><span><i className="legend-neutron" />{ar ? 'نيوترون' : 'Neutron'}</span><span><i className="legend-electron" />{ar ? 'إلكترون' : 'Electron'}</span></div>
    </div>;
  }
  if (kind === 'energy') {
    const x = 24 + thermal.progress * 4.72;
    const y = energyCurveY(thermal.progress);
    return <div className={`simulation-art thermal-art phase-${thermal.phase}`}>
      <div className="thermal-chart"><svg viewBox="0 0 520 120" role="img" aria-label={ar ? 'منحنى نوعي يبين ارتفاع درجة الحرارة وهضبتي الانصهار والغليان' : 'Qualitative heating curve showing temperature rise and melting and boiling plateaus'}><path className="curve-grid" d="M24 18V104H498M24 92H498M24 30H498" /><path className="curve-line" d="M24 104L118 92L236 92L378 30L448 30L496 18" /><circle className="curve-cursor" cx={x} cy={y} r="7" /></svg><div className="curve-phase-labels"><span>{ar ? 'صلب' : 'Solid'}</span><span>{ar ? 'انصهار' : 'Melting'}</span><span>{ar ? 'سائل' : 'Liquid'}</span><span>{ar ? 'غليان' : 'Boiling'}</span><span>{ar ? 'غاز' : 'Gas'}</span></div></div>
      <div className={`phase-vessel phase-vessel-${thermal.phase}`}><div className="phase-vessel-liquid" />{Array.from({ length: 12 },(_,i)=><i key={i} style={{'--particle-index':i} as CSSProperties} />)}</div>
      <div className="thermal-readout"><span>{ar ? 'الحالة في النموذج' : 'Model state'}</span><strong>{ar ? thermal.labelAr : thermal.labelEn}</strong><b dir="ltr">{thermal.temperature.toFixed(0)} °C</b>{thermal.coexist && <small>{ar ? 'طوران يتعايشان أثناء التحول' : 'Two phases coexist during the transition'}</small>}</div>
      <span className="thermal-volume-note">{ar ? 'ماء نقي · ضغط يقارب 1 atm · نموذج نوعي' : 'Pure water · ≈1 atm · qualitative model'}</span>
    </div>;
  }
  if (kind === 'ph') {
    const left = Math.max(0,Math.min(100,value / 14 * 100));
    return <div className="simulation-art ph-art-v8"><div className="ph-vessel"><span className="ph-vessel-fill" style={{height:`${48 + (volume-0.5)*15}%`}} /><strong>{value.toFixed(1)}</strong><small>pH</small><Beaker /></div><div className="ph-scale-v8"><div className="ph-scale-labels"><span>{ar ? 'حمضي' : 'Acidic'}</span><span>{ar ? 'متعادل' : 'Neutral'}</span><span>{ar ? 'قاعدي' : 'Basic'}</span></div><div className="ph-spectrum-v8"><i style={{left:`${left}%`}} /></div><div className="ph-numberline" dir="ltr"><span>0</span><span>7</span><span>14</span></div></div><div className="ph-art-note">{ar ? `حجم العينة الافتراضي: ${volume.toFixed(1)} L` : `Idealized sample volume: ${volume.toFixed(1)} L`}</div></div>;
  }
  if (kind === 'equilibrium') {
    const aCount = balanceState === 'add-a' ? 8 : 6;
    const bCount = balanceState === 'add-b' ? 8 : 6;
    return <div className={`simulation-art equilibrium-art equilibrium-${balanceState}`}><div className="equilibrium-vessel"><span className="vessel-caption">A ⇌ B</span><div className="species species-a"><span className="species-count">A · {aCount}</span>{Array.from({length:aCount},(_,i)=><i key={i} style={{'--particle-index':i} as CSSProperties} />)}</div><div className="species species-b"><span className="species-count">B · {bCount}</span>{Array.from({length:bCount},(_,i)=><i key={i} style={{'--particle-index':i} as CSSProperties} />)}</div></div><div className="rate-panel">{balanceState === 'initial' ? <><div><span>{ar ? 'المعدل الأمامي' : 'Forward rate'}</span><i style={{width:'50%'}} /><strong>{ar ? 'يساوي العكسي عند الاتزان' : 'Equals reverse at equilibrium'}</strong></div><ArrowLeftRight /><div><span>{ar ? 'المعدل العكسي' : 'Reverse rate'}</span><i style={{width:'50%'}} /><strong>{ar ? 'الاتجاهان مستمران' : 'Both directions continue'}</strong></div></> : <><div><span>{ar ? 'الاضطراب المضاف' : 'Added perturbation'}</span><strong>{balanceState === 'add-a' ? (ar ? 'إضافة A' : 'A added') : (ar ? 'إضافة B' : 'B added')}</strong><small>{ar ? 'لقطة مباشرة بعد الإضافة' : 'Immediate post-addition snapshot'}</small></div><ArrowLeftRight /><div><span>{ar ? 'الاستجابة الصافية' : 'Net response'}</span><strong>{balanceState === 'add-a' ? (ar ? 'نحو تكوين B' : 'Toward forming B') : (ar ? 'نحو تكوين A' : 'Toward forming A')}</strong><small>{ar ? 'تتساوى السرعتان مجدداً عند الاتزان الجديد' : 'Rates become equal again at the new equilibrium'}</small></div></>}</div><div className="equilibrium-state-label">{balanceState === 'initial' ? (ar ? 'اتزان ديناميكي' : 'Dynamic equilibrium') : balanceState === 'add-a' ? (ar ? 'لقطة بعد إضافة A' : 'Snapshot after adding A') : (ar ? 'لقطة بعد إضافة B' : 'Snapshot after adding B')}</div></div>;
  }
  if (kind === 'mole') {
    const bars = Math.max(1,Math.min(9,Math.round(Math.log10(Math.max(amount,1e-8)+1)*3)));
    return <div className="simulation-art mole-art-v8"><div className="mole-vessel"><span className="mole-fill" style={{height:`${Math.max(5,Math.min(94,amount / 3 * 100))}%`}} />{Array.from({length:bars},(_,i)=><i key={i} style={{'--particle-index':i} as CSSProperties} />)}<b>CO₂</b></div><div className="mole-conversion-path"><span className={moleMode === 'mass' ? 'active' : ''}>{moleMode === 'mass' ? `${Number.isFinite(amount) ? amount.toPrecision(3) : '0'} mol` : `${value.toPrecision(3)} mol`}</span><ArrowLeftRight /><span>{(amount * avogadro).toExponential(2)}<small>{ar ? 'جزيئاً' : 'molecules'}</small></span></div><div className="mole-constants"><span>44.01 g/mol</span><span>Nₐ = 6.02214076 × 10²³ mol⁻¹</span></div><small className="mole-art-disclaimer">{ar ? 'الرسم تمثيلي؛ لا يعرض كل الجسيمات فعلياً.' : 'Illustrative view; it does not display every particle.'}</small></div>;
  }
  const shift = Math.min(1,Math.max(0,value/3.5));
  const delta = bondSide === 'right' ? shift : -shift;
  return <div className="simulation-art bond-art-v8" style={{'--bond-shift':`${delta*1.6}rem`} as CSSProperties}><div className="bond-atom-v8 atom-left"><span>{bondSide === 'left' && value > 0.2 ? 'δ−' : value > 0.2 ? 'δ+' : 'A'}</span><small>A</small></div><div className="bond-electron-cloud"><i /><i /><i /><i /></div><div className="bond-atom-v8 atom-right"><span>{bondSide === 'right' && value > 0.2 ? 'δ−' : value > 0.2 ? 'δ+' : 'B'}</span><small>B</small></div><div className="bond-direction-arrow">{bondSide === 'right' ? '→' : '←'}<span>{ar ? 'اتجاه كثافة الإلكترونات' : 'Electron-density shift'}</span></div><div className="bond-continuum"><span>{ar ? 'تقاسم شبه متساوٍ' : 'Nearly even sharing'}</span><i style={{width:`${Math.max(5,shift*100)}%`}} /><span>{ar ? 'استقطاب أكبر' : 'Greater polarity'}</span></div></div>;
}

function thermalState(progress: number) {
  const p = Math.max(0,Math.min(100,progress));
  if (p < 20) return { progress:p,temperature:-20+p,phase:'solid',labelAr:'صلب',labelEn:'Solid',coexist:false };
  if (p < 45) return { progress:p,temperature:0,phase:'melting',labelAr:'انصهار',labelEn:'Melting',coexist:true };
  if (p < 75) return { progress:p,temperature:(p-45)/30*100,phase:'liquid',labelAr:'سائل',labelEn:'Liquid',coexist:false };
  if (p < 90) return { progress:p,temperature:100,phase:'boiling',labelAr:'غليان',labelEn:'Boiling',coexist:true };
  return { progress:p,temperature:100+(p-90)*2,phase:'gas',labelAr:'غاز',labelEn:'Gas',coexist:false };
}

function energyCurveY(progress: number) {
  const p=Math.max(0,Math.min(100,progress));
  if(p<20) return 104-p/20*12;
  if(p<45) return 92;
  if(p<75) return 92-(p-45)/30*62;
  if(p<90) return 30;
  return 30-(p-90)/10*12;
}

function atomOutput(electrons: number, locale: Locale) {
  const ar=isAr(locale), charge=6-Math.round(electrons);
  const title=charge===0?(ar?'ذرة كربون متعادلة':'Neutral carbon atom'):ar?`أيون كربون ${charge>0?'موجب':'سالب'} (${Math.abs(charge)}${charge>0?'+':'−'})`:`${charge>0?'Positive':'Negative'} carbon ion (${Math.abs(charge)}${charge>0?'+':'−'})`;
  const body=ar?`عدد البروتونات ثابت عند 6، لذلك يبقى العدد الذري وهوية العنصر ثابتين. الشحنة الصافية = 6 − ${Math.round(electrons)} = ${charge>0?`${charge}+`:charge<0?`${Math.abs(charge)}−`:'0'}. فقد الإلكترونات يعطي شحنة موجبة واكتسابها يعطي شحنة سالبة.`:`The proton count remains 6, so the atomic number and element identity stay fixed. Net charge = 6 − ${Math.round(electrons)} = ${charge>0?`${charge}+`:charge<0?`${Math.abs(charge)}−`:'0'}. Losing electrons gives a positive charge; gaining them gives a negative charge.`;
  return {title,body};
}
function energyOutput(state: ReturnType<typeof thermalState>, locale: Locale) {
  const ar=isAr(locale),label=ar?state.labelAr:state.labelEn;
  if(state.phase==='melting') return {title:ar?'انصهار: طوران معاً تقريباً':'Melting: two phases coexist',body:ar?'تُستخدم الطاقة المضافة في التحول بين الصلب والسائل؛ لذلك تبقى درجة الحرارة قريبة من 0°C في النموذج، فيما يتغير ترتيب الجسيمات.':'Added energy goes into the solid-to-liquid transition, so temperature stays near 0°C in this model while particle arrangement changes.'};
  if(state.phase==='boiling') return {title:ar?'غليان: طوران معاً تقريباً':'Boiling: two phases coexist',body:ar?'تُستخدم الطاقة في التحول بين السائل والغاز؛ لذلك تبقى درجة الحرارة قريبة من 100°C في النموذج، فيما تتباعد الجسيمات.':'Energy goes into the liquid-to-gas transition, so temperature stays near 100°C in this model while particles spread apart.'};
  return {title:`${label} · ${state.temperature.toFixed(0)} °C`,body:ar?'داخل الطور، تغيّر الطاقة الحركية المتوسطة للجسيمات ودرجة الحرارة. هذا منحنى نوعي للماء النقي قرب ضغط جوي واحد.':'Within one phase, changing energy changes average particle motion and temperature. This is a qualitative curve for pure water near one atmosphere.'};
}
function phOutput(value:number,locale:Locale){
  const ar=isAr(locale),group=value<7?(ar?'المحلول حمضي في هذا النموذج':'Acidic in this model'):value>7?(ar?'المحلول قاعدي في هذا النموذج':'Basic in this model'):(ar?'متعادل تقريباً':'Approximately neutral');
  const h=Math.pow(10,-value),oh=Math.pow(10,-(14-value));
  return {title:group,body:ar?`عند 25°C تقريباً: [H₃O⁺] ≈ ${h.toExponential(2)} mol/L و[OH⁻] ≈ ${oh.toExponential(2)} mol/L. كل وحدة pH تعني تغيراً بعشرة أضعاف، لا فرقاً خطياً.`:`At about 25°C: [H₃O⁺] ≈ ${h.toExponential(2)} mol/L and [OH⁻] ≈ ${oh.toExponential(2)} mol/L. One pH unit is a tenfold change, not a linear difference.`};
}
function equilibriumOutput(state:BalanceState,locale:Locale){
  const ar=isAr(locale);
  if(state==='add-a') return {title:ar?'استجابة نوعية نحو تكوين B':'Qualitative response toward forming B',body:ar?'إضافة A إلى النموذج تدفع الاستجابة نحو B حتى يستقر النظام مجدداً؛ عند الحالة النهائية يستمر الاتجاهان بمعدلين متساويين، ولا تتغير K ما دامت الحرارة ثابتة.':'Adding A prompts a qualitative shift toward B until the system settles again. At the new equilibrium both directions continue at equal rates, and K is unchanged at fixed temperature.'};
  if(state==='add-b') return {title:ar?'استجابة نوعية نحو تكوين A':'Qualitative response toward forming A',body:ar?'إضافة B إلى النموذج تدفع الاستجابة نحو A حتى يستقر النظام مجدداً؛ عند الحالة النهائية يستمر الاتجاهان بمعدلين متساويين، ولا تتغير K ما دامت الحرارة ثابتة.':'Adding B prompts a qualitative shift toward A until the system settles again. At the new equilibrium both directions continue at equal rates, and K is unchanged at fixed temperature.'};
  return {title:ar?'اتزان ديناميكي':'Dynamic equilibrium',body:ar?'يستمر التفاعل الأمامي والعكسي بالمعدل نفسه؛ تبقى التراكيز ثابتة ظاهرياً، لكن الجسيمات لا تتوقف عن التحول.':'Forward and reverse reactions continue at equal rates. Concentrations stay macroscopically steady, but particles continue to interconvert.'};
}
function moleOutput(value:number,mode:MoleMode,amount:number,count:number,locale:Locale){
  const ar=isAr(locale);
  return mode==='mass'?{title:ar?`${amount.toPrecision(4)} mol من CO₂`:`${amount.toPrecision(4)} mol of CO₂`,body:ar?`${value.toPrecision(4)} g ÷ 44.01 g/mol = ${amount.toPrecision(4)} mol؛ ومنها N = nNₐ ≈ ${count.toExponential(3)} جزيئاً.`:`${value.toPrecision(4)} g ÷ 44.01 g/mol = ${amount.toPrecision(4)} mol; therefore N = nNₐ ≈ ${count.toExponential(3)} molecules.`}:{title:ar?`${count.toExponential(3)} جزيئاً من CO₂`:`${count.toExponential(3)} CO₂ molecules`,body:ar?`${value.toPrecision(4)} mol × 6.02214076×10²³ mol⁻¹ = ${count.toExponential(3)} جزيئاً. ثابت أفوجادرو يحوّل المولات إلى عدد الجسيمات.`:`${value.toPrecision(4)} mol × 6.02214076×10²³ mol⁻¹ = ${count.toExponential(3)} molecules. Avogadro’s constant converts moles to particle count.`};
}
function bondOutput(value:number,side:'left'|'right',locale:Locale){
  const ar=isAr(locale),target=side==='right'?(ar?'الذرة اليمنى':'the right atom'):(ar?'الذرة اليسرى':'the left atom');
  const title=value<0.15?(ar?'تقاسم شبه متساوٍ في هذا التصور':'Nearly even sharing in this model'):(ar?`تميل الكثافة نحو ${target}`:`Density shifts toward ${target}`);
  const body=ar?`مع زيادة Δχ في هذا النموذج تزداد لا مساواة توزيع الكثافة الإلكترونية. الشحنات δ⁺ وδ⁻ جزئية؛ الرابطة طيف متصل ولا توجد عتبة عالمية واحدة تفصل الأنواع.`:`As Δχ increases in this model, the electron-density distribution becomes more unequal. δ⁺ and δ⁻ are partial charges; bonding is a continuum, not split by one universal cutoff.`;
  return {title,body};
}
function modelLimit(kind:SimulationKind,locale:Locale){
  const ar=isAr(locale);
  const notes:Record<SimulationKind,[string,string]>={
    'atomic-charge':['السحابة تمثيل نوعي غير مرسوم بمقياس؛ لا توجد مدارات أو مسارات إلكترونية مرسومة كحقائق.','The cloud is a qualitative, not-to-scale illustration; no electron paths are presented as literal.'],
    energy:['درجات تحول الماء تقريبية عند ضغط يقارب 1 atm؛ الجسيمات ليست مرسومة بمقياس.','Water transition temperatures are approximate near 1 atm; particles are not to scale.'],
    ph:['يفترض النموذج محلولاً مائياً مثالياً عند 25°C؛ pH وحده لا يحدد التركيز الكلي أو السلامة.','Assumes an ideal aqueous solution at 25°C; pH alone does not determine total concentration or safety.'],
    equilibrium:['المعادلة A ⇌ B افتراضية؛ الأعداد لقطة رمزية مباشرة بعد الإضافة، ولا يحسب الرسم التراكيز أو قيمة K.','A ⇌ B is hypothetical; counts are a symbolic immediate post-addition snapshot, and the visual does not calculate concentrations or K.'],
    mole:['يفترض CO₂ نقياً وكتلة مولية 44.01 g/mol؛ أيقونات الجزيئات تمثيلية وليست تعداداً حرفياً.','Assumes pure CO₂ and molar mass 44.01 g/mol; molecule icons are illustrative, not literal counts.'],
    bond:['السالبية وفرقها تقريبيان؛ لا توجد حدود عالمية صارمة لتصنيف الروابط.','Electronegativity and its difference are approximate; no rigid universal bond-classification thresholds exist.'],
  };
  return notes[kind][ar?0:1];
}
