package com.ljmu.andre.artdeoptimiser.Utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;
import com.ljmu.andre.artdeoptimiser.MainActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class PropUtils {
	@NonNull public static String getProperty(String key) {
		try {
			Class<?> systemPropertiesClass = Class.forName("android.os.SystemProperties");

			Method method = systemPropertiesClass.getDeclaredMethod(
					"get",
					String.class
			);

			return (String) method.invoke(null, key);
		} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
			Log.e(MainActivity.TAG, "Error calling getProp", e);
		}

		return "";
	}
}
