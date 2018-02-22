package com.ljmu.andre.artdeoptimiser.Utils;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class AnimationUtils {
	public static void scaleUp(View v) {
		ScaleAnimation animation = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(400);
		v.startAnimation(animation);
	}

	public static void expand(View v) {
		expand(v, 1);
	}

	public static void expand(View v, float durationModifier) {
		expand(v, false, durationModifier);
	}

	public static void expand(View v, boolean horizontal, float durationModifier) {
		v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		int targetSize = !horizontal ? v.getMeasuredHeight() : v.getMeasuredWidth();

		// Older versions of android (pre API 21) cancel animations for views with a height of 0.
		if (!horizontal)
			v.getLayoutParams().height = 1;
		else
			v.getLayoutParams().width = 1;

		v.setVisibility(View.VISIBLE);
		Animation a = new Animation() {
			@Override
			public boolean willChangeBounds() {
				return true;
			}

			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (!horizontal) {
					v.getLayoutParams().height = interpolatedTime == 1
							? LayoutParams.WRAP_CONTENT
							: (int) (targetSize * interpolatedTime);
				} else
					v.getLayoutParams().width = interpolatedTime == 1
							? LayoutParams.WRAP_CONTENT
							: (int) (targetSize * interpolatedTime);
				v.requestLayout();
			}


		};

		a.setDuration((int) (((float) targetSize / v.getContext().getResources().getDisplayMetrics().density) * durationModifier));
		v.startAnimation(a);
	}

	public static void collapse(View v) {
		collapse(v, false, 1);
	}

	public static void collapse(View v, boolean horizontal, float durationModifier) {
		int initialSize = !horizontal ? v.getMeasuredHeight() : v.getMeasuredWidth();

		Animation a = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 1) {
					v.setVisibility(View.GONE);
				} else {
					if (!horizontal)
						v.getLayoutParams().height = initialSize - (int) (initialSize * interpolatedTime);
					else
						v.getLayoutParams().width = initialSize - (int) (initialSize * interpolatedTime);

					v.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		a.setDuration((int) (((float) initialSize / v.getContext().getResources().getDisplayMetrics().density) * durationModifier));
		v.startAnimation(a);
	}

	public static void collapse(View v, float durationModifier) {
		collapse(v, false, durationModifier);
	}
}
