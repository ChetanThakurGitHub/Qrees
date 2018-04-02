package com.qrees.session;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.qrees.activity.LoginActivity;
import com.qrees.model.UserFullDetail;
import com.qrees.util.Constant;

public class Session {

    private SharedPreferences mypref,rememberMePref;
    private SharedPreferences.Editor editor,editor_r;
    private static final String PREF_NAME_R = "QREES_R";
    private static final String PREF_NAME = "QREES";
    private static final String IS_LOGEDIN = "isLogedin";

    public Session(Context context){
        Context mcontext = context;
        mypref = mcontext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = mypref.edit();
        editor.apply();

        rememberMePref = mcontext.getSharedPreferences(PREF_NAME_R, Context.MODE_PRIVATE);
        editor_r = rememberMePref.edit();
        editor_r.apply();
    }

    public void createSession(UserFullDetail userFullDetail) {

        editor.putString(Constant.USER_ID, userFullDetail.userId);
        editor.putString(Constant.NAME, userFullDetail.name);
        editor.putString(Constant.EMAIL, userFullDetail.email);
        editor.putString(Constant.PASSWORD, userFullDetail.password);
        editor.putString(Constant.PROFILE_IMAGE, userFullDetail.profileImage);
        editor.putString(Constant.CONTACT_NUMBER, userFullDetail.contactNumber);
        editor.putString(Constant.COUNTRY_CODE, userFullDetail.countryCode);
        editor.putString(Constant.LOCATION, userFullDetail.location);
        editor.putString(Constant.LATITUDE, userFullDetail.latitude);
        editor.putString(Constant.LOGITUDE, userFullDetail.longitude);
        editor.putString(Constant.DEVICETYPE, userFullDetail.deviceType);
        editor.putString(Constant.DEVICE_TOKEN, userFullDetail.deviceToken);
        editor.putString(Constant.SOCIAL_ID, userFullDetail.socialId);
        editor.putString(Constant.SOCILA_TYPE, userFullDetail.socialType);
        editor.putString(Constant.AUTHTOKEN, userFullDetail.authToken);
        editor.putString(Constant.FIREBASE_ID, userFullDetail.firebase_id);
        editor.putString(Constant.IS_NOTIFY, userFullDetail.Is_notify);
        editor.putString(Constant.STATUS, userFullDetail.status);
        editor.putString(Constant.CRD, userFullDetail.crd);
        editor.putString(Constant.UPD, userFullDetail.upd);

        editor.putBoolean(IS_LOGEDIN,true);
        editor.commit();

        editor_r.putString(Constant.EMAIL, userFullDetail.email);
        editor_r.putString(Constant.PASSWORD, userFullDetail.password);
        editor_r.commit();
    }

    public String getEmailR(){
        return rememberMePref.getString(Constant.EMAIL,"");}
    public String getPasswordR(){
        return rememberMePref.getString(Constant.PASSWORD,"");}
    public String getProfileImage(){
        return mypref.getString(Constant.PROFILE_IMAGE,"");}
    public String getFullName(){
        return mypref.getString(Constant.NAME,"");}
    public String getEmail(){
        return mypref.getString(Constant.EMAIL,"");}
    public String getPassword(){
        return mypref.getString(Constant.PASSWORD,"");}

    public void setEmailR(String emailR) {
        editor_r.putString(Constant.EMAIL, emailR);
        editor_r.commit();}
    public void setFullName(String chatCount) {
        editor.putString(Constant.NAME, chatCount);
        editor.commit();}
    public void setEmail(String chatCount) {
        editor.putString(Constant.EMAIL, chatCount);
        editor.commit();}
    public void setProfileImage(String chatCount) {
        editor.putString(Constant.PROFILE_IMAGE, chatCount);
        editor.commit();}

    public String getAuthToken(){return mypref.getString(Constant.AUTHTOKEN,"");}
    public boolean getIsLogedIn(){
        return mypref.getBoolean(IS_LOGEDIN, false);
    }

    public void logout(Activity activity){
        editor.clear();
        editor.apply();
       Toast.makeText(activity, "Logout sucessfully", Toast.LENGTH_SHORT).show();
    }
    public void logoutMyPre(){
        editor_r.clear();
        editor_r.apply();
    }
}
