package com.qrees.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Window;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.qrees.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by abc on 25/11/2017.
 */

public class Constant {

    public static final String USER_ID = "userId";
    public static final String NAME = "name";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String PROFILE_IMAGE = "profileImage";
    public static final String CONTACT_NUMBER = "contactNumber";
    public static final String COUNTRY_CODE = "countryCode";
    public static final String LOCATION = "location";
    public static final String LATITUDE = "latitude";
    public static final String LOGITUDE = "longitude";
    public static final String DEVICETYPE = "deviceType";
    public static final String DEVICE_TOKEN = "deviceToken";
    public static final String SOCIAL_ID = "socialId";
    public static final String SOCILA_TYPE = "socialType";
    public static final String AUTHTOKEN = "authToken";
    public static final String FIREBASE_ID = "firebase_id";
    public static final String IS_NOTIFY = "Is_notify";
    public static final String STATUS = "status";
    public static final String CRD = "crd";
    public static final String UPD = "upd";

    public static Boolean isVideoUploading = true;
    public static int NETWORK_CHECK = 0;
    public static int SELECT_VIDEO_REQUEST = 0;

    public static final String URL_WITH_LOGIN = "http://dev.mindiii.com/Qrees/service/user/";
    public static final String URL_WITHOUT_LOGIN = "http://dev.mindiii.com/Qrees/service/";

    public static final int BackPressed_Exit = 2000;
    public static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 10;
    public static final int CAMERA = 7;
    public static final int GALLERY = 3;
    public static final int SPLESH_TIME = 3000;
    public static final int RequestPermissionCode = 1;
    public static final int PROGRESS_BAR_MAX = 1000;
    public static final int REQUEST_VIDEO_CAPTURE = 14;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 5;
    public static final int RECORD_AUDIO = 15;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 12;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 13;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 8;
    public static final String NETWORK_SWITCH_FILTER = "com.devglan.broadcastreceiver.NETWORK_SWITCH_FILTER";


    public static void errorHandle(VolleyError error, Activity activity) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorMessage = "Unknown error";
        if (networkResponse == null) {
            if (error.getClass().equals(TimeoutError.class)) {
                errorMessage = "Request timeout";
            } else if (error.getClass().equals(NoConnectionError.class)) {
                errorMessage = "Failed to connect server";
            }
        } else {
            String result = new String(networkResponse.data);
            try {
                JSONObject response = new JSONObject(result);

                String status = response.getString("responseCode");
                String message = response.getString("message");

                if (status.equals("300")) {
                    if (activity != null) {
                        showAlertDialog(activity, "Please Login Again", "Session Expired", "LogOut");
                    }
                }

                Log.e("Error Status", "" + status);
                Log.e("Error Message", message);

                if (networkResponse.statusCode == 404) {
                    errorMessage = "Resource not found";
                } else if (networkResponse.statusCode == 401) {
                    errorMessage = message + " Please login again";
                } else if (networkResponse.statusCode == 400) {
                    errorMessage = message + " Check your inputs";
                } else if (networkResponse.statusCode == 500) {
                    errorMessage = message + " Something is getting wrong";
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if (activity != null) {

                }
            }
        }
    }

    public static void showAlertDialog(final Activity con, String msg, String title, String ok) {

        AlertDialog.Builder builder1 = new AlertDialog.Builder(con);
        builder1.setTitle(title);
        builder1.setMessage(msg);
        builder1.setCancelable(false);
        builder1.setPositiveButton(ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        //logout(con);
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

 /*   public static void logout(final Activity con) {

        if (Utils.isNetworkAvailable(con)) {

            final Dialog pDialog = new Dialog(con);
            Constant.myDialog(con,pDialog);
            pDialog.show();

            final Session session = new Session(con);

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.GET,
                    Constant.URL_After_LOGIN + "logout", new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);


                        String message = jsonObject.getString("message");

                        if (message.equals("Invalid Auth Token")){
                            signOut(con, session);
                        }
                        String status = jsonObject.getString("status");

                        if (status.equals("SUCCESS")) {

                            signOut(con, session);


                        } else {
                            Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                    pDialog.dismiss();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Constant.errorHandle(error, con);
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(con, networkResponse + "", Toast.LENGTH_SHORT).show();
                    pDialog.dismiss();
                    error.printStackTrace();
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();

                    String auth = session.getAuthToken();
                    headers.put("authToken", auth);

                    return headers;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(con).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(con, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    public static void signOut(final Activity con, final Session session) {

        FirebaseDatabase.getInstance().getReference().child("users").child(session.getUserID()).child("firebaseToken").setValue("");
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signOut();
        NotificationManager notificationManager = (NotificationManager) con.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Intent intent = new Intent(con, WelcomeActivity.class);
        session.logout();
        Constant.USER_TYPE = "0";
        con.finish();
        con.startActivity(intent);
    }*/

    public static File getTemporalFile(Context context) {
        return new File(context.getExternalCacheDir(), "getu.jpeg");
    }

    public static void myDialog(Context context, Dialog pDialog) {
        pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pDialog.setCancelable(false);
        pDialog.setContentView(R.layout.progress_bar_layout);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //pDialog.show();
    }

    public static void videoDialog(Context context, Dialog pDialog) {
        pDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        pDialog.setCancelable(false);
        pDialog.setContentView(R.layout.progress_bar_video);
        pDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //pDialog.show();
    }



}
