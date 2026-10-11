# Dot Island — Much ado about a dot

A shared launch campaign: static website + original FFrames/Metal film.
Revision 02 replaces the approximate UI with actual screenshot crops and reduces
the page to one product story. See [CREATIVE_DIRECTION.md](CREATIVE_DIRECTION.md)
for the reference study and [CAPTURE_PROVENANCE.md](CAPTURE_PROVENANCE.md) for
sources, crop coordinates and capture-session limits. Research archives and current
reference-site screenshots are retained in [research/](research/README.md).
No phone settings are involved.

## Website

```sh
python3 -m http.server 4173 --directory marketing/site --bind 127.0.0.1
```

Open http://localhost:4173. No Node dependencies, build step, tracking or remote
font calls. Responsive layouts, reduced-motion support, keyboard-operable captured
state comparison and small print. Hold/release compares real music screenshots;
idle tap reveals a real captured pill and tucks after five seconds. These editorial
crossfades are not native animation recordings. There is no pretend checkout.

### GitHub Pages

Public site: https://qarasky.github.io/dot-island/

`.github/workflows/pages.yml` deploys only `marketing/site` when that directory or
the workflow changes on `main`. It can also be run manually from Actions. Pages
uses the GitHub Actions build source; no `gh-pages` branch or Node build is needed.
Film and poster are checked in, so the Linux workflow does not need Metal or Rust.
Research and phone capture source files outside the site are not deployed.

## Film

Rust, macOS Metal and the FFrames 1.2.0 dependencies. Barlow and IBM Plex Mono
are bundled under their included SIL OFL licenses. The synthesized score is
original; regenerate it as follows:

```sh
python3 marketing/video/generate_score.py
ffmpeg -y -i marketing/video/media/score-raw.wav -af loudnorm=I=-14:TP=-1:LRA=7 -ar 48000 marketing/video/media/score.wav
```

Reproduce the captures, then review and export:

```sh
swift marketing/prepare-captures.swift
```

```sh
cd marketing/video
cargo run --release -- inspect
cargo run --release -- strip -n 16
cargo run --release -- frame 2s,7s,11s,17s,23s,29s
cargo run --release -- preview 0s
cargo run --release -- render -o ../site/assets/launch.mp4
cp frames/2s.png ../site/assets/launch-poster.png
```

The launch film is 32 seconds, 1920 × 1080, 60fps. Its UI pixels come from actual
phone captures, while the transitions are editorial, not a native-animation or
phone frame-time benchmark. The small final web MP4 is retained alongside
source, licensed fonts, original sound and Cargo.lock. Intermediate renders and
QA frames are ignored. The website is ready to serve with its film included.

Before publishing: collect a fresh unified phone capture session, review artwork
rights and listen to the full film,
confirm all source links and personal-build wording. Canonical and Open Graph URLs
target the Pages site. There is no fake conversion data.

## Revision 02 verification

- FFrames inspected 129 sampled frames with no visual findings; contact sheet and
  key frames reviewed. This is not a claim of human review of every frame.
- Export: H.264/AAC, 1920 × 1080, 60fps, 1,920 frames, 32 seconds, about 3.0 MB;
  MP4 header moved to the beginning for progressive web playback.
- Encoded audio: -14.15 LUFS integrated, -1.67 dBTP; deliberate quiet end-card tail.
- Browser checks: all captured images load; idle tap/re-tap/five-second tuck;
  pointer and keyboard music hold/release, state-selection buttons and Escape;
  small print and film playback. No horizontal overflow at 320, 390, 768, 1024 or
  1440px; reduced-motion transitions disabled; no JavaScript exceptions or failed
  HTTP requests in the browser run.
- Desktop and 390px mobile screenshots visually reviewed. Playback and duration
  verified separately in a browser. Full listening review remains yours.
