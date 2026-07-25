#!/usr/bin/env python3
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
GALLERY = ROOT / "app/src/main/assets/gallery_v4.json"
CANONICAL = ROOT / "app/src/main/assets/canonical/ungtb10d_graffiti.txt"
MAIN = ROOT / "app/src/main/java/org/blckswan/art/MainActivity.kt"
WORK_ID = "git-log-graffiti"
SOURCE_URL = "https://github.com/ungtb10d/graffiti/blob/master/ungtb10d_graffiti.md"

art = CANONICAL.read_text(encoding="utf-8").rstrip("\n")
lines = art.splitlines()
if len(lines) != 10:
    raise SystemExit(f"Canonical graffiti must contain 10 lines, got {len(lines)}")
if not lines[3].startswith("#▄æ╥"):
    raise SystemExit("Canonical graffiti line 4 is damaged")
if max(map(len, lines)) < 90:
    raise SystemExit("Canonical graffiti width is unexpectedly short")

data = json.loads(GALLERY.read_text(encoding="utf-8"))
work = next((item for item in data["works"] if item["id"] == WORK_ID), None)
if work is None:
    raise SystemExit(f"Missing artwork {WORK_ID}")

work["art"] = art
work["artType"] = "text"
work["visualType"] = "KANONISK ORIGINALFLATE"
work["sourceUrl"] = SOURCE_URL
GALLERY.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

renderer = MAIN.read_text(encoding="utf-8")
required_renderer_markers = (
    'work.id == "git-log-graffiti" -> 2.75f',
    'work.art.trimEnd()',
    'contentAlignment = Alignment.CenterStart',
)
for marker in required_renderer_markers:
    if marker not in renderer:
        raise SystemExit(f"Canonical renderer marker missing: {marker}")

print(f"Synchronized {WORK_ID}: {len(lines)} lines, {max(map(len, lines))} columns")
