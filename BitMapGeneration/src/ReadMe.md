BitmapGenerator
Overview

BitmapGenerator is a Java utility that converts a given text string into a bitmap representation using a specified TrueType font. The program renders the text onto a binary image, processes the image data into rows of 8-bit chunks, encodes each row in Base64, and finally outputs a JSON object containing the bitmap data along with the image dimensions.

This tool is especially useful for applications such as embedded systems or any environment where you need a compact, pre-rendered bitmap representation of text.
Features

    Font Loading: Loads a TrueType font from a file.
    Dynamic Text Rendering: Renders custom text at a specified font size.
    Bitmap Processing: Converts rendered text into a binary image.
    Base64 Encoding: Encodes each row of the bitmap as a Base64 string.
    JSON Output: Outputs the bitmap data along with its width and the number of rows with data (height) as a JSON object.

Prerequisites

    Java Development Kit (JDK): Version 8 or later.
    JSON Library: The program uses the org.json library. You can download it from Maven Central or include it via your build tool (e.g., Maven, Gradle).

Directory Structure

The program expects font files to be located in a ../fonts/ directory relative to the compiled BitmapGenerator class. For example:

BitmapGeneration/
├── BitmapGenerator.java
├── fonts/
│   └── YourFontFile.ttf
└── README.md

Note: When running the program, specify the font file name without the .ttf extension.
Compilation

    Download the JSON Library:
        For example, download json-20210307.jar and place it in your project directory.

    Compile the Program:

    Open a terminal in the project directory and run:

javac -cp .:json-20210307.jar BitmapGenerator.java

    Tip for Windows Users: Replace : with ; in the classpath:

        javac -cp .;json-20210307.jar BitmapGenerator.java

Usage

Run the program using the following command:

java -cp .:json-20210307.jar BitmapGenerator <text> <fontFile> <size>

Where:

    <text>: The text string to render (e.g., "Hello, World!").
    <fontFile>: The name of the font file (without the .ttf extension) located in the ../fonts/ directory (e.g., Roboto).
    <size>: The font size (an integer) used for rendering the text (e.g., 14).

Example

If you have a font file named Roboto.ttf in the ../fonts/ directory, run:

java -cp .:json-20210307.jar BitmapGenerator "Hello, World!" Roboto 14

The output will be a JSON object similar to:

{
    "bitmap": [
        "Base64EncodedRow1",
        "Base64EncodedRow2",
        "... more rows ..."
    ],
    "height": 10,
    "width": 80
}

    bitmap: An array where each element is a Base64-encoded string representing one row of the bitmap that contains at least one non-zero (white) pixel.
    height: The number of rows with non-zero pixels.
    width: The width of the bitmap in pixels.

Code Explanation

    getFontPath(String fileName)
        Constructs the relative path for the given font file (assumes fonts are in ../fonts/).

    generateBitmapToHeader(String text, String fontFile, int size)
        Loads the specified TrueType font and derives it at the desired size.
        Creates a dummy image to calculate the dimensions of the rendered text.
        Renders the text onto a binary image and processes each row into 8-bit chunks.
        Converts each row's data to a Base64-encoded string.
        Collects only the rows that contain at least one non-zero (white) pixel and stores them in a JSON array.
        Constructs and returns a JSON object containing the bitmap data, height, and width.

    main(String[] args)
        Parses command-line arguments.
        Invokes generateBitmapToHeader to create the bitmap data.
        Prints the resulting JSON string.

Troubleshooting

    Font Loading Issues: Ensure that the font file is present in the ../fonts/ directory and that you specify the correct file name (without the .ttf extension).
    Classpath Errors: Confirm that the org.json library is correctly referenced during both compilation and execution.
    Incorrect Arguments: The program expects exactly three arguments (text, fontFile, and size). Running it with fewer arguments will display usage instructions.

License

This project is open source. You are free to modify and use it as needed.
