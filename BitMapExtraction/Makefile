# Compiler and flags
CC = gcc
CFLAGS = -Wall -Wextra -Iinc -g -I/usr/include/freetype2 -I/usr/include/libpng16


# Directories
SRC_DIR = src
INC_DIR = inc
BUILD_DIR = build
LDFLAGS = -lfreetype

# Files
SRCS = $(wildcard $(SRC_DIR)/*.c)
OBJS = $(SRCS:$(SRC_DIR)/%.c=$(BUILD_DIR)/%.o)
TARGET = $(BUILD_DIR)/show_bitmap


# Rules
all: $(TARGET)

$(BUILD_DIR):
	mkdir -p $(BUILD_DIR)

$(BUILD_DIR)/%.o: $(SRC_DIR)/%.c | $(BUILD_DIR)
	$(CC) $(CFLAGS) -c $< -o $@

$(TARGET): $(OBJS)
	$(CC) $(CFLAGS) $^ -o $@ $(LDFLAGS)

clean:
	rm -rf $(BUILD_DIR)

.PHONY: all clean

