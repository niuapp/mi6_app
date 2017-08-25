package com.xxx.showallapp;

import android.graphics.drawable.Drawable;

public class ApplicationData {

	private long updateTime;

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	private String packageName;

	private String appName;
	
	private Drawable icon;

	public String getPackageName() {
		return packageName;
	}

	public CharSequence getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;

	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public void setIcon(Drawable icon) {
		this.icon = icon;
	}

	public Drawable getIcon() {
		return icon;
	}

	@Override
	public String toString() {
		return "ApplicationData{" +
				"updateTime=" + updateTime +
				", packageName='" + packageName + '\'' +
				", appName='" + appName + '\'' +
				", icon=" + icon +
				'}';
	}
}
