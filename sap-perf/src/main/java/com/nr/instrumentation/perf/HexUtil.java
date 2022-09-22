package com.nr.instrumentation.perf;

public class HexUtil {

	private static final char[] aHexAlphabet = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B',
			'C', 'D', 'E', 'F'};
	private static final byte[] aHexConversion = new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1, -1, -1, -1, -1, -1, -1, 10, 11, 12, 13, 14, 15,
			-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 10,
			11, 12, 13, 14, 15, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
			-1, -1, -1};
	private static final int BLOCK_SIZE = 512;

	public static synchronized String encode(byte[] aByte) {
		if (aByte == null) {
			return null;
		} else {
			char[] buffer = new char[aByte.length * 2];
			int pos = 0;

			for (int i = 0; i < aByte.length; ++i) {
				buffer[pos++] = aHexAlphabet[(aByte[i] & 240) >>> 4];
				buffer[pos++] = aHexAlphabet[aByte[i] & 15];
			}

			return new String(buffer);
		}
	}

	public static synchronized String encodeColon(byte[] aByte) {
		if (aByte == null) {
			return null;
		} else {
			char[] buffer = new char[aByte.length * 2 + (aByte.length - 1)];
			int pos = 0;

			for (int i = 0; i < aByte.length; ++i) {
				if (i != 0) {
					buffer[pos++] = ':';
				}

				buffer[pos++] = aHexAlphabet[(aByte[i] & 240) >>> 4];
				buffer[pos++] = aHexAlphabet[aByte[i] & 15];
			}

			return new String(buffer);
		}
	}

	public static byte[] decode(String hexadecimal) {
		if (hexadecimal == null) {
			return null;
		} else if (hexadecimal.length() % 2 == 1) {
			throw new NumberFormatException(
					"Odd number of hexadecimal characters! Length: " + Integer.toString(hexadecimal.length()));
		} else {
			int size = hexadecimal.length() / 2;
			byte[] aByte = new byte[size];
			int bufferSize;
			if (size > BLOCK_SIZE) {
				bufferSize = 1024;
			} else {
				bufferSize = 2 * size;
			}

			char[] buffer = new char[bufferSize];
			int pos = 0;

			for (int i = 0; i < size; i += BLOCK_SIZE) {
				int end = BLOCK_SIZE + i;
				if (end > size) {
					end = size;
				}

				hexadecimal.getChars(pos, pos + 2 * (end - i), buffer, 0);
				int _pos = 0;

				for (int k = i; k < end; ++k) {
					char cTemp = buffer[_pos++];
					byte hByte = aHexConversion[cTemp];
					if (hByte == -1) {
						throw new NumberFormatException("Found NO hexadecimal character: " + cTemp);
					}

					cTemp = buffer[_pos++];
					byte lByte = aHexConversion[cTemp];
					if (lByte == -1) {
						throw new NumberFormatException("Found NO hexadecimal character: " + cTemp);
					}

					aByte[k] = (byte) ((byte) (hByte << 4) + lByte);
				}

				pos += 1024;
			}

			return aByte;
		}
	}

}
