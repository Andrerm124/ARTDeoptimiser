package com.ljmu.andre.artdeoptimiser.Utils;

import android.util.Log;

import com.google.gson.Gson;
import com.ljmu.andre.artdeoptimiser.PackageData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ljmu.andre.artdeoptimiser.MainActivity.TAG;
import static com.ljmu.andre.artdeoptimiser.Utils.MiscUtils.getExternalDir;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class PackageUtils {
	public static final String DEOP_EXT = ".fdeop";
	public static final String DEBUG_EXT = ".fdebug";

	public static File getDeoptimisationFolder() {
		File deopFolder = new File(
				getExternalDir(),
				"ARTDeoptimisation"
		);

		Log.d(TAG, "DeopFolder: " + deopFolder);

		if (!deopFolder.exists() && !deopFolder.mkdir())
			throw new IllegalStateException("Deoptimisation folder couldn't be found or created");

		deopFolder.setReadable(true, false);
		return deopFolder;
	}

	public static Result<Boolean, String> updatePackageDataFiles(PackageData packageData) {
		File deopFolder = getDeoptimisationFolder();
		File deopFile = new File(deopFolder, packageData.getPackageName() + DEOP_EXT);
		File debugFile = new File(deopFolder, packageData.getPackageName() + DEBUG_EXT);

		Result<Boolean, String> result = new Result<>(true, "Success");

		if (packageData.isForceDeop()) {
			try {
				if (!deopFile.exists() && !deopFile.createNewFile()) {
					result.setKey(false)
							.setValue("Failed to create deoptimisation file marker");
				}
			} catch (IOException e) {
				result.setKey(false)
						.setValue("Failed to create deoptimisation file marker");
			}
		} else {
			if(deopFile.exists() && !deopFile.delete()) {
				result.setKey(false)
						.setValue("Failed to delete deoptimation file marker");
			}
		}

		if (packageData.isForceDebuggable()) {
			try {
				if (!debugFile.exists() && !debugFile.createNewFile()) {
					String errorMessage = "Failed to create debug file marker";

					result.setKey(false)
							.setValue(
									result.getValue() == null ? errorMessage
											: result.getValue() + "\n" + errorMessage
							);
				}
			} catch (IOException e) {
				String errorMessage = "Failed to create debug file marker";

				result.setKey(false)
						.setValue(
								result.getValue() == null ? errorMessage
										: result.getValue() + "\n" + errorMessage
						);
			}
		} else {
			if(debugFile.exists() && !debugFile.delete()) {
				String errorMessage = "Failed to delete debug file marker";

				result.setKey(false)
						.setValue(
								result.getValue() == null ? errorMessage
										: result.getValue() + "\n" + errorMessage
						);
			}
		}

		return result;
	}

	public static Collection<PackageData> readSavedPackageData() {
		Log.d(TAG, "Reading saved package data");

		File deopFolder = getDeoptimisationFolder();

		File[] files = deopFolder.listFiles(file -> {
			if (file.isDirectory())
				return false;

			String filename = file.getName();

			return filename.endsWith(DEOP_EXT) || filename.endsWith(DEBUG_EXT);
		});

		if (files == null)
			return Collections.emptyList();

		Map<String, PackageData> packageDataMap = new HashMap<>(16);

		for (File file : files) {
			String filename = file.getName();
			String packageName;
			int fileType;

			if (filename.endsWith(DEOP_EXT)) {
				fileType = 1;
				packageName = filename.replace(DEOP_EXT, "");
			} else if (filename.endsWith(DEBUG_EXT)) {
				fileType = 2;
				packageName = filename.replace(DEBUG_EXT, "");
			} else
				continue;

			PackageData packageData = packageDataMap.get(packageName);

			if (packageData == null) {
				packageData = new PackageData(packageName);
				packageDataMap.put(packageName, packageData);
			}

			if (fileType == 1) {
				packageData.setForceDeop(true);
			} else {
				packageData.setForceDebuggable(true);
			}
		}

		return packageDataMap.values();
	}
}
