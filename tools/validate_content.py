#!/usr/bin/env python3
"""Validate offline bilingual content structure and cross-references; no network needed.

This detects broken content wiring, not scientific or translation accuracy.
Run from any directory: python3 tools/validate_content.py
"""
from collections import Counter
import json
from pathlib import Path
import re
import runpy
import sys
from urllib.parse import urlsplit

ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / 'app/src/main/assets'
ARABIC = re.compile(r'[\u0600-\u06ff]')
errors = []


def check(condition, message):
    if not condition:
        errors.append(message)


def unique_keys(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError(f'duplicate JSON key: {key}')
        result[key] = value
    return result


def load(name):
    try:
        result = json.loads((ASSETS / name).read_text(encoding='utf-8'), object_pairs_hook=unique_keys)
        check(isinstance(result, list), f'{name}: root must be an array')
        return result if isinstance(result, list) else []
    except (OSError, ValueError) as exc:
        errors.append(f'{name}: {exc}')
        return []


def text(value, location, english=False):
    check(isinstance(value, str) and bool(value.strip()), f'{location}: nonempty text required')
    if english and isinstance(value, str):
        check(not ARABIC.search(value), f'{location}: untranslated Arabic in English content')


def url(value, location):
    try:
        parsed = urlsplit(value)
        valid = parsed.scheme == 'https' and bool(parsed.hostname) and not parsed.username and not parsed.password
    except (TypeError, ValueError, AttributeError):
        valid = False
    check(valid, f'{location}: public HTTPS URL required')


def ids(items, location):
    result = [item.get('id') for item in items if isinstance(item, dict)]
    check(len(result) == len(items), f'{location}: every entry must be an object')
    check(all(isinstance(value, str) and re.fullmatch(r'[a-z0-9]+(?:-[a-z0-9]+)*', value) for value in result),
          f'{location}: stable ASCII IDs required')
    check(len(set(result)) == len(result), f'{location}: duplicate IDs')
    return result


def validate_lessons(items, english):
    label = 'lessons_en.json' if english else 'lessons.json'
    lesson_ids = ids(items, label)
    check(len(items) >= 42, f'{label}: fewer than the 42 original lessons')
    allowed_levels = {'Foundation', 'Intermediate', 'University', 'Specialist'} if english else {'تأسيسي', 'متوسط', 'جامعي', 'تخصصي'}
    check({l.get('level') for l in items} == allowed_levels, f'{label}: four level labels expected')
    for lesson in items:
        if not isinstance(lesson, dict):
            continue
        where = f'{label}:{lesson.get("id", "?")}'
        for key in ('title', 'level', 'section', 'intro', 'detail', 'example', 'summary', 'source'):
            text(lesson.get(key), f'{where}.{key}', english)
        objectives = lesson.get('objectives', [])
        check(isinstance(objectives, list) and bool(objectives), f'{where}: objectives required')
        if isinstance(objectives, list):
            for n, value in enumerate(objectives):
                text(value, f'{where}.objectives[{n}]', english)
        prerequisites = lesson.get('prerequisites', [])
        check(isinstance(prerequisites, list), f'{where}: prerequisite list required')
        if isinstance(prerequisites, list):
            check(len(prerequisites) == len(set(prerequisites)), f'{where}: duplicate prerequisites')
            for dependency in prerequisites:
                check(dependency in lesson_ids, f'{where}: unknown prerequisite {dependency}')
                check(dependency != lesson.get('id'), f'{where}: self prerequisite')
        sources = lesson.get('sourceUrls', [])
        check(isinstance(sources, list) and bool(sources), f'{where}: source URLs required')
        if isinstance(sources, list):
            for value in sources:
                url(value, where)
        questions = lesson.get('questions', [])
        check(isinstance(questions, list) and len(questions) >= 3, f'{where}: at least three MCQs required')
        if not isinstance(questions, list):
            continue
        prompts = []
        for n, question in enumerate(questions):
            qwhere = f'{where}.questions[{n}]'
            check(isinstance(question, dict), f'{qwhere}: object required')
            if not isinstance(question, dict):
                continue
            for key in ('question', 'explanation'):
                text(question.get(key), f'{qwhere}.{key}', english)
            prompts.append(question.get('question'))
            options = question.get('options', [])
            check(isinstance(options, list) and len(options) >= 3, f'{qwhere}: at least three options required')
            if isinstance(options, list):
                check(len(options) == len(set(options)), f'{qwhere}: duplicate options')
                for value in options:
                    text(value, qwhere, english)
                answer = question.get('answer')
                check(type(answer) is int and 0 <= answer < len(options), f'{qwhere}: answer outside options')
        check(len(prompts) == len(set(prompts)), f'{where}: duplicate question prompts')
    # Detect dependency loops even if links refer to later lessons.
    graph = {lesson['id']: lesson.get('prerequisites', []) for lesson in items if isinstance(lesson, dict) and isinstance(lesson.get('id'), str)}
    visiting, visited = set(), set()

    def visit(node):
        if node in visiting:
            errors.append(f'{label}: prerequisite cycle at {node}')
            return
        if node in visited:
            return
        visiting.add(node)
        for dependency in graph.get(node, []):
            if isinstance(dependency, str) and dependency in graph:
                visit(dependency)
        visiting.remove(node)
        visited.add(node)

    for node in graph:
        visit(node)
    return lesson_ids


def validate_guides(items, english):
    label = 'guides_en.json' if english else 'guides_ar.json'
    guide_ids = ids(items, label)
    counts = Counter(guide.get('section') for guide in items)
    check(set(counts) == {'geology', 'gold', 'safety'}, f'{label}: unknown or missing guide section')
    check(all(counts[section] >= 3 for section in ('geology', 'gold', 'safety')), f'{label}: at least three guides per section')
    for guide in items:
        where = f'{label}:{guide.get("id", "?")}'
        for key in ('title', 'intro', 'reviewStatus'):
            text(guide.get(key), f'{where}.{key}', english)
        topics = guide.get('topics', [])
        check(isinstance(topics, list) and bool(topics), f'{where}: topic list required')
        if isinstance(topics, list):
            ids(topics, where)
            for topic in topics:
                for key in ('title', 'body'):
                    text(topic.get(key), f'{where}.{key}', english)
        sources = guide.get('sources', [])
        check(isinstance(sources, list) and bool(sources), f'{where}: source list required')
        if isinstance(sources, list):
            for source in sources:
                text(source.get('title'), f'{where}.source.title')
                url(source.get('url'), f'{where}.source.url')
    return guide_ids


def main():
    ar, en = load('lessons.json'), load('lessons_en.json')
    ar_ids, en_ids = validate_lessons(ar, False), validate_lessons(en, True)
    check(ar_ids == en_ids, 'Lessons: translation IDs/order mismatch')
    for a, e in zip(ar, en):
        where = f'Lessons:{a.get("id", "?")}'
        check(a.get('prerequisites') == e.get('prerequisites'), f'{where}: translated prerequisite mismatch')
        aq, eq = a.get('questions', []), e.get('questions', [])
        check(len(aq) == len(eq), f'{where}: translated question count mismatch')
        for x, y in zip(aq, eq):
            check(x.get('answer') == y.get('answer'), f'{where}: translated answer mismatch')
            check(len(x.get('options', [])) == len(y.get('options', [])), f'{where}: translated options mismatch')
    ga, ge = load('guides_ar.json'), load('guides_en.json')
    check(validate_guides(ga, False) == validate_guides(ge, True), 'Guides: translation IDs/order mismatch')
    for a, e in zip(ga, ge):
        check(a.get('section') == e.get('section'), f'Guide {a.get("id")}: translated section mismatch')
        check([t.get('id') for t in a.get('topics', [])] == [t.get('id') for t in e.get('topics', [])],
              f'Guide {a.get("id")}: translated topic IDs/order mismatch')
        check(a.get('sources') == e.get('sources'), f'Guide {a.get("id")}: translated references mismatch')
    try:
        runpy.run_path(str(ROOT / 'tools/validate_elements.py'), run_name='__main__')
    except (AssertionError, KeyError, TypeError, ValueError, OSError) as exc:
        errors.append(f'Periodic table: {exc}')
    if errors:
        print('\n'.join(f'FAIL: {error}' for error in errors), file=sys.stderr)
        return 1
    print(f'PASS: {len(ar)} bilingual lessons, {sum(len(l["questions"]) for l in ar)} MCQs per language; '
          f'{len(ga)} bilingual guides, {sum(len(g["topics"]) for g in ga)} topics per language; valid prerequisites and HTTPS references')
    print('Structural checks do not replace scientific, translation or local geological review.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
