/*
 * Copyright (C) 2013 Simon Marquis (http://www.simon-marquis.fr)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.simon.marquis.preferencesmanager.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.spazedog.lib.rootfw.container.FileStat;

import fr.simon.marquis.preferencesmanager.R;
import fr.simon.marquis.preferencesmanager.model.AppEntry;
import fr.simon.marquis.preferencesmanager.ui.App;

public class Utils {

	public static final String TAG = "PreferencesManager";
	private static final String FAVORITES_KEY = "FAVORITES_KEY";
	private static ArrayList<AppEntry> applications;
	private static HashSet<String> favorites;

	public static AlertDialog displayNoRoot(Context ctx) {
		return new Builder(ctx)
				.setIcon(R.drawable.ic_launcher)
				.setTitle(R.string.no_root_title)
				.setMessage(R.string.no_root_message)
				.setPositiveButton(R.string.no_root_button,
						new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						}).create();
	}

	public static ArrayList<AppEntry> getApplications(Context ctx) {
		if (applications != null) {
			return applications;
		}

		List<ApplicationInfo> appsInfo = ctx.getPackageManager()
				.getInstalledApplications(
						PackageManager.GET_UNINSTALLED_PACKAGES
								| PackageManager.GET_DISABLED_COMPONENTS);
		if (appsInfo == null)
			appsInfo = new ArrayList<ApplicationInfo>();

		List<AppEntry> entries = new ArrayList<AppEntry>(appsInfo.size());
		for (int i = 0; i < appsInfo.size(); i++) {
			entries.add(new AppEntry(appsInfo.get(i), ctx));
		}

		Collections.sort(entries, new MyComparator());
		applications = new ArrayList<AppEntry>(entries);
		return applications;
	}
	
	public static void verifyFavorites(Context ctx){
		for (AppEntry a : applications) {
			boolean b = isFavorite(a.getApplicationInfo().packageName, ctx);
			if(b){
				Log.e("",a.getApplicationInfo().packageName + " " + b);
			}
			a.setFavorite(b);
		}
	}

	public static void setFavorite(String packageName, boolean favorite,
			Context ctx) {
		Log.e(TAG, "setFavorite " + favorite + " " + packageName);

		if (favorites == null) {
			initFavorites(ctx);
		}

		if (favorite) {
			favorites.add(packageName);
		} else {
			favorites.remove(packageName);
		}

		Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();

		if (favorites.size() == 0) {
			ed.remove(FAVORITES_KEY);
		} else {
			JSONArray array = new JSONArray(favorites);
			ed.putString(FAVORITES_KEY, array.toString());
		}

		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
			ed.apply();
		} else {
			ed.commit();
		}

		updateApplicationInfo(packageName, favorite);
	}

	private static void updateApplicationInfo(String packageName,
			boolean favorite) {
		for (AppEntry a : applications) {
			if (a.getApplicationInfo().packageName.equals(packageName)) {
				a.setFavorite(favorite);
				return;
			}
		}
	}

	public static boolean isFavorite(String packageName, Context ctx) {
		if (favorites == null) {
			initFavorites(ctx);
		}
		return favorites.contains(packageName);
	}

	private static void initFavorites(Context ctx) {
		if (favorites == null) {
			favorites = new HashSet<String>();

			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);

			if (sp.contains(FAVORITES_KEY)) {
				try {
					JSONArray array = new JSONArray(sp.getString(FAVORITES_KEY,
							"[]"));
					for (int i = 0; i < array.length(); i++) {
						favorites.add(array.optString(i));
					}
				} catch (JSONException e) {
					Log.e(TAG, "error parsing JSON", e);
				}
				Log.e(TAG, "Favorites are : " + favorites.toString());
			}
		}
	}

	public static void debugFile(String file){
		FileStat fileStat = App.getRoot().file.stat(file);
		Log.d(Utils.TAG,
				file + " [ `" + fileStat.access() + "` , `" + fileStat.link()
						+ "` , `" + fileStat.mm() + "` , `" + fileStat.name()
						+ "` , `" + fileStat.permission() + "` , `"
						+ fileStat.type() + "` , `" + fileStat.group()
						+ "` , `" + fileStat.size() + "` , `" + fileStat.user()
						+ "` ]");
	}
	
}