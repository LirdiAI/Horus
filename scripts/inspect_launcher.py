import os

with open("launcher/index.html", "r", encoding="utf-8") as f:
    orig = f.read()

print("tabBtnConfigs in orig:", "tabBtnConfigs" in orig)
print("contentVersions in orig:", "contentVersions" in orig)
print("contentConfigs in orig:", "contentConfigs" in orig)
print("contentAccounts in orig:", "contentAccounts" in orig)
print("contentBypasses in orig:", "contentBypasses" in orig)
