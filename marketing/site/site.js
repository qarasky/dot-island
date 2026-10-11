const island = document.querySelector('#demo-island');
const hint = document.querySelector('#demo-hint');
const expandButton = document.querySelector('#expand-demo');
const dialog = document.querySelector('#acquire-dialog');
let mode = 'idle';
let revealTimer;
let holdTimer;
let keyboardTimer;
let held = false;
let pressStarted = false;
let suppressClick = false;
const hints = {
  idle: 'Tap the ring. It says hello, then tucks away.',
  music: 'Hold the island to see the controls. Release to tuck them away.',
  delivery: 'A source-approved update, right up top. Sample status: on the way.'
};
function expand(value) {
  island.classList.toggle('expanded', value);
  island.setAttribute('aria-expanded', String(value));
}
function reveal() {
  clearTimeout(revealTimer);
  const value = !island.classList.contains('revealed');
  island.classList.toggle('revealed', value);
  island.setAttribute('aria-expanded', String(value));
  if (value) revealTimer = setTimeout(() => {
    island.classList.remove('revealed');
    island.setAttribute('aria-expanded', 'false');
  }, 5000);
}
function finishPress() {
  clearTimeout(holdTimer);
  pressStarted = false;
  if (held) { suppressClick = true; expand(false); }
  held = false;
}
document.querySelectorAll('.mode-option').forEach(button => {
  button.addEventListener('click', () => {
    finishPress();
    clearTimeout(revealTimer);
    clearTimeout(keyboardTimer);
    suppressClick = false;
    mode = button.dataset.mode;
    island.dataset.mode = mode;
    island.classList.remove('revealed');
    expand(false);
    document.querySelectorAll('.mode-option').forEach(item => item.setAttribute('aria-pressed', String(item === button)));
    island.querySelector('.compact-icon').textContent = mode === 'delivery' ? '◔' : '♫';
    document.querySelector('.demo-state').textContent = {idle:'AT REST',music:'NOW PLAYING',delivery:'ON THE WAY'}[mode];
    island.setAttribute('aria-label', mode === 'idle' ? 'Tap to reveal the idle pill' : mode === 'music' ? 'Hold to expand music controls' : 'Illustrative delivery update: on the way');
    hint.textContent = hints[mode];
    expandButton.hidden = mode !== 'music';
  });
});
island.addEventListener('pointerdown', event => {
  if (event.button !== 0 || mode !== 'music') return;
  clearTimeout(keyboardTimer);
  suppressClick = false;
  pressStarted = true;
  island.setPointerCapture(event.pointerId);
  holdTimer = setTimeout(() => {
    if (!pressStarted) return;
    held = true;
    expand(true);
  }, 300);
});
island.addEventListener('pointerup', finishPress);
island.addEventListener('pointercancel', finishPress);
island.addEventListener('lostpointercapture', finishPress);
island.addEventListener('click', () => {
  if (suppressClick) { suppressClick = false; return; }
  if (mode === 'idle') reveal();
  else hint.textContent = mode === 'music' ? 'In the app, a tap opens your music source. Here, try a hold.' : 'Delivery status belongs to the source. This is an illustrative update.';
});
// A separate keyboard-friendly preview avoids requiring a timed pointer gesture.
expandButton.addEventListener('click', () => {
  clearTimeout(keyboardTimer);
  const value = !island.classList.contains('expanded');
  expand(value);
  if (value) keyboardTimer = setTimeout(() => expand(false), 4000);
});
document.addEventListener('keydown', event => {
  if (event.key === 'Escape') { finishPress(); expand(false); }
});
document.addEventListener('visibilitychange', () => {
  if (document.hidden) { finishPress(); expand(false); }
});
document.querySelectorAll('[data-acquire]').forEach(button => button.addEventListener('click', () => dialog.showModal()));
dialog.querySelector('.dialog-close').addEventListener('click', () => dialog.close());
dialog.addEventListener('click', event => {
  const bounds = dialog.getBoundingClientRect();
  if (event.target === dialog && (event.clientX < bounds.left || event.clientX > bounds.right || event.clientY < bounds.top || event.clientY > bounds.bottom)) dialog.close();
});
const film = document.querySelector('#launch-film');
document.querySelector('#play-film').addEventListener('click', async () => {
  try {
    await film.play();
    film.scrollIntoView({block:'center',behavior:matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth'});
  } catch {
    document.querySelector('.film-error').hidden = false;
  }
});
film.addEventListener('error', () => { document.querySelector('.film-error').hidden = false; });
