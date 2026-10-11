"""Original, deterministic tactile/electronic cue; no third-party audio.

Generate the raw mono WAV, then loudness-normalize with the README command.
Pulse positions deliberately match the film's edit and hold/release cues.
"""
import math
import random
import struct
import wave
from pathlib import Path

RATE = 48000
DURATION = 32
samples = [0.0] * (RATE * DURATION)
rng = random.Random(29)


def tone(start, length, frequency, gain, decay=4.0):
    begin = round(start * RATE)
    for i in range(round(length * RATE)):
        t = i / RATE
        envelope = min(t / 0.008, 1) * math.exp(-decay * t / length)
        sound = math.sin(2 * math.pi * frequency * t)
        sound += 0.18 * math.sin(2 * math.pi * frequency * 2 * t)
        if begin + i < len(samples):
            samples[begin + i] += sound * envelope * gain


def click(start, gain):
    begin = round(start * RATE)
    for i in range(round(0.035 * RATE)):
        envelope = math.exp(-i / (RATE * 0.005))
        samples[begin + i] += rng.uniform(-1, 1) * envelope * gain


for half_beat in range(59):
    t = half_beat * 0.5
    if t < 4 and half_beat % 2 or 20 <= t < 26 and half_beat % 2:
        continue
    click(t, 0.13 if half_beat % 4 == 0 else 0.045)
    if half_beat % 2 == 0:
        tone(t, 0.38, [73.416, 73.416, 82.407, 65.406][(half_beat // 8) % 4], 0.27)

for t in [0, 4.15, 9, 14.8, 18.6, 20, 26]:
    click(t, 0.24)
    tone(t, 0.65, 146.832, 0.19)
    tone(t + 0.035, 0.45, 293.665, 0.055)

for t in [5, 7, 10, 12, 16, 18]:
    tone(t, 0.75, 440 if t % 2 else 391.995, 0.035)

tone(27, 2.5, 146.832, 0.2, decay=5)
tone(27.5, 3, 220, 0.16, decay=5)
tone(27.5, 3, 293.665, 0.04, decay=5)
peak = max(abs(v) for v in samples)
output = Path(__file__).parent / 'media' / 'score-raw.wav'
with wave.open(str(output), 'wb') as wav:
    wav.setnchannels(1)
    wav.setsampwidth(2)
    wav.setframerate(RATE)
    wav.writeframes(b''.join(struct.pack('<h', round(v / peak * 25000)) for v in samples))
print(output)
