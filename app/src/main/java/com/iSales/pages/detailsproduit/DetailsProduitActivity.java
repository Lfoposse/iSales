package com.iSales.pages.detailsproduit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.iSales.R;
import com.iSales.adapter.ProductVirtualAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.AppExecutors;
import com.iSales.database.entry.PanierEntry;
import com.iSales.interfaces.FindProductVirtualListener;
import com.iSales.interfaces.ProductVirtualAdapterListener;
import com.iSales.model.ProduitParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.model.ProductVirtual;
import com.iSales.remote.rest.FindProductVirtualREST;
import com.iSales.task.FindProductVirtualTask;
import com.iSales.utility.ISalesUtility;
import com.iSales.utility.InputFilterMinMax;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailsProduitActivity extends AppCompatActivity implements FindProductVirtualListener, ProductVirtualAdapterListener {
    private static final String TAG = com.iSales.pages.detailsproduit.DetailsProduitActivity.class.getSimpleName();

    private View activityView;
    private TextView mLabelTV, mRefTV, mPrixHtTV, mPrixTtcTV, mStockTV, mTvaTV, mDescriptionTV, mNoteTV, mPriceNature, mQuantiteNature;
    private ImageView mPosterIV;
    private EditText mQuantiteNumberBtn;
    private EditText mPriceET, mPriceUnitaireET, mRemiseValeur, mRemisePourcentage;
    private RecyclerView mPdtVirtualRecyclerview;
    private FloatingActionButton mShoppingFAB;

    private AppDatabase mDb;
    private ProduitParcelable mProduitParcelable;

    private List<ProductVirtual> productVirtualList;
    private ProductVirtualAdapter productVirtualAdapter;
    private ProductVirtual mProductVirtual;

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public void showLog() {

        if (isExternalStorageWritable()) {

            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/iSalesLog");
            File logDirectory = new File(appDirectory + "/log");
            File logFile = new File(logDirectory, "details_produit_logcat" + System.currentTimeMillis() + ".txt");

            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (isExternalStorageReadable()) {
            // only readable
            Log.e(TAG, "onCreate: isExternalStorageReadable");
        } else {
            // not accessible
            Log.e(TAG, "onCreate: non isExternalStorageReadable");
        }
    }

    public void initValues() {
        mProductVirtual = new ProductVirtual();
        mProductVirtual.setRowid("" + mProduitParcelable.getId());
        mProductVirtual.setFk_product_fils("" + mProduitParcelable.getId());
        mProductVirtual.setFk_product_pere("" + mProduitParcelable.getId());
        mProductVirtual.setQty("1");
        mProductVirtual.setRef(mProduitParcelable.getRef());
        mProductVirtual.setDatec(mProduitParcelable.getDate_creation());
        mProductVirtual.setLabel(mProduitParcelable.getLabel() + " UNITÉ");
        mProductVirtual.setDescription(mProduitParcelable.getDescription());
        mProductVirtual.setNote_public(mProduitParcelable.getNote_public());
        mProductVirtual.setNote(mProduitParcelable.getNote());
        mProductVirtual.setPrice(mProduitParcelable.getPrice());
        mProductVirtual.setPrice_ttc(mProduitParcelable.getPrice_ttc());
        mProductVirtual.setPrice_min(mProduitParcelable.getPrice_min());
        mProductVirtual.setPrice_min_ttc(mProduitParcelable.getPrice_min_ttc());
        mProductVirtual.setPrice_base_type(mProduitParcelable.getPrice_base_type());
        mProductVirtual.setTva_tx(mProduitParcelable.getTva_tx());
        mProductVirtual.setLocal_poster_path(mProduitParcelable.getLocal_poster_path());
        mProductVirtual.setSeuil_stock_alerte(mProduitParcelable.getSeuil_stock_alerte());
        mProductVirtual.setStock("" + mProduitParcelable.getStock_reel());
        productVirtualList.add(mProductVirtual);

        productVirtualAdapter.notifyDataSetChanged();

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
        mPriceUnitaireET.setText(ISalesUtility.roundOffTo2DecPlaces(mProduitParcelable.getPrice()));
        mDescriptionTV.setText(ISalesUtility.getDescProduit(mProduitParcelable.getDescription()));
        mNoteTV.setText(mProduitParcelable.getNote());
        mQuantiteNumberBtn.setText("0");

        double price = Double.parseDouble(mProduitParcelable.getPrice()) * Integer.parseInt(mProductVirtual.getQty());
        mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces("" + price));

        String[] label = mProductVirtual.getLabel().split(" ");

        mPriceNature.setText(String.format("/ %s", label[label.length - 1]));
        mQuantiteNature.setText(String.format("%s(S)", label[label.length - 1]));

        mQuantiteNumberBtn.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mQuantiteNumberBtn, InputMethodManager.SHOW_IMPLICIT);

        if (mProduitParcelable.getLocal_poster_path() != null) {
            Log.e(TAG, "onBindViewHolder: getLocal_poster_path=" + mProduitParcelable.getLocal_poster_path());
//            si le fichier existe dans la memoire locale
            File imgFile = new File(mProduitParcelable.getLocal_poster_path());
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                mPosterIV.setImageBitmap(myBitmap);
                return;

            } else {
//        Log.e(TAG, "onBindViewHolder: downloadLinkImg="+ApiUtils.getDownloadImg(mContext, module_part, original_file));
                mPosterIV.setImageResource(R.drawable.isales_no_image);
            }
        } else {
            Log.e(TAG, "onBindViewHolder: getLocal_poster_path=" + mProduitParcelable.getLocal_poster_path());

            if (mProduitParcelable.getLocal_poster_path() == null) {
                Picasso.with(com.iSales.pages.detailsproduit.DetailsProduitActivity.this)
                        .load(ApiUtils.getDownloadProductImg(com.iSales.pages.detailsproduit.DetailsProduitActivity.this, mProduitParcelable.getRef()))
                        .placeholder(R.drawable.isales_no_image)
                        .error(R.drawable.isales_no_image)
                        .into(mPosterIV, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                                Bitmap imageBitmap = ((BitmapDrawable) mPosterIV.getDrawable()).getBitmap();

                                String pathFile = ISalesUtility.saveProduitImage(com.iSales.pages.detailsproduit.DetailsProduitActivity.this, imageBitmap, mProduitParcelable.getRef());
//                        Log.e(TAG, "onPostExecute: pathFile=" + pathFile);

//                            if (pathFile != null) mProduitParcelable.setLocal_poster_path(pathFile);

//                    Modification du path de la photo du produit
                                mDb.produitDao().updateLocalImgPath(mProduitParcelable.getId(), pathFile);
                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else {
                mPosterIV.setImageResource(R.drawable.isales_no_image);
            }
        }
    }

    private void updateProductValues(ProductVirtual productVirtual) {
        mProductVirtual = productVirtual;

        Log.e(TAG, "updateProductValues: qty=" + mProductVirtual.getQty());

        mPrixHtTV.setText(String.format("%s %s HT",
                ISalesUtility.amountFormat2(mProductVirtual.getPrice()),
                ISalesUtility.CURRENCY));
        mPrixTtcTV.setText(String.format("%s %s TTC",
                ISalesUtility.amountFormat2(mProductVirtual.getPrice_ttc()),
                ISalesUtility.CURRENCY));
        mStockTV.setText(String.format("%s", mProductVirtual.getStock()));
        mTvaTV.setText(String.format("%s %s", ISalesUtility.amountFormat2(mProductVirtual.getTva_tx()), "%"));

        /*
        String prixU = mPriceUnitaireET.getText().toString().replace(",", ".");
        prixU = prixU.equals("") ? mProduitParcelable.getPrice() : prixU;
        double price = Double.parseDouble(prixU) * Integer.parseInt(mProductVirtual.getQty()); */

        mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces("" + mProductVirtual.getPrice()));

        String[] label = mProductVirtual.getLabel().split(" ");

        mPriceNature.setText(String.format("/ %s", label[label.length - 1]));
        mQuantiteNature.setText(String.format("%s(S)", label[label.length - 1]));
    }

    //    insert a movie in database
    public void addPanier() {
        boolean cancel = false;
        View focusView = null;
        double prix = 0;
        double remiseVal = 0;
        double remisePercent = 0;

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

        /*
        if (!mPriceET.getText().toString().equals("")) {
            if (prix < Double.parseDouble(mProductVirtual.getPrice())) {
                mPriceET.setError(getString(R.string.prix_vente_trop_petit));
                focusView = mPriceET;
                cancel = true;
            }
        } */

        if (!mRemiseValeur.getText().toString().equals("")) {
            remiseVal = Double.parseDouble(mRemiseValeur.getText().toString().replace(",", "."));
            if (remiseVal > Double.parseDouble(mProductVirtual.getPrice())) {
                mRemiseValeur.setError(getString(R.string.remise_doit_etre_inferieure_au_prix_u));
                focusView = mRemiseValeur;
                cancel = true;
            }
        }

        if (!mRemisePourcentage.getText().toString().equals("")) {
            remisePercent = Double.parseDouble(mRemisePourcentage.getText().toString().replace(",", "."));
            if (remisePercent > 100) {
                mRemisePourcentage.setError(getString(R.string.remise_doit_etre_inferieure_cent));
                focusView = mRemisePourcentage;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        prix = Double.parseDouble(mPriceET.getText().toString().replace(",", "."));
        double prix_u = prix;
//        int quantite = Integer.parseInt(mQuantiteNumberBtn.getText().toString()) * Integer.parseInt(mProductVirtual.getQty());
        int quantite = Integer.parseInt(mQuantiteNumberBtn.getText().toString());
        Log.e(TAG, "addPanier: id = " + mProductVirtual.getRowid() + " quantite" + quantite + " prix_u" + prix_u);

        // get movie in db
//        final PanierEntry panierEntryTest = mDb.panierDao().getPanierById(mProduitParcelable.getId());
        final PanierEntry panierEntryTest = mDb.panierDao().getPanierById(Long.parseLong(mProductVirtual.getRowid()));

//        Teste si le produit n'est pas deja dans le panier
        if (panierEntryTest != null) {
//            Si le produit existe et la quantite est la meme, alors on renvoit un message d'erreur
            if (panierEntryTest.getQuantity() == quantite) {
//            Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                final Snackbar snackbar = Snackbar
                        .make(activityView, String.format("%s existe dans le panier.", mProductVirtual.getLabel()), Snackbar.LENGTH_LONG);

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

//                double price = Double.parseDouble(priceString.replace(',', '.'));
                double tva = Double.parseDouble(mProductVirtual.getTva_tx());
                double pricettc = prix_u + ((prix_u * tva) / 100);

//                Sinon, on modifi la quantite a commander
                panierEntryTest.setQuantity(quantite);
                panierEntryTest.setPrice("" + prix_u);
                panierEntryTest.setPrice_ttc("" + pricettc);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // insert new panier
                        mDb.panierDao().updatePanier(panierEntryTest);
//                Log.e(TAG, "run: addPanier");
//                Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                        final Snackbar snackbar = Snackbar
                                .make(activityView, String.format("Quantité %s mis à jour.", mProductVirtual.getLabel()), Snackbar.LENGTH_LONG);

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

//        double price = Double.parseDouble(priceString.replace(',', '.'));
        double tva = Double.parseDouble(mProductVirtual.getTva_tx());

        Log.e(TAG, "addPanier: priceString=" + priceString + " price=" + prix_u + " fk_product=" + mProductVirtual.getRowid());
        double pricettc = prix_u + ((prix_u * tva) / 100);

        panierEntry.setPrice("" + prix_u);
        panierEntry.setPrice_ttc("" + pricettc);
        panierEntry.setRemise("" + remiseVal);
        panierEntry.setRemise_percent("" + remisePercent);

//        initialisation des valeur du produit a ajouter dans le panier
        panierEntry.setId(Long.parseLong(mProductVirtual.getRowid()));
        panierEntry.setFk_product(Long.parseLong(mProductVirtual.getRowid()));
        panierEntry.setLabel(mProductVirtual.getLabel());
        panierEntry.setDescription(mProductVirtual.getDescription());
        panierEntry.setRef(mProductVirtual.getRef());
        panierEntry.setFile_content(mProduitParcelable.getLocal_poster_path());
        panierEntry.setPoster_content(mProduitParcelable.getLocal_poster_path());
//        panierEntry.setStock_reel(mProduitParcelable.getStock_reel());
        panierEntry.setTva_tx(mProductVirtual.getTva_tx());
        panierEntry.setQuantity(quantite);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // insert new panier
                mDb.panierDao().insertPanier(panierEntry);
//                Log.e(TAG, "run: addPanier");
//                Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                final Snackbar snackbar = Snackbar
                        .make(activityView, String.format("%s ajouté dans le panier.", mProductVirtual.getLabel()), Snackbar.LENGTH_LONG);

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

    private void executeFindproductVirtual() {
        FindProductVirtualTask task = new FindProductVirtualTask(com.iSales.pages.detailsproduit.DetailsProduitActivity.this, mProduitParcelable.getId(), com.iSales.pages.detailsproduit.DetailsProduitActivity.this);
        task.execute();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Creation fichier de log pour les erreurs
        showLog();

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

        executeFindproductVirtual();

//        Referencement des vues
        activityView = (View) findViewById(R.id.iv_produitdetails_poster);
        mPosterIV = (ImageView) findViewById(R.id.iv_produitdetails_poster);
        mLabelTV = (TextView) findViewById(R.id.tv_produitdetails_label);
        mPdtVirtualRecyclerview = (RecyclerView) findViewById(R.id.recyclerview_produitdetails_virtuals);
        mRefTV = (TextView) findViewById(R.id.tv_produitdetails_ref);
        mPrixHtTV = (TextView) findViewById(R.id.tv_produitdetails_prix_ht);
        mPrixTtcTV = (TextView) findViewById(R.id.tv_produitdetails_prix_ttc);
        mStockTV = (TextView) findViewById(R.id.tv_produitdetails_stock);
        mTvaTV = (TextView) findViewById(R.id.tv_produitdetails_tva);
        mDescriptionTV = (TextView) findViewById(R.id.tv_produitdetails_description);
        mNoteTV = (TextView) findViewById(R.id.tv_produitdetails_note);
        mPriceNature = (TextView) findViewById(R.id.tv_produitdetails_price_nature);
        mQuantiteNature = (TextView) findViewById(R.id.tv_produitdetails_quantite_nature);
        mShoppingFAB = (FloatingActionButton) findViewById(R.id.fab_produitdetails_shopping);
        mQuantiteNumberBtn = (EditText) findViewById(R.id.et_produitdetails_quantite);
        mPriceET = (EditText) findViewById(R.id.et_produitdetails_price);
        mPriceUnitaireET = (EditText) findViewById(R.id.et_produitdetails_price_u);
        mRemiseValeur = (EditText) findViewById(R.id.et_produitdetails_remise_valeur);
        mRemisePourcentage = (EditText) findViewById(R.id.et_produitdetails_remise_percent);

//            fix image in view
        mPosterIV.setAdjustViewBounds(true);
        mPosterIV.setScaleType(ImageView.ScaleType.FIT_XY);

        productVirtualList = new ArrayList<>();

        productVirtualAdapter = new ProductVirtualAdapter(com.iSales.pages.detailsproduit.DetailsProduitActivity.this, productVirtualList, com.iSales.pages.detailsproduit.DetailsProduitActivity.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(com.iSales.pages.detailsproduit.DetailsProduitActivity.this, LinearLayoutManager.HORIZONTAL, false);
        mPdtVirtualRecyclerview.setLayoutManager(mLayoutManager);
        mPdtVirtualRecyclerview.setItemAnimator(new DefaultItemAnimator());
        mPdtVirtualRecyclerview.setAdapter(productVirtualAdapter);

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
        mPriceUnitaireET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                Log.e(TAG, "beforeTextChanged: charSequence=" + charSequence);
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
//                Log.e(TAG, "afterTextChanged: editable=" + editable.toString());

                if (editable.toString().equals("")) {
//                    Log.e(TAG, "afterTextChanged: editable empty");
                    return;
                }

                double newValue = Double.parseDouble(editable.toString().replace(",", "."));

                Log.e(TAG, "afterTextChanged:mPriceUnitaireET newValue=" + newValue + " mProductVirtual=" + mProductVirtual.getLabel());

                if (mProductVirtual.getLabel().toLowerCase().contains("palette")) {
                    double price = newValue * Integer.parseInt(productVirtualList.get(1).getQty()) * Integer.parseInt(mProductVirtual.getQty());
                    mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces("" + price));
                } else {
                    double price = newValue * Integer.parseInt(mProductVirtual.getQty());
                    mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces("" + price));
                }
            }
        });


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

    @Override
    public void onFindProductVirtualCompleted(FindProductVirtualREST findProductVirtualREST) {
        if (findProductVirtualREST != null && findProductVirtualREST.getProductVirtuals() != null) {
            if (findProductVirtualREST.getProductVirtuals().size() > 0) {
//                Log.e(TAG, "onFindProductVirtualCompleted: size="+findProductVirtualREST.getProductVirtuals().size()+
//                        " product_parent_id="+findProductVirtualREST.getProduct_parent_id());

                double price0 = Double.parseDouble(mProduitParcelable.getPrice()) * Integer.parseInt(findProductVirtualREST.getProductVirtuals().get(0).getQty());
                double priceTTC0 = Double.parseDouble(mProduitParcelable.getPrice_ttc()) * Integer.parseInt(findProductVirtualREST.getProductVirtuals().get(0).getQty());
                findProductVirtualREST.getProductVirtuals().get(0).setPrice("" + price0);
                findProductVirtualREST.getProductVirtuals().get(0).setPrice_ttc("" + priceTTC0);

                for (int i = 1; i < findProductVirtualREST.getProductVirtuals().size(); i++) {
                    double price = Double.parseDouble(findProductVirtualREST.getProductVirtuals().get(i - 1).getPrice()) * Integer.parseInt(findProductVirtualREST.getProductVirtuals().get(i).getQty());
                    double priceTTC = Double.parseDouble(findProductVirtualREST.getProductVirtuals().get(i - 1).getPrice_ttc()) * Integer.parseInt(findProductVirtualREST.getProductVirtuals().get(i).getQty());
                    findProductVirtualREST.getProductVirtuals().get(i).setPrice("" + price);
                    findProductVirtualREST.getProductVirtuals().get(i).setPrice_ttc("" + priceTTC);
                }

                productVirtualList.addAll(findProductVirtualREST.getProductVirtuals());

                productVirtualAdapter.notifyDataSetChanged();

                int activePos = productVirtualList.size() >= 1 ? 1 : 0;

                updateProductValues(productVirtualList.get(activePos));
            }
        }
    }

    @Override
    public void onProductVirtualClicked(ProductVirtual productVirtual, int position) {
        Log.e(TAG, "onProductVirtualClicked: position=" + position +
                " label=" + productVirtual.getLabel() +
                " price=" + productVirtual.getPrice() +
                " Qty=" + productVirtual.getQty());

        if (!mProductVirtual.getRowid().equals(productVirtual.getRowid())) {
            updateProductValues(productVirtual);
        }
    }
}
