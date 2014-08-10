package com.learn.support;


import com.learn.foursquareexplorer.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;


public class ConnectionCheckSingleton {

	private static ConnectionCheckSingleton instance;

	private ConnectionCheckSingleton() {

	}

	public static synchronized ConnectionCheckSingleton getInstance() {
		if (instance == null)
			instance = new ConnectionCheckSingleton();

		return instance;
	}

	public boolean isNetworkAvailable(Context context) {
		boolean available = false;
		/** Getting the system's connectivity service */
		ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		/** Getting active network interface to get the network's status */
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isAvailable())
			available = true;

		/** Returning the status of the network */
		return available;
	}

	public void networkDialog(final Context context) {
		if (!ConnectionCheckSingleton.getInstance().isNetworkAvailable(context)) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					context);

			// setup a dialog window
			alertDialogBuilder
					.setMessage("Network is not avaiable")
					.setTitle("Network issue")
					.setIcon(R.drawable.ic_warning)
					.setCancelable(false)
					.setPositiveButton("Enable",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent settingsIntent = new Intent(
											Settings.ACTION_SETTINGS);
									context.startActivity(settingsIntent);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});

			// create an alert dialog
			AlertDialog alert = alertDialogBuilder.create();
			alert.show();
		}
	}

}
