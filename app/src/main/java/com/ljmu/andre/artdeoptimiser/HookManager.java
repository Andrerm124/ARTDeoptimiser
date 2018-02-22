package com.ljmu.andre.artdeoptimiser;

import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.ljmu.andre.artdeoptimiser.Utils.PackageUtils;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class HookManager implements IXposedHookLoadPackage {
	private static final Object DEOP_FOLDER_LOCK = new Object();
	private File deopFolder;

	@Override public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals("android")) {
			try {
				ClassLoader classLoader = lpparam.classLoader;
				Class packageParserClass = findClass("android.content.pm.PackageParser", classLoader);
				Class packageClass = findClass("android.content.pm.PackageParser$Package", classLoader);

				findAndHookMethod(
						packageParserClass,
						"parseBaseApplication", packageClass, Resources.class, XmlResourceParser.class, int.class, String[].class,
						new XC_MethodHook() {
							@Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
								Log.d(TAG, "Parsing Base Application");

								// Lock required as this function is highly multi-threaded ===================
								synchronized (DEOP_FOLDER_LOCK) {
									if (deopFolder == null) {
										deopFolder = PackageUtils.getDeoptimisationFolder();
									}
								}

								ApplicationInfo appInfo = (ApplicationInfo) getObjectField(param.args[0], "applicationInfo");

								if (appInfo == null) {
									return;
								}

								String packageName = appInfo.packageName;

								XposedBridge.log("Adjusting package flags for: " + packageName);

								Log.d(TAG, "Starting Flag Manipulation");
								Log.d(TAG, "Start Flags: " + appInfo.flags);

								if (new File(deopFolder, packageName + PackageUtils.DEOP_EXT).exists())
									appInfo.flags |= ApplicationInfo.FLAG_VM_SAFE_MODE;

								if (new File(deopFolder, packageName + PackageUtils.DEBUG_EXT).exists())
									appInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;

								Log.d(TAG, "End Flags: " + appInfo.flags);
							}
						}
				);
			} catch (Throwable t) {
				Log.e(TAG, "Error with main xposed hooking", t);
				XposedBridge.log(t);
			}
		}
	}
}
