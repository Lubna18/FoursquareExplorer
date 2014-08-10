package com.learn.support;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.learn.volley.AppController;

public class VenuesSingleton {

	private static VenuesSingleton obj = new VenuesSingleton();
	public ArrayList<VenuePojo> venueDetails = new ArrayList<VenuePojo>();
	GoogleMap googleMap;

	public static VenuesSingleton getInstance() {
		return obj;
	}

	public ArrayList<VenuePojo> parseVenuesResponse(String response) {
		ArrayList<VenuePojo> venueDetails = new ArrayList<VenuePojo>();
		try {
			JSONObject responseJSON = new JSONObject(response);
			JSONObject responseArray = responseJSON.getJSONObject("response");
			JSONArray venues = responseArray.getJSONArray("groups");
			for (int j = 0; j < venues.length(); j++) {
				JSONArray venuesArray = venues.getJSONObject(j).getJSONArray(
						"items");

				for (int i = 0; i < venuesArray.length(); i++) {

					String venueID = venuesArray.getJSONObject(i)
							.getJSONObject("venue").getString("id");
					String venueName = venuesArray.getJSONObject(i)
							.getJSONObject("venue").getString("name");
					JSONObject location = venuesArray.getJSONObject(i)
							.getJSONObject("venue").getJSONObject("location");
					Double latitude = location.getDouble("lat");
					Double longitude = location.getDouble("lng");

					ArrayList<String> address = new ArrayList<String>();
					JSONArray formattedAddress = location
							.getJSONArray("formattedAddress");
					for (int k = 0; k < formattedAddress.length(); k++) {
						address.add(formattedAddress.get(k).toString());
						address.add("\n");
					}
					JSONArray groupsArray = venuesArray.getJSONObject(i)
							.getJSONObject("venue").getJSONObject("photos")
							.getJSONArray("groups");

					String photoPrefix = "", photoSuffix = "";
					for (int q = 0; q < groupsArray.length(); q++) {
						JSONArray items = groupsArray.getJSONObject(q)
								.getJSONArray("items");

						photoPrefix = items.getJSONObject(0)
								.getString("prefix");
						photoSuffix = items.getJSONObject(0)
								.getString("suffix");
					}

					venueDetails.add(new VenuePojo(venueID, longitude,
							latitude, venueName, "", address, photoPrefix,
							photoSuffix));
				}

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		this.venueDetails = venueDetails;
		return venueDetails;
	}

	public void createVenueMarkers(ArrayList<VenuePojo> venueDetails,
			final GoogleMap googleMap, Context context) {
		this.googleMap = googleMap;
		googleMap.clear(); // to remove existing markers
		for (int i = 0; i < venueDetails.size(); i++) {
			final MarkerOptions marker = new MarkerOptions().position(
					new LatLng(venueDetails.get(i).getLatitude(), venueDetails
							.get(i).getLongitude())).title(
					venueDetails.get(i).getId());

			ImageLoader imageLoader = AppController.getInstance()
					.getImageLoader();
			imageLoader.get(venueDetails.get(i).getPhoto(),
					new ImageListener() {

						@Override
						public void onErrorResponse(VolleyError error) {
							Log.e("ERROR LOADING",
									"Image Load Error: " + error.getMessage());
						}

						@Override
						public void onResponse(ImageContainer response,
								boolean arg1) {
							if (response.getBitmap() != null) {

								marker.icon(BitmapDescriptorFactory
										.fromBitmap(response.getBitmap()));

								googleMap.addMarker(marker);
							}
						}
					});

		}
	}

	public String getCacheUrl(Context context) {
		SharedPreferences data = context.getSharedPreferences(
				"FoursquareExplorer", 0);
		return data.getString("venues_url", "empty");
	}

	public void saveURL(String url, Context context) {
		SharedPreferences data = context.getSharedPreferences(
				"FoursquareExplorer", 0);
		SharedPreferences.Editor editor = data.edit();
		editor.putString("venues_url", url);
		editor.commit();
		Log.d("savedSharedPreference", "done");
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

}
