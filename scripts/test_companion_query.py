import urllib.request
import json
import time
import subprocess

# Query launcher on port 38242 if running, or launch it
try:
    req = urllib.request.Request("http://127.0.0.1:38242/api/status")
    with urllib.request.urlopen(req, timeout=1.0) as resp:
        print("Port 38242 status:", resp.read().decode('utf-8'))
except Exception as e:
    print("Port 38242 not active yet:", e)
