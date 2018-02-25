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

				XposedBridge.hookAllMethods(
						findClass("com.android.server.pm.PackageDexOptimizer", classLoader),
						"performDexOpt",
						new XC_MethodHook() {
							@Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
								Log.d(TAG, "PackageDexOptimiser running");

								Object packageObj = param.args[0];
								ApplicationInfo appInfo = (ApplicationInfo) getObjectField(packageObj, "applicationInfo");
								String packageName = appInfo.packageName;
								Log.d(TAG, "PackageDexOptimiser running: " + packageName);

								synchronized (DEOP_FOLDER_LOCK) {
									if (deopFolder == null) {
										deopFolder = PackageUtils.getDeoptimisationFolder();
									}
								}

								if ((appInfo.flags & ApplicationInfo.FLAG_VM_SAFE_MODE) != ApplicationInfo.FLAG_VM_SAFE_MODE) {
									Log.d(TAG, "App safemode not assigned");

									if (new File(deopFolder, packageName + PackageUtils.DEOP_EXT).exists()) {
										Log.d(TAG, "Assigning safe mode");
										appInfo.flags |= ApplicationInfo.FLAG_VM_SAFE_MODE;
									}
								}

								if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != ApplicationInfo.FLAG_DEBUGGABLE) {
									Log.d(TAG, "App debuggable not assigned");

									if (new File(deopFolder, packageName + PackageUtils.DEBUG_EXT).exists()) {
										Log.d(TAG, "Assigning debuggable mode");
										appInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
									}
								}
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
