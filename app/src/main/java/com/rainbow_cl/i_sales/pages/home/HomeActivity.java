package com.rainbow_cl.i_sales.pages.home;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.pages.home.fragment.CategoriesFragment;
import com.rainbow_cl.i_sales.pages.home.fragment.ClientsFragment;
import com.rainbow_cl.i_sales.pages.home.fragment.CommandesFragment;
import com.rainbow_cl.i_sales.pages.home.fragment.PanierFragment;
import com.rainbow_cl.i_sales.pages.home.fragment.ProfilFragment;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.ViewGroup;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.content.Context;


public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private TabLayout tablayout;

    private String tabNames[] = {"clients", "categories", "panier", "commandes", "profil"};

    private int[] tabIconsUnSelected = {
            R.drawable.ic_clients_desactive,
            R.drawable.img_cardenas,
            R.drawable.ic_panier_desactive,
            R.drawable.img_cardenas,
            R.drawable.img_cardenas};

    private int[] tabIconsSelected = {
            R.drawable.ic_clients_active,
            R.drawable.img_user,
            R.drawable.ic_panier_active,
            R.drawable.img_user,
            R.drawable.img_user};

    public static Drawable setDrawableSelector(Context context, int normal, int selected) {

        Drawable state_normal = ContextCompat.getDrawable(context, normal);

        Drawable state_pressed = ContextCompat.getDrawable(context, selected);

        StateListDrawable drawable = new StateListDrawable();

        drawable.addState(new int[]{android.R.attr.state_selected},
                state_pressed);
        drawable.addState(new int[]{android.R.attr.state_enabled},
                state_normal);

        return drawable;
    }

    public static ColorStateList setTextselector(int normal, int pressed) {
        ColorStateList colorStates = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_selected},
                        new int[]{}
                },
                new int[]{
                        pressed,
                        normal});
        return colorStates;
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        tablayout = (TabLayout) findViewById(R.id.tablayout);
    }

    private void initToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void initTab() {
        if (tablayout != null) {
            for (int i = 0; i < tabNames.length; i++) {
                tablayout.addTab(tablayout.newTab());
                TabLayout.Tab tab = tablayout.getTabAt(i);
                if (tab != null)
                    tab.setCustomView(getTabView(i));
            }
        }

    }

    private View getTabView(int position) {
        View view = LayoutInflater.from(HomeActivity.this).inflate(R.layout.view_tabs, null);

        /*
        ImageView icon = (ImageView) view.findViewById(R.id.tab_icon);
        icon.setImageDrawable(setDrawableSelector(HomeActivity.this, tabIconsUnSelected[position], tabIconsSelected[position]));
        */

        TextView text = (TextView) view.findViewById(R.id.tab_text);
        text.setText(tabNames[position]);
        text.setTextColor(setTextselector(Color.parseColor("#E6E6E6"), Color.parseColor("#FFFFFF")));


        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private void switchTab(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (position) {

            case 0:

                fragmentTransaction.replace(R.id.content_frame, ClientsFragment.newInstance());
                break;

            case 1:

                fragmentTransaction.replace(R.id.content_frame, CategoriesFragment.newInstance());
                break;

            case 2:
                fragmentTransaction.replace(R.id.content_frame, PanierFragment.newInstance());
                break;

            case 3:
                fragmentTransaction.replace(R.id.content_frame, CommandesFragment.newInstance());
                break;
            default:
                fragmentTransaction.replace(R.id.content_frame, ProfilFragment.newInstance());
                break;
        }
        fragmentTransaction.commit();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initView();

        initToolbar();

        setupTabLayout();

        initTab();

    }

    private void setupTabLayout() {

        tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {


                switchTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
