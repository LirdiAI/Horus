with open('launcher/index.html', 'r', encoding='utf-8') as f:
    text = f.read()

checks = {
    'tabBtnConfigs removed': 'tabBtnConfigs' not in text,
    'quickConfigSelect removed': 'quickConfigSelect' not in text,
    'contentConfigs removed': 'contentConfigs' not in text,
    'ReallyWorld Anarchy preset removed': 'ReallyWorld Anarchy' not in text,
    'Horus 1.21.11 in quickVersionSelect': 'Horus 1.21.11 (Latest Release)' in text,
    'Only Horus 1.21.11 in version options': '1.20.4' not in text and '1.21.4' not in text,
    'Add account form present': 'newAccountNickname' in text and 'handleCreateAccountForm' in text,
    'User accounts list present': 'launcherAccountsList' in text,
    'Account delete function present': 'deleteLauncherAccount' in text,
    'No hardcoded test accounts array': 'localAccounts = [' not in text and 'Nikita_2026' not in text and 'HorusPegas' not in text and 'NoMakoron' not in text,
    'Supabase sync updated': 'swarweprhwoelirrmztv.supabase.co' in text,
}

all_ok = True
for name, res in checks.items():
    print(f'[{ "OK" if res else "FAIL" }] {name}')
    if not res: all_ok = False

if all_ok:
    print("\nALL CHECKS PASSED PERFECTLY!")
