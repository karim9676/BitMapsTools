#!/bin/bash

# Set the JAR filename
JAR_FILE="json-20210307.jar"

# Download the JSON library if not already present
if [ ! -f "$JAR_FILE" ]; then
    echo "Downloading $JAR_FILE..."
    wget -q -O $JAR_FILE "https://repo1.maven.org/maven2/org/json/json/20210307/json-20210307.jar"
    if [ $? -ne 0 ]; then
        echo "Failed to download $JAR_FILE. Check your internet connection."
        exit 1
    fi
    echo "Download completed."
else
    echo "$JAR_FILE already exists. Skipping download."
fi

# Compile the Java file
JAVA_FILE="BitmapGenerator.java"
if [ -f "$JAVA_FILE" ]; then
    echo "Compiling $JAVA_FILE..."
    javac -cp .:$JAR_FILE $JAVA_FILE
    if [ $? -eq 0 ]; then
        echo "Compilation successful."
    else
        echo "Compilation failed. Check for errors."
        exit 1
    fi
else
    echo "$JAVA_FILE not found! Place your Java file in the current directory."
    exit 1
fi

# Run the compiled Java class (assuming main method exists)
CLASS_NAME="BitmapGenerator"
echo "Running $CLASS_NAME..."
java -cp .:$JAR_FILE $CLASS_NAME

