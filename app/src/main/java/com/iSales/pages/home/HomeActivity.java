package com.iSales.pages.home;

import com.iSales.database.AppDatabase;
import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.interfaces.DialogCategorieListener;
import com.iSales.interfaces.DialogClientListener;
import com.iSales.interfaces.MyCropImageListener;
import com.iSales.model.CategorieParcelable;
import com.iSales.model.ClientParcelable;
import com.iSales.pages.home.fragment.AProposFragment;
import com.iSales.pages.home.fragment.CategorieProduitFragment;
import com.iSales.pages.home.fragment.CategoriesFragment;
import com.iSales.pages.home.fragment.ClientProfileFragment;
import com.iSales.pages.home.fragment.ClientsFragment;
import com.iSales.pages.home.fragment.ClientsRadioFragment;
import com.iSales.pages.home.fragment.CommandesFragment;
import com.iSales.pages.home.fragment.PanierFragment;
import com.iSales.pages.home.fragment.ProfilFragment;
import com.iSales.task.FindPaymentTypesTask;
import com.iSales.R;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.view.ViewGroup;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.content.Context;


public class HomeActivity extends AppCompatActivity {

    private static final String TAG = HomeActivity.class.getSimpleName();
    private Toolbar toolbar;

    private TabLayout tablayout;

    private com.iSales.pages.home.fragment.ClientsFragment masterClientFragment;
    private com.iSales.pages.home.fragment.ClientProfileFragment detailsClientProfileFragment;

    private com.iSales.database.AppDatabase mDb;

    private String tabNames[] = {"clients", "panier", "categories", "commandes", "profil", "a propos"};

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

    private int activeTab = 0;

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

                if(isLayoutInDualPaneMode(true)){

                    masterClientFragment = com.iSales.pages.home.fragment.ClientsFragment.newInstance(new com.iSales.interfaces.DialogClientListener() {
                        @Override
                        public void onClientDialogSelected(com.iSales.model.ClientParcelable clientParcelable, int position) {

                            detailsClientProfileFragment.onClientDialogSelected(clientParcelable, position);
                        }
                    }, true);
                    detailsClientProfileFragment = ClientProfileFragment.newInstance(null, -1, new MyCropImageListener() {
                        @Override
                        public void onClientLogoChange(com.iSales.model.ClientParcelable clientParcelable, int position) {
                            masterClientFragment.onClientLogoChange(clientParcelable, position);
                        }
                    }, new ClientsAdapterListener() {
                        @Override
                        public void onClientsSelected(com.iSales.model.ClientParcelable clientParcelable, int position) {
                            masterClientFragment.onClientsSelected(clientParcelable, position);
                        }

                        @Override
                        public void onClientsUpdated(com.iSales.model.ClientParcelable clientParcelable, int position) {
                            masterClientFragment.onClientsUpdated(clientParcelable, position);
                        }
                    });

                    fragmentTransaction.replace(R.id.master_frame, masterClientFragment);
                    fragmentTransaction.replace(R.id.details_frame, detailsClientProfileFragment);

                } else {
                    fragmentTransaction.replace(R.id.master_frame, ClientsFragment.newInstance(null, false));
                }

                activeTab = 0;
                break;

            case 1:
                if(isLayoutInDualPaneMode(true)) {
                    final com.iSales.pages.home.fragment.PanierFragment detailsFragment = com.iSales.pages.home.fragment.PanierFragment.newInstance();
                    fragmentTransaction.replace(R.id.master_frame, ClientsRadioFragment.newInstance(new DialogClientListener() {
                        @Override
                        public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {

                            detailsFragment.onClientDialogSelected(clientParcelable, position);
                        }
                    }));

                    fragmentTransaction.replace(R.id.details_frame, detailsFragment);
                } else {
                    fragmentTransaction.replace(R.id.master_frame, PanierFragment.newInstance());
                }

                activeTab = 1;
                break;

            case 2:
                if(isLayoutInDualPaneMode(true)) {

                    final com.iSales.pages.home.fragment.CategoriesFragment detailsFragment = com.iSales.pages.home.fragment.CategoriesFragment.newInstance();
                    fragmentTransaction.replace(R.id.master_frame, CategorieProduitFragment.newInstance(new DialogCategorieListener() {
                        @Override
                        public void onCategorieDialogSelected(CategorieParcelable categorieParcelable) {

                            detailsFragment.onCategorieDialogSelected(categorieParcelable);
                        }
                    }, "product"));

                    fragmentTransaction.replace(R.id.details_frame, detailsFragment);

                } else {
                    fragmentTransaction.replace(R.id.master_frame, CategoriesFragment.newInstance());
                }

                activeTab = 2;
                break;

            case 3:
                if(isLayoutInDualPaneMode(false)) {
                    fragmentTransaction.replace(R.id.master_frame, com.iSales.pages.home.fragment.CommandesFragment.newInstance());
                } else {
                    fragmentTransaction.replace(R.id.master_frame, CommandesFragment.newInstance());
                }

                activeTab = 3;
                break;
            case 4:
                if(isLayoutInDualPaneMode(false)) {
                    fragmentTransaction.replace(R.id.master_frame, com.iSales.pages.home.fragment.ProfilFragment.newInstance());
                } else {
                    fragmentTransaction.replace(R.id.master_frame, ProfilFragment.newInstance());
                }

                activeTab = 4;
                break;
            default:
                if(isLayoutInDualPaneMode(false)) {
                    fragmentTransaction.replace(R.id.master_frame, com.iSales.pages.home.fragment.AProposFragment.newInstance());
                } else {
                    fragmentTransaction.replace(R.id.master_frame, AProposFragment.newInstance());
                }

                activeTab = 5;
                break;
        }
        fragmentTransaction.commit();

    }
    /**
     *
     * @param show_dual_pane indique si on veut que la vue s'affiche en dual pane au cas ou le mode est disponible
     * @return True si l'apareil permet l'affiche en dual pane et false sinon
     */
    private boolean isLayoutInDualPaneMode(boolean show_dual_pane){

        FrameLayout frameLayout = findViewById(R.id.details_frame);
        if(frameLayout != null) {
            if(show_dual_pane)
                frameLayout.setVisibility(FrameLayout.VISIBLE);
            else
                frameLayout.setVisibility(FrameLayout.GONE);
        }
        return  frameLayout != null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        this.mDb = AppDatabase.getInstance(HomeActivity.this);
        if (this.mDb.paymentTypesDao().getAllPayments().size() <= 0 ) {
            com.iSales.task.FindPaymentTypesTask task = new FindPaymentTypesTask(HomeActivity.this, null);
            task.execute();
        }

        initView();

        initToolbar();

        setupTabLayout();

        initTab();

        if (savedInstanceState != null) {

            activeTab = savedInstanceState.getInt("activeTab");
//            Log.e(TAG, "onCreate: activeTab="+activeTab );

            switchTab(activeTab);
            tablayout.getTabAt(activeTab).select();
        }
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
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("activeTab", activeTab);
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }
}
