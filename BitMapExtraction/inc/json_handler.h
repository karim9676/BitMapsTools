/*
 * json_handler.h
 *
 *  Created on: Jan 15, 2025
 *      Author: karim
 */

#ifndef INC_JSON_HANDLER_H_
#define INC_JSON_HANDLER_H_


#include "json.h"
/*----------------------------------------------------------------
  Macros for text sizes, bitmap sizes, etc.
----------------------------------------------------------------*/
#define MAX_TEXT_LENGTH         200
#define MIN_TEXT_LENGTH         50
#define MAX_TYPE_LENGTH         10
#define MAX_NUM_LENGTH          20
#define MAX_TRANSLATION_LENGTH  256
#define MAX_BITMAP_STRING_LENGTH 1024

/* A pool size for JSON tokens (adjust if needed) */
#define JSON_POOL_SIZE 512

/*----------------------------------------------------------------
  Structures for the “route” part.
  (The new JSON uses "routeNumber", "source", "destination", "via",
   "splitRoute", "routeNumber1" and "routeNumber2".)
----------------------------------------------------------------*/
typedef struct {
    char routeNumber[MIN_TEXT_LENGTH];
    char source[MAX_TEXT_LENGTH];
    char destination[MAX_TEXT_LENGTH];
    char via[MAX_TEXT_LENGTH];
    int  splitRoute;         // 0 or 1
    char routeNumber1[MAX_NUM_LENGTH]; // e.g. upper half route number
    char routeNumber2[MAX_NUM_LENGTH]; // e.g. lower half route number
} RouteInfo;

/*----------------------------------------------------------------
  Structures for the bitmap information.
----------------------------------------------------------------*/
typedef struct {
    char bitmap[MAX_BITMAP_STRING_LENGTH];
    int width;
    int height;
} Bitmap;

typedef struct {
    Bitmap en;
    Bitmap te;
} Bitmaps;

/*----------------------------------------------------------------
  Structures for the translations.
----------------------------------------------------------------*/
typedef struct {
    char en[MAX_TRANSLATION_LENGTH];
    char te[MAX_TRANSLATION_LENGTH];
} Translations;

/*----------------------------------------------------------------
  Structure for display parameters of a text item.
----------------------------------------------------------------*/
typedef struct {
    char scrollType[32];
    char position[32];
    int scrollSpeed;
} TextDisplay;

/*----------------------------------------------------------------
  Structure for a text item that holds translations, bitmaps, and
  display parameters.
----------------------------------------------------------------*/
typedef struct {
    Translations translations;
    Bitmaps bitmaps;
    TextDisplay display;
} TextItem;

/*----------------------------------------------------------------
  Structures for the screens.
  For screens with format "single" (front, side, internal) the texts
  object contains a single text item (with key "text").
  For the "rear" screen (format "three") the texts object contains three
  text items: "sideText", "upperHalfText", and "lowerHalfText".
----------------------------------------------------------------*/
typedef struct {
    char format[16];  // e.g. "single"
    TextItem text;
} SingleScreen;

typedef struct {
    char format[16];  // e.g. "three"
    TextItem sideText;
    TextItem upperHalfText;
    TextItem lowerHalfText;
} ThreeScreen;

/*----------------------------------------------------------------
  Display configuration: a set of screens.
----------------------------------------------------------------*/
typedef struct {
    SingleScreen front;
    SingleScreen side;
    ThreeScreen  rear;
    SingleScreen internal;
} DisplayConfig;

/*----------------------------------------------------------------
  Complete data for one element: the route and the display configuration.
----------------------------------------------------------------*/
typedef struct {
    RouteInfo route;
    DisplayConfig displayConfig;
} BusData;

/*----------------------------------------------------------------
  Top-level configuration: an array of BusData.
----------------------------------------------------------------*/
typedef struct {
    BusData *items;
    size_t count;
} Config;


int base64_index(char c);
void base64_decode(const char *input, unsigned char *output, int *out_len);
void decode_bitmap_string(const char *bitmap_str, int width, int height);
void debugPrintConfig(const Config *config);
void debugPrintBusData(const BusData *data);
void debugPrintDisplayConfig(const DisplayConfig *dc);
void debugPrintThreeScreen(const char *screenName, const ThreeScreen *screen);
void debugPrintSingleScreen(const char *screenName, const SingleScreen *screen);
void debugPrintTextItem(const TextItem *item);
int parseRouteJSON(const char *path, const char *file, Config *config);
int parseDisplayConfig(const json_t *screensJson, DisplayConfig *dc);
int parseThreeScreen(const json_t *screenJson, ThreeScreen *screen);
int parseTextItem(const json_t *textJson, TextItem *item);
char* Read_File(const char *path, const char *file);

#endif

