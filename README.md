# Bitmap Tools

Bitmap Tools is a collection of Java utilities for working with bitmaps. The project currently includes two main components:

1. **Bitmap Generation:** Converts a given text string into a bitmap representation using a specified TrueType font.
2. **Bitmap Extraction:** Extracts bitmap data from a binary image file and outputs it in a structured JSON format.

Both tools output the bitmap data as a JSON object that contains (at least) the bitmap rows (encoded in Base64), the image width, and the effective height (i.e. number of rows that contain non-zero pixels).

> **Note:** If the Bitmap Extraction functionality is not yet implemented, you can use this README as a guideline for adding and documenting it in the future.

## Directory Structure

The project is organized as follows:


├── BitMapExtraction
│   ├── inc
│   ├── Makefile
│   ├── Makefile~
│   ├── README.md
│   └── src
├── BitMapGeneration
│   ├── fonts
│   ├── README.md
│   └── src
└── README.md
