import java.io.InputStream;


/**

 This class contains all the methods required to decode and run a GameBoy ROM
 on a Java Virtual Machine. Hacked by David Edwards from David Winchurch's
 GameBoyEmu, in a manner to make it independent of the Java implementation.

@author David Winchurch, David Edwards.
@version 1.0

 **/

public final class GameBoyCPU implements Runnable {
private static final char[] DAA_TABLE = { // Decimal Add Adjust Array
    0x0044, 0x0100, 0x0200, 0x0304, 0x0400, 0x0504, 0x0604, 0x0700, 0x0808,
    0x090C, 0x1010, 0x1114, 0x1214, 0x1310, 0x1414, 0x1510, 0x1000, 0x1104,
    0x1204, 0x1300, 0x1404, 0x1500, 0x1600, 0x1704, 0x180C, 0x1908, 0x2030,
    0x2134, 0x2234, 0x2330, 0x2434, 0x2530, 0x2020, 0x2124, 0x2224, 0x2320,
    0x2424, 0x2520, 0x2620, 0x2724, 0x282C, 0x2928, 0x3034, 0x3130, 0x3230,
    0x3334, 0x3430, 0x3534, 0x3024, 0x3120, 0x3220, 0x3324, 0x3420, 0x3524,
    0x3624, 0x3720, 0x3828, 0x392C, 0x4010, 0x4114, 0x4214, 0x4310, 0x4414,
    0x4510, 0x4000, 0x4104, 0x4204, 0x4300, 0x4404, 0x4500, 0x4600, 0x4704,
    0x480C, 0x4908, 0x5014, 0x5110, 0x5210, 0x5314, 0x5410, 0x5514, 0x5004,
    0x5100, 0x5200, 0x5304, 0x5400, 0x5504, 0x5604, 0x5700, 0x5808, 0x590C,
    0x6034, 0x6130, 0x6230, 0x6334, 0x6430, 0x6534, 0x6024, 0x6120, 0x6220,
    0x6324, 0x6420, 0x6524, 0x6624, 0x6720, 0x6828, 0x692C, 0x7030, 0x7134,
    0x7234, 0x7330, 0x7434, 0x7530, 0x7020, 0x7124, 0x7224, 0x7320, 0x7424,
    0x7520, 0x7620, 0x7724, 0x782C, 0x7928, 0x8090, 0x8194, 0x8294, 0x8390,
    0x8494, 0x8590, 0x8080, 0x8184, 0x8284, 0x8380, 0x8484, 0x8580, 0x8680,
    0x8784, 0x888C, 0x8988, 0x9094, 0x9190, 0x9290, 0x9394, 0x9490, 0x9594,
    0x9084, 0x9180, 0x9280, 0x9384, 0x9480, 0x9584, 0x9684, 0x9780, 0x9888,
    0x998C, 0x0055, 0x0111, 0x0211, 0x0315, 0x0411, 0x0515, 0x0045, 0x0101,
    0x0201, 0x0305, 0x0401, 0x0505, 0x0605, 0x0701, 0x0809, 0x090D, 0x1011,
    0x1115, 0x1215, 0x1311, 0x1415, 0x1511, 0x1001, 0x1105, 0x1205, 0x1301,
    0x1405, 0x1501, 0x1601, 0x1705, 0x180D, 0x1909, 0x2031, 0x2135, 0x2235,
    0x2331, 0x2435, 0x2531, 0x2021, 0x2125, 0x2225, 0x2321, 0x2425, 0x2521,
    0x2621, 0x2725, 0x282D, 0x2929, 0x3035, 0x3131, 0x3231, 0x3335, 0x3431,
    0x3535, 0x3025, 0x3121, 0x3221, 0x3325, 0x3421, 0x3525, 0x3625, 0x3721,
    0x3829, 0x392D, 0x4011, 0x4115, 0x4215, 0x4311, 0x4415, 0x4511, 0x4001,
    0x4105, 0x4205, 0x4301, 0x4405, 0x4501, 0x4601, 0x4705, 0x480D, 0x4909,
    0x5015, 0x5111, 0x5211, 0x5315, 0x5411, 0x5515, 0x5005, 0x5101, 0x5201,
    0x5305, 0x5401, 0x5505, 0x5605, 0x5701, 0x5809, 0x590D, 0x6035, 0x6131,
    0x6231, 0x6335, 0x6431, 0x6535, 0x6025, 0x6121, 0x6221, 0x6325, 0x6421,
    0x6525, 0x6625, 0x6721, 0x6829, 0x692D, 0x7031, 0x7135, 0x7235, 0x7331,
    0x7435, 0x7531, 0x7021, 0x7125, 0x7225, 0x7321, 0x7425, 0x7521, 0x7621,
    0x7725, 0x782D, 0x7929, 0x8091, 0x8195, 0x8295, 0x8391, 0x8495, 0x8591,
    0x8081, 0x8185, 0x8285, 0x8381, 0x8485, 0x8581, 0x8681, 0x8785, 0x888D,
    0x8989, 0x9095, 0x9191, 0x9291, 0x9395, 0x9491, 0x9595, 0x9085, 0x9181,
    0x9281, 0x9385, 0x9481, 0x9585, 0x9685, 0x9781, 0x9889, 0x998D, 0xA0B5,
    0xA1B1, 0xA2B1, 0xA3B5, 0xA4B1, 0xA5B5, 0xA0A5, 0xA1A1, 0xA2A1, 0xA3A5,
    0xA4A1, 0xA5A5, 0xA6A5, 0xA7A1, 0xA8A9, 0xA9AD, 0xB0B1, 0xB1B5, 0xB2B5,
    0xB3B1, 0xB4B5, 0xB5B1, 0xB0A1, 0xB1A5, 0xB2A5, 0xB3A1, 0xB4A5, 0xB5A1,
    0xB6A1, 0xB7A5, 0xB8AD, 0xB9A9, 0xC095, 0xC191, 0xC291, 0xC395, 0xC491,
    0xC595, 0xC085, 0xC181, 0xC281, 0xC385, 0xC481, 0xC585, 0xC685, 0xC781,
    0xC889, 0xC98D, 0xD091, 0xD195, 0xD295, 0xD391, 0xD495, 0xD591, 0xD081,
    0xD185, 0xD285, 0xD381, 0xD485, 0xD581, 0xD681, 0xD785, 0xD88D, 0xD989,
    0xE0B1, 0xE1B5, 0xE2B5, 0xE3B1, 0xE4B5, 0xE5B1, 0xE0A1, 0xE1A5, 0xE2A5,
    0xE3A1, 0xE4A5, 0xE5A1, 0xE6A1, 0xE7A5, 0xE8AD, 0xE9A9, 0xF0B5, 0xF1B1,
    0xF2B1, 0xF3B5, 0xF4B1, 0xF5B5, 0xF0A5, 0xF1A1, 0xF2A1, 0xF3A5, 0xF4A1,
    0xF5A5, 0xF6A5, 0xF7A1, 0xF8A9, 0xF9AD, 0x0055, 0x0111, 0x0211, 0x0315,
    0x0411, 0x0515, 0x0045, 0x0101, 0x0201, 0x0305, 0x0401, 0x0505, 0x0605,
    0x0701, 0x0809, 0x090D, 0x1011, 0x1115, 0x1215, 0x1311, 0x1415, 0x1511,
    0x1001, 0x1105, 0x1205, 0x1301, 0x1405, 0x1501, 0x1601, 0x1705, 0x180D,
    0x1909, 0x2031, 0x2135, 0x2235, 0x2331, 0x2435, 0x2531, 0x2021, 0x2125,
    0x2225, 0x2321, 0x2425, 0x2521, 0x2621, 0x2725, 0x282D, 0x2929, 0x3035,
    0x3131, 0x3231, 0x3335, 0x3431, 0x3535, 0x3025, 0x3121, 0x3221, 0x3325,
    0x3421, 0x3525, 0x3625, 0x3721, 0x3829, 0x392D, 0x4011, 0x4115, 0x4215,
    0x4311, 0x4415, 0x4511, 0x4001, 0x4105, 0x4205, 0x4301, 0x4405, 0x4501,
    0x4601, 0x4705, 0x480D, 0x4909, 0x5015, 0x5111, 0x5211, 0x5315, 0x5411,
    0x5515, 0x5005, 0x5101, 0x5201, 0x5305, 0x5401, 0x5505, 0x5605, 0x5701,
    0x5809, 0x590D, 0x6035, 0x6131, 0x6231, 0x6335, 0x6431, 0x6535, 0x0604,
    0x0700, 0x0808, 0x090C, 0x0A0C, 0x0B08, 0x0C0C, 0x0D08, 0x0E08, 0x0F0C,
    0x1010, 0x1114, 0x1214, 0x1310, 0x1414, 0x1510, 0x1600, 0x1704, 0x180C,
    0x1908, 0x1A08, 0x1B0C, 0x1C08, 0x1D0C, 0x1E0C, 0x1F08, 0x2030, 0x2134,
    0x2234, 0x2330, 0x2434, 0x2530, 0x2620, 0x2724, 0x282C, 0x2928, 0x2A28,
    0x2B2C, 0x2C28, 0x2D2C, 0x2E2C, 0x2F28, 0x3034, 0x3130, 0x3230, 0x3334,
    0x3430, 0x3534, 0x3624, 0x3720, 0x3828, 0x392C, 0x3A2C, 0x3B28, 0x3C2C,
    0x3D28, 0x3E28, 0x3F2C, 0x4010, 0x4114, 0x4214, 0x4310, 0x4414, 0x4510,
    0x4600, 0x4704, 0x480C, 0x4908, 0x4A08, 0x4B0C, 0x4C08, 0x4D0C, 0x4E0C,
    0x4F08, 0x5014, 0x5110, 0x5210, 0x5314, 0x5410, 0x5514, 0x5604, 0x5700,
    0x5808, 0x590C, 0x5A0C, 0x5B08, 0x5C0C, 0x5D08, 0x5E08, 0x5F0C, 0x6034,
    0x6130, 0x6230, 0x6334, 0x6430, 0x6534, 0x6624, 0x6720, 0x6828, 0x692C,
    0x6A2C, 0x6B28, 0x6C2C, 0x6D28, 0x6E28, 0x6F2C, 0x7030, 0x7134, 0x7234,
    0x7330, 0x7434, 0x7530, 0x7620, 0x7724, 0x782C, 0x7928, 0x7A28, 0x7B2C,
    0x7C28, 0x7D2C, 0x7E2C, 0x7F28, 0x8090, 0x8194, 0x8294, 0x8390, 0x8494,
    0x8590, 0x8680, 0x8784, 0x888C, 0x8988, 0x8A88, 0x8B8C, 0x8C88, 0x8D8C,
    0x8E8C, 0x8F88, 0x9094, 0x9190, 0x9290, 0x9394, 0x9490, 0x9594, 0x9684,
    0x9780, 0x9888, 0x998C, 0x9A8C, 0x9B88, 0x9C8C, 0x9D88, 0x9E88, 0x9F8C,
    0x0055, 0x0111, 0x0211, 0x0315, 0x0411, 0x0515, 0x0605, 0x0701, 0x0809,
    0x090D, 0x0A0D, 0x0B09, 0x0C0D, 0x0D09, 0x0E09, 0x0F0D, 0x1011, 0x1115,
    0x1215, 0x1311, 0x1415, 0x1511, 0x1601, 0x1705, 0x180D, 0x1909, 0x1A09,
    0x1B0D, 0x1C09, 0x1D0D, 0x1E0D, 0x1F09, 0x2031, 0x2135, 0x2235, 0x2331,
    0x2435, 0x2531, 0x2621, 0x2725, 0x282D, 0x2929, 0x2A29, 0x2B2D, 0x2C29,
    0x2D2D, 0x2E2D, 0x2F29, 0x3035, 0x3131, 0x3231, 0x3335, 0x3431, 0x3535,
    0x3625, 0x3721, 0x3829, 0x392D, 0x3A2D, 0x3B29, 0x3C2D, 0x3D29, 0x3E29,
    0x3F2D, 0x4011, 0x4115, 0x4215, 0x4311, 0x4415, 0x4511, 0x4601, 0x4705,
    0x480D, 0x4909, 0x4A09, 0x4B0D, 0x4C09, 0x4D0D, 0x4E0D, 0x4F09, 0x5015,
    0x5111, 0x5211, 0x5315, 0x5411, 0x5515, 0x5605, 0x5701, 0x5809, 0x590D,
    0x5A0D, 0x5B09, 0x5C0D, 0x5D09, 0x5E09, 0x5F0D, 0x6035, 0x6131, 0x6231,
    0x6335, 0x6431, 0x6535, 0x6625, 0x6721, 0x6829, 0x692D, 0x6A2D, 0x6B29,
    0x6C2D, 0x6D29, 0x6E29, 0x6F2D, 0x7031, 0x7135, 0x7235, 0x7331, 0x7435,
    0x7531, 0x7621, 0x7725, 0x782D, 0x7929, 0x7A29, 0x7B2D, 0x7C29, 0x7D2D,
    0x7E2D, 0x7F29, 0x8091, 0x8195, 0x8295, 0x8391, 0x8495, 0x8591, 0x8681,
    0x8785, 0x888D, 0x8989, 0x8A89, 0x8B8D, 0x8C89, 0x8D8D, 0x8E8D, 0x8F89,
    0x9095, 0x9191, 0x9291, 0x9395, 0x9491, 0x9595, 0x9685, 0x9781, 0x9889,
    0x998D, 0x9A8D, 0x9B89, 0x9C8D, 0x9D89, 0x9E89, 0x9F8D, 0xA0B5, 0xA1B1,
    0xA2B1, 0xA3B5, 0xA4B1, 0xA5B5, 0xA6A5, 0xA7A1, 0xA8A9, 0xA9AD, 0xAAAD,
    0xABA9, 0xACAD, 0xADA9, 0xAEA9, 0xAFAD, 0xB0B1, 0xB1B5, 0xB2B5, 0xB3B1,
    0xB4B5, 0xB5B1, 0xB6A1, 0xB7A5, 0xB8AD, 0xB9A9, 0xBAA9, 0xBBAD, 0xBCA9,
    0xBDAD, 0xBEAD, 0xBFA9, 0xC095, 0xC191, 0xC291, 0xC395, 0xC491, 0xC595,
    0xC685, 0xC781, 0xC889, 0xC98D, 0xCA8D, 0xCB89, 0xCC8D, 0xCD89, 0xCE89,
    0xCF8D, 0xD091, 0xD195, 0xD295, 0xD391, 0xD495, 0xD591, 0xD681, 0xD785,
    0xD88D, 0xD989, 0xDA89, 0xDB8D, 0xDC89, 0xDD8D, 0xDE8D, 0xDF89, 0xE0B1,
    0xE1B5, 0xE2B5, 0xE3B1, 0xE4B5, 0xE5B1, 0xE6A1, 0xE7A5, 0xE8AD, 0xE9A9,
    0xEAA9, 0xEBAD, 0xECA9, 0xEDAD, 0xEEAD, 0xEFA9, 0xF0B5, 0xF1B1, 0xF2B1,
    0xF3B5, 0xF4B1, 0xF5B5, 0xF6A5, 0xF7A1, 0xF8A9, 0xF9AD, 0xFAAD, 0xFBA9,
    0xFCAD, 0xFDA9, 0xFEA9, 0xFFAD, 0x0055, 0x0111, 0x0211, 0x0315, 0x0411,
    0x0515, 0x0605, 0x0701, 0x0809, 0x090D, 0x0A0D, 0x0B09, 0x0C0D, 0x0D09,
    0x0E09, 0x0F0D, 0x1011, 0x1115, 0x1215, 0x1311, 0x1415, 0x1511, 0x1601,
    0x1705, 0x180D, 0x1909, 0x1A09, 0x1B0D, 0x1C09, 0x1D0D, 0x1E0D, 0x1F09,
    0x2031, 0x2135, 0x2235, 0x2331, 0x2435, 0x2531, 0x2621, 0x2725, 0x282D,
    0x2929, 0x2A29, 0x2B2D, 0x2C29, 0x2D2D, 0x2E2D, 0x2F29, 0x3035, 0x3131,
    0x3231, 0x3335, 0x3431, 0x3535, 0x3625, 0x3721, 0x3829, 0x392D, 0x3A2D,
    0x3B29, 0x3C2D, 0x3D29, 0x3E29, 0x3F2D, 0x4011, 0x4115, 0x4215, 0x4311,
    0x4415, 0x4511, 0x4601, 0x4705, 0x480D, 0x4909, 0x4A09, 0x4B0D, 0x4C09,
    0x4D0D, 0x4E0D, 0x4F09, 0x5015, 0x5111, 0x5211, 0x5315, 0x5411, 0x5515,
    0x5605, 0x5701, 0x5809, 0x590D, 0x5A0D, 0x5B09, 0x5C0D, 0x5D09, 0x5E09,
    0x5F0D, 0x6035, 0x6131, 0x6231, 0x6335, 0x6431, 0x6535, 0x0046, 0x0102,
    0x0202, 0x0306, 0x0402, 0x0506, 0x0606, 0x0702, 0x080A, 0x090E, 0x0402,
    0x0506, 0x0606, 0x0702, 0x080A, 0x090E, 0x1002, 0x1106, 0x1206, 0x1302,
    0x1406, 0x1502, 0x1602, 0x1706, 0x180E, 0x190A, 0x1406, 0x1502, 0x1602,
    0x1706, 0x180E, 0x190A, 0x2022, 0x2126, 0x2226, 0x2322, 0x2426, 0x2522,
    0x2622, 0x2726, 0x282E, 0x292A, 0x2426, 0x2522, 0x2622, 0x2726, 0x282E,
    0x292A, 0x3026, 0x3122, 0x3222, 0x3326, 0x3422, 0x3526, 0x3626, 0x3722,
    0x382A, 0x392E, 0x3422, 0x3526, 0x3626, 0x3722, 0x382A, 0x392E, 0x4002,
    0x4106, 0x4206, 0x4302, 0x4406, 0x4502, 0x4602, 0x4706, 0x480E, 0x490A,
    0x4406, 0x4502, 0x4602, 0x4706, 0x480E, 0x490A, 0x5006, 0x5102, 0x5202,
    0x5306, 0x5402, 0x5506, 0x5606, 0x5702, 0x580A, 0x590E, 0x5402, 0x5506,
    0x5606, 0x5702, 0x580A, 0x590E, 0x6026, 0x6122, 0x6222, 0x6326, 0x6422,
    0x6526, 0x6626, 0x6722, 0x682A, 0x692E, 0x6422, 0x6526, 0x6626, 0x6722,
    0x682A, 0x692E, 0x7022, 0x7126, 0x7226, 0x7322, 0x7426, 0x7522, 0x7622,
    0x7726, 0x782E, 0x792A, 0x7426, 0x7522, 0x7622, 0x7726, 0x782E, 0x792A,
    0x8082, 0x8186, 0x8286, 0x8382, 0x8486, 0x8582, 0x8682, 0x8786, 0x888E,
    0x898A, 0x8486, 0x8582, 0x8682, 0x8786, 0x888E, 0x898A, 0x9086, 0x9182,
    0x9282, 0x9386, 0x9482, 0x9586, 0x9686, 0x9782, 0x988A, 0x998E, 0x3423,
    0x3527, 0x3627, 0x3723, 0x382B, 0x392F, 0x4003, 0x4107, 0x4207, 0x4303,
    0x4407, 0x4503, 0x4603, 0x4707, 0x480F, 0x490B, 0x4407, 0x4503, 0x4603,
    0x4707, 0x480F, 0x490B, 0x5007, 0x5103, 0x5203, 0x5307, 0x5403, 0x5507,
    0x5607, 0x5703, 0x580B, 0x590F, 0x5403, 0x5507, 0x5607, 0x5703, 0x580B,
    0x590F, 0x6027, 0x6123, 0x6223, 0x6327, 0x6423, 0x6527, 0x6627, 0x6723,
    0x682B, 0x692F, 0x6423, 0x6527, 0x6627, 0x6723, 0x682B, 0x692F, 0x7023,
    0x7127, 0x7227, 0x7323, 0x7427, 0x7523, 0x7623, 0x7727, 0x782F, 0x792B,
    0x7427, 0x7523, 0x7623, 0x7727, 0x782F, 0x792B, 0x8083, 0x8187, 0x8287,
    0x8383, 0x8487, 0x8583, 0x8683, 0x8787, 0x888F, 0x898B, 0x8487, 0x8583,
    0x8683, 0x8787, 0x888F, 0x898B, 0x9087, 0x9183, 0x9283, 0x9387, 0x9483,
    0x9587, 0x9687, 0x9783, 0x988B, 0x998F, 0x9483, 0x9587, 0x9687, 0x9783,
    0x988B, 0x998F, 0xA0A7, 0xA1A3, 0xA2A3, 0xA3A7, 0xA4A3, 0xA5A7, 0xA6A7,
    0xA7A3, 0xA8AB, 0xA9AF, 0xA4A3, 0xA5A7, 0xA6A7, 0xA7A3, 0xA8AB, 0xA9AF,
    0xB0A3, 0xB1A7, 0xB2A7, 0xB3A3, 0xB4A7, 0xB5A3, 0xB6A3, 0xB7A7, 0xB8AF,
    0xB9AB, 0xB4A7, 0xB5A3, 0xB6A3, 0xB7A7, 0xB8AF, 0xB9AB, 0xC087, 0xC183,
    0xC283, 0xC387, 0xC483, 0xC587, 0xC687, 0xC783, 0xC88B, 0xC98F, 0xC483,
    0xC587, 0xC687, 0xC783, 0xC88B, 0xC98F, 0xD083, 0xD187, 0xD287, 0xD383,
    0xD487, 0xD583, 0xD683, 0xD787, 0xD88F, 0xD98B, 0xD487, 0xD583, 0xD683,
    0xD787, 0xD88F, 0xD98B, 0xE0A3, 0xE1A7, 0xE2A7, 0xE3A3, 0xE4A7, 0xE5A3,
    0xE6A3, 0xE7A7, 0xE8AF, 0xE9AB, 0xE4A7, 0xE5A3, 0xE6A3, 0xE7A7, 0xE8AF,
    0xE9AB, 0xF0A7, 0xF1A3, 0xF2A3, 0xF3A7, 0xF4A3, 0xF5A7, 0xF6A7, 0xF7A3,
    0xF8AB, 0xF9AF, 0xF4A3, 0xF5A7, 0xF6A7, 0xF7A3, 0xF8AB, 0xF9AF, 0x0047,
    0x0103, 0x0203, 0x0307, 0x0403, 0x0507, 0x0607, 0x0703, 0x080B, 0x090F,
    0x0403, 0x0507, 0x0607, 0x0703, 0x080B, 0x090F, 0x1003, 0x1107, 0x1207,
    0x1303, 0x1407, 0x1503, 0x1603, 0x1707, 0x180F, 0x190B, 0x1407, 0x1503,
    0x1603, 0x1707, 0x180F, 0x190B, 0x2023, 0x2127, 0x2227, 0x2323, 0x2427,
    0x2523, 0x2623, 0x2727, 0x282F, 0x292B, 0x2427, 0x2523, 0x2623, 0x2727,
    0x282F, 0x292B, 0x3027, 0x3123, 0x3223, 0x3327, 0x3423, 0x3527, 0x3627,
    0x3723, 0x382B, 0x392F, 0x3423, 0x3527, 0x3627, 0x3723, 0x382B, 0x392F,
    0x4003, 0x4107, 0x4207, 0x4303, 0x4407, 0x4503, 0x4603, 0x4707, 0x480F,
    0x490B, 0x4407, 0x4503, 0x4603, 0x4707, 0x480F, 0x490B, 0x5007, 0x5103,
    0x5203, 0x5307, 0x5403, 0x5507, 0x5607, 0x5703, 0x580B, 0x590F, 0x5403,
    0x5507, 0x5607, 0x5703, 0x580B, 0x590F, 0x6027, 0x6123, 0x6223, 0x6327,
    0x6423, 0x6527, 0x6627, 0x6723, 0x682B, 0x692F, 0x6423, 0x6527, 0x6627,
    0x6723, 0x682B, 0x692F, 0x7023, 0x7127, 0x7227, 0x7323, 0x7427, 0x7523,
    0x7623, 0x7727, 0x782F, 0x792B, 0x7427, 0x7523, 0x7623, 0x7727, 0x782F,
    0x792B, 0x8083, 0x8187, 0x8287, 0x8383, 0x8487, 0x8583, 0x8683, 0x8787,
    0x888F, 0x898B, 0x8487, 0x8583, 0x8683, 0x8787, 0x888F, 0x898B, 0x9087,
    0x9183, 0x9283, 0x9387, 0x9483, 0x9587, 0x9687, 0x9783, 0x988B, 0x998F,
    0x9483, 0x9587, 0x9687, 0x9783, 0x988B, 0x998F, 0xFABE, 0xFBBA, 0xFCBE,
    0xFDBA, 0xFEBA, 0xFFBE, 0x0046, 0x0102, 0x0202, 0x0306, 0x0402, 0x0506,
    0x0606, 0x0702, 0x080A, 0x090E, 0x0A1E, 0x0B1A, 0x0C1E, 0x0D1A, 0x0E1A,
    0x0F1E, 0x1002, 0x1106, 0x1206, 0x1302, 0x1406, 0x1502, 0x1602, 0x1706,
    0x180E, 0x190A, 0x1A1A, 0x1B1E, 0x1C1A, 0x1D1E, 0x1E1E, 0x1F1A, 0x2022,
    0x2126, 0x2226, 0x2322, 0x2426, 0x2522, 0x2622, 0x2726, 0x282E, 0x292A,
    0x2A3A, 0x2B3E, 0x2C3A, 0x2D3E, 0x2E3E, 0x2F3A, 0x3026, 0x3122, 0x3222,
    0x3326, 0x3422, 0x3526, 0x3626, 0x3722, 0x382A, 0x392E, 0x3A3E, 0x3B3A,
    0x3C3E, 0x3D3A, 0x3E3A, 0x3F3E, 0x4002, 0x4106, 0x4206, 0x4302, 0x4406,
    0x4502, 0x4602, 0x4706, 0x480E, 0x490A, 0x4A1A, 0x4B1E, 0x4C1A, 0x4D1E,
    0x4E1E, 0x4F1A, 0x5006, 0x5102, 0x5202, 0x5306, 0x5402, 0x5506, 0x5606,
    0x5702, 0x580A, 0x590E, 0x5A1E, 0x5B1A, 0x5C1E, 0x5D1A, 0x5E1A, 0x5F1E,
    0x6026, 0x6122, 0x6222, 0x6326, 0x6422, 0x6526, 0x6626, 0x6722, 0x682A,
    0x692E, 0x6A3E, 0x6B3A, 0x6C3E, 0x6D3A, 0x6E3A, 0x6F3E, 0x7022, 0x7126,
    0x7226, 0x7322, 0x7426, 0x7522, 0x7622, 0x7726, 0x782E, 0x792A, 0x7A3A,
    0x7B3E, 0x7C3A, 0x7D3E, 0x7E3E, 0x7F3A, 0x8082, 0x8186, 0x8286, 0x8382,
    0x8486, 0x8582, 0x8682, 0x8786, 0x888E, 0x898A, 0x8A9A, 0x8B9E, 0x8C9A,
    0x8D9E, 0x8E9E, 0x8F9A, 0x9086, 0x9182, 0x9282, 0x9386, 0x3423, 0x3527,
    0x3627, 0x3723, 0x382B, 0x392F, 0x3A3F, 0x3B3B, 0x3C3F, 0x3D3B, 0x3E3B,
    0x3F3F, 0x4003, 0x4107, 0x4207, 0x4303, 0x4407, 0x4503, 0x4603, 0x4707,
    0x480F, 0x490B, 0x4A1B, 0x4B1F, 0x4C1B, 0x4D1F, 0x4E1F, 0x4F1B, 0x5007,
    0x5103, 0x5203, 0x5307, 0x5403, 0x5507, 0x5607, 0x5703, 0x580B, 0x590F,
    0x5A1F, 0x5B1B, 0x5C1F, 0x5D1B, 0x5E1B, 0x5F1F, 0x6027, 0x6123, 0x6223,
    0x6327, 0x6423, 0x6527, 0x6627, 0x6723, 0x682B, 0x692F, 0x6A3F, 0x6B3B,
    0x6C3F, 0x6D3B, 0x6E3B, 0x6F3F, 0x7023, 0x7127, 0x7227, 0x7323, 0x7427,
    0x7523, 0x7623, 0x7727, 0x782F, 0x792B, 0x7A3B, 0x7B3F, 0x7C3B, 0x7D3F,
    0x7E3F, 0x7F3B, 0x8083, 0x8187, 0x8287, 0x8383, 0x8487, 0x8583, 0x8683,
    0x8787, 0x888F, 0x898B, 0x8A9B, 0x8B9F, 0x8C9B, 0x8D9F, 0x8E9F, 0x8F9B,
    0x9087, 0x9183, 0x9283, 0x9387, 0x9483, 0x9587, 0x9687, 0x9783, 0x988B,
    0x998F, 0x9A9F, 0x9B9B, 0x9C9F, 0x9D9B, 0x9E9B, 0x9F9F, 0xA0A7, 0xA1A3,
    0xA2A3, 0xA3A7, 0xA4A3, 0xA5A7, 0xA6A7, 0xA7A3, 0xA8AB, 0xA9AF, 0xAABF,
    0xABBB, 0xACBF, 0xADBB, 0xAEBB, 0xAFBF, 0xB0A3, 0xB1A7, 0xB2A7, 0xB3A3,
    0xB4A7, 0xB5A3, 0xB6A3, 0xB7A7, 0xB8AF, 0xB9AB, 0xBABB, 0xBBBF, 0xBCBB,
    0xBDBF, 0xBEBF, 0xBFBB, 0xC087, 0xC183, 0xC283, 0xC387, 0xC483, 0xC587,
    0xC687, 0xC783, 0xC88B, 0xC98F, 0xCA9F, 0xCB9B, 0xCC9F, 0xCD9B, 0xCE9B,
    0xCF9F, 0xD083, 0xD187, 0xD287, 0xD383, 0xD487, 0xD583, 0xD683, 0xD787,
    0xD88F, 0xD98B, 0xDA9B, 0xDB9F, 0xDC9B, 0xDD9F, 0xDE9F, 0xDF9B, 0xE0A3,
    0xE1A7, 0xE2A7, 0xE3A3, 0xE4A7, 0xE5A3, 0xE6A3, 0xE7A7, 0xE8AF, 0xE9AB,
    0xEABB, 0xEBBF, 0xECBB, 0xEDBF, 0xEEBF, 0xEFBB, 0xF0A7, 0xF1A3, 0xF2A3,
    0xF3A7, 0xF4A3, 0xF5A7, 0xF6A7, 0xF7A3, 0xF8AB, 0xF9AF, 0xFABF, 0xFBBB,
    0xFCBF, 0xFDBB, 0xFEBB, 0xFFBF, 0x0047, 0x0103, 0x0203, 0x0307, 0x0403,
    0x0507, 0x0607, 0x0703, 0x080B, 0x090F, 0x0A1F, 0x0B1B, 0x0C1F, 0x0D1B,
    0x0E1B, 0x0F1F, 0x1003, 0x1107, 0x1207, 0x1303, 0x1407, 0x1503, 0x1603,
    0x1707, 0x180F, 0x190B, 0x1A1B, 0x1B1F, 0x1C1B, 0x1D1F, 0x1E1F, 0x1F1B,
    0x2023, 0x2127, 0x2227, 0x2323, 0x2427, 0x2523, 0x2623, 0x2727, 0x282F,
    0x292B, 0x2A3B, 0x2B3F, 0x2C3B, 0x2D3F, 0x2E3F, 0x2F3B, 0x3027, 0x3123,
    0x3223, 0x3327, 0x3423, 0x3527, 0x3627, 0x3723, 0x382B, 0x392F, 0x3A3F,
    0x3B3B, 0x3C3F, 0x3D3B, 0x3E3B, 0x3F3F, 0x4003, 0x4107, 0x4207, 0x4303,
    0x4407, 0x4503, 0x4603, 0x4707, 0x480F, 0x490B, 0x4A1B, 0x4B1F, 0x4C1B,
    0x4D1F, 0x4E1F, 0x4F1B, 0x5007, 0x5103, 0x5203, 0x5307, 0x5403, 0x5507,
    0x5607, 0x5703, 0x580B, 0x590F, 0x5A1F, 0x5B1B, 0x5C1F, 0x5D1B, 0x5E1B,
    0x5F1F, 0x6027, 0x6123, 0x6223, 0x6327, 0x6423, 0x6527, 0x6627, 0x6723,
    0x682B, 0x692F, 0x6A3F, 0x6B3B, 0x6C3F, 0x6D3B, 0x6E3B, 0x6F3F, 0x7023,
    0x7127, 0x7227, 0x7323, 0x7427, 0x7523, 0x7623, 0x7727, 0x782F, 0x792B,
    0x7A3B, 0x7B3F, 0x7C3B, 0x7D3F, 0x7E3F, 0x7F3B, 0x8083, 0x8187, 0x8287,
    0x8383, 0x8487, 0x8583, 0x8683, 0x8787, 0x888F, 0x898B, 0x8A9B, 0x8B9F,
    0x8C9B, 0x8D9F, 0x8E9F, 0x8F9B, 0x9087, 0x9183, 0x9283, 0x9387, 0x9483,
    0x9587, 0x9687, 0x9783, 0x988B, 0x998F
};
private static final byte[] SWAP_TABLE = { // Byte-swap Array
    0x00, 0x10, 0x20, 0x30, 0x40, 0x50, 0x60, 0x70, (byte) 0x80, (byte) 0x90,
    (byte) 0xA0, (byte) 0xB0, (byte) 0xC0, (byte) 0xD0, (byte) 0xE0,
    (byte) 0xF0, 0x01, 0x11, 0x21, 0x31, 0x41, 0x51, 0x61, 0x71, (byte) 0x81,
    (byte) 0x91, (byte) 0xA1, (byte) 0xB1, (byte) 0xC1, (byte) 0xD1,
    (byte) 0xE1, (byte) 0xF1, 0x02, 0x12, 0x22, 0x32, 0x42, 0x52, 0x62, 0x72,
    (byte) 0x82, (byte) 0x92, (byte) 0xA2, (byte) 0xB2, (byte) 0xC2,
    (byte) 0xD2, (byte) 0xE2, (byte) 0xF2, 0x03, 0x13, 0x23, 0x33, 0x43,
    0x53, 0x63, 0x73, (byte) 0x83, (byte) 0x93, (byte) 0xA3, (byte) 0xB3,
    (byte) 0xC3, (byte) 0xD3, (byte) 0xE3, (byte) 0xF3, 0x04, 0x14, 0x24,
    0x34, 0x44, 0x54, 0x64, 0x74, (byte) 0x84, (byte) 0x94, (byte) 0xA4,
    (byte) 0xB4, (byte) 0xC4, (byte) 0xD4, (byte) 0xE4, (byte) 0xF4, 0x05,
    0x15, 0x25, 0x35, 0x45, 0x55, 0x65, 0x75, (byte) 0x85, (byte) 0x95,
    (byte) 0xA5, (byte) 0xB5, (byte) 0xC5, (byte) 0xD5, (byte) 0xE5,
    (byte) 0xF5, 0x06, 0x16, 0x26, 0x36, 0x46, 0x56, 0x66, 0x76, (byte) 0x86,
    (byte) 0x96, (byte) 0xA6, (byte) 0xB6, (byte) 0xC6, (byte) 0xD6,
    (byte) 0xE6, (byte) 0xF6, 0x07, 0x17, 0x27, 0x37, 0x47, 0x57, 0x67, 0x77,
    (byte) 0x87, (byte) 0x97, (byte) 0xA7, (byte) 0xB7, (byte) 0xC7,
    (byte) 0xD7, (byte) 0xE7, (byte) 0xF7, 0x08, 0x18, 0x28, 0x38, 0x48,
    0x58, 0x68, 0x78, (byte) 0x88, (byte) 0x98, (byte) 0xA8, (byte) 0xB8,
    (byte) 0xC8, (byte) 0xD8, (byte) 0xE8, (byte) 0xF8, 0x09, 0x19, 0x29,
    0x39, 0x49, 0x59, 0x69, 0x79, (byte) 0x89, (byte) 0x99, (byte) 0xA9,
    (byte) 0xB9, (byte) 0xC9, (byte) 0xD9, (byte) 0xE9, (byte) 0xF9, 0x0A,
    0x1A, 0x2A, 0x3A, 0x4A, 0x5A, 0x6A, 0x7A, (byte) 0x8A, (byte) 0x9A,
    (byte) 0xAA, (byte) 0xBA, (byte) 0xCA, (byte) 0xDA, (byte) 0xEA,
    (byte) 0xFA, 0x0B, 0x1B, 0x2B, 0x3B, 0x4B, 0x5B, 0x6B, 0x7B, (byte) 0x8B,
    (byte) 0x9B, (byte) 0xAB, (byte) 0xBB, (byte) 0xCB, (byte) 0xDB,
    (byte) 0xEB, (byte) 0xFB, 0x0C, 0x1C, 0x2C, 0x3C, 0x4C, 0x5C, 0x6C, 0x7C,
    (byte) 0x8C, (byte) 0x9C, (byte) 0xAC, (byte) 0xBC, (byte) 0xCC,
    (byte) 0xDC, (byte) 0xEC, (byte) 0xFC, 0x0D, 0x1D, 0x2D, 0x3D, 0x4D,
    0x5D, 0x6D, 0x7D, (byte) 0x8D, (byte) 0x9D, (byte) 0xAD, (byte) 0xBD,
    (byte) 0xCD, (byte) 0xDD, (byte) 0xED, (byte) 0xFD, 0x0E, 0x1E, 0x2E,
    0x3E, 0x4E, 0x5E, 0x6E, 0x7E, (byte) 0x8E, (byte) 0x9E, (byte) 0xAE,
    (byte) 0xBE, (byte) 0xCE, (byte) 0xDE, (byte) 0xEE, (byte) 0xFE, 0x0F,
    0x1F, 0x2F, 0x3F, 0x4F, 0x5F, 0x6F, 0x7F, (byte) 0x8F, (byte) 0x9F,
    (byte) 0xAF, (byte) 0xBF, (byte) 0xCF, (byte) 0xDF, (byte) 0xEF,
    (byte) 0xFF
};
/*
 * The virtual memory arrays that store the loaded rom and perform the
 * functions of the gameboys memory. Not all PAGED_RAM is needed if the
 * cartridge is an early one. The same must be true of VRAM. The PAGED_RAM is
 * larger, and the access points are limited, so we catch the page faults, and
 * reallocate if it faults.
 *
 * With a segmented implementation for the main memory, we save much of
 * David Winchurch's 64 K GB_MEM. Given that the original machine only had
 * 8 K RAM, 64 K is somewhat profligate.
 *
 * In an effort to avoid blowing up during initialisation, we postpone
 * allocation of GB_MEM until after the ROM is loaded.
 */
private byte LOW_MEM[],
        HIGH_MEM[],
        AC_MEM[],
        CF_MEM[],
        VRAM[],
        CART_RAM[],
        PAGED_RAM[],
        CART_ROM[],
/*
 * The following pair are used to map the correct segment of memory for DMA
 * operations etc., and to save on memory management lookups.
 */
        srcArray[],
        destArray[];
private long thisTime;
private long lastTime;
private int drawTime;
private final int lowDelayAddr;
private final int highDelayAddr;
private final String romFileName;
/*
 * The virtual video subsystems
 */
private GameBoyVideo VIDEO; //    Gameboy Video sub-system emulation
private GameBoyAudible SOUND; //    Gameboy Sound sub-system emulation
private byte A, B, C, D, E, F, H, L, //    The virtual 8 bit registers
        tempChar;
private int PC, SP, HL, //    The virtual 16 bit registers
        xAddr, MBC, MBC_MODE, RAM_PAGE, ROM_PAGE, TIMER, TIMER_MAX,
        //    Internal variables required for certain subsystem emulation
        CPU_CYCLES, CPU_SPEED, TIMER_CYCLES, DIV, opCode,
        xRegister;
private boolean APPLET_RUNNING, //  Controls the start stop state of the thread
        IME, //    Interrupt master enable
        STOP, HALT, //    Controls the state of the cpu
        //    Internal variables required for certain subsystem emulation
        HALT_NEXT, TIMER_ON, RAM_ENABLE, RUMBLE_PACK;
private volatile boolean KEYS_PRESSED[];
//    Stores which virtual joypad buttons have been pressed
private int toRefresh;           //    Scan line counter
private int lazy;                //    Missed Scan line counter
//    The thread
private Thread instance;
//    Unchangeable values storing the flag bits
private static final byte Z_FLAG = (byte) 0x80; //    1000 0000
private static final byte N_FLAG = 0x40; //    0100 0000
private static final byte H_FLAG = 0x20; //    0010 0000
private static final byte C_FLAG = 0x10; //    0001 0000
/*
 * Unchangeable values representing important Gameboy registers
 * These are in the last 512 bytes of the memory (HIGH_MEM), so
 * the virtual addresses are 0xFE00 greater than these values
 */
private static final int JOYPAD = 0x100;
private static final int SERIAL_DATA = 0x101;
private static final int SERIAL_CTRL = 0x102;
private static final int DIV_CNTR = 0x104;
private static final int TIMER_COUNT = 0x105;
private static final int TIMER_RELOAD = 0x106;
private static final int TIMER_CRTL = 0x107;
private static final int INT_FLAG = 0x10F;
public static final int SND_1_ENT = 0x110;
public static final int SND_1_WAV_LEN = 0x111;
public static final int SND_1_ENV = 0x112;
public static final int SND_1_FREQ_KICK_LOWER = 0x113;
public static final int SND_1_FREQ_KICK_UPPER = 0x114;
public static final int SND_2_WAV_LEN = 0x116;
public static final int SND_2_ENV = 0x117;
public static final int SND_2_FREQ_KICK_LOWER = 0x118;
public static final int SND_2_FREQ_KICK_UPPER = 0x119;
public static final int SND_3_ON_OFF = 0x11A;
public static final int SND_3_LEN = 0x11B;
public static final int SND_3_VOLUME = 0x11C;
public static final int SND_3_FREQ_KICK_LOWER = 0x11D;
public static final int SND_3_FREQ_KICK_UPPER = 0x11E;
public static final int SND_4_LEN = 0x120;
public static final int SND_4_ENV = 0x121;
public static final int SND_4_POLY_KICK_LOWER = 0x122;
public static final int SND_4_POLY_KICK_UPPER = 0x123;
public static final int SND_VOICE_INP = 0x124;
public static final int SND_STEREO = 0x125;
public static final int SND_STAT = 0x126;
public static final int SND_BNK_10 = 0x130;
public static final int SND_BNK_11 = 0x131;
public static final int SND_BNK_12 = 0x132;
public static final int SND_BNK_13 = 0x133;
public static final int SND_BNK_14 = 0x134;
public static final int SND_BNK_15 = 0x135;
public static final int SND_BNK_16 = 0x136;
public static final int SND_BNK_17 = 0x137;
public static final int SND_BNK_20 = 0x138;
public static final int SND_BNK_21 = 0x139;
public static final int SND_BNK_22 = 0x13A;
public static final int SND_BNK_23 = 0x13B;
public static final int SND_BNK_24 = 0x13C;
public static final int SND_BNK_25 = 0x13D;
public static final int SND_BNK_26 = 0x13E;
public static final int SND_BNK_27 = 0x13F;
public static final int LCD_CTRL = 0x140;
public static final int LCD_STAT = 0x141;
public static final int LCD_SCROLL_Y = 0x142;
public static final int LCD_SCROLL_X = 0x143;
public static final int LCD_Y_LOC = 0x144;
public static final int LCD_Y_COMP = 0x145;
public static final int LCD_DMA = 0x146;
public static final int LCD_BACK_PALETTE = 0x147;
public static final int LCD_SPR0_PALETTE = 0x148;
public static final int LCD_SPR1_PALETTE = 0x149;
public static final int LCD_WIN_Y = 0x14A;
public static final int LCD_WIN_X = 0x14B;
private static final int CPU_SPEED_REG = 0x14D; //    GBC
private static final int VRAM_BANK = 0x14F;     //    GBC
private static final int DMA_SRC_UPPER = 0x151; //    GBC
private static final int DMA_SRC_LOWER = 0x152; //    GBC
private static final int DMA_DST_UPPER = 0x153; //    GBC
private static final int DMA_DST_LOWER = 0x154; //    GBC
private static final int DMA_LEN_TYPE = 0x155;  //    GBC
private static final int IR_PORT = 0x156;       //    GBC
private static final int BGP_INDEX = 0x168;     //    GBC
private static final int BGP_DATA = 0x169;      //    GBC
private static final int OBP_INDEX = 0x16A;     //    GBC
private static final int OBP_DATA = 0x16B;      //    GBC
private static final int RAM_BANK = 0x170;      //    GBC
private static final int INT_ENABLE = 0x1FF;
//    Unchangeable values representing keys
private static final int DIK_UP = 0;
private static final int DIK_DOWN = 1;
private static final int DIK_LEFT = 2;
private static final int DIK_RIGHT = 3;
private static final int DIK_A = 4;
private static final int DIK_B = 5;
private static final int DIK_START = 6;
private static final int DIK_SELECT = 7;
/**

 Constructor.

 @param keys pointer to the keys pressed array
 @param video; the GameBoy Video sub-system.
 @param fileName; the GameBoy ROM
 @param low; the lower address for the video wait
 @param high; higher address for the video wait

 **/

public GameBoyCPU(boolean[] keys, GameBoyVideo video, String fileName, int low,
int high)
{
    //    Notes the key array and the Video instance
    VIDEO = video;
    KEYS_PRESSED = keys;
    romFileName = fileName;
    lowDelayAddr = low;
    highDelayAddr = high;
}

/**

Starts the thread and causes the virtual cpu to start

 **/

public void startThread()
{
    if (instance == null)
    {
        APPLET_RUNNING = true;
        instance = new Thread(this);
        instance.start();
    }
}

/**

 Stops the thread thus causing the virtual CPU to stop

 **/

public void stopThread()
{
    APPLET_RUNNING = false;
    instance = null;
    if (SOUND != null)
    {
//        SOUND.stopThread();
        SOUND = null;
    }

}

/**

 Called when the thread starts; constantly calls opCodeList()

 **/

public void run()
{
    instance.setPriority(Thread.MIN_PRIORITY);
    try
    {
        while (APPLET_RUNNING)
        {
            opCodeList(16000);
            try
            {
                if (SOUND != null)
                    SOUND.outputSound(32);
            }
            catch (Throwable e)
            {
                 e.printStackTrace();
                 System.err.println("Problem with Sound");
                 SOUND = null;
            }
        }
    }
    catch (Throwable e)
    {
        e.printStackTrace();
        System.err.println("Problem executing game");
/*
 * J2SE 1.4 required ...
 *
 * StackTraceElement stack[] = e.getStackTrace();
 *  
 *  stack[0] contains the method that created the exception.
 *  stack[stack.length-1] contains the oldest method call.
 *  Enumerate each stack element.
 *
 *     for (int i=0; i<stack.length; i++)
 *     {
 *         String filename = stack[i].getFileName();
 *         if (filename == null) {
 *             // The source filename is not available
 *         }
 *         String className = stack[i].getClassName();
 *         String methodName = stack[i].getMethodName();
 *         boolean isNativeMethod = stack[i].isNativeMethod();
 *         int line = stack[i].getLineNumber();
 *         System.out.println(className+"."+methodName+ " line " + line);
 *     }
 */
    }
}

/**

 Look for delay loops in the ROM and patch them.

 **/
private void patchDelay()
{
    try
    {
/*
 * Look for a straightforward delay. Reduce it to zero.
 */
        for (int i = 4; i < CART_ROM.length;)
        {
            switch (CART_ROM[i])
            {
            case 0x3E: /*    LD A, $xx */
                if (CART_ROM[i + 2] == 0x3D && CART_ROM[i + 3] == 0x20
                        && CART_ROM[i + 4] == -3)
                {
//    System.out.println("D - " + Integer.toHexString(i + 1) + " " +
//    Integer.toHexString(((int) CART_ROM[i + 1]) & 0xFF));
                    CART_ROM[i + 1] = 1;
                }
                i += 5;
                break;

            case 0x3D: /*    DEC A */
                if (CART_ROM[i - 2] == 0x3E && CART_ROM[i + 1] == 0x20
                        && CART_ROM[i + 2] == -3)
                {
//    System.out.println("D - " + Integer.toHexString(i - 1) + " " +
//    Integer.toHexString(((int) CART_ROM[i - 1]) & 0xFF));
                    CART_ROM[i - 1] = 1;
                }
                i += 5;
                break;

            case 0x20: /*    JR NZ */
                if (CART_ROM[i - 3] == 0x3E && CART_ROM[i - 1] == 0x3D
                        && CART_ROM[i + 1] == -3)
                {
//    System.out.println("D - " + Integer.toHexString(i - 2) + " " +
//    Integer.toHexString(((int) CART_ROM[i - 2]) & 0xFF));
                    CART_ROM[i - 2] = 1;
                }
                i += 5;
                break;

            case -3: /*    Skip back */
                if (CART_ROM[i - 4] == 0x3E && CART_ROM[i - 2] == 0x3D
                        && CART_ROM[i - 1] == 0x20)
                {
//    System.out.println("D - " + Integer.toHexString(i - 3) + " " + 
//    Integer.toHexString(((int) CART_ROM[i - 3]) & 0xFF));
                    CART_ROM[i - 3] = 1;
                }
                i += 5;
                break;

            default:
                i += 3;
                break;
            }
        }
/*
 * Look for busy waits (probably associated with the the LCD registers).
 * Patch them out.
 */
//    for (int i = 0; i < CART_ROM.length; )
//    {
//    if (CART_ROM[i] != -16)
//    i++;
//    else
//    {
//    int j = i;
//
//    i++;
//    //            if (CART_ROM[i] != 0x41 && CART_ROM[i] != 0x44 && CART_ROM[i] != (byte) (0x85))
//    //                continue;
//    i++;
//    while (i < CART_ROM.length && i < (j + 126)
//    && CART_ROM[i] != -16
//    && CART_ROM[i] != 0x30
//    && CART_ROM[i] != 0x28
//    && CART_ROM[i] != 0x20
//    && CART_ROM[i] != 0x38)
//    i++;
//    if (i < CART_ROM.length && i < (j + 126))
//    {
//    if (CART_ROM[i] == -16)
//    continue;
//    i++;
//    if (CART_ROM[i] == (byte) (j - i - 1))
//    {
//    System.out.println("I - " + Integer.toHexString(j) + " " + 
//    Integer.toHexString(((int) CART_ROM[j + 1]) & 0xFF) + " " +
//    Integer.toHexString(i));
//    i--;
//    //                    if (CART_ROM[i] == 0x20)
//    //                        CART_ROM[i] = (byte) 0xE3;
//    //                    else
//    //                    if (CART_ROM[i] == 0x28)
//    //                        CART_ROM[i] = (byte) 0xE4;
//    //                    else
//    //                    if (CART_ROM[i] == 0x30)
//    //                        CART_ROM[i] = (byte) 0xEB;
//    //                    else
//    //                    if (CART_ROM[i] == 0x38)
//    //                        CART_ROM[i] = (byte) 0xEC;
//    i += 2;
//    continue;
//    }
//    }
//    }
//    }
    }
    catch (Exception e)
    {}
    return;
}

/**

  Load a GameBoy ROM into memory.

  @return true should the loading of the rom be successful

 **/

public boolean loadRom()
{
boolean romLoaded; //    Stores the state of the loading process

    LOW_MEM = null;
    LOW_MEM = new byte[0x8000]; /*    The minimum size for a ROM */

    try
    {
/*
 * Open the file
 */
        InputStream romInput = getClass().getResourceAsStream(romFileName);
/*
 * Loads the first 0x8000 bytes into the virtual memory. This represents the
 * first 2 pages of the ROM, the minimum in any ROM
 */
        int rd = 0;
        int len = 0;
        while (len < 0x8000)
        {
            rd = romInput.read(LOW_MEM, len, (0x8000 - len));
            if (rd <= 0)
                break;
            len += rd;
        }
        if (len != 0x8000)
        {
            System.out.println("Could only read " + len + " from ROM file; " +
                    " should be able to read at least 32768 bytes"
                            + " so it may be corrupt");
        }

/*
 * Calculate the array size needed to store the ROM
 */
        int fileSize;
        switch (LOW_MEM[0x148])
        {
        case 0x00: //    256Kbit=32KByte=2 banks
        case 0x01: //    512Kbit=64KByte=4 banks
        case 0x02: //    1Mbit=128KByte=8 banks
        case 0x03: //    2Mbit=256KByte=16 banks
        case 0x04: //    4Mbit=512KByte=32 banks
        case 0x05: //    8Mbit=1MByte=64 banks
        case 0x06: //    16Mbit=2MByte=128 banks
        case 0x07: //    32Mbit=4MByte=256 banks
            fileSize = 0x4000 << (LOW_MEM[0x148] + 1);
            break;

        case 0x52: //    9Mbit=1.1MByte=72 banks
            fileSize = 0x120000;
            break;

        case 0x53: //    10Mbit=1.2MByte=80 banks
            fileSize = 0x140000;
            break;

        case 0x54: //    12Mbit=1.5MByte=96 banks
            fileSize = 0x180000;
            break;

        default:
            System.out.println(
                    "Unknown file size, indicated by "
                            + Integer.toHexString(LOW_MEM[0x148]));
            fileSize = -1;
            break;
        }
/*
 * Creates an array big enough to load the whole ROM
 */
        if (fileSize > 0x8000)
        {
            CART_ROM = new byte[fileSize];
/*
 * Copy the already loaded portion of the rom into the new array
 */
            System.arraycopy(LOW_MEM, 0, CART_ROM, 0, 0x8000);
            LOW_MEM = CART_ROM;  /* Save on the duplicated memory */
            rd = 0;
            len = 0x8000;
            while (len < fileSize)
            {
                rd = romInput.read(CART_ROM, len, (fileSize - len));
                if (rd <= 0)
                    break;
                len += rd;
            }
            if (len != fileSize)
            {
                System.out.println("Could only read " + len +
                     " from ROM file; " +
                    " should have read " + fileSize + " bytes"
                            + " so it may be corrupt");
            }
        }
        else
            CART_ROM = LOW_MEM;      /* CART_ROM and LOW_MEM are synonymous */
/*
 * Close the file
 */
        romInput.close();
        patchDelay();
/*
 * Calculate the file size needed to store the RAM in.
 */
        int ramSize;
        switch (LOW_MEM[0x149])
        {
        case 0x00:
            ramSize = 0x00;
            break;

        case 0x01:
            ramSize = 0x500;
            break;

        case 0x02:
            ramSize = 0x2000;
            break;

        case 0x03:
            ramSize = 0x8000;
            break;

        case 0x04:
            ramSize = 0x16000;
            break;

        default:
            System.out.println(
                    "Unknown RAM size, indicated by "
                            + Integer.toHexString(LOW_MEM[0x149]));
            ramSize = 0;
            break;
        }
        if (ramSize > 0)
            CART_RAM = new byte[ramSize];
        else
            CART_RAM = null;
/*
 * If the method has got this far the ROM has been loaded successfully
 */
//    System.out.println("New ROM size=" + fileSize + " RAM size=" + ramSize);
        romLoaded = true;
    }
    catch (Exception ex)
    {
        ex.printStackTrace();
        System.out.println("Attempt to load ROM failed");
        romLoaded = false;
    }
//    Return the success (or otherwise) of the ROM loading procedure
    return romLoaded;
}

/**

 Resets all the important virtual memory locations and subsystems back to
 the values they would have in the original GameBoy at Power-On.

 **/

public void fullReset()
{
    int i;
//    The 16 bit register initial values
    SP = 0xFFFE;
    PC = 0x0100;
//    Ensure that the 8 bit registers contain the correct initial values
    A = 0x11;
    F = (byte) 0xB0; //    GBC
    B = 0;
    C = 0x13;
    D = 0;
    E = (byte) 0xD8;
    H = 1;
    L = 0x4D;
//    Refresh the arrays
    AC_MEM = null;
    CF_MEM = null;
    HIGH_MEM = null;
    srcArray = null;
    destArray = null;
    AC_MEM = new byte[0x2000];
    CF_MEM = new byte[0x2000];
    HIGH_MEM = new byte[0x200];
    if (CART_ROM != null)
    {
        System.arraycopy(CART_ROM, 0, LOW_MEM, 0, 0x8000);
        if ((CART_ROM[0x0143] & 0x80) == 0)
            VIDEO.bwSetup();
    }
    VRAM = null;
    VRAM = new byte[0x4000];
    PAGED_RAM = null;
    VIDEO.init(LOW_MEM, HIGH_MEM, VRAM);
    PAGED_RAM = new byte[0x1000];
/*
 * Set internal CPU state variables to initial values
 */
    MBC = 0;
    MBC_MODE = 0;
    RAM_PAGE = 0;
    ROM_PAGE = 0;
    TIMER = 0;
    TIMER_MAX = 0;
    CPU_CYCLES = 0;
    CPU_SPEED = 0;
    TIMER_CYCLES = 0;
    DIV = 0;
//    SPR_PER_LINE= new int[144];
    IME = false;
    STOP = false;
    HALT = false;
    HALT_NEXT = false;
    TIMER_ON = false;
    RAM_ENABLE = false;
    RUMBLE_PACK = false;
/*
 * Certain virtual memory locations must have specific values
 */
    HIGH_MEM[JOYPAD] = (byte) 0xCF;
    HIGH_MEM[SERIAL_DATA] = 0x00;
    HIGH_MEM[SERIAL_CTRL] = 0x7E;
    HIGH_MEM[0x103] = (byte) 0xFF;
    HIGH_MEM[DIV_CNTR] = (byte) 0xAF;
    HIGH_MEM[TIMER_COUNT] = 0x00;
    HIGH_MEM[TIMER_RELOAD] = 0x00;
    HIGH_MEM[TIMER_CRTL] = (byte) 0xF8;
    HIGH_MEM[0x108] = (byte) 0xFF;
    HIGH_MEM[0x109] = (byte) 0xFF;
    HIGH_MEM[0x10A] = (byte) 0xFF;
    HIGH_MEM[0x10B] = (byte) 0xFF;
    HIGH_MEM[0x10C] = (byte) 0xFF;
    HIGH_MEM[0x10D] = (byte) 0xFF;
    HIGH_MEM[0x10E] = (byte) 0xFF;
    HIGH_MEM[INT_FLAG] = 0x00;
    HIGH_MEM[SND_1_ENT] = (byte) 0x80;
    HIGH_MEM[SND_1_WAV_LEN] = (byte) 0xBF;
    HIGH_MEM[SND_1_ENV] = (byte) 0xF3;
    HIGH_MEM[SND_1_FREQ_KICK_LOWER] = (byte) 0xFF;
    HIGH_MEM[SND_1_FREQ_KICK_UPPER] = (byte) 0xBF;
    HIGH_MEM[0x115] = (byte) 0xFF;
    HIGH_MEM[SND_2_WAV_LEN] = (byte) 0x3F;
    HIGH_MEM[SND_2_ENV] = 0x00;
    HIGH_MEM[SND_2_FREQ_KICK_LOWER] = (byte) 0xFF;
    HIGH_MEM[SND_2_FREQ_KICK_UPPER] = (byte) 0xBF;
    HIGH_MEM[SND_3_ON_OFF] = 0x7F;
    HIGH_MEM[SND_3_LEN] = (byte) 0xFF;
    HIGH_MEM[SND_3_VOLUME] = (byte) 0x9F;
    HIGH_MEM[SND_3_FREQ_KICK_LOWER] = (byte) 0xFF;
    HIGH_MEM[SND_3_FREQ_KICK_UPPER] = (byte) 0xBF;
    HIGH_MEM[0x11E] = (byte) 0xFF;
    HIGH_MEM[0x11F] = (byte) 0xFF;
    HIGH_MEM[SND_4_LEN] = (byte) 0xFF;
    HIGH_MEM[SND_4_ENV] = 0x00;
    HIGH_MEM[SND_4_POLY_KICK_LOWER] = 0x00;
    HIGH_MEM[SND_4_POLY_KICK_UPPER] = (byte) 0xBF;
    HIGH_MEM[SND_VOICE_INP] = 0x77;
    HIGH_MEM[SND_STEREO] = (byte) 0xF3;
    HIGH_MEM[SND_STAT] = (byte) 0xF1;
    HIGH_MEM[SND_BNK_10] = 0x06;
    HIGH_MEM[SND_BNK_11] = (byte) 0xFE;
    HIGH_MEM[SND_BNK_12] = 0x0E;
    HIGH_MEM[SND_BNK_13] = 0x7F;
    HIGH_MEM[SND_BNK_14] = 0x00;
    HIGH_MEM[SND_BNK_15] = (byte) 0xFF;
    HIGH_MEM[SND_BNK_16] = 0x58;
    HIGH_MEM[SND_BNK_17] = (byte) 0xDF;
    HIGH_MEM[SND_BNK_20] = 0x00;
    HIGH_MEM[SND_BNK_21] = (byte) 0xEC;
    HIGH_MEM[SND_BNK_22] = 0x00;
    HIGH_MEM[SND_BNK_23] = (byte) 0xBF;
    HIGH_MEM[SND_BNK_24] = 0x0C;
    HIGH_MEM[SND_BNK_25] = (byte) 0xED;
    HIGH_MEM[SND_BNK_26] = 0x03;
    HIGH_MEM[SND_BNK_27] = (byte) 0xF7;
    if (SOUND != null)
    {
//        SOUND.stopThread();
        SOUND = null;
    }
    try
    {
        SOUND = (GameBoyAudible) new GameBoySound(HIGH_MEM);
    }
//    catch (Exception e1)
//    {
//        try
//        {
//            SOUND = (GameBoyAudible) new GameBoyTone(HIGH_MEM);
//        }
        catch (Exception e)
        {
            SOUND = null;
            e.printStackTrace();
            System.out.println("Failed to start Sound; continuing in silence");
        }
//    }
    HIGH_MEM[LCD_CTRL] = (byte) 0x91;
    HIGH_MEM[LCD_STAT] = (byte) 0x85;
    HIGH_MEM[LCD_SCROLL_Y] = 0x00;
    HIGH_MEM[LCD_SCROLL_X] = 0x00;
    HIGH_MEM[LCD_Y_LOC] = 0x00;
    HIGH_MEM[LCD_Y_COMP] = 0x00;
    HIGH_MEM[LCD_DMA] = 0x00;
    HIGH_MEM[LCD_BACK_PALETTE] = (byte) 0xFC;
    HIGH_MEM[LCD_SPR0_PALETTE] = (byte) 0xFF;
    HIGH_MEM[LCD_SPR1_PALETTE] = (byte) 0xFF;
    HIGH_MEM[LCD_WIN_Y] = 0x00;
    HIGH_MEM[LCD_WIN_X] = 0x00;
    HIGH_MEM[0x14C] = (byte) 0xFF;
    HIGH_MEM[CPU_SPEED_REG] = 0x7E;
    HIGH_MEM[0x14E] = (byte) 0xFF;
    HIGH_MEM[VRAM_BANK] = (byte) 0xFE;
    HIGH_MEM[0x150] = (byte) 0xFF;
    HIGH_MEM[DMA_SRC_UPPER] = 0x00;
    HIGH_MEM[DMA_SRC_LOWER] = 0x00;
    HIGH_MEM[DMA_DST_UPPER] = 0x00;
    HIGH_MEM[DMA_DST_LOWER] = 0x00;
    HIGH_MEM[DMA_LEN_TYPE] = (byte) 0xFF;
    HIGH_MEM[IR_PORT] = 0x00;
    HIGH_MEM[BGP_INDEX] = (byte) 0xC0;
    HIGH_MEM[BGP_DATA] = 0x00;
    HIGH_MEM[OBP_INDEX] = (byte) 0xC1;
    HIGH_MEM[OBP_DATA] = 0x00;
    HIGH_MEM[RAM_BANK] = (byte) 0xF8;
    HIGH_MEM[INT_ENABLE] = 0x00;
/*
 *    Work out the configuration of the ROM's MBC
 */
    switch ((int) (LOW_MEM[0x0147] & 0xFF))
    {
    case 0x00: //    return "ROM ONLY";
    case 0x08: //    return "ROM+CART_RAM";
    case 0x09: //    return "ROM+CART_RAM+BATTERY";
    case 0x1F: /*    FIND OUT*///    return "POCKET CAMERA";
    case 0xFD: /*    FIND OUT*///    return "BANDAI TAMA5";
        MBC = 0;
        break;

    case 0x01: //    return "ROM+MBC1";
    case 0x02: //    return "ROM+MBC1+CART_RAM";
    case 0x03: //    return "ROM+MBC1+CART_RAM+BATTERY";
    case 0xFF: //    return "HUDSON HuC-1";
        MBC = 1;
        break;

    case 0x05: //    return "ROM+MBC2";
    case 0x06: //    return "ROM+MBC2+BATTERY";
        MBC = 2;
        break;

    case 0x0B: //    return "ROM+MMM01";
    case 0x0C: //    return "ROM+MMM01+SRAM";
    case 0x0D: //    return "ROM+MMM01+SRAM+BATTERY";
        MBC = 101;
        break;

    case 0x0F: //    return "ROM+MBC3+TIMER+BATTERY";
    case 0x10: //    return "ROM+MBC3+TIMER+CART_RAM+BATTERY";
    case 0x11: //    return "ROM+MBC3";
    case 0x12: //    return "ROM+MBC3+CART_RAM";
    case 0x13: //    return "ROM+MBC3+CART_RAM+BATTERY";
    case 0xFE: //    return "HUDSON HuC-3";
        MBC = 3;
        break;

    case 0x15: //    return "ROM+MBC4";
    case 0x16: //    return "ROM+MBC4+CART_RAM";
    case 0x17: //    return "ROM+MBC4+CART_RAM+BATTERY";
        MBC = 4;
        break;

    case 0x1C: //    return "ROM+MBC5+RUMBLE";
    case 0x1D: //    return "ROM+MBC5+RUMBLE+SRAM";
    case 0x1E: //    return "ROM+MBC5+RUMBLE+SRAM+BATTERY";
        RUMBLE_PACK = true;

    case 0x19: //    return "ROM+MBC5";
    case 0x1A: //    return "ROM+MBC5+CART_RAM";
    case 0x1B: //    return "ROM+MBC5+CART_RAM+BATTERY";
        MBC = 5;
        break;

    default:
        System.out.println(
                "Unknown MBC indicator "
                        + Integer.toHexString((LOW_MEM[0x0147] & 0xFF)));
        break;
    }
/*
 * Default the palette colours
 */
    VIDEO.resetPalettes();
/*
 * Re-calculate the colour values using values from the ROM.
 */
    VIDEO.setBackPalette();
    VIDEO.setForePalette0();
    VIDEO.setForePalette1();
/*
 * Set video and timer sub-systems to defaults
 */
    setLCDControl();
    setTimer();
    thisTime = System.currentTimeMillis();
    drawTime = 33;
}

/**

  Gets the virtual video subsystem's instance

  @return the Video sub-system

 **/

public GameBoyVideo getVideo()
{
    return VIDEO;
}

/**

 Assign variables used by the Video sub-system

 **/

private void setLCDControl()
{
    VIDEO.setWinPointers((HIGH_MEM[LCD_CTRL] & 0x40) != 0 ? 0x1C00 : 0x1800);
    VIDEO.setTileArea((HIGH_MEM[LCD_CTRL] & 0x10) != 0 ? 0 : 0x800);
    VIDEO.setBGPointers((HIGH_MEM[LCD_CTRL] & 0x08) != 0 ? 0x1C00 : 0x1800);
}

/**

  Calculate the values used in the timer subsystem

 **/

private void setTimer()
{
    TIMER_ON = ((HIGH_MEM[TIMER_CRTL] & 0x04) != 0 ? true : false);
/*
 * Try to make up for very slow emulators
    TIMER_MAX=((HIGH_MEM[TIMER_CRTL]&0x02)!=0
    ? ((HIGH_MEM[TIMER_CRTL]&0x01)!=0? 256:64)
    : ((HIGH_MEM[TIMER_CRTL]&0x01)!=0? 1024:16));
 */
    TIMER_MAX = ((HIGH_MEM[TIMER_CRTL] & 0x02) != 0
            ? ((HIGH_MEM[TIMER_CRTL] & 0x01) != 0 ? 16 : 4)
            : ((HIGH_MEM[TIMER_CRTL] & 0x01) != 0 ? 64 : 1));
}

/**

  Set certain locations in the virtual memory array depending on virtual
  joypad input

 **/

private void handleGBInput()
{
    HIGH_MEM[JOYPAD] &= 0xF0;
    if (HIGH_MEM[JOYPAD] == 0x30)
        HIGH_MEM[JOYPAD] = 0x3F;
    if (HIGH_MEM[JOYPAD] == 0x20) //    Check UP, DOWN, LEFT, RIGHT
    {
        if (!KEYS_PRESSED[DIK_DOWN])
            HIGH_MEM[JOYPAD] |= 0x08;
        if (!KEYS_PRESSED[DIK_UP])
            HIGH_MEM[JOYPAD] |= 0x04;
        if (!KEYS_PRESSED[DIK_LEFT])
            HIGH_MEM[JOYPAD] |= 0x02;
        if (!KEYS_PRESSED[DIK_RIGHT])
            HIGH_MEM[JOYPAD] |= 0x01;
    }
    else
    if (HIGH_MEM[JOYPAD] == 0x10) //    Check A, B, SELECT, START
    {
        if (!KEYS_PRESSED[DIK_START])
            HIGH_MEM[JOYPAD] |= 0x08;
        if (!KEYS_PRESSED[DIK_SELECT])
            HIGH_MEM[JOYPAD] |= 0x04;
        if (!KEYS_PRESSED[DIK_B])
            HIGH_MEM[JOYPAD] |= 0x02;
        if (!KEYS_PRESSED[DIK_A])
            HIGH_MEM[JOYPAD] |= 0x01;
    }
    if (drawTime > 33 && (toRefresh == 0 || (toRefresh == -1 && lazy > 320)))
    {
        thisTime = System.currentTimeMillis();
        if ((thisTime - lastTime) > (drawTime))
        {
            toRefresh = 144;
            lazy = 0;
        }
    }
}

/**

  Gets a specified location from the virtual memory
  The xAddr side effect is very important.

  @param loc the virtual address
  @return the appropriate value

 **/
public final byte readMem(int loc)
{
//    if (toRefresh > 0 && (loc == 0xFF41 || loc == 0xFF44))
//    {
//    for (toRefresh =(((int) HIGH_MEM[LCD_Y_LOC]) & 0xFF);toRefresh < 144; toRefresh++)
//    VIDEO.drawLine(toRefresh);
//    HIGH_MEM[LCD_Y_LOC] = (byte) 144;
//    VIDEO.repaint();
//    lazy = 500;
//    toRefresh = 0;
//    }
    if (loc < 0x4000)
        srcArray = LOW_MEM;
    else
    if (loc >= 0xFE00)
    {
        loc -= 0xFE00;
        srcArray = HIGH_MEM;
    }
    else
    if ((loc >= 0x8000) && (loc < 0xA000))
    {
        loc = (loc - 0x8000)
                + (((HIGH_MEM[VRAM_BANK] & 0x01) == 0) ? 0 : 0x2000);
        srcArray = VRAM;
    }
    else
    if ((loc >= 0xD000) && (loc < 0xE000))
    {
        loc = (loc - 0xD000) + (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12);
        srcArray = PAGED_RAM;
    }
    else
    if (loc >= 0xC000)
    {
        loc -= 0xC000;
        if (loc > 0x2000)
            loc -= 0x2000;
        srcArray = CF_MEM;
    }
    else
    if (loc < 0x8000)
    {
        if (MBC <= 0 || CART_ROM == null)
            srcArray = LOW_MEM;
        else
        {
            loc = loc - 0x4000 + (ROM_PAGE << 14);
            srcArray = CART_ROM;
        }
    }
    else
    {
        loc -= 0xA000;
        if (MBC > 0 && RAM_ENABLE)
        {
            loc += (RAM_PAGE << 13);
            srcArray = CART_RAM;
        }
        else
            srcArray = AC_MEM;
    }
    xAddr = loc;
/*#ExecutionTrace#*///<editor-fold>
//--       System.out.println("G " + Integer.toHexString(loc) + " - " +
//--                 Integer.toHexString(srcArray[xAddr] & 0xFF));
/*$ExecutionTrace$*///</editor-fold>

    return srcArray[xAddr];
}

/**

 Set the source array based on the memory map

 @return offset into array

 **/

private int doSrcMap(int src)
{
//    System.out.println("M " + Integer.toHexString(src));
    if (src < 0x4000)
        srcArray = LOW_MEM;
    else
    if (src >= 0xFE00)
    {
        src -= 0xFE00;
        srcArray = HIGH_MEM;
    }
    else
    if ((src >= 0x8000) && (src < 0xA000))
    {
        src = (src - 0x8000)
                + (((HIGH_MEM[VRAM_BANK] & 0x01) == 0) ? 0 : 0x2000);
        srcArray = VRAM;
    }
    else
    if ((src >= 0xD000) && (src < 0xE000))
    {
        src = (src - 0xD000) + (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12);
        srcArray = PAGED_RAM;
    }
    else
    if (src >= 0xC000)
    {
        src -= 0xC000;
        if (src > 0x2000)
            src -= 0x2000;
        srcArray = CF_MEM;
    }
    else
    if (src < 0x8000)
    {
        if (MBC <= 0 || CART_ROM == null)
            srcArray = LOW_MEM;
        else
        {
            src = src - 0x4000 + (ROM_PAGE << 14);
            srcArray = CART_ROM;
        }
    }
    else
    {
        src -= 0xA000;
        if (MBC > 0 && RAM_ENABLE)
        {
            src += (RAM_PAGE << 13);
            srcArray = CART_RAM;
        }
        else
            srcArray = AC_MEM;
    }
    return src;
}

/**

 Set the Destination array based on the memory map

 @return offset into array

 **/

private int doDestMap(int dest)
{
    if (dest >= 0xFE00)
    {
        dest -= 0xFE00;
        destArray = HIGH_MEM;
    }
    else
    if ((dest >= 0x8000) && (dest < 0xA000))
    {
        dest = (dest - 0x8000)
                + (((HIGH_MEM[VRAM_BANK] & 0x01) == 0) ? 0 : 0x2000);
        destArray = VRAM;
    }
    else
    if ((dest >= 0xD000) && (dest < 0xE000))
    {
        dest = (dest - 0xD000) + (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12);
        destArray = PAGED_RAM;
    }
    else
    if (dest >= 0xC000)
    {
        dest -= 0xC000;
        if (dest > 0x2000)
            dest -= 0x2000;
        destArray = CF_MEM;
    }
    else
    if (dest < 0x8000)
        destArray = null;
    else
    {
        dest -= 0xA000;
        if (MBC > 0 && RAM_ENABLE)
        {
            dest += (RAM_PAGE << 13);
            destArray = CART_RAM;
        }
        else
            destArray = AC_MEM;
    }
    return dest;
}

/**

 Get a 16 bit integer from the virtual memory by combining two locations

 @return 16 bit integer from memory

 **/

private final int readMemNN()
{
    xAddr = doSrcMap(PC);
    PC += 2;
    return (((int) (srcArray[xAddr + 1]) & 0xFF) << 8)
            | (((int) srcArray[xAddr]) & 0xFF);
}

/**

 Sets a location in the virtual memory

 @param loc the location in memory to be altered
 @param mem the value to be set

 **/

private void writeMem(int loc, byte mem)
{
    int src, dest;
/*#ExecutionTrace#*///<editor-fold>
//--          System.out.println( "S " + Integer.toHexString(loc) + " - " +
//--                  Integer.toHexString(mem & 0xFF));
/*$ExecutionTrace$*///</editor-fold>

    try
    {
        if (loc >= 0xFE00)
        {
            loc -= 0xFE00;
            if (loc == LCD_Y_LOC)
                HIGH_MEM[LCD_Y_LOC] = 0x00;
                //    Writes to LCD_Y_LOC reset the current horizontal
                //    scanline to 0
            else
            if (loc == DMA_LEN_TYPE && (LOW_MEM[0x0143] & 0x80) != 0)
            { //    GDMA and HDMA
                HIGH_MEM[DMA_LEN_TYPE] = mem;
/*
 * Copies a chunk of data into VRAM
 */
                dest = (((int) (HIGH_MEM[DMA_DST_UPPER] & 0xFF)) << 8)
                        + (HIGH_MEM[DMA_DST_LOWER] & 0xFF);
                src = (((int) (HIGH_MEM[DMA_SRC_UPPER] & 0xFF)) << 8)
                        + (HIGH_MEM[DMA_SRC_LOWER] & 0xFF);
                src = doSrcMap(src);
                dest = doDestMap(dest);
                System.arraycopy(srcArray, src, destArray, dest,
                        (((int) (mem & 0x7F)) + 1) << 4);
                HIGH_MEM[DMA_LEN_TYPE] &= 0x80;
            }
            else
            if (loc == DIV_CNTR)
                HIGH_MEM[DIV_CNTR] = 0x00; //    DIV writes reset it
            else
            if (loc == LCD_CTRL)
            {//    Writes to this location alter the LCDC properties
                HIGH_MEM[LCD_CTRL] = mem; 
//                if ((mem & 0x20) != 0)
//                    System.out.println("Win");
                setLCDControl();
            }
            else
            if (loc == LCD_DMA) //    DMA code
            {
                HIGH_MEM[LCD_DMA] = mem;
                src = doSrcMap(((((int) mem) & 0xFF) << 8));
                System.arraycopy(srcArray, src, HIGH_MEM, 0, 0xA0);
            }
            else
            if (loc == JOYPAD)
            { //    Writes here cause the GameBoy to handle Joypad input
                HIGH_MEM[JOYPAD] = mem;
                handleGBInput();
            }
            else
            if (loc == TIMER_CRTL)
            { //    Writes here alter the timer properties
                HIGH_MEM[TIMER_CRTL] = mem;
                setTimer();
            }
            else
            if (loc == LCD_BACK_PALETTE && (LOW_MEM[0x0143] & 0x80) == 0)
            { //    Writes here reconfigure the background palette
                HIGH_MEM[LCD_BACK_PALETTE] = mem;
                VIDEO.setBackPalette();
            }
            else
            if (loc == LCD_SPR0_PALETTE && (LOW_MEM[0x0143] & 0x80) == 0)
            { //    Writes here reconfigure the SPR0 foreground palettes
                HIGH_MEM[LCD_SPR0_PALETTE] = mem;
                VIDEO.setForePalette0();
            }
            else
            if (loc == LCD_SPR1_PALETTE && (LOW_MEM[0x0143] & 0x80) == 0)
            { //    Writes here reconfigure the SPR1 palettes
                HIGH_MEM[LCD_SPR1_PALETTE] = mem;
                VIDEO.setForePalette1();
            }
            else
            if (loc == BGP_DATA && (LOW_MEM[0x0143] & 0x80) != 0)
            { //    Writes here set a colour in one of the GameBoy Color
              //    BG palettes
                HIGH_MEM[BGP_DATA] = mem;

                int pal = (HIGH_MEM[BGP_INDEX] & 0x38) >> 3;
                int colIndex = (HIGH_MEM[BGP_INDEX] & 0x06) >> 1;
//    System.out.println("BG Before: (" + colIndex + "," + pal +")" +
//    Integer.toHexString(VIDEO.getBackColour(colIndex,pal)) +
//    " set " + Integer.toHexString(mem & 0xFF) + " flags " +
//    Integer.toHexString(HIGH_MEM[BGP_INDEX] & 0xFF));
             
                VIDEO.setBackColour(colIndex, pal,
                        GameBoyVideo.getGBColour(
                        (HIGH_MEM[BGP_INDEX] & 0x01) != 0
                                ? VIDEO.rgb2GBLower(
                                        VIDEO.getBackColour(colIndex, pal))
                                          | ((((int) mem) & 0xFF) << 8)
                                                : VIDEO.rgb2GBUpper(
                                                        VIDEO.getBackColour(
                                                                colIndex,
                                                                pal))
                                               | (((int) mem) & 0xFF)));
//    System.out.println("BG After: (" + colIndex + "," + pal +")" +
//    Integer.toHexString(VIDEO.getBackColour(colIndex,pal)));
          
                //    Increment the colour index if necessary
                if ((HIGH_MEM[BGP_INDEX] & 0x80) != 0)
                {
                    if ((HIGH_MEM[BGP_INDEX] & 0x3F) != 0x3F)
                        HIGH_MEM[BGP_INDEX]++;
                    else
                        HIGH_MEM[BGP_INDEX] &= 0xC0;
                }
            }
            else
            if (loc == OBP_DATA && (LOW_MEM[0x0143] & 0x80) != 0)
            { //    Writes here set a colour in one of the FG palettes
                HIGH_MEM[OBP_DATA] = mem;

                int pal = (HIGH_MEM[OBP_INDEX] & 0x38) >> 3;
                int colIndex = (HIGH_MEM[OBP_INDEX] & 0x06) >> 1;
//    System.out.println("FG Before: (" + colIndex + "," + pal +")" +
//    Integer.toHexString(VIDEO.getForeColour(colIndex,pal)) +
//    " set " + Integer.toHexString(mem & 0xFF) + " flags " +
//    Integer.toHexString(HIGH_MEM[OBP_INDEX] & 0xFF));
                VIDEO.setForeColour(colIndex, pal,
                        GameBoyVideo.getGBColour(
                        (HIGH_MEM[OBP_INDEX] & 0x01) != 0
                                ? VIDEO.rgb2GBLower(
                                        VIDEO.getForeColour(colIndex, pal))
                                                | ((((int) mem) & 0xFF) << 8)
                                                : VIDEO.rgb2GBUpper(
                                                        VIDEO.getForeColour(
                                                                colIndex,
                                                                pal))
                                                   | (((int) mem) & 0xFF)));
//    Increment the colour index if necessary
//    System.out.println("FG After: (" + colIndex + "," + pal +")" +
//    Integer.toHexString(VIDEO.getForeColour(colIndex,pal)));
                if (((HIGH_MEM[OBP_INDEX]) & 0x80) != 0)
                {
                    if ((HIGH_MEM[OBP_INDEX] & 0x3F) != 0x3F)
                        HIGH_MEM[OBP_INDEX]++;
                    else
                        HIGH_MEM[OBP_INDEX] &= 0xC0;
                }
            }
            else
                HIGH_MEM[loc] = mem; //    Think about invoking Sound here.
        }
        else
        if (loc >= 0xC000)
        {
            if (loc >= 0xE000)
                loc -= 0xE000;
            else
                loc -= 0xC000;
            CF_MEM[loc] = mem;
            if (loc >= 0x1000)
            {
                try
                {
                    PAGED_RAM[(loc - 0x1000) +
                      (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12)] = mem;
                }
                catch (ArrayIndexOutOfBoundsException ex)
                {
//    ex.printStackTrace();
//    System.out.println("Attempted array out of bounds write " +
//    "in PAGED_RAM at location: " + Integer.toHexString(loc));
                    byte[] new_page = new byte[0x8000];
                    System.arraycopy(PAGED_RAM, 0, new_page, 0,
                            PAGED_RAM.length);
                    PAGED_RAM = new_page;
                    PAGED_RAM[(loc - 0x1000) + (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12)] = mem;    
                }
            }
        }
        else
        if ((loc >= 0x8000) && (loc < 0xA000))
            VRAM[(loc - 0x8000) + (((HIGH_MEM[VRAM_BANK] & 0x01) == 0)
                                    ? 0 : 0x2000)] = mem;
        else
        if (loc < 0x8000)
        {
            if (MBC == 1) //    MBC1 code
            {
                if (loc < 0x2000) //    Enable RAM access
                    RAM_ENABLE = (mem & 0x0F) == 0x0A ? true : false;
                else
                if (loc < 0x4000) //    Select ROM page
                    ROM_PAGE = mem & 0x1F;
                else
                if (MBC_MODE == 0x01 && loc < 0x6000) //    Select RAM page
                    RAM_PAGE = mem & 0x03;
                else
                if (MBC_MODE == 0x00 && loc < 0x6000) // Set ROM page bits 3 and 4
                    ROM_PAGE = (ROM_PAGE & 0x07) | ((mem & 0x03) << 3);
                else
                if (loc >= 0x6000) //    Change MBC1 mode
                    MBC_MODE = mem & 0x01;
            }
            else
            if (MBC == 2) //    MBC2 code
            {
                if (loc >= 0x2000 && loc < 0x4000)
                    ROM_PAGE = mem & 0x0F;           //    Select ROM page
            }
            else
            if (MBC == 3) //    MBC3 code
            {
                if (loc < 0x2000) //    Enable RAM access
                    RAM_ENABLE = (mem & 0x0F) == 0x0A ? true : false;
                else
                if (loc < 0x4000) //    Select ROM page
                    ROM_PAGE = mem & 0x7F;
                else
                if (loc < 0x6000) //    Select RAM page
                    RAM_PAGE = mem & 0x03;
            }
            else
            if (MBC == 5) //    MBC5 code
            {
                if (loc < 0x2000) //    Enable RAM access
                    RAM_ENABLE = (mem & 0x0F) == 0x0A ? true : false;
                else
                if (loc < 0x3000) //    Select ROM page
                    ROM_PAGE = (ROM_PAGE & 0x100) | (mem & 0xFF);
                else
                if (loc < 0x4000) //    Set ROM page selection bit 8
                    ROM_PAGE |= (((int) (mem & 0x01)) << 8);
                else
                if (loc < 0x6000) //    select RAM page
                    RAM_PAGE = RUMBLE_PACK ? mem & 0x03 : mem & 0x0F;
            }
            else
            {
/*
 * Possibly an attempt to set the bank; not obvious why Tetris should call this,
 * though!
      System.out.println("Write of " + Integer.toHexString(mem & 0xFF) +
                " to location " + Integer.toHexString(loc) +
                " is not understood\n" +
                "MBC=" + MBC + " MBC_MODE=" + MBC_MODE +
                " ROM_PAGE=" + ROM_PAGE);
                System.out.println("PC="  + Integer.toHexString(PC) + " - " +
                Integer.toHexString(opCode));
                System.out.println("HL="  + Integer.toHexString(HL)
                + " SP="  + Integer.toHexString(HL));
                System.out.println( "A=" + Integer.toHexString(A)
                + " B=" + Integer.toHexString(B)
                + " C=" + Integer.toHexString(C)
                + " D=" + Integer.toHexString(D)
                + " E=" + Integer.toHexString(E)
                + " F=" + Integer.toHexString(F));
*/
            }
            if (CART_ROM != null && (ROM_PAGE << 14) >= CART_ROM.length)
            {
                System.out.println(
                        "After write of " + Integer.toHexString(mem & 0xFF)
                        + " to location " + Integer.toHexString(loc)
                        + " ROM_PAGE is illegal\n" + "MBC=" + MBC
                        + " MBC_MODE=" + MBC_MODE + " ROM_PAGE=" + ROM_PAGE);
            }
        }
        else
        if (RAM_ENABLE && CART_RAM != null)//    Cart RAM write
            CART_RAM[(loc - 0xA000) + (RAM_PAGE << 13)] = mem;
        else
            AC_MEM[(loc - 0xA000)] = mem;
    }
    catch (Exception ex)
    {
        System.out.println(
                "writeMem(loc=" + Integer.toHexString(loc) + ", mem="
                + Integer.toHexString(mem & 0xFF) + ") RAM=" + RAM_ENABLE
                + " provoked Exception " + ex);
    }
}

/**

  The add with carry op code.

  @param src the source register

 **/

private void ADC(byte src)
{
    xRegister = (((int) A) & 0xFF) + (((int) src) & 0xFF)
            + ((F & C_FLAG) != 0 ? 1 : 0);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0)
            | (((A & 0x0F) + (src & 0x0F) + ((F & C_FLAG) > 0 ? 1 : 0))
                    > 0x0F
                            ? H_FLAG
                            : 0)
                            | (xRegister > 0xFF ? C_FLAG : 0));
//    Result of addition ends up in the accumulator register
    A = (byte) (xRegister & 0xFF);
}

/**

  The 8 bit add op code.

  @param src the source register

 **/

private void ADD(byte src)
{
    xRegister = (((int) A) & 0xff) + (((int) src) & 0xff);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0)
            | (((A & 0x0F) + (src & 0x0F)) > 0x0F ? H_FLAG : 0)
            | (xRegister > 0xFF ? C_FLAG : 0));
//    Result of addition ends up in the accumulator register
    A = (byte) (xRegister & 0xFF);
}

/**

 The 16 bit add op code.

 @param src the source register

 **/

private void ADD_HL(int src)
{
    xRegister = ((((int) H) & 0xFF) << 8) + ((((int) L) & 0xFF))
            + src;
    F = (byte) ((F & Z_FLAG) | ((xRegister > 0xFFFF) ? C_FLAG : 0)
            | (((((((int) L) & 0xFF) + (((int) (H & 0xF)) << 8))
            + (src & 0x0FFF))
                    > 0x0FFF)
                            ? H_FLAG
                            : 0));
/*
 *
 * According to David Winchurch, this resets the Z flag
    F = (byte) (((xRegister == 0) ? Z_FLAG : 0)
    | ((xRegister>0xFFFF)? C_FLAG:0)
    | (((((((int)L)&0xFF) + (((int) (H&0xF))<< 8))+(src&0x0FFF))>0x0FFF)
    ? H_FLAG:0));
 */
    H = (byte) ((xRegister >> 8) & 0xFF);
    L = (byte) (xRegister & 0xFF);
}

private void ADD_HL(byte h, byte l)
{
    xRegister = ((((int) H) & 0xFF) << 8) + ((((int) L) & 0xFF))
            + (((int) (h & 0xFF)) << 8) + ((int) (l & 0xFF));
    F = (byte) ((F & Z_FLAG) | ((xRegister > 0xFFFF) ? C_FLAG : 0)
            | ((((((int) L) & 0xFF) + (((int) (H & 0xF)) << 8)
            + (((int) l) & 0xFF) + (((int) (h & 0xF)) << 8))
            > 0x0FFF)
                    ? H_FLAG
                    : 0));
/*
 *
 * According to David Winchurch, this resets the Z flag
    F = (byte) (((xRegister == 0) ? Z_FLAG : 0)
    | ((xRegister>0xFFFF)? C_FLAG:0)
    | (((((((int)L)&0xFF) + (((int) (H&0xF))<< 8))+((((int)l)&0xFF) + (((int) (h&0xF))<< 8)))>0x0FFF)
    ? H_FLAG:0));
 */
    H = (byte) ((xRegister >> 8) & 0xFF);
    L = (byte) (xRegister & 0xFF);
}

/**

 Adds a value to the stack pointer.

 @param src the source register
 @return the result of the addition

 **/

private int ADDSP(byte src)
{
    xRegister = SP + src;
    F = (byte) ((((xRegister > 0xFFFF) ? C_FLAG : 0)
            | ((((xRegister & 0x0FFF) + src) > 0x0FFF) ? H_FLAG : 0)));
/*
 * According to David Winchurch, this does not affect the H flag

    F= (byte) ((xRegister>0xFFFF)? C_FLAG:0);
 */
    return xRegister & 0xFFFF;
}

/**

  The and op code.

  @param src the source register

 **/

private void AND(byte src)
{
//    Result of and ends up in the accumulator register
    A &= src;
    F = (A == 0) ? (H_FLAG | Z_FLAG) : H_FLAG;
}

/**

   Checks if a specified bit is set in a value. 

   @param bit the tested bit
   @paran dest the tested value

 **/

private void BIT(int bit, byte dest)
{
    if ((dest & (0x01 << bit)) != 0)
    {
        F &= (~Z_FLAG);
    }
    else
    {
        F |= Z_FLAG;
    }
/*
 * The internet is not unanimous on the following ...
 */
    F &= (~N_FLAG);
    F |= H_FLAG;
}

/**

  Checks if a specified bit is set in a value.

  @param num the tested value
  @param bit the tested bit

 **/

public static int BIT(byte num, int bit)
{
    return ((num & (0x01 << bit)) != 0 ? 1 : 0);
}

/**

 The Call op code.

 **/

private void CALL()
{
    PUSH(PC + 2);
    PC = readMemNN();
}

/**

 The CCF op code.

 **/

private void CCF()
{
    F &= ~(N_FLAG | H_FLAG);
    F ^= C_FLAG;
}

/**

  The Compare op code.

  @param src the source register

 **/

private void CP(byte src)
{
    F = (byte) ((A == src ? Z_FLAG : 0) | N_FLAG
            | ((A & 0x0F) < (src & 0x0F) ? H_FLAG : 0)
            | ((((int) A) & 0xFF) < (((int) src) & 0xFF) ? C_FLAG : 0));
    return;
}

/**

 The CPL op code.

 **/

private void CPL()
{
    A ^= 0xFF;
    F |= N_FLAG | H_FLAG;
    return;
}

/**

 The DAA (Decimal Add Adjust) op code.

 **/

private void DAA()
{
    xRegister = ((int) A) & 0xFF;
    if ((F & C_FLAG) != 0)
        xRegister |= 256;
    if ((F & H_FLAG) != 0)
        xRegister |= 512;
    if ((F & N_FLAG) != 0)
        xRegister |= 1024;
    A = (byte) ((DAA_TABLE[xRegister] >> 8) & 0xFF);
    if ((DAA_TABLE[xRegister] & 0x40) != 0)
        F |= Z_FLAG;
    else
        F &= (~Z_FLAG);
    F &= (~H_FLAG);
    if ((DAA_TABLE[xRegister] & 0x01) != 0)
        F |= C_FLAG;
    else
        F &= (~C_FLAG);
    return;
}

/**

 The decrement op code.

 @param dest the target register
 @return the decremented value

 **/

private byte DEC(byte dest)
{
    dest--;
    if (dest == 0x00)
        F |= Z_FLAG;
    else
        F &= (~Z_FLAG);
    F |= N_FLAG;
    if ((dest & 0xF) == 0xF)
        F |= H_FLAG;
    else
        F &= (~H_FLAG);
    return dest;
}

/**

  The Increment op code.

  @param dest the target register
  @return the incremented value

 **/

private byte INC(byte dest)
{
    dest++;
    if (dest == 0x00)
        F |= Z_FLAG;
    else
        F &= (~Z_FLAG);
    F &= (~N_FLAG);
    if ((dest & 0x0F) != 0)
        F &= (~H_FLAG);
    else
        F |= H_FLAG;
    return dest;
}

/**

  The jump op code.

 **/

private void JP()
{
    PC = readMemNN();
}

/**

  The or op code.

  @param src the source register

 **/

private void OR(byte src)
{
    A |= src;
    F = (A == 0) ? Z_FLAG : 0;
}

/**

 The POP op code.

 **/

private int POP()
{
    xAddr = doSrcMap(SP);
    SP += 2;
    return ((((int) srcArray[xAddr + 1]) & 0xFF) << 8)
            + (((int) srcArray[xAddr]) & 0xFF);
}

/**

 The PUSH op code.

 **/

private void PUSH(byte h, byte l)
{
    writeMem(--SP, h);
    writeMem(--SP, l);
    return;
}

private void PUSH(int nn)
{
    writeMem(--SP, (byte) ((nn >> 8) & 0xFF));
    writeMem(--SP, (byte) (nn & 0xFF));
    return;
}

/**

 The res op code clears a specific bit in a register

 @param bit the bit to be set
 @param dest value to have the bit set
 @return the doctored register value

 **/

private byte RES(int bit, byte dest)
{
    return (byte) (dest & (~(0x01 << bit)));
}

/**

 The Return op code.

 **/

private void RET()
{
    PC = POP();
}

/**

 The rotate left op code.

 **/

private void RLA()
{
    xRegister = ((((int) A) & 0xFF) << 1)
            | ((F & C_FLAG) != 0 ? 1 : 0);
    F = (byte) ((xRegister > 0xFF ? C_FLAG : 0));
    A = (byte) (xRegister & 0xFF);
    return;
}

/**

 The rotate left with carry op code.

 **/

private void RLCA()
{
    xRegister = ((((int) A) & 0xFF) << 1) | BIT(A, 7);
    F = (byte) ((xRegister > 0xFF ? C_FLAG : 0));
    A = (byte) (xRegister & 0xFF);
    return;
}

/**

 The rotate right op code.

 **/

private void RRA()
{
    tempChar = (byte) (((A & 0xFF) >> 1) | (((F & C_FLAG) != 0 ? 0x80 : 0)));
    F = (byte) (((A & 0x01) != 0 ? C_FLAG : 0));
    A = tempChar;
    return;
}

/**

 The rotate right with carry op code.

 **/

private void RRCA()
{
    tempChar = (byte) (((A & 0xFF) >> 1) | ((BIT(A, 0) != 0) ? 0x80 : 0));
    F = (byte) (((A & 0x01) != 0 ? C_FLAG : 0));
    A = tempChar;
    return;
}

/**

 The rotate left op code.

 @param dest the destination register
 @param the rotated register

 **/

private byte RL(byte dest)
{
    xRegister = ((((int) dest) & 0xFF) << 1)
            | ((F & C_FLAG) != 0 ? 1 : 0);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0)
            | (xRegister > 0xFF ? C_FLAG : 0));
    return (byte) (xRegister & 0xFF);
}

/**

 The rotate left with carry op code.

 @param dest the destination register
 @param the rotated register

 **/

private byte RLC(byte dest)
{
    xRegister = ((((int) dest) & 0xFF) << 1) | BIT(dest, 7);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0)
            | (xRegister > 0xFF ? C_FLAG : 0));
    return (byte) (xRegister & 0xFF);
}

/**

 The rotate right op code.

 @param dest the destination register
 @param the rotated register

 **/

private byte RR(byte dest)
{
    tempChar = (byte) (((dest & 0xFF) >> 1)
            | (((F & C_FLAG) != 0 ? 0x80 : 0)));
    F = (byte) ((tempChar == 0 ? Z_FLAG : 0)
            | ((dest & 0x01) != 0 ? C_FLAG : 0));
    return tempChar;
}

/**

 The rotate right with carry op code.

 @param dest the destination register
 @param the rotated register

 **/

private byte RRC(byte dest)
{
    tempChar = (byte) (((dest & 0xFF) >> 1)
            | ((BIT(dest, 0) != 0) ? 0x80 : 0));
    F = (byte) ((tempChar == 0 ? Z_FLAG : 0)
            | ((dest & 0x01) != 0 ? C_FLAG : 0));
    return tempChar;
}

/**

 The reset op code.

 @param b the position to jump to

 **/

private void RST(int b)
{
    PUSH(PC);
    PC = b;
    return;
}

/**

 The subtract with carry op code.

 @param src the source register

 **/

private void SBC(byte src)
{
    xRegister = (((int) A) & 0xff) - (((int) src) & 0xff)
            - ((F & C_FLAG) != 0 ? 1 : 0);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0) | N_FLAG
            | ((A & 0x0F)
            < (((src & 0xff) + ((F & C_FLAG) != 0 ? 1 : 0)) & 0x0F)
                    ? H_FLAG
                    : 0)
                    | (xRegister < 0 ? C_FLAG : 0));
    A = (byte) (xRegister & 0xFF);
}

/**

 The subtract op code.

 @param src the source register

 **/

private void SUB(byte src)
{
    xRegister = (((int) A) & 0xff) - (((int) src) & 0xff);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0) | N_FLAG
            | ((A & 0x0F) < (src & 0x0F) ? H_FLAG : 0)
            | (xRegister < 0 ? C_FLAG : 0));
    A = (byte) (xRegister & 0xFF);
}

/**

 The SCF op code.

 **/

private void SCF()
{
    F |= C_FLAG;
    F &= ~(N_FLAG | H_FLAG);
    return;
}

/**

 The set op code sets a specific bit in a register

 @param bit the bit to be set
 @param dest value to have the bit set
 @return the doctored register

 **/

private byte SET(int bit, byte dest)
{
    return (byte) (dest | (0x01 << bit));
}

/**

 The SLA op code.

 @param dest the destination register
 @return the shifted register

 **/

private byte SLA(byte dest)
{
    xRegister = ((((int) dest) & 0xFF) << 1);
    F = (byte) (((xRegister & 0xFF) == 0 ? Z_FLAG : 0)
            | (xRegister > 0xFF ? C_FLAG : 0));
    return (byte) (xRegister & 0xFF);
}

/**

 The SRA op code.

 @param dest the destination register
 @return the shifted register

 **/

private byte SRA(byte dest)
{
    xRegister = (dest >> 1);
    F = (byte) ((xRegister == 0 ? Z_FLAG : 0)
            | ((dest & 0x01) != 0 ? C_FLAG : 0));
    return (byte) (xRegister & 0xFF);
}

/**

The SRL op code.

 @param dest the destination register
 @return the shifted register

 **/

private byte SRL(byte dest)
{
    xRegister = ((dest & 0xFF) >> 1);
    F = (byte) ((xRegister == 0 ? Z_FLAG : 0)
            | ((dest & 0x01) != 0 ? C_FLAG : 0));
    return (byte) (xRegister & 0xFF);
}

/**

 The SWAP op code swaps the upper and lower 4 bits around.

 @param dest the destination register
 @return the nibble-swapped register

 **/

private byte SWAP(byte dest)
{
    F = (byte) (dest == 0 ? Z_FLAG : 0);
    return SWAP_TABLE[(((int) dest) & 0xFF)];
}

private final void doHL()
{
    HL = (((((int) H) & 0xFF)) << 8) + (((int) L) & 0xFF);
}

/**

 The XOR op code.

 @param r the source register
 @return the result, XOR'ed with A

 **/

private void XOR(byte r)
{
    A ^= r;
    F = (byte) (A == 0 ? Z_FLAG : 0);
    return;
}
private int periodic;
private boolean spritesOnly;
private boolean half_rate = true;
/**

 The main method of the CPU. Calculates the mode logic, handles interrupts
 and works out which op code to run for loops of the CPU

 **/
public void opCodeList(int loops)
{
    while (loops > 0)
    {
/*
 * If the cpu has been stopped check to see if the cpu speed must be changed
 */
        if (STOP)
        {
            if ((HIGH_MEM[CPU_SPEED_REG] & 0x01) != 0)
            {
                HIGH_MEM[CPU_SPEED_REG] |= 0x80;
//                CPU_SPEED^=0x01;
                STOP = false;
            }
            return;
        }
/*
 * Interrupt code
 */
        if (IME)
        {
            tempChar = (byte)(HIGH_MEM[INT_FLAG] & HIGH_MEM[INT_ENABLE] & 0x1F);
            if (tempChar != 0)
            {
                HALT = false;
                PUSH(PC);
                IME = false;
                if ((tempChar&1) != 0)
                {   //    V-Blank
                    PC = 0x40;
                    HIGH_MEM[INT_FLAG] &= (~0x01);
                }
                else
                if ((tempChar&2) != 0)
                {  //    LCDC
                    PC = 0x48;
                    HIGH_MEM[INT_FLAG] &= (~0x02);
                }
                else
                if ((tempChar&4) != 0)
                {  //    Timer overflow
                    PC = 0x50;
                    HIGH_MEM[INT_FLAG] &= (~0x04);
                }
                else
                if ((tempChar&8) != 0)
                { //    Serial
                    PC = 0x58;
                    HIGH_MEM[INT_FLAG] &= (~0x08);
                }
                else /* ((tempChar&16) != 0) */
                { //    Hi->lo
                    PC = 0x60;
                    HIGH_MEM[INT_FLAG] &= (~0x10);
                }
            }
        }
        if (PC < 0x4000)
        {
            srcArray = LOW_MEM;
            xAddr = PC;
        }
        else
        if (PC >= 0xFE00)
        {
            xAddr = PC - 0xFE00;
            srcArray = HIGH_MEM;
        }
        else
        if ((PC >= 0x8000) && (PC < 0xA000))
        {
            xAddr = (PC - 0x8000)
                    + (((HIGH_MEM[VRAM_BANK] & 0x01) == 0) ? 0 : 0x2000);
            srcArray = VRAM;
        }
        else
        if ((PC >= 0xD000) && (PC < 0xE000))
        {
            xAddr = (PC - 0xD000)
                    + (((int) (HIGH_MEM[RAM_BANK] & 0x07)) << 12);
            srcArray = PAGED_RAM;
        }
        else
        if (PC >= 0xC000)
        {
            xAddr = PC - 0xC000;
            if (xAddr > 0x2000)
                xAddr -= 0x2000;
            srcArray = CF_MEM;
        }
        else
        if (PC < 0x8000)
        {
            if (MBC <= 0 || CART_ROM == null)
            {
                srcArray = LOW_MEM;
                xAddr = PC;
            }
            else
            {
                xAddr = PC - 0x4000 + (ROM_PAGE << 14);
                srcArray = CART_ROM;
            }
        }
        else
        {
            xAddr = PC - 0xA000;
            if (MBC > 0 && RAM_ENABLE)
            {
                xAddr += (RAM_PAGE << 13);
                srcArray = CART_RAM;
            }
            else
                srcArray = AC_MEM;
        }
        opCode = (((int) srcArray[xAddr]) & 0xFF);
        PC++;
/*#ExecutionTrace#*///<editor-fold>
//--        System.out.println( Integer.toHexString(PC) + " - " +
//--                  Integer.toHexString(opCode) + " " +
//--             decodeOpCode(opCode) + ((opCode == 0xCB) ?
//--  (" " + decodecbOpCode(srcArray[xAddr + 1])) : 
//--  ( " " + Integer.toHexString(((int)srcArray[xAddr + 1]) & 0xFF) + " " +
//--  Integer.toHexString(((int) srcArray[xAddr + 2]) & 0xFF))));
/*$ExecutionTrace$*///</editor-fold>
/**************************************************************************
 * The general problem is that Mobile Phones are SLOOOOOOOOOW.
 *
 * The strategy is:
 * -  Shorten timings based on the number of instructions
 * -  Increase the number of instructions executed as the video kicks on, so
 *    that we make maximum progress when we are dependent on something like the
 *    Horizontal blank
 * -  Minimise the number of instructions executed when we are waiting for the
 *    next scan line.
 * -  This is the per-ROM dependency. An execution profile is used to identify
 *    the loop that is checking for the next Video interrupt; we kick on as
 *    fast as possible (depends on the number of instructions in the check loop) *    when this is the case.
 */

        periodic++;
//    if (((periodic > 3)
        if ((periodic > 63)
          || (PC >= lowDelayAddr && PC <= highDelayAddr))
        {
            CPU_CYCLES += 37;
            DIV += 37;
            if (DIV > (128))
            {
                DIV = 0;
                HIGH_MEM[DIV_CNTR]++;
            }
            if (TIMER_ON)
            {
                TIMER += 37;
                if (TIMER > (TIMER_MAX))
                {
                    TIMER = 0;
                    HIGH_MEM[TIMER_COUNT]++;
                    if (HIGH_MEM[TIMER_COUNT] == 0)
                    {
                        HIGH_MEM[INT_FLAG] |= 0x04;
                    }
                }
            }
/*
 * Juggle with the time given to graphics versus time just executing
 * instructions. 
 *
 * Originally the magic numbers were 256, 81 and 174.  Change 81 to 131?
 * Try 128, 39 and 81
 */
            if (CPU_CYCLES > (128))
            { //    Checks to see if it is time to draw a line
/*
                try
                {
                    if (SOUND != null)
                        SOUND.outputSound(16);
                }
                catch (Throwable e)
                {
                     e.printStackTrace();
                     System.err.println("Problem with Sound");
                     SOUND = null;
                }
 */
                CPU_CYCLES = 0x00;
                if (HIGH_MEM[LCD_Y_LOC] == (byte) 0x99)
                {
                    HIGH_MEM[LCD_Y_LOC] = 0x00;
                    if (toRefresh == -1)
                        toRefresh = 144;
                }
                else
                    HIGH_MEM[LCD_Y_LOC]++;
                //    Checks the line compare to the current line
                if (HIGH_MEM[LCD_Y_LOC] == HIGH_MEM[LCD_Y_COMP])
                {
                    HIGH_MEM[LCD_STAT] |= 0x04;
                    if ((HIGH_MEM[LCD_STAT] & 0x40) != 0)
                        HIGH_MEM[INT_FLAG] |= 0x02;
                }
                else
                    HIGH_MEM[LCD_STAT] &= 0xFB;
            }
            if ((((int) HIGH_MEM[LCD_Y_LOC]) & 0xFF) < 0x90)
            { //    Mode selection code
                //    Mode 10 logic
                if (CPU_CYCLES < (35/*    <<CPU_SPEED */)
                        && (HIGH_MEM[LCD_STAT] & 0x03) != 0x02)
                {
                    HIGH_MEM[LCD_STAT] = (byte) ((((int) HIGH_MEM[LCD_STAT])
                            & 0xFC)
                                    | 0x02);
                    if ((HIGH_MEM[LCD_STAT] & 0x20) != 0)
                        HIGH_MEM[INT_FLAG] |= 0x02;
                }
                //    Mode 11 logic
                else
                if (CPU_CYCLES >= (35) && CPU_CYCLES < (81)
                        && (HIGH_MEM[LCD_STAT] & 0x03) != 0x03)
                {
                    //    Calculates the current line of graphics
                    lazy++;
                    if (toRefresh > 0)
                    {
                        if (half_rate)
                        {
//                            SPR_PER_LINE[HIGH_MEM[LCD_Y_LOC] & 0xFF] =
//                 VIDEO.drawLine(HIGH_MEM[LCD_Y_LOC] & 0xFF, (spritesOnly ?
//              ((SPR_PER_LINE[HIGH_MEM[LCD_Y_LOC] & 0xFF] == 0) ? false : true)
//                             : false ));
                            half_rate = false;
                            VIDEO.drawLine(HIGH_MEM[LCD_Y_LOC] & 0xFF,
                                    spritesOnly);
                            toRefresh--;
                        }
                        else
                            half_rate = true;
                    }
                    else
                    {
                        if (lazy > 700)
                        {
                            lazy = 0;
                            toRefresh = -1;
                        }
                    }
                    CPU_CYCLES = 81;
                    HIGH_MEM[LCD_STAT] = (byte) ((HIGH_MEM[LCD_STAT] & 0xFC)
                            | 0x03);
                }
                //    Mode 00 logic
                else
                if (CPU_CYCLES >= 81 && (HIGH_MEM[LCD_STAT] & 0x03) != 0)
                {
                    HIGH_MEM[LCD_STAT] &= 0xFC;
                    if ((HIGH_MEM[LCD_STAT] & 0x08) != 0)
                    {
                        HIGH_MEM[INT_FLAG] |= 0x02;
                    }
                }
            }
            //    Mode 01 logic
            else
            if ((((int) (HIGH_MEM[LCD_Y_LOC])) & 0xFF) >= 0x90
                    && (HIGH_MEM[LCD_STAT] & 0x03) != 0x01)
            {
                //    Toggles the drawn line, to give the interleave
                half_rate = (half_rate == true) ? false : true;
                if (lazy <= 320 && (toRefresh == 0 || toRefresh > 70))
                {
                    VIDEO.repaint();
                    lastTime = System.currentTimeMillis();
                    drawTime = (int) (lastTime - thisTime);
//    System.out.println("T: " + thisTime + " " + lastTime + " "
//    + drawTime);
                    if (drawTime < 33)
                    {
                        try
                        {
                            Thread.sleep(33 - drawTime);
                        }
                        catch (Exception e)
                        {}
                        lastTime = System.currentTimeMillis();
                        drawTime = 33;
                        if (toRefresh == 0)
                        {
                            toRefresh = -1;
                            lazy = 0;
                        }
                        spritesOnly = false;
                    }
                    else
                    if (toRefresh == 0)
                    {
                        lazy = 500;
                        toRefresh = 0;
                        if (drawTime > 200 && spritesOnly == false)
                        {
                            spritesOnly = true;
                            drawTime = 200;
                        }
                        else
                            spritesOnly = false;
                    }
                    else
                    if (drawTime > 200)
                        drawTime = 200;
//    System.out.println(drawTime);
                    thisTime = lastTime;
                }
                HIGH_MEM[LCD_STAT] = (byte) ((HIGH_MEM[LCD_STAT] & 0xFC)
                        | 0x01);
                if ((HIGH_MEM[LCD_STAT] & 0x10) != 0)
                    HIGH_MEM[INT_FLAG] |= 0x02;
                HIGH_MEM[INT_FLAG] |= 0x01;
            }
            periodic = 0;
        }
        try
        {
/*
 * Execute the machine instruction. Illegal op codes 0xE3, 0xE4, 0xEB and 0xED
 * could be used to provide relative jumps that are conditional on whether we
 * are refreshing the screen or not. If we are, the scan lines and the machine
 * instructions must be synchronised. If we are not, we hypothesise that we do
 * not need synchronisation. Note that any illegal op codes would hang the real
 * Gameboy. In contrast, we treat them as NO-OP's.
 */
            switch (opCode)
            {
            case 0x40: //    LD B,B
            case 0x49: //    LD C,C
            case 0x52: //    LD D,D
            case 0x5B: //    LD E,E
            case 0x64: //    LD H,H
            case 0x6D: //    LD L,L
            case 0x7F: //    LD A,A
            case 0x00:
                break; //    NOP

            case 0xD3:
            case 0xDB:
            case 0xDD:
            case 0xE3:
            case 0xE4:
            case 0xEB:
            case 0xEC:
            case 0xED:
            case 0xF4:
            case 0xFC:
            case 0xFD:
                HIGH_MEM[CPU_SPEED_REG] = 0x00;
                STOP = true;
                System.out.println(
                        "Not a GameBoy opCode; this was a Z80 instruction: "
                                + Integer.toHexString(opCode) + " at "
                                + Integer.toHexString(PC - 1));
                break; //    NOP

            case 0x01: //    LD BC,$aabb
                C = srcArray[xAddr + 1];
                B = srcArray[xAddr + 2];
                PC += 2;
                break;

            case 0x02: //    LD (BC),A
                writeMem(((((int) B) & 0xFF) << 8) + (((int) C) & 0xFF), A);
                break;

            case 0x03: //    INC BC
                C++;
                if (C == 0)
                    B++;
                break;

            case 0x04: //    INC B
                B = INC(B);
                break;

            case 0x05: //    DEC B
                B = DEC(B);
                break;

            case 0x06: //    LD B,$xx
                B = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x07: //    RLCA
                RLCA();
                break;

            case 0x08: //    LD ($aabb),SP
            {
                int targLoc = (((int) srcArray[xAddr + 1]) & 0xFF)
                        + (((int) (srcArray[xAddr + 2]) & 0xFF) << 8);
                PC += 2;
                writeMem(targLoc, (byte) (SP & 0xFF));
                writeMem(targLoc + 1, (byte) ((SP >> 8) & 0xFF));
                break;
            }

            case 0x09: //    ADD HL,BC
                ADD_HL(B, C);
                break;

            case 0x0A: //    LD A,(BC)
                A = readMem(((((int) B) & 0xFF) << 8) + (((int) C) & 0xFF));
                break;

            case 0x0B: //    DEC BC
                C--;
                if (C == -1)
                    B--;
                break;

            case 0x0C: //    INC C
                C = INC(C);
                break;

            case 0x0D:  //    DEC C
                C = DEC(C);
                break;

            case 0x0E: //    LD C,$xx
                C = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x0F: //    RRCA
                RRCA();
                break;

            case 0x10: //    STOP
                STOP = true;
                break;

            case 0x11: //    LD DE,$aabb
                E = srcArray[xAddr + 1];
                D = srcArray[xAddr + 2];
                PC += 2;
                break;

            case 0x12: //    LD (DE),A
                writeMem(((((int) D) & 0xFF) << 8) + (((int) E) & 0xFF), A);
                break;

            case 0x13: //    INC DE
                E++;
                if (E == 0)
                    D++;
                break;

            case 0x14: //    INC D
                D = INC(D);
                break;

            case 0x15: //    DEC D
                D = DEC(D);
                break;

            case 0x16: //    LD D,$xx
                D = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x17: //    RLA
                RLA();
                break;

            case 0x18: //    JR $xx
                PC = srcArray[xAddr + 1] + PC + 1;
                break;

            case 0x19: //    ADD HL,DE
                ADD_HL(D, E);
                break;

            case 0x1A: //    LD A,(DE)
                A = readMem(((((int) D) & 0xFF) << 8) + (((int) E) & 0xFF));
                break;

            case 0x1B: //    DEC DE
                E--;
                if (E == -1)
                    D--;
                break;

            case 0x1C: //    INC E
                E = INC(E);
                break;

            case 0x1D: //    DEC E
                E = DEC(E);
                break;

            case 0x1E: //    LD E,$xx
                E = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x1F: //    RRA
                RRA();
                break;

            case 0x20: //    JR NZ,$xx
                if ((F & Z_FLAG) == 0x00)
                {
                    periodic++;
                    PC = srcArray[xAddr + 1] + PC + 1;
                }
                else
                {
                    PC++;
                }
                break;
  
            case 0x21: //    LD HL,$aabb
                L = srcArray[xAddr + 1];
                H = srcArray[xAddr + 2];
                PC += 2;
                break;

            case 0x22: //    LD (HLI),A
                doHL();
                writeMem(HL, A);

            case 0x23: //    INC HL
                L++;
                if (L == 0)
                    H++;
                break;

            case 0x24: //    INC H
                H = INC(H);
                break;

            case 0x25: //    DEC H
                H = DEC(H);
                break;

            case 0x26: //    LD H,$xx
                H = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x27: //    DAA
                DAA();
                break;

            case 0x28: //    JR Z,$xx
                if ((F & Z_FLAG) != 0)
                {
                    periodic++;
                    PC = srcArray[xAddr + 1] + 1 + PC;
                }
                else
                    PC++;
                break;

            case 0x29: //    ADD HL,HL
                ADD_HL(H, L);
                break;

            case 0x2A: //    LD A,(HLI)
                doHL();
                A = readMem(HL);
                L++;
                if (L == 0)
                    H++;
                break;

            case 0x32: //    LD (HLD),A
                doHL();
                writeMem(HL, A);

            case 0x2B: //    DEC HL
                L--;
                if (L == -1)
                    H--;
                break;

            case 0x2C: //    INC L
                L = INC(L);
                break;

            case 0x2D: //    DEC L
                L = DEC(L);
                break;

            case 0x2E: //    LD L,$xx
                L = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x2F: //    CPL
                CPL();
                break;

            case 0x30: //    JR NC,$xx
                if ((F & C_FLAG) == 0x00)
                {
                    periodic++;
                    PC = srcArray[xAddr + 1] + PC + 1;
                }
                else
                    PC++;
                break;

            case 0x31: //    LD SP,$aabb
                SP = (((int) srcArray[xAddr + 1]) & 0xFF)
                        + ((((int) srcArray[xAddr + 2]) & 0xFF) << 8);
                PC += 2;
                break;

            case 0x33: //    INC SP
                SP = (SP == 0xFFFF) ? 0 : SP++;
                break;

            case 0x34: //    INC (HL)
                doHL();
                writeMem(HL, INC(readMem(HL)));
                break;

            case 0x35: //    DEC (HL)
                doHL();
                writeMem(HL, DEC(readMem(HL)));
                break;

            case 0x36: //    LD (HL),$xx
                doHL();
                writeMem(HL, srcArray[xAddr + 1]);
                PC++;
                break;

            case 0x37: //    SCF
                SCF();
                break;

            case 0x38: //    JR C,$xx
                if ((F & C_FLAG) != 0)
                {
                    periodic++;
                    PC = srcArray[xAddr + 1] + PC + 1;
                }
                else
                    PC++;
                break;

            case 0x39: //    ADD HL,SP
                ADD_HL(SP);
                break;

            case 0x3A: //    LD A,(HLD),
                doHL();
                A = readMem(HL);
                L--;
                if (L == -1)
                    H--;
                break;

            case 0x3B: //    DEC SP
                SP = (SP == 0) ? 0xFFFF : SP--;
                break;

            case 0x3C: //    INC A
                A = INC(A);
                break;

            case 0x3D: //    DEC A
                A = DEC(A);
                break;

            case 0x3E: //    LD A,$xx
                A = srcArray[xAddr + 1];
                PC++;
                break;

            case 0x3F: //    CCF
                CCF();
                break;

            case 0x41: //    LD B,C
                B = C;
                break;

            case 0x42: //    LD B,D
                B = D;
                break;

            case 0x43: //    LD B,E
                B = E;
                break;

            case 0x44: //    LD B,H
                B = H;
                break;

            case 0x45: //    LD B,L
                B = L;
                break;

            case 0x46: //    LD B,(HL)
                doHL();
                B = readMem(HL);
                break;

            case 0x47: //    LD B,A
                B = A;
                break;

            case 0x48: //    LD C,B
                C = B;
                break;

            case 0x4A: //    LD C,D
                C = D;
                break;

            case 0x4B: //    LD C,E
                C = E;
                break;

            case 0x4C: //    LD C,H
                C = H;
                break;

            case 0x4D: //    LD C,L
                C = L;
                break;

            case 0x4E: //    LD C,(HL)
                doHL();
                C = readMem(HL);
                break;

            case 0x4F: //    LD C,A
                C = A;
                break;

            case 0x50: //    LD D,B
                D = B;
                break;

            case 0x51: //    LD D,C
                D = C;
                break;

            case 0x53: //    LD D,E
                D = E;
                break;

            case 0x54: //    LD D,H
                D = H;
                break;

            case 0x55: //    LD D,L
                D = L;
                break;

            case 0x56: //    LD D,(HL)
                doHL();
                D = readMem(HL);
                break;

            case 0x57: //    LD D,A
                D = A;
                break;

            case 0x58: //    LD E,B
                E = B;
                break;

            case 0x59: //    LD E,C
                E = C;
                break;

            case 0x5A: //    LD E,D
                E = D;
                break;

            case 0x5C: //    LD E,H
                E = H;
                break;

            case 0x5D: //    LD E,L
                E = L;
                break;

            case 0x5E: //    LD E,(HL)
                doHL();
                E = readMem(HL);
                break;

            case 0x5F: //    LD E,A
                E = A;
                break;

            case 0x60: //    LD H,B
                H = B;
                break;

            case 0x61: //    LD H,C
                H = C;
                break;

            case 0x62: //    LD H,D
                H = D;
                break;

            case 0x63: //    LD H,E
                H = E;
                break;

            case 0x65: //    LD H,L
                H = L;
                break;

            case 0x66: //    LD H,(HL)
                doHL();
                H = readMem(HL);
                break;

            case 0x67: //    LD H,A
                H = A;
                break;

            case 0x68: //    LD L,B
                L = B;
                break;

            case 0x69: //    LD L,C
                L = C;
                break;

            case 0x6A: //    LD L,D
                L = D;
                break;

            case 0x6B: //    LD L,E
                L = E;
                break;

            case 0x6C: //    LD L,H
                L = H;
                break;

            case 0x6E: //    LD L,(HL)
                doHL();
                L = readMem(HL);
                break;

            case 0x6F: //    LD L,A
                L = A;
                break;

            case 0x70: //    LD (HL),B
                doHL();
                writeMem(HL, B);
                break;

            case 0x71: //    LD (HL),C
                doHL();
                writeMem(HL, C);
                break;

            case 0x72: //    LD (HL),D
                doHL();
                writeMem(HL, D);
                break;

            case 0x73: //    LD (HL),E
                doHL();
                writeMem(HL, E);
                break;

            case 0x74: //    LD (HL),H
                doHL();
                writeMem(HL, H);
                break;

            case 0x75: //    LD (HL),L
                doHL();
                writeMem(HL, L);
                break;

            case 0x76: //    HALT
//    try
//    {
//        Thread.sleep(10);
//    }
//    catch (Exception e)
//    {
//    }
                if (IME)
                    HALT = true;
                else
                    HALT_NEXT = true;
                break;

            case 0x77: //    LD (HL),A
                doHL();
                writeMem(HL, A);
                break;

            case 0x78: //    LD A,B
                A = B;
                break;

            case 0x79: //    LD A,C
                A = C;
                break;

            case 0x7A: //    LD A,D
                A = D;
                break;

            case 0x7B: //    LD A,E
                A = E;
                break;

            case 0x7C: //    LD A,H
                A = H;
                break;

            case 0x7D: //    LD A,L
                A = L;
                break;

            case 0x7E: //    LD A,(HL)
                doHL();
                A = readMem(HL);
                break;

            case 0x80: //    ADD A,B
                ADD(B);
                break;

            case 0x81: //    ADD A,C
                ADD(C);
                break;

            case 0x82: //    ADD A,D
                ADD(D);
                break;

            case 0x83: //    ADD A,E
                ADD(E);
                break;

            case 0x84: //    ADD A,H
                ADD(H);
                break;

            case 0x85: //    ADD A,L
                ADD(L);
                break;

            case 0x86: //    ADD A,(HL)
                doHL();
                ADD(readMem(HL));
                break;

            case 0x87: //    ADD A,A
                ADD(A);
                break;

            case 0x88: //    ADDC A,B
                ADC(B);
                break;

            case 0x89: //    ADDC A,C
                ADC(C);
                break;

            case 0x8A: //    ADDC A,D
                ADC(D);
                break;

            case 0x8B: //    ADDC A,E
                ADC(E);
                break;

            case 0x8C: //    ADDC A,H
                ADC(H);
                break;

            case 0x8D: //    ADDC A,L
                ADC(L);
                break;

            case 0x8E: //    ADDC A,(HL)
                doHL();
                ADC(readMem(HL));
                break;

            case 0x8F: //    ADDC A,A
                ADC(A);
                break;

            case 0x90: //    SUB B
                SUB(B);
                break;

            case 0x91: //    SUB C
                SUB(C);
                break;

            case 0x92: //    SUB D
                SUB(D);
                break;

            case 0x93: //    SUB E
                SUB(E);
                break;

            case 0x94: //    SUB H
                SUB(H);
                break;

            case 0x95: //    SUB L
                SUB(L);
                break;

            case 0x96: //    SUB (HL)
                doHL();
                SUB(readMem(HL));
                break;

            case 0x97: //    SUB A
                SUB(A);
                break;

            case 0x98: //    SUBC B
                SBC(B);
                break;

            case 0x99: //    SUBC C
                SBC(C);
                break;

            case 0x9A: //    SUBC D
                SBC(D);
                break;

            case 0x9B: //    SUBC E
                SBC(E);
                break;

            case 0x9C: //    SUBC H
                SBC(H);
                break;

            case 0x9D: //    SUBC L
                SBC(L);
                break;

            case 0x9E: //    SUBC (HL)
                doHL();
                SBC(readMem(HL));
                break;

            case 0x9F: //    SUBC A
                SBC(A);
                break;

            case 0xA0: //    AND B
                AND(B);
                break;

            case 0xA1: //    AND C
                AND(C);
                break;

            case 0xA2: //    AND D
                AND(D);
                break;

            case 0xA3: //    AND E
                AND(E);
                break;

            case 0xA4: //    AND H
                AND(H);
                break;

            case 0xA5: //    AND L
                AND(L);
                break;

            case 0xA6: //    AND (HL)
                doHL();
                AND(readMem(HL));
                break;

            case 0xA7: //    AND A
                F = (A == 0) ? (Z_FLAG | H_FLAG) : H_FLAG;
                break;

            case 0xA8: //    XOR B
                XOR(B);
                break;

            case 0xA9: //    XOR C
                XOR(C);
                break;

            case 0xAA: //    XOR D
                XOR(D);
                break;

            case 0xAB: //    XOR E
                XOR(E);
                break;

            case 0xAC: //    XOR H
                XOR(H);
                break;

            case 0xAD: //    XOR L
                XOR(L);
                break;

            case 0xAE: //    XOR (HL)
                doHL();
                XOR(readMem(HL));
                break;

            case 0xAF: //    XOR A
                A = 0;
                F = Z_FLAG;
                break;

            case 0xB0: //    OR B
                OR(B);
                break;

            case 0xB1: //    OR C
                OR(C);
                break;

            case 0xB2: //    OR D
                OR(D);
                break;

            case 0xB3: //    OR E
                OR(E);
                break;

            case 0xB4: //    OR H
                OR(H);
                break;

            case 0xB5: //    OR L
                OR(L);
                break;

            case 0xB6: //    OR (HL)
                doHL();
                OR(readMem(HL));
                break;

            case 0xB7: //    OR A
                OR(A);
                break;

            case 0xB8: //    CP B
                CP(B);
                break;

            case 0xB9: //    CP C
                CP(C);
                break;

            case 0xBA: //    CP D
                CP(D);
                break;

            case 0xBB: //    CP E
                CP(E);
                break;

            case 0xBC: //    CP H
                CP(H);
                break;

            case 0xBD: //    CP L
                CP(L);
                break;

            case 0xBE: //    CP (HL)
                doHL();
                CP(readMem(HL));
                break;

            case 0xBF: //    CP A
                CP(A);
                break;

            case 0xC0: //    RET NZ
                if ((F & Z_FLAG) == 0)
                {
                    periodic++;
                    RET();
                }
                break;

            case 0xC1: //    POP BC
                C = readMem(SP);
                B = srcArray[xAddr + 1];
                SP += 2;
                break;

            case 0xC2: //    JP NZ, $aabb
                if ((F & Z_FLAG) == 0)
                {
                    periodic++;
                    JP();
                }
                else
                    PC += 2;
                break;

            case 0xC3: //    JP $aabb
                JP();
                break;

            case 0xC4: //    CALL NZ,$aabb
                if ((F & Z_FLAG) == 0)
                {
                    periodic++;
                    CALL();
                }
                else
                    PC += 2;
                break;

            case 0xC5: //    PUSH BC
                PUSH(B, C);
                break;

            case 0xC6: //    ADD A,$aa
                ADD(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xC7: //    RST $00
                RST(0x00);
                break;

            case 0xC8: //    RET Z
                if ((F & Z_FLAG) != 0)
                {
                    periodic++;
                    RET();
                }
                break;

            case 0xC9: //    RET
                RET();
                break;

            case 0xCA: //    JP Z,$aabb
                if ((F & Z_FLAG) != 0)
                {
                    periodic++;
                    JP();
                }
                else
                    PC += 2;
                break;

            case 0xCB: //    CB Oppcodes
                singleCPUStepCB();
                break;

            case 0xCC: //    CALL Z,$aabb
                if ((F & Z_FLAG) != 0)
                {
                    periodic++;
                    CALL();
                }
                else
                    PC += 2;
                break;

            case 0xCD: //    CALL nn
                CALL();
                break;

            case 0xCE: //    ADC A,$aa
                ADC(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xCF: //    RST $08
                RST(0x08);
                break;

            case 0xD0: //    RET NC
                if ((F & C_FLAG) == 0)
                {
                    periodic++;
                    RET();
                }
                break;

            case 0xD1: //    POP DE
                E = readMem(SP);
                D = srcArray[xAddr + 1];
                SP += 2;
                break;

            case 0xD2: //    JP NC, $aabb
                if ((F & C_FLAG) == 0)
                {
                    periodic++;
                    JP();
                }
                else
                    PC += 2;
                break;

            case 0xD4: //    CALL NC, $aabb
                if ((F & C_FLAG) == 0)
                {
                    periodic++;
                    CALL();
                }
                else
                    PC += 2;
                break;

            case 0xD5: //    PUSH DE
                PUSH(D, E);
                break;

            case 0xD6: //    SUB $aa
                SUB(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xD7: //    RST $10
                RST(0x10);
                break;

            case 0xD8: //    RET C
                if ((F & C_FLAG) == C_FLAG)
                {
                    periodic++;
                    RET();
                }
                break;

            case 0xD9: //    RETI
                RET();
                opCodeList(1);
                IME = true;
                if (HALT_NEXT)
                {
                    HALT = true;
                    HALT_NEXT = false;
                }
                break;

            case 0xDA: //    JP C, $aabb
                if ((F & C_FLAG) != 0)
                {
                    periodic++;
                    JP();
                }
                else
                    PC += 2;
                break;

            case 0xDC: //    CALL C,$aabb
                if ((F & C_FLAG) != 0)
                {
                    periodic++;
                    CALL();
                }
                else
                    PC += 2;
                break;

            case 0xDE: //    SBC A, $aa
                SBC(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xDF: //    RST $18
                RST(0x18);
                break;

            case 0xE0: //    LD ($FFaa),A
                writeMem(0xFF00 + (((int) srcArray[xAddr + 1]) & 0xFF), A);
                PC++;
                break;

            case 0xE1: //    POP HL
                L = readMem(SP);
                H = srcArray[xAddr + 1];
                SP += 2;
                break;

            case 0xE2: //    LD ($FF00+C),A
                writeMem(0xFF00 + (((int) C) & 0xFF), A);
                break;

            case 0xE5: //    PUSH HL
                PUSH(H, L);
                break;

            case 0xE6: //    AND $aa
                AND(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xE7: //    RST 0x20
                RST(0x20);
                break;

            case 0xE8: //    ADD SP,xx
                SP = ADDSP((srcArray[xAddr + 1]));
                PC++;
                break;

            case 0xE9: //    JP (HL)
                doHL();
                PC = HL;
                break;

            case 0xEA: //    LD ($aabb),A
                writeMem(
                        (((int) srcArray[xAddr + 1]) & 0xFF)
                                + ((((int) srcArray[xAddr + 2]) & 0xFF)
                                        << 8),
                                        A);
                PC += 2;
                break;

            case 0xEE: //    XOR $aa
                XOR(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xEF: //    RST $28
                RST(0x28);
                break;

            case 0xF0: //    LD A,($FFaa)
                A = readMem(0xFF00 + (((int) srcArray[xAddr + 1]) & 0xFF));
                PC++;
                break;

            case 0xF1: //    POP AF
                F = readMem(SP);
                A = srcArray[xAddr + 1];
                SP += 2;
                break;

            case 0xF2: //    LD A,($FF00+C)
                A = readMem(0xFF00 + (((int) C) & 0xFF));
                break;

            case 0xF3: //    DI
                IME = false;
                break;

            case 0xF5: //    PUSH AF
                PUSH(A, F);
                break;

            case 0xF6: //    OR $aa
                OR(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xF7: //    RST 0x30
                RST(0x30);
                break;

            case 0xF8: //    LD HL,SP+$aa
                HL = ADDSP((srcArray[xAddr + 1]));
                PC++;
                H = (byte) ((HL >> 8) & 0xFF);
                L = (byte) (HL & 0xFF);
                break;

            case 0xF9: //    LD SP,HL
                doHL();
                SP = HL;
                break;

            case 0xFA: //    LD A ($aabb)
                A = readMem(
                        (((int) srcArray[xAddr + 1]) & 0xFF)
                                + ((((int) srcArray[xAddr + 2]) & 0xFF)
                                        << 8));
                PC += 2;
                break;

            case 0xFB: //    EI
                opCodeList(1);
                IME = true;
                if (HALT_NEXT)
                {
                    HALT = true;
                    HALT_NEXT = false;
                }
                break;

            case 0xFE: //    CP N
                CP(srcArray[xAddr + 1]);
                PC++;
                break;

            case 0xFF: //    RST $38
                RST(0x38);
                break;

            default:
            {
                System.out.println(
                        "Unknown op code " + Integer.toHexString(opCode));
                break;
            }
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            System.err.println(
                    "CPU failed at " + PC + " executing "
                    + Integer.toHexString(opCode & 0xff) + " "
                    + Integer.toHexString(srcArray[xAddr + 1] & 0xff)
                    + "\n" + " PC " + Integer.toHexString(PC & 0xFF)
                    + " HL " + Integer.toHexString(HL & 0xFF) + " SP "
                    + Integer.toHexString(SP & 0xFF));
            HIGH_MEM[CPU_SPEED_REG] = 0x00;
            STOP = true;
            return;
        }
        loops--;
    }
}

/**

  The CB or secondary opcodes

 **/

private void singleCPUStepCB()
{
    opCode = (int) (srcArray[xAddr + 1] & 0xFF);
    PC++;

    switch (opCode)
    {
    case 0x00: //    RLC B
        B = RLC(B);
        break;

    case 0x01: //    RLC C
        C = RLC(C);
        break;

    case 0x02: //    RLC D
        D = RLC(D);
        break;

    case 0x03: //    RLC E
        E = RLC(E);
        break;

    case 0x04: //    RLC H
        H = RLC(H);
        break;

    case 0x05: //    RLC L
        L = RLC(L);
        break;

    case 0x06: //    RLC (HL)
        doHL();
        writeMem(HL, RLC(readMem(HL)));
        break;

    case 0x07: //    RLC A
        A = RLC(A);
        break;

    case 0x08: //    RRC B
        B = RRC(B);
        break;

    case 0x09: //    RRC C
        C = RRC(C);
        break;

    case 0x0A: //    RRC D
        D = RRC(D);
        break;

    case 0x0B: //    RRC E
        E = RRC(E);
        break;

    case 0x0C: //    RRC H
        H = RRC(H);
        break;

    case 0x0D: //    RRC L
        L = RRC(L);
        break;

    case 0x0E: //    RRC (HL)
        doHL();
        writeMem(HL, RRC(readMem(HL)));
        break;

    case 0x0F: //    RRC A
        A = RRC(A);
        break;

    case 0x10: //    RL B
        B = RL(B);
        break;

    case 0x11: //    RL C
        C = RL(C);
        break;

    case 0x12: //    RL D
        D = RL(D);
        break;

    case 0x13: //    RL E
        E = RL(E);
        break;

    case 0x14: //    RL H
        H = RL(H);
        break;

    case 0x15: //    RL L
        L = RL(L);
        break;

    case 0x16: //    RL (HL)
        doHL();
        writeMem(HL, RL(readMem(HL)));
        break;

    case 0x17: //    RL A
        A = RL(A);
        break;

    case 0x18: //    RR B
        B = RR(B);
        break;

    case 0x19: //    RR C
        C = RR(C);
        break;

    case 0x1A: //    RR D
        D = RR(D);
        break;

    case 0x1B: //    RR E
        E = RR(E);
        break;

    case 0x1C: //    RR H
        H = RR(H);
        break;

    case 0x1D: //    RR L
        L = RR(L);
        break;

    case 0x1E: //    RR (HL)
        doHL();
        writeMem(HL, RR(readMem(HL)));
        break;

    case 0x1F: //    RR A
        A = RR(A);
        break;

    case 0x20: //    SLA B
        B = SLA(B);
        break;

    case 0x21: //    SLA C
        C = SLA(C);
        break;

    case 0x22: //    SLA D
        D = SLA(D);
        break;

    case 0x23: //    SLA E
        E = SLA(E);
        break;

    case 0x24: //    SLA H
        H = SLA(H);
        break;

    case 0x25: //    SLA L
        L = SLA(L);
        break;

    case 0x26: //    SLA (HL)
        doHL();
        writeMem(HL, SLA(readMem(HL)));
        break;

    case 0x27: //    SLA A
        A = SLA(A);
        break;

    case 0x28: //    SRA B
        B = SRA(B);
        break;

    case 0x29: //    SRA C
        C = SRA(C);
        break;

    case 0x2A: //    SRA D
        D = SRA(D);
        break;

    case 0x2B: //    SRA E
        E = SRA(E);
        break;

    case 0x2C: //    SRA H
        H = SRA(H);
        break;

    case 0x2D: //    SRA L
        L = SRA(L);
        break;

    case 0x2E: //    SRA (HL)
        doHL();
        writeMem(HL, SRA(readMem(HL)));
        break;

    case 0x2F: //    SRA A
        A = SRA(A);
        break;

    case 0x30: //    SWAP B
        B = SWAP(B);
        break;

    case 0x31: //    SWAP C
        C = SWAP(C);
        break;

    case 0x32: //    SWAP D
        D = SWAP(D);
        break;

    case 0x33: //    SWAP E
        E = SWAP(E);
        break;

    case 0x34: //    SWAP H
        H = SWAP(H);
        break;

    case 0x35: //    SWAP L
        L = SWAP(L);
        break;

    case 0x36: //    SWAP (HL)
        doHL();
        writeMem(HL, SWAP(readMem((HL))));
        break;

    case 0x37: //    SWAP A
        A = SWAP(A);
        break;

    case 0x38: //    SRL B
        B = SRL(B);
        break;

    case 0x39: //    SRL C
        C = SRL(C);
        break;

    case 0x3A: //    SRL D
        D = SRL(D);
        break;

    case 0x3B: //    SRL E
        E = SRL(E);
        break;

    case 0x3C: //    SRL H
        H = SRL(H);
        break;

    case 0x3D: //    SRL L
        L = SRL(L);
        break;

    case 0x3E: //    SRL (HL)
        doHL();
        writeMem(HL, SRL(readMem(HL)));
        break;

    case 0x3F: //    SRL A
        A = SRL(A);
        break;

    case 0x40: //    BIT 0,B
        BIT(0, B);
        break;

    case 0x41: //    BIT 0,C
        BIT(0, C);
        break;

    case 0x42: //    BIT 0,D
        BIT(0, D);
        break;

    case 0x43: //    BIT 0,E
        BIT(0, E);
        break;

    case 0x44: //    BIT 0,H
        BIT(0, H);
        break;

    case 0x45: //    BIT 0,L
        BIT(0, L);
        break;

    case 0x46: //    BIT 0,(HL)
        doHL();
        BIT(0, readMem(HL));
        break;

    case 0x47: //    BIT 0,A
        BIT(0, A);
        break;

    case 0x48: //    BIT 1,B
        BIT(1, B);
        break;

    case 0x49: //    BIT 1,C
        BIT(1, C);
        break;

    case 0x4A: //    BIT 1,D
        BIT(1, D);
        break;

    case 0x4B: //    BIT 1,E
        BIT(1, E);
        break;

    case 0x4C: //    BIT 1,H
        BIT(1, H);
        break;

    case 0x4D: //    BIT 1,L
        BIT(1, L);
        break;

    case 0x4E: //    BIT 1,(HL)
        doHL();
        BIT(1, readMem(HL));
        break;

    case 0x4F: //    BIT 1,A
        BIT(1, A);
        break;

    case 0x50: //    BIT 2,B
        BIT(2, B);
        break;

    case 0x51: //    BIT 2,C
        BIT(2, C);
        break;

    case 0x52: //    BIT 2,D
        BIT(2, D);
        break;

    case 0x53: //    BIT 2,E
        BIT(2, E);
        break;

    case 0x54: //    BIT 2,H
        BIT(2, H);
        break;

    case 0x55: //    BIT 2,L
        BIT(2, L);
        break;

    case 0x56: //    BIT 2,(HL)
        doHL();
        BIT(2, readMem(HL));
        break;

    case 0x57: //    BIT 2,A
        BIT(2, A);
        break;

    case 0x58: //    BIT 3,B
        BIT(3, B);
        break;

    case 0x59: //    BIT 3,C
        BIT(3, C);
        break;

    case 0x5A: //    BIT 3,D
        BIT(3, D);
        break;

    case 0x5B: //    BIT 3,E
        BIT(3, E);
        break;

    case 0x5C: //    BIT 3,H
        BIT(3, H);
        break;

    case 0x5D: //    BIT 3,L
        BIT(3, L);
        break;

    case 0x5E: //    BIT 3,(HL)
        doHL();
        BIT(3, readMem(HL));
        break;

    case 0x5F: //    BIT 3,A
        BIT(3, A);
        break;

    case 0x60: //    BIT 4,B
        BIT(4, B);
        break;

    case 0x61: //    BIT 4,C
        BIT(4, C);
        break;

    case 0x62: //    BIT 4,D
        BIT(4, D);
        break;

    case 0x63: //    BIT 4,E
        BIT(4, E);
        break;

    case 0x64: //    BIT 4,H
        BIT(4, H);
        break;

    case 0x65: //    BIT 4,L
        BIT(4, L);
        break;

    case 0x66: //    BIT 4,(HL)
        doHL();
        BIT(4, readMem(HL));
        break;

    case 0x67: //    BIT 4,A
        BIT(4, A);
        break;

    case 0x68: //    BIT 5,B
        BIT(5, B);
        break;

    case 0x69: //    BIT 5,C
        BIT(5, C);
        break;

    case 0x6A: //    BIT 5,D
        BIT(5, D);
        break;

    case 0x6B: //    BIT 5,E
        BIT(5, E);
        break;

    case 0x6C: //    BIT 5,H
        BIT(5, H);
        break;

    case 0x6D: //    BIT 5,L
        BIT(5, L);
        break;

    case 0x6E: //    BIT 5,(HL)
        doHL();
        BIT(5, readMem(HL));
        break;

    case 0x6F: //    BIT 5,A
        BIT(5, A);
        break;

    case 0x70: //    BIT 6,B
        BIT(6, B);
        break;

    case 0x71: //    BIT 6,C
        BIT(6, C);
        break;

    case 0x72: //    BIT 6,D
        BIT(6, D);
        break;

    case 0x73: //    BIT 6,E
        BIT(6, E);
        break;

    case 0x74: //    BIT 6,H
        BIT(6, H);
        break;

    case 0x75: //    BIT 6,L
        BIT(6, L);
        break;

    case 0x76: //    BIT 6,(HL)
        doHL();
        BIT(6, readMem(HL));
        break;

    case 0x77: //    BIT 6,A
        BIT(6, A);
        break;

    case 0x78: //    BIT 7,B
        BIT(7, B);
        break;

    case 0x79: //    BIT 7,C
        BIT(7, C);
        break;

    case 0x7A: //    BIT 7,D
        BIT(7, D);
        break;

    case 0x7B: //    BIT 7,E
        BIT(7, E);
        break;

    case 0x7C: //    BIT 7,H
        BIT(7, H);
        break;

    case 0x7D: //    BIT 7,L
        BIT(7, L);
        break;

    case 0x7E: //    BIT 7,(HL)
        doHL();
        BIT(7, readMem(HL));
        break;

    case 0x7F: //    BIT 7,A
        BIT(7, A);
        break;

    case 0x80: //    RES 0,B
        B = RES(0, B);
        break;

    case 0x81: //    RES 0,C
        C = RES(0, C);
        break;

    case 0x82: //    RES 0,D
        D = RES(0, D);
        break;

    case 0x83: //    RES 0,E
        E = RES(0, E);
        break;

    case 0x84: //    RES 0,H
        H = RES(0, H);
        break;

    case 0x85: //    RES 0,L
        L = RES(0, L);
        break;

    case 0x86: //    RES 0,(HL)
        doHL();
        writeMem(HL, RES(0, readMem(HL)));
        break;

    case 0x87: //    RES 0,A
        A = RES(0, A);
        break;

    case 0x88: //    RES 1,B
        B = RES(1, B);
        break;

    case 0x89: //    RES 1,C
        C = RES(1, C);
        break;

    case 0x8A: //    RES 1,D
        D = RES(1, D);
        break;

    case 0x8B: //    RES 1,E
        E = RES(1, E);
        break;

    case 0x8C: //    RES 1,H
        H = RES(1, H);
        break;

    case 0x8D: //    RES 1,L
        L = RES(1, L);
        break;

    case 0x8E: //    RES 1,(HL)
        doHL();
        writeMem(HL, RES(1, readMem(HL)));
        break;

    case 0x8F: //    RES 1,A
        A = RES(1, A);
        break;

    case 0x90: //    RES 2,B
        B = RES(2, B);
        break;

    case 0x91: //    RES 2,C
        C = RES(2, C);
        break;

    case 0x92: //    RES 2,D
        D = RES(2, D);
        break;

    case 0x93: //    RES 2,E
        E = RES(2, E);
        break;

    case 0x94: //    RES 2,H
        H = RES(2, H);
        break;

    case 0x95: //    RES 2,L
        L = RES(2, L);
        break;

    case 0x96: //    RES 2,(HL)
        doHL();
        writeMem(HL, RES(2, readMem(HL)));
        break;

    case 0x97: //    RES 2,A
        A = RES(2, A);
        break;

    case 0x98: //    RES 3,B
        B = RES(3, B);
        break;

    case 0x99: //    RES 3,C
        C = RES(3, C);
        break;

    case 0x9A: //    RES 3,D
        D = RES(3, D);
        break;

    case 0x9B: //    RES 3,E
        E = RES(3, E);
        break;

    case 0x9C: //    RES 3,H
        H = RES(3, H);
        break;

    case 0x9D: //    RES 3,L
        L = RES(3, L);
        break;

    case 0x9E: //    RES 3,(HL)
        doHL();
        writeMem(HL, RES(3, readMem(HL)));
        break;

    case 0x9F: //    RES 3,A
        A = RES(3, A);
        break;

    case 0xA0: //    RES 4,B
        B = RES(4, B);
        break;

    case 0xA1: //    RES 4,C
        C = RES(4, C);
        break;

    case 0xA2: //    RES 4,D
        D = RES(4, D);
        break;

    case 0xA3: //    RES 4,E
        E = RES(4, E);
        break;

    case 0xA4: //    RES 4,H
        H = RES(4, H);
        break;

    case 0xA5: //    RES 4,L
        L = RES(4, L);
        break;

    case 0xA6: //    RES 4,(HL)
        doHL();
        writeMem(HL, RES(4, readMem(HL)));
        break;

    case 0xA7: //    RES 4,A
        A = RES(4, A);
        break;

    case 0xA8: //    RES 5,B
        B = RES(5, B);
        break;

    case 0xA9: //    RES 5,C
        C = RES(5, C);
        break;

    case 0xAA: //    RES 5,D
        D = RES(5, D);
        break;

    case 0xAB: //    RES 5,E
        E = RES(5, E);
        break;

    case 0xAC: //    RES 5,H
        H = RES(5, H);
        break;

    case 0xAD: //    RES 5,L
        L = RES(5, L);
        break;

    case 0xAE: //    RES 5,(HL)
        doHL();
        writeMem(HL, RES(5, readMem(HL)));
        break;

    case 0xAF: //    RES 5,A
        A = RES(5, A);
        break;

    case 0xB0: //    RES 6,B
        B = RES(6, B);
        break;

    case 0xB1: //    RES 6,C
        C = RES(6, C);
        break;

    case 0xB2: //    RES 6,D
        D = RES(6, D);
        break;

    case 0xB3: //    RES 6,E
        E = RES(6, E);
        break;

    case 0xB4: //    RES 6,H
        H = RES(6, H);
        break;

    case 0xB5: //    RES 6,L
        L = RES(6, L);
        break;

    case 0xB6: //    RES 6,(HL)
        doHL();
        writeMem(HL, RES(6, readMem(HL)));
        break;

    case 0xB7: //    RES 6,A
        A = RES(6, A);
        break;

    case 0xB8: //    RES 7,B
        B = RES(7, B);
        break;

    case 0xB9: //    RES 7,C
        C = RES(7, C);
        break;

    case 0xBA: //    RES 7,D
        D = RES(7, D);
        break;

    case 0xBB: //    RES 7,E
        E = RES(7, E);
        break;

    case 0xBC: //    RES 7,H
        H = RES(7, H);
        break;

    case 0xBD: //    RES 7,L
        L = RES(7, L);
        break;

    case 0xBE: //    RES 7,(HL)
        doHL();
        writeMem(HL, RES(7, readMem(HL)));
        break;

    case 0xBF: //    RES 7,A
        A = RES(7, A);
        break;

    case 0xC0: //    SET 0,B
        B = SET(0, B);
        break;

    case 0xC1: //    SET 0,C
        C = SET(0, C);
        break;

    case 0xC2: //    SET 0,D
        D = SET(0, D);
        break;

    case 0xC3: //    SET 0,E
        E = SET(0, E);
        break;

    case 0xC4: //    SET 0,H
        H = SET(0, H);
        break;

    case 0xC5: //    SET 0,L
        L = SET(0, L);
        break;

    case 0xC6: //    SET 0,(HL)
        doHL();
        writeMem(HL, SET(0, readMem(HL)));
        break;

    case 0xC7: //    SET 0,A
        A = SET(0, A);
        break;

    case 0xC8: //    SET 1,B
        B = SET(1, B);
        break;

    case 0xC9: //    SET 1,C
        C = SET(1, C);
        break;

    case 0xCA: //    SET 1,D
        D = SET(1, D);
        break;

    case 0xCB: //    SET 1,E
        E = SET(1, E);
        break;

    case 0xCC: //    SET 1,H
        H = SET(1, H);
        break;

    case 0xCD: //    SET 1,L
        L = SET(1, L);
        break;

    case 0xCE: //    SET 1,(HL)
        doHL();
        writeMem(HL, SET(1, readMem(HL)));
        break;

    case 0xCF: //    SET 1,A
        A = SET(1, A);
        break;

    case 0xD0: //    SET 2,B
        B = SET(2, B);
        break;

    case 0xD1: //    SET 2,C
        C = SET(2, C);
        break;

    case 0xD2: //    SET 2,D
        D = SET(2, D);
        break;

    case 0xD3: //    SET 2,E
        E = SET(2, E);
        break;

    case 0xD4: //    SET 2,H
        H = SET(2, H);
        break;

    case 0xD5: //    SET 2,L
        L = SET(2, L);
        break;

    case 0xD6: //    SET 2,(HL)
        doHL();
        writeMem(HL, SET(2, readMem(HL)));
        break;

    case 0xD7: //    SET 2,A
        A = SET(2, A);
        break;

    case 0xD8: //    SET 3,B
        B = SET(3, B);
        break;

    case 0xD9: //    SET 3,C
        C = SET(3, C);
        break;

    case 0xDA: //    SET 3,D
        D = SET(3, D);
        break;

    case 0xDB: //    SET 3,E
        E = SET(3, E);
        break;

    case 0xDC: //    SET 3,H
        H = SET(3, H);
        break;

    case 0xDD: //    SET 3,L
        L = SET(3, L);
        break;

    case 0xDE: //    SET 3,(HL)
        doHL();
        writeMem(HL, SET(3, readMem(HL)));
        break;

    case 0xDF: //    SET 3,A
        A = SET(3, A);
        break;

    case 0xE0: //    SET 4,B
        B = SET(4, B);
        break;

    case 0xE1: //    SET 4,C
        C = SET(4, C);
        break;

    case 0xE2: //    SET 4,D
        D = SET(4, D);
        break;

    case 0xE3: //    SET 4,E
        E = SET(4, E);
        break;

    case 0xE4: //    SET 4,H
        H = SET(4, H);
        break;

    case 0xE5: //    SET 4,L
        L = SET(4, L);
        break;

    case 0xE6: //    SET 4,(HL)
        doHL();
        writeMem(HL, SET(4, readMem(HL)));
        break;

    case 0xE7: //    SET 4,A
        A = SET(4, A);
        break;

    case 0xE8: //    SET 5,B
        B = SET(5, B);
        break;

    case 0xE9: //    SET 5,C
        C = SET(5, C);
        break;

    case 0xEA: //    SET 5,D
        D = SET(5, D);
        break;

    case 0xEB: //    SET 5,E
        E = SET(5, E);
        break;

    case 0xEC: //    SET 5,H
        H = SET(5, H);
        break;

    case 0xED: //    SET 5,L
        L = SET(5, L);
        break;

    case 0xEE: //    SET 5,(HL)
        doHL();
        writeMem(HL, SET(5, readMem(HL)));
        break;

    case 0xEF: //    SET 5,A
        A = SET(5, A);
        break;

    case 0xF0: //    SET 6,B
        B = SET(6, B);
        break;

    case 0xF1: //    SET 6,C
        C = SET(6, C);
        break;

    case 0xF2: //    SET 6,D
        D = SET(6, D);
        break;

    case 0xF3: //    SET 6,E
        E = SET(6, E);
        break;

    case 0xF4: //    SET 6,H
        H = SET(6, H);
        break;

    case 0xF5: //    SET 6,L
        L = SET(6, L);
        break;

    case 0xF6: //    SET 6,(HL)
        doHL();
        writeMem(HL, SET(6, readMem(HL)));
        break;

    case 0xF7: //    SET 6,A
        A = SET(6, A);
        break;

    case 0xF8: //    SET 7,B
        B = SET(7, B);
        break;

    case 0xF9: //    SET 7,C
        C = SET(7, C);
        break;

    case 0xFA: //    SET 7,D
        D = SET(7, D);
        break;

    case 0xFB: //    SET 7,E
        E = SET(7, E);
        break;

    case 0xFC: //    SET 7,H
        H = SET(7, H);
        break;

    case 0xFD: //    SET 7,L
        L = SET(7, L);
        break;

    case 0xFE: //    SET 7,(HL)
        doHL();
        writeMem(HL, SET(7, readMem(HL)));
        break;

    case 0xFF: //    SET 7,A
        A = SET(7, A);
        break;

    default:
        System.out.println(
                "Unknown CB op code " + Integer.toHexString(opCode));
        break;
    }
}
/*#ExecutionTrace#*///<editor-fold>
//--
//--private String decodecbOpCode(int opcode)
//--{
//--    switch (opcode)
//--    {
//--    case 0x00 :  return "RLC B";
//--    case 0x01 :  return "RLC C";
//--    case 0x02 :  return "RLC D";
//--    case 0x03 :  return "RLC E";
//--    case 0x04 :  return "RLC H";
//--    case 0x05 :  return "RLC L";
//--    case 0x06 :  return "RLC (HL)";
//--    case 0x07 :  return "RLC A";
//--    case 0x08 :  return "RRC B";
//--    case 0x09 :  return "RRC C";
//--    case 0x0A :  return "RRC D";
//--    case 0x0B :  return "RRC E";
//--    case 0x0C :  return "RRC H";
//--    case 0x0D :  return "RRC L";
//--    case 0x0E :  return "RRC (HL)";
//--    case 0x0F :  return "RRC A";
//--
//--    case 0x10 :  return "RL B";
//--    case 0x11 :  return "RL C";
//--    case 0x12 :  return "RL D";
//--    case 0x13 :  return "RL E";
//--    case 0x14 :  return "RL H";
//--    case 0x15 :  return "RL L";
//--    case 0x16 :  return "RL (HL)";
//--    case 0x17 :  return "RL A";
//--    case 0x18 :  return "RR B";
//--    case 0x19 :  return "RR C";
//--    case 0x1A :  return "RR D";
//--    case 0x1B :  return "RR E";
//--    case 0x1C :  return "RR H";
//--    case 0x1D :  return "RR L";
//--    case 0x1E :  return "RR (HL)";
//--    case 0x1F :  return "RR A";
//--
//--    case 0x20 :  return "SLA B";
//--    case 0x21 :  return "SLA C";
//--    case 0x22 :  return "SLA D";
//--    case 0x23 :  return "SLA E";
//--    case 0x24 :  return "SLA H";
//--    case 0x25 :  return "SLA L";
//--    case 0x26 :  return "SLA (HL)";
//--    case 0x27 :  return "SLA A";
//--    case 0x28 :  return "SRA B";
//--    case 0x29 :  return "SRA C";
//--    case 0x2A :  return "SRA D";
//--    case 0x2B :  return "SRA E";
//--    case 0x2C :  return "SRA H";
//--    case 0x2D :  return "SRA L";
//--    case 0x2E :  return "SRA (HL)";
//--    case 0x2F :  return "SRA A";
//--
//--    case 0x30 :  return "SWAP B";
//--    case 0x31 :  return "SWAP C";
//--    case 0x32 :  return "SWAP D";
//--    case 0x33 :  return "SWAP E";
//--    case 0x34 :  return "SWAP H";
//--    case 0x35 :  return "SWAP L";
//--    case 0x36 :  return "SWAP (HL)";
//--    case 0x37 :  return "SWAP A";
//--    case 0x38 :  return "SRL B";
//--    case 0x39 :  return "SRL C";
//--    case 0x3A :  return "SRL D";
//--    case 0x3B :  return "SRL E";
//--    case 0x3C :  return "SRL H";
//--    case 0x3D :  return "SRL L";
//--    case 0x3E :  return "SRL (HL)";
//--    case 0x3F :  return "SRL A";
//--
//--    case 0x40 :  return "BIT 0,B";
//--    case 0x41 :  return "BIT 0,C";
//--    case 0x42 :  return "BIT 0,D";
//--    case 0x43 :  return "BIT 0,E";
//--    case 0x44 :  return "BIT 0,H";
//--    case 0x45 :  return "BIT 0,L";
//--    case 0x46 :  return "BIT 0,(HL)";
//--    case 0x47 :  return "BIT 0,A";
//--    case 0x48 :  return "BIT 1,B";
//--    case 0x49 :  return "BIT 1,C";
//--    case 0x4A :  return "BIT 1,D";
//--    case 0x4B :  return "BIT 1,E";
//--    case 0x4C :  return "BIT 1,H";
//--    case 0x4D :  return "BIT 1,L";
//--    case 0x4E :  return "BIT 1,(HL)";
//--    case 0x4F :  return "BIT 1,A";
//--
//--    case 0x50 :  return "BIT 2,B";
//--    case 0x51 :  return "BIT 2,C";
//--    case 0x52 :  return "BIT 2,D";
//--    case 0x53 :  return "BIT 2,E";
//--    case 0x54 :  return "BIT 2,H";
//--    case 0x55 :  return "BIT 2,L";
//--    case 0x56 :  return "BIT 2,(HL)";
//--    case 0x57 :  return "BIT 2,A";
//--    case 0x58 :  return "BIT 3,B";
//--    case 0x59 :  return "BIT 3,C";
//--    case 0x5A :  return "BIT 3,D";
//--    case 0x5B :  return "BIT 3,E";
//--    case 0x5C :  return "BIT 3,H";
//--    case 0x5D :  return "BIT 3,L";
//--    case 0x5E :  return "BIT 3,(HL)";
//--    case 0x5F :  return "BIT 3,A";
//--
//--    case 0x60 :  return "BIT 4,B";
//--    case 0x61 :  return "BIT 4,C";
//--    case 0x62 :  return "BIT 4,D";
//--    case 0x63 :  return "BIT 4,E";
//--    case 0x64 :  return "BIT 4,H";
//--    case 0x65 :  return "BIT 4,L";
//--    case 0x66 :  return "BIT 4,(HL)";
//--    case 0x67 :  return "BIT 4,A";
//--    case 0x68 :  return "BIT 5,B";
//--    case 0x69 :  return "BIT 5,C";
//--    case 0x6A :  return "BIT 5,D";
//--    case 0x6B :  return "BIT 5,E";
//--    case 0x6C :  return "BIT 5,H";
//--    case 0x6D :  return "BIT 5,L";
//--    case 0x6E :  return "BIT 5,(HL)";
//--    case 0x6F :  return "BIT 5,A";
//--
//--    case 0x70 :  return "BIT 6,B";
//--    case 0x71 :  return "BIT 6,C";
//--    case 0x72 :  return "BIT 6,D";
//--    case 0x73 :  return "BIT 6,E";
//--    case 0x74 :  return "BIT 6,H";
//--    case 0x75 :  return "BIT 6,L";
//--    case 0x76 :  return "BIT 6,(HL)";
//--    case 0x77 :  return "BIT 6,A";
//--    case 0x78 :  return "BIT 7,B";
//--    case 0x79 :  return "BIT 7,C";
//--    case 0x7A :  return "BIT 7,D";
//--    case 0x7B :  return "BIT 7,E";
//--    case 0x7C :  return "BIT 7,H";
//--    case 0x7D :  return "BIT 7,L";
//--    case 0x7E :  return "BIT 7,(HL)";
//--    case 0x7F :  return "BIT 7,A";
//--
//--    case 0x80 :  return "RES 0,B";
//--    case 0x81 :  return "RES 0,C";
//--    case 0x82 :  return "RES 0,D";
//--    case 0x83 :  return "RES 0,E";
//--    case 0x84 :  return "RES 0,H";
//--    case 0x85 :  return "RES 0,L";
//--    case 0x86 :  return "RES 0,(HL)";
//--    case 0x87 :  return "RES 0,A";
//--    case 0x88 :  return "RES 1,B";
//--    case 0x89 :  return "RES 1,C";
//--    case 0x8A :  return "RES 1,D";
//--    case 0x8B :  return "RES 1,E";
//--    case 0x8C :  return "RES 1,H";
//--    case 0x8D :  return "RES 1,L";
//--    case 0x8E :  return "RES 1,(HL)";
//--    case 0x8F :  return "RES 1,A";
//--
//--    case 0x90 :  return "RES 2,B";
//--    case 0x91 :  return "RES 2,C";
//--    case 0x92 :  return "RES 2,D";
//--    case 0x93 :  return "RES 2,E";
//--    case 0x94 :  return "RES 2,H";
//--    case 0x95 :  return "RES 2,L";
//--    case 0x96 :  return "RES 2,(HL)";
//--    case 0x97 :  return "RES 2,A";
//--    case 0x98 :  return "RES 3,B";
//--    case 0x99 :  return "RES 3,C";
//--    case 0x9A :  return "RES 3,D";
//--    case 0x9B :  return "RES 3,E";
//--    case 0x9C :  return "RES 3,H";
//--    case 0x9D :  return "RES 3,L";
//--    case 0x9E :  return "RES 3,(HL)";
//--    case 0x9F :  return "RES 3,A";
//--
//--    case 0xA0 :  return "RES 4,B";
//--    case 0xA1 :  return "RES 4,C";
//--    case 0xA2 :  return "RES 4,D";
//--    case 0xA3 :  return "RES 4,E";
//--    case 0xA4 :  return "RES 4,H";
//--    case 0xA5 :  return "RES 4,L";
//--    case 0xA6 :  return "RES 4,(HL)";
//--    case 0xA7 :  return "RES 4,A";
//--    case 0xA8 :  return "RES 5,B";
//--    case 0xA9 :  return "RES 5,C";
//--    case 0xAA :  return "RES 5,D";
//--    case 0xAB :  return "RES 5,E";
//--    case 0xAC :  return "RES 5,H";
//--    case 0xAD :  return "RES 5,L";
//--    case 0xAE :  return "RES 5,(HL)";
//--    case 0xAF :  return "RES 5,A";
//--
//--    case 0xB0 :  return "RES 6,B";
//--    case 0xB1 :  return "RES 6,C";
//--    case 0xB2 :  return "RES 6,D";
//--    case 0xB3 :  return "RES 6,E";
//--    case 0xB4 :  return "RES 6,H";
//--    case 0xB5 :  return "RES 6,L";
//--    case 0xB6 :  return "RES 6,(HL)";
//--    case 0xB7 :  return "RES 6,A";
//--    case 0xB8 :  return "RES 7,B";
//--    case 0xB9 :  return "RES 7,C";
//--    case 0xBA :  return "RES 7,D";
//--    case 0xBB :  return "RES 7,E";
//--    case 0xBC :  return "RES 7,H";
//--    case 0xBD :  return "RES 7,L";
//--    case 0xBE :  return "RES 7,(HL)";
//--    case 0xBF :  return "RES 7,A";
//--
//--    case 0xC0 :  return "SET 0,B";
//--    case 0xC1 :  return "SET 0,C";
//--    case 0xC2 :  return "SET 0,D";
//--    case 0xC3 :  return "SET 0,E";
//--    case 0xC4 :  return "SET 0,H";
//--    case 0xC5 :  return "SET 0,L";
//--    case 0xC6 :  return "SET 0,(HL)";
//--    case 0xC7 :  return "SET 0,A";
//--    case 0xC8 :  return "SET 1,B";
//--    case 0xC9 :  return "SET 1,C";
//--    case 0xCA :  return "SET 1,D";
//--    case 0xCB :  return "SET 1,E";
//--    case 0xCC :  return "SET 1,H";
//--    case 0xCD :  return "SET 1,L";
//--    case 0xCE :  return "SET 1,(HL)";
//--    case 0xCF :  return "SET 1,A";
//--
//--    case 0xD0 :  return "SET 2,B";
//--    case 0xD1 :  return "SET 2,C";
//--    case 0xD2 :  return "SET 2,D";
//--    case 0xD3 :  return "SET 2,E";
//--    case 0xD4 :  return "SET 2,H";
//--    case 0xD5 :  return "SET 2,L";
//--    case 0xD6 :  return "SET 2,(HL)";
//--    case 0xD7 :  return "SET 2,A";
//--    case 0xD8 :  return "SET 3,B";
//--    case 0xD9 :  return "SET 3,C";
//--    case 0xDA :  return "SET 3,D";
//--    case 0xDB :  return "SET 3,E";
//--    case 0xDC :  return "SET 3,H";
//--    case 0xDD :  return "SET 3,L";
//--    case 0xDE :  return "SET 3,(HL)";
//--    case 0xDF :  return "SET 3,A";
//--
//--    case 0xE0 :  return "SET 4,B";
//--    case 0xE1 :  return "SET 4,C";
//--    case 0xE2 :  return "SET 4,D";
//--    case 0xE3 :  return "SET 4,E";
//--    case 0xE4 :  return "SET 4,H";
//--    case 0xE5 :  return "SET 4,L";
//--    case 0xE6 :  return "SET 4,(HL)";
//--    case 0xE7 :  return "SET 4,A";
//--    case 0xE8 :  return "SET 5,B";
//--    case 0xE9 :  return "SET 5,C";
//--    case 0xEA :  return "SET 5,D";
//--    case 0xEB :  return "SET 5,E";
//--    case 0xEC :  return "SET 5,H";
//--    case 0xED :  return "SET 5,L";
//--    case 0xEE :  return "SET 5,(HL)";
//--    case 0xEF :  return "SET 5,A";
//--
//--    case 0xF0 :  return "SET 6,B";
//--    case 0xF1 :  return "SET 6,C";
//--    case 0xF2 :  return "SET 6,D";
//--    case 0xF3 :  return "SET 6,E";
//--    case 0xF4 :  return "SET 6,H";
//--    case 0xF5 :  return "SET 6,L";
//--    case 0xF6 :  return "SET 6,(HL)";
//--    case 0xF7 :  return "SET 6,A";
//--    case 0xF8 :  return "SET 7,B";
//--    case 0xF9 :  return "SET 7,C";
//--    case 0xFA :  return "SET 7,D";
//--    case 0xFB :  return "SET 7,E";
//--    case 0xFC :  return "SET 7,H";
//--    case 0xFD :  return "SET 7,L";
//--    case 0xFE :  return "SET 7,(HL)";
//--    case 0xFF :  return "SET 7,A";
//--    }
//--    return "Unknown CB op code " + Integer.toHexString(opcode);
//--}
//--private String decodeOpCode(int opcode)
//--{
//--    switch (opcode)
//--    {
//--    case 0x00 :
//--           return "NOP";
//--    case 0x01 :
//--           return  "LD BC, $aabb";
//--    case 0x02 :
//--           return "LD (BC), A";
//--    case 0x03 :
//--           return "INC BC";
//--    case 0x04 :
//--           return "INC B";
//--    case 0x05 :
//--           return "DEC B";
//--    case 0x06 :
//--           return "LD B, $aa";
//--    case 0x07 :
//--           return "RLC A";
//--    case 0x08 :
//--           return "LD ($aabb), SP";
//--    case 0x09 :
//--           return "ADD HL, BC";
//--    case 0x0A :
//--           return "LD A, (BC)";
//--    case 0x0B :
//--           return "DEC BC";
//--    case 0x0C :
//--           return "INC C";
//--    case 0x0D :
//--           return "DEC C";
//--    case 0x0E :
//--           return "LD C, $aa";
//--    case 0x0F :
//--           return "RRC A";
//--    case 0x10 :
//--           return "STOP";
//--    case 0x11 :
//--           return "LD DE, $aabb";
//--    case 0x12 :
//--           return "LD (DE), A";
//--    case 0x13 :
//--           return "INC DE";
//--    case 0x14 :
//--           return "INC D";
//--    case 0x15 :
//--           return "DEC D";
//--    case 0x16 :
//--           return "LD D, $aa";
//--    case 0x17 :
//--           return "RL A";
//--    case 0x18 :
//--           return "JR $aa";
//--    case 0x19 :
//--           return "ADD HL, DE";
//--    case 0x1A :
//--           return "LD A, (DE)";
//--    case 0x1B :
//--           return "DEC DE";
//--    case 0x1C :
//--           return "INC E";
//--    case 0x1D :
//--           return "DEC E";
//--    case 0x1E :
//--           return "LD E, $aa";
//--    case 0x1F :
//--           return "RR A";
//--    case 0x20 :
//--           return "JR NZ, $aa";
//--    case 0x21 :
//--           return "LD HL, $aabb";
//--    case 0x22 :
//--           return "LD (HLI), A";
//--    case 0x23 :
//--           return "INC HL";
//--    case 0x24 :
//--           return "INC H";
//--    case 0x25 :
//--           return "DEC H";
//--    case 0x26 :
//--           return "LD H, $aa";
//--    case 0x27 :
//--           return "DAA";
//--    case 0x28 :
//--           return "JR Z, $aa";
//--    case 0x29 :
//--           return "ADD HL, HL";
//--    case 0x2A :
//--           return "LD A, (HLI)";
//--    case 0x2B :
//--           return "DEC HL";
//--    case 0x2C :
//--           return "INC L";
//--    case 0x2D :
//--           return "DEC L";
//--    case 0x2E :
//--           return "LD L, $aa";
//--    case 0x2F :
//--           return "CPL";
//--    case 0x30 :
//--           return "JR NC, $aa";
//--    case 0x31 :
//--           return "LD SP, $aabb";
//--    case 0x32 :
//--           return "LD (HLD), A";
//--    case 0x33 :
//--           return "INC SP";
//--    case 0x34 :
//--           return "INC (HL)";
//--    case 0x35 :
//--           return "DEC (HL)";
//--    case 0x36 :
//--           return "LD (HL), $aa";
//--    case 0x37 :
//--           return "SCF";
//--    case 0x38 :
//--           return "JR C, $aa";
//--    case 0x39 :
//--           return "ADD HL, SP";
//--    case 0x3A :
//--           return "LD A, (HLD)";
//--    case 0x3B :
//--           return "DEC SP";
//--    case 0x3C :
//--           return "INC A";
//--    case 0x3D :
//--           return "DEC A";
//--    case 0x3E :
//--           return "LD A, $aa";
//--    case 0x3F :
//--           return "CCF";
//--    case 0x76 :
//--           return "HALT";
//--    case 0x40 :    return "LD B,B";
//--    case 0x49 :    return "LD C,C";
//--    case 0x52 :    return "LD D,D";
//--    case 0x5B :    return "LD E,E";
//--    case 0x64 :    return "LD H,H";
//--    case 0x6D :    return "LD L,L";
//--    case 0x7F :    return "LD A,A";
//--    case 0x41 :  return "LD B,C";
//--    case 0x42 :  return "LD B,D";
//--    case 0x43 :  return "LD B,E";
//--    case 0x44 :  return "LD B,H";
//--    case 0x45 :  return "LD B,L";
//--    case 0x46 :  return "LD B,(HL)";
//--    case 0x47 :  return "LD B,A";
//--    case 0x48 :  return "LD C,B";
//--    case 0x4A :  return "LD C,D";
//--    case 0x4B :  return "LD C,E";
//--    case 0x4C :  return "LD C,H";
//--    case 0x4D :  return "LD C,L";
//--    case 0x4E :  return "LD C,(HL)";
//--    case 0x4F :  return "LD C,A";
//--    case 0x50 :  return "LD D,B";
//--    case 0x51 :  return "LD D,C";
//--    case 0x53 :  return "LD D,E";
//--    case 0x54 :  return "LD D,H";
//--    case 0x55 :  return "LD D,L";
//--    case 0x56 :  return "LD D,(HL)";
//--    case 0x57 :  return "LD D,A";
//--    case 0x58 :  return "LD E,B";
//--    case 0x59 :  return "LD E,C";
//--    case 0x5A :  return "LD E,D";
//--    case 0x5C :  return "LD E,H";
//--    case 0x5D :  return "LD E,L";
//--    case 0x5E :  return "LD E,(HL)";
//--    case 0x5F :  return "LD E,A";
//--    case 0x60 :  return "LD H,B";
//--    case 0x61 :  return "LD H,C";
//--    case 0x62 :  return "LD H,D";
//--    case 0x63 :  return "LD H,E";
//--    case 0x65 :  return "LD H,L";
//--    case 0x66 :  return "LD H,(HL)";
//--    case 0x67 :  return "LD H,A";
//--    case 0x68 :  return "LD L,B";
//--    case 0x69 :  return "LD L,C";
//--    case 0x6A :  return "LD L,D";
//--    case 0x6B :  return "LD L,E";
//--    case 0x6C :  return "LD L,H";
//--    case 0x6E :  return "LD L,(HL)";
//--    case 0x6F :  return "LD L,A";
//--    case 0x70 :  return "LD (HL),B";
//--    case 0x71 :  return "LD (HL),C";
//--    case 0x72 :  return "LD (HL),D";
//--    case 0x73 :  return "LD (HL),E";
//--    case 0x74 :  return "LD (HL),H";
//--    case 0x75 :  return "LD (HL),L";
//--    case 0x77 :  return "LD (HL),A";
//--    case 0x78 :  return "LD A,B";
//--    case 0x79 :  return "LD A,C";
//--    case 0x7A :  return "LD A,D";
//--    case 0x7B :  return "LD A,E";
//--    case 0x7C :  return "LD A,H";
//--    case 0x7D :  return "LD A,L";
//--    case 0x7E :  return "LD A,(HL)";
//--    case 0x80 :  return "ADD A,B";
//--    case 0x81 :  return "ADD A,C";
//--    case 0x82 :  return "ADD A,D";
//--    case 0x83 :  return "ADD A,E";
//--    case 0x84 :  return "ADD A,H";
//--    case 0x85 :  return "ADD A,L";
//--    case 0x86 :  return "ADD A,(HL)";
//--    case 0x87 :  return "ADD A,A";
//--    case 0x88 :  return "ADDC A,B";
//--    case 0x89 :  return "ADDC A,C";
//--    case 0x8A :  return "ADDC A,D";
//--    case 0x8B :  return "ADDC A,E";
//--    case 0x8C :  return "ADDC A,H";
//--    case 0x8D :  return "ADDC A,L";
//--    case 0x8E :  return "ADDC A,(HL)";
//--    case 0x8F :  return "ADDC A,A";
//--    case 0x90 :  return "SUB B";
//--    case 0x91 :  return "SUB C";
//--    case 0x92 :  return "SUB D";
//--    case 0x93 :  return "SUB E";
//--    case 0x94 :  return "SUB H";
//--    case 0x95 :  return "SUB L";
//--    case 0x96 :  return "SUB (HL)";
//--    case 0x97 :  return "SUB A";
//--    case 0x98 :  return "SUBC B";
//--    case 0x99 :  return "SUBC C";
//--    case 0x9A :  return "SUBC D";
//--    case 0x9B :  return "SUBC E";
//--    case 0x9C :  return "SUBC H";
//--    case 0x9D :  return "SUBC L";
//--    case 0x9E :  return "SUBC (HL)";
//--    case 0x9F :  return "SUBC A";
//--    case 0xA0 :  return "AND B";
//--    case 0xA1 :  return "AND C";
//--    case 0xA2 :  return "AND D";
//--    case 0xA3 :  return "AND E";
//--    case 0xA4 :  return "AND H";
//--    case 0xA5 :  return "AND L";
//--    case 0xA6 :  return "AND (HL)";
//--    case 0xA7 :  return "AND A";
//--    case 0xA8 :  return "XOR B";
//--    case 0xA9 :  return "XOR C";
//--    case 0xAA :  return "XOR D";
//--    case 0xAB :  return "XOR E";
//--    case 0xAC :  return "XOR H";
//--    case 0xAD :  return "XOR L";
//--    case 0xAE :  return "XOR (HL)";
//--    case 0xAF :  return "XOR A";
//--    case 0xB0 :  return "OR B";
//--    case 0xB1 :  return "OR C";
//--    case 0xB2 :  return "OR D";
//--    case 0xB3 :  return "OR E";
//--    case 0xB4 :  return "OR H";
//--    case 0xB5 :  return "OR L";
//--    case 0xB6 :  return "OR (HL)";
//--    case 0xB7 :  return "OR A";
//--    case 0xB8 :  return "CP B";
//--    case 0xB9 :  return "CP C";
//--    case 0xBA :  return "CP D";
//--    case 0xBB :  return "CP E";
//--    case 0xBC :  return "CP H";
//--    case 0xBD :  return "CP L";
//--    case 0xBE :  return "CP (HL)";
//--    case 0xBF :  return "CP A";
//--    case 0xC0 :
//--           return "RET NZ";
//--    case 0xC1 :
//--           return "POP BC";
//--    case 0xC2 :
//--           return "JP NZ, $aabb";
//--    case 0xC3 :
//--           return "JP $aabb";
//--    case 0xC4 :
//--           return "CALL NZ, $aabb";
//--    case 0xC5 :
//--           return "PUSH BC";
//--    case 0xC6 :
//--           return "ADD A, $aa";
//--    case 0xC7 :
//--           return "RST 00";
//--    case 0xC8 :
//--           return "RET Z";
//--    case 0xC9 :
//--           return "RET";
//--    case 0xCA :
//--           return "JP Z, $aabb";
//--    case 0xCB :
//--           return "0xcb op";
//--    case 0xCC :
//--           return "CALL Z, $aabb";
//--    case 0xCD :
//--           return "CALL $aabb";
//--    case 0xCE :
//--           return "ADC A, $aa";
//--    case 0xCF :
//--           return "RST 08";
//--    case 0xD0 :
//--           return "RET NC";
//--    case 0xD1 :
//--           return "POP DE";
//--    case 0xD2 :
//--           return "JP NC, $aabb";
//--    case 0xD4 :
//--           return "CALL NC, $aabb";
//--    case 0xD5 :
//--           return "PUSH DE";
//--    case 0xD6 :
//--           return "SUB A, $aa";
//--    case 0xD7 :
//--           return "RST 10";
//--    case 0xD8 :
//--           return "RET C";
//--    case 0xD9 :
//--           return "RETI";
//--    case 0xDA :
//--           return "JP C, $aabb";
//--    case 0xDC :
//--           return "CALL C, $aabb";
//--    case 0xDE :
//--           return "SBC A, $aa";
//--    case 0xDF :
//--           return "RST 18";
//--    case 0xE0 :
//--           return "LD ($FFxx), A";
//--    case 0xE1 :
//--           return "POP HL";
//--    case 0xE2 :
//--           return "LD (FF00 + C), A";
//--    case 0xE5 :
//--           return "PUSH HL";
//--    case 0xE6 :
//--           return "AND $aa";
//--    case 0xE7 :
//--           return "RST 20";
//--    case 0xE8 :
//--           return "ADD SP, $aa";
//--    case 0xE9 :
//--           return "JP (HL)";
//--    case 0xEA :
//--           return "LD ($aabb), A";
//--    case 0xEE :
//--           return "XOR $aa";
//--    case 0xEF :
//--           return "RST 28";
//--    case 0xF0 :
//--           return "LD A, (FFaa)";
//--    case 0xF1 :
//--           return "POP AF";
//--    case 0xF2 :
//--           return "LD A, (FF00 + C)";
//--    case 0xF3 :
//--           return "DI";
//--    case 0xF5 :
//--           return "PUSH AF";
//--    case 0xF6 :
//--           return "OR $aa";
//--    case 0xF7 :
//--           return "RST 30";
//--    case 0xF8 :
//--           return "LD HL, SP + $aa";
//--    case 0xF9 :
//--           return "LD SP, HL";
//--    case 0xFA :
//--           return "LD A, ($aabb)";
//--    case 0xFB :
//--           return "EI";
//--    case 0xFE :
//--           return "CP $aa";
//--    case 0xFF :
//--           return "RST 38";
//--   }
//--   return "Unknown op " + Integer.toHexString(opcode);
//--}
/*$ExecutionTrace$*///</editor-fold>

}
