package com.rainbow_cl.i_sales.pages.home.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ClientsAdapter;
import com.rainbow_cl.i_sales.adapter.CommandeAdapter;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.interfaces.CommandeAdapterListener;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.model.CommandeParcelable;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommandesFragment extends Fragment implements CommandeAdapterListener {

    private RecyclerView mRecyclerView;
    private ImageView mProgressIV;
    private EditText searchET;
    private ArrayList<CommandeParcelable> commandeParcelableList;
    private CommandeAdapter mAdapter;

    public CommandesFragment() {
        // Required empty public constructor
    }

    public static CommandesFragment newInstance() {

        Bundle args = new Bundle();
        CommandesFragment fragment = new CommandesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCommandeSelected(CommandeParcelable commandeParcelable) {

    }

    private void fetchCommandeList() {


//        Affichage de la liste des produits sur la vue
        ArrayList<CommandeParcelable> commandeParcelables = new ArrayList<>();
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());

        commandeParcelableList.addAll(commandeParcelables);

        mAdapter.notifyDataSetChanged();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_commandes, container, false);

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recyclerview_commandes);
        mProgressIV = rootView.findViewById(R.id.iv_progress_commandes);
//        searchET = rootView.findViewById(R.id.et_search);
        commandeParcelableList = new ArrayList<>();

        mAdapter = new CommandeAdapter(getContext(), commandeParcelableList, CommandesFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mRecyclerView.setAdapter(mAdapter);

//        ecoute de la recherche d'un client
        /*
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mAdapter.performFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
         */

//        recuperation des clients sur le serveur
        fetchCommandeList();

        return rootView;
    }
}
