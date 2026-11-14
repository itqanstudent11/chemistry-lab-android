import { useEffect, useMemo, useState } from 'react';
import type { CSSProperties, ReactNode } from 'react';
import {
  ArrowLeft, ArrowLeftRight, Atom, Beaker, BookOpen, Bookmark, Check, ChevronLeft,
  ChevronRight, CircleHelp, Clock3, FlaskConical, Globe2, Home, Layers3, Lightbulb,
  Menu, Microscope, Moon, Network, Search, Settings2, ShieldCheck, Sparkles, Sun,
  Target, Trophy, X, ZoomIn, ZoomOut,
} from 'lucide-react';
import atomLesson from './content-lessons/atom.json';
import matterLesson from './content-lessons/matter.json';
import acidLesson from './content-lessons/acid.json';
import balanceLesson from './content-lessons/balance.json';
import moleLesson from './content-lessons/mole.json';
import bondLesson from './content-lessons/bond.json';
import type { Lesson, LessonText, Locale, SimulationKind, Theme } from './lessonTypes';
import { copy } from './i18n';
import { glossary, goldStages } from './referenceData';

type View = 'home' | 'lessons' | 'gold' | 'glossary' | 'progress' | 'settings' | 'lesson';
type Settings = { locale: Locale; theme: Theme; fontScale: number };

const lessons: Lesson[] = [atomLesson, matterLesson, acidLesson, balanceLesson, moleLesson, bondLesson] as Lesson[];
const defaultSettings: Settings = { locale: 'ar', theme: 'light', fontScale: 1 };
const readStore = <T,>(key: string, fallback: T): T => {
  try { return (JSON.parse(localStorage.getItem(key) || 'null') as T | null) ?? fallback; } catch { return fallback; }
};
const writeStore = (key: string, value: unknown) => {
  try { localStorage.setItem(key, JSON.stringify(value)); } catch { /* storage can be disabled */ }
};

function lessonIcon(kind: SimulationKind) {
  if (kind === 'atomic-charge') return <Atom />;
  if (kind === 'energy') return <Layers3 />;
  if (kind === 'ph') return <Beaker />;
  if (kind === 'equilibrium') return <ArrowLeftRight />;
  if (kind === 'mole') return <FlaskConical />;
  return <Network />;
}

export default function App() {
  const [view, setView] = useState<View>('home');
  const [activeId, setActiveId] = useState('atom');
  const [settings, setSettings] = useState<Settings>(() => ({ ...defaultSettings, ...readStore<Partial<Settings>>('chemistry-settings-v2', {}) }));
  const [completed, setCompleted] = useState<string[]>(() => readStore('chemistry-completed', [] as string[]));
  const [saved, setSaved] = useState<string[]>(() => readStore('chemistry-saved', [] as string[]));
  const [search, setSearch] = useState('');
  const [subject, setSubject] = useState('all');
  const [menuOpen, setMenuOpen] = useState(false);
  const [goldIndex, setGoldIndex] = useState(0);
  const c = copy[settings.locale];
  const activeLesson = lessons.find((lesson) => lesson.id === activeId) ?? lessons[0];
  const activeText = activeLesson[settings.locale];
  const percent = Math.round((completed.length / lessons.length) * 100);
  const subjects = Array.from(new Set(lessons.map((lesson) => lesson[settings.locale].subject)));

  useEffect(() => {
    writeStore('chemistry-settings-v2', settings);
    document.documentElement.lang = settings.locale;
    document.documentElement.dir = settings.locale === 'ar' ? 'rtl' : 'ltr';
    document.documentElement.dataset.theme = settings.theme;
    document.documentElement.style.setProperty('--font-scale', String(settings.fontScale));
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', settings.theme === 'dark' ? '#111c25' : '#f6f7f6');
  }, [settings]);
  useEffect(() => { writeStore('chemistry-completed', completed); }, [completed]);
  useEffect(() => { writeStore('chemistry-saved', saved); }, [saved]);

  const filteredLessons = useMemo(() => lessons.filter((lesson) => {
    const text = lesson[settings.locale];
    const haystack = `${text.title} ${text.subject} ${text.summary} ${text.glossary.map((term) => term.term).join(' ')}`.toLowerCase();
    return haystack.includes(search.trim().toLowerCase()) && (subject === 'all' || text.subject === subject);
  }), [search, subject, settings.locale]);
  const filteredGlossary = useMemo(() => glossary.filter((term) => {
    const text = term[settings.locale];
    return `${text.term} ${text.definition} ${term.category[settings.locale]}`.toLowerCase().includes(search.trim().toLowerCase());
  }), [search, settings.locale]);

  const navigate = (next: View) => { setView(next); setMenuOpen(false); setSearch(''); setSubject('all'); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const openLesson = (id: string) => { setActiveId(id); setView('lesson'); setMenuOpen(false); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const toggleList = (list: string[], setter: (value: string[]) => void, id: string) => setter(list.includes(id) ? list.filter((item) => item !== id) : [...list, id]);
  const navItems = [
    { id: 'home' as const, label: c.nav.home, icon: <Home /> },
    { id: 'lessons' as const, label: c.nav.lessons, icon: <BookOpen /> },
    { id: 'gold' as const, label: c.nav.gold, icon: <Microscope /> },
    { id: 'glossary' as const, label: c.nav.glossary, icon: <Layers3 /> },
    { id: 'progress' as const, label: c.nav.progress, icon: <Trophy /> },
  ];
  const tone = view === 'lesson' ? 'lessons' : view;

  return <div className="app-shell" data-tone={tone} data-theme={settings.theme}>
    <header className={`topbar ${menuOpen ? 'menu-open' : ''}`}>
      <button className="brand" onClick={() => navigate('home')} aria-label={c.nav.home}><span className="brand-mark"><Atom size={24} strokeWidth={1.8} /></span><span className="brand-copy"><strong>{c.brand.name}</strong><small>{c.brand.tagline}</small></span></button>
      <nav className="desktop-nav" aria-label={settings.locale === 'ar' ? 'التنقل الرئيسي' : 'Main navigation'}>{navItems.map((item) => <button key={item.id} className={`nav-link ${view === item.id || (view === 'lesson' && item.id === 'lessons') ? 'active' : ''}`} onClick={() => navigate(item.id)}>{item.label}</button>)}</nav>
      <div className="top-actions"><button className="icon-button settings-shortcut" onClick={() => navigate('settings')} aria-label={c.nav.settings}><Settings2 size={20} /></button><button className="menu-trigger icon-button" onClick={() => setMenuOpen(!menuOpen)} aria-label={menuOpen ? c.common.close : c.nav.lessons}>{menuOpen ? <X size={20} /> : <Menu size={20} />}</button></div>
      {menuOpen && <nav className="mobile-menu">{navItems.map((item) => <button key={item.id} onClick={() => navigate(item.id)}>{item.icon}<span>{item.label}</span><ChevronLeft size={16} /></button>)}<button onClick={() => navigate('settings')}><Settings2 /><span>{c.nav.settings}</span><ChevronLeft size={16} /></button></nav>}
    </header>
    <main id="main-content">
      {view === 'home' && <HomePage c={c} locale={settings.locale} lessons={lessons} completed={completed} progress={percent} onOpen={openLesson} onNavigate={navigate} />}
      {view === 'lessons' && <LessonsPage c={c} locale={settings.locale} lessons={filteredLessons} subjects={subjects} subject={subject} setSubject={setSubject} search={search} setSearch={setSearch} completed={completed} saved={saved} onOpen={openLesson} onSave={(id) => toggleList(saved, setSaved, id)} />}
      {view === 'lesson' && <LessonPage c={c} locale={settings.locale} lesson={activeLesson} text={activeText} done={completed.includes(activeId)} saved={saved.includes(activeId)} onComplete={() => toggleList(completed, setCompleted, activeId)} onSave={() => toggleList(saved, setSaved, activeId)} onOpen={openLesson} onBack={() => navigate('lessons')} />}
      {view === 'gold' && <GoldPage c={c} locale={settings.locale} index={goldIndex} setIndex={setGoldIndex} />}
      {view === 'glossary' && <GlossaryPage c={c} locale={settings.locale} terms={filteredGlossary} search={search} setSearch={setSearch} onOpen={openLesson} />}
      {view === 'progress' && <ProgressPage c={c} lessons={lessons} locale={settings.locale} completed={completed} saved={saved} progress={percent} onOpen={openLesson} onReset={() => { if (window.confirm(c.progressPage.resetConfirm)) { setCompleted([]); setSaved([]); } }} />}
      {view === 'settings' && <SettingsPage c={c} settings={settings} setSettings={setSettings} />}
    </main>
    <footer className="site-footer"><strong>{c.footer.name}</strong><span>{c.footer.tagline}</span><small>{c.footer.version}</small></footer>
    <nav className="bottom-nav" aria-label={settings.locale === 'ar' ? 'التنقل السفلي' : 'Bottom navigation'}>{navItems.map((item) => <button key={item.id} className={view === item.id || (view === 'lesson' && item.id === 'lessons') ? 'active' : ''} onClick={() => navigate(item.id)}>{item.icon}<span>{item.id === 'gold' ? (settings.locale === 'ar' ? 'الذهب' : 'Gold') : item.id === 'progress' ? (settings.locale === 'ar' ? 'تقدّمي' : 'Progress') : item.label}</span></button>)}</nav>
  </div>;
}

function HomePage({ c, locale, lessons, completed, progress, onOpen, onNavigate }: { c: typeof copy.ar | typeof copy.en; locale: Locale; lessons: Lesson[]; completed: string[]; progress: number; onOpen: (id: string) => void; onNavigate: (view: View) => void }) {
  const featured = lessons.find((lesson) => !completed.includes(lesson.id)) ?? lessons[0];
  return <div className="page page-home">
    <section className="home-hero"><div className="hero-copy"><p className="eyebrow"><Sparkles size={16} />{c.home.eyebrow}</p><h1>{c.home.headline}</h1><p>{c.home.intro}</p><button className="button button-primary" onClick={() => onOpen(featured.id)}>{completed.length ? c.common.continue : c.common.start}<ChevronLeft size={18} /></button></div><MoleculeIllustration /><div className="hero-orbit-label"><span>01</span><b>{locale === 'ar' ? 'افهم · تخيّل · اكتشف' : 'Understand · Visualize · Discover'}</b></div></section>
    <section className="home-progress card-surface"><div className="progress-text"><span className="eyebrow">{c.home.progress}</span><h2>{c.home.completedOf.replace('{done}', String(completed.length)).replace('{total}', String(lessons.length))}</h2><div className="progress-track"><span style={{ width: `${progress}%` }} /></div><small>{progress}%</small></div><ProgressDial value={progress} /></section>
    <section className="section-block"><SectionHeading eyebrow={c.home.continueTitle} title={c.home.howLearn} action={c.common.viewAll} onAction={() => onNavigate('lessons')} /><div className="learning-steps"><div><span>01</span><Lightbulb /><strong>{c.home.stepOne}</strong><small>{locale === 'ar' ? 'شرح يربط المصطلح بمعناه.' : 'Connect each term to its meaning.'}</small></div><div><span>02</span><Atom /><strong>{c.home.stepTwo}</strong><small>{locale === 'ar' ? 'مخططات ونماذج بصرية.' : 'Use diagrams and visual models.'}</small></div><div><span>03</span><Target /><strong>{c.home.stepThree}</strong><small>{locale === 'ar' ? 'مثال محلول وسؤال للتفكير.' : 'Work through an example and a question.'}</small></div></div></section>
    <section className="section-block"><SectionHeading eyebrow={c.home.continueTitle} title={c.home.continueSubtitle} action={c.common.viewAll} onAction={() => onNavigate('lessons')} /><div className="course-grid"><button className="course-card course-foundations" onClick={() => onNavigate('lessons')}><span className="course-art"><Atom size={32} /></span><span className="course-overline">{locale === 'ar' ? 'المسار 01 · 6 دروس' : 'PATH 01 · 6 LESSONS'}</span><strong>{c.home.courseBasics}</strong><small>{c.home.courseBasicsText}</small><ChevronLeft className="course-arrow" /></button><button className="course-card course-geology" onClick={() => onNavigate('gold')}><span className="course-art"><Microscope size={32} /></span><span className="course-overline">{locale === 'ar' ? 'رحلة من 8 مراحل' : '8-STAGE JOURNEY'}</span><strong>{c.home.courseGeology}</strong><small>{c.home.courseGeologyText}</small><ChevronLeft className="course-arrow" /></button></div></section>
    <section className="section-block"><SectionHeading eyebrow={c.home.featured} title={featured[locale].title} action={c.common.start} onAction={() => onOpen(featured.id)} /><LessonCard lesson={featured} locale={locale} done={completed.includes(featured.id)} onClick={() => onOpen(featured.id)} /></section>
    <div className="safety-banner"><ShieldCheck /><div><strong>{c.home.safety}</strong><p>{locale === 'ar' ? 'المحتوى يشرح المبادئ ويستخدم نماذج آمنة؛ لا ينفّذ تفاعلات أو معالجة كيميائية فعلية.' : 'Lessons explain principles using safe models; they do not perform real chemical reactions or processing.'}</p></div></div>
  </div>;
}

function MoleculeIllustration() {
  return <div className="molecule-illustration" aria-hidden="true"><div className="molecule-orbit orbit-a" /><div className="molecule-orbit orbit-b" /><div className="molecule-orbit orbit-c" /><span className="molecule-core" /><span className="molecule-electron electron-a" /><span className="molecule-electron electron-b" /><span className="molecule-electron electron-c" /><span className="molecule-label">p⁺</span></div>;
}

function SectionHeading({ eyebrow, title, action, onAction }: { eyebrow: string; title: string; action?: string; onAction?: () => void }) {
  return <div className="section-heading"><div><p className="eyebrow">{eyebrow}</p><h2>{title}</h2></div>{action && onAction && <button className="text-action" onClick={onAction}>{action}<ChevronLeft size={16} /></button>}</div>;
}

function LessonCard({ lesson, locale, done, onClick, onSave, saved }: { lesson: Lesson; locale: Locale; done: boolean; onClick: () => void; onSave?: () => void; saved?: boolean }) {
  const text = lesson[locale];
  return <article className={`lesson-card lesson-${lesson.kind}`}><button className="lesson-card-main" onClick={onClick}><span className="lesson-icon">{lessonIcon(lesson.kind)}</span><span className="lesson-card-text"><span className="lesson-meta">{text.subject}<i />{text.level}<i /><Clock3 size={14} />{text.duration}</span><strong>{text.title}</strong><small>{text.summary}</small></span><span className={`lesson-status ${done ? 'is-done' : ''}`}>{done ? <Check size={17} /> : <ChevronLeft size={18} />}</span></button>{onSave && <button className={`bookmark-button ${saved ? 'is-saved' : ''}`} onClick={onSave} aria-label={saved ? (locale === 'ar' ? 'إزالة من المحفوظات' : 'Remove saved lesson') : (locale === 'ar' ? 'حفظ الدرس' : 'Save lesson')}><Bookmark size={17} fill={saved ? 'currentColor' : 'none'} /></button>}</article>;
}

function LessonsPage({ c, locale, lessons, subjects, subject, setSubject, search, setSearch, completed, saved, onOpen, onSave }: { c: typeof copy.ar | typeof copy.en; locale: Locale; lessons: Lesson[]; subjects: string[]; subject: string; setSubject: (value: string) => void; search: string; setSearch: (value: string) => void; completed: string[]; saved: string[]; onOpen: (id: string) => void; onSave: (id: string) => void }) {
  return <div className="page page-lessons"><PageHeader eyebrow={c.lessons.eyebrow} title={c.lessons.title} description={c.lessons.intro} /><label className="search-field"><Search size={19} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={c.common.search} /><span>{lessons.length}</span></label>
    <div className="filter-strip" aria-label={c.lessons.filters}><button className={subject === 'all' ? 'selected' : ''} onClick={() => setSubject('all')}>{c.common.all}</button>{subjects.map((item) => <button key={item} className={subject === item ? 'selected' : ''} onClick={() => setSubject(item)}>{item}</button>)}</div>
    <div className="results-heading"><strong>{c.lessons.results} <span>{lessons.length}</span></strong><small>{locale === 'ar' ? 'كل درس يضم شرحاً ومثالاً ونشاطاً واختباراً' : 'Every lesson includes explanation, an example, an activity, and a quiz'}</small></div>
    <div className="lesson-list">{lessons.map((lesson) => <LessonCard key={lesson.id} lesson={lesson} locale={locale} done={completed.includes(lesson.id)} saved={saved.includes(lesson.id)} onClick={() => onOpen(lesson.id)} onSave={() => onSave(lesson.id)} />)}</div>{!lessons.length && <EmptyState title={c.lessons.emptyTitle} text={c.lessons.emptyText} />}
  </div>;
}

function PageHeader({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return <div className="page-header"><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{description}</p></div>;
}

function LessonPage({ c, locale, lesson, text, done, saved, onComplete, onSave, onOpen, onBack }: { c: typeof copy.ar | typeof copy.en; locale: Locale; lesson: Lesson; text: LessonText; done: boolean; saved: boolean; onComplete: () => void; onSave: () => void; onOpen: (id: string) => void; onBack: () => void }) {
  const [choice, setChoice] = useState<number | null>(null);
  useEffect(() => setChoice(null), [lesson.id]);
  const anchors = ['intro', 'objectives', 'map', 'explanation', 'equation', 'example', 'compare', 'simulation', 'applications', 'misconception', 'quiz', 'safety', 'summary', 'glossary'];
  const labels = [c.common.why, c.common.objective, c.common.conceptMap, c.common.explanation, c.common.equation, c.common.workedExample, c.common.compare, c.common.simulation, c.common.applications, c.common.misconception, c.common.quiz, c.common.safety, c.common.summary, c.common.glossary];
  const nextLesson = lessons[(lessons.findIndex((item) => item.id === lesson.id) + 1) % lessons.length];
  return <div className={`page page-lesson lesson-${lesson.kind}`}>
    <button className="back-button" onClick={onBack}><ArrowLeft size={17} />{c.common.back}</button>
    <div className="lesson-hero"><div className="lesson-hero-symbol">{lessonIcon(lesson.kind)}</div><div className="lesson-hero-copy"><div className="lesson-meta-line"><span>{text.subject}</span><i /><span>{text.level}</span><i /><span><Clock3 size={15} />{text.duration}</span></div><h1>{text.title}</h1><p>{text.summary}</p><div className="lesson-hero-actions"><button className={`button ${done ? 'button-complete' : 'button-primary'}`} onClick={onComplete}>{done ? <Check size={17} /> : <Target size={17} />}{done ? c.common.completed : c.common.markComplete}</button><button className={`button button-quiet ${saved ? 'is-saved' : ''}`} onClick={onSave}><Bookmark size={17} fill={saved ? 'currentColor' : 'none'} />{saved ? c.common.saved : c.common.save}</button></div></div></div>
    <div className="lesson-layout"><aside className="lesson-toc"><strong>{c.lesson.tableContents}</strong><small>{c.lesson.jumpTo}</small>{anchors.map((anchor, index) => <a key={anchor} href={`#${anchor}`}><span>{String(index + 1).padStart(2, '0')}</span>{labels[index]}</a>)}</aside>
      <article className="lesson-body">
        <section id="intro" className="learning-card intro-card"><SectionLabel number="01" title={c.common.why} icon={<Sparkles />} /><h2>{text.goal}</h2><p className="lead-text">{text.why}</p><div className="prerequisite-box"><CircleHelp size={18} /><div><strong>{c.common.prerequisite}</strong><p>{text.prerequisite}</p></div></div></section>
        <section id="objectives" className="learning-card"><SectionLabel number="02" title={c.common.objective} icon={<Target />} /><h2>{c.common.takeaways}</h2><ul className="objective-list">{text.objectives.map((item, index) => <li key={item}><span>{String(index + 1).padStart(2, '0')}</span>{item}</li>)}</ul></section>
        <section id="map" className="learning-card"><SectionLabel number="03" title={c.common.conceptMap} icon={<Network />} /><div className="concept-map">{text.conceptMap.map((node, index) => <div className="concept-node" key={node.label}><span className={`concept-node-icon node-${index}`}>{index === 0 ? <Atom /> : index === 1 ? <ArrowLeftRight /> : <Sparkles />}</span><strong>{node.label}</strong><small>{node.detail}</small>{index < text.conceptMap.length - 1 && <ChevronLeft className="concept-arrow" />}</div>)}</div></section>
        <section id="explanation" className="learning-card"><SectionLabel number="04" title={c.common.explanation} icon={<BookOpen />} /><div className="explanation-list">{text.explanation.map((item, index) => <article className="explanation-step" key={item.heading}><span className="step-number">{String(index + 1).padStart(2, '0')}</span><div><h3>{item.heading}</h3><p>{item.body}</p></div></article>)}</div></section>
        <section id="equation" className="learning-card equation-card"><SectionLabel number="05" title={c.common.equation} icon={<FlaskConical />} /><h2>{text.equation.heading}</h2><div className="equation-display" dir="ltr">{text.equation.expression}</div><p>{text.equation.note}</p><div className="symbol-list">{text.equation.symbols.map((symbol) => <div key={symbol.symbol}><b dir="ltr">{symbol.symbol}</b><span>{symbol.meaning}</span><small>{symbol.unit}</small></div>)}</div></section>
        <section id="example" className="learning-card example-card"><SectionLabel number="06" title={c.common.workedExample} icon={<Lightbulb />} /><h2>{text.workedExample.heading}</h2><p className="example-problem">{text.workedExample.problem}</p><div className="example-steps">{text.workedExample.steps.map((step, index) => <div key={step.label}><span>{index + 1}</span><div><strong>{step.label}</strong><p>{step.body}</p></div></div>)}</div><div className="example-answer"><Check size={18} /><div><strong>{c.common.answer}: {text.workedExample.answer}</strong><p>{text.workedExample.takeaway}</p></div></div></section>
        <section id="compare" className="learning-card"><SectionLabel number="07" title={c.common.compare} icon={<ArrowLeftRight />} /><h2>{text.comparison.heading}</h2><div className="comparison-table"><div className="comparison-row comparison-head">{text.comparison.columns.map((column) => <strong key={column}>{column}</strong>)}</div>{text.comparison.rows.map((row) => <div className="comparison-row" key={row.label}><strong>{row.label}</strong>{row.values.map((value, index) => <span key={`${row.label}-${index}`}>{value}</span>)}</div>)}</div></section>
        <section id="simulation" className="learning-card simulation-card"><SectionLabel number="08" title={c.common.simulation} icon={<Atom />} /><Simulation c={c} locale={locale} kind={lesson.kind} /></section>
        <section id="applications" className="learning-card"><SectionLabel number="09" title={c.common.applications} icon={<Microscope />} /><div className="application-grid">{text.applications.map((item, index) => <article key={item.title}><span>{index === 0 ? <Beaker /> : <Target />}</span><h3>{item.title}</h3><p>{item.body}</p></article>)}</div></section>
        <section id="misconception" className="learning-card misconception-card"><SectionLabel number="10" title={c.common.misconception} icon={<CircleHelp />} /><div className="myth-grid"><div><span>{locale === 'ar' ? 'قد تسمع' : 'You may hear'}</span><p>“{text.misconception.claim}”</p></div><div><Check size={19} /><div><strong>{locale === 'ar' ? 'التفسير الأدق' : 'A better explanation'}</strong><p>{text.misconception.correction}</p></div></div></div></section>
        <section id="quiz" className="learning-card quiz-card"><SectionLabel number="11" title={c.common.quiz} icon={<Target />} /><h2>{text.quiz.question}</h2><div className="quiz-options">{text.quiz.options.map((option, index) => <button key={option} className={`${choice === index ? 'chosen' : ''} ${choice !== null && index === text.quiz.correctIndex ? 'is-correct' : ''}`} onClick={() => setChoice(index)}><span>{String.fromCharCode(65 + index)}</span><span>{option}</span>{choice !== null && index === text.quiz.correctIndex && <Check size={17} />}</button>)}</div>{choice !== null && <div className={`quiz-feedback ${choice === text.quiz.correctIndex ? 'success' : 'review'}`}><strong>{choice === text.quiz.correctIndex ? c.common.correct : c.common.tryAgain}</strong><p>{text.quiz.explanation}</p></div>}</section>
        <section id="safety" className="learning-card safety-card"><SectionLabel number="12" title={c.common.safety} icon={<ShieldCheck />} /><h2>{text.safety.title}</h2><p>{text.safety.body}</p></section>
        <section id="summary" className="learning-card summary-card"><SectionLabel number="13" title={c.common.summary} icon={<Check />} /><div className="summary-grid">{text.summaryPoints.map((item, index) => <div key={item}><span>{String(index + 1).padStart(2, '0')}</span><p>{item}</p></div>)}</div></section>
        <section id="glossary" className="learning-card"><SectionLabel number="14" title={c.common.glossary} icon={<Layers3 />} /><div className="lesson-glossary">{text.glossary.map((term) => <article key={term.term}><strong>{term.term}</strong><p>{term.definition}</p></article>)}</div></section>
        <section className="next-lesson-card"><div><span>{c.common.related}</span><h2>{nextLesson[locale].title}</h2><p>{nextLesson[locale].summary}</p></div><button className="button button-primary" onClick={() => onOpen(nextLesson.id)}>{c.common.next}<ChevronLeft size={17} /></button></section>
      </article></div>
  </div>;
}

function SectionLabel({ number, title, icon }: { number: string; title: string; icon: ReactNode }) {
  return <div className="section-label"><span>{number}</span><span className="section-label-icon">{icon}</span><strong>{title}</strong></div>;
}

function Simulation({ c, locale, kind }: { c: typeof copy.ar | typeof copy.en; locale: Locale; kind: SimulationKind }) {
  const configs: Record<SimulationKind, { min: number; max: number; step: number; initial: number; labelAr: string; labelEn: string; unitAr: string; unitEn: string }> = {
    'atomic-charge': { min: 0, max: 12, step: 1, initial: 6, labelAr: 'عدد الإلكترونات', labelEn: 'Electron count', unitAr: 'إلكترون', unitEn: 'electrons' },
    energy: { min: -20, max: 120, step: 1, initial: 25, labelAr: 'درجة حرارة الماء عند ضغط جوي واحد', labelEn: 'Water temperature at 1 atm', unitAr: '°م', unitEn: '°C' },
    ph: { min: 0, max: 14, step: 0.1, initial: 7, labelAr: 'قيمة الأس الهيدروجيني', labelEn: 'pH value', unitAr: 'pH', unitEn: 'pH' },
    equilibrium: { min: 0, max: 100, step: 1, initial: 50, labelAr: 'كمية ناتج مضاف إلى نموذج الاتزان', labelEn: 'Product added to the equilibrium model', unitAr: '%', unitEn: '%' },
    mole: { min: 0, max: 88, step: 0.1, initial: 44, labelAr: 'كتلة ثاني أكسيد الكربون', labelEn: 'Mass of carbon dioxide', unitAr: 'غ', unitEn: 'g' },
    bond: { min: 0, max: 3.5, step: 0.1, initial: 1.2, labelAr: 'فرق السالبية الكهربائية التقريبي', labelEn: 'Approximate electronegativity difference', unitAr: 'ΔEN', unitEn: 'ΔEN' },
  };
  const config = configs[kind];
  const [value, setValue] = useState(config.initial);
  useEffect(() => setValue(config.initial), [kind]);
  const current = getSimulationResult(kind, value, locale);
  const displayValue = kind === 'ph' || kind === 'bond' || kind === 'mole' ? value.toFixed(1) : String(Math.round(value));
  return <div className={`simulation-panel sim-${kind}`}><div className="simulation-head"><div><span className="live-pill"><i />{locale === 'ar' ? 'نموذج آمن' : 'Safe model'}</span><h2>{locale === 'ar' ? 'حرّك المتغير، ثم فسّر النتيجة' : 'Change one variable and interpret the result'}</h2><p>{locale === 'ar' ? 'هذا نموذج نوعي يساعد على التفكير، وليس بديلاً عن قياس تجريبي.' : 'This qualitative model supports reasoning; it is not a substitute for measurement.'}</p></div><span className="simulation-symbol">{lessonIcon(kind)}</span></div><SimulationArt kind={kind} value={value} />
    <div className="simulation-control"><div className="simulation-value"><label htmlFor={`sim-${kind}`}>{locale === 'ar' ? config.labelAr : config.labelEn}</label><strong dir="ltr">{displayValue} <small>{locale === 'ar' ? config.unitAr : config.unitEn}</small></strong></div><input id={`sim-${kind}`} type="range" min={config.min} max={config.max} step={config.step} value={value} onChange={(event) => setValue(Number(event.target.value))} aria-label={locale === 'ar' ? config.labelAr : config.labelEn} /><div className="range-ends" dir="ltr"><span>{config.min}</span><span>{config.max}</span></div></div>
    <div className="simulation-result"><span>{locale === 'ar' ? 'ما الذي نلاحظه؟' : 'What do we observe?'}</span><strong>{current.title}</strong><p>{current.body}</p></div><p className="simulation-footnote"><Lightbulb size={15} />{simulationNote(kind, locale)}</p><p className="screenreader-hint">{c.lesson.interactionHint}</p>
  </div>;
}

function getSimulationResult(kind: SimulationKind, value: number, locale: Locale) {
  if (kind === 'atomic-charge') {
    const charge = 6 - Math.round(value);
    if (locale === 'ar') return { title: charge === 0 ? 'ذرة كربون متعادلة' : `أيون كربون شحنته ${charge > 0 ? `${charge}+` : `${Math.abs(charge)}−`}`, body: `عدد البروتونات ثابت عند 6، والشحنة الصافية = 6 − ${Math.round(value)} = ${charge > 0 ? `${charge}+` : charge < 0 ? `${Math.abs(charge)}−` : '0'}. يتغير عدد الإلكترونات والشحنة، لكن هوية العنصر لا تتغير.` };
    return { title: charge === 0 ? 'Neutral carbon atom' : `Carbon ion with charge ${charge > 0 ? `${charge}+` : `${Math.abs(charge)}−`}`, body: `The proton count stays at 6. Net charge = 6 − ${Math.round(value)} = ${charge > 0 ? `${charge}+` : charge < 0 ? `${Math.abs(charge)}−` : '0'}. Electron count and charge change, but the element identity does not.` };
  }
  if (kind === 'energy') {
    const phaseAr = value < 0 ? 'جليد صلب' : value < 100 ? 'ماء سائل' : 'بخار ماء';
    const phaseEn = value < 0 ? 'solid ice' : value < 100 ? 'liquid water' : 'water vapor';
    return locale === 'ar' ? { title: phaseAr, body: value < 0 ? 'في هذا النموذج للماء النقي عند ضغط جوي واحد، تقع درجة الحرارة دون نقطة الانصهار التقريبية؛ تتحرك الجسيمات اهتزازياً حول مواقعها.' : value < 100 ? 'في هذا النموذج تكون الحالة السائلة؛ تتحرك الجسيمات وتغيّر جيرانها مع بقائها متقاربة.' : 'في هذا النموذج تقع الحرارة فوق درجة الغليان التقريبية للماء عند ضغط جوي واحد، وتكون الجسيمات متباعدة أكثر.' } : { title: phaseEn, body: value < 0 ? 'For pure water at approximately 1 atm, this is below the approximate melting point; particles mainly vibrate around positions.' : value < 100 ? 'The model shows liquid water: particles move and exchange neighbors while remaining close together.' : 'The model is above water’s approximate boiling point at 1 atm, so particles are much farther apart.' };
  }
  if (kind === 'ph') {
    const group = value < 7 ? (locale === 'ar' ? 'محلول حمضي' : 'Acidic solution') : value > 7 ? (locale === 'ar' ? 'محلول قاعدي' : 'Basic solution') : (locale === 'ar' ? 'متعادل تقريباً' : 'Approximately neutral');
    return { title: group, body: locale === 'ar' ? 'مقياس pH لوغاريتمي؛ فرق وحدة واحدة يقابل تغيراً بمقدار عشرة أضعاف في نشاط أيون الهيدرونيوم. لا تعني القيمة وحدها أن المحلول مركز أو آمن.' : 'The pH scale is logarithmic; a one-unit difference corresponds to a tenfold change in hydronium-ion activity. The value alone does not tell you concentration or safety.' };
  }
  if (kind === 'equilibrium') return value > 55 ? (locale === 'ar' ? { title: 'يميل النموذج نحو المتفاعلات', body: 'زيادة ناتج في نظام متزن تدفع الاستجابة النوعية نحو استهلاك جزء منه. ثابت الاتزان K لا يتغير بتغيير التركيز وحده عند ثبات درجة الحرارة.' } : { title: 'The model shifts toward reactants', body: 'Adding a product to an equilibrium system qualitatively favors a response that consumes some of it. At constant temperature, changing concentration alone does not change K.' }) : value < 45 ? (locale === 'ar' ? { title: 'ناتج أقل: قد يتكون المزيد منه', body: 'إزالة جزء من الناتج قد تجعل النظام يستجيب بتكوين المزيد منه. هذا وصف نوعي لمبدأ لوشاتيليه، وليس حساباً لتركيب الاتزان النهائي.' } : { title: 'Less product: the system may form more', body: 'Removing some product may cause the system to respond by forming more. This is a qualitative illustration of Le Châtelier’s principle, not a calculation of final composition.' }) : (locale === 'ar' ? { title: 'قريب من حالة البداية', body: 'عند عدم إحداث اضطراب كبير، تبقى تراكيز النظام ثابتة ظاهرياً بينما يستمر التفاعلان الأمامي والعكسي بالسرعة نفسها.' } : { title: 'Near the starting condition', body: 'Without a substantial disturbance, concentrations remain macroscopically steady while forward and reverse reactions continue at equal rates.' });
  if (kind === 'mole') {
    const n = value / 44.01; const particles = n * 6.02214076e23;
    return locale === 'ar' ? { title: `${n.toFixed(3)} مول من CO₂ تقريباً`, body: `${value.toFixed(1)} g ÷ 44.01 g/mol = ${n.toFixed(3)} mol، أي نحو ${particles.toExponential(2)} جزيئاً باستخدام ثابت أفوجادرو.` } : { title: `About ${n.toFixed(3)} mol of CO₂`, body: `${value.toFixed(1)} g ÷ 44.01 g/mol = ${n.toFixed(3)} mol, or about ${particles.toExponential(2)} molecules using the Avogadro constant.` };
  }
  const type = value < 0.4 ? (locale === 'ar' ? 'تساهمية غير قطبية تقريباً' : 'approximately nonpolar covalent') : value < 1.8 ? (locale === 'ar' ? 'تساهمية قطبية غالباً' : 'often polar covalent') : (locale === 'ar' ? 'تزداد الصفة الأيونية' : 'increasing ionic character');
  return locale === 'ar' ? { title: type, body: 'تقسيم تقريبي لفرق السالبية الكهربائية؛ نوع الترابط طيف مستمر، والحدود تختلف بين المراجع ولا تحدد وحدها خواص المادة.' } : { title: type, body: 'A rough classification by electronegativity difference. Bonding is a continuum; cutoffs vary among references and do not alone determine material properties.' };
}

function simulationNote(kind: SimulationKind, locale: Locale) {
  if (kind === 'energy') return locale === 'ar' ? 'نقاط تحول الماء تقريبية وتتأثر بالضغط والمواد الذائبة.' : 'Water transition points are approximate and depend on pressure and dissolved substances.';
  if (kind === 'ph') return locale === 'ar' ? 'التصنيف تقريبي للمحاليل المائية قرب درجة حرارة الغرفة.' : 'This classification is approximate for aqueous solutions near room temperature.';
  if (kind === 'mole') return locale === 'ar' ? 'استخدمت كتلة مولية تقريبية لثاني أكسيد الكربون: 44.01 g/mol.' : 'The approximate molar mass used for carbon dioxide is 44.01 g/mol.';
  if (kind === 'equilibrium') return locale === 'ar' ? 'لا يعرض النموذج تراكيز نهائية؛ يعتمد الاتزان على المعادلة والحرارة.' : 'The model does not calculate final concentrations; equilibrium depends on the reaction and temperature.';
  if (kind === 'bond') return locale === 'ar' ? 'حدود التصنيف ليست قوانين فاصلة؛ استخدمها أداة تقريبية للفهم.' : 'Classification cutoffs are not sharp laws; use them only as a rough learning aid.';
  return locale === 'ar' ? 'الشحنة بوحدات الشحنة الأولية؛ تبقى النواة ثابتة في هذا النموذج.' : 'Charge is given in elementary-charge units; the nucleus is fixed in this model.';
}

function SimulationArt({ kind, value }: { kind: SimulationKind; value: number }) {
  if (kind === 'atomic-charge') return <div className="simulation-art atom-art"><div className="sim-orbit sim-orbit-a" /><div className="sim-orbit sim-orbit-b" /><div className="sim-nucleus"><span>6p⁺</span><small>6n⁰</small></div>{Array.from({ length: Math.round(value) }, (_, index) => <i className={`sim-electron electron-${index % 8}`} key={index} />)}</div>;
  if (kind === 'energy') return <div className={`simulation-art particle-art ${value < 0 ? 'solid' : value < 100 ? 'liquid' : 'gas'}`}>{Array.from({ length: 12 }, (_, index) => <i key={index} style={{ '--particle-i': index } as unknown as CSSProperties} />)}<span>{Math.round(value)}°</span></div>;
  if (kind === 'ph') return <div className="simulation-art ph-art"><div className="ph-spectrum" /><div className="ph-pointer" style={{ insetInlineStart: `${Math.max(0, Math.min(100, value / 14 * 100))}%` }} /><div className="ph-labels" dir="ltr"><span>0</span><span>7</span><span>14</span></div><strong>{value.toFixed(1)}</strong></div>;
  if (kind === 'equilibrium') return <div className="simulation-art balance-art"><div className="balance-pan pan-a" style={{ transform: `translateY(${(value - 50) / 6}px)` }}><span>Reactants</span><i /><i /><i /></div><div className="balance-axis" /><div className="balance-pan pan-b" style={{ transform: `translateY(${(50 - value) / 6}px)` }}><span>Products</span><i /><i /><i /></div></div>;
  if (kind === 'mole') return <div className="simulation-art mole-art"><div className="mole-container">{Array.from({ length: Math.max(1, Math.round(value / 7.34)) }, (_, index) => <i key={index} />)}</div><span className="mole-number">{(value / 44.01).toFixed(2)} mol</span><small>CO₂</small></div>;
  return <div className="simulation-art bond-art"><div className="bond-atom atom-left"><span>δ⁺</span></div><div className="bond-cloud" style={{ opacity: 0.4 + Math.min(value / 3.5, 1) * 0.55 }}><i /><i /></div><div className="bond-atom atom-right"><span>δ⁻</span></div><div className="bond-labels"><small>{value.toFixed(1)} ΔEN</small><small>shared electron density</small></div></div>;
}

function GoldPage({ c, locale, index, setIndex }: { c: typeof copy.ar | typeof copy.en; locale: Locale; index: number; setIndex: (index: number) => void }) {
  const item = goldStages[index]; const stage = item[locale];
  return <div className="page page-gold"><section className="gold-hero"><div className="gold-copy"><p className="eyebrow"><Microscope size={16} />{c.gold.eyebrow}</p><h1>{c.gold.title}</h1><p>{c.gold.intro}</p><div className="gold-badges"><span>{locale === 'ar' ? 'جيولوجيا' : 'Geology'}</span><span>{locale === 'ar' ? 'تحقق علمي' : 'Scientific verification'}</span><span>{locale === 'ar' ? 'سلامة بيئية' : 'Environmental safety'}</span></div></div><div className="gold-medallion" aria-hidden="true"><span>Au</span><small>79</small><i /></div></section>
    <section className="gold-purpose"><span><Lightbulb /></span><div><strong>{c.gold.useTitle}</strong><p>{c.gold.useText}</p></div></section><div className="gold-journey"><div className="gold-stage-list">{goldStages.map((entry, stageIndex) => <button key={entry.id} className={`gold-stage ${index === stageIndex ? 'selected' : ''}`} onClick={() => setIndex(stageIndex)}><span>{String(stageIndex + 1).padStart(2, '0')}</span><div><strong>{entry[locale].title}</strong><small>{entry[locale].short}</small></div><ChevronLeft size={17} /></button>)}</div>
      <article className="gold-detail"><div className="gold-detail-top"><span>{c.gold.stage} {String(index + 1).padStart(2, '0')} {c.gold.of} 08</span><span className="gold-detail-icon"><Microscope /></span></div><h2>{stage.title}</h2><p className="gold-detail-text">{stage.detail}</p><div className="gold-evidence"><div><strong>{c.gold.question}</strong><p>{locale === 'ar' ? 'ما الدليل الذي يدعم التفسير، وما البدائل الممكنة؟' : 'What evidence supports the interpretation, and what alternatives exist?'}</p></div><div><strong>{c.gold.note}</strong><p>{stage.note}</p></div></div><div className="gold-flow"><span>{c.gold.question}</span><ChevronLeft /><span>{c.gold.observation}</span><ChevronLeft /><span>{c.gold.evidence}</span></div><div className="gold-caution"><ShieldCheck /><p>{c.gold.caution}</p></div><div className="gold-controls"><button disabled={index === 0} onClick={() => setIndex(Math.max(0, index - 1))}><ChevronRight />{c.gold.previous}</button><button disabled={index === goldStages.length - 1} onClick={() => setIndex(Math.min(goldStages.length - 1, index + 1))}>{c.gold.next}<ChevronLeft /></button></div></article></div>
    <div className="gold-principles">{c.gold.principles.map((principle, principleIndex) => <div key={principle}><span>0{principleIndex + 1}</span><strong>{principle}</strong><small>{locale === 'ar' ? ['الوصف ليس إثباتاً للهوية.', 'النتائج تحتاج طريقة مناسبة وضبط جودة.', 'حماية الإنسان والبيئة شرط في العمل العلمي.'][principleIndex] : ['A description is not an identity test.', 'Results need suitable methods and quality control.', 'Protecting people and ecosystems is part of science.'][principleIndex]}</small></div>)}</div>
  </div>;
}

function GlossaryPage({ c, locale, terms, search, setSearch, onOpen }: { c: typeof copy.ar | typeof copy.en; locale: Locale; terms: typeof glossary[number][]; search: string; setSearch: (value: string) => void; onOpen: (id: string) => void }) {
  return <div className="page page-glossary"><PageHeader eyebrow={c.glossaryPage.eyebrow} title={c.glossaryPage.title} description={c.glossaryPage.intro} /><label className="search-field"><Search size={19} /><input value={search} onChange={(event) => setSearch(event.target.value)} placeholder={c.common.searchGlossary} /><span>{terms.length}</span></label><div className="glossary-summary"><strong>{terms.length}</strong><span>{c.glossaryPage.count}</span><small>{locale === 'ar' ? 'مرتبطة بمسارات التعلّم الستة' : 'Connected to all six learning paths'}</small></div><div className="glossary-grid">{terms.map((term) => <article className="glossary-card" key={term.id}><div className="glossary-card-head"><span>{term.category[locale]}</span><button onClick={() => onOpen(term.lessonId)} aria-label={`${c.glossaryPage.related}: ${term[locale].term}`}><ChevronLeft size={18} /></button></div><h2>{term[locale].term}</h2><p>{term[locale].definition}</p><button className="glossary-related" onClick={() => onOpen(term.lessonId)}>{c.glossaryPage.related}<ChevronLeft size={15} /></button></article>)}</div>{!terms.length && <EmptyState title={c.common.noResults} text={c.common.trySearch} />}</div>;
}

function ProgressPage({ c, lessons, locale, completed, saved, progress, onOpen, onReset }: { c: typeof copy.ar | typeof copy.en; lessons: Lesson[]; locale: Locale; completed: string[]; saved: string[]; progress: number; onOpen: (id: string) => void; onReset: () => void }) {
  const doneLessons = lessons.filter((lesson) => completed.includes(lesson.id)); const savedLessons = lessons.filter((lesson) => saved.includes(lesson.id));
  return <div className="page page-progress"><PageHeader eyebrow={c.progressPage.eyebrow} title={c.progressPage.title} description={c.progressPage.intro} /><section className="profile-overview"><div className="profile-avatar"><Atom /></div><div className="profile-greeting"><span>{c.progressPage.learner}</span><h2>{c.progressPage.hello}</h2><small>{c.progressPage.continue}</small></div><ProgressDial value={progress} /><span className="profile-progress-label">{c.progressPage.completion}</span></section><div className="progress-metrics"><div><BookOpen /><strong>{completed.length}</strong><span>{c.progressPage.finished}</span></div><div><Bookmark /><strong>{saved.length}</strong><span>{c.progressPage.bookmarked}</span></div><div><Target /><strong>{progress}%</strong><span>{c.progressPage.completion}</span></div></div>
    <section className="section-block"><SectionHeading eyebrow={c.progressPage.finished} title={c.progressPage.finished} />{doneLessons.length ? <div className="lesson-list">{doneLessons.map((lesson) => <LessonCard key={lesson.id} lesson={lesson} locale={locale} done onClick={() => onOpen(lesson.id)} />)}</div> : <EmptyState title={c.progressPage.emptyDone} text={c.home.intro} />}</section><section className="section-block"><SectionHeading eyebrow={c.progressPage.bookmarked} title={c.progressPage.bookmarked} />{savedLessons.length ? <div className="lesson-list">{savedLessons.map((lesson) => <LessonCard key={lesson.id} lesson={lesson} locale={locale} done={completed.includes(lesson.id)} onClick={() => onOpen(lesson.id)} />)}</div> : <EmptyState title={c.progressPage.emptySaved} text={c.common.save} />}</section><div className="privacy-note"><ShieldCheck />{c.progressPage.privacy}</div><button className="reset-button" onClick={onReset}>{c.progressPage.reset}</button>
  </div>;
}

function SettingsPage({ c, settings, setSettings }: { c: typeof copy.ar | typeof copy.en; settings: Settings; setSettings: (settings: Settings) => void }) {
  const update = (patch: Partial<Settings>) => setSettings({ ...settings, ...patch });
  return <div className="page page-settings"><PageHeader eyebrow={c.settings.eyebrow} title={c.settings.title} description={c.settings.intro} />
    <section className="settings-card"><div className="settings-title"><span className="settings-icon"><Sun /></span><div><h2>{c.settings.appearance}</h2><p>{c.settings.themeHint}</p></div></div><div className="choice-row"><button className={settings.theme === 'light' ? 'selected' : ''} onClick={() => update({ theme: 'light' })}><Sun />{c.settings.light}<span>{settings.theme === 'light' && <Check />}</span></button><button className={settings.theme === 'dark' ? 'selected' : ''} onClick={() => update({ theme: 'dark' })}><Moon />{c.settings.dark}<span>{settings.theme === 'dark' && <Check />}</span></button></div></section>
    <section className="settings-card"><div className="settings-title"><span className="settings-icon"><Globe2 /></span><div><h2>{c.settings.language}</h2><p>{c.settings.languageHint}</p></div></div><div className="choice-row"><button className={settings.locale === 'ar' ? 'selected' : ''} onClick={() => update({ locale: 'ar' })}><span className="language-mark">ع</span>{c.settings.arabic}<span>{settings.locale === 'ar' && <Check />}</span></button><button className={settings.locale === 'en' ? 'selected' : ''} onClick={() => update({ locale: 'en' })}><span className="language-mark">EN</span>{c.settings.english}<span>{settings.locale === 'en' && <Check />}</span></button></div></section>
    <section className="settings-card"><div className="settings-title"><span className="settings-icon"><ZoomIn /></span><div><h2>{c.settings.typeSize}</h2><p>{c.settings.sizeHint}</p></div></div><div className="font-control"><button onClick={() => update({ fontScale: Math.max(0.9, Math.round((settings.fontScale - 0.05) * 100) / 100) })} disabled={settings.fontScale <= 0.9} aria-label={c.settings.smaller}><ZoomOut /></button><input aria-label={c.settings.typeSize} type="range" min="0.9" max="1.3" step="0.05" value={settings.fontScale} onChange={(event) => update({ fontScale: Number(event.target.value) })} /><button onClick={() => update({ fontScale: Math.min(1.3, Math.round((settings.fontScale + 0.05) * 100) / 100) })} disabled={settings.fontScale >= 1.3} aria-label={c.settings.larger}><ZoomIn /></button><strong>{Math.round(settings.fontScale * 100)}%</strong></div><div className="font-preview"><span>{c.settings.fontScale}</span><p>{c.settings.accessibilityText}</p></div></section>
    <section className="accessibility-note"><ShieldCheck /><div><strong>{c.settings.accessibility}</strong><p>{c.settings.accessibilityText}</p></div><button onClick={() => setSettings(defaultSettings)}>{c.settings.reset}</button></section><p className="settings-saved"><Check size={16} />{c.settings.saved}</p>
  </div>;
}

function ProgressDial({ value }: { value: number }) {
  return <div className="progress-dial" style={{ '--progress': `${value}%` } as unknown as CSSProperties}><span>{value}%</span></div>;
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return <div className="empty-state"><span><Search size={21} /></span><strong>{title}</strong><p>{text}</p></div>;
}
