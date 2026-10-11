//! A deliberately over-serious launch for a very small personal Android app.
use fframes::{
    AudioMap,
    AudioTimestamp::*,
    AudioTrack, Color, Duration, FFramesContext, Frame, Svgr, Video,
    animation::{Easing, KeyFramesAnimation},
    include_media_dir,
};

// include_media_dir reads this directory only, not nested font folders.
include_media_dir!(pub struct DotIslandFilmMedia, "media");
pub const WIDTH: usize = 1920;
pub const HEIGHT: usize = 1080;
const PAPER: &str = "#eeede8";
const BLACK: &str = "#0b0c0b";
const FONT: &str = "Barlow";
const MONO: &str = "IBM Plex Mono";

pub struct DotIslandFilmVideo {
    width: KeyFramesAnimation<f32>,
    height: KeyFramesAnimation<f32>,
    radius: KeyFramesAnimation<f32>,
    camera: KeyFramesAnimation<f32>,
    position: KeyFramesAnimation<f32>,
    center: KeyFramesAnimation<f32>,
    content: KeyFramesAnimation<f32>,
    expanded: KeyFramesAnimation<f32>,
}

impl DotIslandFilmVideo {
    pub fn new(_media: &DotIslandFilmMedia, _title: &str) -> Self {
        let morph = Easing::CubicBezier(0.65, 0.0, 0.35, 1.0);
        let settle = Easing::CubicBezier(0.16, 1.0, 0.3, 1.0);
        Self {
            width: fframes::timeline!(
                at 0.0 => 2.0, animate 480.0_f32 => 500.0, settle,
                at 4.15 => 4.95, animate 500.0 => 650.0, morph,
                at 20.0 => 21.0, animate 650.0 => 60.0, morph,
                at 26.0 => 26.8, animate 60.0 => 120.0, morph,
            ),
            height: fframes::timeline!(
                at 0.0 => 2.0, animate 480.0_f32 => 500.0, settle,
                at 4.15 => 4.95, animate 500.0 => 110.0, morph,
                at 14.8 => 15.45, animate 110.0 => 390.0, settle,
                at 18.6 => 19.15, animate 390.0 => 110.0, morph,
                at 20.0 => 21.0, animate 110.0 => 60.0, morph,
                at 26.0 => 26.8, animate 60.0 => 120.0, morph,
            ),
            camera: fframes::timeline!(
                at 0.0 => 2.0, animate 180.0_f32 => 188.0, settle,
                at 4.15 => 4.95, animate 188.0 => 39.0, morph,
                at 20.0 => 21.0, animate 39.0 => 22.0, morph,
                at 26.0 => 26.8, animate 22.0 => 44.0, morph,
            ),
            radius: fframes::timeline!(
                at 0.0 => 2.0, animate 240.0_f32 => 250.0, settle,
                at 4.15 => 4.95, animate 250.0 => 55.0, morph,
                at 14.8 => 15.45, animate 55.0 => 65.0, settle,
                at 18.6 => 19.15, animate 65.0 => 55.0, morph,
                at 20.0 => 21.0, animate 55.0 => 30.0, morph,
                at 26.0 => 26.8, animate 30.0 => 60.0, morph,
            ),
            position: fframes::timeline!(
                at 14.8 => 15.45, animate 520.0_f32 => 660.0, settle,
                at 18.6 => 19.15, animate 660.0 => 520.0, morph,
                at 26.0 => 26.8, animate 520.0 => 350.0, morph,
            ),
            center: fframes::timeline!(at 26.0 => 26.8, animate 1340.0_f32 => 960.0, morph),
            content: fframes::timeline!(
                at 4.85 => 5.15, animate 0.0_f32 => 1.0, Easing::EaseOut,
                at 14.8 => 15.0, animate 1.0 => 0.0, Easing::EaseIn,
                at 18.9 => 19.2, animate 0.0 => 1.0, Easing::EaseOut,
                at 20.0 => 20.2, animate 1.0 => 0.0, Easing::EaseIn,
            ),
            expanded: fframes::timeline!(
                at 15.25 => 15.55, animate 0.0_f32 => 1.0, Easing::EaseOut,
                at 18.6 => 18.8, animate 1.0 => 0.0, Easing::EaseIn,
            ),
        }
    }
}

impl Video for DotIslandFilmVideo {
    const FPS: usize = 60;
    const WIDTH: usize = WIDTH;
    const HEIGHT: usize = HEIGHT;
    const BACKGROUND_COLOR: Color = Color::WHITE;
    fn duration(&self) -> Duration<'_> {
        Duration::Seconds(32.0)
    }
    fn audio(&self) -> AudioMap<'_> {
        AudioMap::from([AudioTrack::new("score.wav", Second(0.0)..Eof)
            .gain_db(-1.5)
            .fade_out(0.4)])
    }
    fn render_frame<'a>(&'a self, frame: Frame, _ctx: &FFramesContext<'a, '_>) -> Svgr<'a> {
        let t = frame.seconds();
        let dark = (4.0..20.0).contains(&t);
        let bg = if dark { BLACK } else { PAPER };
        let fg = if dark { PAPER } else { BLACK };
        let muted = if dark { "#b2b7a9" } else { "#62635f" };
        let line = if dark { "#33372e" } else { "#c8cbc0" };
        let w = frame.animate(&self.width);
        let h = frame.animate(&self.height);
        let cx = frame.animate(&self.center);
        let cy = frame.animate(&self.position);
        let cr = frame.animate(&self.camera);
        let content_alpha = frame.animate(&self.content);
        let expanded_alpha = frame.animate(&self.expanded);
        let radius = frame.animate(&self.radius);
        let title = if t < 4.0 {
            ["Much ado", "about a dot."]
        } else if t < 9.0 {
            ["A little room", "for your music."]
        } else if t < 14.0 {
            ["And your very", "important pizza."]
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
            "DELIVERY UPDATES FROM THE SOURCE."
        } else if t < 20.0 {
            "RELEASE. BACK TO WHAT YOU WERE DOING."
        } else {
            "AN UNREASONABLE AMOUNT OF CARE."
        };
        let delivery = (9.0..14.0).contains(&t);
        let title_group = if t < 26.0 {
            fframes::svgr!(<g>
                <text x="160" y="450" font-family={FONT} font-size="126" font-weight="600" letter-spacing="-4" fill={fg}>{title[0]}</text>
                <text x="160" y="592" font-family={FONT} font-size="126" font-weight="600" letter-spacing="-4" fill={fg}>{title[1]}</text>
                <text x="166" y="705" font-family={MONO} font-size="28" fill={muted}>{subtitle}</text>
            </g>)
        } else {
            fframes::svgr!(<g opacity={frame.animate(fframes::timeline!(at 26.5 => 27.0, animate 0.0_f32 => 1.0, Easing::EaseOut))}>
                <text x="960" y="605" text-anchor="middle" font-family={FONT} font-size="178" font-weight="600" letter-spacing="-6" fill={fg}>"dot island"</text>
                <text x="960" y="699" text-anchor="middle" font-family={FONT} font-size="48" font-weight="500" fill={muted}>"Much ado about a dot."</text>
                <text x="960" y="875" text-anchor="middle" font-family={MONO} font-size="30" fill={fg}>"INSPECT THE SOURCE  /  GITHUB.COM/QARASKY/DOT-ISLAND"</text>
            </g>)
        };
        let glyph = if delivery {
            fframes::svgr!(<g transform="translate(1088 486)">
                <path d="M 0 0 L 46 0 L 23 64 Z" fill="none" stroke={PAPER} stroke-width="5" stroke-linejoin="round" />
                <circle cx="20" cy="19" r="4" fill={PAPER}/><circle cx="28" cy="35" r="4" fill={PAPER}/>
            </g>)
        } else {
            fframes::svgr!(<g fill={PAPER}>
                <rect x="1095" y="500" width="8" height="40" rx="4"/>
                <rect x="1115" y="483" width="8" height="74" rx="4"/>
                <rect x="1135" y="495" width="8" height="50" rx="4"/>
                <rect x="1155" y="510" width="8" height="20" rx="4"/>
            </g>)
        };
        let dots: Vec<Svgr> = (0..4).map(|i| fframes::svgr!(
            <circle cx={1480 + i * 29} cy="520" r="6" fill={if i < 3 { PAPER } else { "#53594b" }}/>
        )).collect();
        let measurement = if (21.0..26.0).contains(&t) {
            fframes::svgr!(<g>
                <line x1="1310" x2="1370" y1="602" y2="602" stroke={muted}/>
                <line x1="1310" x2="1310" y1="591" y2="613" stroke={muted}/>
                <line x1="1370" x2="1370" y1="591" y2="613" stroke={muted}/>
                <text x="1340" y="665" text-anchor="middle" font-family={MONO} font-size="30" fill={muted}>"~29 dp"</text>
                <text x="1340" y="750" text-anchor="middle" font-family={MONO} font-size="28" fill={muted}>"A069 / PERSONAL BUILD"</text>
            </g>)
        } else {
            Svgr::empty()
        };
        let expanded = fframes::svgr!(<g opacity={expanded_alpha}>
            <rect x="1072" y="590" width="128" height="128" rx="32" fill="#d1d4c9"/>
            <circle cx="1136" cy="654" r="44" fill="#151712" stroke="#737967" stroke-width="5"/>
            <circle cx="1136" cy="654" r="23" fill="none" stroke="#737967" stroke-width="2"/>
            <circle cx="1136" cy="654" r="6" fill="#d1d4c9"/>
            <text x="1230" y="626" font-family={FONT} font-size="36" font-weight="600" fill={PAPER}>"A little headspace"</text>
            <text x="1230" y="675" font-family={FONT} font-size="28" fill="#b2b7a9">"Dot Island / Demo track"</text>
            <path d="M 1240 732 L 1220 746 L 1240 760 Z M 1220 732 L 1200 746 L 1220 760 Z" fill={PAPER}/>
            <rect x="1330" y="729" width="7" height="34" fill={PAPER}/><rect x="1347" y="729" width="7" height="34" fill={PAPER}/>
            <path d="M 1450 732 L 1470 746 L 1450 760 Z M 1470 732 L 1490 746 L 1470 760 Z" fill={PAPER}/>
            <rect x="1072" y="794" width="536" height="3" fill="#4f5547"/><rect x="1072" y="794" width="205" height="3" fill={PAPER}/>
            <text x="1072" y="835" font-family={MONO} font-size="28" fill="#b2b7a9">"01:24"</text>
            <text x="1608" y="835" text-anchor="end" font-family={MONO} font-size="28" fill="#b2b7a9">"03:42"</text>
        </g>);
        let gesture = if (14.3..15.3).contains(&t) {
            fframes::svgr!(<g opacity={frame.animate(fframes::timeline!(
                at 14.3 => 14.4, animate 0.0_f32 => 1.0, Easing::EaseOut,
                at 14.9 => 15.3, animate 1.0 => 0.0, Easing::EaseIn,
            ))}>
                <circle cx="1450" cy="545" r="22" fill="none" stroke={PAPER} stroke-width="2"/>
                <circle cx="1450" cy="545" r="9" fill={PAPER}/>
            </g>)
        } else {
            Svgr::empty()
        };
        fframes::svgr!(<svg xmlns="http://www.w3.org/2000/svg" width={WIDTH} height={HEIGHT} viewBox="0 0 1920 1080">
            <rect width="1920" height="1080" fill={bg}/>
            <line x1="160" y1="166" x2="1760" y2="166" stroke={line}/>
            <text x="160" y="128" font-family={MONO} font-size="30" fill={fg}>"DOT ISLAND / 002"</text>
            <text x="1760" y="128" text-anchor="end" font-family={MONO} font-size="28" fill={muted}>"A VERY SMALL LAUNCH"</text>
            {title_group}
            <rect x={cx - w / 2.0} y={cy - h / 2.0} width={w} height={h} rx={radius} fill="#000000" stroke={if dark { "#444a3b" } else { "#111111" }} stroke-width="2"/>
            <circle cx={cx} cy={if t >= 26.0 { cy } else { 520.0 }} r={cr} fill={bg} stroke={line} stroke-width="1"/>
            <g opacity={content_alpha}>{glyph}{dots}</g>
            {expanded}
            {gesture}
            {measurement}
            <text x="160" y="968" font-family={MONO} font-size="28" fill={muted}>"PERSONAL PROJECT. PRETEND PRODUCT LAUNCH."</text>
            <text x="1760" y="968" text-anchor="end" font-family={MONO} font-size="28" fill={muted}>"ILLUSTRATIVE UI"</text>
        </svg>)
    }
}
