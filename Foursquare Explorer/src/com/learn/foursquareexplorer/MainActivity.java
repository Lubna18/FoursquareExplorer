package com.learn.foursquareexplorer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.foursquare.android.nativeoauth.FoursquareCancelException;
import com.foursquare.android.nativeoauth.FoursquareDenyException;
import com.foursquare.android.nativeoauth.FoursquareInvalidRequestException;
import com.foursquare.android.nativeoauth.FoursquareOAuth;
import com.foursquare.android.nativeoauth.FoursquareOAuthException;
import com.foursquare.android.nativeoauth.FoursquareUnsupportedVersionException;
import com.foursquare.android.nativeoauth.model.AccessTokenResponse;
import com.foursquare.android.nativeoauth.model.AuthCodeResponse;
import com.learn.support.ConnectionCheckSingleton;

public class MainActivity extends Activity {

	private static final int REQUEST_CODE_FSQ_CONNECT = 200;
	private static final int REQUEST_CODE_FSQ_TOKEN_EXCHANGE = 201;
	private SharedPreferences sharedPref;

	/**
	 * Obtain your client id and secret from:
	 * https://foursquare.com/developers/apps
	 */
	private static final String CLIENT_ID = "F4FGBYX1HNWVRA0VDWV1T2OGNNLRLWIJ4LOYOXUBIFCAXU1P";
	private static final String CLIENT_SECRET = "G042RIPM3RQTAJQ01PKIHPKIZTY3HLMAYGVJV3ZRCHPJOH4F";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		sharedPref = getSharedPreferences("FoursquareExplorer", 0);
		if (!ConnectionCheckSingleton.getInstance().isNetworkAvailable(
				getApplicationContext())) {
			ConnectionCheckSingleton.getInstance().networkDialog(
					MainActivity.this);
		}
		accesTokenFound();
		Button btnLogin = (Button) findViewById(R.id.btn_launch_oauth);
		btnLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start the native auth flow.
				Intent intent = FoursquareOAuth.getConnectIntent(
						MainActivity.this, CLIENT_ID);

				// If the device does not have the Foursquare app installed,
				// we'd
				// get an intent back that would open the Play Store for
				// download.
				// Otherwise we start the auth flow.
				if (FoursquareOAuth.isPlayStoreIntent(intent)) {
					toastMessage(MainActivity.this,
							"To do this, you’ll need the latest version of Foursquare installed.");
					startActivity(intent);
				} else {
					startActivityForResult(intent, REQUEST_CODE_FSQ_CONNECT);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_FSQ_CONNECT:
			onCompleteConnect(resultCode, data);
			break;

		case REQUEST_CODE_FSQ_TOKEN_EXCHANGE:
			onCompleteTokenExchange(resultCode, data);
			break;

		default:
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	/**
	 * Update the UI. If we already fetched a token, we'll just show a success
	 * message.
	 */
	private void accesTokenFound() {
		String accessToken = sharedPref.getString("access_token", "empty");
		if (!accessToken.equals("empty")) {
			Log.d("TOKEN", sharedPref.getString("access_token", "empty"));
			Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
			MainActivity.this.finish();
			startActivity(mapIntent);
		}

	}

	private void onCompleteConnect(int resultCode, Intent data) {
		AuthCodeResponse codeResponse = FoursquareOAuth.getAuthCodeFromResult(
				resultCode, data);
		Exception exception = codeResponse.getException();

		if (exception == null) {
			// Success.
			String code = codeResponse.getCode();
			performTokenExchange(code);

		} else {
			if (exception instanceof FoursquareCancelException) {
				// Cancel.
				toastMessage(this, "Canceled");

			} else if (exception instanceof FoursquareDenyException) {
				// Deny.
				toastMessage(this, "Denied");

			} else if (exception instanceof FoursquareOAuthException) {
				// OAuth error.
				String errorMessage = exception.getMessage();
				String errorCode = ((FoursquareOAuthException) exception)
						.getErrorCode();
				toastMessage(this, errorMessage + " [" + errorCode + "]");

			} else if (exception instanceof FoursquareUnsupportedVersionException) {
				// Unsupported Fourquare app version on the device.
				toastError(this, exception);

			} else if (exception instanceof FoursquareInvalidRequestException) {
				// Invalid request.
				toastError(this, exception);

			} else {
				// Error.
				toastError(this, exception);
			}
		}
	}

	private void onCompleteTokenExchange(int resultCode, Intent data) {
		AccessTokenResponse tokenResponse = FoursquareOAuth.getTokenFromResult(
				resultCode, data);
		Exception exception = tokenResponse.getException();

		if (exception == null) {
			String accessToken = tokenResponse.getAccessToken();
			// Success.
			//toastMessage(this, "Access token: " + accessToken);

			sharedPref.edit().putString("access_token", accessToken).commit();
			// Refresh UI.
			accesTokenFound();

		} else {
			if (exception instanceof FoursquareOAuthException) {
				// OAuth error.
				String errorMessage = ((FoursquareOAuthException) exception)
						.getMessage();
				String errorCode = ((FoursquareOAuthException) exception)
						.getErrorCode();
				toastMessage(this, errorMessage + " [" + errorCode + "]");

			} else {
				// Other exception type.
				toastError(this, exception);
			}
		}
	}

	/**
	 * Exchange a code for an OAuth Token. Note that we do not recommend you do
	 * this in your app, rather do the exchange on your server. Added here for
	 * demo purposes.
	 * 
	 * @param code
	 *            The auth code returned from the native auth flow.
	 */
	private void performTokenExchange(String code) {
		Intent intent = FoursquareOAuth.getTokenExchangeIntent(this, CLIENT_ID,
				CLIENT_SECRET, code);
		startActivityForResult(intent, REQUEST_CODE_FSQ_TOKEN_EXCHANGE);
	}

	public static void toastMessage(Context context, String message) {
		Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	}

	public static void toastError(Context context, Throwable t) {
		Toast.makeText(context, t.getMessage(), Toast.LENGTH_SHORT).show();
	}

}
