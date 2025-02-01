# BitMapExtraction


Bus Data JSON Parser and Bitmap Decoder

# compile  make .
# Run the code ./build/show_bitmap <path_for_json file> <json file name>
 
This project is a C-based parser that reads a custom JSON file containing bus route data and display configuration details, decodes Base64-encoded bitmap strings into 16-bit arrays, and prints all parsed information (including a graphical and hexadecimal representation of the bitmaps) for debugging purposes.
Overview

The JSON file is expected to contain an array of objects. Each object holds:

    A route object with fields such as routeNumber, source, destination, via, splitRoute, routeNumber1, and routeNumber2.
    A displayConfig object with a nested screens object that defines different screen configurations:
        front, side, and internal screens (using the "single" format)
        A rear screen (using the "three" format) that includes multiple text items

Each text item contains:

    translations: The text for different languages (e.g., "en" and "te").
    bitmaps: A Base64-encoded bitmap string (which may contain comma-separated values), along with width and height for the bitmap.
    display: Display parameters including scrollType, position, and scrollSpeed.

The project uses the tiny-json library for JSON parsing.
Features

    JSON Parsing:
    Uses the tiny-json library to parse the input JSON file into custom C structures representing route data and display configuration.

    Bitmap Extraction:
    Includes Base64 decoding functions to convert encoded bitmap strings into 16-bit arrays. The decoded bitmap data is printed both in a graphical format (using # for set bits) and in hexadecimal format.

    Debug Output:
    Comprehensive debug functions print all parsed details—including route information, display configuration, translations, and bitmap Extraction output—to the console.

Files

    main.c
    Contains the JSON parsing logic, bitmap Extraction functions, and debug print functions.

    json.c and json.h
    The tiny-json implementation used for parsing the JSON file.

    README.md
    This file.

Requirements

    A C compiler (e.g., gcc).
    Standard C libraries.
    The tiny-json library (included in the project).

Compilation

Compile the project using a command similar to the following:

gcc -o busdata main.c json.c -std=c99 -Wall -Wextra

Adjust file names and compiler options as needed.
Running the Application

After compilation, run the executable and provide the path to your JSON file. For example:

./busdata path/to/json data.json

The program will:

    Read and parse the JSON file.
    Decode Base64-encoded bitmap strings into 16-bit arrays.
    Print all parsed data along with a graphical (using #) and hexadecimal representation of the bitmaps.

JSON Format

Below is an example snippet of the expected JSON structure:

[
  {
    "route": {
      "routeNumber": "555",
      "source": "delhi",
      "destination": "pune",
      "via": "nagpur,shiridi",
      "splitRoute": false,
      "routeNumber1": "",
      "routeNumber2": ""
    },
    "displayConfig": {
      "screens": {
        "front": {
          "format": "single",
          "texts": {
            "text": {
              "translations": {
                "en": "555 - DELHI - PUNE",
                "te": "555 - ఢిల్లీ - పూణే"
              },
              "bitmaps": {
                "en": {
                  "bitmap": "Base64EncodedString...",
                  "width": 144,
                  "height": 12
                },
                "te": {
                  "bitmap": "Base64EncodedString...",
                  "width": 114,
                  "height": 20
                }
              },
              "display": {
                "scrollType": "right-to-left",
                "position": "center",
                "scrollSpeed": 4
              }
            }
          }
        },
        "side": {
          "format": "single",
          "texts": {
            "text": {
              "translations": {
                "en": "DELHI - PUNE",
                "te": "ఢిల్లీ - పూణే"
              },
              "bitmaps": {
                "en": { /* ... */ },
                "te": { /* ... */ }
              },
              "display": { /* ... */ }
            }
          }
        },
        "rear": {
          "format": "three",
          "texts": {
            "sideText": { /* ... */ },
            "upperHalfText": { /* ... */ },
            "lowerHalfText": { /* ... */ }
          }
        },
        "internal": {
          "format": "single",
          "texts": {
            "text": { /* ... */ }
          }
        }
      }
    }
  }
]

Each text object should include:

    translations: An object with keys "en" and "te".
    bitmaps: An object with keys "en" and "te". Each contains a Base64 string (bitmap), a width, and a height.
    display: An object with scrollType, position, and scrollSpeed.

Bitmap Extraction

The project extends the parsing to decode bitmap strings:

    Base64 Decoding:
    The provided functions decode a Base64-encoded string into a byte array.

    16-bit Array Conversion:
    The function decode_bitmap_string():
        Splits a comma-separated bitmap string into rows.
        Decodes each row into bytes.
        Groups every two bytes into a 16-bit value.
        Prints a graphical representation (using # for set bits) and the corresponding hexadecimal values.

Debugging

After parsing, call the function debugPrintConfig() to print all parsed details including route information, display configurations, and the decoded bitmap data.
License

This project is licensed under the MIT License.
Acknowledgments

    tiny-json for the lightweight JSON parser.
    Base64 decoding inspired by common implementations in C.
