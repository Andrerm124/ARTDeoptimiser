package com.ljmu.andre.artdeoptimiser;

import android.Manifest.permission;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.Theme;
import com.afollestad.materialdialogs.internal.MDButton;
import com.ljmu.andre.artdeoptimiser.Utils.PackageUtils;
import com.ljmu.andre.artdeoptimiser.Utils.Provider;
import com.ljmu.andre.artdeoptimiser.Utils.Result;
import com.ljmu.andre.artdeoptimiser.Utils.ViewUtils;
import com.ljmu.andre.artdeoptimiser.Utils.ViewUtils.TextChangedWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.ljmu.andre.artdeoptimiser.Utils.MiscUtils.timeOffset;
import static com.ljmu.andre.artdeoptimiser.Utils.PrefKeys.FIRST_TIME;
import static com.ljmu.andre.artdeoptimiser.Utils.PrefKeys.LAST_GREETED;
import static com.ljmu.andre.artdeoptimiser.Utils.PrefKeys.SHOW_SYS_APPS;
import static com.ljmu.andre.artdeoptimiser.Utils.PrefKeys.SHOW_TOGGLE_HINT;

public class MainActivity extends AppCompatActivity {
	public static final String TAG = "ARTDeoptimisation";
	private static final String[] PERMISSION_REQUESTS = {
			permission.READ_EXTERNAL_STORAGE,
			permission.WRITE_EXTERNAL_STORAGE
	};

	// ===========================================================================

	private final ArrayList<PackageData> packageDataList = new ArrayList<>(16);

	// ===========================================================================

	@BindView(R.id.toolbar) Toolbar toolbar;
	@BindView(R.id.swipe_refresh_packages) SwipeRefreshLayout swipeRefreshPackages;
	@BindView(R.id.list_packages) ListView listPackages;
	Unbinder unbinder;

	// ===========================================================================

	private SharedPreferences preferences;
	private PackageListAdapter packageListAdapter;

	// ===========================================================================

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		unbinder = ButterKnife.bind(this);
		preferences = getPreferences(MODE_PRIVATE);

		setSupportActionBar(toolbar);

		if (!checkAndroidVersion()) {
			return;
		}

		if (handlePermissions())
			initialiseApplication();
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		unbinder.unbind();
	}

	private boolean checkAndroidVersion() {
		if (VERSION.SDK_INT < VERSION_CODES.N) {
			new MaterialDialog.Builder(this)
					.title(R.string.old_android_title)
					.content(R.string.old_android_message)
					.iconRes(R.drawable.cancel_red)
					.positiveText("Understood")
					.onPositive((dialog, which) -> {
						dialog.dismiss();
						finish();
					})
					.show();

			return false;
		}

		return true;
	}

	/**
	 * ===========================================================================
	 * Check for necessary Permissions -> Perform request on missing perms
	 * ===========================================================================
	 */
	private boolean handlePermissions() {
		for (String permission : PERMISSION_REQUESTS) {
			if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

				// Ask the user if they wish to resolve missing permissions
				new MaterialDialog.Builder(this)
						.title(R.string.permission_request_title)
						.content(R.string.permission_request_msg)
						.iconRes(R.drawable.cancel_red)
						.theme(Theme.DARK)
						.positiveText("Continue")
						.onPositive((dialog, which) -> ActivityCompat.requestPermissions(
								MainActivity.this,
								PERMISSION_REQUESTS,
								1
						))
						.show();

				return false;
			}
		}

		return true;
	}

	private void initialiseApplication() {
		packageListAdapter = new PackageListAdapter(this, packageDataList, packageData -> packageDataUpdated());

		listPackages.setAdapter(packageListAdapter);

		generatePackageData(this::packageDataGenerated);
		swipeRefreshPackages.setOnRefreshListener(() -> generatePackageData(this::packageDataGenerated));

		initGreeting();
	}

	private void generatePackageData(Provider<Result<String, List<PackageData>>> errorCallback) {
		swipeRefreshPackages.setRefreshing(true);

		new Thread(() -> {
			ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

			if (activityManager == null) {
				errorCallback.call(new Result<>(getString(R.string.generate_data_failed), null));
				return;
			}

			List<PackageData> packageDataList = new ArrayList<>(16);
			packageDataList.addAll(PackageUtils.getStoredPackageData());
			Log.d(TAG, "Found " + packageDataList.size() + " saved packages");
			populateWithInstalledPackages(packageDataList);

			checkAndInsertSnapTools(packageDataList);

			Collections.sort(packageDataList);

			errorCallback.call(new Result<>("Success", packageDataList));
		}).start();
	}

	private void initGreeting() {
		long lastGreetedTime = preferences.getLong(LAST_GREETED, 0L);
		long lastGreetedOffset = timeOffset(lastGreetedTime);

		boolean showGreeting = preferences.getBoolean(FIRST_TIME, true) ||
				lastGreetedOffset > TimeUnit.DAYS.toMillis(7);

		if (showGreeting) {
			new MaterialDialog.Builder(this)
					.title(R.string.greeting_title)
					.content(R.string.greeting_message)
					.theme(Theme.DARK)
					.positiveText("Done")
					.onPositive((dialog, which) -> {
						dialog.dismiss();

						preferences.edit()
								.putBoolean(FIRST_TIME, false)
								.putLong(LAST_GREETED, System.currentTimeMillis())
								.apply();
					})
					.show();
		}
	}

	private void populateWithInstalledPackages(List<PackageData> packageData) {
		boolean showSystemApps = preferences.getBoolean(SHOW_SYS_APPS, false);
		PackageManager pm = getPackageManager();
		List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

		for (ApplicationInfo applicationInfo : packages) {
			if (!showSystemApps && (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
				continue;
			}

			String appName = (String) pm.getApplicationLabel(applicationInfo);
			String packageName = applicationInfo.packageName;

			PackageData installedPackageData = new PackageData(appName, packageName);

			if (packageData.contains(installedPackageData)) {
				Log.w(TAG, "Already contains package: " + installedPackageData.getPackageName());

				PackageData existingData = packageData.get(packageData.indexOf(installedPackageData));

				if (existingData != null) {
					existingData.setAppName(appName);
					Log.d(TAG, "Updating app name: " + appName);
				}

				continue;
			}

			packageData.add(installedPackageData);
		}
	}

	/**
	 * ===========================================================================
	 * A tiny bit of marketing... Allow me just one
	 * ===========================================================================
	 */
	private void checkAndInsertSnapTools(List<PackageData> packageDataList) {
		boolean containsST = false;

		for (PackageData packageData : packageDataList) {
			if (packageData.getPackageName().endsWith("com.ljmu.andre.snaptools")) {
				containsST = true;
				break;
			}
		}

		if (!containsST) {
			packageDataList.add(
					new PackageData("SnapTools", "com.ljmu.andre.snaptools")
							.setSnapToolsAdvert(true)
			);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			if (item == null) {
				continue;
			}

			switch (item.getItemId()) {
				case R.id.menu_show_sys_apps:
					item.setChecked(preferences.getBoolean(SHOW_SYS_APPS, false));
					break;
			}
		}
		return true;
	}

	@Override public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case R.id.menu_show_sys_apps:
				boolean newState = !item.isChecked();

				preferences.edit()
						.putBoolean(SHOW_SYS_APPS, newState)
						.apply();

				item.setChecked(newState);

				generatePackageData(this::packageDataGenerated);
				return true;
			case R.id.menu_add_manual:
				showManualPackageDialog();
				return true;
			case R.id.menu_show_legal:
				Intent legalIntent = new Intent(this, LegalActivity.class);
				startActivity(legalIntent);
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * ===========================================================================
	 * Show a dialog to let the user create a manual package entry
	 * ===========================================================================
	 */
	private void showManualPackageDialog() {
		MaterialDialog materialDialog = new MaterialDialog.Builder(this)
				.title("Manual Package")
				.customView(R.layout.dialog_manual_package, true)
				.positiveText("Apply")
				.negativeText("Cancel")
				.onPositive((dialog, which) -> {
					View customView = dialog.getCustomView();
					String packageName = ViewUtils.<EditText>getView(customView, R.id.edit_package_name)
							.getText().toString();
					boolean forceDeop = ViewUtils.<CheckBox>getView(customView, R.id.check_deop)
							.isChecked();
					boolean forceDebug = ViewUtils.<CheckBox>getView(customView, R.id.check_debug)
							.isChecked();

					if (!forceDeop && !forceDebug) {
						Toast.makeText(
								this,
								"Manual package must have either force deoptimisation or debug enabled",
								Toast.LENGTH_LONG
						).show();

						return;
					}

					PackageData manualPackageData = new PackageData(packageName)
							.setForceDeop(forceDeop)
							.setForceDebuggable(forceDebug);

					// Update our files and UI ===================================================
					packageDataUpdated();
					packageDataList.add(manualPackageData);
					packageListAdapter.notifyDataSetChanged();

					Collections.sort(packageDataList);

					Toast.makeText(
							this,
							"Manual packages will be displayed under \"Unknown\" if not installed",
							Toast.LENGTH_LONG
					).show();
				})
				.show();

		final MDButton positiveButton = materialDialog.getActionButton(DialogAction.POSITIVE);
		positiveButton.setEnabled(false);

		ViewUtils.<EditText>getView(materialDialog.getCustomView(), R.id.edit_package_name)
				.addTextChangedListener(new TextChangedWatcher(text -> positiveButton.setEnabled(true)));
	}

	private void packageDataUpdated() {
		if (preferences.getBoolean(SHOW_TOGGLE_HINT, true)) {
			new Builder(this)
					.title(R.string.toggle_hint_title)
					.content(R.string.toggle_hint_message)
					.theme(Theme.DARK)
					.positiveText("Okay")
					.neutralText("Don't show again")
					.onPositive((dialog, which) -> PackageUtils.refreshPropertiesList(packageDataList))
					.onNeutral((dialog, which) -> {
						preferences.edit()
								.putBoolean(SHOW_TOGGLE_HINT, false)
								.apply();


						PackageUtils.refreshPropertiesList(packageDataList);
					})
					.show();

			return;
		}

		PackageUtils.refreshPropertiesList(packageDataList);
		sendBroadcast(new Intent("something"));
	}

	private void packageDataGenerated(Result<String, List<PackageData>> result) {
		runOnUiThread(() -> {
			swipeRefreshPackages.setRefreshing(false);

			List<PackageData> updatedPackageDataList = result.getValue();

			if (updatedPackageDataList == null) {
				Toast.makeText(this, result.getKey(), Toast.LENGTH_LONG).show();
				return;
			}

			packageDataList.clear();
			packageDataList.addAll(updatedPackageDataList);
			packageListAdapter.notifyDataSetChanged();
		});
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		for (int grantResult : grantResults) {
			if (grantResult != PackageManager.PERMISSION_GRANTED) {
				new Builder(this)
						.title(R.string.permission_denied_title)
						.content(R.string.permission_denied_message)
						.positiveText("Finish")
						.onPositive((dialog, which) -> finish())
						.iconRes(R.drawable.cancel_red)
						.theme(Theme.DARK)
						.show();

				return;
			}
		}

		initialiseApplication();
	}
}
