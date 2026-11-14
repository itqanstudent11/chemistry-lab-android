import type { Locale } from './lessonTypes';

export const copy = {
  ar: {
    brand: { name: 'مختبر الكيمياء', tagline: 'افهم العالم من حولك' },
    nav: { home: 'الرئيسية', lessons: 'الدروس', gold: 'الذهب والجيولوجيا', glossary: 'المعجم', progress: 'تقدّمي', settings: 'الإعدادات' },
    common: {
      minutes: 'دقيقة', start: 'ابدأ التعلّم', continue: 'متابعة التعلّم', viewAll: 'عرض الكل', search: 'ابحث في الدروس والمفاهيم…', searchGlossary: 'ابحث عن مصطلح علمي…',
      saved: 'محفوظ', save: 'حفظ للمراجعة', completed: 'مكتمل', markComplete: 'علّم الدرس كمكتمل', next: 'التالي', previous: 'السابق', back: 'عودة', close: 'إغلاق', all: 'الكل',
      lesson: 'درس', lessons: 'دروس', minute: 'د', safety: 'السلامة أولاً', noResults: 'لم نجد نتائج', trySearch: 'جرّب مصطلحاً آخر أو أزل عوامل التصفية.', answer: 'الإجابة', correct: 'إجابة صحيحة', tryAgain: 'راجع الفكرة ثم أعد المحاولة.',
      objective: 'ماذا ستتعلم؟', conceptMap: 'خريطة المفاهيم', explanation: 'الشرح خطوة بخطوة', equation: 'العلاقة العلمية', workedExample: 'مثال محلول', compare: 'قارن بين',
      simulation: 'محاكاة تفاعلية', applications: 'الكيمياء حولنا', misconception: 'فكرة شائعة تحتاج تصحيحاً', quiz: 'اختبر فهمك', summary: 'خلاصة الدرس', glossary: 'مصطلحات الدرس',
      why: 'لماذا يهم هذا الموضوع؟', prerequisite: 'قبل أن تبدأ', takeaways: 'بعد هذا الدرس ستكون قادراً على', related: 'تابع رحلتك', explore: 'استكشف المسار',
      sourceNote: 'نموذج تعليمي مبسّط؛ القيم والحدود قد تختلف باختلاف المادة والظروف.',
    },
    home: {
      eyebrow: 'مساحة تعلّم تفاعلية', headline: 'الكيمياء أقرب مما تتخيّل.', intro: 'من بنية الذرة إلى خواص المواد — تعلّم الفكرة بعمق، شاهدها بصرياً، واختبر فهمك خطوة بخطوة.',
      continueTitle: 'مسارك المقترح', continueSubtitle: 'ابدأ بالأساسيات، ثم ابنِ معرفة مترابطة درساً بعد درس.', courseBasics: 'أساسيات الكيمياء', courseBasicsText: 'الذرات، المادة، الأحماض، الاتزان، المول والروابط.', courseGeology: 'الذهب والجيولوجيا', courseGeologyText: 'رحلة جيولوجية تعليمية من الصخور إلى التحليل المسؤول.',
      progress: 'تقدّمك محفوظ على هذا الجهاز', completedOf: 'أنجزت {done} من {total} درساً', lessonsCount: 'دروس كاملة', modulesCount: 'وحدات تعليمية', minutesLearning: 'دقائق من التعلّم',
      featured: 'درس مقترح', howLearn: 'كيف ستتعلّم', stepOne: 'افهم الفكرة', stepTwo: 'شاهد النموذج', stepThree: 'طبّق ما تعلّمت', safety: 'محتوى تعليمي آمن',
    },
    lessons: {
      eyebrow: 'مكتبة التعلّم', title: 'تعلّم الفكرة، لا تحفظ العنوان.', intro: 'دروس كاملة تجمع الشرح والأمثلة والرسوم والمحاكاة والاختبار القصير في تجربة واحدة.',
      filters: 'الموضوعات', results: 'نتائج البحث', duration: 'المدة التقريبية', level: 'المستوى', levels: { beginner: 'تمهيدي', basic: 'أساسي', intermediate: 'متوسط' },
      path: 'مسار الأساسيات', emptyTitle: 'لا توجد دروس مطابقة', emptyText: 'عدّل كلمة البحث أو اختر موضوعاً آخر.',
    },
    lesson: {
      first: 'تمهيد', step: 'خطوة', readTime: 'وقت التعلّم', tableContents: 'محتويات الدرس', jumpTo: 'انتقل إلى القسم', ask: 'سؤال للتفكير', model: 'النموذج لا يمثل كل التفاصيل المجهرية.',
      interactionHint: 'حرّك المؤشر ولاحظ كيف تتغير النتيجة. اقرأ الملاحظة العلمية أسفل المحاكاة.', checkAnswer: 'تحقق من الإجابة', correctFeedback: 'أحسنت! {explanation}', incorrectFeedback: 'ليس تماماً. {explanation}',
    },
    settings: {
      eyebrow: 'تجربة تناسبك', title: 'إعدادات العرض', intro: 'خصّص الألوان واللغة وحجم النص لتكون القراءة مريحة على هاتفك.', appearance: 'المظهر', light: 'نهاري', dark: 'ليلي', themeHint: 'اختر لوحة مريحة للقراءة في أوقات مختلفة.',
      language: 'لغة التطبيق', arabic: 'العربية', english: 'English', languageHint: 'يتغير اتجاه الواجهة ومحتوى الدروس بحسب اللغة المختارة.',
      typeSize: 'حجم الخط', smaller: 'أصغر', larger: 'أكبر', sizeHint: 'المعاينة: تُحفظ التغييرات على هذا الجهاز فوراً.', fontScale: 'الحجم الحالي', reset: 'إعادة الإعدادات الافتراضية', saved: 'حُفظت الإعدادات على جهازك.',
      accessibility: 'سهولة القراءة', accessibilityText: 'التباين واضح، وحجم النص قابل للتعديل، ويمكن التنقل بلوحة المفاتيح أو اللمس.',
    },
    gold: {
      eyebrow: 'جيولوجيا تطبيقية', title: 'رحلة الذهب: من الصخر إلى التحقق.', intro: 'افهم السياق الجيولوجي وطريقة التفكير في العينات والتحليل المسؤول؛ هذا مسار معرفي وليس دليلاً للاستخلاص.',
      useTitle: 'كيف تستخدم هذه الرحلة؟', useText: 'اتبع المراحل بالتسلسل. ركّز على السؤال والملاحظة والدليل، وتذكّر أن اللمعان وحده لا يثبت هوية المعدن.',
      stage: 'المرحلة', of: 'من', question: 'السؤال العلمي', observation: 'الملاحظة', evidence: 'الدليل والتحقق', note: 'ملاحظة ميدانية تعليمية', caution: 'لا تسحق أو تطحن عينات مجهولة ولا تستخدم أحماضاً أو زئبقاً أو سيانيداً. التحليل والمعالجة الفعلية من اختصاص منشآت مرخصة ومختصين.',
      previous: 'المرحلة السابقة', next: 'المرحلة التالية', principles: ['وصف قبل استنتاج', 'دليل قبل ادعاء', 'سلامة قبل تجربة'],
    },
    glossaryPage: { eyebrow: 'مرجع سريع', title: 'المفاهيم بكلمات واضحة.', intro: 'تعريفات موسعة للمصطلحات الأساسية، مع وصلها بالدرس والمسار المناسب.', count: 'مصطلحاً علمياً', category: 'المجال', related: 'الدرس المرتبط' },
    progressPage: {
      eyebrow: 'مساحتك التعليمية', title: 'كل خطوة تصنع فرقاً.', intro: 'تقدّمك ومحفوظاتك محفوظة محلياً في هذا الجهاز فقط.', learner: 'متعلّم فضولي', hello: 'مرحباً بك في المختبر', continue: 'واصل التعلّم بوتيرتك الخاصة', completion: 'نسبة الإنجاز', finished: 'الدروس المكتملة', bookmarked: 'الدروس المحفوظة', reset: 'مسح تقدّمي', resetConfirm: 'هل تريد مسح التقدم والمحفواظات من هذا الجهاز؟', privacy: 'لا يحتاج التطبيق إلى حساب. لا يغادر سجلّ تعلمك هذا الجهاز.', emptyDone: 'رحلتك تبدأ من درس واحد.', emptySaved: 'لا توجد دروس محفوظة بعد.',
    },
    footer: { name: 'مختبر الكيمياء', tagline: 'فضول علمي · فهم عميق · تعلّم آمن', version: 'الإصدار التعليمي 2.0' },
  },
  en: {
    brand: { name: 'Chemistry Lab', tagline: 'Understand the world around you' },
    nav: { home: 'Home', lessons: 'Lessons', gold: 'Gold & Geology', glossary: 'Glossary', progress: 'My progress', settings: 'Settings' },
    common: {
      minutes: 'min', start: 'Start learning', continue: 'Continue learning', viewAll: 'View all', search: 'Search lessons and concepts…', searchGlossary: 'Search scientific terms…',
      saved: 'Saved', save: 'Save for review', completed: 'Completed', markComplete: 'Mark lesson complete', next: 'Next', previous: 'Previous', back: 'Back', close: 'Close', all: 'All',
      lesson: 'lesson', lessons: 'lessons', minute: 'min', safety: 'Safety first', noResults: 'No results found', trySearch: 'Try another term or clear a filter.', answer: 'Answer', correct: 'Correct', tryAgain: 'Review the idea and try again.',
      objective: 'What you will learn', conceptMap: 'Concept map', explanation: 'Step-by-step explanation', equation: 'Scientific relationship', workedExample: 'Worked example', compare: 'Compare',
      simulation: 'Interactive model', applications: 'Chemistry around us', misconception: 'A common misconception', quiz: 'Check your understanding', summary: 'Lesson takeaways', glossary: 'Key terms',
      why: 'Why does this matter?', prerequisite: 'Before you begin', takeaways: 'By the end, you can', related: 'Continue your learning', explore: 'Explore the path',
      sourceNote: 'A simplified learning model; values and boundaries can vary with the substance and conditions.',
    },
    home: {
      eyebrow: 'Interactive learning space', headline: 'Chemistry is closer than you think.', intro: 'From atomic structure to material properties—learn the idea deeply, see it visually, and check your understanding step by step.',
      continueTitle: 'Your suggested path', continueSubtitle: 'Start with the foundations, then build connected knowledge lesson by lesson.', courseBasics: 'Chemistry foundations', courseBasicsText: 'Atoms, matter, acids, equilibrium, the mole, and bonding.', courseGeology: 'Gold & geology', courseGeologyText: 'A geology learning journey from rocks to responsible analysis.',
      progress: 'Your progress is saved on this device', completedOf: 'You completed {done} of {total} lessons', lessonsCount: 'Full lessons', modulesCount: 'Learning modules', minutesLearning: 'minutes of learning',
      featured: 'Featured lesson', howLearn: 'How you will learn', stepOne: 'Understand the idea', stepTwo: 'See the model', stepThree: 'Apply what you learned', safety: 'Safe educational content',
    },
    lessons: {
      eyebrow: 'Learning library', title: 'Learn the idea, not just the heading.', intro: 'Complete lessons combine explanations, examples, visuals, an interactive model, and a short check for understanding.',
      filters: 'Topics', results: 'Search results', duration: 'Estimated time', level: 'Level', levels: { beginner: 'Introductory', basic: 'Foundational', intermediate: 'Intermediate' },
      path: 'Foundations path', emptyTitle: 'No matching lessons', emptyText: 'Change your search or choose another topic.',
    },
    lesson: {
      first: 'Introduction', step: 'Step', readTime: 'Learning time', tableContents: 'In this lesson', jumpTo: 'Jump to section', ask: 'Think about it', model: 'A model does not capture every microscopic detail.',
      interactionHint: 'Move the control and observe how the result changes. Read the scientific note below the model.', checkAnswer: 'Check answer', correctFeedback: 'Well done! {explanation}', incorrectFeedback: 'Not quite. {explanation}',
    },
    settings: {
      eyebrow: 'Your learning, your way', title: 'Display settings', intro: 'Choose a comfortable theme, language, and text size for your phone.', appearance: 'Appearance', light: 'Light', dark: 'Dark', themeHint: 'Pick a calm palette that feels comfortable at different times of day.',
      language: 'App language', arabic: 'العربية', english: 'English', languageHint: 'The interface direction and lesson content follow your selected language.',
      typeSize: 'Text size', smaller: 'Smaller', larger: 'Larger', sizeHint: 'Preview: changes are saved on this device immediately.', fontScale: 'Current size', reset: 'Reset to defaults', saved: 'Settings are saved on your device.',
      accessibility: 'Readable by design', accessibilityText: 'Clear contrast, adjustable text, and controls designed for touch and keyboard navigation.',
    },
    gold: {
      eyebrow: 'Applied geology', title: 'Gold: from rock to verification.', intro: 'Understand geological context, sample reasoning, and responsible analysis; this is a knowledge path, not an extraction guide.',
      useTitle: 'How to use this journey', useText: 'Follow the stages in order. Focus on the question, observation, and evidence; shine alone cannot identify a mineral.',
      stage: 'Stage', of: 'of', question: 'Scientific question', observation: 'Observation', evidence: 'Evidence and verification', note: 'Field-learning note', caution: 'Do not crush or grind unknown samples, or use acids, mercury, or cyanide. Actual analysis and processing belong to licensed facilities and trained professionals.',
      previous: 'Previous stage', next: 'Next stage', principles: ['Describe before concluding', 'Evidence before claims', 'Safety before practice'],
    },
    glossaryPage: { eyebrow: 'Quick reference', title: 'Concepts in clear language.', intro: 'Expanded definitions for key terms, connected to the relevant lesson and path.', count: 'scientific terms', category: 'Field', related: 'Related lesson' },
    progressPage: {
      eyebrow: 'Your learning space', title: 'Every step makes a difference.', intro: 'Your progress and saved lessons are stored locally on this device only.', learner: 'Curious learner', hello: 'Welcome to the lab', continue: 'Learn at your own pace', completion: 'Progress', finished: 'Lessons completed', bookmarked: 'Saved lessons', reset: 'Clear my progress', resetConfirm: 'Clear progress and saved items from this device?', privacy: 'No account is required. Your learning history stays on this device.', emptyDone: 'Your journey begins with one lesson.', emptySaved: 'No saved lessons yet.',
    },
    footer: { name: 'Chemistry Lab', tagline: 'Scientific curiosity · Deep understanding · Safe learning', version: 'Learning edition 2.0' },
  },
} as const;

export type Copy = (typeof copy)[Locale];
