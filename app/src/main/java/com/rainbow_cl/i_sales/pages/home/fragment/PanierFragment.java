package com.rainbow_cl.i_sales.pages.home.fragment;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ClientsAdapter;
import com.rainbow_cl.i_sales.adapter.PanierProduitAdapter;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.model.ProduitParcelable;

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
import android.widget.ImageView;

import java.util.ArrayList;

public class PanierFragment extends Fragment {
    private static final String TAG = PanierFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ImageView mProgressIV;

    private PanierProduitAdapter mAdapter;
    private ArrayList<ProduitParcelable> produitParcelableList;
    /**
     * fetches json by making http calls
     */
    private void initPanier() {
        ArrayList<ProduitParcelable> produitParcelables = new ArrayList<>();
        produitParcelables.add(new ProduitParcelable("Cacahuetes choco", "15", 54));
        produitParcelables.add(new ProduitParcelable("Hamburgre", "5", 54));
        produitParcelables.add(new ProduitParcelable("Sandwich épicé", "18", 54));
        produitParcelables.add(new ProduitParcelable("Frites de pomme sauté", "100", 54));
        produitParcelables.add(new ProduitParcelable("Sauce vinaigrée", "68", 54));

//        ajout des clientParcelables dans la liste
        if (produitParcelableList != null) {
            produitParcelableList.clear();
        }
        produitParcelableList.addAll(produitParcelables);

        // rafraichissement recycler view
        mAdapter.notifyDataSetChanged();
    }

    public PanierFragment() {
        // Required empty public constructor
    }

    public static PanierFragment newInstance() {

        Bundle args = new Bundle();
        PanierFragment fragment = new PanierFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_panier, container, false);

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recyclerview_panier_produits);
        mProgressIV = rootView.findViewById(R.id.iv_panier_progress_produits);
        produitParcelableList = new ArrayList<>();

        mAdapter = new PanierProduitAdapter(getContext(), produitParcelableList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mRecyclerView.setAdapter(mAdapter);

//        recuperation des clients sur le serveur
//        executeFindClients();
        initPanier();

        return rootView;
    }

}
