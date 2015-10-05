package etapps.library.instagram.oauth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * @author jars
 */
public class InstagramOAuthActivity extends AppCompatActivity {

    private static final String TAG = InstagramOAuthActivity.class.getName();

    private static final String CLIENT_ID = "<client id here>";
    private static final String REDIRECT_URI = "http://bwuit.com/auth/instagram/callback";
    private static final String FAILURE_URL = "http://bwuit.com/auth/failure";
    private static final String AUTH_URI = "https://instagram.com/oauth/authorize/?client_id="
            + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=token";

    private ProgressDialog linking;

    /** */
    public InstagramOAuthActivity() {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        WebView wb = new WebView(this);
        setContentView(wb);
        setClient(this, wb);

        wb.loadUrl(AUTH_URI);
    }
//
//    private AsyncTask<String, Void, APIResponse> getConnectInstagramTask() {
//        final Activity self = this;
//        return new AsyncTask<String, Void, APIResponse>() {
//
//            @Override
//            protected void onPreExecute() {
//                String message = String.format(self.getString(R.string.loading), "instagram");
//                linking = ProgressDialog.show(self, "Bwuit", message);
//            }
//
//            @Override
//            protected APIResponse doInBackground(String... params) {
//                String accessToken = params[0];
//
//                // get user info
//                try {
//                    URL url = new URL("https://api.instagram.com/v1/users/self?access_token="
//                            + accessToken);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//
//                    conn.setRequestMethod("GET");
//                    conn.setDoInput(true);
//                    conn.connect();
//
//                    int response = conn.getResponseCode();
//                    String message = conn.getResponseMessage();
//                    Log.d(TAG, "The API response is: " + response + ", message: " + message);
//                    if (response == HttpStatus.SC_OK) {
//                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//                        StringBuilder sb = new StringBuilder();
//                        String line;
//                        while ((line = br.readLine()) != null) {
//                            sb.append(line + "\n");
//                        }
//                        br.close();
//                        JSONObject data = new JSONObject(sb.toString());
//                        // update user
//                        String userName = data.getJSONObject("data").getString("username");
//                        Log.d(TAG, "Instagram username: " + userName);
//
//                        User u = User.load();
//                        API api = new API(self);
//                        APIResponse apiResponse = null;
//                        if (u.exist()) {
//                            // register instagram
//                            apiResponse = api.post(
//                                    "users/" + u.id + "/add/instagram",
//                                    new BasicNameValuePair("token", accessToken),
//                                    new BasicNameValuePair("bwuit_token", u.bwuitToken)
//                            );
//                        } else {
//                            // add instagram
//                            apiResponse = api.post(
//                                    "users/register/instagram",
//                                    new BasicNameValuePair("token", accessToken)
//                            );
//                            if (apiResponse.isOk()) {
//                                u.updateFromServer(apiResponse.content);
//                                u.sendDeviceTokenToBackendAndSaveIt(self);
//                            }
//                        }
//                        if (apiResponse.isOk()) {
//                            // save accessToken client
//                            try {
//                                u.instagramUserName = userName;
//                                u.instagramAccessToken = accessToken;
//                                u.searchInInstagram = "1";
//                                u.save();
//                            } catch (Exception e) {
//                                Utils.notify(e);
//                            }
//                        }
//                        return apiResponse;
//                    } else {
//                        // TODO: Error, pending to do something
//                        return new APIResponse(500, null);
//                    }
//
//                } catch (Exception e) {
//                    // TODO: Error, pending to do something
//                    Log.e(TAG, e.getMessage());
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(APIResponse result) {
//                super.onPostExecute(result);
//                linking.dismiss();
//                APIResponse response = (APIResponse) result;
//                if (response != null && !response.isOk()) {
//                    String m = String.format(self.getString(
//                            R.string.error_linking_account), "instagram");
//                    Utils.errorAlert(self, m + ", error: "
//                            + response.responseCode + " - " + response.content, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            finish();
//                        }
//
//                    });
//                } else {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(self);
//                    String msg = String.format(self.getString(
//                            R.string.message_we_will_organize_info_of), "Instagram");
//                    builder.setMessage(msg)
//                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    finish();
//                                }
//                            });
//                    builder.create().show();
//                }
//            }
//
//        };
//    }

    private void setClient(final Activity act, WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "URL : " + url);
                if (url.startsWith(REDIRECT_URI)) {
                    if (url.contains("access_token")) {
                        String accessToken = url.split("#access_token=")[1];
                        Log.d(TAG, "Instagram TOKEN: " + accessToken);
                    } else if (url.contains("error_reason")) {
                        String error = url.contains("user_denied") ? "User denied access" : "Authentication failed";
                        Log.e(TAG, "error: " + error);
                        finish();
                    }
                    return true;
                } else if (url.startsWith(FAILURE_URL)) {
                    // TODO: Alert unknown error
                    finish();
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}