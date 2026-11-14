import { useMemo, useState } from 'react';
import {
  ArrowDown, ArrowLeft, Atom, Beaker, BookOpen, Bookmark, Check, ChevronLeft,
  ChevronRight, CircleHelp, Clock3, FlaskConical, Home, Layers3,
  Lightbulb, Menu, Microscope, Network, Search, ShieldCheck, Sparkles, Target,
  Trophy, Waves, X, Zap,
} from 'lucide-react';

type Tab = 'home' | 'lessons' | 'gold' | 'glossary' | 'profile' | 'lesson';
type Lesson = { id: string; title: string; subject: string; duration: string; level: string; color: string; icon: string; summary: string };

const lessons: Lesson[] = [
  { id: 'atom', title: 'من الذرة إلى الرابطة', subject: 'أساسيات الكيمياء', duration: '١٢ دقيقة', level: 'تمهيدي', color: 'mint', icon: 'atom', summary: 'اكتشف كيف تحدد البروتونات هوية العنصر، وكيف تمنح الإلكترونات الذرة قدرتها على الارتباط.' },
  { id: 'matter', title: 'المادة وتغيراتها', subject: 'المادة والطاقة', duration: '٩ دقائق', level: 'تمهيدي', color: 'sand', icon: 'layers', summary: 'ميّز بين التغير الفيزيائي والكيميائي عبر أمثلة من المطبخ والطبيعة.' },
  { id: 'acid', title: 'الأحماض والقواعد', subject: 'المحاليل', duration: '١٥ دقيقة', level: 'أساسي', color: 'lilac', icon: 'drop', summary: 'افهم مقياس الأس الهيدروجيني والفرق بين قوة الحمض وتركيزه.' },
  { id: 'balance', title: 'الاتزان الكيميائي', subject: 'التفاعلات', duration: '١٨ دقيقة', level: 'متوسط', color: 'blue', icon: 'balance', summary: 'تابع التفاعل في الاتجاهين، وتعرّف كيف يستجيب النظام لتغيّر الظروف.' },
  { id: 'mole', title: 'المول والحسابات', subject: 'الحسابات الكيميائية', duration: '١٦ دقيقة', level: 'أساسي', color: 'gold', icon: 'flask', summary: 'اربط عدد الجسيمات بكمية المادة والكتلة المولية خطوة بخطوة.' },
  { id: 'bond', title: 'أنواع الروابط', subject: 'الترابط', duration: '١١ دقيقة', level: 'أساسي', color: 'rose', icon: 'network', summary: 'قارن بين الروابط الأيونية والتساهمية والفلزية من خلال نماذج مبسطة.' },
];

const terms = [
  { word: 'الذرة', category: 'بنية المادة', definition: 'أصغر وحدة تحتفظ بالخواص الكيميائية للعنصر، وتتكوّن من نواة وإلكترونات.' },
  { word: 'العنصر', category: 'المادة', definition: 'مادة نقية تتكوّن ذراتها من العدد نفسه من البروتونات.' },
  { word: 'المركّب', category: 'المادة', definition: 'مادة تنتج من اتحاد عنصرين أو أكثر بنسب ثابتة وروابط كيميائية.' },
  { word: 'المول', category: 'القياس', definition: 'وحدة كمية المادة؛ يحتوي مول واحد على نحو 6.022 × 10²³ كياناً.' },
  { word: 'الأس الهيدروجيني', category: 'المحاليل', definition: 'مقياس لوغاريتمي يرتبط بنشاط أيونات الهيدروجين في المحلول.' },
  { word: 'الرابطة الأيونية', category: 'الترابط', definition: 'تجاذب كهربائي بين أيونات موجبة وسالبة بعد انتقال إلكترون أو أكثر.' },
  { word: 'الاتزان الكيميائي', category: 'التفاعلات', definition: 'حالة تتساوى فيها سرعتا التفاعل الأمامي والعكسي، مع استمرار حدوثهما.' },
  { word: 'الكتلة المولية', category: 'القياس', definition: 'كتلة مول واحد من المادة، وتقاس غالباً بوحدة غرام لكل مول (g/mol).' },
];

const goldStages = [
  { title: 'الصخر والبيئة الجيولوجية', short: 'افهم السياق قبل العينة', detail: 'تتكوّن الرواسب الذهبية في بيئات جيولوجية متعددة. تساعد الخرائط والطبقات الصخرية والسياق الإقليمي على صياغة أسئلة علمية، ولا تكفي ملاحظة واحدة لتأكيد وجود الذهب.' },
  { title: 'العروق والرواسب', short: 'لاحظ البنية والامتداد', detail: 'تُدرس العروق والرواسب بوصفها تراكيب جيولوجية: موقعها واتجاهها وعلاقتها بالصخور المحيطة. التوثيق المنهجي أهم من الاستنتاج السريع من اللون أو اللمعان.' },
  { title: 'توصيف العينة', short: 'سجّل الخصائص بأمان', detail: 'يبدأ التوصيف بوصف غير إتلافي: مصدر العينة، المظهر، الكتلة، وحالة الحفظ. لا تُطحن العينات مجهولة المصدر ولا تُستنشق الأتربة.' },
  { title: 'تحرير الحبيبات', short: 'مفهوم بنيوي لا وصفة', detail: 'يشير تحرير الحبيبات إلى فهم كيفية وجود المعدن داخل الصخر. في المختبر التعليمي ندرس الفكرة بصور ونماذج، لا بتجارب سحق أو معالجة منزلية.' },
  { title: 'التركيز والفصل كمفاهيم', short: 'قارن الخواص الفيزيائية', detail: 'تُشرح مبادئ الفصل من خلال اختلاف الكثافة والحجم والخواص السطحية. تطبيقها العملي يتطلب إشرافاً مختصاً وتصاريح وتجهيزات مهنية مناسبة.' },
  { title: 'التحليل والتحقق', short: 'لا تعتمد على اختبار واحد', detail: 'تُستخدم طرق تحليل معتمدة للتحقق من هوية العينة وتركيبها، مع ضبط جودة ومرجعيات موثوقة. المظهر البصري وحده لا يثبت أن العينة ذهب.' },
  { title: 'التنقية الصناعية الآمنة', short: 'عمليات مرخصة ومضبوطة', detail: 'تتم التنقية الصناعية في منشآت مرخصة بأنظمة احتواء ومراقبة وتدريب مهني. لا يشرح هذا المسار وصفات أو إجراءات استخلاص منزلية.' },
  { title: 'البيئة وإدارة المخلفات', short: 'السلامة جزء من العلم', detail: 'تُقيّم آثار التعدين والمخلفات والمياه والتربة، وتُدار وفق الأنظمة البيئية. لا تُصرف المواد أو مخلفات العينات في المصارف أو البيئة.' },
];

function IconBadge({ kind, className = '' }: { kind: string; className?: string }) {
  const icon = kind === 'atom' ? <Atom /> : kind === 'layers' ? <Layers3 /> : kind === 'drop' ? <Waves /> : kind === 'balance' ? <Zap /> : kind === 'network' ? <Network /> : <FlaskConical />;
  return <span className={`icon-badge ${className}`}>{icon}</span>;
}

export default function App() {
  const [tab, setTab] = useState<Tab>('home');
  const [activeLesson, setActiveLesson] = useState<Lesson>(lessons[0]);
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('الكل');
  const [goldOpen, setGoldOpen] = useState(0);
  const [electronCount, setElectronCount] = useState(6);
  const [quizChoice, setQuizChoice] = useState<number | null>(null);
  const [completed, setCompleted] = useState<string[]>(() => {
    try { return JSON.parse(localStorage.getItem('chemistry-completed') || '[]') as string[]; } catch { return []; }
  });
  const [saved, setSaved] = useState<string[]>(() => {
    try { return JSON.parse(localStorage.getItem('chemistry-saved') || '[]') as string[]; } catch { return []; }
  });
  const [mobileMenu, setMobileMenu] = useState(false);

  const filteredLessons = useMemo(() => lessons.filter((lesson) => {
    const matchesSearch = `${lesson.title} ${lesson.subject} ${lesson.summary}`.includes(search.trim());
    const matchesFilter = filter === 'الكل' || lesson.subject === filter;
    return matchesSearch && matchesFilter;
  }), [search, filter]);
  const filteredTerms = useMemo(() => terms.filter((term) => `${term.word} ${term.category} ${term.definition}`.includes(search.trim())), [search]);
  const progress = Math.min(100, Math.round((completed.length / lessons.length) * 100));

  const go = (next: Tab) => { setTab(next); setMobileMenu(false); setSearch(''); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const openLesson = (lesson: Lesson) => { setActiveLesson(lesson); setQuizChoice(null); setTab('lesson'); window.scrollTo({ top: 0, behavior: 'smooth' }); };
  const toggleComplete = (id: string) => {
    const next = completed.includes(id) ? completed.filter((item) => item !== id) : [...completed, id];
    setCompleted(next); localStorage.setItem('chemistry-completed', JSON.stringify(next));
  };
  const toggleSaved = (id: string) => {
    const next = saved.includes(id) ? saved.filter((item) => item !== id) : [...saved, id];
    setSaved(next); localStorage.setItem('chemistry-saved', JSON.stringify(next));
  };

  const navItems: { id: Tab; label: string; icon: JSX.Element }[] = [
    { id: 'home', label: 'الرئيسية', icon: <Home /> },
    { id: 'lessons', label: 'الدروس', icon: <BookOpen /> },
    { id: 'gold', label: 'الذهب والجيولوجيا', icon: <Microscope /> },
    { id: 'glossary', label: 'المعجم', icon: <Layers3 /> },
    { id: 'profile', label: 'تقدّمي', icon: <Trophy /> },
  ];

  return (
    <div className="app-shell">
      <header className="topbar">
        <button className="brand" onClick={() => go('home')} aria-label="العودة للرئيسية">
          <span className="brand-mark"><Atom size={21} strokeWidth={2.1} /></span>
          <span><strong>مختبر الكيمياء</strong><small>افهم العالم من حولك</small></span>
        </button>
        <nav className={`desktop-nav ${mobileMenu ? 'is-open' : ''}`} aria-label="التنقل الرئيسي">
          {navItems.map((item) => <button key={item.id} className={`nav-link ${tab === item.id || (tab === 'lesson' && item.id === 'lessons') ? 'active' : ''}`} onClick={() => go(item.id)}>{item.label}</button>)}
        </nav>
        <div className="top-actions">
          <button className="icon-button saved-trigger" onClick={() => go('profile')} aria-label="المحفوظات"><Bookmark size={18} /><span className="saved-dot">{saved.length}</span></button>
          <button className="menu-trigger" onClick={() => setMobileMenu(!mobileMenu)} aria-label="فتح القائمة">{mobileMenu ? <X /> : <Menu />}</button>
          <button className="avatar" onClick={() => go('profile')} aria-label="الملف الشخصي">م</button>
        </div>
      </header>

      <main>
        {tab === 'home' && <HomePage progress={progress} completed={completed} openLesson={openLesson} go={go} />}
        {tab === 'lessons' && <LessonsPage search={search} setSearch={setSearch} filter={filter} setFilter={setFilter} list={filteredLessons} openLesson={openLesson} completed={completed} />}
        {tab === 'gold' && <GoldPage open={goldOpen} setOpen={setGoldOpen} />}
        {tab === 'glossary' && <GlossaryPage search={search} setSearch={setSearch} list={filteredTerms} />}
        {tab === 'profile' && <ProfilePage progress={progress} completed={completed} saved={saved} openLesson={openLesson} />}
        {tab === 'lesson' && <LessonPage lesson={activeLesson} done={completed.includes(activeLesson.id)} saved={saved.includes(activeLesson.id)} toggleComplete={toggleComplete} toggleSaved={toggleSaved} openLesson={openLesson} electronCount={electronCount} setElectronCount={setElectronCount} quizChoice={quizChoice} setQuizChoice={setQuizChoice} />}
      </main>

      <footer className="site-footer"><span>مختبر الكيمياء</span><span>تعلّم آمن · فضول علمي · معرفة موثوقة</span><span>الإصدار التعليمي ١.٠</span></footer>
      <nav className="bottom-nav" aria-label="التنقل السفلي">
        {navItems.map((item) => <button key={item.id} className={tab === item.id || (tab === 'lesson' && item.id === 'lessons') ? 'active' : ''} onClick={() => go(item.id)}>{item.icon}<span>{item.id === 'gold' ? 'الذهب' : item.label === 'تقدّمي' ? 'تقدمي' : item.label}</span></button>)}
      </nav>
    </div>
  );
}

function HomePage({ progress, completed, openLesson, go }: { progress: number; completed: string[]; openLesson: (lesson: Lesson) => void; go: (tab: Tab) => void }) {
  return <div className="page home-page">
    <div className="welcome-row"><div><p className="eyebrow"><Sparkles size={14} /> مساحة تعلّم تفاعلية</p><h1>الكيمياء أقرب <span>مما تتخيّل.</span></h1><p className="welcome-copy">من الذرة الصغيرة إلى المعادن في أعماق الأرض — تعلّم الفكرة، شاهدها، ثم جرّبها بأمان.</p></div><div className="hero-atom" aria-hidden="true"><div className="orbit orbit-one"></div><div className="orbit orbit-two"></div><div className="nucleus"></div><span className="electron e-one"></span><span className="electron e-two"></span><span className="electron e-three"></span></div></div>
    <section className="hero-card"><div className="hero-card-copy"><span className="card-kicker">مسارك المقترح</span><h2>ابدأ من أساس المادة</h2><p>درس بصري قصير يربط تركيب الذرة بسلوك العناصر والروابط.</p><button className="button light-button" onClick={() => openLesson(lessons[0])}>ابدأ الدرس <ChevronLeft size={17} /></button></div><div className="hero-schematic"><div className="schematic-label"><span>01</span> الفكرة الأساسية</div><div className="schematic-line"><i></i><b>ذرة</b><i></i><b>إلكترونات</b><i></i><b>روابط</b></div><div className="schematic-caption">البنية تحدد السلوك الكيميائي</div></div></section>
    <div className="home-stats"><div className="stat-card"><span className="stat-icon mint-bg"><BookOpen size={18} /></span><div><strong>٦</strong><small>دروس مصغرة</small></div></div><div className="stat-card"><span className="stat-icon gold-bg"><Microscope size={18} /></span><div><strong>٨</strong><small>مراحل جيولوجية</small></div></div><div className="stat-card"><span className="stat-icon blue-bg"><Target size={18} /></span><div><strong>{completed}</strong><small>دروس مكتملة</small></div></div></div>
    <section className="section-block"><div className="section-heading"><div><p className="eyebrow">تعلّم بخطوات صغيرة</p><h2>مسارات تناسب فضولك</h2></div><button className="text-link" onClick={() => go('lessons')}>عرض كل الدروس <ChevronLeft size={16} /></button></div><div className="path-grid"><button className="path-card path-chem" onClick={() => go('lessons')}><span className="path-art art-atom"><Atom /></span><span className="path-meta">المسار ٠١ · ٦ دروس</span><strong>أساسيات الكيمياء</strong><small>من الذرات والروابط إلى التفاعلات.</small><span className="path-arrow"><ChevronLeft size={17} /></span></button><button className="path-card path-earth" onClick={() => go('gold')}><span className="path-art art-earth"><Microscope /></span><span className="path-meta">رحلة بصرية · ٨ مراحل</span><strong>الذهب والجيولوجيا</strong><small>تتبّع العينة من السياق الجيولوجي إلى التحقق.</small><span className="path-arrow"><ChevronLeft size={17} /></span></button></div></section>
    <section className="section-block lesson-section"><div className="section-heading"><div><p className="eyebrow">اقتراح اليوم</p><h2>درس يستحق أن تبدأ به</h2></div><span className="micro-label"><Clock3 size={14} /> ١٢ دقيقة</span></div><LessonCard lesson={lessons[0]} onClick={() => openLesson(lessons[0])} done={completed.includes(lessons[0].id)} /></section>
    <section className="progress-strip"><div className="progress-copy"><span className="progress-symbol"><Trophy size={19} /></span><div><strong>تقدّمك محفوظ على جهازك</strong><small>أنجزت {completed.length} من {lessons.length} دروس — خطوتك التالية تبدأ الآن.</small></div></div><div className="progress-ring" style={{ '--progress': `${progress}%` } as React.CSSProperties}><span>{progress}%</span></div></section>
    <div className="safety-note"><ShieldCheck size={18} /><p><strong>نتعلّم بأمان.</strong> التجارب في هذا التطبيق تعليمية مبسطة؛ لا تخلط مواد كيميائية ولا تعالج عينات مجهولة خارج مختبر مجهّز وإشراف مختص.</p></div>
  </div>;
}

function LessonsPage({ search, setSearch, filter, setFilter, list, openLesson, completed }: { search: string; setSearch: (value: string) => void; filter: string; setFilter: (value: string) => void; list: Lesson[]; openLesson: (lesson: Lesson) => void; completed: string[] }) {
  const filters = ['الكل', 'أساسيات الكيمياء', 'المادة والطاقة', 'المحاليل', 'التفاعلات', 'الحسابات الكيميائية', 'الترابط'];
  return <div className="page"><PageIntro eyebrow="مكتبة التعلّم" title="دروس قصيرة، <span>فهم أعمق.</span>" description="كل درس رحلة من الملاحظة إلى التفسير الجزيئي — مع أمثلة محلولة ورسوم وأسئلة تفكير." />
    <div className="search-box"><Search size={18} /><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="ابحث عن درس أو مفهوم..." /><kbd>⌕</kbd></div>
    <div className="filter-row">{filters.map((item) => <button key={item} onClick={() => setFilter(item)} className={filter === item ? 'selected' : ''}>{item}</button>)}</div>
    <div className="results-line"><span>{list.length} دروس</span><span>مرتّبة حسب المسار التعليمي</span></div>
    <div className="lesson-list">{list.map((lesson, index) => <LessonCard key={lesson.id} lesson={lesson} onClick={() => openLesson(lesson)} done={completed.includes(lesson.id)} index={index + 1} />)}</div>
    {list.length === 0 && <EmptyState title="لم نعثر على هذا الدرس" text="جرّب كلمة أخرى أو غيّر التصفية." />}
    <div className="safety-note"><ShieldCheck size={18} /><p><strong>تذكير مختبري:</strong> المحتوى تعليمي، وأي تجربة عملية يجب أن تتم وفق تعليمات المدرسة أو المختبر وبإشراف مؤهل.</p></div>
  </div>;
}

function LessonCard({ lesson, onClick, done, index }: { lesson: Lesson; onClick: () => void; done: boolean; index?: number }) {
  return <button className="lesson-card" onClick={onClick}><span className={`lesson-visual ${lesson.color}`}><IconBadge kind={lesson.icon} />{index && <small>{String(index).padStart(2, '0')}</small>}</span><span className="lesson-card-content"><span className="lesson-meta">{lesson.subject}<i></i>{lesson.level}</span><strong>{lesson.title}</strong><small>{lesson.summary}</small><span className="lesson-time"><Clock3 size={13} /> {lesson.duration}</span></span><span className={`lesson-open ${done ? 'is-done' : ''}`}>{done ? <Check size={17} /> : <ChevronLeft size={18} />}</span></button>;
}

function GoldPage({ open, setOpen }: { open: number; setOpen: (value: number) => void }) {
  return <div className="page gold-page"><div className="gold-hero"><div><p className="eyebrow gold-eyebrow"><Microscope size={14} /> جيولوجيا تطبيقية</p><h1>رحلة الذهب<br /><span>من الصخر إلى التحقق.</span></h1><p>ثماني محطات تشرح كيف نفكر علمياً في الرواسب والعينات، مع السلامة والبيئة في قلب كل خطوة.</p><div className="gold-tags"><span>مفاهيم لا وصفات</span><span>محتوى آمن</span><span>تحقق علمي</span></div></div><div className="gold-orb"><div className="orb-ring"></div><div className="orb-core">Au</div><span className="orb-label">79</span><i className="orb-dot"></i></div></div>
    <div className="gold-intro"><div className="gold-intro-icon"><Lightbulb size={20} /></div><div><strong>كيف نستخدم هذه الرحلة؟</strong><p>اتبع تسلسل المراحل. الهدف بناء فهم جيولوجي ومخبري، وليس تنفيذ استخلاص أو معالجة منزلية.</p></div></div>
    <div className="journey-layout"><div className="journey-list">{goldStages.map((stage, i) => <button key={stage.title} className={`journey-item ${open === i ? 'expanded' : ''}`} onClick={() => setOpen(i)}><span className="journey-index">{String(i + 1).padStart(2, '0')}</span><span className="journey-item-copy"><strong>{stage.title}</strong><small>{stage.short}</small></span><ChevronLeft size={18} className="journey-chevron" /></button>)}</div><article className="journey-detail"><div className="detail-top"><span className="detail-kicker">محطة {String(open + 1).padStart(2, '0')} من ٠٨</span><span className="detail-icon"><Microscope size={19} /></span></div><h2>{goldStages[open].title}</h2><p>{goldStages[open].detail}</p><div className="journey-flow"><span>السؤال العلمي</span><ChevronLeft size={14} /><span>الملاحظة</span><ChevronLeft size={14} /><span>التحقق</span></div><div className="safety-callout"><ShieldCheck size={17} /><span>لا تستخدم أحماضاً أو زئبقاً أو سيانيداً ولا تسحق عينات مجهولة. المعالجة الفعلية للمعادن شأن مهني منظم.</span></div><div className="detail-controls"><button disabled={open === 0} onClick={() => setOpen(Math.max(0, open - 1))}><ArrowLeft size={15} /> السابقة</button><button disabled={open === goldStages.length - 1} onClick={() => setOpen(Math.min(goldStages.length - 1, open + 1))}>التالية <ArrowDown size={15} /></button></div></article></div>
    <div className="gold-principles"><div><span>01</span><strong>وصف قبل استنتاج</strong><small>اللون وحده لا يكفي لتحديد معدن.</small></div><div><span>02</span><strong>دليل قبل ادعاء</strong><small>التحليل الموثوق يحتاج ضبط جودة.</small></div><div><span>03</span><strong>سلامة قبل تجربة</strong><small>المخلفات والبيئة مسؤولية علمية.</small></div></div>
  </div>;
}

function GlossaryPage({ search, setSearch, list }: { search: string; setSearch: (value: string) => void; list: typeof terms }) {
  return <div className="page"><PageIntro eyebrow="مرجع سريع" title="المفاهيم، <span>بكلمات واضحة.</span>" description="مصطلحات أساسية مرتبطة بالدروس. ابحث بالكلمة أو بالمجال لتصل إلى المعنى بسرعة." />
    <div className="search-box"><Search size={18} /><input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="اكتب مصطلحاً مثل: المول..." /></div>
    <div className="glossary-count"><span className="count-number">{list.length}</span><span>مصطلح علمي</span><span className="count-divider"></span><span>تعريفات موجزة قابلة للمراجعة</span></div>
    <div className="term-list">{list.map((term) => <article className="term-card" key={term.word}><div className="term-head"><span>{term.category}</span><button aria-label={`حفظ ${term.word}`}><Bookmark size={16} /></button></div><h2>{term.word}</h2><p>{term.definition}</p><span className="term-link">مرتبط بالدروس <ChevronLeft size={14} /></span></article>)}</div>
    {list.length === 0 && <EmptyState title="لا توجد نتائج" text="غيّر كلمة البحث وحاول مرة أخرى." />}
  </div>;
}

function ProfilePage({ progress, completed, saved, openLesson }: { progress: number; completed: string[]; saved: string[]; openLesson: (lesson: Lesson) => void }) {
  const doneList = lessons.filter((lesson) => completed.includes(lesson.id));
  const savedList = lessons.filter((lesson) => saved.includes(lesson.id));
  return <div className="page profile-page"><PageIntro eyebrow="مساحتك التعليمية" title="كل خطوة تصنع <span>فرقاً.</span>" description="تقدّمك ومحفوظاتك تُحفظ على هذا الجهاز فقط." />
    <section className="profile-summary"><div className="profile-avatar">م</div><div className="profile-info"><span>متعلّم فضولي</span><h2>مرحباً بك في المختبر</h2><small>واصل التعلّم بوتيرتك الخاصة</small></div><div className="profile-progress"><div className="progress-ring large-ring" style={{ '--progress': `${progress}%` } as React.CSSProperties}><span>{progress}%</span></div><small>إنجاز المسار</small></div></section>
    <div className="profile-metrics"><div><strong>{completed.length}</strong><span>دروس مكتملة</span></div><div><strong>{saved.length}</strong><span>عناصر محفوظة</span></div><div><strong>٨</strong><span>محطات جيولوجية</span></div></div>
    <section className="section-block"><div className="section-heading"><div><p className="eyebrow">سجلّك</p><h2>الدروس المكتملة</h2></div></div>{doneList.length ? <div className="lesson-list">{doneList.map((lesson) => <LessonCard key={lesson.id} lesson={lesson} done onClick={() => openLesson(lesson)} />)}</div> : <EmptyState title="رحلتك تبدأ من درس" text="أكمل أي درس وسيظهر هنا تلقائياً." />}</section>
    <section className="section-block"><div className="section-heading"><div><p className="eyebrow">للمراجعة لاحقاً</p><h2>المحفوظات</h2></div></div>{savedList.length ? <div className="lesson-list">{savedList.map((lesson) => <LessonCard key={lesson.id} lesson={lesson} done={completed.includes(lesson.id)} onClick={() => openLesson(lesson)} />)}</div> : <EmptyState title="لا توجد عناصر محفوظة بعد" text="استخدم علامة الحفظ داخل صفحة الدرس للعودة إليه بسهولة." />}</section>
    <div className="privacy-note"><ShieldCheck size={17} /><span>لا يحتاج التطبيق إلى حساب. بيانات التقدم محفوظة محلياً في هذا الجهاز.</span></div>
  </div>;
}

function LessonPage({ lesson, done, saved, toggleComplete, toggleSaved, openLesson, electronCount, setElectronCount, quizChoice, setQuizChoice }: { lesson: Lesson; done: boolean; saved: boolean; toggleComplete: (id: string) => void; toggleSaved: (id: string) => void; openLesson: (lesson: Lesson) => void; electronCount: number; setElectronCount: (n: number) => void; quizChoice: number | null; setQuizChoice: (n: number) => void }) {
  const charge = 6 - electronCount;
  const answerOptions = ['العنصر يتحدد بعدد النيوترونات', 'العنصر يتحدد بعدد البروتونات', 'الإلكترونات توجد داخل النواة'];
  return <div className="page lesson-page"><button className="back-link" onClick={() => openLesson(lesson)}><ChevronRight size={16} /> الدرس</button>
    <div className="lesson-title-block"><span className="lesson-pill">{lesson.subject} <i></i> {lesson.duration}</span><h1>{lesson.title}</h1><p>{lesson.summary}</p><div className="lesson-actions"><button className={`button ${done ? 'done-button' : 'primary-button'}`} onClick={() => toggleComplete(lesson.id)}>{done ? <><Check size={16} /> مكتمل</> : <>وضع علامة كمكتمل <Check size={16} /></>}</button><button className={`save-button ${saved ? 'saved' : ''}`} onClick={() => toggleSaved(lesson.id)}><Bookmark size={16} /> {saved ? 'محفوظ' : 'حفظ للمراجعة'}</button></div></div>
    <div className="lesson-content-grid"><article className="lesson-article">
      <section className="content-card warm-card"><span className="content-kicker"><Sparkles size={14} /> قبل أن نبدأ</span><h2>لماذا ندرس الذرة؟</h2><p>كل مادة حولنا تتكون من جسيمات. يساعدنا نموذج الذرة على تفسير لماذا يتفاعل عنصر مع آخر، ولماذا تختلف خصائص المواد رغم أن مكوّناتها الأساسية متشابهة.</p><div className="question-chip"><CircleHelp size={16} /><span>سؤال البداية: ما الذي يجعل ذرة الكربون مختلفة عن ذرة الأكسجين؟</span></div></section>
      <section className="content-card"><span className="content-kicker">الفكرة في مخطط</span><h2>من البنية إلى السلوك</h2><div className="concept-map"><div className="map-node node-proton"><Atom size={18} /><strong>عدد البروتونات</strong><small>هوية العنصر</small></div><div className="map-connector"><span></span><small>يحدد</small></div><div className="map-node node-electron"><Zap size={18} /><strong>إلكترونات التكافؤ</strong><small>سلوك الترابط</small></div><div className="map-connector"><span></span><small>تؤثر في</small></div><div className="map-node node-bond"><Network size={18} /><strong>الروابط</strong><small>خواص المادة</small></div></div><div className="map-caption">المخطط يوضح علاقة مبسطة؛ السلوك الفعلي يعتمد على البنية والظروف.</div></section>
      <section className="content-card"><span className="content-kicker">شرح تدريجي</span><h2>من الملاحظة إلى التفسير</h2><div className="step-list"><div className="explain-step"><span>١</span><div><strong>نلاحظ</strong><p>قد نرى مواد مختلفة في اللون أو الصلابة أو التوصيل الكهربائي.</p></div></div><div className="explain-step"><span>٢</span><div><strong>نسأل</strong><p>هل سبب الاختلاف نوع الذرات، أم طريقة ترتيبها واتصالها؟</p></div></div><div className="explain-step"><span>٣</span><div><strong>نفسّر جزيئياً</strong><p>عدد البروتونات يعرّف العنصر، أما ترتيب الإلكترونات الخارجية فيؤثر بقوة في الروابط والتفاعلات.</p></div></div></div></section>
      <section className="content-card equation-card"><span className="content-kicker">علاقة مهمة</span><h2>الشحنة الصافية للأيون</h2><div className="equation">الشحنة = عدد البروتونات − عدد الإلكترونات</div><div className="symbol-grid"><span><b>p⁺</b> عدد البروتونات (بلا وحدة)</span><span><b>e⁻</b> عدد الإلكترونات (بلا وحدة)</span><span><b>الشحنة</b> بوحدة الشحنة الأولية (e)</span></div><p className="equation-footnote">في النظام الدولي تُقاس الشحنة الكهربائية بوحدة الكولوم (C). الرمز e هنا يعني مقدار الشحنة الأولية، وليس كتلة الجسيم.</p></section>
      <section className="content-card example-card"><span className="content-kicker">مثال محلول</span><h2>ذرة متعادلة أصبحت أيوناً</h2><div className="worked-example"><div><span className="example-step">المعطيات</span><p>ذرة كربون فيها ٦ بروتونات، وفقدت إلكترونين.</p></div><ChevronLeft size={17} /><div><span className="example-step">التعويض</span><p>الشحنة = ٦ − ٤ = +٢</p></div><ChevronLeft size={17} /><div><span className="example-step">النتيجة</span><p>أيون موجب شحنته ٢+، ويظل العنصر كربوناً.</p></div></div><div className="example-note"><Lightbulb size={15} /> تغيير عدد الإلكترونات لا يغيّر هوية العنصر؛ تغيير عدد البروتونات يغيّرها.</div></section>
      <section className="content-card interactive-card"><div className="interactive-heading"><div><span className="content-kicker">جرّب بنفسك</span><h2>مختبر الشحنة</h2></div><span className="live-tag"><i></i> تفاعلي</span></div><p>غيّر عدد الإلكترونات ولاحظ كيف تتغير شحنة الجسيم. نثبت عدد البروتونات عند ٦.</p><div className="atom-lab"><div className="atom-visual"><div className="atom-ring ring-a"></div><div className="atom-ring ring-b"></div><div className="atom-center">6p⁺</div>{Array.from({ length: electronCount }, (_, i) => <span className={`lab-electron electron-${i}`} key={i}></span>)}</div><div className="atom-controls"><span>البروتونات <b>٦</b></span><div className="electron-control"><button onClick={() => setElectronCount(Math.max(0, electronCount - 1))} aria-label="تقليل الإلكترونات">−</button><div><strong>{electronCount}</strong><small>إلكترونات</small></div><button onClick={() => setElectronCount(Math.min(12, electronCount + 1))} aria-label="زيادة الإلكترونات">+</button></div><div className="charge-readout"><span>الشحنة الصافية</span><strong>{charge === 0 ? 'متعادل' : `${Math.abs(charge)}${charge > 0 ? '+' : '−'}`}</strong></div></div></div></section>
      <section className="content-card compare-card"><span className="content-kicker">بطاقة مقارنة</span><h2>الذرة المتعادلة والأيون</h2><div className="compare-table"><div className="compare-head"><span>وجه المقارنة</span><strong>ذرة متعادلة</strong><strong>أيون</strong></div><div><span>البروتونات والإلكترونات</span><span>متساوية</span><span>غير متساوية</span></div><div><span>الشحنة الكلية</span><span>صفر</span><span>موجبة أو سالبة</span></div><div><span>هوية العنصر</span><span>تحددها البروتونات</span><span>تبقى كما هي</span></div></div></section>
      <section className="content-card real-life-card"><span className="content-kicker">حولنا في الحياة</span><h2>لماذا يهمنا هذا؟</h2><div className="life-grid"><div><Beaker size={18} /><strong>المحاليل</strong><p>الشحنة تساعد في فهم ذوبان بعض الأملاح وتوصيل محاليلها للكهرباء.</p></div><div><Microscope size={18} /><strong>المواد</strong><p>تُستخدم نماذج الذرات لتفسير خواص المواد وتصميم مركبات جديدة.</p></div></div></section>
      <section className="content-card misconception-card"><span className="content-kicker">صحّح الفكرة</span><h2>خطأ شائع</h2><div className="misconception"><span>الفكرة الشائعة</span><p>«إذا تغيّر عدد الإلكترونات، صار العنصر عنصراً آخر.»</p><div><Check size={16} /><strong>التصحيح:</strong> هوية العنصر يحددها عدد البروتونات. تغيّر الإلكترونات يغيّر الشحنة فقط.</div></div></section>
      <section className="content-card quiz-card"><span className="content-kicker">فكّر ثم أجب</span><h2>ما الذي يحدد هوية العنصر؟</h2><div className="quiz-options">{answerOptions.map((option, i) => <button key={option} className={`${quizChoice === i ? 'chosen' : ''} ${quizChoice !== null && i === 1 ? 'correct' : ''}`} onClick={() => setQuizChoice(i)}><span>{String.fromCharCode(65 + i)}</span>{option}{quizChoice !== null && i === 1 && <Check size={16} />}</button>)}</div>{quizChoice !== null && <p className={`quiz-explanation ${quizChoice === 1 ? 'is-correct' : ''}`}>{quizChoice === 1 ? 'إجابة صحيحة. العدد الذري يساوي عدد البروتونات، وهو الذي يميّز العنصر.' : 'ليست هذه الإجابة. فكّر في الجسيم الموجود في النواة وشحنته موجبة.'}</p>}</section>
      <section className="content-card safety-topic"><ShieldCheck size={19} /><div><span className="content-kicker">سلامة مرتبطة بالموضوع</span><h2>النموذج ليس تجربة على مادة حقيقية</h2><p>استخدم المحاكاة لفهم الفكرة. لا تفتح أجهزة أو مصادر إشعاع ولا تتعامل مع عينات كيميائية مجهولة. في التجارب المدرسية اتبع تعليمات المشرف وملصق السلامة.</p></div></section>
      <section className="content-card summary-card"><span className="content-kicker">ملخص سريع</span><h2>ثلاث نقاط تذكّرها</h2><div className="summary-points"><span><b>١</b> البروتونات تحدد العنصر.</span><span><b>٢</b> تساوي البروتونات والإلكترونات يعني ذرة متعادلة.</span><span><b>٣</b> فقد أو اكتساب الإلكترونات يكوّن أيوناً.</span></div></section>
      <section className="related-section"><div className="section-heading"><div><p className="eyebrow">الخطوة التالية</p><h2>وسّع فهمك</h2></div></div><div className="related-cards">{lessons.filter((item) => item.id !== lesson.id).slice(0, 2).map((item) => <LessonCard key={item.id} lesson={item} onClick={() => openLesson(item)} done={false} />)}</div></section>
    </article><aside className="lesson-aside"><div className="aside-progress"><span>محتوى الدرس</span><div className="aside-progress-line"><i></i></div><small>تعلّم بالترتيب أو انتقل مباشرة</small></div><a href="#" onClick={(e) => { e.preventDefault(); document.querySelector('.concept-map')?.scrollIntoView({ behavior: 'smooth' }); }}>مخطط الفكرة <ChevronLeft size={14} /></a><a href="#" onClick={(e) => { e.preventDefault(); document.querySelector('.interactive-card')?.scrollIntoView({ behavior: 'smooth' }); }}>مختبر الشحنة <ChevronLeft size={14} /></a><a href="#" onClick={(e) => { e.preventDefault(); document.querySelector('.quiz-card')?.scrollIntoView({ behavior: 'smooth' }); }}>سؤال التفكير <ChevronLeft size={14} /></a><div className="aside-safe"><ShieldCheck size={16} /><span>تعليم آمن ومفاهيم موثوقة</span></div></aside></div>
  </div>;
}

function PageIntro({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return <div className="page-intro"><p className="eyebrow">{eyebrow}</p><h1 dangerouslySetInnerHTML={{ __html: title }} /><p>{description}</p></div>;
}

function EmptyState({ title, text }: { title: string; text: string }) {
  return <div className="empty-state"><span><Search size={19} /></span><strong>{title}</strong><p>{text}</p></div>;
}
