export type Locale = 'ar' | 'en';
export type Theme = 'light' | 'dark';
export type SimulationKind = 'atomic-charge' | 'energy' | 'ph' | 'equilibrium' | 'mole' | 'bond';

export type LessonText = {
  title: string;
  subject: string;
  duration: string;
  level: string;
  summary: string;
  goal: string;
  why: string;
  prerequisite: string;
  objectives: string[];
  conceptMap: { label: string; detail: string }[];
  explanation: { heading: string; body: string }[];
  equation: {
    heading: string;
    expression: string;
    note: string;
    symbols: { symbol: string; meaning: string; unit: string }[];
  };
  workedExample: {
    heading: string;
    problem: string;
    steps: { label: string; body: string }[];
    answer: string;
    takeaway: string;
  };
  comparison: {
    heading: string;
    columns: string[];
    rows: { label: string; values: string[] }[];
  };
  applications: { title: string; body: string }[];
  misconception: { claim: string; correction: string };
  quiz: { question: string; options: string[]; correctIndex: number; explanation: string };
  safety: { title: string; body: string };
  summaryPoints: string[];
  glossary: { term: string; definition: string }[];
};

export type Lesson = {
  id: string;
  kind: SimulationKind;
  ar: LessonText;
  en: LessonText;
};
