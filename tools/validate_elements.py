#!/usr/bin/env python3
"""Check the offline periodic-table asset and selected scientific invariants."""
import json
import re
from pathlib import Path

PATH = Path(__file__).resolve().parents[1] / 'app/src/main/assets/elements.json'
ELEMENTS = json.loads(PATH.read_text(encoding='utf-8'))
CATEGORIES = {'alkali', 'alkaline', 'transition', 'post-transition', 'metalloid',
              'nonmetal', 'halogen', 'noble', 'lanthanide', 'actinide'}
REQUIRED_TEXT = ('symbol', 'name', 'nameEn', 'mass', 'category', 'block',
                 'stateAr', 'stateEn', 'descriptionAr', 'descriptionEn',
                 'sourceUrl', 'massSourceUrl')
assert len(ELEMENTS) == 118, 'Expected 118 elements'
assert [e['number'] for e in ELEMENTS] == list(range(1, 119)), 'Atomic-number ordering'
assert len({e['symbol'] for e in ELEMENTS}) == 118, 'Duplicate symbol'
assert len({e['nameEn'] for e in ELEMENTS}) == 118, 'Duplicate English name'
for element in ELEMENTS:
    z = element['number']
    assert all(isinstance(element.get(k), str) and element[k].strip() for k in REQUIRED_TEXT), z
    assert re.fullmatch(r'[A-Z][a-z]?', element['symbol']), z
    assert re.search(r'[\u0600-\u06ff]', element['name']), z
    assert re.search(r'[\u0600-\u06ff]', element['descriptionAr']), z
    assert not re.search(r'[\u0600-\u06ff]', element['descriptionEn']), z
    assert 1 <= element['period'] <= 7 and 1 <= element['group'] <= 18, z
    assert element['category'] in CATEGORIES and element['block'] in 'spdf', z
    assert element['sourceUrl'].startswith('https://periodic-table.rsc.org/element/'), z
    if element['massKind'] == 'standard':
        assert float(element['mass']) > 0 and float(element['massUncertainty']) > 0, z
        assert element['massSourceUrl'] == 'https://ciaaw.org/abridged-atomic-weights.htm', z
    else:
        assert element['massKind'] == 'isotope', z
        assert re.fullmatch(r'\[\d+\]', element['mass']) and element['massUncertainty'] == '', z
        assert int(element['mass'][1:-1]) >= z, z
        assert element['massSourceUrl'].endswith('IUPAC_Periodic_Table-04May22.pdf'), z
    if 57 <= z <= 71 or 89 <= z <= 103:
        assert element['group'] == 3 and element['block'] == 'f', z

by_z = {e['number']: e for e in ELEMENTS}
for z, symbol, period, group in [(1,'H',1,1),(2,'He',1,18),(17,'Cl',3,17),
                                  (26,'Fe',4,8),(79,'Au',6,11),(118,'Og',7,18)]:
    assert (by_z[z]['symbol'], by_z[z]['period'], by_z[z]['group']) == (symbol,period,group), z
assert by_z[40]['mass'] == '91.222' and by_z[40]['massUncertainty'] == '0.003'
assert by_z[43]['mass'] == '[97]' and by_z[103]['mass'] == '[262]'
assert by_z[118]['mass'] == '[294]'
assert by_z[35]['stateEn'] == by_z[80]['stateEn'] == 'Liquid'
assert by_z[31]['stateEn'] == 'Solid'
assert all('Not experimentally established' in by_z[z]['stateEn'] for z in (85,87,100,104,118))
print('PASS: 118 elements; bilingual fields, ordering, layout, mass conventions and reference cases')
