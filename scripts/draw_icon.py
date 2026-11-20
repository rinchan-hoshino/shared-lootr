#!/usr/bin/env python3
"""Generate the 64x64 pixel-art badge icon for Shared Lootr (upscaled 8x to 512x512)."""

from pathlib import Path
from PIL import Image

SIZE = 64
SCALE = 8

# Palette (crisp high-contrast pixel art)
TRANSPARENT = (0, 0, 0, 0)

# Rounded slate/obsidian badge frame
RIM_OUTLINE = (16, 18, 28, 255)
RIM_DARK = (34, 40, 58, 255)
RIM_MID = (54, 64, 88, 255)
RIM_LIGHT = (86, 100, 132, 255)
GOLD_TRIM = (224, 178, 66, 255)
GOLD_TRIM_DARK = (158, 114, 34, 255)

BG_TOP = (24, 30, 52, 255)
BG_MID = (30, 38, 64, 255)
BG_BOT = (19, 24, 42, 255)

# Cyan-gold treasure glow
GLOW_OUTER = (44, 168, 186, 255)
GLOW_MID = (92, 232, 224, 255)
GLOW_CORE = (212, 255, 244, 255)
GLOW_GOLD = (255, 236, 132, 255)
SPARKLE = (255, 252, 214, 255)

# Chest wood & gold bands
OUTLINE = (24, 16, 14, 255)
WOOD_DARK = (98, 52, 26, 255)
WOOD_MID = (138, 76, 36, 255)
WOOD_LIGHT = (172, 100, 48, 255)
WOOD_HI = (196, 122, 62, 255)

GOLD_SHADOW = (146, 94, 22, 255)
GOLD_DARK = (188, 132, 32, 255)
GOLD_MID = (234, 182, 54, 255)
GOLD_BRIGHT = (255, 226, 108, 255)
GOLD_SPEC = (255, 249, 198, 255)

# Interlocking sharing rings (cyan-teal + warm gold)
RING_OUT = (18, 20, 30, 255)
CYAN_DARK = (26, 128, 152, 255)
CYAN_MID = (58, 196, 214, 255)
CYAN_BRIGHT = (146, 242, 248, 255)

SHADOW_FLOOR = (14, 17, 30, 255)


def put(px, x, y, color):
    if 0 <= x < SIZE and 0 <= y < SIZE:
        px[x, y] = color


def rect(px, x0, y0, x1, y1, color):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            put(px, x, y, color)


def in_rounded_rect(x, y, x0, y0, x1, y1, r):
    if x < x0 or x > x1 or y < y0 or y > y1:
        return False
    cx = min(max(x, x0 + r), x1 - r)
    cy = min(max(y, y0 + r), y1 - r)
    dx = x - cx
    dy = y - cy
    return dx * dx + dy * dy <= r * r + 1


def draw_badge_frame(px):
    for y in range(SIZE):
        for x in range(SIZE):
            if not in_rounded_rect(x, y, 2, 2, 61, 61, 9):
                continue
            if not in_rounded_rect(x, y, 3, 3, 60, 60, 8):
                put(px, x, y, RIM_OUTLINE)
                continue
            if not in_rounded_rect(x, y, 6, 6, 57, 57, 6):
                # Clean solid beveled stone rim
                if y == 4 or x == 4:
                    put(px, x, y, RIM_LIGHT)
                elif y >= 58 or x >= 58:
                    put(px, x, y, RIM_DARK)
                else:
                    put(px, x, y, RIM_MID)
                continue
            if not in_rounded_rect(x, y, 7, 7, 56, 56, 5):
                # Inner gold trim border
                put(px, x, y, GOLD_TRIM if (x + y) < 64 else GOLD_TRIM_DARK)
                continue
            # Deep radial-ish slate interior
            dist2 = (x - 31.5) ** 2 + (y - 26.0) ** 2
            if dist2 < 240:
                put(px, x, y, BG_MID)
            elif y > 46:
                put(px, x, y, BG_BOT)
            else:
                put(px, x, y, BG_TOP)

    # Corner stone rivets
    for rx, ry in [(8, 8), (55, 8), (8, 55), (55, 55)]:
        put(px, rx, ry, GOLD_BRIGHT)
        put(px, rx + (1 if rx < 32 else -1), ry, GOLD_TRIM_DARK)
        put(px, rx, ry + (1 if ry < 32 else -1), GOLD_TRIM_DARK)


def draw_treasure_glow(px):
    # Soft radiant cyan-gold aura rising behind the slightly open lid
    for y in range(10, 20):
        for x in range(14, 50):
            if not in_rounded_rect(x, y, 8, 8, 55, 55, 5):
                continue
            dx = (x - 31.5) / 14.0
            dy = (y - 21.0) / 11.0
            d = dx * dx + dy * dy
            if d < 0.36:
                put(px, x, y, GLOW_MID)
            elif d < 0.72:
                put(px, x, y, GLOW_OUTER)

    # Clean upward light rays
    for step in range(1, 7):
        put(px, 31, 17 - step, GLOW_CORE if step <= 3 else GLOW_MID)
        put(px, 32, 17 - step, GLOW_CORE if step <= 3 else GLOW_MID)
        put(px, 26 - step, 17 - step, GLOW_GOLD if step <= 2 else GLOW_MID)
        put(px, 37 + step, 17 - step, GLOW_GOLD if step <= 2 else GLOW_MID)

    # Crisp 4-point pixel sparkles
    for sx, sy in [(18, 13), (45, 13), (25, 10), (38, 10)]:
        put(px, sx, sy, SPARKLE)
        put(px, sx - 1, sy, GLOW_MID)
        put(px, sx + 1, sy, GLOW_MID)
        put(px, sx, sy - 1, GLOW_MID)
        put(px, sx, sy + 1, GLOW_MID)


def draw_lootr_chest(px):
    # Subtle floor shadow
    rect(px, 14, 48, 49, 50, SHADOW_FLOOR)
    rect(px, 16, 51, 47, 51, SHADOW_FLOOR)

    # --- CHEST BODY (x: 14..49, y: 29..48) ---
    rect(px, 14, 29, 49, 48, OUTLINE)
    # Wood interior
    rect(px, 15, 30, 48, 47, WOOD_MID)
    # Horizontal plank shading
    for y in [33, 37, 41, 45]:
        rect(px, 15, y, 48, y, WOOD_DARK)
    for y in [30, 34, 38, 42]:
        rect(px, 16, y, 47, y, WOOD_LIGHT)
    # Right/bottom wood shadow
    rect(px, 46, 30, 48, 47, WOOD_DARK)
    rect(px, 15, 46, 48, 47, WOOD_DARK)

    # Thick golden perimeter & vertical bands (classic Lootr gilded chest)
    # Left & right vertical gold trim
    rect(px, 15, 30, 17, 47, GOLD_MID)
    rect(px, 15, 30, 15, 47, GOLD_BRIGHT)
    rect(px, 17, 30, 17, 47, GOLD_SHADOW)

    rect(px, 46, 30, 48, 47, GOLD_MID)
    rect(px, 46, 30, 46, 47, GOLD_BRIGHT)
    rect(px, 48, 30, 48, 47, GOLD_SHADOW)

    # Inner vertical reinforcing straps
    for bx in (23, 39):
        rect(px, bx, 30, bx + 1, 47, GOLD_MID)
        rect(px, bx, 30, bx, 47, GOLD_BRIGHT)
        rect(px, bx + 1, 30, bx + 1, 47, GOLD_DARK)

    # Top & bottom gold bands on body
    rect(px, 15, 30, 48, 31, GOLD_MID)
    rect(px, 15, 30, 48, 30, GOLD_BRIGHT)
    rect(px, 15, 46, 48, 47, GOLD_DARK)
    rect(px, 15, 46, 48, 46, GOLD_MID)

    # Corner rivets on body
    for rx, ry in [(16, 31), (47, 31), (16, 46), (47, 46), (23, 36), (40, 36), (23, 43), (40, 43)]:
        put(px, rx, ry, GOLD_SPEC)

    # --- GLOWING GAP BETWEEN BODY AND AJAR LID (y: 26..29) ---
    rect(px, 15, 26, 48, 29, OUTLINE)
    rect(px, 16, 27, 47, 29, GLOW_MID)
    rect(px, 18, 27, 45, 28, GLOW_CORE)
    rect(px, 22, 28, 41, 28, GLOW_GOLD)
    rect(px, 27, 27, 36, 28, SPARKLE)

    # --- AJAR CHEST LID (slightly lifted & tilted back: x: 13..50, y: 17..26) ---
    rect(px, 13, 17, 50, 26, OUTLINE)
    # Lid top bevel
    rect(px, 14, 18, 49, 25, WOOD_LIGHT)
    rect(px, 15, 18, 48, 19, WOOD_HI)
    rect(px, 14, 21, 49, 21, WOOD_DARK)
    rect(px, 14, 24, 49, 25, WOOD_MID)

    # Lid golden trim & straps
    rect(px, 14, 18, 49, 19, GOLD_MID)
    rect(px, 15, 18, 48, 18, GOLD_BRIGHT)
    rect(px, 14, 24, 49, 25, GOLD_MID)
    rect(px, 14, 25, 49, 25, GOLD_SHADOW)

    rect(px, 14, 18, 16, 25, GOLD_MID)
    rect(px, 14, 18, 14, 25, GOLD_BRIGHT)
    rect(px, 47, 18, 49, 25, GOLD_MID)
    rect(px, 49, 18, 49, 25, GOLD_SHADOW)

    for bx in (23, 39):
        rect(px, bx, 18, bx + 1, 25, GOLD_MID)
        rect(px, bx, 18, bx, 25, GOLD_BRIGHT)
        rect(px, bx + 1, 18, bx + 1, 25, GOLD_DARK)

    # Lid specular accents
    for rx, ry in [(15, 19), (48, 19), (23, 21), (39, 21)]:
        put(px, rx, ry, GOLD_SPEC)

    # --- GOLDEN CLASP / LOCK (centered at x: 29..34, y: 24..33) ---
    rect(px, 29, 24, 34, 33, OUTLINE)
    rect(px, 30, 25, 33, 32, GOLD_MID)
    rect(px, 30, 25, 32, 26, GOLD_SPEC)
    rect(px, 33, 26, 33, 32, GOLD_SHADOW)
    rect(px, 30, 32, 33, 32, GOLD_SHADOW)
    # Keyhole
    put(px, 31, 28, OUTLINE)
    put(px, 32, 28, OUTLINE)
    put(px, 31, 29, OUTLINE)
    put(px, 32, 29, OUTLINE)
    put(px, 31, 30, GOLD_DARK)


def draw_ring(px, cx, cy, r_out, r_in, col_dark, col_mid, col_bright, draw_mask=None):
    ir = int(r_out) + 2
    # 1px dark outline
    for y in range(cy - ir, cy + ir + 1):
        for x in range(cx - ir, cx + ir + 1):
            if draw_mask and not draw_mask(x, y):
                continue
            d2 = (x - cx) ** 2 + (y - cy) ** 2
            if (r_in - 1.1) ** 2 <= d2 <= (r_out + 1.1) ** 2:
                if d2 > r_out ** 2 or d2 < r_in ** 2:
                    put(px, x, y, RING_OUT)

    # Ring body with top-left highlight and bottom-right shade
    for y in range(cy - ir, cy + ir + 1):
        for x in range(cx - ir, cx + ir + 1):
            if draw_mask and not draw_mask(x, y):
                continue
            d2 = (x - cx) ** 2 + (y - cy) ** 2
            if r_in ** 2 <= d2 <= r_out ** 2:
                if (x - cx) + (y - cy) <= -3:
                    put(px, x, y, col_bright)
                elif (x - cx) + (y - cy) >= 3:
                    put(px, x, y, col_dark)
                else:
                    put(px, x, y, col_mid)


def draw_interlocking_rings(px):
    # Two smooth interlocking rings floating in front of the lower chest (y = 45)
    # Left ring: cyan-teal (cx=27, cy=45), Right ring: gleaming gold (cx=36, cy=45)
    r_out = 6.6
    r_in = 3.7
    draw_ring(px, 27, 45, r_out, r_in, CYAN_DARK, CYAN_MID, CYAN_BRIGHT)
    draw_ring(px, 36, 45, r_out, r_in, GOLD_DARK, GOLD_MID, GOLD_BRIGHT)
    # Weave: left ring passes OVER right ring at the bottom crossing (y >= 46)
    draw_ring(
        px, 27, 45, r_out, r_in, CYAN_DARK, CYAN_MID, CYAN_BRIGHT,
        draw_mask=lambda x, y: y >= 46 and x >= 29
    )
    # Specular glints on both rings
    put(px, 23, 41, SPARKLE)
    put(px, 33, 41, GOLD_SPEC)


def main():
    img = Image.new("RGBA", (SIZE, SIZE), TRANSPARENT)
    px = img.load()

    draw_badge_frame(px)
    draw_treasure_glow(px)
    draw_lootr_chest(px)
    draw_interlocking_rings(px)

    upscaled = img.resize((SIZE * SCALE, SIZE * SCALE), Image.Resampling.NEAREST)

    repo_root = Path(__file__).resolve().parent.parent
    outputs = [
        repo_root / "common" / "src" / "main" / "resources" / "icon.png",
        repo_root / "docs" / "artwork" / "icon.png",
    ]
    for out in outputs:
        out.parent.mkdir(parents=True, exist_ok=True)
        upscaled.save(out, format="PNG", optimize=True)
        print(f"Wrote {out} ({upscaled.width}x{upscaled.height})")


if __name__ == "__main__":
    main()
