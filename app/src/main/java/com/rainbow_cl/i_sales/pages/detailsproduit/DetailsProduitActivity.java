package com.rainbow_cl.i_sales.pages.detailsproduit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.AppExecutors;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.rainbow_cl.i_sales.utility.InputFilterMinMax;
import com.squareup.picasso.Picasso;

import java.io.File;

public class DetailsProduitActivity extends AppCompatActivity {
    private static final String TAG = DetailsProduitActivity.class.getSimpleName();

    private View activityView;
    private TextView mLabelTV, mRefTV, mPrixHtTV, mPrixTtcTV, mStockTV, mTvaTV, mDescriptionTV;
    private ImageView mPosterIV;
    private EditText mQuantiteNumberBtn;
    private EditText mPriceET;
    private FloatingActionButton mShoppingFAB;

    private AppDatabase mDb;
    private ProduitParcelable mProduitParcelable;

    public void initValues() {
        mLabelTV.setText(mProduitParcelable.getLabel());
        mRefTV.setText(mProduitParcelable.getRef());
        mPrixHtTV.setText(String.format("%s %s HT",
                ISalesUtility.amountFormat2(mProduitParcelable.getPrice()),
                ISalesUtility.CURRENCY));
        mPrixTtcTV.setText(String.format("%s %s TTC",
                ISalesUtility.amountFormat2(mProduitParcelable.getPrice_ttc()),
                ISalesUtility.CURRENCY));
        mStockTV.setText(String.format("%s", mProduitParcelable.getStock_reel()));
        mTvaTV.setText(String.format("%s %s", ISalesUtility.amountFormat2(mProduitParcelable.getTva_tx()), "%"));
        mDescriptionTV.setText(ISalesUtility.getDescProduit(mProduitParcelable.getDescription()));
        mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces(mProduitParcelable.getPrice_ttc()));
        mQuantiteNumberBtn.setText("0");

        mQuantiteNumberBtn.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mQuantiteNumberBtn, InputMethodManager.SHOW_IMPLICIT);

        if (mProduitParcelable.getLocal_poster_path() != null) {
//            si le fichier existe dans la memoire locale
            File imgFile = new File(mProduitParcelable.getLocal_poster_path());
            if (imgFile.exists()) {
                Picasso.with(DetailsProduitActivity.this)
                        .load(imgFile)
                        .into(mPosterIV);
                return;

            } else {
                Picasso.with(DetailsProduitActivity.this)
                        .load(R.drawable.isales_no_image)
                        .into(mPosterIV);
            }
        } else {
            Picasso.with(DetailsProduitActivity.this)
                    .load(R.drawable.isales_no_image)
                    .into(mPosterIV);
        }

        String original_file = mProduitParcelable.getRef() + "/" + mProduitParcelable.getPoster().getFilename();
        String module_part = "produit";
//        Log.e(TAG, "onBindViewHolder: downloadLinkImg="+ApiUtils.getDownloadImg(mContext, module_part, original_file));
        Picasso.with(DetailsProduitActivity.this)
                .load(ApiUtils.getDownloadImg(DetailsProduitActivity.this, module_part, original_file))
                .placeholder(R.drawable.isales_no_image)
                .error(R.drawable.isales_no_image)
                .into(mPosterIV, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                        Bitmap imageBitmap = ((BitmapDrawable) mPosterIV.getDrawable()).getBitmap();

                        String pathFile = ISalesUtility.saveProduitImage(DetailsProduitActivity.this, imageBitmap, mProduitParcelable.getRef());
                        Log.e(TAG, "onPostExecute: pathFile=" + pathFile);

                        if (pathFile != null) mProduitParcelable.setLocal_poster_path(pathFile);

//                    Modification du path de la photo du produit
                        mDb.produitDao().updateLocalImgPath(mProduitParcelable.getId(), pathFile);
                    }

                    @Override
                    public void onError() {

                    }
                });
    }

    //    insert a movie in database
    public void addPanier() {
        boolean cancel = false;
        View focusView = null;
        if (mQuantiteNumberBtn.getText().toString().equals("")) {
            mQuantiteNumberBtn.setError(getString(R.string.veuillez_saisir_quantite));
            focusView = mQuantiteNumberBtn;
            cancel = true;
        }
        if (mPriceET.getText().toString().equals("")) {
            mPriceET.setError(getString(R.string.veuillez_saisir_prix_vente));
            focusView = mPriceET;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        int quantite = Integer.parseInt(mQuantiteNumberBtn.getText().toString());
        Log.e(TAG, "addPanier: id = " + mProduitParcelable.getId() + " quantite" + quantite);

        // get movie in db
        final PanierEntry panierEntryTest = mDb.panierDao().getPanierById(mProduitParcelable.getId());

//        Teste si le produit n'est pas deja dans le panier
        if (panierEntryTest != null) {
//            Si le produit existe et la quantite est la meme, alors on renvoit un message d'erreur
            if (panierEntryTest.getQuantity() == quantite) {
//            Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                final Snackbar snackbar = Snackbar
                        .make(activityView, String.format("%s existe dans le panier.", mProduitParcelable.getLabel()), Snackbar.LENGTH_LONG);

// Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.snackbar_error));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.white));
                snackbar.show();
                return;
            } else {
                String priceString = mPriceET.getText().toString();

                double price = Double.parseDouble(priceString.replace(',', '.'));
//                Sinon, on modifi la quantite a commander
                panierEntryTest.setQuantity(quantite);
                panierEntryTest.setPrice_ttc("" + price);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // insert new panier
                        mDb.panierDao().updatePanier(panierEntryTest);
//                Log.e(TAG, "run: addPanier");
//                Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                        final Snackbar snackbar = Snackbar
                                .make(activityView, String.format("Quantité %s mis à jour.", mProduitParcelable.getLabel()), Snackbar.LENGTH_LONG);

// Changing action button text color
                        View sbView = snackbar.getView();
                        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(getResources().getColor(R.color.snackbar_update));
                        snackbar.setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.setActionTextColor(getResources().getColor(R.color.white));
                        snackbar.show();
                    }
                });
                return;
            }
        }

//        Ajout du produit dans la bd locale
        final PanierEntry panierEntry = new PanierEntry();
        String priceString = mPriceET.getText().toString();

        double price = Double.parseDouble(priceString.replace(',', '.'));

        Log.e(TAG, "addPanier: priceString=" + priceString + " price=" + price);
        if (price >= Double.parseDouble(mProduitParcelable.getPrice_ttc())) {
            panierEntry.setPrice_ttc("" + price);
        } else {
            panierEntry.setPrice_ttc(mProduitParcelable.getPrice_ttc());
        }

//        initialisation des valeur du produit a ajouter dans le panier
        panierEntry.setId(mProduitParcelable.getId());
        panierEntry.setLabel(mProduitParcelable.getLabel());
        panierEntry.setDescription(mProduitParcelable.getDescription());
        panierEntry.setPrice(mProduitParcelable.getPrice());
        panierEntry.setRef(mProduitParcelable.getRef());
        panierEntry.setPoster_content(mProduitParcelable.getLocal_poster_path());
        panierEntry.setStock_reel(mProduitParcelable.getStock_reel());
        panierEntry.setTva_tx(mProduitParcelable.getTva_tx());
        panierEntry.setQuantity(quantite);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // insert new panier
                mDb.panierDao().insertPanier(panierEntry);
//                Log.e(TAG, "run: addPanier");
//                Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                final Snackbar snackbar = Snackbar
                        .make(activityView, String.format("%s ajouté dans le panier.", mProduitParcelable.getLabel()), Snackbar.LENGTH_LONG);

// Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getResources().getColor(R.color.snackbar_success));
                snackbar.setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        snackbar.dismiss();
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.white));
                snackbar.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_produit);

        mDb = AppDatabase.getInstance(getApplicationContext());
        if (getIntent().getExtras().getParcelable("produit") != null) {
            mProduitParcelable = getIntent().getExtras().getParcelable("produit");
            Log.e(TAG, "onCreate: " + mProduitParcelable.getRef() +
                    " produitID=" + mProduitParcelable.getId() +
                    " description=" + mProduitParcelable.getDescription() +
                    " produitID=" + mProduitParcelable.getPoster().getContent());
        }

//        Referencement des vues
        activityView = (View) findViewById(R.id.iv_produitdetails_poster);
        mPosterIV = (ImageView) findViewById(R.id.iv_produitdetails_poster);
        mLabelTV = (TextView) findViewById(R.id.tv_produitdetails_label);
        mRefTV = (TextView) findViewById(R.id.tv_produitdetails_ref);
        mPrixHtTV = (TextView) findViewById(R.id.tv_produitdetails_prix_ht);
        mPrixTtcTV = (TextView) findViewById(R.id.tv_produitdetails_prix_ttc);
        mStockTV = (TextView) findViewById(R.id.tv_produitdetails_stock);
        mTvaTV = (TextView) findViewById(R.id.tv_produitdetails_tva);
        mDescriptionTV = (TextView) findViewById(R.id.tv_produitdetails_description);
        mShoppingFAB = (FloatingActionButton) findViewById(R.id.fab_produitdetails_shopping);
        mQuantiteNumberBtn = (EditText) findViewById(R.id.et_produitdetails_quantite);
        mPriceET = (EditText) findViewById(R.id.et_produitdetails_price);

//        Filtre la saisie de la quantite avec la valeur min (1)
        mQuantiteNumberBtn.setFilters(new InputFilter[]{new InputFilterMinMax("1", "15")});

        mShoppingFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPanier();
            }
        });

        if (savedInstanceState != null) {
            mProduitParcelable = (ProduitParcelable) getIntent().getParcelableExtra("produit");

        }

        initValues();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        Log.e(TAG, "onSaveInstanceState: ");

        outState.putParcelable("produit", mProduitParcelable);
        super.onSaveInstanceState(outState);
    }
}
