package com.iSales.pages.boncmdeverification;

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

import com.iSales.adapter.RecapPanierAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.PanierEntry;
import com.iSales.database.entry.ServerEntry;
import com.iSales.model.ClientParcelable;
import com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity;
import com.iSales.pages.home.viewmodel.PanierViewModel;
import com.iSales.utility.ISalesUtility;
import com.iSales.R;

import java.util.ArrayList;
import java.util.List;

public class BonCmdeVerificationActivity extends AppCompatActivity {
    private Button mValiderBTN, mAnnulerBTN;
    private RecyclerView mPanierRecyclerView;
    private ImageView mProgressIV;
    private TextView mPanierTotalTV, mErrorTV;
    private TextView mClientNomTV, mClientVilleTV, mClientPaysTV, mClientPhoneTV, mClientEmailTV;
    private TextView mComgnieNomTV, mComgnieVilleTV, mComgniePaysTV, mComgniePhoneTV, mComgnieEmailTV;

    private ArrayList<com.iSales.database.entry.PanierEntry> panierEntriesList;
    private ClientParcelable mClientParcelableSelected;
    private ServerEntry mCompagnie;
    private double mTotalPanier = 0;
    private com.iSales.adapter.RecapPanierAdapter mAdapter;

    private com.iSales.database.AppDatabase mDb;

    //    recuperation des produits du panier
    private void loadPanier() {
        mProgressIV.setVisibility(View.VISIBLE);

        final com.iSales.pages.home.viewmodel.PanierViewModel viewModel = ViewModelProviders.of(this).get(PanierViewModel.class);
        viewModel.getAllPanierEntries().observe(this, new Observer<List<com.iSales.database.entry.PanierEntry>>() {
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

        mPanierTotalTV.setText(String.format("%s %s", com.iSales.utility.ISalesUtility.amountFormat2(""+total),
                ISalesUtility.CURRENCY));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bon_cmde_verification);

        mDb = AppDatabase.getInstance(getApplicationContext());


        mClientParcelableSelected = getIntent().getExtras().getParcelable("client");
        mCompagnie = mDb.serverDao().getActiveServer(true);
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
        mComgnieNomTV.setText(String.format("%s %s", getString(R.string.raison_sociale_), mCompagnie.getRaison_sociale()));
        mComgnieVilleTV.setText(String.format("%s %s", getString(R.string.ville_), mCompagnie.getVille()));
        mComgniePaysTV.setText(String.format("%s %s", getString(R.string.pays_), mCompagnie.getPays()));
        mComgniePhoneTV.setText(String.format("%s %s", getString(R.string.telephone_), mCompagnie.getTelephone()));
        mComgnieEmailTV.setText(String.format("%s %s", getString(R.string.email_), mCompagnie.getMail()));

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
