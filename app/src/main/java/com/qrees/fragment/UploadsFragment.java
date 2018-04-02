package com.qrees.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.qrees.R;
import com.qrees.adapter.VideosAdapter;
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
import java.util.HashMap;
import java.util.Map;


public class UploadsFragment extends Fragment {

    private Session session;
    private int limit = 20,start = 0;
    private ArrayList<VideosListModel> videosList;
    private RecyclerView recycler_view;
    private VideosAdapter videosAdapter;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView tv_for_noData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_uploads, container, false);

        session = new Session(getActivity());
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        recycler_view = view.findViewById(R.id.recycler_view);
        tv_for_noData = view.findViewById(R.id.tv_for_noData);

        videosList = new ArrayList<>();
        videosAdapter = new VideosAdapter(videosList, getActivity());

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                videosList.clear();
                start = 0;
                showViewedListAPI();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);
                showViewedListAPI();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recycler_view.setLayoutManager(linearLayoutManager);

        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page, totalItemsCount);
            }
        };
        recycler_view.addOnScrollListener(scrollListener);

        return view;
    }

    public void loadNextDataFromApi(int page, int totalItemsCount) {
        //Log.e(TAG, "loadNextDataFromApi: " + page);
        //Log.e(TAG, "loadNextDataFromApi: " + limit);
        showViewedListAPI();
    } // pagination

    private void showViewedListAPI() {

        if (Utils.isNetworkAvailable(getContext())) {

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.GET,
                    Constant.URL_WITH_LOGIN + "getViewedUploadsList?"+"limit="+limit+"&start="+start+"&type=upload", new Response.Listener<NetworkResponse>() {
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
                            JSONArray homedata = jsonObject.getJSONArray("Upload");
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
                            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        }
                        if (videosList.size() == 0){
                            tv_for_noData.setVisibility(View.VISIBLE);
                        }else {
                            tv_for_noData.setVisibility(View.GONE);
                        }

                    } catch (Throwable t) {
                        Log.e("My App", "Could not parse malformed JSON: \"" + response + "\"");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    NetworkResponse networkResponse = error.networkResponse;
                    Constant.errorHandle(error, getActivity());
                    Log.i("Error", networkResponse + "");
                    Toast.makeText(getContext(), networkResponse + "", Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    error.printStackTrace();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();

                    headers.put("authToken", session.getAuthToken());
                    return headers;
                }
            };
            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);
        } else {
            Toast.makeText(getActivity(), R.string.check_net_connection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

}
