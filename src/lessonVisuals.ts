export type LessonVisual = {
  cover: string;
  altAr: string;
  altEn: string;
  focusAr: string;
  focusEn: string;
  badgeAr: string;
  badgeEn: string;
};

const cover = (file: string) => new URL(`./assets/${file}`, import.meta.url).href;

export const lessonVisuals: Record<string, LessonVisual> = {
  atom: {
    cover: cover('lesson-atom.webp'),
    altAr: 'تصور بصري تعليمي للنواة والسحابة الإلكترونية، وليس مسارات مدارية حقيقية',
    altEn: 'An educational visualization of a nucleus and electron cloud, not literal orbital paths',
    focusAr: 'النواة تحدد هوية العنصر؛ تغيّر الإلكترونات يغيّر الشحنة.',
    focusEn: 'The nucleus defines the element; changing electrons changes charge.',
    badgeAr: 'بنية الذرة', badgeEn: 'Atomic structure',
  },
  matter: {
    cover: cover('lesson-matter.webp'),
    altAr: 'تمثيل للجسيمات في الحالات الصلبة والسائلة والغازية',
    altEn: 'A particle model of solid, liquid, and gaseous states',
    focusAr: 'تابع كيف تغيّر الحرارة حركة الجسيمات وترتيبها.',
    focusEn: 'See how heat changes particle motion and arrangement.',
    badgeAr: 'المادة والتحولات', badgeEn: 'Matter & change',
  },
  acid: {
    cover: cover('lesson-acid.webp'),
    altAr: 'تصور طيفي تعليمي يرمز إلى تغير الحموضة دون تجربة مخبرية',
    altEn: 'A conceptual spectrum illustrating acidity without a laboratory experiment',
    focusAr: 'مقياس pH لوغاريتمي؛ الدرجة الواحدة تعني فرقاً بعشرة أضعاف.',
    focusEn: 'The pH scale is logarithmic; one unit means a tenfold difference.',
    badgeAr: 'الأحماض والقواعد', badgeEn: 'Acids & bases',
  },
  balance: {
    cover: cover('lesson-equilibrium.webp'),
    altAr: 'تمثيل بصري لحركة التفاعلين الأمامي والعكسي عند الاتزان',
    altEn: 'A visual model of forward and reverse reactions at equilibrium',
    focusAr: 'الاتزان ديناميكي: يستمر التفاعلان بمعدلين متساويين.',
    focusEn: 'Equilibrium is dynamic: both reactions continue at equal rates.',
    badgeAr: 'الاتزان الكيميائي', badgeEn: 'Chemical equilibrium',
  },
  mole: {
    cover: cover('lesson-mole.webp'),
    altAr: 'تصور لموازنة عينة وتحويل كتلتها إلى مولات وجسيمات',
    altEn: 'A sample balance and a visualization of mass-to-moles-to-particles',
    focusAr: 'المول جسر بين كتلة قابلة للقياس وعدد هائل من الجسيمات.',
    focusEn: 'The mole links measurable mass to an immense number of particles.',
    badgeAr: 'المول والحسابات', badgeEn: 'The mole & calculations',
  },
  bond: {
    cover: cover('lesson-bond.webp'),
    altAr: 'تصور لسحابة إلكترونية موزعة بين ذرتين برابطة قطبية',
    altEn: 'A conceptual electron cloud shared unequally between two bonded atoms',
    focusAr: 'القطبية طيف متدرج ينتج من اختلاف جذب الذرتين للإلكترونات.',
    focusEn: 'Polarity is a continuum caused by unequal attraction for electrons.',
    badgeAr: 'الروابط الكيميائية', badgeEn: 'Chemical bonding',
  },
};
