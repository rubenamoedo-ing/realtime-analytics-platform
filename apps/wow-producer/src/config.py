import os, re
from pathlib import Path
from dotenv import load_dotenv

ROOT = Path(__file__).resolve().parents[1]
load_dotenv(ROOT / ".env")

def normalize_windows_path(p: str) -> str:
    if not p: return p
    if os.name != "nt" and re.match(r"^[A-Za-z]:", p):
        drive = p[0].lower(); rest = p[2:].replace("\\","/")
        return f"/mnt/{drive}{rest}"
    return p

KAFKA_BROKER = os.getenv("KAFKA_BROKER", "localhost:9092")  
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "wow.player.metrics.v1")
WOW_LOGS_DIRECTORY = normalize_windows_path(os.getenv("WOW_LOGS_DIRECTORY",""))
WOW_LOG_FILE_PATTERN = os.getenv("WOW_LOG_FILE_PATTERN","WoWCombatLog*.txt")
POLL_INTERVAL_SECONDS = float(os.getenv("POLL_INTERVAL_SECONDS","0.05"))
SEND_ALL_EVENTS = os.getenv("SEND_ALL_EVENTS","false").lower() == "true"
