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
typography_replacements = {
    'TextStyle(FontFamily.SansSerif, FontWeight.Black, 40.sp, 42.sp, (-1.3).sp)':
        'TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 42.sp, letterSpacing = (-1.3).sp)',
    'TextStyle(FontFamily.SansSerif, FontWeight.Black, 30.sp, 32.sp, (-0.8).sp)':
        'TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 30.sp, lineHeight = 32.sp, letterSpacing = (-0.8).sp)',
    'TextStyle(FontFamily.SansSerif, FontWeight.Bold, 22.sp, 25.sp, (-0.2).sp)':
        'TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 25.sp, letterSpacing = (-0.2).sp)',
    'TextStyle(FontFamily.SansSerif, FontWeight.Normal, 17.sp, 26.sp)':
        'TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp)',
    'TextStyle(FontFamily.SansSerif, FontWeight.Normal, 15.sp, 23.sp)':
        'TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp)',
    'TextStyle(FontFamily.Monospace, FontWeight.Bold, 10.sp, 13.sp, 0.6.sp)':
        'TextStyle(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.6.sp)',
}
for old, new in typography_replacements.items():
    renderer = renderer.replace(old, new)

required_renderer_markers = (
    'work.id == "git-log-graffiti" -> 2.75f',
    'work.art.trimEnd()',
    'contentAlignment = Alignment.CenterStart',
    'fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black, fontSize = 40.sp',
)
for marker in required_renderer_markers:
    if marker not in renderer:
        raise SystemExit(f"Renderer marker missing: {marker}")

MAIN.write_text(renderer, encoding="utf-8")
print(f"Synchronized {WORK_ID}: {len(lines)} lines, {max(map(len, lines))} columns")
