# BLCKSWAN KUNST

**Spatial Exhibition Engine** — Native Android gallery that treats artworks as rooms.

Built from the original `bspippi1337/kunst` foundation, rewritten as a full immersive Compose experience.

## What it is

- Offline-first art archive
- Horizontal spatial pager with parallax + perspective
- Per-work visual materials: CRT scanlines, phosphor bloom, glitch overlay
- Immersive fullscreen viewer with chrome toggle
- Crash-defensive loading of `exhibition.json`
- Pure BLCKSWAN aesthetic (ink / phosphor / fog / moon-red)

## Architecture

```
org.blckswan.art
├── data/
│   ├── models/          # Exhibition, Work, VisualMode
│   └── ExhibitionRepository.kt
├── ui/
│   ├── theme/           # Colors + typography
│   ├── materials/       # scanlines, glitch, phosphor modifiers
│   ├── gallery/         # SpatialGallery + ImmersiveViewer
│   └── components/
└── MainActivity.kt
```

## Quick start

1. Open in Android Studio (Ladybug / 2025+ recommended)
2. Sync Gradle
3. Run on device or emulator (minSdk 26)

No internet permission required for core experience.

## exhibition.json schema

```json
{
  "title": "BLCKSWAN ART",
  "subtitle": "GitHub er lerretet.",
  "edition": "Spatial Exhibition v2",
  "works": [
    {
      "id": "unique-id",
      "title": "Title",
      "medium": "Medium",
      "description": "Optional body text",
      "sourceUrl": "https://...",
      "imageAsset": "works/filename.png",
      "visualMode": "static | glitch | phosphor | interactive",
      "glitchIntensity": 0.0–1.0,
      "audioAsset": null
    }
  ]
}
```

Place images under `app/src/main/assets/works/`.

## Visual modes

| Mode         | Effect                                      |
|--------------|---------------------------------------------|
| static       | Subtle scanlines only                       |
| glitch       | Scanlines + RGB fringe + occasional tear    |
| phosphor     | Soft pulsing green bloom + light scanlines  |
| interactive  | Combined glitch + bloom (for living works)  |

## Design rules

- Background: `#040706`
- Primary: `#66F59A` (phosphor)
- Fog text: `#B4C4BA`
- Accent glitch: `#FF2D95`
- No Material elevation shadows — hard edges or phosphor glow only
- Monospace for titles and labels

## Build (GitHub Actions)

Workflow: `.github/workflows/build.yml`

Triggers on push/PR to `main`, manual dispatch, and tags.

Produces a release APK artifact + SHA256.  
On tags it also creates a GitHub Release with the APK attached.

## Status

MVP Spatial + Glitch ready.

Next possible layers:
- Real image loading via Coil from assets
- AGSL custom shaders for heavier glitch
- Generative canvas per work
- Multiple exhibition files + switcher
- Haptic feedback on page settle

---

**BLCKSWAN** · Node-42 · Restless edition

