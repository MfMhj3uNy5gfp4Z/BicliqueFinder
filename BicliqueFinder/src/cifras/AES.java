/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cifras;

import core.Cipher;
import core.ByteArray;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ´Gabriel
 */
public class AES extends Cipher{
    public static final String NAME = "AES";
    public static final int NUM_ROUNDS = 10;
    public static final int NUM_STATES = 41;
    public static final int NUM_KEYS = 11;
    public static final int KEY_SIZE_IN_BYTES = 16;
    public static final int ROUND_KEY_SIZE_IN_BYTES = 16;
    public static final int BLOCK_SIZE_IN_BYTES = 16;

    public static final int WORD_SIZE = 8;
    private static final int NUM_BYTES_IN_256_BITS = 256 / Byte.SIZE;
    private static final int NUM_BYTES_IN_192_BITS = 192 / Byte.SIZE;
    private static final int NUM_BYTES_IN_128_BITS = 128 / Byte.SIZE;
    private static final int EXPANDED_KEY_SIZE = NUM_ROUNDS * NUM_BYTES_IN_128_BITS;
    	
    private static final int[] INVERSE_SBOX = { // inverse s-box
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb, 0x7c, 0xe3, 0x39,
            0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb, 0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2,
            0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e, 0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76,
            0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25, 0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc,
            0x5d, 0x65, 0xb6, 0x92, 0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d,
            0x84, 0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06, 0xd0, 0x2c,
            0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b, 0x3a, 0x91, 0x11, 0x41, 0x4f,
            0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73, 0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85,
            0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e, 0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62,
            0x0e, 0xaa, 0x18, 0xbe, 0x1b, 0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd,
            0x5a, 0xf4, 0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f, 0x60,
            0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef, 0xa0, 0xe0, 0x3b, 0x4d,
            0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61, 0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6,
            0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d};
    private static final int[] RCON = {0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36, 0x6c, 0xd8, 0xab, 0x4d, 0x9a};

    private static final int[] SBOX = { // forward s-box
            // 0   1	  2     3    4     5     6     7     8     9	  A    B     C     D     E     F
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76, // 0
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0, // 1
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15, // 2
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75, // 3
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84, // 4
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf, // 5
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8, // 6
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2, // 7
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73, // 8
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb, // 9
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79, // A
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08, // B
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a, // C
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e, // D
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf, // E
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16  // F
    };
    // modular multiplication tables
    // based on:
    // Xtime2[x] = (x & 0x80 ? 0x1b : 0) ^ (x + x)
    // Xtime3[x] = x^Xtime2[x];
    private static final int[] X_TIMES_2 = {0x00, 0x02, 0x04, 0x06, 0x08, 0x0a, 0x0c, 0x0e, 0x10, 0x12, 0x14, 0x16,
            0x18, 0x1a, 0x1c, 0x1e, 0x20, 0x22, 0x24, 0x26, 0x28, 0x2a, 0x2c, 0x2e, 0x30, 0x32, 0x34, 0x36, 0x38, 0x3a, 0x3c,
            0x3e, 0x40, 0x42, 0x44, 0x46, 0x48, 0x4a, 0x4c, 0x4e, 0x50, 0x52, 0x54, 0x56, 0x58, 0x5a, 0x5c, 0x5e, 0x60, 0x62,
            0x64, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x74, 0x76, 0x78, 0x7a, 0x7c, 0x7e, 0x80, 0x82, 0x84, 0x86, 0x88,
            0x8a, 0x8c, 0x8e, 0x90, 0x92, 0x94, 0x96, 0x98, 0x9a, 0x9c, 0x9e, 0xa0, 0xa2, 0xa4, 0xa6, 0xa8, 0xaa, 0xac, 0xae,
            0xb0, 0xb2, 0xb4, 0xb6, 0xb8, 0xba, 0xbc, 0xbe, 0xc0, 0xc2, 0xc4, 0xc6, 0xc8, 0xca, 0xcc, 0xce, 0xd0, 0xd2, 0xd4,
            0xd6, 0xd8, 0xda, 0xdc, 0xde, 0xe0, 0xe2, 0xe4, 0xe6, 0xe8, 0xea, 0xec, 0xee, 0xf0, 0xf2, 0xf4, 0xf6, 0xf8, 0xfa,
            0xfc, 0xfe, 0x1b, 0x19, 0x1f, 0x1d, 0x13, 0x11, 0x17, 0x15, 0x0b, 0x09, 0x0f, 0x0d, 0x03, 0x01, 0x07, 0x05, 0x3b,
            0x39, 0x3f, 0x3d, 0x33, 0x31, 0x37, 0x35, 0x2b, 0x29, 0x2f, 0x2d, 0x23, 0x21, 0x27, 0x25, 0x5b, 0x59, 0x5f, 0x5d,
            0x53, 0x51, 0x57, 0x55, 0x4b, 0x49, 0x4f, 0x4d, 0x43, 0x41, 0x47, 0x45, 0x7b, 0x79, 0x7f, 0x7d, 0x73, 0x71, 0x77,
            0x75, 0x6b, 0x69, 0x6f, 0x6d, 0x63, 0x61, 0x67, 0x65, 0x9b, 0x99, 0x9f, 0x9d, 0x93, 0x91, 0x97, 0x95, 0x8b, 0x89,
            0x8f, 0x8d, 0x83, 0x81, 0x87, 0x85, 0xbb, 0xb9, 0xbf, 0xbd, 0xb3, 0xb1, 0xb7, 0xb5, 0xab, 0xa9, 0xaf, 0xad, 0xa3,
            0xa1, 0xa7, 0xa5, 0xdb, 0xd9, 0xdf, 0xdd, 0xd3, 0xd1, 0xd7, 0xd5, 0xcb, 0xc9, 0xcf, 0xcd, 0xc3, 0xc1, 0xc7, 0xc5,
            0xfb, 0xf9, 0xff, 0xfd, 0xf3, 0xf1, 0xf7, 0xf5, 0xeb, 0xe9, 0xef, 0xed, 0xe3, 0xe1, 0xe7, 0xe5};
    private static final int[] X_TIMES_3 = {0x00, 0x03, 0x06, 0x05, 0x0c, 0x0f, 0x0a, 0x09, 0x18, 0x1b, 0x1e, 0x1d,
            0x14, 0x17, 0x12, 0x11, 0x30, 0x33, 0x36, 0x35, 0x3c, 0x3f, 0x3a, 0x39, 0x28, 0x2b, 0x2e, 0x2d, 0x24, 0x27, 0x22,
            0x21, 0x60, 0x63, 0x66, 0x65, 0x6c, 0x6f, 0x6a, 0x69, 0x78, 0x7b, 0x7e, 0x7d, 0x74, 0x77, 0x72, 0x71, 0x50, 0x53,
            0x56, 0x55, 0x5c, 0x5f, 0x5a, 0x59, 0x48, 0x4b, 0x4e, 0x4d, 0x44, 0x47, 0x42, 0x41, 0xc0, 0xc3, 0xc6, 0xc5, 0xcc,
            0xcf, 0xca, 0xc9, 0xd8, 0xdb, 0xde, 0xdd, 0xd4, 0xd7, 0xd2, 0xd1, 0xf0, 0xf3, 0xf6, 0xf5, 0xfc, 0xff, 0xfa, 0xf9,
            0xe8, 0xeb, 0xee, 0xed, 0xe4, 0xe7, 0xe2, 0xe1, 0xa0, 0xa3, 0xa6, 0xa5, 0xac, 0xaf, 0xaa, 0xa9, 0xb8, 0xbb, 0xbe,
            0xbd, 0xb4, 0xb7, 0xb2, 0xb1, 0x90, 0x93, 0x96, 0x95, 0x9c, 0x9f, 0x9a, 0x99, 0x88, 0x8b, 0x8e, 0x8d, 0x84, 0x87,
            0x82, 0x81, 0x9b, 0x98, 0x9d, 0x9e, 0x97, 0x94, 0x91, 0x92, 0x83, 0x80, 0x85, 0x86, 0x8f, 0x8c, 0x89, 0x8a, 0xab,
            0xa8, 0xad, 0xae, 0xa7, 0xa4, 0xa1, 0xa2, 0xb3, 0xb0, 0xb5, 0xb6, 0xbf, 0xbc, 0xb9, 0xba, 0xfb, 0xf8, 0xfd, 0xfe,
            0xf7, 0xf4, 0xf1, 0xf2, 0xe3, 0xe0, 0xe5, 0xe6, 0xef, 0xec, 0xe9, 0xea, 0xcb, 0xc8, 0xcd, 0xce, 0xc7, 0xc4, 0xc1,
            0xc2, 0xd3, 0xd0, 0xd5, 0xd6, 0xdf, 0xdc, 0xd9, 0xda, 0x5b, 0x58, 0x5d, 0x5e, 0x57, 0x54, 0x51, 0x52, 0x43, 0x40,
            0x45, 0x46, 0x4f, 0x4c, 0x49, 0x4a, 0x6b, 0x68, 0x6d, 0x6e, 0x67, 0x64, 0x61, 0x62, 0x73, 0x70, 0x75, 0x76, 0x7f,
            0x7c, 0x79, 0x7a, 0x3b, 0x38, 0x3d, 0x3e, 0x37, 0x34, 0x31, 0x32, 0x23, 0x20, 0x25, 0x26, 0x2f, 0x2c, 0x29, 0x2a,
            0x0b, 0x08, 0x0d, 0x0e, 0x07, 0x04, 0x01, 0x02, 0x13, 0x10, 0x15, 0x16, 0x1f, 0x1c, 0x19, 0x1a};
    private static final int[] X_TIMES_9 = {0x00, 0x09, 0x12, 0x1b, 0x24, 0x2d, 0x36, 0x3f, 0x48, 0x41, 0x5a, 0x53,
            0x6c, 0x65, 0x7e, 0x77, 0x90, 0x99, 0x82, 0x8b, 0xb4, 0xbd, 0xa6, 0xaf, 0xd8, 0xd1, 0xca, 0xc3, 0xfc, 0xf5, 0xee,
            0xe7, 0x3b, 0x32, 0x29, 0x20, 0x1f, 0x16, 0x0d, 0x04, 0x73, 0x7a, 0x61, 0x68, 0x57, 0x5e, 0x45, 0x4c, 0xab, 0xa2,
            0xb9, 0xb0, 0x8f, 0x86, 0x9d, 0x94, 0xe3, 0xea, 0xf1, 0xf8, 0xc7, 0xce, 0xd5, 0xdc, 0x76, 0x7f, 0x64, 0x6d, 0x52,
            0x5b, 0x40, 0x49, 0x3e, 0x37, 0x2c, 0x25, 0x1a, 0x13, 0x08, 0x01, 0xe6, 0xef, 0xf4, 0xfd, 0xc2, 0xcb, 0xd0, 0xd9,
            0xae, 0xa7, 0xbc, 0xb5, 0x8a, 0x83, 0x98, 0x91, 0x4d, 0x44, 0x5f, 0x56, 0x69, 0x60, 0x7b, 0x72, 0x05, 0x0c, 0x17,
            0x1e, 0x21, 0x28, 0x33, 0x3a, 0xdd, 0xd4, 0xcf, 0xc6, 0xf9, 0xf0, 0xeb, 0xe2, 0x95, 0x9c, 0x87, 0x8e, 0xb1, 0xb8,
            0xa3, 0xaa, 0xec, 0xe5, 0xfe, 0xf7, 0xc8, 0xc1, 0xda, 0xd3, 0xa4, 0xad, 0xb6, 0xbf, 0x80, 0x89, 0x92, 0x9b, 0x7c,
            0x75, 0x6e, 0x67, 0x58, 0x51, 0x4a, 0x43, 0x34, 0x3d, 0x26, 0x2f, 0x10, 0x19, 0x02, 0x0b, 0xd7, 0xde, 0xc5, 0xcc,
            0xf3, 0xfa, 0xe1, 0xe8, 0x9f, 0x96, 0x8d, 0x84, 0xbb, 0xb2, 0xa9, 0xa0, 0x47, 0x4e, 0x55, 0x5c, 0x63, 0x6a, 0x71,
            0x78, 0x0f, 0x06, 0x1d, 0x14, 0x2b, 0x22, 0x39, 0x30, 0x9a, 0x93, 0x88, 0x81, 0xbe, 0xb7, 0xac, 0xa5, 0xd2, 0xdb,
            0xc0, 0xc9, 0xf6, 0xff, 0xe4, 0xed, 0x0a, 0x03, 0x18, 0x11, 0x2e, 0x27, 0x3c, 0x35, 0x42, 0x4b, 0x50, 0x59, 0x66,
            0x6f, 0x74, 0x7d, 0xa1, 0xa8, 0xb3, 0xba, 0x85, 0x8c, 0x97, 0x9e, 0xe9, 0xe0, 0xfb, 0xf2, 0xcd, 0xc4, 0xdf, 0xd6,
            0x31, 0x38, 0x23, 0x2a, 0x15, 0x1c, 0x07, 0x0e, 0x79, 0x70, 0x6b, 0x62, 0x5d, 0x54, 0x4f, 0x46};
    public static final int[] X_TIMES_B = {0x00, 0x0b, 0x16, 0x1d, 0x2c, 0x27, 0x3a, 0x31, 0x58, 0x53, 0x4e, 0x45,
            0x74, 0x7f, 0x62, 0x69, 0xb0, 0xbb, 0xa6, 0xad, 0x9c, 0x97, 0x8a, 0x81, 0xe8, 0xe3, 0xfe, 0xf5, 0xc4, 0xcf, 0xd2,
            0xd9, 0x7b, 0x70, 0x6d, 0x66, 0x57, 0x5c, 0x41, 0x4a, 0x23, 0x28, 0x35, 0x3e, 0x0f, 0x04, 0x19, 0x12, 0xcb, 0xc0,
            0xdd, 0xd6, 0xe7, 0xec, 0xf1, 0xfa, 0x93, 0x98, 0x85, 0x8e, 0xbf, 0xb4, 0xa9, 0xa2, 0xf6, 0xfd, 0xe0, 0xeb, 0xda,
            0xd1, 0xcc, 0xc7, 0xae, 0xa5, 0xb8, 0xb3, 0x82, 0x89, 0x94, 0x9f, 0x46, 0x4d, 0x50, 0x5b, 0x6a, 0x61, 0x7c, 0x77,
            0x1e, 0x15, 0x08, 0x03, 0x32, 0x39, 0x24, 0x2f, 0x8d, 0x86, 0x9b, 0x90, 0xa1, 0xaa, 0xb7, 0xbc, 0xd5, 0xde, 0xc3,
            0xc8, 0xf9, 0xf2, 0xef, 0xe4, 0x3d, 0x36, 0x2b, 0x20, 0x11, 0x1a, 0x07, 0x0c, 0x65, 0x6e, 0x73, 0x78, 0x49, 0x42,
            0x5f, 0x54, 0xf7, 0xfc, 0xe1, 0xea, 0xdb, 0xd0, 0xcd, 0xc6, 0xaf, 0xa4, 0xb9, 0xb2, 0x83, 0x88, 0x95, 0x9e, 0x47,
            0x4c, 0x51, 0x5a, 0x6b, 0x60, 0x7d, 0x76, 0x1f, 0x14, 0x09, 0x02, 0x33, 0x38, 0x25, 0x2e, 0x8c, 0x87, 0x9a, 0x91,
            0xa0, 0xab, 0xb6, 0xbd, 0xd4, 0xdf, 0xc2, 0xc9, 0xf8, 0xf3, 0xee, 0xe5, 0x3c, 0x37, 0x2a, 0x21, 0x10, 0x1b, 0x06,
            0x0d, 0x64, 0x6f, 0x72, 0x79, 0x48, 0x43, 0x5e, 0x55, 0x01, 0x0a, 0x17, 0x1c, 0x2d, 0x26, 0x3b, 0x30, 0x59, 0x52,
            0x4f, 0x44, 0x75, 0x7e, 0x63, 0x68, 0xb1, 0xba, 0xa7, 0xac, 0x9d, 0x96, 0x8b, 0x80, 0xe9, 0xe2, 0xff, 0xf4, 0xc5,
            0xce, 0xd3, 0xd8, 0x7a, 0x71, 0x6c, 0x67, 0x56, 0x5d, 0x40, 0x4b, 0x22, 0x29, 0x34, 0x3f, 0x0e, 0x05, 0x18, 0x13,
            0xca, 0xc1, 0xdc, 0xd7, 0xe6, 0xed, 0xf0, 0xfb, 0x92, 0x99, 0x84, 0x8f, 0xbe, 0xb5, 0xa8, 0xa3};
    public static final int[] X_TIMES_D = {0x00, 0x0d, 0x1a, 0x17, 0x34, 0x39, 0x2e, 0x23, 0x68, 0x65, 0x72, 0x7f,
            0x5c, 0x51, 0x46, 0x4b, 0xd0, 0xdd, 0xca, 0xc7, 0xe4, 0xe9, 0xfe, 0xf3, 0xb8, 0xb5, 0xa2, 0xaf, 0x8c, 0x81, 0x96,
            0x9b, 0xbb, 0xb6, 0xa1, 0xac, 0x8f, 0x82, 0x95, 0x98, 0xd3, 0xde, 0xc9, 0xc4, 0xe7, 0xea, 0xfd, 0xf0, 0x6b, 0x66,
            0x71, 0x7c, 0x5f, 0x52, 0x45, 0x48, 0x03, 0x0e, 0x19, 0x14, 0x37, 0x3a, 0x2d, 0x20, 0x6d, 0x60, 0x77, 0x7a, 0x59,
            0x54, 0x43, 0x4e, 0x05, 0x08, 0x1f, 0x12, 0x31, 0x3c, 0x2b, 0x26, 0xbd, 0xb0, 0xa7, 0xaa, 0x89, 0x84, 0x93, 0x9e,
            0xd5, 0xd8, 0xcf, 0xc2, 0xe1, 0xec, 0xfb, 0xf6, 0xd6, 0xdb, 0xcc, 0xc1, 0xe2, 0xef, 0xf8, 0xf5, 0xbe, 0xb3, 0xa4,
            0xa9, 0x8a, 0x87, 0x90, 0x9d, 0x06, 0x0b, 0x1c, 0x11, 0x32, 0x3f, 0x28, 0x25, 0x6e, 0x63, 0x74, 0x79, 0x5a, 0x57,
            0x40, 0x4d, 0xda, 0xd7, 0xc0, 0xcd, 0xee, 0xe3, 0xf4, 0xf9, 0xb2, 0xbf, 0xa8, 0xa5, 0x86, 0x8b, 0x9c, 0x91, 0x0a,
            0x07, 0x10, 0x1d, 0x3e, 0x33, 0x24, 0x29, 0x62, 0x6f, 0x78, 0x75, 0x56, 0x5b, 0x4c, 0x41, 0x61, 0x6c, 0x7b, 0x76,
            0x55, 0x58, 0x4f, 0x42, 0x09, 0x04, 0x13, 0x1e, 0x3d, 0x30, 0x27, 0x2a, 0xb1, 0xbc, 0xab, 0xa6, 0x85, 0x88, 0x9f,
            0x92, 0xd9, 0xd4, 0xc3, 0xce, 0xed, 0xe0, 0xf7, 0xfa, 0xb7, 0xba, 0xad, 0xa0, 0x83, 0x8e, 0x99, 0x94, 0xdf, 0xd2,
            0xc5, 0xc8, 0xeb, 0xe6, 0xf1, 0xfc, 0x67, 0x6a, 0x7d, 0x70, 0x53, 0x5e, 0x49, 0x44, 0x0f, 0x02, 0x15, 0x18, 0x3b,
            0x36, 0x21, 0x2c, 0x0c, 0x01, 0x16, 0x1b, 0x38, 0x35, 0x22, 0x2f, 0x64, 0x69, 0x7e, 0x73, 0x50, 0x5d, 0x4a, 0x47,
            0xdc, 0xd1, 0xc6, 0xcb, 0xe8, 0xe5, 0xf2, 0xff, 0xb4, 0xb9, 0xae, 0xa3, 0x80, 0x8d, 0x9a, 0x97};
    public static final int[] X_TIMES_E = {0x00, 0x0e, 0x1c, 0x12, 0x38, 0x36, 0x24, 0x2a, 0x70, 0x7e, 0x6c, 0x62,
            0x48, 0x46, 0x54, 0x5a, 0xe0, 0xee, 0xfc, 0xf2, 0xd8, 0xd6, 0xc4, 0xca, 0x90, 0x9e, 0x8c, 0x82, 0xa8, 0xa6, 0xb4,
            0xba, 0xdb, 0xd5, 0xc7, 0xc9, 0xe3, 0xed, 0xff, 0xf1, 0xab, 0xa5, 0xb7, 0xb9, 0x93, 0x9d, 0x8f, 0x81, 0x3b, 0x35,
            0x27, 0x29, 0x03, 0x0d, 0x1f, 0x11, 0x4b, 0x45, 0x57, 0x59, 0x73, 0x7d, 0x6f, 0x61, 0xad, 0xa3, 0xb1, 0xbf, 0x95,
            0x9b, 0x89, 0x87, 0xdd, 0xd3, 0xc1, 0xcf, 0xe5, 0xeb, 0xf9, 0xf7, 0x4d, 0x43, 0x51, 0x5f, 0x75, 0x7b, 0x69, 0x67,
            0x3d, 0x33, 0x21, 0x2f, 0x05, 0x0b, 0x19, 0x17, 0x76, 0x78, 0x6a, 0x64, 0x4e, 0x40, 0x52, 0x5c, 0x06, 0x08, 0x1a,
            0x14, 0x3e, 0x30, 0x22, 0x2c, 0x96, 0x98, 0x8a, 0x84, 0xae, 0xa0, 0xb2, 0xbc, 0xe6, 0xe8, 0xfa, 0xf4, 0xde, 0xd0,
            0xc2, 0xcc, 0x41, 0x4f, 0x5d, 0x53, 0x79, 0x77, 0x65, 0x6b, 0x31, 0x3f, 0x2d, 0x23, 0x09, 0x07, 0x15, 0x1b, 0xa1,
            0xaf, 0xbd, 0xb3, 0x99, 0x97, 0x85, 0x8b, 0xd1, 0xdf, 0xcd, 0xc3, 0xe9, 0xe7, 0xf5, 0xfb, 0x9a, 0x94, 0x86, 0x88,
            0xa2, 0xac, 0xbe, 0xb0, 0xea, 0xe4, 0xf6, 0xf8, 0xd2, 0xdc, 0xce, 0xc0, 0x7a, 0x74, 0x66, 0x68, 0x42, 0x4c, 0x5e,
            0x50, 0x0a, 0x04, 0x16, 0x18, 0x32, 0x3c, 0x2e, 0x20, 0xec, 0xe2, 0xf0, 0xfe, 0xd4, 0xda, 0xc8, 0xc6, 0x9c, 0x92,
            0x80, 0x8e, 0xa4, 0xaa, 0xb8, 0xb6, 0x0c, 0x02, 0x10, 0x1e, 0x34, 0x3a, 0x28, 0x26, 0x7c, 0x72, 0x60, 0x6e, 0x44,
            0x4a, 0x58, 0x56, 0x37, 0x39, 0x2b, 0x25, 0x0f, 0x01, 0x13, 0x1d, 0x47, 0x49, 0x5b, 0x55, 0x7f, 0x71, 0x63, 0x6d,
            0xd7, 0xd9, 0xcb, 0xc5, 0xef, 0xe1, 0xf3, 0xfd, 0xa7, 0xa9, 0xbb, 0xb5, 0x9f, 0x91, 0x83, 0x8d
    };
    private int keySize = NUM_BYTES_IN_128_BITS;
    private int numRounds = NUM_ROUNDS;
    private int stateSize = NUM_BYTES_IN_128_BITS;
    private ByteArray secretKey;
    private ArrayList<ByteArray> internalExpandedKey;
    private int roundOfFirstKeyToBeApplied;
    
    public static void main(String[] args) throws Exception{
//        TESTE DE EXPANDED KEY
//        ByteArray block = new ByteArray(16);
//        block.set(0, 0xf2);
//        block.set(1, 0x7a);
//        block.set(2, 0x59);
//        block.set(3, 0x73);
//        block.set(4, 0xc2);
//        block.set(5, 0x96);
//        block.set(6, 0x35);
//        block.set(7, 0x59);
//        block.set(8, 0x95);
//        block.set(9, 0xb9);
//        block.set(10, 0x80);
//        block.set(11, 0xf6);
//        block.set(12, 0xf2);
//        block.set(13, 0x43);
//        block.set(14, 0x7a);
//        block.set(15, 0x7f);
//        ByteArray block2 = block.clone();
//        block2.set(5, block2.get(5)^0xff);
////        block2.set(6, block2.get(6)^0xff);
//        ArrayList<ByteArray> block3 = ByteArray.concatenateAll(new AES().expandKey(block,2)).split(16);
//        ArrayList<ByteArray> block4 = ByteArray.concatenateAll(new AES().expandKey(block2,2)).split(16);
//        System.out.println(block);
//        System.out.println(block2);
//        System.out.println("keyDiff : "+block.clone().xor(block2));
//        for (ByteArray b : block3) {
////            b.setShape("4x4");
//            System.out.println(b);
//        }
//        System.out.println("");
//        for (ByteArray b : block4) {
////            b.setShape("4x4");
//            System.out.println(b);
//        }
//        System.out.println("");
//        System.out.println("expandedKeyDiff :");
//        ByteArray aux;
//        for (int i = 0; i < block3.size(); i++) {
//            aux = block3.get(i).clone().xor(block4.get(i));
////            aux.setShape("4x4");
//            System.out.println(aux);
//        }

//        TESTE DE DIFERENÇA DE ESTADOS
//        ByteArray key1 = new ByteArray(16);
//        key1.randomize();
//        ByteArray key2 = key1.clone();
//        key1.set(5, key1.get(5)^0xFF);
//        
//        ByteArray block = new ByteArray(16);
//        block.randomize();
//        ByteArray block2 = block.clone();
//        AES aes1 = new AES(key1,10);
//        AES aes2 = new AES(key2,10);
//        ArrayList<ByteArray> expandedKey1 = aes1.getExpandedKey().split(16);
//        ArrayList<ByteArray> expandedKey2 = aes2.getExpandedKey().split(16);
//        
//        ArrayList<ByteArray> block3 = aes1.encryptFullSavingStates(block, 10);
//        ArrayList<ByteArray> block4 = aes2.encryptFullSavingStates(block2, 10);
//        
//        System.out.println(block);
//        System.out.println(block2);
//        System.out.println("stateDiff :\n"+block.clone().xor(block2));
//        
//        System.out.println("keyDiff : ");
//        
//        for (int i = 0; i < expandedKey1.size(); i++) {
//            System.out.println(expandedKey1.get(i).clone().xor(expandedKey2.get(i)));
//        }
//        System.out.println("");
//        
//        for (ByteArray b : block3) {
//            System.out.println(b);
//        }
//        System.out.println("");
//        
//        for (ByteArray b : block4) {
//            System.out.println(b);
//        }
//        System.out.println("");
//        
//        System.out.println("fullStateDiff :");
//        for (int i = 0; i < block3.size(); i++) {
//            System.out.println(block3.get(i).clone().xor(block4.get(i)));
//        }


        // Step 1.1: get base key
        // Step 1.2: expand the base key
        // Step 1.3: get only the generator set for delta
        // Step 2.1: get base key
        // Step 2.2: expand the base key
        // Step 2.3: get only the generator set for nabla
        // Step 3  : apply differences and expand
        ByteArray key1 = new ByteArray(16);
        key1.randomize();
        key1.set(4, 0);
        key1.set(0, 0);
        ByteArray key2 = key1.clone();
        
        AES aes1 = new AES(key1, 10);
        AES aes2 = new AES(key2, 10);
        
        System.out.println("key1 : "+key1);
        System.out.println("key2 : "+key2);
        System.out.println("key1^key2 : "+key1.clone().xor(key2));
        
        ByteArray expandedKey1 = aes1.getExpandedKey();
        ByteArray expandedKey2 = aes2.getExpandedKey();
        
        System.out.println("expandedKey1 : "+expandedKey1);
        System.out.println("expandedKey2 : "+expandedKey2);
        System.out.println("expandedKey1^expandedKey2 : "+expandedKey1.clone().xor(expandedKey2));
        
        key1 = aes1.getRoundKey(10);
        key2 = aes1.getRoundKey(8);
        System.out.println("key1(round 10) : "+key1);
        System.out.println("key2(round 8) : "+key2);
        
        key1.set(4, key1.get(4)^0xff);
        key2.set(0, key2.get(0)^0xff);
        System.out.println("key1(round 10) after diff : "+key1);
        System.out.println("key2(round 8) after diff : "+key2);
        
        aes1 = new AES(key1, 10);
        aes2 = new AES(key2, 8);
        
        expandedKey1 = aes1.getExpandedKey();
        expandedKey2 = aes2.getExpandedKey();
        
        System.out.println("expandedKey1 : "+expandedKey1);
        System.out.println("expandedKey2 : "+expandedKey2);
        System.out.println("expandedKey1^expandedKey2 : "+expandedKey1.clone().xor(expandedKey2));
        
        aes1.getInitialKeyBiclique();
    }
    
    public AES() {
        setWordSize(8);
    }

    public AES(ByteArray key) throws Exception {
        setWordSize(8);
        setKeys(key, 0);
    }

    public AES(ByteArray key,int round) throws Exception{
        setWordSize(8);
        setKeys(key, round);
    }

    private void setKeys(ByteArray key, int round) throws Exception {
//        if(!"4x4".equals(key.getShape())){
//            if(key.length() == KEY_SIZE_IN_BYTES) key.setShape("4x4");
//            else throw new Exception("Invalid length for the key : "+key.length());
//        }
        internalExpandedKey = expandKey(key, round);
        secretKey = new ByteArray(KEY_SIZE_IN_BYTES);
        secretKey = key.clone();
    }
    
    private ArrayList<ByteArray> expandKey(ByteArray key) throws Exception {
            return expandKey(key, 0);
    }
    
    public ByteArray encryptFull(ByteArray block){
        return encryptRounds(block, 0, NUM_ROUNDS);
    }
    
    public ArrayList<ByteArray> decryptFull(ByteArray block){
        return decryptRounds(block, 0, NUM_ROUNDS);
    }
        
    /**
     *
     * 
     * @param block Estado final 
     * @param fromRound de 0 a 9
     * @param toRound de 0 a 9. Deve ser menor que fromround
     * @return Lista de todos os estados gerados ao longo da decriptação
     */
    public ArrayList<ByteArray> decryptRounds(ByteArray block, int fromRound, int toRound) {
        ArrayList<ByteArray> internalStates = new ArrayList<>();
        ByteArray currBlock = block.clone();
        internalStates.add(block);
        for (int i = fromRound; i >= toRound; i--) {
            if(i != 9) currBlock = invertedMixCollumns(currBlock);
            else currBlock = currBlock.xor(internalExpandedKey.get(i+1));
            internalStates.add(currBlock);
            
            currBlock = invertedShiftRows(currBlock);
            internalStates.add(currBlock);
            
            currBlock = invertedSubBytes(currBlock);
            internalStates.add(currBlock);
            
            currBlock = currBlock.xor(internalExpandedKey.get(i));
            internalStates.add(currBlock);
        }
        return internalStates;
    }
        
    public ByteArray rcon(ByteArray col, int round) throws Exception {
        //ByteArray col = new ByteArray(key.getNumRows());
        ByteArray aux = new ByteArray(col.length());
        aux.set(0, RCON[round]);
        col = col.xor(aux);
        return col;
    }
    
    public ByteArray rotWord(ByteArray col) throws Exception{
        //ByteArray col = key.getColumn(column);
        int aux = col.get(0);
        
        for (int i = 0; i < col.length()-1; i++) 
            col.set(i, col.get(i+1));
        col.set(col.length()-1, aux);
        //key.setColumn(column, col);
        
        return col;
    }
    
    public ByteArray subWord(ByteArray col) throws Exception {
        //ByteArray col = key.getColumn(column);
        
        for (int i = 0; i < col.length(); i++) 
            col.set(i, SBOX[col.get(i)]);
        //key.setColumn(column, col);
        
        return col;
    }
        
    public ByteArray inverseRotWord(ByteArray col) throws Exception{
        //ByteArray col = key.getColumn(column);
        int aux = col.get(col.length()-1);
        
        for (int i = col.length()-1; i >0; i--) 
            col.set(i, col.get(i-1));
        col.set(0, aux);
        
        return col;
    }
    
    public ByteArray inverseSubWord(ByteArray col) throws Exception {
        for (int i = 0; i < col.length(); i++) 
            col.set(i, INVERSE_SBOX[col.get(i)]);
        
        return col;
    }
    
    public ArrayList<ByteArray> expandKey(ByteArray key, int round) throws Exception {
        String originalShape = key.getShape();
        key.setShape("4x4");
        ArrayList<ByteArray> keys = new ArrayList<>();
        keys.add(key);
        ByteArray col;
        ByteArray aux;
        
        //System.out.println(keys);
        for (int i = round-1; i >=0; i--) {
            
            aux = new ByteArray(NUM_BYTES_IN_128_BITS);
            aux.setShape("4x4");
            aux.setColumn(3, keys.get(0).getColumn(3).xor(keys.get(0).getColumn(2)));
            aux.setColumn(2, keys.get(0).getColumn(2).xor(keys.get(0).getColumn(1)));
            aux.setColumn(1, keys.get(0).getColumn(1).xor(keys.get(0).getColumn(0)));
            
            
            col = aux.getColumn(aux.getNumCols()-1);
            col = rotWord(col);
            col = subWord(col);
            col = rcon(col, i+1);
            
            aux.setColumn(0, keys.get(0).getColumn(0).xor(col));
            keys.add(0, aux);
            //System.out.println(keys);
        }
        int pos = keys.size()-1;
        for (int i = round; i < NUM_ROUNDS; i++) {
            col = keys.get(i-round+pos).getColumn(key.getNumCols()-1);
//            System.out.println("col : "+col);
            col = rotWord(col);
//            System.out.println("col(after rotWord) : "+col);
            col = subWord(col);
//            System.out.println("col(after subWord) : "+col);
            col = rcon(col, i+1);
//            System.out.println("col(after rcon) : "+col);
            aux = new ByteArray(NUM_BYTES_IN_128_BITS);
            aux.setShape("4x4");
            aux.setColumn(0, col.xor(keys.get(i-round+pos).getColumn(0)));
            aux.setColumn(1, aux.getColumn(0).xor(keys.get(i-round+pos).getColumn(1)));
            aux.setColumn(2, aux.getColumn(1).xor(keys.get(i-round+pos).getColumn(2)));
            aux.setColumn(3, aux.getColumn(2).xor(keys.get(i-round+pos).getColumn(3)));
//            System.out.println("key : "+aux);
            keys.add(aux);
        }
        key.setShape(originalShape);
        return keys;
    }
    
    public ByteArray addKey(ByteArray block, ByteArray key){
        return block.clone().xor(key);
    }
    
    public ByteArray subBytes(ByteArray block) {
        ByteArray result = block.clone();
        for (int i = 0; i < block.length(); i++) 
            result.set(i, SBOX[block.get(i)]);
        
        return result;
    }
    
    public ByteArray invertedSubBytes(ByteArray block) {
        ByteArray result = block.clone();
        for (int i = 0; i < block.length(); i++) 
            result.set(i, INVERSE_SBOX[block.get(i)]);
        
        return result;
    }
    
    public ByteArray shiftRows(ByteArray block) {
        ByteArray result = block.clone();
        String originalShape = result.getShape();
        try {
            if(!originalShape.equals("4x4")) result.setShape("4x4");
            for (int i = 0; i < result.getNumRows(); i++) 
                for (int j = 0; j < i; j++) result.rotateRow(i);
            if(!originalShape.equals("4x4")) result.setShape(originalShape);
            
        } catch (Exception ex) {
            if(block.length()!= BLOCK_SIZE_IN_BYTES) System.out.println("shiftRows Exception : block has length "+block.length()+". (must be "+BLOCK_SIZE_IN_BYTES+")");
            //Never happens
            ex.printStackTrace();
        }
        return result;
    }
    
    public ByteArray invertedShiftRows(ByteArray block) {
        ByteArray result = block.clone();
        String originalShape = result.getShape();
        try {
            if(!originalShape.equals("4x4")) result.setShape("4x4");
            for (int i = 0; i < result.getNumRows(); i++) 
                for (int j = 0; j < result.getNumRows()-i; j++) result.rotateRow(i);
            if(!originalShape.equals("4x4")) result.setShape(originalShape);
            
        } catch (Exception ex) {
            if(block.length()!= BLOCK_SIZE_IN_BYTES) System.out.println("shiftRows Exception : block has length "+block.length()+". (must be "+BLOCK_SIZE_IN_BYTES+")");
            //Never happens
            ex.printStackTrace();
        }
        return result;
    }
    
    public ByteArray mixCollumns(ByteArray block) {
        ByteArray result = block.clone();

        result.set(0, X_TIMES_2[block.get(0)] ^ X_TIMES_3[block.get(4)] ^ block.get(8) ^ block.get(12));
        result.set(4, block.get(0) ^ X_TIMES_2[block.get(4)] ^ X_TIMES_3[block.get(8)] ^ block.get(12));
        result.set(8, block.get(0) ^ block.get(4) ^ X_TIMES_2[block.get(8)] ^ X_TIMES_3[block.get(12)]);
        result.set(12, X_TIMES_3[block.get(0)] ^ block.get(4) ^ block.get(8) ^ X_TIMES_2[block.get(12)]);

        result.set(1, X_TIMES_2[block.get(1)] ^ X_TIMES_3[block.get(5)] ^ block.get(9) ^ block.get(13));
        result.set(5, block.get(1) ^ X_TIMES_2[block.get(5)] ^ X_TIMES_3[block.get(9)] ^ block.get(13));
        result.set(9, block.get(1) ^ block.get(5) ^ X_TIMES_2[block.get(9)] ^ X_TIMES_3[block.get(13)]);
        result.set(13, X_TIMES_3[block.get(1)] ^ block.get(5) ^ block.get(9) ^ X_TIMES_2[block.get(13)]);

        result.set(2, X_TIMES_2[block.get(2)] ^ X_TIMES_3[block.get(6)] ^ block.get(10) ^ block.get(14));
        result.set(6, block.get(2) ^ X_TIMES_2[block.get(6)] ^ X_TIMES_3[block.get(10)] ^ block.get(14));
        result.set(10, block.get(2) ^ block.get(6) ^ X_TIMES_2[block.get(10)] ^ X_TIMES_3[block.get(14)]);
        result.set(14, X_TIMES_3[block.get(2)] ^ block.get(6) ^ block.get(10) ^ X_TIMES_2[block.get(14)]);

        result.set(3, X_TIMES_2[block.get(3)] ^ X_TIMES_3[block.get(7)] ^ block.get(11) ^ block.get(15));
        result.set(7, block.get(3) ^ X_TIMES_2[block.get(7)] ^ X_TIMES_3[block.get(11)] ^ block.get(15));
        result.set(11, block.get(3) ^ block.get(7) ^ X_TIMES_2[block.get(11)] ^ X_TIMES_3[block.get(15)]);
        result.set(15, X_TIMES_3[block.get(3)] ^ block.get(7) ^ block.get(11) ^ X_TIMES_2[block.get(15)]);

        return result;
    }
    
    public ByteArray invertedMixCollumns(ByteArray block) {
        ByteArray result = block.clone();
        
        result.set(0, X_TIMES_E[block.get(0)] ^ X_TIMES_B[block.get(4)]  ^ X_TIMES_D[block.get(8)] ^ X_TIMES_9[block.get(12)]);
        result.set(4, X_TIMES_9[block.get(0)] ^ X_TIMES_E[block.get(4)]  ^ X_TIMES_B[block.get(8)] ^ X_TIMES_D[block.get(12)]);
        result.set(8, X_TIMES_D[block.get(0)] ^ X_TIMES_9[block.get(4)]  ^ X_TIMES_E[block.get(8)] ^ X_TIMES_B[block.get(12)]);
        result.set(12, X_TIMES_B[block.get(0)] ^ X_TIMES_D[block.get(4)] ^ X_TIMES_9[block.get(8)] ^ X_TIMES_E[block.get(12)]);

        result.set(1, X_TIMES_E[block.get(1)] ^ X_TIMES_B[block.get(5)] ^ X_TIMES_D[block.get(9)] ^ X_TIMES_9[block.get(13)]);
        result.set(5, X_TIMES_9[block.get(1)] ^ X_TIMES_E[block.get(5)] ^ X_TIMES_B[block.get(9)] ^ X_TIMES_D[block.get(13)]);
        result.set(9, X_TIMES_D[block.get(1)] ^ X_TIMES_9[block.get(5)] ^ X_TIMES_E[block.get(9)] ^ X_TIMES_B[block.get(13)]);
        result.set(13, X_TIMES_B[block.get(1)] ^ X_TIMES_D[block.get(5)] ^ X_TIMES_9[block.get(9)] ^ X_TIMES_E[block.get(13)]);

        result.set(2, X_TIMES_E[block.get(2)] ^ X_TIMES_B[block.get(6)] ^ X_TIMES_D[block.get(10)] ^ X_TIMES_9[block.get(14)]);
        result.set(6, X_TIMES_9[block.get(2)] ^ X_TIMES_E[block.get(6)] ^ X_TIMES_B[block.get(10)] ^ X_TIMES_D[block.get(14)]);
        result.set(10, X_TIMES_D[block.get(2)] ^ X_TIMES_9[block.get(6)] ^ X_TIMES_E[block.get(10)] ^ X_TIMES_B[block.get(14)]);
        result.set(14, X_TIMES_B[block.get(2)] ^ X_TIMES_D[block.get(6)] ^ X_TIMES_9[block.get(10)] ^ X_TIMES_E[block.get(14)]);

        result.set(3, X_TIMES_E[block.get(3)] ^ X_TIMES_B[block.get(7)] ^ X_TIMES_D[block.get(11)] ^ X_TIMES_9[block.get(15)]);
        result.set(7, X_TIMES_9[block.get(3)] ^ X_TIMES_E[block.get(7)] ^ X_TIMES_B[block.get(11)] ^ X_TIMES_D[block.get(15)]);
        result.set(11, X_TIMES_D[block.get(3)] ^ X_TIMES_9[block.get(7)] ^ X_TIMES_E[block.get(11)] ^ X_TIMES_B[block.get(15)]);
        result.set(15, X_TIMES_B[block.get(3)] ^ X_TIMES_D[block.get(7)] ^ X_TIMES_9[block.get(11)] ^ X_TIMES_E[block.get(15)]);

        return result;
    }

    /**
     *
     * 
     * @param block Estado inicial 
     * @param fromRound de 0 a 9
     * @param toRound de 0 a 9. Deve ser maior que fromround
     * @return Lista de todos os estados gerados ao longo da encriptação
     */
    @Override
    public ByteArray encryptRounds(ByteArray block, int fromRound, int toRound) {
        ArrayList<ByteArray> internalStates = new ArrayList<>();
        ByteArray currBlock = block.clone();
        internalStates.add(block);
        for (int i = fromRound; i <= toRound; i++) {
            currBlock = currBlock.xor(internalExpandedKey.get(i));
            internalStates.add(currBlock);
            
            currBlock = subBytes(currBlock);
            internalStates.add(currBlock);
            
            currBlock = shiftRows(currBlock);
            internalStates.add(currBlock);
            
            if(i != 9) currBlock = mixCollumns(currBlock);
            else currBlock = currBlock.xor(internalExpandedKey.get(i+1));
            internalStates.add(currBlock);
        }
        return internalStates.get(internalStates.size()-1);
    }
    
    @Override
    public int getBLOCK_SIZE_IN_BYTES() {
        return BLOCK_SIZE_IN_BYTES;
    }

    @Override
    public int getKEY_SIZE_IN_BYTES() {
        return KEY_SIZE_IN_BYTES;
    }

    @Override
    public int getROUND_KEY_SIZE_IN_BYTES() {
        return ROUND_KEY_SIZE_IN_BYTES;
    }

    @Override
    public int getNUM_ROUNDS() {
        return NUM_ROUNDS;
    }

    @Override
    public int getNUM_KEYS() {
        return NUM_KEYS;
    }

    @Override
    public void setKey(ByteArray key, int round) {
        try {
            setKeys(key, round);
            /*try {
            internalExpandedKey = expandKey(key,round);
            } catch (Exception ex) {
            ex.printStackTrace();
            }
            secretKey = key.clone();
            roundOfFirstKeyToBeApplied = round;*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public ByteArray getExpandedKey() {
        if(secretKey == null) throw new RuntimeException("secretKey is null for some reason");            
        return ByteArray.concatenateAll(internalExpandedKey);
        
    }

    @Override
    public ByteArray getExpandedKey(ByteArray key, int round1) {
        List<ByteArray> aux = null;
        try {
            //System.out.println("getExpandedKey here");
            aux = expandKey(key, round1);
            //System.out.println("getExpandedKey here2");
            //System.out.println(aux);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println("getExpandedKey aux : "+aux);
        return ByteArray.concatenateAll(aux);
    }


    @Override
    public ArrayList<ByteArray> encryptRoundsFromStatesSavingStates(ByteArray block, int fromState, int toState) {
        //System.out.println("encryptRoundsFromStatesSavingStates params: block : "+block+" formState : "+fromState+" toState : "+toState);
        ArrayList<ByteArray> internalStates = new ArrayList<>();
        ByteArray currBlock = block.clone();
        internalStates.add(block);
        
        for (int i = fromState; i < toState; i++) {
            switch (i%4) {
                case 0:
                    currBlock = addKey(currBlock, internalExpandedKey.get(i/4));
                    internalStates.add(currBlock);
                    break;
                case 1:
                    currBlock = subBytes(currBlock);
                    internalStates.add(currBlock);
                    break;
                case 2:
                    currBlock = shiftRows(currBlock);
                    internalStates.add(currBlock);
                    break;
                default:
                    //System.out.println("i: "+i+" j : "+j);
                    if(i != 39) currBlock = mixCollumns(currBlock);
                    else currBlock = addKey(currBlock, internalExpandedKey.get(10));
                    internalStates.add(currBlock);
                    break;
            }
        }
        //System.out.println("internalStates (size : "+internalStates.size()+") : "+internalStates.get(0).clone().xor(block));
        return internalStates;       
    }

    @Override
    public ArrayList<ByteArray> encryptRoundsBackwardsFromStatesSavingStates(ByteArray block, int fromState, int toState) {
        ArrayList<ByteArray> internalStates = new ArrayList<>();
        ByteArray currBlock = block.clone();
//        internalStates.add(block);
//        System.out.println("fromState : "+fromState+", toState : "+toState);
//        System.out.println("key : "+internalExpandedKey + ", roundKey : "+j);
//        System.out.println("block : "+block);
        for (int i = fromState; i > toState; i--) {
            if(i%4 == 3){
                currBlock = invertedShiftRows(currBlock);
                internalStates.add(currBlock);
//                System.out.println("iSR : "+currBlock);
            }else if(i%4 == 2){
                currBlock = invertedSubBytes(currBlock);
                internalStates.add(currBlock);
//                System.out.println("iSB : "+currBlock);
            }else if (i%4 == 1){
//                System.out.println("pre-AK : "+currBlock);
                currBlock = addKey(currBlock,internalExpandedKey.get((i-1)/4));
                internalStates.add(currBlock);
//                System.out.println("key"+((i-1)/4)+" : "+internalExpandedKey.get((i-1)/4));
//                System.out.println("AK : "+currBlock);
            }else{
                if(i != 40) currBlock = invertedMixCollumns(currBlock);
                else currBlock = addKey(currBlock,internalExpandedKey.get(10));
                internalStates.add(currBlock);
//                System.out.println("iMC : "+currBlock);
            }
        }
        internalStates = ByteArray.reverse(internalStates);
//        System.out.println(internalStates);
        return internalStates; 
    }

    @Override
    public ArrayList<ByteArray> encryptFullSavingStates(ByteArray block, int indexOfFirstKeyToBeApplied) {
        return encryptFullSavingStates(block, 4*indexOfFirstKeyToBeApplied, 40);
    }

    @Override
    public int getWORD_SIZE() {
        return WORD_SIZE_IN_BITS;
    }

    @Override
    public int getNUM_STATES() {
        return NUM_STATES;
    }

    @Override
    public int getInitialStateBiclique() {
        return 29; //Valor arbitrário
    }

    @Override
    public int getInitialKeyBiclique() {
        int aux = getInitialStateBiclique()+1;
        while(getINDEX_OF_POST_KEY(aux) == -1) aux++;
//        System.out.println("aux : "+aux);
//        System.out.println("getInitialKeyBiclique : "+getINDEX_OF_POST_KEY(aux));
        return getINDEX_OF_POST_KEY(aux);
    }

    @Override
    public int getNUM_WORDS_KEY_DELTA() {
        return 16;
    }

    @Override
    public int getNUM_WORDS_KEY_NABLA() {
        return 16;
    }

    @Override
    public int getStateOfV() {
        return this.getINDEX_OF_STATE_POST_KEY(2); //Valor arbitrário
    }
    @Override
    public int getWordOfV() {
        return 0; //Valor arbitrário
    }

    @Override
    public int[] getINDEXES_OF_PRE_SBOX_STATES() {
        int[] result = {1,5,9,13,17,21,25,29,33,37};
        return result;
    }

    @Override
    public int[] getINDEXES_OF_POST_SBOX_STATES() {
        int[] result = {2,6,10,14,18,22,26,30,34,38};
        return result;
    }

    @Override
    public int getNUM_MITM_STATES() {
        return getInitialStateBiclique()+1;
    }

    @Override
    public int getAMOUNT_OF_KEYS() {
        return 1;
    }

    @Override
    public int getBLOCK_SIZE_IN_WORDS() {
        return getBLOCK_SIZE_IN_BYTES();
    }

    @Override
    public int getNUM_SBOXES_TOTAL() {
        return 200;
    }

    @Override
    public int[] getINDEXES_OF_PRE_ADD_KEY() {
        int[] result = {0,4,8,12,16,20,24,28,32,36,39};
        return result;
    }

    @Override
    public int[] getINDEXES_OF_POST_ADD_KEY() {
        int[] result = {1,5,9,13,17,21,25,29,33,37,40};
        return result;
    }

    @Override
    public int[] getSBOX_RELEVANT_KEY_WORDS() {
        int[] result = {3,7,11,15};
        return result;
    }

    @Override
    public int[] getSBOX_RELEVANT_STATE_WORDS() {
        int result[] = new int[getBLOCK_SIZE_IN_BYTES()];
        for (int i = 0; i < result.length; i++)
            result[i] = i;
        return result;
    }

    @Override
    public int getINDEX_OF_PRE_KEY(int state) {
        int[] indexes = getINDEXES_OF_PRE_ADD_KEY();
        int index = Arrays.binarySearch(indexes, state);
        if(index < 0) return -1;
        return index;
    }

    @Override
    public int getINDEX_OF_POST_KEY(int state) {
        int[] indexes = getINDEXES_OF_POST_ADD_KEY();
        int index = Arrays.binarySearch(indexes, state);
        if(index < 0) return -1;
        return index;
    }

    @Override
    public int getINDEX_OF_STATE_PRE_KEY(int index) {
        int[] indexes = getINDEXES_OF_PRE_ADD_KEY();
        if(index < 0 || index >= indexes.length) return -1;
        return indexes[index];
    }

    @Override
    public int getINDEX_OF_STATE_POST_KEY(int index) {
        int[] indexes = getINDEXES_OF_POST_ADD_KEY();
        if(index < 0 || index >= indexes.length) return -1;
        return indexes[index];
    }
    
    public ByteArray getRoundKey(int index) {
        return internalExpandedKey.get(index);
    }
}
