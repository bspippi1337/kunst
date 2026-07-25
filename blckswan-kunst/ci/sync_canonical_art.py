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

source = MAIN.read_text(encoding="utf-8")
replacements = {
    ".aspectRatio(1.22f)": ".aspectRatio(if (work.id == \"git-log-graffiti\") 2.35f else 1.22f)",
    "modifier = modifier.background(Stage),\n        contentAlignment = Alignment.Center\n    ) {\n        if (work.artType == \"text\" && work.art.isNotBlank()) {":
        "modifier = modifier.background(Stage),\n        contentAlignment = if (work.id == \"git-log-graffiti\") Alignment.CenterStart else Alignment.Center\n    ) {\n        if (work.artType == \"text\" && work.art.isNotBlank()) {",
    "text = previewArt(work.art, 20),":
        "text = if (work.id == \"git-log-graffiti\") work.art.trimEnd() else previewArt(work.art, 20),",
    "modifier = Modifier.padding(14.dp)\n            )\n        } else {":
        "modifier = Modifier.padding(\n                    horizontal = if (work.id == \"git-log-graffiti\") 8.dp else 14.dp,\n                    vertical = if (work.id == \"git-log-graffiti\") 6.dp else 14.dp\n                )\n            )\n        } else {",
}
for old, new in replacements.items():
    if old in source:
        source = source.replace(old, new, 1)
    elif new not in source:
        raise SystemExit(f"Renderer patch target missing: {old[:60]!r}")
MAIN.write_text(source, encoding="utf-8")

print(f"Synchronized {WORK_ID}: {len(lines)} lines, {max(map(len, lines))} columns")
