import os, time, json, glob, io, re
from typing import Optional
from kafka import KafkaProducer
from .config import (
    WOW_LOGS_DIRECTORY, WOW_LOG_FILE_PATTERN, KAFKA_BROKER, KAFKA_TOPIC,
    POLL_INTERVAL_SECONDS, SEND_ALL_EVENTS
)

from kafka.errors import NoBrokersAvailable



producer = KafkaProducer(
    bootstrap_servers=[KAFKA_BROKER],
    value_serializer=lambda v: json.dumps(v).encode("utf-8"),
    acks=1,                       # use int instead of "1"
    linger_ms=50,
    batch_size=64_000,
    compression_type="lz4",       # pip install lz4
    retries=5,
    retry_backoff_ms=200,
    client_id="wow-firehose",
)

if not producer.bootstrap_connected():
    raise NoBrokersAvailable(f"Cannot reach broker at {KAFKA_BROKER}")
print("✅ Connected to:", KAFKA_BROKER)

HDR_SPLIT = re.compile(r"\s{2,}", re.ASCII)

def find_latest_log_file(directory: str, pattern: str) -> Optional[str]:
    files = glob.glob(os.path.join(directory, pattern))
    if not files: return None
    return max(files, key=os.path.getmtime)

def parse_any(line: str):
    """Return dict for ANY combat log line (firehose)."""
    line = line.rstrip("\r\n")
    if not line: return None
    parts = HDR_SPLIT.split(line, maxsplit=1)
    if len(parts) != 2: return None
    ts_str, payload = parts
    if "," in payload:
        event, rest = payload.split(",", 1)
    else:
        event, rest = payload, ""
    return {
        "type": "raw_event",
        "ts": time.time(),            # wall clock (fast/portable)
        "event": event,
        "raw": rest                   # CSV tail (parse more downstream)
    }

def tail_and_send(path: str):
    print(f"👀 Tailing: {path}")
    with open(path, "r", encoding="utf-8", errors="ignore") as f:
        f.seek(0, io.SEEK_END)
        inode = os.fstat(f.fileno()).st_ino
        sent = 0
        last_log = time.time()
        while True:
            line = f.readline()
            if not line:
                # rotation check
                latest = find_latest_log_file(WOW_LOGS_DIRECTORY, WOW_LOG_FILE_PATTERN)
                try:
                    if latest and os.stat(latest).st_ino != inode:
                        print(f"🔄 Rotated → {latest}")
                        f.close()
                        f2 = open(latest, "r", encoding="utf-8", errors="ignore")
                        f = f2
                        f.seek(0, io.SEEK_END)
                        inode = os.fstat(f.fileno()).st_ino
                except Exception:
                    pass
                time.sleep(POLL_INTERVAL_SECONDS)
                continue

            evt = parse_any(line)
            if not evt: continue
            key = evt["event"].encode("utf-8", "ignore")
            producer.send(KAFKA_TOPIC, key=key, value=evt)
            sent += 1

            # lightweight progress log
            now = time.time()
            if now - last_log > 2:
                print(f"→ firehose events sent: +{sent} (last 2s window)")
                sent = 0
                last_log = now

if __name__ == "__main__":
    latest = find_latest_log_file(WOW_LOGS_DIRECTORY, WOW_LOG_FILE_PATTERN)
    if not latest:
        print(f"⚠️ No combat logs in '{WOW_LOGS_DIRECTORY}'. Run /combatlog.")
        raise SystemExit(1)
    tail_and_send(latest)
