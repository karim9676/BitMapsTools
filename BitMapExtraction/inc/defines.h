#ifndef __DEFINES_
#define __DEFINES_
	
#define PRINT_BINARY(value) do { \
    for (int i = sizeof(unsigned short) * 8 - 1; i >= 0; i--) { \
        printf("%c", (value & (1 << i)) ? '#' : '.'); \
    } \
    printf(" "); \
} while (0)


#endif //__DEFINES_
