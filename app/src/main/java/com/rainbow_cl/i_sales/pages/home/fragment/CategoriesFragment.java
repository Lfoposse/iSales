package com.rainbow_cl.i_sales.pages.home.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ProduitsAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.AppExecutors;
import com.rainbow_cl.i_sales.database.entry.CategorieEntry;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.database.entry.ProduitEntry;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindProductsListener;
import com.rainbow_cl.i_sales.interfaces.ProduitsAdapterListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.pages.addcategorie.AddCategorieActivity;
import com.rainbow_cl.i_sales.pages.detailsproduit.DetailsProduitActivity;
import com.rainbow_cl.i_sales.pages.home.dialog.FullScreenCatPdtDialog;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.Categorie;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.remote.rest.FindCategoriesREST;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;
import com.rainbow_cl.i_sales.task.FindCategorieTask;
import com.rainbow_cl.i_sales.task.FindProductsTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment implements ProduitsAdapterListener, DialogCategorieListener,
        FindProductsListener, FindCategorieListener {

    private static final String TAG = CategoriesFragment.class.getSimpleName();


    //    task de recuperation des produits
    private FindProductsTask mFindProductsTask = null;
    //    task de recuperation des categories
    private FindCategorieTask mFindCategorieTask = null;

    private int mPageCategorie = 0;
    private int mCountRequestPdt = 0;

    ProgressDialog mProgressDialog;

    //    position courante de la requete de recuperation des produit
    private int mCurrentPdtQuery = 0;
    private int mTotalPdtQuery = 0;

    private EditText mSearchET;
    private RecyclerView mRecyclerViewProduits;
    private ImageView mProgressProduitsTV;
    private TextView mErrorTVProduits, mCategoryTV;
    private ImageButton mShowCategoriesDialog, mSearchIB, mSearchCancelIB;
    private ImageView mCategoryIV;

    private FloatingActionMenu mMenuFAM;
    private FloatingActionButton mItemCategorieFAB, mItemRafraichirFAB;

    //    Adapter des produits
    private ProduitsAdapter mProduitsAdapter;
    //    liste des produits affichée sur la vue
    private ArrayList<ProduitParcelable> produitsParcelableList;
    //    Categorie des produits selectionée
    private CategorieParcelable mCategorieParcelable;

    private AppDatabase mDb;

    private int mLimit = 10;
    private long mLastProduitId = 0;

    public CategoriesFragment() {
        // Required empty public constructor
    }

    public static CategoriesFragment newInstance() {
        Log.e(TAG, "newInstance: ");

        Bundle args = new Bundle();
        CategoriesFragment fragment = new CategoriesFragment();
        fragment.setArguments(args);
        return fragment;
    }


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read 695574095 */
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
            File logFile = new File( logDirectory, "categoriefrag_logcat" + System.currentTimeMillis() + ".txt" );

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
    //    Recupération de la liste des produits
    private void executeFindProducts() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        List<CategorieEntry> categorieEntries = mDb.categorieDao().getAllCategories();
//            modification de la position de la requete totale de recupération des produits
        mTotalPdtQuery = categorieEntries.size();

        Log.e(TAG, "executeFindProducts: categorieEntries=" + categorieEntries.size() +
                " mTotalPdtQuery=" + mTotalPdtQuery);

        /*if (categorieEntries.size() <= 0) {

            //        Fermeture du loader
            showProgressDialog(false, null, null);

            Toast.makeText(getContext(), getString(R.string.aucune_categorie_produit), Toast.LENGTH_LONG).show();
            loadProduits(-1);

            return;
        } */
        int i = 0;
        while (i < categorieEntries.size()) {

            CategorieEntry categorieEntry = categorieEntries.get(i);

            Log.e(TAG, "executeFindProducts: mCurrentPdtQuery=" + mCurrentPdtQuery +
                    " categorieID=" + categorieEntry.getId() +
                    " categorieLabel=" + categorieEntry.getLabel());

            FindProductsTask findProductsTask = new FindProductsTask(getContext(), CategoriesFragment.this, "label", "asc", 0, -1, categorieEntry.getId());
            findProductsTask.execute();

            i++;
        }

    }

    //    Recupération de la liste des categories produits
    private void executeFindCategorieProducts() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
        }

        if (mFindCategorieTask == null) {

            Log.e(TAG, "executeFindCategorieProducts: page=" + mPageCategorie);
            mFindCategorieTask = new FindCategorieTask(getContext(), CategoriesFragment.this, "label", "asc", mLimit, mPageCategorie, "product");
            mFindCategorieTask.execute();
        }
    }

    private void loadProduits(long categorieId, String searchString) {
        List<ProduitEntry> produitEntries;
//        Getting the sharedPreference value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String mode = sharedPreferences.getString(getContext().getString(R.string.commande_mode), "online");
        Boolean produitazero = sharedPreferences.getBoolean(getContext().getString(R.string.parametres_produits_azero), false);
//        Log.e(TAG, "loadProduits: produitazero="+produitazero+" produits="+produits.size());

        if (categorieId > 0) {
            if (produitazero){
                if (searchString == null) {
                    produitEntries = mDb.produitDao().getProduitsLimitByCategorieAZero(mLastProduitId, categorieId, mLimit);
                } else {
                    produitEntries = mDb.produitDao().getProduitsLimitByCategorieStrAZero(mLastProduitId, categorieId, mLimit, searchString);
                }
            } else {
                if (searchString == null) {
                            produitEntries = mDb.produitDao().getProduitsLimitByCategorie(mLastProduitId, categorieId, mLimit);
                } else {
                    produitEntries = mDb.produitDao().getProduitsLimitByCategorieStr(mLastProduitId, categorieId, mLimit, searchString);
                }
            }
        } else {
            if (produitazero){
                if (searchString == null) {
                    produitEntries = mDb.produitDao().getProduitsLimitAZero(mLastProduitId, mLimit);
                } else {
                        produitEntries = mDb.produitDao().getProduitsLimitByStrAZero(mLastProduitId, mLimit, searchString);
                }
            } else {
                if (searchString == null) {
                    produitEntries = mDb.produitDao().getProduitsLimit(mLastProduitId, mLimit);
                } else {
                    produitEntries = mDb.produitDao().getProduitsLimitByStr(mLastProduitId, mLimit, searchString);
                }
            }
        }

        if (produitEntries.size() <= 0) {
//        affichage de l'image d'attente
            showProgress(false);

//            Toast.makeText(getContext(), getString(R.string.aucun_produit_trouve), Toast.LENGTH_SHORT).show();

            /*
//        Suppression des images des clients en local
            ISalesUtility.deleteProduitsImgFolder();

            if (categorieId <= 0) {
                Log.e(TAG, "loadProduits: executeFindCategorieProducts");
                executeFindCategorieProducts();
            } */
            return;
        }

//        Affichage de la liste des produits sur la vue
        ArrayList<ProduitParcelable> produitParcelables = new ArrayList<>();
        for (ProduitEntry produitEntry : produitEntries) {
            ProduitParcelable produitParcelable = new ProduitParcelable();
            produitParcelable.setId(produitEntry.getId());
            produitParcelable.setLabel(produitEntry.getLabel());
            produitParcelable.setPrice(produitEntry.getPrice());
            produitParcelable.setPrice_ttc(produitEntry.getPrice_ttc());
            produitParcelable.setRef(produitEntry.getRef());
            produitParcelable.setPoster(new DolPhoto());
            produitParcelable.setStock_reel(produitEntry.getStock_reel());
//            produitParcelable.setDescription(ISalesUtility.getImgProduit(productItem.getDescription()));
            produitParcelable.setDescription(produitEntry.getDescription());
            produitParcelable.getPoster().setFilename(ISalesUtility.getImgProduit(produitEntry.getDescription()));
            produitParcelable.setLocal_poster_path(produitEntry.getFile_content());
            produitParcelable.setCategorie_id(produitEntry.getCategorie_id());
            produitParcelable.setTva_tx(produitEntry.getTva_tx());
            produitParcelable.setNote(produitEntry.getNote());
            produitParcelable.setNote_private(produitEntry.getNote_private());
            produitParcelable.setNote_public(produitEntry.getNote_public());

            produitParcelables.add(produitParcelable);
        }

        Log.e(TAG, "onFindThirdpartieCompleted: mLastClientId=" + mLastProduitId);
//        incrementation du nombre de page
        mLastProduitId = produitEntries.get(produitEntries.size() - 1).getId();

        if (produitsParcelableList.size() > 0 && mLastProduitId <= 0) {
            produitsParcelableList.clear();
        }
        this.produitsParcelableList.addAll(produitParcelables);

        this.mProduitsAdapter.notifyDataSetChanged();

//        affichage de l'image d'attente
        showProgress(false);
    }

    //    arrete le processus de recupération des infos sur le serveur
    private void cancelFind() {
        if (mFindProductsTask != null) {
            mFindProductsTask.cancel(true);
            mFindProductsTask = null;
        }
    }

    /**
     * Shows the progress UI and hides.
     */
    private void showProgress(final boolean show) {

        mProgressProduitsTV.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows the progress UI and hides.
     */
    private void showProgressDialog(boolean show, String title, String message) {

        if (show) {
            mProgressDialog = new ProgressDialog(getContext());
            if (title != null) mProgressDialog.setTitle(title);
            if (message != null) mProgressDialog.setMessage(message);

            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }
    }

    //    insert a movie in database
    public void addPanier(final ProduitParcelable produitParcelable) {
        Log.e(TAG, "addPanier: id" + produitParcelable.getId());
        // get movie in db
        final PanierEntry panierEntryTest = mDb.panierDao().getPanierById(produitParcelable.getId());

//        Teste si le produit n'est pas deja dans le panier
        if (panierEntryTest != null) {
//            Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
            final Snackbar snackbar = Snackbar
                    .make(getView(), String.format("%s existe dans le panier.", produitParcelable.getLabel()), Snackbar.LENGTH_LONG);

// Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(getContext().getResources().getColor(R.color.snackbar_error));
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(getResources().getColor(R.color.white));
            snackbar.show();
            return;
        }

//        Ajout du produit dans la bd locale
        final PanierEntry panierEntry = new PanierEntry();

//        initialisation des valeur du produit a ajouter dans le panier
        panierEntry.setId(produitParcelable.getId());
        panierEntry.setFk_product(produitParcelable.getId());
        panierEntry.setLabel(produitParcelable.getLabel());
        panierEntry.setDescription(produitParcelable.getDescription());
        panierEntry.setPrice(produitParcelable.getPrice());
        panierEntry.setPrice_ttc(produitParcelable.getPrice_ttc());
        panierEntry.setRef(produitParcelable.getRef());
        panierEntry.setPoster_content(produitParcelable.getPoster().getFilename());
        panierEntry.setFile_content(produitParcelable.getLocal_poster_path());
        panierEntry.setStock_reel(produitParcelable.getStock_reel());
        panierEntry.setTva_tx(produitParcelable.getTva_tx());
        panierEntry.setQuantity(1);

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // insert new panier
                mDb.panierDao().insertPanier(panierEntry);
//                Log.e(TAG, "run: addPanier");
//                Toast.makeText(getContext(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Toast.LENGTH_SHORT).show();
                final Snackbar snackbar = Snackbar
                        .make(getView(), String.format("%s ajouté dans le panier.", produitParcelable.getLabel()), Snackbar.LENGTH_LONG);

// Changing action button text color
                View sbView = snackbar.getView();
                TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(getContext().getResources().getColor(R.color.snackbar_success));
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
    public void onFindProductsCompleted(FindProductsREST findProductsREST) {
//            modification de la position de la requete courante de recupération des produits
        mCurrentPdtQuery++;
        Log.e(TAG, "onFindProductsCompleted: FindProductsREST getThirdparties mCurrentPdtQuery="+mCurrentPdtQuery+" mTotalPdtQuery="+mTotalPdtQuery);

        if (findProductsREST != null && findProductsREST.getProducts() != null) {
//            Log.e(TAG, "onFindProductsCompleted: saving product categorie=" + findProductsREST.getCategorie_id() + " pdtSize=" + findProductsREST.getProducts().size());
            for (Product productItem : findProductsREST.getProducts()) {
                Log.e(TAG, "onFindProductsCompleted: tva_tx=" + productItem.getTva_tx());
                ProduitEntry produitEntry = new ProduitEntry();
                produitEntry.setId(Long.parseLong(productItem.getId()));
                produitEntry.setCategorie_id(findProductsREST.getCategorie_id());
                produitEntry.setLabel(productItem.getLabel());
                produitEntry.setPrice(productItem.getPrice());
                produitEntry.setPrice_ttc(productItem.getPrice_ttc());
                produitEntry.setRef(productItem.getRef());
                produitEntry.setStock_reel(productItem.getStock_reel());
                produitEntry.setDescription(productItem.getDescription());
                produitEntry.setTva_tx(productItem.getTva_tx());
                produitEntry.setNote(productItem.getNote());
                produitEntry.setNote_public(productItem.getNote_public());
                produitEntry.setNote_private(productItem.getNote_private());

//                Log.e(TAG, "onFindProductsCompleted: product name=" + produitEntry.getLabel());
                if (mDb.produitDao().getProduitById(produitEntry.getId()) == null) {
//                    Log.e(TAG, "onFindThirdpartieCompleted: insert produitEntry");
//            insertion du client dans la BD
                    mDb.produitDao().insertProduit(produitEntry);
                } else {
//                    Log.e(TAG, "onFindThirdpartieCompleted: update produitEntry");
//            mise a jour du client dans la BD
                    mDb.produitDao().updateProduit(produitEntry);
                }
            }
            Log.e(TAG, "onFindProductsCompleted: mPage=" + mCurrentPdtQuery);

            if (mCurrentPdtQuery >= mTotalPdtQuery - 1) {

//        Fermeture du loader
                //        Fermeture du loader
                showProgressDialog(false, null, null);

                Toast.makeText(getContext(), getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();
                loadProduits(-1, null);

                return;
            }
        } else {

            Log.e(TAG, "onFindProductsCompleted: FindProductsREST getThirdparties null");

            if (mCurrentPdtQuery >= mTotalPdtQuery - 1) {

//        Fermeture du loader
                //        Fermeture du loader
                showProgressDialog(false, null, null);

                Toast.makeText(getContext(), getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();
                loadProduits(-1, null);

                return;
            }

        }

    }

    @Override
    public void onFindCategorieCompleted(FindCategoriesREST findCategoriesREST) {
        mFindCategorieTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findCategoriesREST == null) {
            //        Fermeture du loader
            showProgressDialog(false, null, null);
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findCategoriesREST.getCategories() == null) {
            Log.e(TAG, "onFindCategorieCompleted: findCategoriesREST findCategoriesREST null");
//            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageCategorie = 0;
//            Toast.makeText(SynchronisationActivity.this, getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();

            executeFindProducts();
//            loadProduits(-1);


            return;
        }

        for (Categorie categorieItem : findCategoriesREST.getCategories()) {
            CategorieEntry categorieEntry = new CategorieEntry();
            categorieEntry.setId(Long.parseLong(categorieItem.getId()));
            categorieEntry.setLabel(categorieItem.getLabel());
            categorieEntry.setDescription(categorieItem.getDescription());
            categorieEntry.setPoster_name(ISalesUtility.getImgProduit(categorieItem.getDescription()));

            if (mDb.categorieDao().getCategorieById(categorieEntry.getId()) == null) {
                Log.e(TAG, "onFindThirdpartieCompleted: insert categorieEntry");
//            insertion du client dans la BD
                mDb.categorieDao().insertCategorie(categorieEntry);
            } else {
                Log.e(TAG, "onFindThirdpartieCompleted: update categorieEntry");
//            mise a jour du client dans la BD
                mDb.categorieDao().updateCategorie(categorieEntry);
            }
        }
        Log.e(TAG, "onFindCategorieCompleted: mPage=" + mPageCategorie);

        mPageCategorie++;
        executeFindCategorieProducts();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        showLog();

        mDb = AppDatabase.getInstance(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        setHasOptionsMenu(true);

        mRecyclerViewProduits = (RecyclerView) rootView.findViewById(R.id.gridview_produits);
        mProgressProduitsTV = (ImageView) rootView.findViewById(R.id.iv_progress_produits);
        mSearchET = (EditText) rootView.findViewById(R.id.et_search_produits);
        mSearchIB = (ImageButton) rootView.findViewById(R.id.imgbtn_search_produit);
        mSearchCancelIB = (ImageButton) rootView.findViewById(R.id.imgbtn_search_produit_cancel);
        mErrorTVProduits = (TextView) rootView.findViewById(R.id.tv_error_produits);
        mShowCategoriesDialog = (ImageButton) rootView.findViewById(R.id.ib_categories_show);
        mCategoryIV = (ImageView) rootView.findViewById(R.id.iv_selected_categorie);
        mCategoryTV = (TextView) rootView.findViewById(R.id.tv_selected_categorie);

        mMenuFAM = (FloatingActionMenu) rootView.findViewById(R.id.fab_menu_categorie);
        mItemCategorieFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_item_categorieproduit);
        mItemRafraichirFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_item_rafraichir_produits);

        produitsParcelableList = new ArrayList<>();

        this.mProduitsAdapter = new ProduitsAdapter(getContext(), produitsParcelableList, CategoriesFragment.this);

        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), ISalesUtility.calculateNoOfColumns(getContext()));
        mRecyclerViewProduits.setLayoutManager(mLayoutManager);
        mRecyclerViewProduits.setItemAnimator(new DefaultItemAnimator());
        mRecyclerViewProduits.setAdapter(mProduitsAdapter);
        mRecyclerViewProduits.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
//                    Log.e(TAG, "onScrolled: Scrolling up");
                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

                    int itemsCount = recyclerView.getAdapter().getItemCount();

//                    Log.e(TAG, "onScroll: lastPosition=" + lastPosition + " itemsCount=" + itemsCount);
                    if (lastPosition > 0 && (lastPosition + 2) >= itemsCount) {
                        long idCategorie = 0;
                        if (mCategorieParcelable != null)
                            idCategorie = Long.parseLong(mCategorieParcelable.getId());
//                        executeFindProducts(idCategorie);
                        loadProduits(idCategorie, null);
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
//                    Log.e(TAG, "onScrolled: SCROLL_STATE_FLING");
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
//                    Log.e(TAG, "onScrolled: SCROLL_STATE_TOUCH_SCROLL");
                } else {
                    // Do something
//                    Log.e(TAG, "onScrolled: else");
                }
            }
        });

//        ecoute de la recherche d'un client
        mSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();

                loadProduits(-1, searchString);
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mProduitsAdapter.performFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

                Log.e(TAG, "afterTextChanged: string="+editable.toString() );
                if (editable.toString().equals("")) {
                    mSearchCancelIB.setVisibility(View.GONE);
                    mSearchIB.setVisibility(View.VISIBLE);
                } else {
                    mSearchCancelIB.setVisibility(View.VISIBLE);
                    mSearchIB.setVisibility(View.GONE);
                }
            }
        });
        mSearchCancelIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSearchET.setText("");
            }
        });
        mShowCategoriesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullScreenCatPdtDialog dialog = FullScreenCatPdtDialog.newInstance(CategoriesFragment.this, "product");
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, FullScreenCatPdtDialog.TAG);
            }
        });

//        ecoute du click pour ajout d'une categorie de produit
        mItemCategorieFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddCategorieActivity.class);
                startActivity(intent);
                mMenuFAM.close(true);
            }
        });

//        ecoute du click pour rafraichir la liste des clients
        mItemRafraichirFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetProductsList();

//        recuperation des clients sur le serveur
//                executeFindProducts(Long.parseLong(mCategorieParcelable.getId()));
                loadProduits(Long.parseLong(mCategorieParcelable.getId()), null);
                mMenuFAM.close(true);
            }
        });

//        Recupération de la liste des produits sur le serveur
//        executeFindProducts(0);
        loadProduits(0, null);

        return rootView;
    }

    private void resetProductsList() {
        this.produitsParcelableList.clear();
        mProduitsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frag_categorie_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//        redirige le user vers la page de synchronisation
            case R.id.action_fragcategorie_sync:
                /*
                Intent intent = new Intent(getContext(), SynchronisationActivity.class);
                startActivity(intent); */

//        Si le téléphone n'est pas connecté
                if (!ConnectionManager.isPhoneConnected(getContext())) {
                    Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                    return true;
                }

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_produits_encours));

                mDb.categorieDao().deleteAllCategorie();
                mDb.produitDao().deleteAllProduit();
//        Suppression des images des clients en local
                ISalesUtility.deleteProduitsImgFolder();

//            recupere la liste des clients sur le serveur
                executeFindCategorieProducts();
                return true;
            default:
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();

        if (produitsParcelableList.size() <= 0) {
            loadProduits(mCategorieParcelable != null ? Long.parseLong(mCategorieParcelable.getId()) : -1, null);
        } else {
            mProduitsAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDestroy() {
//        Arret de la tache de recupération des produits a la destruction du fragment
        if (mFindProductsTask != null) {
            mFindProductsTask.cancel(true);
            mFindProductsTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onDetailsSelected(ProduitParcelable produitParcelable) {
//        Toast.makeText(getContext(), "Detail "+ produitParcelable.getLabel(), Toast.LENGTH_SHORT).show();
//        Log.e(TAG, "onDetailsSelected: "+produitParcelable.getDescription());

        Intent intent = new Intent(getContext(), DetailsProduitActivity.class);
        intent.putExtra("produit", produitParcelable);
        startActivity(intent);
    }

    @Override
    public void onShoppingSelected(ProduitParcelable produitParcelable, int position) {

        addPanier(produitParcelable);
    }

    @Override
    public void onCategorieDialogSelected(CategorieParcelable categorieParcelable) {
        if (categorieParcelable == null) {
            return;
        }
        else if (categorieParcelable.getId().equals("-1")) {
            mCategorieParcelable = null;
//        modification du label de la categorie
            mCategoryTV.setText(getContext().getResources().getString(R.string.toutes_les_categories));
//            chargement de la photo par defaut dans la vue
            mCategoryIV.setBackgroundResource(R.drawable.ic_view_list);

            loadProduits(0, null);
            return;
        }
//        Log.e(TAG, "onCategorieAdapterSelected: label="+categorieParcelable.getLabel()+" content="+categorieParcelable.getPoster().getContent());

//        Toast.makeText(getContext(), "Category "+ categorieParcelable.getLabel(), Toast.LENGTH_SHORT).show();

        mCategorieParcelable = categorieParcelable;

//        reinitialisation du nombre de page
        mLastProduitId = 0;

//        modification du label de la categorie
        mCategoryTV.setText(mCategorieParcelable.getLabel().toUpperCase());

        if (mCategorieParcelable.getPoster().getContent() != null) {
//            conversion de la photo du Base64 en bitmap
            byte[] decodedString = Base64.decode(mCategorieParcelable.getPoster().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                    chargement de la photo dans la vue
            mCategoryIV.setBackground(new BitmapDrawable(decodedByte));

        } else {
//            chargement de la photo par defaut dans la vue
            mCategoryIV.setBackgroundResource(R.drawable.isales_no_image);
        }

//        Suppresion des produits dans la vue
        if (this.produitsParcelableList != null) {
            resetProductsList();

        }

//        Mise a jour de la liste des produits
        loadProduits(Long.parseLong(mCategorieParcelable.getId()), null);
    }

    @Override
    public void onDestroyView() {
        cancelFind();
        super.onDestroyView();
    }
}
