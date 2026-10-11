# Dot Island — Much ado about a dot

A shared launch campaign: static website + original FFrames/Metal film.
See [CREATIVE_DIRECTION.md](CREATIVE_DIRECTION.md) for the brief, storyboard and
online references. Nothing is deployed and no phone settings are involved.

## Website

```sh
python3 -m http.server 4173 --directory marketing/site --bind 127.0.0.1
```

Open http://localhost:4173. No Node dependencies, build step, tracking or remote
font calls. Responsive layouts, reduced-motion support, keyboard-operable demo,
native dialog and FAQs. “Acquire a dot” explains the joke and links
to the source; it does not take payment or pretend to offer a public release.

## Film

Rust, macOS Metal and the FFrames 1.2.0 dependencies. Barlow and IBM Plex Mono
are bundled under their included SIL OFL licenses. The synthesized score is
original; regenerate it as follows:

```sh
python3 marketing/video/generate_score.py
ffmpeg -y -i marketing/video/media/score-raw.wav -af loudnorm=I=-14:TP=-1:LRA=7 -ar 48000 marketing/video/media/score.wav
```

Review and export:

```sh
cd marketing/video
cargo run --release -- inspect
cargo run --release -- strip -n 16
cargo run --release -- frame 2s,7s,11s,17s,23s,29s
cargo run --release -- preview 0s
cargo run --release -- render -o ../site/assets/launch.mp4
```

The launch film is 32 seconds, 1920 × 1080, 60fps. Its animation is illustrative,
not a phone frame-time benchmark. The small final web MP4 is retained alongside
source, licensed fonts, original sound and Cargo.lock. Intermediate renders and
QA frames are ignored. The website is ready to serve with its film included.

Before publishing: review/listen to the full film,
confirm all source links and personal-build wording, and supply your real hosting
URL before adding canonical/OG URL metadata. There is no fake conversion data.

## First-cut verification

- FFrames inspected 129 sampled frames with no visual findings; contact sheet and
  key frames reviewed. This is not a claim of human review of every frame.
- Export: H.264/AAC, 1920 × 1080, 60fps, 1,920 frames, 32 seconds, about 3.2 MB;
  MP4 header moved to the beginning for progressive web playback.
- Encoded audio: -14.15 LUFS integrated, -1.67 dBTP; deliberate quiet end-card tail.
- Browser checks: idle reveal/tuck, music hold/release, keyboard preview/Escape,
  acquisition dialog and FAQ. No horizontal overflow at 320, 390, 768, 1024 or
  1440px; reduced-motion transitions disabled; no JavaScript exceptions.
- Desktop and 390px mobile screenshots visually reviewed. Playback and duration
  verified separately in a browser. Full listening review remains yours.
