package com.ljmu.andre.artdeoptimiser;

import android.support.annotation.NonNull;

import static com.ljmu.andre.artdeoptimiser.Utils.StringUtils.nullSafe;

/**
 * This class was created by Andre R M (SID: 701439)
 * It and its contents are free to use by all
 */

public class PackageData implements Comparable<PackageData> {
	private String appName;
	private String packageName;
	private boolean forceDebuggable;
	private boolean forceDeop;
	private boolean isSnapToolsAdvert;

	public PackageData(String appName, String packageName) {
		setAppName(appName);
		setPackageName(packageName);
	}

	public PackageData(String packageName) {
		setAppName(null);
		setPackageName(packageName);
	}

	@Override public int hashCode() {
		return getPackageName() != null ? getPackageName().hashCode() : 0;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PackageData)) return false;

		PackageData that = (PackageData) o;

		return getPackageName() != null ? getPackageName().equals(that.getPackageName()) : that.getPackageName() == null;
	}

	@Override public String toString() {
		return "PackageData{" +
				"appName='" + appName + '\'' +
				", packageName='" + packageName + '\'' +
				", forceDebuggable=" + forceDebuggable +
				", forceDeop=" + forceDeop +
				'}';
	}

	public String getPackageName() {
		return packageName;
	}

	public PackageData setPackageName(String packageName) {
		this.packageName = packageName;
		return this;
	}

	@Override public int compareTo(@NonNull PackageData o) {
		if (!isActive() && o.isActive()) {
			return 1;
		} else if (isActive() && !o.isActive())
			return -1;

		return getAppName().compareTo(o.getAppName());
	}

	public boolean isActive() {
		return isForceDeop() || isForceDebuggable();
	}

	public String getAppName() {
		return appName;
	}

	public PackageData setAppName(String appName) {
		this.appName = nullSafe(appName, "Unknown");
		return this;
	}

	public boolean isForceDeop() {
		return forceDeop;
	}

	public boolean isForceDebuggable() {
		return forceDebuggable;
	}

	public PackageData setForceDebuggable(boolean forceDebuggable) {
		this.forceDebuggable = forceDebuggable;
		return this;
	}

	public PackageData setForceDeop(boolean forceDeop) {
		this.forceDeop = forceDeop;
		return this;
	}

	// This isn't that big of a deal is it? ======================================
	public boolean isSnapToolsAdvert() {
		return isSnapToolsAdvert;
	}

	public PackageData setSnapToolsAdvert(boolean snapToolsAdvert) {
		isSnapToolsAdvert = snapToolsAdvert;
		return this;
	}
	// ===========================================================================
}
