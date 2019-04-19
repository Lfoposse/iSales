package com.iSales.pages.home.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iSales.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link com.iSales.pages.home.fragment.AProposFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AProposFragment extends Fragment {
    private static final String TAG = com.iSales.pages.home.fragment.AProposFragment.class.getSimpleName();

    private ProgressDialog progressDialog;

    int[] sampleImages = {R.drawable.logo_isales, R.drawable.logo_isales, R.drawable.logo_isales};

    public AProposFragment() {
        // Required empty public constructor
    }

    public static com.iSales.pages.home.fragment.AProposFragment newInstance() {
        com.iSales.pages.home.fragment.AProposFragment fragment = new com.iSales.pages.home.fragment.AProposFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_a_propos, container, false);


        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(0, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

}
