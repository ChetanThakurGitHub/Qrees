package com.qrees.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.qrees.R;
import com.qrees.adapter.VideosAdapter;
import com.qrees.broadcastreceiver.activity.NetworkErrorActivity;
import com.qrees.hepler.PermissionAll;
import com.qrees.model.VideosListModel;
import com.qrees.pagination.EndlessRecyclerViewScrollListener;
import com.qrees.session.Session;
import com.qrees.util.Constant;
import com.qrees.util.Utils;
import com.qrees.vollyemultipart.VolleyMultipartRequest;
import com.qrees.vollyemultipart.VolleySingleton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


import static com.qrees.util.Constant.REQUEST_ID_MULTIPLE_PERMISSIONS;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {
    private FloatingActionButton fab_addVideos,fab_profile,fab_aboutUs,fab_TandC,fab_logout;
    private FloatingActionMenu menu_green;
    private Session session;
    private boolean doubleBackToExitPressedOnce = false;
    private int limit = 20,start = 0;
    private ArrayList<VideosListModel> videosList;
    private RecyclerView recycler_view;
    private VideosAdapter videosAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();

        Constant.NETWORK_CHECK = 0;

        PermissionAll permissionAll = new PermissionAll();
        permissionAll.RequestMultiplePermission(this);

        session = new Session(this);

        videosList = new ArrayList<>();
        videosAdapter = new VideosAdapter(videosList, this);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                videosList.clear();
                start = 0;
                showFullListAPI();
            }
        });

      /*  mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                showFullListAPI();
            }
        });*/

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HomeActivity.this);
        recycler_view.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, totalItemsCount);
            }
        };
        recycler_view.addOnScrollListener(scrollListener);


        fab_profile.setOnClickListener(this);
        fab_addVideos.setOnClickListener(this);
        fab_aboutUs.setOnClickListener(this);
        fab_TandC.setOnClickListener(this);
        fab_logout.setOnClickListener(this);

        if (session.getIsLogedIn()){
            fab_logout.setLabelText("Logout");
            //fab_logout.setImageResource(R.drawable.logout);
        }else {
            fab_logout.setLabelText("Login");
            //fab_logout.setImageResource(R.drawable.login_ico);
        }

        menu_green.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(menu_green.isOpened()) {
                    menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                    //StatusBarUtil.setColorNoTranslucent(HomeActivity.this,getResources().getColor(R.color.green));
                }else {
                    menu_green.getMenuIconView().setImageResource(R.drawable.close);
                   // StatusBarUtil.setColorNoTranslucent(HomeActivity.this,getResources().getColor(R.color.colorPrimary1));
                }
                menu_green.toggle(true);
            }
        });
    }

   /* @Override
    protected void onResume() {
        super.onResume();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                videosList.clear();
                start = 0;
                showFullListAPI();
            }
        });
    }*/

    private void initView(){
        fab_addVideos = findViewById(R.id.fab_addVideos);
        fab_profile = findViewById(R.id.fab_profile);
        fab_aboutUs = findViewById(R.id.fab_aboutUs);
        fab_TandC = findViewById(R.id.fab_TandC);
        fab_logout = findViewById(R.id.fab_logout);
        menu_green = findViewById(R.id.menu_green);
        recycler_view = findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        tv_for_tittle.setText(R.string.home);
    }

    public void loadNextDataFromApi(int page, int totalItemsCount) {
        //Log.e(TAG, "loadNextDataFromApi: " + page);
        //Log.e(TAG, "loadNextDataFromApi: " + limit);
        showFullListAPI();
    } // pagination

    @Override
    public void onBackPressed() {
        if (!doubleBackToExitPressedOnce) {
            this.doubleBackToExitPressedOnce = true;
            Snackbar.make(recycler_view, R.string.for_exit, Snackbar.LENGTH_LONG).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, Constant.BackPressed_Exit);
        } else {
            super.onBackPressed();
            finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {

            case REQUEST_ID_MULTIPLE_PERMISSIONS:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean locationPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean externalStoragePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean recordPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                    if (cameraPermission && locationPermission && externalStoragePermission && recordPermission) {

                        Toast.makeText(HomeActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(HomeActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();

                    }
                }
                break;

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.fab_addVideos:
                menu_green.close(false);
                menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                if (session.getIsLogedIn()) {
                    Intent intent = new Intent(HomeActivity.this, AddVideosActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            case R.id.fab_profile:
                menu_green.close(false);
                menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                if (session.getIsLogedIn()) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.fab_aboutUs:
                menu_green.close(false);
                menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.fab_TandC:
                menu_green.close(false);
                menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                Toast.makeText(this, R.string.under_development, Toast.LENGTH_SHORT).show();
                break;
            case R.id.fab_logout:
                menu_green.close(false);
                menu_green.getMenuIconView().setImageResource(R.drawable.menu);
                if (session.getIsLogedIn()) {
                    session.logout(this);
                    fab_logout.setLabelText("Login");
                }else {
                    intent = new Intent(HomeActivity.this,LoginActivity.class);
                    startActivity(intent);
                }
                break;
        }
    }

    private void showFullListAPI() {

        if (Utils.isNetworkAvailable(this)) {

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.GET,
                    Constant.URL_WITHOUT_LOGIN + "getPostList?"+"limit="+limit+"&start="+start, new Response.Listener<NetworkResponse>() {
                @Override
                public void onResponse(NetworkResponse response) {
                    String data = new String(response.data);
                    Log.e("Response", data);

                    try {
                        JSONObject jsonObject = new JSONObject(data);

                        String status = jsonObject.getString("status");
                        String message = jsonObject.getString("message");

                        if (status.equalsIgnoreCase("SUCCESS")) {

                            mSwipeRefreshLayout.setRefreshing(false);
                            JSONArray homedata = jsonObject.getJSONArray("Homedata");
                            if (homedata != null) {
                                for (int i = 0; i < homedata.length(); i++) {
                                    JSONObject object = homedata.getJSONObject(i);
                                    VideosListModel userFullDetail = new Gson().fromJson(object.toString(), VideosListModel.class);
                                    videosList.add(userFullDetail);
                                }
                                if (start == 0) {
                                    recycler_view.setAdapter(videosAdapter);
                                }
                                start = start + 20;
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Constant.errorHandle(error, HomeActivity.this);
                    Log.i("Error", networkResponse + "");
                    mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(HomeActivity.this, networkResponse + "", Toast.LENGTH_SHORT).show();
                    error.printStackTrace();
                }
            });

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(HomeActivity.this).addToRequestQueue(multipartRequest);
        } else {
            Intent intent1 = new Intent(HomeActivity.this,NetworkErrorActivity.class);
            startActivity(intent1);
            //Toast.makeText(HomeActivity.this, R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    BroadcastReceiver netSwitchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isConnectionAvailable = intent.getExtras().getBoolean("is_connected");
            if (!isConnectionAvailable) {
                if (NetworkErrorActivity.isOptedToOffline()) {

                } else {
                    Intent intent1 = new Intent(HomeActivity.this,NetworkErrorActivity.class);
                    startActivity(intent1);
                }
            } else {
                NetworkErrorActivity.setOptedToOffline(false);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //videosList.clear();
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                videosList.clear();
                start = 0;
                showFullListAPI();
            }
        });
        try {
            if (Constant.NETWORK_CHECK == 1) {
                registerReceiver(netSwitchReceiver, new IntentFilter(Constant.NETWORK_SWITCH_FILTER));
                showFullListAPI();
            }else {
                Constant.NETWORK_CHECK = 0;
            }
        } catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(netSwitchReceiver);
        }
        catch (Exception e){

        }
    }
}
