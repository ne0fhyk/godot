/*************************************************************************/
/*  GodotIO.java                                                         */
/*************************************************************************/
/*                       This file is part of:                           */
/*                           GODOT ENGINE                                */
/*                      https://godotengine.org                          */
/*************************************************************************/
/* Copyright (c) 2007-2021 Juan Linietsky, Ariel Manzur.                 */
/* Copyright (c) 2014-2021 Godot Engine contributors (cf. AUTHORS.md).   */
/*                                                                       */
/* Permission is hereby granted, free of charge, to any person obtaining */
/* a copy of this software and associated documentation files (the       */
/* "Software"), to deal in the Software without restriction, including   */
/* without limitation the rights to use, copy, modify, merge, publish,   */
/* distribute, sublicense, and/or sell copies of the Software, and to    */
/* permit persons to whom the Software is furnished to do so, subject to */
/* the following conditions:                                             */
/*                                                                       */
/* The above copyright notice and this permission notice shall be        */
/* included in all copies or substantial portions of the Software.       */
/*                                                                       */
/* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       */
/* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    */
/* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*/
/* IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  */
/* CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  */
/* TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     */
/* SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                */
/*************************************************************************/

package org.godotengine.godot;

import org.godotengine.godot.input.GodotEditText;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.WindowInsets;

import java.util.Locale;

// Wrapper for native library

public class GodotIO {
	private final Activity activity;
	private final String uniqueId;
	GodotEditText edit;

	final int SCREEN_LANDSCAPE = 0;
	final int SCREEN_PORTRAIT = 1;
	final int SCREEN_REVERSE_LANDSCAPE = 2;
	final int SCREEN_REVERSE_PORTRAIT = 3;
	final int SCREEN_SENSOR_LANDSCAPE = 4;
	final int SCREEN_SENSOR_PORTRAIT = 5;
	final int SCREEN_SENSOR = 6;

	GodotIO(Activity p_activity) {
		activity = p_activity;
		String androidId = Settings.Secure.getString(activity.getContentResolver(),
				Settings.Secure.ANDROID_ID);
		if (androidId == null) {
			androidId = "";
		}

		uniqueId = androidId;
	}

	/////////////////////////
	// MISCELLANEOUS OS IO
	/////////////////////////

	public int openURI(String p_uri) {
		try {
			String path = p_uri;
			String type = "";
			if (path.startsWith("/")) {
				//absolute path to filesystem, prepend file://
				path = "file://" + path;
				if (p_uri.endsWith(".png") || p_uri.endsWith(".jpg") || p_uri.endsWith(".gif") || p_uri.endsWith(".webp")) {
					type = "image/*";
				}
			}

			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			if (!type.equals("")) {
				intent.setDataAndType(Uri.parse(path), type);
			} else {
				intent.setData(Uri.parse(path));
			}

			activity.startActivity(intent);
			return 0;
		} catch (ActivityNotFoundException e) {
			return 1;
		}
	}

	public String getCacheDir() {
		return activity.getCacheDir().getAbsolutePath();
	}

	public String getDataDir() {
		return activity.getFilesDir().getAbsolutePath();
	}

	public String getLocale() {
		return Locale.getDefault().toString();
	}

	public String getModel() {
		return Build.MODEL;
	}

	public int getScreenDPI() {
		DisplayMetrics metrics = activity.getApplicationContext().getResources().getDisplayMetrics();
		return (int)(metrics.density * 160f);
	}

	public int[] getWindowSafeArea() {
		DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
		Display display = activity.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getRealSize(size);

		int[] result = { 0, 0, size.x, size.y };
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
			WindowInsets insets = activity.getWindow().getDecorView().getRootWindowInsets();
			DisplayCutout cutout = insets.getDisplayCutout();
			if (cutout != null) {
				int insetLeft = cutout.getSafeInsetLeft();
				int insetTop = cutout.getSafeInsetTop();
				result[0] = insetLeft;
				result[1] = insetTop;
				result[2] -= insetLeft + cutout.getSafeInsetRight();
				result[3] -= insetTop + cutout.getSafeInsetBottom();
			}
		}
		return result;
	}

	public void showKeyboard(String p_existing_text, boolean p_multiline, int p_max_input_length, int p_cursor_start, int p_cursor_end) {
		if (edit != null)
			edit.showKeyboard(p_existing_text, p_multiline, p_max_input_length, p_cursor_start, p_cursor_end);

		//InputMethodManager inputMgr = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		//inputMgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}

	public void hideKeyboard() {
		if (edit != null)
			edit.hideKeyboard();
	}

	public void setScreenOrientation(int p_orientation) {
		switch (p_orientation) {
			case SCREEN_LANDSCAPE: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			} break;
			case SCREEN_PORTRAIT: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} break;
			case SCREEN_REVERSE_LANDSCAPE: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
			} break;
			case SCREEN_REVERSE_PORTRAIT: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
			} break;
			case SCREEN_SENSOR_LANDSCAPE: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
			} break;
			case SCREEN_SENSOR_PORTRAIT: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT);
			} break;
			case SCREEN_SENSOR: {
				activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
			} break;
		}
	};

	public int getScreenOrientation() {
		return activity.getRequestedOrientation();
	}

	public void setEdit(GodotEditText _edit) {
		edit = _edit;
	}

	public static final int SYSTEM_DIR_DESKTOP = 0;
	public static final int SYSTEM_DIR_DCIM = 1;
	public static final int SYSTEM_DIR_DOCUMENTS = 2;
	public static final int SYSTEM_DIR_DOWNLOADS = 3;
	public static final int SYSTEM_DIR_MOVIES = 4;
	public static final int SYSTEM_DIR_MUSIC = 5;
	public static final int SYSTEM_DIR_PICTURES = 6;
	public static final int SYSTEM_DIR_RINGTONES = 7;

	public String getSystemDir(int idx) {
		String what;
		switch (idx) {
			case SYSTEM_DIR_DESKTOP:
			default: {
				what = null; // This leads to the app specific external root directory.
			} break;

			case SYSTEM_DIR_DCIM: {
				what = Environment.DIRECTORY_DCIM;
			} break;

			case SYSTEM_DIR_DOCUMENTS: {
				what = Environment.DIRECTORY_DOCUMENTS;
			} break;

			case SYSTEM_DIR_DOWNLOADS: {
				what = Environment.DIRECTORY_DOWNLOADS;
			} break;

			case SYSTEM_DIR_MOVIES: {
				what = Environment.DIRECTORY_MOVIES;
			} break;

			case SYSTEM_DIR_MUSIC: {
				what = Environment.DIRECTORY_MUSIC;
			} break;

			case SYSTEM_DIR_PICTURES: {
				what = Environment.DIRECTORY_PICTURES;
			} break;

			case SYSTEM_DIR_RINGTONES: {
				what = Environment.DIRECTORY_RINGTONES;
			} break;
		}

		return activity.getExternalFilesDir(what).getAbsolutePath();
	}

	public String getUniqueID() {
		return uniqueId;
	}
}
