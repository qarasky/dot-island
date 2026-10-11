# Island animation audit

Reviewed 11 October 2026 against commit `55b1af8`. This is a source-code review,
not a measured frame-time report. No runtime changes, APK installation or phone
settings changes were made for this audit.

## Implementation follow-up (11 October 2026)

The original findings below describe the pre-fix source. The working-tree fixes
now include:

- Overlay-owned, per-source height measurements keyed by text, progress-row
  presence, font scale, width, density and camera geometry. Cold expansion first
  measures the final-width card while it is invisible, then starts the morph.
  Warm reopening reuses the measurement without retaining hidden card timers.
- Stable-keyed pager with height interpolation read in layout, not a per-frame
  callback/effect feeding another spring. Collapse starts from the last drawn
  height, including after a partially completed page change.
- Companion geometry in a small isolated renderer with layout/layer reads and
  only the needed endpoint content. Both hold detectors use 300ms. The built-in
  long-click haptic is disabled so only the requested expansion haptic fires.
- Explicit expand/collapse gesture commands; the blanket 350ms toggle gate and
  competing source-switch scale bounce are removed. Main size changes remain
  interruptible, with critically damped collapse.
- Music cards use the listener's snapshot and command bus, not another controller
  and synchronous state/metadata queries on every open. Timeline extrapolation
  retains the source position-update time/speed and does not advance buffering.
  The duplicate Spotify publication after session selection is removed.
- Package labels/icons are bounded-cached; native notification drawable loading
  and rasterization happen off the main thread. Version checks protect both
  notification keys and the single native slot against out-of-order workers,
  removal, disabling, source revocation and listener disconnection.
- Cutout-aware music **and** Live Update headers keep their screen-space position
  during the fade instead of independently sliding/scaling under the camera.
- Separate R8-optimized, non-debuggable, shell-profileable `benchmark` variant,
  debug-signed as `dev.qarasky.dotisland.benchmark`. It does not replace the
  personal app. `./gradlew assembleBenchmark` builds it; it was not installed
  on the phone or granted any system access.

Android test dependencies were updated to Espresso 3.7.0 / Test JUnit 1.3.0
because the older runner failed on Android 16 before entering a test, trying to
reflect `InputManager.getInstance`. The final functional run used the A069 phone,
not the slow emulator, with `install -r` and direct instrumentation (no app-data
clearing or permission/settings changes). Five phone UI tests passed, including
companion tap/hold, stable measured height on reopening and the tiny idle reveal.
These are Activity-hosted functional tests, **not** a system-overlay frame-time
benchmark. Instrumentation force-stopped the original target and Android stranded
its accessibility binding. Tests now default to the isolated `.uitest` app ID
(`assembleUitestAndroidTest`, `testUitestUnitTest`) so future test runs do not kill
the personal overlay process.

Connection recovery was also corrected: no overlay attachment before Android's
service connection/token is initialized; observable, instance-owned connection
state instead of a RESUMED-only polling status; live grant observers; bounded
`CONNECTING` messaging with service-specific retry guidance. Disabling the service
component was tried during recovery but Android revoked its grant, so that path
was removed and only Dot Island's original grant restored. Other accessibility
entries were preserved. A screenshot on the A069 now confirms **ACTIVE** and the
actual small camera ring; a real overlay tap revealed the `IDLE` pill. The final
isolated phone run passed all five UI tests in about seven seconds, and 135 unit
tests plus debug/isolated lint passed. Cold/warm frame-time tracing still needs
verification; these results are not a measured 120Hz guarantee.

Remaining measurement-dependent work: the listener still owns synchronous media
session discovery/queries; actual bitmap-upload/GPU cost, display cadence and
window-readiness latency need a real overlay trace. There is no custom baseline
profile or measured before/after p95/p99 frame-time claim yet. Compact transitions
retain the existing system refresh policy; no permanent high-refresh vote or
phone refresh setting was introduced.

## Summary

There are both **motion/response defects** (movement can look delayed even when
every frame renders) and **frame-time risks** (work can cause missed frames).
Changing easing or shortening every animation would not solve both.

The existing architecture already does several things correctly:

- Main width/height animation values are read in layout, not directly in the
  root composition; alpha/translation/scale mostly use graphics-layer lambdas.
- Expanded content has a fixed final width, avoiding intermediate text wrapping.
- The morph waits for the expanded Android window to finish layout.
- WindowManager parameters are deduplicated; the window does not resize every
  animation frame. Collapse waits for transition completion before shrinking it.
- Compact content uses source identity, not changing metadata, for fade keys.
- The seek-bar wave is disabled by default. Dot animation reads are in drawing.

Preserve these choices rather than replacing the overlay with an unrelated
navigation animation or animating WindowManager dimensions frame by frame.

## Findings, in recommended fix order

### 1. Expansion can finish, then start a second height adjustment

`ui/IslandOverlayView.kt:201–229, 985–1004` starts from an estimated height,
defers actual measurements while the transition runs, and applies them after
settling. That changes the animated height target again. `ui/IslandModePresentation.kt`
estimates music at 180dp and Live Updates at 112dp; actual content can differ.
`ui/expanded/IslandExpandedContent.kt:36` also keeps measurements inside the
expanded subtree, which is removed after collapse.

**Confirmed control-flow issue:** a mismatched estimate produces two-stage
movement, not proof of dropped frames. It is especially plausible on first open,
source changes and differently sized Live Update text.

**Recommendation:** retain measurements outside the disposable card subtree,
keyed by source/content and layout inputs; measure new content at final width
before revealing it where practical. Animate toward one resolved height. Do not
keep invisible media callbacks/pulses running merely to preserve measurements.

### 2. Pager-driven height is being animated twice

`ui/expanded/IslandExpandedContent.kt:50–55` interpolates height from the pager's
animated offset and launches a height callback effect as that value changes.
`ui/IslandOverlayView.kt:306–312, 992–1004` feeds those updates to another spring,
or defers them while it runs. This can cause trailing/stepped card resizing and
extra recomposition/effect work during page changes.

**Confirmed coupling; frame-time impact unmeasured.** Let the pager directly
drive interpolated height during page motion; use a spring for discrete size
changes only. Keep high-frequency reads out of the broad composition scope.
Add stable notification keys to the pager so list reorder/removal cannot silently
reuse a page's remembered state for another source.

### 3. Expensive source work shares the animation's main thread

`service/DotIslandNotificationListenerService.kt:50, 174–214, 217–271` runs
ingestion/session publication on `Dispatchers.Main.immediate`. It reads media
sessions/state/metadata, loads package labels/icons and rasterizes drawables.
A Spotify notification also publishes through session selection and again
explicitly in `ingest`, duplicating some work.

`ui/expanded/MusicExpanded.kt:84–111` creates a controller, reads state/metadata
and registers a callback when the card is composed, including on reopening.

**Concrete synchronous-work risk, not a measured stall.** Prefer the listener's
existing media snapshot/controller owner, cache package/icon resources, and move
safe resource processing away from the UI thread. Preserve event ordering and
discard stale background results after revocation/removal. Do not blindly move
repository/session mutation to concurrent workers.

### 4. Companion morph still reads animation state in root composition

`ui/IslandOverlayView.kt:440–446, 1038, 1054–1063` reads animated companion width
and corner values while building modifiers, and calculates pill progress there.
This invalidates the large overlay composition during companion size changes,
despite the main morph's deferred reads. The companion also builds both circle
and mini-pill content; alpha zero does not dispose the hidden branch.

**Confirmed phase choice; actual cost requires a trace.** Isolate the companion
into a small composable; use layout/layer/draw lambdas for geometry and alpha.
Compose only the needed endpoint content when no transition is running.

### 5. A 350ms toggle gate drops legitimate rapid reversals

`ui/IslandViewModel.kt:158–165` ignores any toggle within 350ms of the previous
one. A quick outside tap after expansion can therefore do nothing. This is
response lag, not rendering jank. Main hold uses 300ms; companion hold uses
`combinedClickable`'s platform long-press timeout, so they also feel different.

**Recommendation:** explicit expand/collapse commands for explicit gestures,
and interruptible transitions instead of a blanket time gate. Make hold timing
consistent without reintroducing tap-to-expand or swipe displacement.

### 6. Selection runs several independent motion effects

`ui/IslandOverlayView.kt:379–403` scales the island down to 0.92 for 40ms, then
uses a medium-bouncy return spring. Selection can simultaneously change compact
width/position, run a content crossfade and update companion geometry. Rapid
selection cancels/restarts the scale sequence from its current value.

**Motion-design risk:** this can look like a wobble or hesitation without any
missed frames. Prefer a restrained, coordinated source transition and remove
the extra bounce if it competes with the cutout-safe width morph. Keep collapse
critically damped and keep source-switch animation interruptible.

### 7. Every open mounts content during window enlargement

`ui/OverlayIsland.kt:34` gates expansion on window readiness. This is necessary
to prevent clipping, but adds layout latency before the morph. The expanded
subtree is then newly composed (`ui/IslandOverlayView.kt:955–956`), including pager,
text, media callbacks and artwork drawing. First artwork use can additionally
cost rendering/upload time, depending on its dimensions.

**Recommendation:** distinguish hold detection, window readiness, first content
composition and actual animation frames in the trace. Retain inexpensive cached
layout/source data, not a permanently full-screen input window. Do not remove
the readiness gate to make the animation appear to start sooner.

### 8. Debug installation and refresh-rate hints are not a 120Hz guarantee

The installed APK is the debug build. `app/build.gradle.kts:56–66` enables R8
only for release. ProfileInstaller is present, but there is no app-specific
animation benchmark/profile workflow in the checked source tree. Existing
motion tests assert policy/values, not real frame pacing.

`service/DotIslandOverlayService.kt:474–476` requests a preferred refresh rate
only while the window is expanded (including collapse). Collapsed source swaps
have no such vote. This is a system hint, not proof of the actual refresh rate.

**Recommendation:** benchmark a separately signed, profileable optimized variant
without replacing the user's installed debug app or changing phone settings.
Compare the actual display cadence and frame deadlines; 120Hz nominally allows
about 8.33ms per frame. Consider a transient window vote for compact transitions
only after measuring, rather than permanently requesting high refresh.

## Three concrete animation code references inspected

1. [Android AnimationQuickGuide.kt](https://github.com/android/snippets/blob/master/compose/snippets/src/main/java/com/example/compose/snippets/animations/AnimationQuickGuide.kt)
   — `AnimatedVisibilityCookbook_ModifierAlpha` and `TransitionExampleConcurrent`:
   layer-lambda reads and coordinated transition properties. Applicable to the
   remaining companion composition reads, not just a different easing curve.
2. [Google Animation Codelab — Home.kt](https://github.com/googlecodelabs/android-compose-codelabs/blob/main/AnimationCodelab/finished/src/main/java/com/example/android/codelab/animation/ui/home/Home.kt)
   — `HomeTabIndicator` uses one transition with direction-aware springs;
   `swipeToDismiss` stops ongoing motion and preserves velocity. Useful patterns
   for coordinated, interruptible motion, **not** authorization to restore island
   dragging. These examples are teaching code, not overlay performance benchmarks.
3. [Android AnimationSnippets.kt](https://github.com/android/snippets/blob/master/compose/snippets/src/main/java/com/example/compose/snippets/animations/AnimationSnippets.kt)
   — `AnimatedContentSizeTransform` coordinates target size and staggered fades;
   `Dialer` uses child transitions. Useful for replacing independently timed
   effects with one lifecycle. It does not solve Android overlay window readiness.

Also inspected [Jetsnack SnackDetail.kt](https://github.com/android/compose-samples/blob/main/Jetsnack/app/src/main/java/com/example/jetsnack/ui/snackdetail/SnackDetail.kt):
`sharedBounds` uses stable shared-content keys for bounds/image/title continuity.
Useful design reference, but not a drop-in fix: Dot Island is one overlay surface,
not navigation between screens, and its cutout/window requirements still apply.

Supporting guidance: [Compose performance](https://developer.android.com/develop/ui/compose/performance),
[deferred reads/phases](https://developer.android.com/develop/ui/compose/performance/phases),
[trace-based performance codelab](https://developer.android.com/codelabs/jetpack-compose-performance).

## Verification needed before claiming the lag is fixed

- Record cold and warm music/Live Update expansion and collapse, with real
  refresh cadence and frame deadlines visible in Perfetto/FrameTimeline.
- Repeat companion swaps and rapid expand/outside-collapse/re-expand sequences.
- Include a source update/track skip during expansion and a pager size change.
- Inspect main-thread Binder calls, composition/layout slices, bitmap work,
  RenderThread/GPU deadlines and garbage collection in the same trace.
- Compare before/after missed deadlines and p50/p95/p99 durations per scenario,
  plus input-to-first-motion latency; don't treat a smooth video or passing unit
  tests as frame-time evidence. Keep traces local because notification content
  and unrelated device activity may be private.

First implementation pass should address findings 1, 2, 4 and 5. Then use the
trace to prioritize source-work/cache changes. No single root cause or achieved
frame rate is established by this audit alone.
