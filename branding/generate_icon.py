#!/usr/bin/env python3
"""Generate RealSize's deliberately simple block-art icon."""

import os
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
OUTPUTS = (
    ROOT / "mc121/src/main/resources/assets/realsize/icon.png",
    ROOT / "mc2612/src/main/resources/assets/realsize/icon.png",
)
PREVIEW = Path(os.environ.get("REALSIZE_ICON_PREVIEW", "/tmp/realsize-icon-96.png"))

SIZE = 512
BG = "#172126"
OUTLINE = "#0b0f12"
BROWN = "#a9612d"
DARK_BROWN = "#62371f"
CREAM = "#f1d59b"
WHITE = "#f7f3e8"
CYAN = "#4fd6d2"

image = Image.new("RGB", (SIZE, SIZE), BG)
draw = ImageDraw.Draw(image)

# Resize brackets. Thick, square-ended marks survive Modrinth's 96px rendering.
draw.line([(62, 174), (62, 66), (170, 66)], fill=CYAN, width=24)
draw.polygon([(62, 50), (34, 86), (90, 86)], fill=CYAN)
draw.polygon([(46, 66), (82, 38), (82, 94)], fill=CYAN)
draw.line([(342, 446), (450, 446), (450, 338)], fill=CYAN, width=24)
draw.polygon([(450, 462), (422, 426), (478, 426)], fill=CYAN)
draw.polygon([(466, 446), (430, 418), (430, 474)], fill=CYAN)

# Tail behind the body.
draw.line([(145, 225), (105, 183), (82, 211)], fill=OUTLINE, width=28)
draw.line([(145, 225), (105, 183), (82, 211)], fill=DARK_BROWN, width=14)
draw.rectangle((68, 198, 92, 230), fill=OUTLINE)
draw.rectangle((73, 203, 87, 225), fill=BROWN)

# Body and patches.
draw.rectangle((126, 174, 364, 342), fill=OUTLINE)
draw.rectangle((142, 190, 348, 326), fill=BROWN)
draw.polygon([(160, 190), (228, 190), (245, 233), (211, 273), (155, 257)], fill=CREAM)
draw.polygon([(275, 220), (348, 208), (348, 286), (310, 298), (278, 267)], fill=DARK_BROWN)
draw.rectangle((142, 300, 198, 326), fill=DARK_BROWN)

# Legs, with light hooves for a readable stance.
for left, right in ((158, 211), (285, 338)):
    draw.rectangle((left, 326, right, 419), fill=OUTLINE)
    draw.rectangle((left + 15, 326, right - 15, 390), fill=BROWN)
    draw.rectangle((left + 15, 390, right - 15, 404), fill=CREAM)

# Blocky head, horn, ear, muzzle, and eye.
draw.rectangle((334, 194, 434, 306), fill=OUTLINE)
draw.rectangle((350, 210, 418, 290), fill=BROWN)
draw.rectangle((405, 245, 458, 318), fill=OUTLINE)
draw.rectangle((405, 260, 442, 302), fill=CREAM)
draw.polygon([(350, 207), (329, 162), (372, 198)], fill=OUTLINE)
draw.polygon([(352, 194), (340, 174), (365, 198)], fill=CREAM)
draw.rectangle((422, 202, 453, 238), fill=OUTLINE)
draw.rectangle((422, 210, 443, 229), fill=DARK_BROWN)
draw.rectangle((385, 222, 405, 242), fill=WHITE)
draw.rectangle((394, 228, 405, 242), fill=OUTLINE)
draw.rectangle((424, 278, 435, 290), fill=DARK_BROWN)

for output in OUTPUTS:
    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output, optimize=True)

PREVIEW.parent.mkdir(parents=True, exist_ok=True)
image.resize((96, 96), Image.Resampling.LANCZOS).save(PREVIEW, optimize=True)
print("\n".join(str(path) for path in (*OUTPUTS, PREVIEW)))
