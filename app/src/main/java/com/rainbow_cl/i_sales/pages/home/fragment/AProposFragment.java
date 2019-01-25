package com.rainbow_cl.i_sales.pages.home.fragment;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow_cl.i_sales.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AProposFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AProposFragment extends Fragment {
    private static final String TAG = AProposFragment.class.getSimpleName();

    private ProgressDialog progressDialog;

    int[] sampleImages = {R.drawable.logo_isales, R.drawable.logo_isales, R.drawable.logo_isales};

    public AProposFragment() {
        // Required empty public constructor
    }

    public static AProposFragment newInstance() {
        AProposFragment fragment = new AProposFragment();
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

}
