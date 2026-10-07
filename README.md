# Skill Colours — RuneLite Plugin

Colours skill level text in the **Skills tab** based on your real (base) skill level, giving
you an immediate visual read on your progression without any overlay cluttering the interface.

## Colour Progression

| Level Range | Colour             | Notes                                                 |
|-------------|--------------------|-------------------------------------------------------|
| 1–45        | 🔴 Red             | Low / early level (`#CC2200`)                         |
| 46–65       | 🔴→🟠 Red→Orange   | Smooth linear RGB interpolation towards Orange        |
| 66–89       | 🟠→🟡 Orange→Yellow| Smooth linear RGB interpolation towards Yellow        |
| 90–98       | 🟡→🟢 Yellow→Green | Smooth linear RGB interpolation towards Green         |
| 99          | 🟡 Gold            | Max level (`#FFD700`) — distinctly gold and dimmed    |

Colours are calculated via RGB interpolation between anchor points, so the transition is
smooth and continuous — no jarring colour bands.

## Features

- **Dynamic colours** — reacts instantly to level-ups, XP lamps, and skill resets
- **Boost-immune** — colour is always based on your *real* level, not boosts or drains
- **99 gold + dimming** — max-level skills get a gold text colour and a subtle dimness (~20%)
  to indicate they're complete, without becoming hard to read
- **Zero-overlay approach** — directly sets widget text colours; no permanent canvas overlay
- **Extremely lightweight** — only recalculates on relevant events (stat changes, tab opens)

## Configuration

Open the RuneLite configuration panel and search for **Skill Colours**.

| Option                    | Default            | Description                                               |
|---------------------------|--------------------|-----------------------------------------------------------|
| Enable colour progression | ✅ On              | Toggle the level-based text colouring on/off              |
| Dim completed (99) skills | ✅ On              | Toggle the subtle dimming for max-level skill entries     |

### Custom Colours Section

You can click any colour in the plugin settings to open RuneLite's color picker:

| Level Anchor            | Default Colour       | Description                                              |
|-------------------------|----------------------|----------------------------------------------------------|
| **Level 1–45 (Red)**    | Red (`#CC2200`)      | Base colour for levels 1–45                              |
| **Level 46–65 (Orange)**| Orange (`#FF7000`)   | Target colour reached at level 65 (from level 45)        |
| **Level 66–89 (Yellow)**| Yellow (`#FFFF00`)   | Target colour reached at level 89 (from level 65)        |
| **Level 90–98 (Green)** | Green (`#00CC44`)    | Target colour reached at level 98 (from level 89)        |
| **Level 99 (Gold)**     | Gold (`#FFD700`)     | Colour used exclusively for max-level skills             |

## Technical Notes

### Widget Approach

The plugin modifies widget properties directly via `Widget.setTextColor()` and
`Widget.setOpacity()` — the same mechanism RuneLite itself uses for UI customisation.
No new overlay renderers are added; no coordinate hacks are used.

### Event Strategy

| Event              | Purpose                                                         |
|--------------------|-----------------------------------------------------------------|
| `WidgetLoaded`     | Recolour all skills when the Stats tab first opens              |
| `ScriptPostFired`  | Reapply after the game's own stats-redraw script fires (ID 393) |
| `StatChanged`      | Recolour the specific skill whose level/XP just changed         |
| `ConfigChanged`    | Re-apply when the user toggles config options                   |

### Colour Algorithm

```
level 1–54   → RED    (#CC2200)
level 55     → YELLOW (#FFFF00)  ┐
level 91     → ORANGE (#FF7000)  ┘  linear interpolation in RGB space
level 92     → ORANGE (#FF7000)  ┐
level 98     → GREEN  (#00CC44)  ┘  linear interpolation in RGB space
level 99     → GOLD   (#FFD700)
```

### 99 Dimming

For level-99 skills, the widget's `opacity` property is set to `50` (on a 0–255 scale),
producing approximately a 20% reduction in brightness.  This is applied to the skill's
container child widget so only that entry is affected.

## Building

```bash
./gradlew build
```

### Running tests

```bash
./gradlew test
```

### Running with the RuneLite client (developer mode)

You can double-click `run.bat` in the root folder, or run:

```bash
./gradlew run
```

## Compatibility

- RuneLite API: `latest.release`
- Java: 11+
- OSRS interface ID 320 (Stats tab) — all standard skills are supported automatically
  via `Skill.values()`, including any new skills added by future game updates

## Licence

BSD 2-Clause.  See source file headers for full licence text.
