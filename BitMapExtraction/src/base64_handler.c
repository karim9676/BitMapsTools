
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "json_handler.h"
/*====================*/
/* Base64 Decoding    */
/*====================*/

// Base64 lookup table
const char base64_table[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";


// Function to find the base64 index of a character
int base64_index(char c) {
    for (int i = 0; i < 64; i++) {
        if (base64_table[i] == c)
            return i;
    }
    return -1; // Invalid character
}

// Function to decode a base64 encoded string
// The decoded bytes are stored in output; *out_len is set to the number of bytes written.
void base64_decode(const char *input, unsigned char *output, int *out_len) {
    int len = strlen(input);
    int i, j = 0;
    int pad = 0;
    
    if (len >= 1 && input[len - 1] == '=') pad++;
    if (len >= 2 && input[len - 2] == '=') pad++;

    for (i = 0; i < len; i += 4) {
        int val = (base64_index(input[i]) << 18) +
                  (base64_index(input[i + 1]) << 12) +
                  (base64_index(input[i + 2]) << 6) +
                  (base64_index(input[i + 3]));
        output[j++] = (val >> 16) & 0xFF;
        if (input[i + 2] != '=')
            output[j++] = (val >> 8) & 0xFF;
        if (input[i + 3] != '=')
            output[j++] = val & 0xFF;
    }
    
    *out_len = j;
}

/*========================================*/
/* Decode bitmap string into 16-bit arrays */
/*========================================*/

/*
   This function expects a bitmap string that contains one or more comma-separated
   base64-encoded segments (each representing one row of the bitmap). The parameters
   'width' and 'height' specify the expected dimensions.

   It decodes each row into an array of 16-bit values (each value representing 16 pixels).
   The number of 16-bit values per row is computed as: num_columns = (width + 15) / 16.
*/
void decode_bitmap_string(const char *bitmap_str, int width, int height) {
    int num_columns = (width + 15) / 16;
    
    // Make a writable copy of the bitmap string for tokenization.
    char *str_copy = strdup(bitmap_str);
    if (!str_copy) {
        printf("Memory allocation error.\n");
        return;
    }
    
    // Allocate a 2D array to store the decoded 16-bit values.
    uint16_t **map = malloc(sizeof(uint16_t*) * height);
    if (!map) {
        free(str_copy);
        printf("Memory allocation error.\n");
        return;
    }
    for (int i = 0; i < height; i++) {
        map[i] = calloc(num_columns, sizeof(uint16_t));
        if (!map[i]) {
            for (int k = 0; k < i; k++) free(map[k]);
            free(map);
            free(str_copy);
            printf("Memory allocation error.\n");
            return;
        }
    }
    
    // Tokenize the string by commas.
    char *token = strtok(str_copy, ",");
    int row = 0;
    while (token != NULL && row < height) {
        int token_len = strlen(token);
        // Calculate a buffer size for decoded bytes (4 chars => 3 bytes)
        int buf_size = token_len * 3 / 4 + 3;
        unsigned char *decoded = malloc(buf_size);
        if (!decoded) break;
        int decoded_len = 0;
        base64_decode(token, decoded, &decoded_len);
        
        // For each 16-bit column, combine two bytes (if available)
        for (int j = 0; j < num_columns; j++) {
            int index = 2 * j;
            int byte1 = (index < decoded_len) ? decoded[index] : 0;
            int byte2 = (index + 1 < decoded_len) ? decoded[index + 1] : 0;
            map[row][j] = (byte1 << 8) | byte2;
        }
        free(decoded);
        row++;
        token = strtok(NULL, ",");
    }
    
    free(str_copy);
    
    // Print the binary representation
    printf("Decoded Bitmap Data (%d x %d):\n", width, height);
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < num_columns; j++) {
            for (int k = 15; k >= 0; k--) {
                printf("%c", (map[i][j] & (1 << k)) ? '#' : '.');
            }
        }
        printf("\n");
    }
    
    // Print the stored 16-bit values in hexadecimal format
    printf("\nStored 16-bit Bitmap Data (Hex):\n");
    for (int i = 0; i < height; i++) {
        for (int j = 0; j < num_columns; j++) {
            printf("%04X ", map[i][j]);
        }
        printf("\n");
    }
    
    // Free the allocated 2D array
    for (int i = 0; i < height; i++) {
        free(map[i]);
    }
    free(map);
}

