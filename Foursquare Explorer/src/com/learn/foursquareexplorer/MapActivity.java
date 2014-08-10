package com.learn.foursquareexplorer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Cache;
import com.android.volley.Cache.Entry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.learn.support.ConnectionCheckSingleton;
import com.learn.support.VenuesSingleton;
import com.learn.volley.AppController;

public class MapActivity extends Activity implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		GoogleMap.InfoWindowAdapter, OnInfoWindowClickListener {

	private ProgressDialog pDialog;
	private GoogleMap googleMap;
	private LocationManager locationManager;
	private LocationListener locationListener;
	private LocationClient mLocationClient;
	private final String TAG = "MapActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);

		pDialog = new ProgressDialog(this);
		pDialog.setMessage("Loading...");
		pDialog.setCancelable(false);

		mLocationClient = new LocationClient(this, this, this);
		try {
			// Loading map
			initilizeMap();
			googleMap.setMyLocationEnabled(true);

		} catch (Exception e) {
			e.printStackTrace();
		}

		Cache cache = AppController.getInstance().getRequestQueue().getCache();
		String url = VenuesSingleton.getInstance().getCacheUrl(
				getApplicationContext());
		if (!url.equals("empty")) {
			Entry entry = cache.get(url);
			if (entry != null) {
				try {
					Log.d(TAG, "geting data from cache");
					String data = new String(entry.data, "UTF-8");
					VenuesSingleton.getInstance().createVenueMarkers(
							VenuesSingleton.getInstance().parseVenuesResponse(
									data), googleMap, getApplicationContext());

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
		} else {
			// Cached response doesn't exists. Make network call here
		}

		// Acquire a reference to the system Location Manager

		locationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {

			@Override
			public void onLocationChanged(Location location) {
				Log.d(TAG,"" + location.getLatitude() + "     " + location.getLongitude());
				// zoom camera to the location found
				CameraPosition cameraPosition = new CameraPosition.Builder()
						.target(new LatLng(location.getLatitude(), location
								.getLongitude())).zoom(15).build();

				googleMap.animateCamera(CameraUpdateFactory
						.newCameraPosition(cameraPosition));

				String accessToken = MapActivity.this.getSharedPreferences(
						"FoursquareExplorer", 0).getString("access_token", "empty");
				new VenuesExplore(accessToken, location.getLongitude(),
						location.getLatitude(), googleMap, getApplicationContext());
				// hideProgressDialog();

			}

			@Override
			public void onProviderDisabled(String arg0) { 
				Toast.makeText(MapActivity.this, "Please try again later.",
						Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onProviderEnabled(String arg0) { 

			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) { 

			}

		};

		// Register the listener with the Location Manager to receive location updates every 5 minutes
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, 300000, 0, locationListener);

	}

	/**
	 * function to load map. If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			} else {
				// googleMap.setOnMarkerClickListener(this);
				googleMap.setInfoWindowAdapter(this);
				googleMap.setOnInfoWindowClickListener(this);
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (!ConnectionCheckSingleton.getInstance().isNetworkAvailable(
				getApplicationContext())) {
			ConnectionCheckSingleton.getInstance().networkDialog(
					MapActivity.this);
		}
		// showProgressDialog();
		//mLocationClient.connect();
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		// mLocationClient.disconnect();
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		Toast.makeText(getApplicationContext(), "Failed to connect",
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onConnected(Bundle arg0) {

		Location location = mLocationClient.getLastLocation();
		Log.d(TAG,
				"" + location.getLatitude() + "     " + location.getLongitude());
		// zoom camera to the location found
		CameraPosition cameraPosition = new CameraPosition.Builder()
				.target(new LatLng(location.getLatitude(), location
						.getLongitude())).zoom(15).build();

		googleMap.animateCamera(CameraUpdateFactory
				.newCameraPosition(cameraPosition));

		String accessToken = MapActivity.this.getSharedPreferences(
				"FoursquareExplorer", 0).getString("access_token", "empty");
		new VenuesExplore(accessToken, location.getLongitude(),
				location.getLatitude(), googleMap, getApplicationContext());
		// hideProgressDialog();

	}

	@Override
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected. Please re-connect.",
				Toast.LENGTH_SHORT).show();
	}

	@Override
	public View getInfoContents(Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View popup = inflater.inflate(R.layout.info_window_layout, null);
		TextView venueName = (TextView) popup.findViewById(R.id.venueName);
		venueName.setText(marker.getTitle());

		String address = "";
		for (int i = 0; i < VenuesSingleton.getInstance().venueDetails.size(); i++) {
			if (marker.getTitle()
					.equals(VenuesSingleton.getInstance().venueDetails.get(i)
							.getName())) {
				ArrayList<String> formattedAddress = VenuesSingleton
						.getInstance().venueDetails.get(i).getAddress();
				for (String s : formattedAddress) {
					address += s;
				}
			}
		}
		((TextView) popup.findViewById(R.id.venueAddress)).setText(address);
		return popup;
	}

	@Override
	public View getInfoWindow(Marker marker) {
		LayoutInflater inflater = (LayoutInflater) getApplicationContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View popup = inflater.inflate(R.layout.info_window_layout, null);
		TextView venueName = (TextView) popup.findViewById(R.id.venueName);

		String address = "";
		for (int i = 0; i < VenuesSingleton.getInstance().venueDetails.size(); i++) {
			if (marker.getTitle().equals(
					VenuesSingleton.getInstance().venueDetails.get(i).getId())) {
				venueName.setText(VenuesSingleton.getInstance().venueDetails
						.get(i).getName());
				ArrayList<String> formattedAddress = VenuesSingleton
						.getInstance().venueDetails.get(i).getAddress();
				for (String s : formattedAddress) {
					address += s;
				}
			}
		}
		((TextView) popup.findViewById(R.id.venueAddress)).setText(address);
		return popup;
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		checkIN(marker.getTitle());

	}

	private void checkIN(final String venueID) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MapActivity.this);
		// setup a dialog window

		alertDialogBuilder
				.setMessage("Do you want to check in here")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								new checkin_task().execute(venueID);
							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

	}

	private class checkin_task extends AsyncTask<String, Void, String> {

		String accessToken = MapActivity.this.getSharedPreferences(
				"FoursquareExplorer", 0).getString("access_token", "empty");
		String checkin_url = "https://api.foursquare.com/v2/checkins/add?&v=20140806&m=swarm&oauth_token="
				+ accessToken;

		@Override
		protected String doInBackground(String... venueID) {
			String output = "";
			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(checkin_url);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			nameValuePairs.add(new BasicNameValuePair("venueId", venueID[0]));

			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = client.execute(httppost);

				HttpEntity httpEntity = response.getEntity();
				output = EntityUtils.toString(httpEntity);
				Log.d(TAG, output);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return output;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			String venueName = parseCheckInResponse(result);
			if (venueName != null)
				Toast.makeText(MapActivity.this,
						"Checked in successfully at " + venueName,
						Toast.LENGTH_SHORT).show();
		}

		private String parseCheckInResponse(String response) {
			String venueName = null;
			try {
				JSONObject responseJSON = new JSONObject(response)
						.getJSONObject("response");
				JSONObject checkinJSON = responseJSON.getJSONObject("checkin");
				JSONObject venueJSON = checkinJSON.getJSONObject("venue");
				venueName = venueJSON.getString("name");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return venueName;

		}
	}

	// @Override
	// public boolean onMarkerClick(Marker marker) {
	//
	// Toast.makeText(getApplicationContext(), "onMarkerClick",
	// Toast.LENGTH_SHORT).show();
	// for (int i = 0; i < CommonFunctions.getInstance().venueDetails.size();
	// i++) {
	// if (marker.getTitle()
	// .equals(CommonFunctions.getInstance().venueDetails.get(i)
	// .getName())) {
	//
	// AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
	// MainActivity.this);
	// // alertDialogBuilder.setView(promptView);
	// // setup a dialog window
	// String address = "" ;
	// ArrayList<String> formattedAddress =
	// CommonFunctions.getInstance().venueDetails.get(i).getAddress();
	// for (String s : formattedAddress)
	// {
	// address += s;
	// }
	// for (int j=0;j<formattedAddress.size();j++){
	// address.concat(formattedAddress.get(j));
	// }
	//
	// alertDialogBuilder
	// .setMessage(CommonFunctions.getInstance().venueDetails.get(i).getName()+"\nAddress:\n"+
	// address)
	// .setCancelable(false)
	// .setPositiveButton("Check in", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	//
	// }
	// })
	// .setNegativeButton("Cancel",
	// new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int id) {
	// dialog.cancel();
	// }
	// });
	//
	// // create an alert dialog
	// AlertDialog alert = alertDialogBuilder.create();
	// alert.show();
	// return true;
	// }
	// }
	//
	// return false;
	// }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_map, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.log_out:
			getSharedPreferences("FoursquareExplorer", 0).edit()
					.putString("access_token", "empty").commit();
			MapActivity.this.finish();
			Intent logOut = new Intent(MapActivity.this, MainActivity.class);
			startActivity(logOut);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showProgressDialog() {
		if (!pDialog.isShowing()) {
			Log.d(TAG, "progress dialog appear");
			pDialog.show();
		}
	}

	private void hideProgressDialog() {
		if (pDialog.isShowing()) {
			Log.d(TAG, "progress dialog hide");
			pDialog.hide();
		}
		pDialog.cancel();
	}

}
