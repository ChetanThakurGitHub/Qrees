package com.qrees.activity;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.qrees.R;
import com.qrees.fragment.UploadsFragment;
import com.qrees.fragment.ViewedFragment;
import com.qrees.session.Session;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TabLayout sliding_tabs;
    private ViewPager viewpager;
    private ImageView iv_for_edit,profile_image;
    private TextView tv_for_name,tv_for_email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initView();

        Session session = new Session(this);
        String image = session.getProfileImage();
        if (image != null && !image.equals("")) {
            Picasso.with(this).load(image).into(profile_image);}
        tv_for_name.setText(session.getFullName());
        tv_for_email.setText(session.getEmail());

        sliding_tabs.setupWithViewPager(viewpager);
        setupViewPager(viewpager);
        iv_for_edit.setOnClickListener(this);
    }

    private void setupViewPager(ViewPager viewpager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ViewedFragment(), "Viewed");
        adapter.addFragment(new UploadsFragment(), "Uploads");
        viewpager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Session session = new Session(this);
        String image = session.getProfileImage();
        if (image != null && !image.equals("")) {
            Picasso.with(this).load(image).into(profile_image);}
        tv_for_name.setText(session.getFullName());
        tv_for_email.setText(session.getEmail());
    }

    private void initView() {
        TextView tv_for_tittle = findViewById(R.id.tv_for_tittle);
        ImageView iv_for_back = findViewById(R.id.iv_for_back);
        profile_image = findViewById(R.id.profile_image);
        iv_for_edit = findViewById(R.id.iv_for_edit);
        sliding_tabs = findViewById(R.id.sliding_tabs);
        viewpager = findViewById(R.id.viewpager);
        tv_for_name = findViewById(R.id.tv_for_name);
        tv_for_email = findViewById(R.id.tv_for_email);

        tv_for_tittle.setText(R.string.profile_title);
        iv_for_back.setVisibility(View.VISIBLE);
        iv_for_edit.setVisibility(View.VISIBLE);
        iv_for_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.iv_for_edit:
                Intent intent = new Intent(ProfileActivity.this,EditProfileActivity.class);
                startActivity(intent);
                break;
        }
    }
}
