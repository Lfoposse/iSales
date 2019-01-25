package com.rainbow_cl.i_sales.pages.boncmdeverification;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.PanierProduitAdapter;
import com.rainbow_cl.i_sales.adapter.RecapPanierAdapter;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.pages.boncmdesignature.BonCmdeSignatureActivity;
import com.rainbow_cl.i_sales.pages.home.fragment.PanierFragment;
import com.rainbow_cl.i_sales.pages.home.viewmodel.PanierViewModel;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.util.ArrayList;
import java.util.List;

public class BonCmdeVerificationActivity extends AppCompatActivity {
    private Button mValiderBTN, mAnnulerBTN;
    private RecyclerView mPanierRecyclerView;
    private ImageView mProgressIV;
    private TextView mPanierTotalTV, mErrorTV;
    private TextView mClientNomTV, mClientVilleTV, mClientPaysTV, mClientPhoneTV, mClientEmailTV;
    private TextView mComgnieNomTV, mComgnieVilleTV, mComgniePaysTV, mComgniePhoneTV, mComgnieEmailTV;

    private ArrayList<PanierEntry> panierEntriesList;
    private ClientParcelable mClientParcelableSelected, mComgnietParcelableSelected;
    private double mTotalPanier = 0;
    private RecapPanierAdapter mAdapter;

    //    recuperation des produits du panier
    private void loadPanier() {
        mProgressIV.setVisibility(View.VISIBLE);

        final PanierViewModel viewModel = ViewModelProviders.of(this).get(PanierViewModel.class);
        viewModel.getAllPanierEntries().observe(this, new Observer<List<PanierEntry>>() {
            @Override
            public void onChanged(@Nullable List<PanierEntry> panierEntries) {

                if (panierEntries.size() <= 0) {
                    finish();
                    return;
                }

//        ajout des clientParcelables dans la liste
                if (panierEntriesList != null) {
                    panierEntriesList.clear();
                }
                panierEntriesList.addAll(panierEntries);

//        Mise a jour du montant total du panier
                setMontantTotalPanier();

                // rafraichissement recycler view
                mAdapter.notifyDataSetChanged();
                mProgressIV.setVisibility(View.GONE);
            }
        });
    }

    private void setMontantTotalPanier() {
        double total = 0;
        for (int i = 0; i < panierEntriesList.size(); i++) {
            double totalRow = Double.valueOf(panierEntriesList.get(i).getPrice_ttc()) * panierEntriesList.get(i).getQuantity();

            total += totalRow;
        }

        mPanierTotalTV.setText(String.format("%s %s", ISalesUtility.amountFormat2(""+total),
                ISalesUtility.CURRENCY));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bon_cmde_verification);

        mClientParcelableSelected = getIntent().getExtras().getParcelable("client");
        mComgnietParcelableSelected = getIntent().getExtras().getParcelable("client");
        mTotalPanier = getIntent().getExtras().getDouble("totalPanier");

        mPanierRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_boncmde_verif_produits);
        mValiderBTN = (Button) findViewById(R.id.btn_boncmde_verif_actions_valider);
        mAnnulerBTN = (Button) findViewById(R.id.btn_boncmde_verif_actions_annuler);
        mProgressIV = (ImageView) findViewById(R.id.iv_boncmde_verif_progress_produits);
        mPanierTotalTV = (TextView) findViewById(R.id.tv_boncmde_verif_total);
//        proprités client
        mClientNomTV = (TextView) findViewById(R.id.tv_boncmde_verif_client_nom);
        mClientVilleTV = (TextView) findViewById(R.id.tv_boncmde_verif_client_ville);
        mClientPaysTV = (TextView) findViewById(R.id.tv_boncmde_verif_client_pays);
        mClientPhoneTV = (TextView) findViewById(R.id.tv_boncmde_verif_client_telephone);
        mClientEmailTV = (TextView) findViewById(R.id.tv_boncmde_verif_client_email);
//        proprités compagnie
        mComgnieNomTV = (TextView) findViewById(R.id.tv_boncmde_verif_compagnie_nom);
        mComgnieVilleTV = (TextView) findViewById(R.id.tv_boncmde_verif_compagnie_ville);
        mComgniePaysTV = (TextView) findViewById(R.id.tv_boncmde_verif_compagnie_pays);
        mComgniePhoneTV = (TextView) findViewById(R.id.tv_boncmde_verif_compagnie_telephone);
        mComgnieEmailTV = (TextView) findViewById(R.id.tv_boncmde_verif_compagnie_email);

        panierEntriesList = new ArrayList<>();

        mAdapter = new RecapPanierAdapter(BonCmdeVerificationActivity.this, panierEntriesList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(BonCmdeVerificationActivity.this);
        mPanierRecyclerView.setLayoutManager(mLayoutManager);
        mPanierRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mPanierRecyclerView.setAdapter(mAdapter);

//        init client properties values
        mClientNomTV.setText(String.format("%s %s", getString(R.string.nom_), mClientParcelableSelected.getName()));
        mClientVilleTV.setText(String.format("%s %s", getString(R.string.ville_), mClientParcelableSelected.getTown()));
        mClientPaysTV.setText(String.format("%s %s", getString(R.string.pays_), mClientParcelableSelected.getPays()));
        mClientPhoneTV.setText(String.format("%s %s", getString(R.string.telephone_), mClientParcelableSelected.getPhone()));
        mClientEmailTV.setText(String.format("%s %s", getString(R.string.email_), mClientParcelableSelected.getEmail()));
//        init compagnie properties values
        mComgnieNomTV.setText(String.format("%s %s", getString(R.string.nom_), mComgnietParcelableSelected.getName()));
        mComgnieVilleTV.setText(String.format("%s %s", getString(R.string.ville_), mComgnietParcelableSelected.getTown()));
        mComgniePaysTV.setText(String.format("%s %s", getString(R.string.pays_), mComgnietParcelableSelected.getPays()));
        mComgniePhoneTV.setText(String.format("%s %s", getString(R.string.telephone_), mComgnietParcelableSelected.getPhone()));
        mComgnieEmailTV.setText(String.format("%s %s", getString(R.string.email_), mComgnietParcelableSelected.getEmail()));

//        ecoute du clique pour la validation
        mValiderBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BonCmdeVerificationActivity.this, BonCmdeSignatureActivity.class);
                intent.putExtra("client", mClientParcelableSelected);
                intent.putExtra("totalPanier", mTotalPanier);
                startActivity(intent);
            }
        });
        mAnnulerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

//        recuperation des clients sur le serveur
        loadPanier();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get the id of menu item clicked
        int id = item.getItemId();

//        if is toolbar back button then call device back button method
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
