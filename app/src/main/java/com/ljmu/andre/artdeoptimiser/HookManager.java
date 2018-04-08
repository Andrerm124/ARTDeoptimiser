package com.ljmu.andre.artdeoptimiser;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.ljmu.andre.artdeoptimiser.Utils.HookUtils.HookBefore;
import com.ljmu.andre.artdeoptimiser.Utils.HookUtils.MethodHook;
import com.ljmu.andre.artdeoptimiser.Utils.PackageUtils;

import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class HookManager implements IXposedHookLoadPackage {
	private Set<String> deopPackageSet;
	private Set<String> debugPackageSet;

	@Override public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		if (lpparam.packageName.equals("android")) {
			try {
				ClassLoader classLoader = lpparam.classLoader;
				Class packageOptimizerClass = findClass("com.android.server.pm.PackageDexOptimizer", classLoader);

				XposedBridge.hookAllMethods(
						packageOptimizerClass,
						"performDexOpt",
						new MethodHook((HookBefore) param -> {
							Log.d(TAG, "PackageDexOptimiser running: " + param.method.toString());

							Object packageObj = param.args[0];
							ApplicationInfo appInfo = (ApplicationInfo) getObjectField(packageObj, "applicationInfo");
							String packageName = appInfo.packageName;
							Log.d(TAG, "PackageDexOptimiser running: " + packageName);

							boolean invalidateDataSets = deopPackageSet != null && PackageUtils.shouldInvalidateSets();

							if (deopPackageSet == null || invalidateDataSets) {
								Log.d(TAG, "Deop package set was null");
								deopPackageSet = PackageUtils.getDeopOrDebugProps(PackageUtils.PROP_DEOPTIMISE);
							}

							if (debugPackageSet == null || invalidateDataSets) {
								Log.d(TAG, "Debug package set was null");
								debugPackageSet = PackageUtils.getDeopOrDebugProps(PackageUtils.PROP_DEBUG);
							}

							Log.d(TAG, "CompilerFilter: " + param.args[4]);

							if (deopPackageSet.contains(packageName) && param.args[4].equals("speed-profile")) {
								Log.d(TAG, "Replacing Speed Profile");
								XposedBridge.log("Replaced speed profile for: " + packageName);
								param.args[4] = "quicken";
							}

							if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != ApplicationInfo.FLAG_DEBUGGABLE) {
								Log.d(TAG, "App debuggable not assigned");

								if (debugPackageSet.contains(packageName)) {
									Log.d(TAG, "Assigning debuggable mode");
									appInfo.flags |= ApplicationInfo.FLAG_DEBUGGABLE;
								}
							}
						}
						));
			} catch (Throwable t) {
				Log.e(TAG, "Error with main xposed hooking", t);
				XposedBridge.log(t);
			}
		}
	}
}
