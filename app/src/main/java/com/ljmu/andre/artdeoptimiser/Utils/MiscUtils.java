package com.ljmu.andre.artdeoptimiser.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class MiscUtils {
	public static long timeOffset(long time) {
		return timeOffset(System.currentTimeMillis(), time);
	}

	public static long timeOffset(long largestTime, long shortestTime) {
		return largestTime - shortestTime;
	}

	public static File getExternalDir() {
		try {
			Class<?> environment_cls = Class.forName("android.os.Environment");
			Method setUserRequiredM = environment_cls.getMethod("setUserRequired", boolean.class);
			setUserRequiredM.invoke(null, false);
		} catch (Exception e) {
			Log.e(TAG, "Get external path exception", e);
		}

		return Environment.getExternalStorageDirectory();
	}

	public static void openWebsite(Context context, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(browserIntent);
	}
}
