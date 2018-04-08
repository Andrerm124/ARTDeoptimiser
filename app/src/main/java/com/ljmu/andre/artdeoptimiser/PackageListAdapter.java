package com.ljmu.andre.artdeoptimiser;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;

import com.ljmu.andre.artdeoptimiser.Utils.AnimationUtils;
import com.ljmu.andre.artdeoptimiser.Utils.MiscUtils;
import com.ljmu.andre.artdeoptimiser.Utils.Provider;
import com.ljmu.andre.artdeoptimiser.Utils.ViewUtils;

import java.util.List;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

@SuppressWarnings("DanglingJavadoc") public class PackageListAdapter extends ArrayAdapter<PackageData> {
	private Provider<PackageData> dataUpdateCallback;

	PackageListAdapter(@NonNull Context context, @NonNull List<PackageData> objects, Provider<PackageData> dataUpdateCallback) {
		super(context, 0, objects);
		this.dataUpdateCallback = dataUpdateCallback;
	}

	@NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
		PackageData packageData = getItem(position);

		if (packageData == null)
			throw new IllegalStateException("Null PackageData found... shouldn't happen!");

		if (convertView == null) {
			if (!packageData.isSnapToolsAdvert()) {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.process_list_item, parent, false
				);
			} else {
				convertView = LayoutInflater.from(getContext()).inflate(
						R.layout.snaptools_list_item, parent, false
				);
			}
		}

		if (!packageData.isSnapToolsAdvert()) {
			buildItemRow(packageData, convertView);
		} else {
			buildSnapToolsAdvertRow(packageData, convertView);
		}

		return convertView;
	}

	// Contains duplicated code... ===============================================
	// This idea was an afterthought =============================================
	// You can blame Pedro =======================================================
	private void buildSnapToolsAdvertRow(PackageData packageData, View convertView) {
		// ===========================================================================
		ViewUtils.setText(convertView, R.id.txt_app_name, packageData.getAppName());
		ViewUtils.setText(convertView, R.id.txt_package, packageData.getPackageName());
		// ===========================================================================

		/**
		 * ===========================================================================
		 * Expandable Control Panel
		 * ===========================================================================
		 */
		ViewGroup buttonContainer = ViewUtils.getView(convertView, R.id.button_container);
		ViewUtils.getView(buttonContainer, R.id.btn_check_snaptools)
				.setOnClickListener(
						v -> MiscUtils.openWebsite(convertView.getContext(), "https://snaptools.org/")
				);

		// Animated Expanding/Collapsing =============================================
		convertView.setOnClickListener(v -> {
			if (buttonContainer.getVisibility() == View.GONE)
				AnimationUtils.expand(buttonContainer, 2f);
			else
				AnimationUtils.collapse(buttonContainer, 2f);
		});
		/**===========================================================================**/
	}

	private void buildItemRow(PackageData packageData, View convertView) {
		// Click listeners for buttons and toggles ===================================
		OnClickListener forceDeopClickListener = v -> {
			packageData.setForceDeop(!packageData.isForceDeop());
			buildItemRow(packageData, convertView);
			dataUpdateCallback.call(packageData);
		};

		OnClickListener forceDebugClickListener = v -> {
			packageData.setForceDebuggable(!packageData.isForceDebuggable());
			buildItemRow(packageData, convertView);
			dataUpdateCallback.call(packageData);
		};
		// ===========================================================================

		// ===========================================================================
		ViewUtils.setText(convertView, R.id.txt_app_name, packageData.getAppName());
		ViewUtils.setText(convertView, R.id.txt_package, packageData.getPackageName());
		// ===========================================================================

		// Package Icon Assigning ====================================================
		ImageView imgDeop = ViewUtils.getView(convertView, R.id.img_deop);
		ImageView imgDebug = ViewUtils.getView(convertView, R.id.img_debug);

		imgDeop.setImageResource(
				packageData.isForceDeop()
						? R.drawable.check_green : R.drawable.cancel_red
		);
		imgDebug.setImageResource(
				packageData.isForceDebuggable()
						? R.drawable.check_green : R.drawable.cancel_red
		);

		imgDeop.setOnClickListener(forceDeopClickListener);
		imgDebug.setOnClickListener(forceDebugClickListener);
		// ===========================================================================


		/**
		 * ===========================================================================
		 * Expandable Control Panel
		 * ===========================================================================
		 */
		ViewGroup buttonContainer = ViewUtils.getView(convertView, R.id.button_container);
		buttonContainer.setVisibility(View.GONE);
		Button buttonDeop = ViewUtils.getView(buttonContainer, R.id.btn_deop);
		Button buttonDebug = ViewUtils.getView(buttonContainer, R.id.btn_debug);

		// Control Buttons ===========================================================
		assignButtonState(buttonDeop, packageData.isForceDeop());
		assignButtonState(buttonDebug, packageData.isForceDebuggable());

		buttonDeop.setOnClickListener(forceDeopClickListener);
		buttonDebug.setOnClickListener(forceDebugClickListener);
		// ===========================================================================

		// Animated Expanding/Collapsing =============================================
		convertView.setOnClickListener(v -> {
			if (buttonContainer.getVisibility() == View.GONE)
				AnimationUtils.expand(buttonContainer, 2f);
			else
				AnimationUtils.collapse(buttonContainer, 2f);
		});
		/**===========================================================================**/
	}

	private void assignButtonState(Button button, boolean state) {
		if (state) {
			button.setBackgroundResource(R.drawable.success_button);
			button.setTextColor(button.getContext().getColor(R.color.successLight));
		} else {
			button.setBackgroundResource(R.drawable.error_button);
			button.setTextColor(button.getContext().getColor(R.color.errorLight));
		}
	}
}
