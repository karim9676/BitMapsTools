#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Text to C header bitmap generator with proper Indic script shaping.
Modified to ensure single-pixel width for regular fonts.
"""

import sys
import math
import argparse
import struct
try:
    import uharfbuzz as hb
    from freetype import Face, FT_LOAD_RENDER, FT_LOAD_TARGET_MONO
except ImportError as e:
    sys.stderr.write("Error: missing module – ensure 'uharfbuzz' and 'freetype-py' are installed.\n")
    sys.exit(1)

def main():
    parser = argparse.ArgumentParser(
        description="Render text to a monochrome bitmap C header (supports Indic scripts shaping and font style selection)."
    )
    parser.add_argument("text", help="Text string to render (e.g., Telugu text).")
    parser.add_argument("-f", "--font", required=True, help="Path to TTF/OTF font file (regular font).")
    parser.add_argument("--style", choices=["regular", "bold"], default="regular",
                        help="Font style to use (regular or bold).")
    parser.add_argument("-s", "--size", type=int, default=16, help="Font size in pixels (for FreeType).")
    parser.add_argument("-o", "--output", help="Output file path for C header. If not set, output is printed to stdout.")
    parser.add_argument("--name", default="image", help="Base name for generated C definitions.")
    parser.add_argument("--offset-x", type=int, default=0, help="Horizontal offset in pixels.")
    parser.add_argument("--offset-y", type=int, default=0, help="Vertical offset in pixels.")
    parser.add_argument("--width", type=int, default=0, help="Force image width in pixels (0 = auto).")
    parser.add_argument("--img-height", type=int, default=0, help="Force output image height in pixels (0 = auto).")
    parser.add_argument("--spacing", type=int, default=1, help="Extra spacing between clusters/characters.")
    parser.add_argument("--print", action="store_true", help="Print visual bitmap to console.")
    args = parser.parse_args()

    text = args.text
    font_path = args.font
    font_size = args.size
    name = args.name
    output= args.output

    # Load font with FreeType and set pixel size
    face = Face(font_path)
    face.set_pixel_sizes(0, font_size)

    # HarfBuzz font setup
    font_data = open(font_path, "rb").read()
    hb_face = hb.Face(font_data)
    hb_font = hb.Font(hb_face)
    hb_font.scale = (font_size * 64, font_size * 64)

    # Shape the text
    buf = hb.Buffer()
    buf.add_str(text)
    buf.guess_segment_properties()
    hb.shape(hb_font, buf, {})
    glyph_infos = buf.glyph_infos
    glyph_positions = buf.glyph_positions

    # Calculate dimensions
    ascender_px = math.ceil(face.size.ascender / 64.0)
    descender_px = math.ceil(-face.size.descender / 64.0) if face.size.descender < 0 else 0
    baseline_y = ascender_px + args.offset_y

    pen_x = args.offset_x
    prev_cluster = None
    for i, glyph_info in enumerate(glyph_infos):
        cluster = glyph_info.cluster
        if prev_cluster is not None and cluster != prev_cluster:
            pen_x += args.spacing
        prev_cluster = cluster
        x_adv_px = int(math.floor(glyph_positions[i].x_advance / 64.0 + 0.5))
        pen_x += x_adv_px
    total_advance = pen_x - args.offset_x
    img_width = args.width if args.width > 0 else args.offset_x + total_advance
    if img_width < args.offset_x + total_advance:
        img_width = args.offset_x + total_advance

    dynamic_height = baseline_y + descender_px
    img_height = args.img_height if args.img_height > 0 else dynamic_height

    # Create monochrome image
    from PIL import Image
    dynamic_img = Image.new('1', (img_width, dynamic_height), color=0)
    pen_x = args.offset_x
    prev_cluster = None

    # Render glyphs
    for i, glyph_info in enumerate(glyph_infos):
        cluster = glyph_info.cluster
        if prev_cluster is not None and cluster != prev_cluster:
            pen_x += args.spacing
        prev_cluster = cluster

        glyph_idx = glyph_info.codepoint
        # Use strict monochrome rendering
        face.load_glyph(glyph_idx, flags=FT_LOAD_RENDER | FT_LOAD_TARGET_MONO)
        slot = face.glyph
        bitmap = slot.bitmap
        x_off = int(math.floor(glyph_positions[i].x_offset / 64.0 + 0.5))
        y_off = int(math.floor(glyph_positions[i].y_offset / 64.0 + 0.5))
        x_pos = pen_x + slot.bitmap_left + x_off
        y_pos = baseline_y - slot.bitmap_top - y_off
        if y_pos < 0:
            y_pos = 0

        # Process bitmap ensuring single-pixel width
        for row in range(bitmap.rows):
            row_y = y_pos + row
            if row_y < 0 or row_y >= dynamic_height:
                continue
            for col_byte in range(bitmap.pitch):
                byte_val = bitmap.buffer[row * bitmap.pitch + col_byte]
                for bit in range(8):
                    if col_byte * 8 + bit >= bitmap.width:
                        break
                    if byte_val & (1 << (7 - bit)):
                        x = x_pos + col_byte * 8 + bit
                        if 0 <= x < img_width:
                            dynamic_img.putpixel((x, row_y), 1)
        x_adv_px = int(math.floor(glyph_positions[i].x_advance / 64.0 + 0.5))
        pen_x += x_adv_px

    # Handle fixed height if specified
    if args.img_height and args.img_height > 0:
        final_img = Image.new('1', (img_width, img_height), color=0)
        offset_y = (img_height - dynamic_height) // 2
        if offset_y >= 0:
            final_img.paste(dynamic_img, (0, offset_y))
        else:
            final_img = dynamic_img.crop((0, -offset_y, img_width, -offset_y + img_height))
        img = final_img
    else:
        img = dynamic_img

     # Build binary output.
    # We'll iterate row-by-row, pack 8 pixels per byte, and only include rows that contain at least one "on" pixel.
    rows_data = bytearray()
    non_empty_row_count = 0
    for y in range(img.height):
        bit_acc = 0
        bits_in_acc = 0
        has_nonzero = False
        row_bytes = bytearray()
        for x in range(img_width):
            # In mode '1', pixel value is 0 (off) or 255 (on).
            pixel = img.getpixel((x, y))
            bit = 0 if pixel == 0 else 1
            if bit == 1:
                has_nonzero = True
            bit_acc = (bit_acc << 1) | bit
            bits_in_acc += 1
            if bits_in_acc == 8:
                row_bytes.append(bit_acc)
                bit_acc = 0
                bits_in_acc = 0
        if bits_in_acc > 0:
            bit_acc = bit_acc << (8 - bits_in_acc)
            row_bytes.append(bit_acc)
        if has_nonzero:
            rows_data.extend(row_bytes)
            non_empty_row_count += 1

    # Create header: 4 bytes for width and 4 bytes for non-empty row count, big-endian.
    header = struct.pack(">II", img_width, non_empty_row_count)
    binary_data = header + rows_data

    # Write binary output to file.
    if output:
    	try:
        	with open(args.output, "wb") as bf:
        	    bf.write(binary_data)
        #print(f"Binary bitmap data written to '{args.output}'")
    	except Exception as e:
        	sys.stderr.write(f"Error writing binary file: {e}\n")
        	sys.exit(1)
    # Print visual bitmap and diagnostics
    if args.print:
        #print("\nVisual Bitmap (single-pixel width verification):")
        for y in range(img.height):
            row = ""
            for x in range(img_width):
                row += "1" if img.getpixel((x, y)) != 0 else "0"
            print(row)
     #   print(f"\nFont: {font_path}, Size: {font_size}px, Style: {args.style}")
     #   print(f"Image width: {img_width}px, Image height: {img_height}px")

    # TODO: Add C header generation logic here if needed

if __name__ == "__main__":
    main()
