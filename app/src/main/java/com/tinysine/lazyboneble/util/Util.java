package com.tinysine.lazyboneble.util;

public class Util {
	public static final int BT_DATA_MODE = 104;
	public static final int BT_DATA_STATUS = 105;
	public static final int BT_DATA_PASSWORD= 106;
	public static byte[] hexStr2Bytes(String src) {
		int m;
		int n;
		int l = src.length() / 2;
		System.out.println(l);
		byte[] ret = new byte[l];
		for (int i = 0; i < l; i++) {
			m = i * 2 + 1;
			n = m + 1;
			ret[i] = uniteBytes(src.substring(i * 2, m), src.substring(m, n));
		}
		return ret;
	}

	private static byte uniteBytes(String src0, String src1) {
		byte b0 = Byte.decode("0x" + src0);
		b0 = (byte) (b0 << 4);
		byte b1 = Byte.decode("0x" + src1);
		byte ret = (byte) (b0 | b1);
		return ret;
	}
}
