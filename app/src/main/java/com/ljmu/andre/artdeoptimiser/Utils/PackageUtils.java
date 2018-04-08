package com.ljmu.andre.artdeoptimiser.Utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.util.Log;

import com.jaredrummler.android.shell.CommandResult;
import com.jaredrummler.android.shell.Shell;
import com.ljmu.andre.artdeoptimiser.PackageData;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class PackageUtils {
	public static final String PROP_DEOPTIMISE = "deoptimise";
	public static final String PROP_DEBUG = "debug";
	private static final String PROP_HEADER = "persist.artdeop.";
	private static final String PROP_INVALIDATE = "invalidate";
	private static final int PROP_CHUNK_SIZE = 90;

	public static List<PackageData> getStoredPackageData() {
		List<PackageData> packageDataList = new ArrayList<>();

		Set<String> deopPackageSet = getDeopOrDebugProps(PROP_DEOPTIMISE);
		Set<String> debugPackageSet = getDeopOrDebugProps(PROP_DEBUG);

		for (String deopPackage : deopPackageSet) {
			boolean forceDebuggable = debugPackageSet.contains(deopPackage);

			packageDataList.add(
					new PackageData(deopPackage)
							.setForceDeop(true)
							.setForceDebuggable(forceDebuggable)
			);

			if (forceDebuggable) {
				debugPackageSet.remove(deopPackage);
			}
		}

		for (String debugPackage : debugPackageSet) {
			packageDataList.add(
					new PackageData(debugPackage)
							.setForceDebuggable(true)
							.setForceDeop(false)
			);
		}

		return packageDataList;
	}

	@NonNull public static Set<String> getDeopOrDebugProps(@PropType String propType) {
		Set<String> packageSet = new HashSet<>();

		StringBuilder propertyBuilder = new StringBuilder("");

		int overflowIndex = 0;
		String propOverflow;
		while (true) {
			String propertyName = PROP_HEADER + propType + "." + (++overflowIndex);

			if ((propOverflow = PropUtils.getProperty(propertyName)).isEmpty()) {
				break;
			}

			Log.d(TAG, "Overflow: " + propOverflow);

			propertyBuilder.append(propOverflow);
		}

		String packagesProperty = propertyBuilder.toString();

		if (packagesProperty.isEmpty()) {
			Log.e(TAG, "Couldn't read " + propType + " app list");
			return packageSet;
		}

		String[] packageArray = packagesProperty.split(",");

		for (String deopPackage : packageArray)
			packageSet.add(deopPackage.trim());

		Log.d(TAG, "DeopList: " + packageSet);

		return packageSet;
	}

	public static boolean shouldInvalidateSets() {
		return PropUtils.getProperty(PROP_HEADER + "." + PROP_INVALIDATE).equals("1");
	}

//	public static Result<Boolean, String> setDeopOrDebugProps(@PropType String propType, Collection<PackageData> packageDataCollection) {
//
//	}

	public static Result<Boolean, String> refreshPropertiesList(Collection<PackageData> packageDataCollection) {
		StringBuilder deopListProperty = null;
		StringBuilder debugListProperty = null;

		for (PackageData packageData : packageDataCollection) {
			if (packageData.isForceDeop()) {
				if (deopListProperty == null)
					deopListProperty = new StringBuilder(packageData.getPackageName());
				else
					deopListProperty.append(",").append(packageData.getPackageName());
			}

			if (packageData.isForceDebuggable()) {
				if (debugListProperty == null)
					debugListProperty = new StringBuilder(packageData.getPackageName());
				else
					debugListProperty.append(",").append(packageData.getPackageName());
			}
		}

		Log.d(TAG, "DeopProperty: " + deopListProperty);
		Log.d(TAG, "DebugProperty: " + debugListProperty);

		setDeopOrDebugProps(PROP_DEOPTIMISE, deopListProperty != null ? deopListProperty.toString() : null);
		setDeopOrDebugProps(PROP_DEBUG, debugListProperty != null ? debugListProperty.toString() : null);

		return null;
	}

	private static void setDeopOrDebugProps(@PropType String propType, @Nullable String properties) {
		Log.d(TAG, "Setting property for: " + propType + " | " + properties);
		List<String> commands = new ArrayList<>();

		int propertyOverflowIndex = 1;

		if (properties != null) {
			while (true) {
				String propertyName = PROP_HEADER + propType + "." + propertyOverflowIndex;
				if (properties.length() <= 90) {
					commands.add("setprop " + propertyName + " \"" + properties + "\"");
					Log.d(TAG, "AddCommand: " + "setprop " + propertyName + " \"" + properties + "\"");
					propertyOverflowIndex++;
					break;
				}

				int propertyChunkStartIndex = (propertyOverflowIndex - 1) * PROP_CHUNK_SIZE;
				int propertyChunkEndIndex = Math.min(properties.length(), propertyChunkStartIndex + PROP_CHUNK_SIZE);

				Log.d(TAG, "SetProp indices: " + propertyChunkStartIndex + " | " + propertyChunkEndIndex + " | " + properties.length());
				if (propertyChunkStartIndex >= properties.length())
					break;

				String propertyChunk = properties.substring(propertyChunkStartIndex, propertyChunkEndIndex);
				Log.d(TAG, "Property Chunk: " + propertyChunk);

				commands.add("setprop " + propertyName + " \"" + propertyChunk + "\"");
				Log.d(TAG, "AddCommand: " + "setprop " + propertyName + " \"" + propertyChunk + "\"");

				propertyOverflowIndex++;
			}
		}

		Log.d(TAG, "Finished Overflow Index: " + propertyOverflowIndex);
		// Clear remaining property files ============================================
		int overflowIndex = propertyOverflowIndex;
		while (true) {
			String propertyName = PROP_HEADER + propType + "." + (overflowIndex++);
			Log.d(TAG, "Should clear file: " + propertyName);
			if (PropUtils.getProperty(propertyName).isEmpty()) {
				break;
			}

			Log.d(TAG, "SetCommand: " + "setprop " + propertyName + " \"\"");
			commands.add("setprop " + propertyName + " \"\"");
		}

		if (commands.isEmpty()) {
			Log.d(TAG, "No commands");
			return;
		}

		commands.add("setprop " + PROP_HEADER + "." + PROP_INVALIDATE + " \"1\"");

		CommandResult commandResult = Shell.SU.run(commands.toArray(new String[0]));

		if (commandResult.isSuccessful())
			Log.d(TAG, "Command Success");
		else
			Log.e(TAG, "Command Error: " + commandResult.getStderr());
	}

	@Retention(RetentionPolicy.SOURCE)
	@StringDef({PROP_DEOPTIMISE, PROP_DEBUG}) @interface PropType {
	}
}
