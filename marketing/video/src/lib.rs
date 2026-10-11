//! Screenshot-led revision. Every product pixel comes from a phone capture.
//! Crossfades and camera moves are editorial, not simulated native transitions.
use fframes::{
    AudioMap,
    AudioTimestamp::*,
    AudioTrack, Color, Duration, FFramesContext, Frame, Svgr, Video,
    animation::{Easing, KeyFramesAnimation},
    include_media_dir,
};

include_media_dir!(pub struct DotIslandFilmMedia, "media");
pub const WIDTH: usize = 1920;
pub const HEIGHT: usize = 1080;
const PAPER: &str = "#eeede8";
const FONT: &str = "Barlow";
const MONO: &str = "IBM Plex Mono";

pub struct DotIslandFilmVideo {
    open: KeyFramesAnimation<f32>,
    reveal: KeyFramesAnimation<f32>,
    macro_size: KeyFramesAnimation<f32>,
}

impl DotIslandFilmVideo {
    pub fn new(_media: &DotIslandFilmMedia, _title: &str) -> Self {
        Self {
            open: fframes::timeline!(
                at 14.8 => 15.35, animate 0.0_f32 => 1.0, Easing::EaseInOut,
                at 18.6 => 19.15, animate 1.0 => 0.0, Easing::EaseInOut,
            ),
            reveal: fframes::timeline!(
                at 23.0 => 23.3, animate 0.0_f32 => 1.0, Easing::EaseInOut,
                at 25.3 => 25.6, animate 1.0 => 0.0, Easing::EaseInOut,
            ),
            macro_size: fframes::timeline!(
                at 0.0 => 4.0, animate 320.0_f32 => 350.0,
                    Easing::CubicBezier(0.65, 0.0, 0.35, 1.0),
            ),
        }
    }
}

fn captured<'a>(
    ctx: &FFramesContext<'a, '_>,
    name: &str,
    x: f32,
    y: f32,
    width: f32,
    height: f32,
) -> Svgr<'a> {
    match ctx.get_image(name) {
        Some(image) => fframes::svgr!(<image href={image.href()} x={x} y={y}
            width={width} height={height} preserveAspectRatio="xMidYMid meet"/>),
        None => Svgr::empty(),
    }
}

impl Video for DotIslandFilmVideo {
    const FPS: usize = 60;
    const WIDTH: usize = WIDTH;
    const HEIGHT: usize = HEIGHT;
    const BACKGROUND_COLOR: Color = Color::BLACK;
    fn duration(&self) -> Duration<'_> {
        Duration::Seconds(32.0)
    }
    fn audio(&self) -> AudioMap<'_> {
        AudioMap::from([AudioTrack::new("score.wav", Second(0.0)..Eof)
            .gain_db(-1.5)
            .fade_out(0.4)])
    }
    fn render_frame<'a>(&'a self, frame: Frame, ctx: &FFramesContext<'a, '_>) -> Svgr<'a> {
        let t = frame.seconds();
        let dark = (4.0..20.0).contains(&t);
        let bg = if dark { "#000000" } else { PAPER };
        let fg = if dark { PAPER } else { "#000000" };
        let muted = if dark { "#a5a5a5" } else { "#62635f" };
        let titles = if t < 4.0 {
            ["Much ado", "about a dot."]
        } else if t < 9.0 {
            ["A little room", "for your music."]
        } else if t < 14.0 {
            ["Your screen", "is still yours."]
        } else if t < 20.0 {
            ["Hold for", "a little more."]
        } else {
            ["Built for", "one phone."]
        };
        let subtitle = if t < 4.0 {
            "A PERSONAL ANDROID EXPERIMENT"
        } else if t < 9.0 {
            "THE GOOD BIT. RIGHT UP TOP."
        } else if t < 14.0 {
            "A GLANCE IS ENOUGH."
        } else if t < 20.0 {
            "RELEASE. BACK TO WHAT YOU WERE DOING."
        } else {
            "AN UNREASONABLE AMOUNT OF CARE."
        };
        let type_scene = if t < 26.0 {
            fframes::svgr!(<g>
                <text x="160" y="450" font-family={FONT} font-size="126" font-weight="600" letter-spacing="-4" fill={fg}>{titles[0]}</text>
                <text x="160" y="592" font-family={FONT} font-size="126" font-weight="600" letter-spacing="-4" fill={fg}>{titles[1]}</text>
                <text x="166" y="705" font-family={MONO} font-size="28" fill={muted}>{subtitle}</text>
            </g>)
        } else {
            fframes::svgr!(<g opacity={frame.animate(fframes::timeline!(at 26.0 => 26.5, animate 0.0_f32 => 1.0, Easing::EaseOut))}>
                <text x="960" y="605" text-anchor="middle" font-family={FONT} font-size="178" font-weight="600" letter-spacing="-6" fill={fg}>"dot island"</text>
                <text x="960" y="699" text-anchor="middle" font-family={FONT} font-size="48" font-weight="500" fill={muted}>"Much ado about a dot."</text>
                <text x="960" y="875" text-anchor="middle" font-family={MONO} font-size="30" fill={fg}>"INSPECT THE SOURCE  /  GITHUB.COM/QARASKY/DOT-ISLAND"</text>
            </g>)
        };
        let product = if t < 4.0 {
            let size = frame.animate(&self.macro_size);
            let image = captured(
                ctx,
                "idle-ring.png",
                1400.0 - size / 2.0,
                520.0 - size / 2.0,
                size,
                size,
            );
            fframes::svgr!(<g><defs><clipPath id="macro-crop"><circle cx="1400" cy="520" r={size/2.0}/></clipPath></defs>
                <g clip-path="url(#macro-crop)">{image}</g></g>)
        } else if t < 9.0 {
            captured(ctx, "music-compact.png", 1149.0, 442.0, 502.4, 156.8)
        } else if t < 14.0 {
            captured(ctx, "music-context.png", 1000.0, 360.0, 760.0, 327.84)
        } else if t < 20.0 {
            let open = frame.animate(&self.open);
            // Both images retain their real top edge and use the same display scale.
            // We crossfade states instead of manufacturing a fake morph.
            let compact = captured(ctx, "music-compact.png", 1302.55, 420.0, 194.9, 60.83);
            let expanded = captured(ctx, "music-expanded.png", 1040.0, 420.0, 720.0, 309.1);
            fframes::svgr!(<g><g opacity={1.0-open}>{compact}</g><g opacity={open}>{expanded}</g></g>)
        } else if t < 26.0 {
            let reveal = frame.animate(&self.reveal);
            let rest = captured(ctx, "idle-ring.png", 1359.0, 479.0, 82.0, 84.0);
            let hello = captured(ctx, "idle-reveal.png", 1307.5, 475.0, 185.0, 90.0);
            fframes::svgr!(<g><defs>
                <clipPath id="idle-crop"><circle cx="1400" cy="520" r="41"/></clipPath>
                <clipPath id="reveal-crop"><rect x="1307.5" y="475" width="185" height="90" rx="45"/></clipPath>
            </defs><g opacity={1.0-reveal} clip-path="url(#idle-crop)">{rest}</g>
                <g opacity={reveal} clip-path="url(#reveal-crop)">{hello}</g>
                <text x="1400" y="650" text-anchor="middle" font-family={MONO} font-size="28" fill={muted}>"A069 / ACTUAL IDLE CAPTURE"</text>
            </g>)
        } else {
            let mark = captured(ctx, "idle-ring.png", 900.0, 290.0, 120.0, 120.0);
            fframes::svgr!(<g><defs><clipPath id="end-crop"><circle cx="960" cy="350" r="60"/></clipPath></defs>
                <g clip-path="url(#end-crop)">{mark}</g></g>)
        };
        fframes::svgr!(<svg xmlns="http://www.w3.org/2000/svg" width={WIDTH} height={HEIGHT} viewBox="0 0 1920 1080">
            <rect width="1920" height="1080" fill={bg}/>
            <text x="160" y="128" font-family={MONO} font-size="30" fill={fg}>"DOT ISLAND / 002"</text>
            {type_scene}{product}
            <text x="160" y="968" font-family={MONO} font-size="28" fill={muted}>"REAL APP CAPTURES. EDITORIAL TRANSITIONS."</text>
        </svg>)
    }
}
