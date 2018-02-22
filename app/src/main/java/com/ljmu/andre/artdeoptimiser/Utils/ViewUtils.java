package com.ljmu.andre.artdeoptimiser.Utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IdRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ljmu.andre.artdeoptimiser.R;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class ViewUtils {
	public static <T extends View> T getView(Activity activity, @IdRes int id) {
		return activity.findViewById(id);
	}

	public static <T extends View> T getView(View view, @IdRes int id) {
		return view.findViewById(id);
	}

	public static void setText(View parentOfView, @IdRes int resourceId, String text) {
		TextView tv = getView(parentOfView, resourceId);
		tv.setText(text);
	}

	public static void errorButtonStyle(View parentOfButton, @IdRes int resourceId) {
		errorButtonStyle(getView(parentOfButton, resourceId));
	}

	public static void errorButtonStyle(Button button) {
		Context context = button.getContext();
		button.setBackgroundResource(R.drawable.error_button);
		button.setTextColor(context.getColor(R.color.errorLight));
	}

	public static void successButtonStyle(View parentOfButton, @IdRes int resourceId) {
		successButtonStyle(getView(parentOfButton, resourceId));
	}

	public static void successButtonStyle(Button button) {
		Context context = button.getContext();
		button.setBackgroundResource(R.drawable.success_button);
		button.setTextColor(context.getColor(R.color.successLight));
	}

	public static class TextChangedWatcher implements TextWatcher {
		private OnTextChanged textChangeListener;

		public TextChangedWatcher(OnTextChanged textChangeListener) {
			this.textChangeListener = textChangeListener;
		}

		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
			textChangeListener.textChanged(s.toString());
		}

		@Override public void afterTextChanged(Editable s) {
		}

		public interface OnTextChanged {
			void textChanged(String text);
		}
	}
}
