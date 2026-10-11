// The product pixels come exclusively from captures. Only editorial crossfades
// are animated here; this does not pretend to reproduce native overlay motion.
const study = document.querySelector('.capture-study');
const hold = document.querySelector('#hold-capture');
const caption = document.querySelector('.study-caption');
const compactImage = document.querySelector('.compact-shot');
const expandedImage = document.querySelector('.expanded-shot');
let pressTimer;
let pressActive = false;
let held = false;
function setView(state) {
  study.dataset.state = state;
  const expanded = state === 'expanded';
  hold.setAttribute('aria-expanded', String(expanded));
  compactImage.setAttribute('aria-hidden', String(expanded));
  expandedImage.setAttribute('aria-hidden', String(!expanded));
  document.querySelectorAll('[data-view]').forEach(button => button.setAttribute('aria-pressed', String(button.dataset.view === state)));
  caption.textContent = expanded ? 'Artwork, progress, playback and audio output. Exactly as captured.' : 'Album artwork and the dot matrix. Your song, at a glance.';
}
function release() {
  clearTimeout(pressTimer);
  pressActive = false;
  if (held) setView('compact');
  held = false;
}
document.querySelectorAll('[data-view]').forEach(button => button.addEventListener('click', () => { release(); setView(button.dataset.view); }));
hold.addEventListener('pointerdown', event => {
  if (event.button !== 0) return;
  release();
  pressActive = true;
  hold.setPointerCapture(event.pointerId);
  pressTimer = setTimeout(() => {
    if (pressActive) { held = true; setView('expanded'); }
  }, 300);
});
['pointerup','pointercancel','lostpointercapture'].forEach(type => hold.addEventListener(type, release));
hold.addEventListener('keydown', event => {
  if (event.code === 'Space' || event.code === 'Enter') {
    event.preventDefault();
    if (!event.repeat) { held = true; setView('expanded'); }
  }
});
hold.addEventListener('keyup', event => {
  if (event.code === 'Space' || event.code === 'Enter') { event.preventDefault(); release(); }
});
hold.addEventListener('blur', release);
const idle = document.querySelector('#idle-capture');
let idleTimer;
function setIdle(revealed) {
  clearTimeout(idleTimer);
  idle.setAttribute('aria-expanded', String(revealed));
  idle.setAttribute('aria-label', revealed ? 'Return to the captured idle ring' : 'Reveal the captured idle pill');
  idle.querySelector('.rest-image').setAttribute('aria-hidden', String(revealed));
  idle.querySelector('.reveal-image').setAttribute('aria-hidden', String(!revealed));
  document.querySelector('.idle-hint').textContent = revealed ? 'Hello. Tucking away in five seconds.' : 'Tap to say hello.';
  if (revealed) idleTimer = setTimeout(() => setIdle(false), 5000);
}
idle.addEventListener('click', () => setIdle(idle.getAttribute('aria-expanded') !== 'true'));
document.addEventListener('keydown', event => { if (event.key === 'Escape') { release(); setView('compact'); setIdle(false); } });
document.addEventListener('visibilitychange', () => { if (document.hidden) { release(); setIdle(false); } });
const film = document.querySelector('#launch-film');
document.querySelector('#play-film').addEventListener('click', async () => {
  try {
    await film.play();
    film.scrollIntoView({block:'center',behavior:matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'});
  } catch { document.querySelector('.film-error').hidden = false; }
});
film.addEventListener('error', () => { document.querySelector('.film-error').hidden = false; });
