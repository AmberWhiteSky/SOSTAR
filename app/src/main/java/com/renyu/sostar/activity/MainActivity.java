package com.renyu.sostar.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.renyu.commonlibrary.baseact.BaseActivity;
import com.renyu.commonlibrary.commonutils.BarUtils;
import com.renyu.sostar.R;
import com.renyu.sostar.service.LocationService;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @BindView(R.id.nav_layout)
    RelativeLayout nav_layout;
    @BindView(R.id.main_dl)
    DrawerLayout main_dl;
    @BindView(R.id.nav_left_image)
    ImageView nav_left_image;
    @BindView(R.id.main_menu_layout)
    LinearLayout main_menu_layout;
    @BindView(R.id.main_menu_grid)
    GridLayout main_menu_grid;

    @Override
    public void initParams() {
        // 开启定位上报
        startService(new Intent(this, LocationService.class));

        nav_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        nav_left_image.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));

        main_dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        main_dl.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                main_dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                main_dl.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                Log.d("MainActivity", "newState:" + newState);
            }
        });
        BarUtils.setColorForDrawerLayout(this, main_dl, ContextCompat.getColor(this, R.color.colorPrimaryDark));

        main_menu_grid.post(() -> {
            int height=main_menu_grid.getMeasuredHeight();
            int width=main_menu_grid.getMeasuredWidth();
            for (int i=0;i<main_menu_grid.getChildCount();i++) {
                View view=main_menu_grid.getChildAt(i);
                GridLayout.LayoutParams params= (GridLayout.LayoutParams) view.getLayoutParams();
                params.width=width/3;
                params.height=height/2;
            }
        });
    }

    @Override
    public int initViews() {
        return R.layout.activity_main;
    }

    @Override
    public void loadData() {

    }

    @Override
    public int setStatusBarColor() {
        return 0;
    }

    @Override
    public int setStatusBarTranslucent() {
        return 1;
    }

    @OnClick({R.id.nav_left_image})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nav_left_image:
                if (main_dl.isDrawerOpen(main_menu_layout)) {
                    main_dl.closeDrawer(main_menu_layout);
                }
                else {
                    main_dl.openDrawer(main_menu_layout);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED, new Intent());
        finish();
    }
}
