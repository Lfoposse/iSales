package com.rainbow_cl.i_sales.pages.home.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rainbow_cl.i_sales.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilFragment extends Fragment {


    public ProfilFragment() {
        // Required empty public constructor
    }

    public static ProfilFragment newInstance() {

        Bundle args = new Bundle();
        ProfilFragment fragment = new ProfilFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profil, container, false);
    }

}
