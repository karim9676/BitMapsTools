
#include "ft2build.h"
#include FT_FREETYPE_H
#include <stdio.h>
#include <locale.h>
#define WIDTH 16   // Dot matrix width
#define HEIGHT 16  // Dot matrix height

void render_character_bitmap(FT_Bitmap* bitmap) {
    // Print the bitmap in binary form
    for (int y = 0; y < HEIGHT; y++) {
        for (int x = 0; x < WIDTH; x++) {
            if (y < bitmap->rows && x < bitmap->width) {
                unsigned char pixel = bitmap->buffer[y * bitmap->pitch + x];
                printf("%c", pixel ? '#' : '.');  // Print '1' for set pixels, '0' otherwise
            } else {
                printf(".");  // Fill empty spaces
            }
        }
        printf("\n");
    }
    printf("\n");
}

void render_devanagari_word(const char* word, const char* font_file) {
    FT_Library library;
    FT_Face face;

    // Initialize FreeType library
    if (FT_Init_FreeType(&library)) {
        printf("Error: Could not initialize FreeType library.\n");
        return;
    }

    // Load the font file
    if (FT_New_Face(library, font_file, 0, &face)) {
        printf("Error: Could not load font file: %s\n", font_file);
        FT_Done_FreeType(library);
        return;
    }

    // Set the character size to 16x16 pixels
    FT_Set_Pixel_Sizes(face, WIDTH, HEIGHT);

    // Iterate over each character in the word
    for (int i = 0; i < 1;i++) {
    
   	unsigned int unicode_char = 0x0905;
   
        //unsigned int unicode_char = //word[i];  // Extract the Unicode code point for the character

        // Load and render the character
        if (FT_Load_Char(face, unicode_char, FT_LOAD_RENDER)) {
            printf("Error: Could not render character '%c'.\n", word[i]);
            continue;
        }

        // Access the rendered bitmap
        FT_Bitmap* bitmap = &face->glyph->bitmap;

        // Skip empty characters with no bitmap
        if (bitmap->rows == 0 || bitmap->width == 0)
            continue;

        // Print character info and render the bitmap
        printf("Character: %c (U+%04X)\n", word[i], unicode_char);
        render_character_bitmap(bitmap);
    }

    // Clean up
    FT_Done_Face(face);
    FT_Done_FreeType(library);
}

int Diplay_fonts() {
    // Set locale for Unicode support
    setlocale(LC_ALL, "en_US.UTF-8");
    // Devanagari word to render (e.g., "नमस्ते" meaning "Hello")
    const char* devanagari_word = "नमस्ते";

    // Path to the Devanagari font file
    const char* font_file = "../fonts/mangal.ttf";

    // Render the word
    render_devanagari_word(devanagari_word, font_file);

    return 0;
}

