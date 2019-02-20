package com.rainbow_cl.i_sales.pages.detailsproduit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.rainbow_cl.i_sales.adapter.ProductVirtualAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.AppExecutors;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.interfaces.FindProductVirtualListener;
import com.rainbow_cl.i_sales.interfaces.ProductVirtualAdapterListener;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.model.ProductVirtual;
import com.rainbow_cl.i_sales.remote.rest.FindProductVirtualREST;
import com.rainbow_cl.i_sales.task.FindProductVirtualTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.rainbow_cl.i_sales.utility.InputFilterMinMax;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailsProduitActivity extends AppCompatActivity implements FindProductVirtualListener, ProductVirtualAdapterListener {
    private static final String TAG = DetailsProduitActivity.class.getSimpleName();

    private View activityView;
    private TextView mLabelTV, mRefTV, mPrixHtTV, mPrixTtcTV, mStockTV, mTvaTV, mDescriptionTV, mNoteTV, mPriceNature, mQuantiteNature;
    private ImageView mPosterIV;
    private EditText mQuantiteNumberBtn;
    private EditText mPriceET;
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
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
    public void showLog() {

        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/iSalesLog" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "details_produit_logcat" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
            Log.e(TAG, "onCreate: isExternalStorageReadable" );
        } else {
            // not accessible
            Log.e(TAG, "onCreate: non isExternalStorageReadable" );
        }
    }
    public void initValues() {
        mProductVirtual = new ProductVirtual();
        mProductVirtual.setRowid(""+mProduitParcelable.getId());
        mProductVirtual.setFk_product_fils(""+mProduitParcelable.getId());
        mProductVirtual.setFk_product_pere(""+mProduitParcelable.getId());
        mProductVirtual.setQty("1");
        mProductVirtual.setRef(mProduitParcelable.getRef());
        mProductVirtual.setDatec(mProduitParcelable.getDate_creation());
        mProductVirtual.setLabel(mProduitParcelable.getLabel()+" UNITÉ");
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
        mProductVirtual.setStock(""+mProduitParcelable.getStock_reel());
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
        mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces(mProduitParcelable.getPrice()));
        mDescriptionTV.setText(ISalesUtility.getDescProduit(mProduitParcelable.getDescription()));
        mNoteTV.setText(mProduitParcelable.getNote());
        mQuantiteNumberBtn.setText("0");

        String[] label = mProductVirtual.getLabel().split(" ");

        mPriceNature.setText(String.format("/ %s", label[label.length-1]));
        mQuantiteNature.setText(String.format("%s(S)", label[label.length-1]));

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

//        Log.e(TAG, "onBindViewHolder: downloadLinkImg="+ApiUtils.getDownloadImg(mContext, module_part, original_file));
        Picasso.with(DetailsProduitActivity.this)
                .load(ApiUtils.getDownloadProductImg(DetailsProduitActivity.this, mProduitParcelable.getRef()))
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

    private void updateProductValues(ProductVirtual productVirtual) {
        mProductVirtual = productVirtual;
        Log.e(TAG, "updateProductValues: qty="+mProductVirtual.getQty());

        mPrixHtTV.setText(String.format("%s %s HT",
                ISalesUtility.amountFormat2(mProductVirtual.getPrice()),
                ISalesUtility.CURRENCY));
        mPrixTtcTV.setText(String.format("%s %s TTC",
                ISalesUtility.amountFormat2(mProductVirtual.getPrice_ttc()),
                ISalesUtility.CURRENCY));
        mStockTV.setText(String.format("%s", mProductVirtual.getStock()));
        mTvaTV.setText(String.format("%s %s", ISalesUtility.amountFormat2(mProductVirtual.getTva_tx()), "%"));
        mPriceET.setText(ISalesUtility.roundOffTo2DecPlaces(mProductVirtual.getPrice()));

        String[] label = mProductVirtual.getLabel().split(" ");

        mPriceNature.setText(String.format("/ %s", label[label.length-1]));
        mQuantiteNature.setText(String.format("%s(S)", label[label.length-1]));
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

        double prix = Double.parseDouble(mPriceET.getText().toString().replace(",", "."));
        if (prix < Double.parseDouble(mProductVirtual.getPrice())) {
            mPriceET.setError(getString(R.string.prix_vente_trop_petit));
            focusView = mPriceET;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }

        double prix_u = prix / Integer.parseInt(mProductVirtual.getQty());
        int quantite = Integer.parseInt(mQuantiteNumberBtn.getText().toString()) * Integer.parseInt(mProductVirtual.getQty());
        Log.e(TAG, "addPanier: id = " + mProduitParcelable.getId() + " quantite" + quantite + " prix_u" + prix_u);

        // get movie in db
//        final PanierEntry panierEntryTest = mDb.panierDao().getPanierById(mProduitParcelable.getId());
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

//                double price = Double.parseDouble(priceString.replace(',', '.'));
                double tva = Double.parseDouble(mProduitParcelable.getTva_tx());
                double pricettc = prix_u + ((prix_u * tva)/100);

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

//        double price = Double.parseDouble(priceString.replace(',', '.'));
        double tva = Double.parseDouble(mProduitParcelable.getTva_tx());

        Log.e(TAG, "addPanier: priceString=" + priceString + " price=" + prix_u + " fk_product=" + mProduitParcelable.getId());
//        if (prix_u >= Double.parseDouble(mProduitParcelable.getPrice())) {
            double pricettc = prix_u + ((prix_u * tva)/100);

            panierEntry.setPrice("" + prix_u);
            panierEntry.setPrice_ttc("" + pricettc);
        /*} else {
            double price2 = Double.parseDouble(mProduitParcelable.getPrice().replace(',', '.'));
            double pricettc = price2 + ((price2 * tva)/100);

            panierEntry.setPrice("" + price2);
            panierEntry.setPrice_ttc("" + pricettc);
        }*/

//        initialisation des valeur du produit a ajouter dans le panier
        panierEntry.setId(mProduitParcelable.getId());
        panierEntry.setFk_product(mProduitParcelable.getId());
        panierEntry.setLabel(mProduitParcelable.getLabel());
        panierEntry.setDescription(mProduitParcelable.getDescription());
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

    private void executeFindproductVirtual() {
        FindProductVirtualTask task = new FindProductVirtualTask(DetailsProduitActivity.this, mProduitParcelable.getId(), DetailsProduitActivity.this);
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

        productVirtualList = new ArrayList<>();

        productVirtualAdapter = new ProductVirtualAdapter(DetailsProduitActivity.this, productVirtualList, DetailsProduitActivity.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(DetailsProduitActivity.this, LinearLayoutManager.HORIZONTAL, false);
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
        if (findProductVirtualREST.getProductVirtuals() != null) {
            if (findProductVirtualREST.getProductVirtuals().size() > 0) {
//                Log.e(TAG, "onFindProductVirtualCompleted: size="+findProductVirtualREST.getProductVirtuals().size()+
//                        " product_parent_id="+findProductVirtualREST.getProduct_parent_id());

                productVirtualList.addAll(findProductVirtualREST.getProductVirtuals());

                productVirtualAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onProductVirtualClicked(ProductVirtual productVirtual, int position) {
        Log.e(TAG, "onProductVirtualClicked: position="+position+
                " label="+productVirtual.getLabel()+
                " Qty="+productVirtual.getQty());

        if (!mProductVirtual.getRowid().equals(productVirtual.getRowid())) {
            updateProductValues(productVirtual);
        }
    }
}
