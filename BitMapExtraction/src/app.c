
#include "defines.h"
#include "english_16x16.h"
#include "led_header.h"
#include "Telugu_16x16.h"
#include "json.h"
#include "json_handler.h"

void GetBitMapForReceivedInput(const char *input, user_input_16X16_bit_map *pbit_map) {
    const uint8_t FIRST_CHAR = 0x20;
    uint8_t length = strlen(input);
    pbit_map->input_txt_length = length;

    for (uint8_t idx = 0; idx < length; idx++) {
        char ch = input[idx];
        uint8_t char_index = ch - FIRST_CHAR;
        const uint8_t *char_bitmap = &font16x16[char_index * 32 + 6];
        uint16_t row_data[16]={0};
        for (int bit_idx = 0; bit_idx < FONT_16; bit_idx++) {
             row_data[bit_idx] = (char_bitmap[bit_idx * 2] << 8) | char_bitmap[bit_idx * 2 + 1];
        }
      }
}
/* function called every shift operation..*/
void PropagateDataLineShift(single_ic_payload *st_char_bitmap,device_ics *st_ics_data,single_ic_payload *pprevious_cr) {
    /* find st_char carry bit*/    
    // st_char 
    int icount=0;
    for(int ic=5;ic>=0;ic--) {
        for(int ishift=0;ishift<16;ishift++) {
            int id= ishift / MAX_DATA_LINES;
            int ir= ishift % MAX_ROWS;
            single_ic_payload present_carrys={0};
            present_carrys.st_data_lines[id].st_rows[ir].st_leds[0] = (st_ics_data->st_ics[ic].st_data_lines[id].st_rows[ir].st_leds[0] & 0x8000) >> 15;
            st_ics_data->st_ics[ic].st_data_lines[id].st_rows[ir].st_leds[0] = (st_ics_data->st_ics[ic].st_data_lines[id].st_rows[ir].st_leds[0] << 1) | pprevious_cr->st_data_lines[id].st_rows[ir].st_leds[0];    
            pprevious_cr->st_data_lines[id].st_rows[ir].st_leds[0] = present_carrys.st_data_lines[id].st_rows[id].st_leds[0];
        }   
        DebugPrintDeviceICS(st_ics_data); 
    }
   
}
void LoadBitMapToDeviceICS(const user_input_16X16_bit_map *pbit_map) {
    
    /* n char * 16 shifts ..!*/
    device_ics board={0}; /*ics */
    for (uint8_t idx = 0; idx < pbit_map->input_txt_length; idx++) {
        single_ic_payload st_char_bit_mp={0};
        st_char_bit_mp= pbit_map->payload_batch[idx];   
        board.ics_count++;
        single_ic_payload st_prev_bit_map={0};  
        for(int ishift=0;ishift<16;ishift++) {
            int id= ishift / MAX_DATA_LINES;
            int ir= ishift % MAX_ROWS;
            single_ic_payload present_carrys={0};
            /* 5 th index alway take from input char*/
            present_carrys.st_data_lines[id].st_rows[ir].st_leds[0] = (st_char_bit_mp.st_data_lines[id].st_rows[ir].st_leds[0] & 0x8000) >> 15;
            st_char_bit_mp.st_data_lines[id].st_rows[ir].st_leds[0] <<=1; /* shift input bit map of a character..!*/
            /*shift and add prevoius carry to present LED location..*/
            board.st_ics[5].st_data_lines[id].st_rows[ir].st_leds[0] = (st_char_bit_mp.st_data_lines[id].st_rows[ir].st_leds[0]) | st_prev_bit_map.st_data_lines[id].st_rows[ir].st_leds[0];    
            /*PRINT_BINARY(board.st_ics[5].st_data_lines[id].st_rows[ir].st_leds[0]);
            printf("\n");*/
            st_prev_bit_map.st_data_lines[id].st_rows[ir].st_leds[0] = present_carrys.st_data_lines[id].st_rows[id].st_leds[0];
            for(int ic=4;ic>=0;ic--) {
                single_ic_payload present_carrys={0};
                present_carrys.st_data_lines[id].st_rows[ir].st_leds[0] = (board.st_ics[ic+1].st_data_lines[id].st_rows[ir].st_leds[0] & 0x8000) >> 15;
                board.st_ics[ic].st_data_lines[id].st_rows[ir].st_leds[0] = (board.st_ics[ic+1].st_data_lines[id].st_rows[ir].st_leds[0] << 1) | st_prev_bit_map.st_data_lines[id].st_rows[ir].st_leds[0]; 
                st_prev_bit_map.st_data_lines[id].st_rows[ir].st_leds[0] = present_carrys.st_data_lines[id].st_rows[id].st_leds[0];
            } 
        }
        DebugPrintDeviceICS(&board);

       
    }
   
   
}

void DebugPrintBitMap(const user_input_16X16_bit_map *pbit_map) {
    printf("Text Length: %d\n", pbit_map->input_txt_length);
    for (uint8_t i = 0; i < pbit_map->input_txt_length; i++) {
        printf("Character %d:\n", i + 1);
        for (int line = 0; line < 4; line++) {
            for (int row = 0; row < MAX_ROWS; row++) {
                //printf("    Row %d: ", row + 1);
                uint16_t led_data = pbit_map->payload_batch[i].st_data_lines[line].st_rows[row].st_leds[0];
                for (int bit = 15; bit >= 0; bit--) {
                    printf(" %c", (led_data & (1 << bit)) ? '1' : '0');
                }
                printf("\n");
            }
        }
    }
}

void DebugPrintTeluguBitMap(const user_Telugu_input_16X16_bit_map *pbit_map) {
    printf("Text Length: %d\n", pbit_map->input_txt_length);
    for (uint8_t i = 0; i < pbit_map->input_txt_length; i++) {
        printf("Character %d:\n", i + 1);
        for (int line = 0; line < 4; line++) {
            for (int row = 0; row < MAX_ROWS; row++) {
                //printf("    Row %d: ", row + 1);
                uint16_t led_data = pbit_map->bitmaps[i];
                for (int bit = 15; bit >= 0; bit--) {
                    printf(" %c", (led_data & (1 << bit)) ? '#' : '.');
                }
                printf("\n");
            }
        }
    }
}
void DebugPrintDeviceICSVertical(const device_ics *board) {

    printf("Total ICs: %d\n", board->ics_count);
    for (int row = 0; row < MAX_ROWS; row++) {
        for (int line = 0; line < MAX_DATA_LINES; line++) {
            for (int ic = 0; ic < board->ics_count; ic++) {
                uint16_t led_data = board->st_ics[ic].st_data_lines[line].st_rows[row].st_leds[0];
                for (int bit = 15; bit >= 0; bit--) {
                    printf("%c", (led_data & (1 << bit)) ? '#' : '.');
                }
                printf("    ");
            }
            printf("\n");
        }
    }
}
void DebugPrintDeviceICS(const device_ics *board) {
    for (int ic = 5; ic>=0; ic--) {
        printf("IC %d:\n", ic );
        for (int line = 0; line < MAX_DATA_LINES; line++) {
            // printf("  Data Line %d:\n", line + 1);
            for (int row = 0; row < MAX_ROWS; row++) {
                // printf("    Row %d: ", row + 1);
                uint16_t led_data = board->st_ics[ic].st_data_lines[line].st_rows[row].st_leds[0];
                for (int bit = 15; bit >= 0; bit--) {
                    printf("%c", (led_data & (1 << bit)) ? '#' : '.');
                }
                printf("\n");
            }
        }
        break;
    }
}



void GetTeluguBitMap(const char *input, user_Telugu_input_16X16_bit_map *pbit_map) {
    
    
    
    uint8_t length = strlen(input);
    pbit_map->input_txt_length = length;

    for (uint8_t idx = 0; idx < length; idx++) {
        uint16_t ch = (uint8_t)input[idx];

        // Convert multi-byte UTF-8 to Unicode
        if ((ch & 0xE0) == 0xC0) { // 2-byte sequence
            ch = ((input[idx] & 0x1F) << 6) | (input[++idx] & 0x3F);
        } else if ((ch & 0xF0) == 0xE0) { // 3-byte sequence
            ch = ((input[idx] & 0x0F) << 12) | ((input[++idx] & 0x3F) << 6) | (input[++idx] & 0x3F);
        }

        // Ensure the character is within the supported range
        if (ch < FIRST_T_CHAR || ch >= FIRST_T_CHAR + CHAR_COUNT) {
            printf("Unsupported character: 0x%04X\n", ch);
            continue; // Skip unsupported characters
        }

        uint16_t char_index = ch - FIRST_T_CHAR;
        const uint8_t *char_bitmap = &font16x16_t[char_index * 32]; // Each char takes 32 bytes

        for (int bit_idx = 0; bit_idx < FONT_16; bit_idx++) {
            // Convert two bytes into one 16-bit value
            pbit_map->bitmaps[idx][bit_idx] = 
                (char_bitmap[bit_idx * 2] << 8) | char_bitmap[bit_idx * 2 + 1];
        }
    }
}
void SetDataEachRow(device_ics *board) {
    // Iterate through all 16 bits
    for (int bit_index = 15; bit_index >= 0; bit_index--) {
        for (int ic = 0; ic < board->ics_count; ic++) { // Loop through each IC
            for (int line = 0; line < MAX_DATA_LINES; line++) {
                for (int row = 0; row < MAX_ROWS; row++) {
                    uint16_t *led_data = &board->st_ics[ic].st_data_lines[line].st_rows[row].st_leds[0];
                    uint8_t bit = (*led_data >> bit_index) & 0x01;
                }
            }
        }
    }
}

#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include "Telugu_16x16.h" // Include the bitmap data file

// Function to extract Unicode from UTF-8 encoded Telugu string
uint16_t extract_unicode(const char *str, int *index) {
    uint16_t unicode = 0;
    unsigned char c = str[*index];

    if ((c & 0xF0) == 0xE0) { // 3-byte UTF-8 sequence
        unicode = ((str[*index] & 0x0F) << 12) |
                  ((str[*index + 1] & 0x3F) << 6) |
                  (str[*index + 2] & 0x3F);
        *index += 2; // Skip 2 extra bytes of UTF-8 character
    }
    (*index)++;
    return unicode;
}

// Mapping Telugu Unicode code points to bitmap indices
int unicode_to_bitmap_index(uint16_t unicode) {
    if (unicode >= 0x0C00 && unicode <= 0x0C7F) {
        return unicode - 0x0C00; // Adjust offset based on font file
    }
    return -1; // Character not found
}
//    0x00, 0x00, 
//    0x00, 0x00, 
//    0x00, 0x20, 
//    0x02, 0x60, 
//    0x03, 0xC0, 
//    0x01, 0xE0, 
//    0x00, 0x30, 
//    0x0E, 0x70, 
//    0x0A, 0x90, 
//    0x0E, 0x70, 
//    0x0C, 0x30, 
//    0x07, 0xC0, 
//    0x00, 0x00, 
//    0x00, 0x00, 
//    0x00, 0x00, 
//    0x00, 0x00, // త



// Function to render a given Telugu string
void render_telugu_string(const char *telugu_string,user_Telugu_input_16X16_bit_map *pbit_map) {
    int i = 0;
    int bit_idx=0;
    while (telugu_string[i] != '\0') {
        uint16_t unicode = extract_unicode(telugu_string, &i);
        int index = unicode_to_bitmap_index(unicode);
        if (index >= 0) {
            printf("Character U+%04X:\n", unicode);

            const uint8_t *char_bitmap = &font16x16_t[index * 32]; // Each char takes 32 bytes
            //render_character(&font16x16_t[index * 32]); // Render bitmap

            for (int bit_idx = 0; bit_idx < FONT_16; bit_idx++) {
                pbit_map->bitmaps[bit_idx][bit_idx] = (char_bitmap[bit_idx * 2] << 8) | char_bitmap[bit_idx * 2 + 1];
                //PRINT_BINARY(pbit_map->bitmaps[bit_idx][bit_idx]);
                //printf("\n");
            }
            bit_idx++;
            printf("\n====================\n\n");
        } else {
            printf("Character U+%04X not found in font map.\n", unicode);
        }
    }
}

int telugu_maping() {
    // Telugu string "తెలుగు"
    const char *telugu_string = "తెలుగు";
    printf("Rendering Telugu string:\n");
    user_Telugu_input_16X16_bit_map telugu_str_bit_map; 
    render_telugu_string(telugu_string,&telugu_str_bit_map);
    //DebugPrintTeluguBitMap(&telugu_str_bit_map);
    return 0;
}

int main(int argc, char *argv[]) {
      Config config;
      if (parseRouteJSON(argv[1],argv[2], &config) == 0) {
          // Process parsed data. For example, print the route and front screen translation:
          debugPrintConfig(&config);
      }
      free(config.items);
      return 0;
  }

