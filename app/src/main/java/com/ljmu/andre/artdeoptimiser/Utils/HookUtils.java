package com.ljmu.andre.artdeoptimiser.Utils;

import android.util.Log;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class HookUtils {
	public interface HookBefore {
		void before(MethodHookParam param) throws Throwable;
	}

	public interface HookAfter {
		void after(MethodHookParam param) throws Throwable;
	}

	public static class MethodHook extends XC_MethodHook {
		private HookBefore hookBefore;
		private HookAfter hookAfter;

		public MethodHook(HookBefore hookBefore) {
			this.hookBefore = hookBefore;
		}

		public MethodHook(HookAfter hookAfter) {
			this.hookAfter = hookAfter;
		}

		@Override protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
			if (hookBefore != null) {
				try {
					hookBefore.before(param);
				} catch (Throwable t) {
					Log.e(TAG, t.getMessage(), t);
					XposedBridge.log(t);
				}
			}
		}

		@Override protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			if (hookAfter != null) {
				try {
					hookAfter.after(param);
				} catch (Throwable t) {
					Log.e(TAG, t.getMessage(), t);
					XposedBridge.log(t);
				}
			}
		}
	}
}
