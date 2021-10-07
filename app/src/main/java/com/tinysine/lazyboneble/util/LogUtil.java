package com.tinysine.lazyboneble.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.os.Environment;
import android.util.Log;

public class LogUtil {

	private static final boolean DEBUG = true;
	private static final String TAG = "tag";

	public static void e(String text) {
		if (DEBUG) {
			Log.e(TAG, text);
		}
	}

	public static void i(String text) {
		if (DEBUG) {
			Log.i(TAG, text);
		}
	}

	public static void d(String text) {
		if (DEBUG) {
			Log.d(TAG, text);
		}
	}

	public static void v(String text) {
		if (DEBUG) {
			Log.v(TAG, text);
		}
	}

	public static void log2F(String conent) {
		File file = new File(Environment.getExternalStorageDirectory(),"lazybone33.txt");
		try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file, true))))
			{
				out.write("\n" + conent);
			} catch (Exception ignored)
			{
			}
	}
}
