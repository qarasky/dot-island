# Real product imagery

All website and film product imagery is cropped from existing phone screenshots.
No icons, controls, labels, borders, track timings or artwork are redrawn. The
original artwork colours are preserved; no monochrome filter or tint is applied.

| Asset | Original | Crop `(x, y, width, height)` |
| --- | --- | --- |
| music-compact.png | assets/screenshots/dot_music_collapsed.png | 454, 31, 314, 98 |
| music-expanded.png | assets/screenshots/dot_music_expanded.png | 32, 32, 1160, 498 |
| music-context.png | assets/screenshots/dot_music_collapsed.png | 0, 0, 1224, 528 |
| music-open-context.png | assets/screenshots/dot_music_expanded.png | 0, 0, 1224, 540 |
| idle-ring.png | assets/screenshots/dot_idle_ring_a069.png | 571, 40, 82, 84 |
| idle-reveal.png | assets/screenshots/dot_idle_reveal_a069.png | 520, 37, 185, 90 |

The last two source strips are the top 240px of `connecting-status.png` and
`idle-phone-reveal.png` from the earlier on-phone verification. The first shows the
camera-fitting ring after connection recovery; the second shows its real tap reveal.
Only the status-bar strip is retained, not the surrounding settings screen.

`swift marketing/prepare-captures.swift` reproduces the lossless crops into both
the site and FFrames media folders. Scaling and crossfading happen at presentation
time. Circular/rounded crop masks in light film scenes remove the surrounding black
rectangle; they do not draw an interface. The page uses the unmasked black captures.

## Limits

- Music captures are from the repository's earlier app state, not a fresh session.
  The later cutout-anchoring changes may affect current layout. The page explicitly
  identifies mixed capture sessions; a public-release shoot should replace them
  with a fresh unified set. Do not call this a pixel-exact recording of today's build.
- There is no delivery screenshot in the campaign and no invented delivery ETA.
- Still images show the real endpoints. Website/film dissolves are editorial and
  are not recordings or measurements of native animation.
- The song title, artist, artwork and timings are part of the captured media UI.
  No audio from the song is used; the score remains original synthetic audio.
  Review artwork rights before any paid/public advertising.
- Phone screenshots cannot include the physical camera hardware as photographed
  glass. The black center is the recorded screen region, not a rendered lens.
