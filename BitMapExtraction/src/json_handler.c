#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "json.h"  // your tiny-json header
#include "json_handler.h"
/*----------------------------------------------------------------
  File I/O helper: read the entire file into a buffer.
----------------------------------------------------------------*/
char* Read_File(const char *path, const char *file) {
    char fullPath[512];
    snprintf(fullPath, sizeof(fullPath), "%s/%s", path, file);

    FILE *fp = fopen(fullPath, "r");
    if (!fp) {
        perror("Failed to open file");
        return NULL;
    }

    fseek(fp, 0, SEEK_END);
    long fileSize = ftell(fp);
    rewind(fp);

    char *buffer = (char *)malloc(fileSize + 1);
    if (!buffer) {
        perror("Failed to allocate memory");
        fclose(fp);
        return NULL;
    }

    fread(buffer, 1, fileSize, fp);
    buffer[fileSize] = '\0';

    fclose(fp);
    return buffer;
}

/*----------------------------------------------------------------
  Helper: Parse a text item.
  This function parses the JSON object corresponding to a text item.
  It expects the following keys:
    - "translations": an object with keys "en" and "te"
    - "bitmaps": an object with keys "en" and "te". Each is an object
       with "bitmap", "width", and "height".
    - "display": an object with "scrollType", "position", and "scrollSpeed"
----------------------------------------------------------------*/
int parseTextItem(const json_t *textJson, TextItem *item) {
    if (!textJson || !item) return -1;

    const json_t *temp;
    const char *value;

    /* Parse translations */
    temp = json_getProperty(textJson, "translations");
    if (temp) {
        value = json_getPropertyValue(temp, "en");
        strncpy(item->translations.en, value ? value : "", MAX_TRANSLATION_LENGTH);
        value = json_getPropertyValue(temp, "te");
        strncpy(item->translations.te, value ? value : "", MAX_TRANSLATION_LENGTH);
    } else {
        item->translations.en[0] = '\0';
        item->translations.te[0] = '\0';
    }

    /* Parse bitmaps */
    temp = json_getProperty(textJson, "bitmaps");
    if (temp) {
        const json_t *bm_en = json_getProperty(temp, "en");
        if (bm_en) {
            value = json_getPropertyValue(bm_en, "bitmap");
            strncpy(item->bitmaps.en.bitmap, value ? value : "", MAX_BITMAP_STRING_LENGTH);
            value = json_getPropertyValue(bm_en, "width");
            item->bitmaps.en.width = value ? atoi(value) : 0;
            value = json_getPropertyValue(bm_en, "height");
            item->bitmaps.en.height = value ? atoi(value) : 0;
        } else {
            item->bitmaps.en.bitmap[0] = '\0';
            item->bitmaps.en.width = item->bitmaps.en.height = 0;
        }
        const json_t *bm_te = json_getProperty(temp, "te");
        if (bm_te) {
            value = json_getPropertyValue(bm_te, "bitmap");
            strncpy(item->bitmaps.te.bitmap, value ? value : "", MAX_BITMAP_STRING_LENGTH);
            value = json_getPropertyValue(bm_te, "width");
            item->bitmaps.te.width = value ? atoi(value) : 0;
            value = json_getPropertyValue(bm_te, "height");
            item->bitmaps.te.height = value ? atoi(value) : 0;
        } else {
            item->bitmaps.te.bitmap[0] = '\0';
            item->bitmaps.te.width = item->bitmaps.te.height = 0;
        }
    } else {
        item->bitmaps.en.bitmap[0] = item->bitmaps.te.bitmap[0] = '\0';
        item->bitmaps.en.width = item->bitmaps.en.height = 0;
        item->bitmaps.te.width = item->bitmaps.te.height = 0;
    }

    /* Parse display parameters */
    temp = json_getProperty(textJson, "display");
    if (temp) {
        value = json_getPropertyValue(temp, "scrollType");
        strncpy(item->display.scrollType, value ? value : "", sizeof(item->display.scrollType));
        value = json_getPropertyValue(temp, "position");
        strncpy(item->display.position, value ? value : "", sizeof(item->display.position));
        value = json_getPropertyValue(temp, "scrollSpeed");
        item->display.scrollSpeed = value ? atoi(value) : 0;
    } else {
        item->display.scrollType[0] = '\0';
        item->display.position[0] = '\0';
        item->display.scrollSpeed = 0;
    }

    return 0;
}

/*----------------------------------------------------------------
  Helper: Parse a screen with format "single" (front, side, internal).
  The JSON object is expected to have:
    - "format": a string (should be "single")
    - "texts": an object with a key "text" whose value is a text item.
----------------------------------------------------------------*/
int parseSingleScreen(const json_t *screenJson, SingleScreen *screen) {
    if (!screenJson || !screen) return -1;
    const char *value = json_getPropertyValue(screenJson, "format");
    strncpy(screen->format, value ? value : "", sizeof(screen->format));

    const json_t *texts = json_getProperty(screenJson, "texts");
    if (texts) {
        const json_t *text = json_getProperty(texts, "text");
        if (text) {
            parseTextItem(text, &screen->text);
        }
    }
    return 0;
}

/*----------------------------------------------------------------
  Helper: Parse a screen with format "three" (rear).
  The JSON object is expected to have:
    - "format": a string (should be "three")
    - "texts": an object with keys "sideText", "upperHalfText", and "lowerHalfText"
      each containing a text item.
----------------------------------------------------------------*/
int parseThreeScreen(const json_t *screenJson, ThreeScreen *screen) {
    if (!screenJson || !screen) return -1;
    const char *value = json_getPropertyValue(screenJson, "format");
    strncpy(screen->format, value ? value : "", sizeof(screen->format));

    const json_t *texts = json_getProperty(screenJson, "texts");
    if (texts) {
        const json_t *temp = json_getProperty(texts, "sideText");
        if (temp) parseTextItem(temp, &screen->sideText);
        temp = json_getProperty(texts, "upperHalfText");
        if (temp) parseTextItem(temp, &screen->upperHalfText);
        temp = json_getProperty(texts, "lowerHalfText");
        if (temp) parseTextItem(temp, &screen->lowerHalfText);
    }
    return 0;
}

/*----------------------------------------------------------------
  Helper: Parse the display configuration.
  The JSON object passed in is the value of the "screens" key.
  It contains objects for "front", "side", "rear", and "internal".
----------------------------------------------------------------*/
int parseDisplayConfig(const json_t *screensJson, DisplayConfig *dc) {
    if (!screensJson || !dc) return -1;

    const json_t *temp = json_getProperty(screensJson, "front");
    if (temp) parseSingleScreen(temp, &dc->front);

    temp = json_getProperty(screensJson, "side");
    if (temp) parseSingleScreen(temp, &dc->side);

    temp = json_getProperty(screensJson, "rear");
    if (temp) parseThreeScreen(temp, &dc->rear);

    temp = json_getProperty(screensJson, "internal");
    if (temp) parseSingleScreen(temp, &dc->internal);

    return 0;
}

/*----------------------------------------------------------------
  Top-level parsing function.
  It reads a JSON file whose top-level is an array of objects.
  Each object has a "route" object and a "displayConfig" object.
----------------------------------------------------------------*/
int parseRouteJSON(const char *path, const char *file, Config *config) {
    printf("Loading JSON data from file: %s/%s\n", path, file);
    char *jsonBuffer = Read_File(path, file);
    if (!jsonBuffer) {
        printf("Failed to read the file.\n");
        return -1;
    }

    /* Create a static pool for JSON parsing. */
    json_t jsonPool[JSON_POOL_SIZE];
    const json_t *root = json_create(jsonBuffer, jsonPool, JSON_POOL_SIZE);
    if (!root) {
        printf("Failed to parse JSON data.\n");
        free(jsonBuffer);
        return -1;
    }

    /* Count the number of items in the top-level array */
    size_t count = 0;
    const json_t *child = root->u.c.child;
    while (child) {
        count++;
        child = child->sibling;
    }
    config->count = count;
    config->items = (BusData *)malloc(sizeof(BusData) * count);
    if (!config->items) {
        perror("Failed to allocate memory for items");
        free(jsonBuffer);
        return -1;
    }

    /* Iterate over each element in the array */
    size_t i = 0;
    child = root->u.c.child;
    while (child) {
        BusData *data = &config->items[i];
        const json_t *temp;
        const char *value;

        /* Parse the "route" object */
        temp = json_getProperty(child, "route");
        if (temp) {
            value = json_getPropertyValue(temp, "routeNumber");
            strncpy(data->route.routeNumber, value ? value : "", MIN_TEXT_LENGTH);

            value = json_getPropertyValue(temp, "source");
            strncpy(data->route.source, value ? value : "", MAX_TEXT_LENGTH);

            value = json_getPropertyValue(temp, "destination");
            strncpy(data->route.destination, value ? value : "", MAX_TEXT_LENGTH);

            value = json_getPropertyValue(temp, "via");
            strncpy(data->route.via, value ? value : "", MAX_TEXT_LENGTH);

            value = json_getPropertyValue(temp, "splitRoute");
            data->route.splitRoute = (value && strcmp(value, "true") == 0) ? 1 : 0;

            value = json_getPropertyValue(temp, "routeNumber1");
            strncpy(data->route.routeNumber1, value ? value : "", MAX_NUM_LENGTH);

            value = json_getPropertyValue(temp, "routeNumber2");
            strncpy(data->route.routeNumber2, value ? value : "", MAX_NUM_LENGTH);
        }

        /* Parse the "displayConfig" object */
        temp = json_getProperty(child, "displayConfig");
        if (temp) {
            const json_t *screens = json_getProperty(temp, "screens");
            if (screens) {
                parseDisplayConfig(screens, &data->displayConfig);
            }
        }

        i++;
        child = child->sibling;
    }

    free(jsonBuffer);
    return 0;
}
/* Debug: print a TextItem and decode its bitmaps */
void debugPrintTextItemWithBitmap(const TextItem *item) {
    if (!item) return;
    printf("  Translations:\n");
    printf("    en: %s\n", item->translations.en);
    printf("    te: %s\n", item->translations.te);
    
    printf("  Bitmap (en):\n");
    printf("    Base64 string: %s\n", item->bitmaps.en.bitmap);
    printf("    Width: %d, Height: %d\n", item->bitmaps.en.width, item->bitmaps.en.height);
    // Decode and print the 16-bit array representation.
    decode_bitmap_string(item->bitmaps.en.bitmap, item->bitmaps.en.width, item->bitmaps.en.height);
    
    printf("  Bitmap (te):\n");
    printf("    Base64 string: %s\n", item->bitmaps.te.bitmap);
    printf("    Width: %d, Height: %d\n", item->bitmaps.te.width, item->bitmaps.te.height);
    decode_bitmap_string(item->bitmaps.te.bitmap, item->bitmaps.te.width, item->bitmaps.te.height);
    
    printf("  Display:\n");
    printf("    scrollType: %s\n", item->display.scrollType);
    printf("    position: %s\n", item->display.position);
    printf("    scrollSpeed: %d\n", item->display.scrollSpeed);
}

/* Debug: print a single screen (for front, side, internal) */
void debugPrintSingleScreen(const char *screenName, const SingleScreen *screen) {
    if (!screen) return;
    printf("%s Screen:\n", screenName);
    printf("  Format: %s\n", screen->format);
    debugPrintTextItemWithBitmap(&screen->text);
}

/* Debug: print a three-screen (for rear) */
void debugPrintThreeScreen(const char *screenName, const ThreeScreen *screen) {
    if (!screen) return;
    printf("%s Screen:\n", screenName);
    printf("  Format: %s\n", screen->format);
    printf("  sideText:\n");
    debugPrintTextItemWithBitmap(&screen->sideText);
    printf("  upperHalfText:\n");
    debugPrintTextItemWithBitmap(&screen->upperHalfText);
    printf("  lowerHalfText:\n");
    debugPrintTextItemWithBitmap(&screen->lowerHalfText);
}

/* Debug: print the DisplayConfig */
void debugPrintDisplayConfig(const DisplayConfig *dc) {
    if (!dc) return;
    printf("Display Configuration:\n");
    debugPrintSingleScreen("  Front", &dc->front);
    debugPrintSingleScreen("  Side", &dc->side);
    debugPrintThreeScreen("  Rear", &dc->rear);
    debugPrintSingleScreen("  Internal", &dc->internal);
}

/* Debug: print a BusData element (route and display config) */
void debugPrintBusData(const BusData *data) {
    if (!data) return;
    printf("====================================\n");
    printf("Route:\n");
    printf("  Route Number: %s\n", data->route.routeNumber);
    printf("  Source: %s\n", data->route.source);
    printf("  Destination: %s\n", data->route.destination);
    printf("  Via: %s\n", data->route.via);
    printf("  Split Route: %d\n", data->route.splitRoute);
    printf("  Route Number 1: %s\n", data->route.routeNumber1);
    printf("  Route Number 2: %s\n", data->route.routeNumber2);
    
    debugPrintDisplayConfig(&data->displayConfig);
    printf("====================================\n\n");
}

/* Debug: print the entire configuration */
void debugPrintConfig(const Config *config) {
    if (!config) return;
    printf("Total items: %zu\n\n", config->count);
    for (size_t i = 0; i < config->count; i++) {
        printf("==== Bus Data Item %zu ====\n", i + 1);
        debugPrintBusData(&config->items[i]);
    }
}

