package com.learn.foursquareexplorer;

import android.content.Context;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.learn.support.VenuesSingleton;
import com.learn.volley.AppController;

public class VenuesExplore {

	private final String TAG = "VENUES EXPLORE";
	private String venues_url = "https://api.foursquare.com/v2/venues/explore";
	private GoogleMap googleMap;
	private Context context;

	public VenuesExplore(String access_token,double longitude, double latitude, GoogleMap googleMap,Context context) {
		
		this.context=context;
		this.googleMap = googleMap;
		venues_url += "?ll="
				+ latitude
				+ ","
				+ longitude
				+ "&venuePhotos=1&radius=3000&&v=20140806&oauth_token="+access_token;
		
		if(!(VenuesSingleton.getInstance().getCacheUrl(context).equals(venues_url))){
			Log.d(TAG,"NEW LOCATION");
			makeVenuesReq();
		}else{
			Log.d(TAG,"ALREADY CACHED NO NEW REQUEST");
		}
		
	}

	private void makeVenuesReq() {

		StringRequest strReq = new StringRequest(venues_url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						Log.d(TAG, "RESPONSE");
						AppController.getInstance().getRequestQueue().getCache().remove(VenuesSingleton.getInstance().getCacheUrl(context));
						VenuesSingleton.getInstance().saveURL(venues_url,context);
						VenuesSingleton.getInstance().createVenueMarkers(VenuesSingleton.getInstance().parseVenuesResponse(response),googleMap,context);	
					}

				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "ERROR");
						VolleyLog.e(TAG, "Error: " + error.getMessage());
						// hideProgressDialog();
					}
				}) {
		};

		// Adding request to request queue
		AppController.getInstance().addToRequestQueue(strReq);
	}
	
	

}
