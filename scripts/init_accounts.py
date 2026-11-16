import json
from pathlib import Path

accounts_file = Path(r"C:\Users\Никита\Downloads\HorusClient\launcher\accounts.json")
data = {
    "active": "Lumira",
    "accounts": [
        {"username": "Lumira", "role": "ADMIN", "avatar": "https://mc-heads.net/avatar/Lumira/64"},
        {"username": "Nikita", "role": "ADMIN", "avatar": "https://mc-heads.net/avatar/Nikita/64"}
    ]
}
with open(accounts_file, "w", encoding="utf-8") as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("Created accounts.json successfully!")
