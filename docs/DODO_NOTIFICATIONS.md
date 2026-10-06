# Dodo order notifications: contract and replay

Dodo is now a source of the generic **[Live Updates](LIVE_UPDATES.md)** island.
This document preserves its observed fields and compatibility fallback. Its
source package is allowed by default but can be removed in the Live Updates panel.

Dot Island does **not** call a Dodo HTTP API, poll the order, or read Dodo's
screen. Its Android `NotificationListenerService` reads notifications posted by
**`ru.dodopizza.app`** and maps recognized order updates to `IslandMode.LiveActivity`.
The public [publishing API](PUBLISHING_API.md) is a separate path: it produces
`Published` cards, not the Dodo layout.

## Real fixture (observed 6 October 2026)

An actual ongoing order on the A069 / Android 16 posted:

| Field | Observed value |
| --- | --- |
| Package | `ru.dodopizza.app` |
| Channel ID | `3` |
| Notification ID | `-2109421313` (this order only; not a detection rule) |
| Tag | `null` |
| Flags | `ONGOING_EVENT`, `ONLY_ALERT_ONCE`, `PROMOTED_ONGOING` |
| `android.title` | `Готовим` |
| `android.text` | `Будем через ~43 мин` |
| `android.progress` | `50` |
| `android.progressMax` | `100` |
| `android.progressIndeterminate` | `false` |
| `android.template` | `android.app.Notification$ProgressStyle` |
| Large icon | absent |
| Content intent | activity intent belonging to Dodo |

Dodo also supplied progress segments and a tracker icon. Dot Island currently
does not consume those fields. The original small/app icons are not used for
the order glyph: the island draws a white pizza glyph instead.

Expanded layout uses music's cutout-aware header positioning and equal 20dp
outer insets. Only elements overlapping the physical camera exclusion move down;
Dodo text has 8dp camera clearance. The initial body-height estimate is 112dp
(previously 155dp); actual content is measured rather than forced to music's
180dp height.

Collapsed order pills measure the monospace ETA and reserve the physical cutout
plus 6dp clearance on each side. Content that does not fit grows only its own
edge: a longer ETA extends the right edge without moving the left/icon edge.
The small accessibility touch window uses the same geometry. Expanded order
glyphs center vertically against the title/subtitle block after cutout avoidance.

Expanded progress is a single white dot row: opacity represents the exact source
ratio, with dim remaining dots and a gentle pulse at the progress frontier. The
pulse does not advance progress, stops on an explicit delivered/completed title, and is gated by lifecycle,
screen-interactive state, battery saver and reduced-motion settings. Without a
source progress maximum, no progress row is invented. The row retains the old
bar's 4dp height, so this does not make the card taller.

Observed during the same order: `Везем заказ`, `Будем через ~22 мин`, progress
`100/100`. This is still an active delivery, not proof of completion; at full
source progress the final dot pulses while all other dots remain bright.
The ETA and pizza glyph align with the title/subtitle block. Side and bottom
insets are locked to the same 20dp value.

When music is the companion circle, tapping it only swaps the main activity;
holding it selects and expands. The compact overlay window reserves the union
of both activities' layouts, so source-switch animation cannot be clipped by an
early window shrink. Drag/reposition animation is disabled; swipe commands do
not move the pill.

This fixture contains no order ID, delivery address, or other customer details.
The notification is a snapshot: the Dodo screen can show a newer estimate before
its next notification update. **Do not decrement the ETA locally.**

## Fields Dot Island actually consumes

- Title: `Notification.EXTRA_TITLE`.
- Text: `EXTRA_BIG_TEXT` when present, otherwise `EXTRA_TEXT`.
- Progress: `EXTRA_PROGRESS` / `EXTRA_PROGRESS_MAX`; ratio clamped to 0–1.
  No progress bar when the maximum is zero/missing.
- Ongoing flag and group-summary flag for classification.
- Notification key: updating the same ID/tag updates the same island entry;
  notification removal removes that entry.
- Content intent: tapping opens the source activity. Actions and icons are also
  ingested, but the Dodo card has no order-changing buttons.

Only the exact Dodo package is accepted. Group summaries and recognized
promotional text are rejected. Recognized English/Russian order wording, or an
ongoing notification with a positive progress maximum, qualifies as tracking.
See `PersonalActivityPolicy.kt` and `LiveActivityParser.kt` for the full rules.

ETA parsing recognizes minute values/ranges, including `~43 мин`, `≈15 мин`,
`15–20 мин` and `12 min`. Approximation markers are preserved. An ETA-only
sentence such as `Будем через ~43 мин` is omitted from the detail area when the
header already shows it; other text remains. No delivery stages or percentage
are inferred from the title.

## Replay now: parser regression test (no device)

```bash
./gradlew testDebugUnitTest \
  --tests 'dev.qarasky.dotisland.util.LiveActivityParserTest' \
  --no-parallel --max-workers=1
```

`actualDodoProgressNotificationIsRecognizedWithoutLosingApproximation` contains
the real fixture above. This verifies classification, ETA, progress and detail
deduplication, **not** notification-listener delivery or rendered pixels.

## Replay later: exact notification path on a disposable emulator

There is no simulator app or replay endpoint in this repository yet. To test
end-to-end without an order, create a small Android fixture app with application
ID **`ru.dodopizza.app`**, on an emulator where real Dodo is **not installed**.
Install Dot Island there and grant its notification-listener/accessibility access.
Its geometry is tuned for the A069, so emulator visuals may differ.

**Never install this fixture app on the personal phone or uninstall real Dodo to
make room for it.** Android will reject its unrelated signature if real Dodo is
already installed. A different package name will be filtered out by Dot Island.

The fixture Activity can post this ordinary progress notification:

```kotlin
// Run inside the fixture app (applicationId = "ru.dodopizza.app").
// Android 13+: declare and obtain POST_NOTIFICATIONS first.
val manager = getSystemService(android.app.NotificationManager::class.java)
manager.createNotificationChannel(android.app.NotificationChannel(
    "3", "Order replay", android.app.NotificationManager.IMPORTANCE_DEFAULT
))
val notification = android.app.Notification.Builder(this, "3")
    .setSmallIcon(android.R.drawable.ic_menu_compass)
    .setContentTitle("Готовим")
    .setContentText("Будем через ~43 мин")
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    .setProgress(100, 50, false)
    .build()
manager.notify(42, notification)

// Update: build another notification and notify(42, updatedNotification).
// Cleanup: removes the island activity without touching other notifications.
// manager.cancel(42)
```

The replay does not need Android 16 `ProgressStyle` or promoted-ongoing flags:
Dot Island currently relies on the common fields above. Add a fixture-owned
content intent if testing tap-to-open behavior.

Suggested synthetic updates (only the first row is the observed fixture):

| Title | Text | Progress/max | Check |
| --- | --- | --- | --- |
| `Готовим` | `Будем через ~43 мин` | 50/100 | White glyph, status, approximate ETA, half-full bar |
| `Готовим` | `Будем через ~40 мин` | 50/100 | Same entry, ETA updates without duplicating text |
| `Курьер в пути` | `Через 15 мин` | 80/100 | Source status and progress update |
| `Заказ принят` | `Готовим пиццу` | 0/0 | Detail text, no invented ETA or bar |
| `Ваш заказ` | `Промокод на скидку` | 50/100 | Promotion rejected, existing same-key entry removed |
| cancel notification | — | — | Entry disappears |

`adb shell cmd notification post` posts as the shell package, so it **cannot**
exercise this exact Dodo path. Likewise, publishing Dodo-like text through Dot
Island's broadcast API tests a different card and is not proof of Dodo support.

## Implementation references

Under `app/src/main/java/dev/qarasky/dotisland/`:

- `service/DotIslandNotificationListenerService.kt` — ingest, updates, removals.
- `util/PersonalActivityPolicy.kt` — package/source selection.
- `util/LiveActivityParser.kt` — classification and source-value parsing.
- `ui/IslandCollapsedContent.kt` — monochrome glyph and compact ETA.
- `ui/expanded/LiveUpdateExpanded.kt` — generic status/ETA and progress layout.
