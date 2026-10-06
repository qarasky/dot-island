# Live Updates

One generic island for ongoing Android Live Update notifications: deliveries,
rides, downloads and other progress-style activities. Dodo is the first verified
source, with a compatibility parser; it is no longer the identity of the card.

This is a **notification-listener integration**, not an HTTP API. Dot Island
does not poll apps, inspect their screens or replay their sounds. The separate
[publishing broadcast API](PUBLISHING_API.md) still creates `Published` cards.

## Allow a source

Open **Dot Island → Live Updates → Allowed notification sources** and add the
app's package name. Dodo (`ru.dodopizza.app`) is the only default source.
Remove a source to stop showing its Live Updates immediately; the original
notification stays untouched. Removing every source really leaves an empty
allowlist, including after restart. Enabling the island does not reset this list.

This list is independent of the publishing API's publisher allowlist. Adding an
app to one does not add it to the other. Spotify and native calls retain their
existing dedicated paths.

## Accepted notifications

All generic notifications must:

1. Belong to an explicitly allowed source package (the system-provided
   `StatusBarNotification.packageName`, not an identity supplied in extras).
2. Have `Notification.FLAG_ONGOING_EVENT`.
3. Use `android.app.Notification$ProgressStyle` (`Notification.EXTRA_TEMPLATE`)
   **or** have Android 16's `Notification.FLAG_PROMOTED_ONGOING`.
4. Not be a group summary, media notification or call notification.

Ordinary alerts and older generic progress notifications without that native
style/promotion are **not** automatically admitted—even from an allowed app.
This deliberately is not a mirror of every notification in the shade.

Dodo may additionally qualify through its conservative order-wording/ongoing
progress fallback. Its promotional filter applies even if progress-styled.
See [DODO_NOTIFICATIONS.md](DODO_NOTIFICATIONS.md) for the real order fixture.

## Rendered fields

| Source | Display |
| --- | --- |
| Actual package's application label | Source name (no hardcoded Dodo label) |
| Notification small icon | Monochrome source glyph; launcher-icon fallback, then generic glyph |
| `EXTRA_TITLE` | Status title |
| `EXTRA_BIG_TEXT`, otherwise `EXTRA_TEXT` | Additional detail |
| Explicit minute/range text | ETA, preserving `~` / `≈`; no local countdown |
| `EXTRA_PROGRESS` / `EXTRA_PROGRESS_MAX` | Dot-row opacity, bounded to 0–100% |
| `EXTRA_PROGRESS_INDETERMINATE` | Uniformly breathing dot row; no made-up percentage |
| Content intent | Open the source on main-pill/card tap |

Dodo keeps its white pizza glyph. Other sources use their own notification icon.
Native progress segments/tracker icons, chronometers, custom RemoteViews, action
buttons and custom colours are not interpreted in this version.

Compact status prioritizes a supplied ETA, then a known percentage, then `LIVE`
(`ORDER` for Dodo's compatibility fallback). The collapsed camera gap and
content-driven one-sided widening apply to every Live Update source.

Expanded cards retain equal 20dp outer insets, cutout-safe header alignment and
one 4dp dot row. Pulse is activity feedback, not progress interpolation. Ongoing
notifications may report 100% while still active; the final dot can continue to
pulse. Dodo's explicit delivered title stops its pulse. No maximum and no
indeterminate flag means **no progress row**.

Notification updates with the same key update the same island entry. Removing
the original notification removes its entry. Island dismissal never cancels
the source notification. Live Updates rank below native calls and above music.
This personal build keeps one Live Update slot: another accepted Live Update
replaces that slot, rather than creating an unlimited notification carousel.

## Replay on an emulator (without a real order)

Create an Android fixture app with your own package, for example
`dev.example.liveupdatefixture`, and `compileSdk = 36`. On a disposable Android
16 emulator, install Dot Island, grant its normal system access, then add the
fixture package to **Live Updates → Allowed notification sources**.

The fixture app needs Android 13+'s `POST_NOTIFICATIONS` permission declared and
granted. From its Activity on Android 16:

```kotlin
val manager = getSystemService(android.app.NotificationManager::class.java)
manager.createNotificationChannel(android.app.NotificationChannel(
    "live_replay", "Live Update replay",
    android.app.NotificationManager.IMPORTANCE_DEFAULT
))
val style = android.app.Notification.ProgressStyle()
    .addProgressSegment(android.app.Notification.ProgressStyle.Segment(100))
    .setProgress(40)
val notification = android.app.Notification.Builder(this, "live_replay")
    .setSmallIcon(android.R.drawable.ic_menu_compass)
    .setContentTitle("Delivery on the way")
    .setContentText("Arriving in ~12 min")
    .setOngoing(true)
    .setOnlyAlertOnce(true)
    .setStyle(style)
    .build()
manager.notify(42, notification)
```

Build another notification and call `notify(42, updated)` to replay an update.
Use `style.setProgressIndeterminate(true)` to test unknown progress, and omit
the ETA text to check the `LIVE` compact label. Add a fixture-owned content
intent to verify tap-to-open. `manager.cancel(42)` cleans up.

There is no fixture APK/module bundled yet. This is a recipe for a normal Android
app, not a new Dot Island broadcast endpoint. Unlike the Dodo-specific replay,
it does not impersonate a real package or require uninstalling a real app.
`adb shell cmd notification post` cannot supply this full native ProgressStyle;
publishing generic text by the custom API exercises the separate Published UI.

Replay checks:

- Before adding the package: ignored.
- Allowed + ongoing + ProgressStyle: source label/icon, ETA and dots.
- Same key updated to 70/100: same entry, dot opacity changes.
- Indeterminate: no fabricated completed segment or percentage.
- Allowed but ordinary notification/non-ongoing/group summary: ignored.
- Source removed from allowlist: island entry removed, shade unchanged.
- Dodo + Spotify companion: tap only swaps; hold expands; no drag movement.

## Verification

```bash
./gradlew assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug \
  --no-parallel --max-workers=1
```

Policy/presentation tests cover approved/revoked sources, ordinary-alert
rejection, full-vs-indeterminate progress, empty-list defaulting semantics and
Dodo compatibility. Building the Android test APK is not the same as running
it. Generic third-party Live Updates still need app-by-app on-device verification;
the real Dodo order has been observed on the A069.

Relevant code under `app/src/main/java/dev/qarasky/dotisland/`:

- `data/LiveUpdateSourceStore.kt` — durable source allowlist.
- `util/PersonalActivityPolicy.kt` — admission rules.
- `service/DotIslandNotificationListenerService.kt` — native notification fields.
- `ui/LiveUpdatePresentation.kt` — compact status and activity feedback.
- `ui/LiveUpdateProgressDots.kt` — determinate/indeterminate dots.
- `ui/expanded/LiveUpdateExpanded.kt` — generic card.

The internal enum value remains `IslandMode.LiveActivity`; the user-facing name
and renderer are **Live Updates**.
