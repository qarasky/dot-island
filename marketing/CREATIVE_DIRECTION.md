# Much ado about a dot / revision 02

## The correction

The first campaign used approximate SVG UI and too many independent visual ideas.
It substituted a waveform glyph for real artwork, simple dots for the dot matrix,
invented a music card, exaggerated the ring thickness, and never established a
continuous product story. Attractive styling did not compensate for that mismatch.

Revision 02 uses captured app pixels, not a reimplementation of the interface.
The app is the focal object. Its actual music artwork supplies the only colour.
The page follows one interaction: glance → hold → release → quiet idle.

## Before drawing, look

Compared the repository's music screenshots, the later A069 idle/reveal screenshots,
and these implementation files:

- `IslandCollapsedContent.kt`: squircle source artwork on the left; music dot matrix
  on the right; idle standby dots and monospaced label. No music waveform glyph.
- `expanded/MusicExpanded.kt`: 64dp artwork, 20dp insets; title/artist and dot matrix;
  seek timeline above controls; previous/pause/next plus audio-output action.
- `util/IdleIslandLayout.kt`: approximately 29dp around the actual camera path,
  with 2dp clearance. A thin grey boundary on a black surface, not a thick donut.

See [CAPTURE_PROVENANCE.md](CAPTURE_PROVENANCE.md) for exact sources and limitations.
The music screenshots predate the later idle verification. Do not pretend they are
one freshly captured session or that still-image dissolves prove native motion.

## Visual hierarchy

- Black is the product's environment, not a boxed decoration.
- One dominant headline and one real music panel in the hero.
- Captures stay in the same top-of-screen position through the comparison study.
- Real idle ring at native pixel size supplies the surprising quiet counterpoint.
- One light ending carries the personal-build punchline.
- Remove the technical grid, dimension callouts, spec ticker, numbered feature cards,
  fake pricing/acquisition panel and duplicated sales messages.
- Type remains Barlow. Monospace is reserved for the film, not scattered across the
  page as decoration. Fonts remain local and SIL OFL licensed.
- The primary action is to watch; the secondary action is to inspect the source.
  There is no public release, checkout, tracking or mailing list.

## Film edit

Retain the approved 32-second rhythm, score, type and end card. Replace all product
drawings with image crops. Editorial camera moves and dissolves are allowed;
manufacturing a pixel-inaccurate native morph is not.

| Time | Beat | Actual visual |
| --- | --- | --- |
| 0–4 | Much ado about a dot. | Enlarged idle capture, cropped circularly for the edit. |
| 4–9 | A little room for your music. | Captured compact music island. |
| 9–14 | Your screen is still yours. | Original home-screen capture with the island in context. |
| 14–20 | Hold for a little more. | Equal-scale compact/expanded captures; dissolve in and out. |
| 20–26 | Built for one phone. | Captured idle and tap-reveal states. |
| 26–32 | Dot Island. | Captured dot above the original end-card typography. |

No usable delivery screenshot was available and the A069 was not connected during
this revision. The pizza beat was replaced instead of inventing its interface or
testing on the emulator. Live Updates remain described truthfully in the page copy.

## Further reference study

Reviewed on 2026-10-11, including actual browser screenshots of the current sites.
Current sites are distinguished from their archived award submissions. Awards are
craft references; no conversion claims are inferred.

- [Daylight](https://daylightcomputer.com/) and
  [basement's launch case study](https://basement.studio/showcase/daylight-simplicity-in-motion):
  a single real product scene grounds the promise. Their story proceeds from a
  recognizable object into specific uses. We borrow that grounding, not their
  photography, palette or floating promotion panels.
  [Awwwards SOTD](https://www.awwwards.com/sites/daylight) (2024, 7.39);
  [Webby visual/moving-image nominations](https://winners.webbyawards.com/2025/websites-and-mobile-sites/features-design/best-visual-design-aesthetic/323444/daylight-computer-store).
- [Teenage Engineering OP–1 field](https://teenage.engineering/products/op-1):
  actual product imagery is given room; close-ups explain a real detail instead of
  framing every sentence with a decorative UI device. Our crop must be real too.
- [Apple AirPods Pro](https://www.apple.com/airpods-pro/): consistent object context
  through increasingly specific interaction arguments. Use that continuity, not
  an Apple-style feature carousel or unsupported superiority claims.
- [The Light Phone](https://www.thelightphone.com/): an intentional, restrained
  product worldview. Its product awards
  ([Red Dot](https://www.red-dot.org/project/the-light-phone-39697),
  [iF](https://ifdesign.com/en/winner-ranking/project/the-light-phone/258258))
  are hardware-design awards, not claimed website awards.
- [Opal Tadpole / Awwwards SOTD](https://www.awwwards.com/sites/opal-tadpole):
  small-product confidence and a distinct hero. Retained from the initial research.
- [Google ABCDs](https://support.google.com/google-ads/answer/14783551?hl=en):
  retain the immediate hook, early brand, focused story and specific source CTA.

## Engineering references retained

- [FFrames official skill and design/API/audio guides](https://github.com/dmtrKovalenko/fframes/tree/main/skills/fframes-video):
  deterministic frame-based editing, bundled media, contact sheets and audio QA.
- [Remotion official skills](https://www.remotion.dev/docs/ai/skills): considered;
  FFrames remains suitable for this typography-and-capture film on Metal.
- [Copywriting guidance](https://github.com/coreyhaines31/marketingskills/tree/main/skills/copywriting):
  honest specifics and one message per section. The earlier page had too many
  variations of the same joke; the new ending lands it once.

## Release bar

Inspect contact sheet and detail frames, verify media resolution/duration/audio,
and check actual image loading plus pointer, keyboard and reduced-motion behavior.
Fresh unified phone captures and real footage are the next fidelity improvement;
they require the A069, not a more elaborate SVG mock.
