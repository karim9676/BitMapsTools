#ifndef __LED_HEADER_
#define __LED_HEADER_

#include<stdio.h>
#include<string.h>
#include<stdint.h>
#include<stdbool.h>


#define MAX_ICS 6
#define MAX_ROWS 4
#define MAX_DATA_LINES 4
#define MAX_SHIFT_REG_LEDS 1

#define MAX_USER_TEXT 256
#define FONT_16 16
#define FONT_24 24


#define FIRST_T_CHAR 0x0C00    // Unicode start for Telugu characters
#define CHAR_COUNT 128       // Number of characters in font16x16
#define FONT_16 16           // Number of rows/columns in the bitmap


// Define a struct for storing the input's bitmap
typedef struct {
    uint8_t input_txt_length;      // Length of the input text
    uint16_t bitmaps[128][16];     // Maximum 128 characters, each with 16 rows of bitmap
} user_Telugu_input_16X16_bit_map;

typedef struct row {
    uint16_t st_leds[MAX_SHIFT_REG_LEDS];
} row;

typedef struct data_line {
    row st_rows[MAX_ROWS];
} data_line;

typedef struct single_ic_payload {
    data_line st_data_lines[MAX_DATA_LINES];
} single_ic_payload;


typedef struct user_input_16X16_bit_map {
    uint8_t input_txt_length;
    single_ic_payload  payload_batch[MAX_USER_TEXT];
} user_input_16X16_bit_map;

typedef struct device_ics {
    uint8_t ics_count;
    single_ic_payload st_ics[MAX_ICS];
}device_ics;





#endif
