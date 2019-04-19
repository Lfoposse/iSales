package com.iSales.pages.home.fragment;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.iSales.R;
import com.iSales.adapter.CategorieProduitAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.CategorieEntry;
import com.iSales.decoration.MyDividerItemDecoration;
import com.iSales.interfaces.CategorieProduitAdapterListener;
import com.iSales.interfaces.DialogCategorieListener;
import com.iSales.interfaces.FindCategorieListener;
import com.iSales.interfaces.FindProductsListener;
import com.iSales.model.CategorieParcelable;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Categorie;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.rest.FindCategoriesREST;
import com.iSales.remote.rest.FindProductsREST;
import com.iSales.task.FindCategorieTask;
import com.iSales.utility.ISalesUtility;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CategorieProduitFragment extends Fragment implements FindCategorieListener, FindProductsListener, CategorieProduitAdapterListener {
    public static String TAG = com.iSales.pages.home.fragment.CategorieProduitFragment.class.getSimpleName();

    //    views
    private RecyclerView mrecyclerView;
    private ImageView mProgressIV;
    private TextView mErrorTV;
    private EditText mSearchET;
    private ImageButton mSearchIB, mSearchCancelIB;
    private View mToutescategorie;

    //    Adapter des produits
    private CategorieProduitAdapter mCategorieAdapter;
    //    Liste des categories affichées sur la vue
    private ArrayList<CategorieParcelable> categorieParcelableList;

    //    asynctask
    private FindCategorieTask mFindCategorieTask = null;

    //    Listener de sortie apres selection d'une categorie
    private static DialogCategorieListener dialogCategorieListener;

    //    Parametre de recuperation de la liste des categories
    private static String mType = "";
    private static int mPage = 0;

    private AppDatabase mDb;

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read 695574095 */
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
            File logFile = new File(logDirectory, "categorieproduit_logcat" + System.currentTimeMillis() + ".txt");

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

    //    Recupération de la liste des categories produits
    private void executeFindCategorieProducts() {
//        masquage du formulaire de connexion
        showProgress(true);

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindCategorieTask == null) {

            Log.e(TAG, "executeFindCategorieProducts: type=" + mType + " page=" + mPage);
            mFindCategorieTask = new FindCategorieTask(getContext(), com.iSales.pages.home.fragment.CategorieProduitFragment.this, "label", "asc", 100, mPage, mType);
            mFindCategorieTask.execute();
        }
    }

    private void loadCategories() {

//        Getting the sharedPreference value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String mode = sharedPreferences.getString(getContext().getString(R.string.commande_mode), "online");
        Boolean catazero = sharedPreferences.getBoolean(getContext().getString(R.string.parametres_categories_azero), false);
        Log.e(TAG, "loadCategories: catazero=" + catazero);

        List<CategorieEntry> categorieEntries;
        List<CategorieEntry> categories = mDb.categorieDao().getCategories();
        if (catazero) {
            categorieEntries = mDb.categorieDao().getAllCategoriesAZero();
        } else {
            categorieEntries = mDb.categorieDao().getAllCategories();
        }
        Log.e(TAG, "loadCategories: categorieEntries=" + categories.size());

        if (categorieEntries.size() <= 0) {
            Toast.makeText(getContext(), getString(R.string.aucune_categorie_trouve), Toast.LENGTH_LONG).show();
//        affichage de l'image d'attente
            showProgress(false);
            return;
        }

//        Affichage de la liste des categories sur la vue
        ArrayList<CategorieParcelable> categorieParcelables = new ArrayList<>();
        for (CategorieEntry categorieEntry : categorieEntries) {
            CategorieParcelable categorieParcelable = new CategorieParcelable();
            categorieParcelable.setId("" + categorieEntry.getId());
            categorieParcelable.setLabel(categorieEntry.getLabel());
            categorieParcelable.setPoster(new DolPhoto());
            categorieParcelable.setCount_produits(categorieEntry.getCount_produits());
            categorieParcelable.getPoster().setFilename(ISalesUtility.getImgProduit(categorieEntry.getDescription()));
//            produitParcelable.setPoster(productItem.getPoster());

            categorieParcelables.add(categorieParcelable);
        }
        if (this.categorieParcelableList != null) {
            this.categorieParcelableList.clear();

        }
        this.categorieParcelableList.addAll(categorieParcelables);

        this.mCategorieAdapter.notifyDataSetChanged();

//        affichage de l'image d'attente
        showProgress(false);
    }

    /**
     * Shows the progress UI and hides.
     */
    private void showProgress(final boolean show) {

        mProgressIV.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onFindCategorieCompleted(FindCategoriesREST findCategoriesREST) {
        mFindCategorieTask = null;
//        affichage du formulaire de connexion
        showProgress(false);

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findCategoriesREST == null) {
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findCategoriesREST.getCategories() == null) {
            if (findCategoriesREST.getErrorCode() == 404) {
                Toast.makeText(getContext(), getString(R.string.aucune_categorie_trouve), Toast.LENGTH_LONG).show();
                return;
            }
            if (findCategoriesREST.getErrorCode() == 401) {
                Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (findCategoriesREST.getCategories().size() == 0) {
            Toast.makeText(getContext(), getString(R.string.aucune_categorie_trouve), Toast.LENGTH_LONG).show();
            return;
        }


//        Affichage de la liste des categories sur la vue
        ArrayList<CategorieParcelable> categorieParcelables = new ArrayList<>();
        for (Categorie categorieItem : findCategoriesREST.getCategories()) {
            CategorieParcelable categorieParcelable = new CategorieParcelable();
            categorieParcelable.setId(categorieItem.getId());
            categorieParcelable.setLabel(categorieItem.getLabel());
            categorieParcelable.setPoster(new DolPhoto());
            categorieParcelable.getPoster().setFilename(ISalesUtility.getImgProduit(categorieItem.getDescription()));
//            produitParcelable.setPoster(productItem.getPoster());

            categorieParcelables.add(categorieParcelable);
        }

        if (this.categorieParcelableList != null) {
            this.categorieParcelableList.clear();

        }
        this.categorieParcelableList.addAll(categorieParcelables);

        this.mCategorieAdapter.notifyDataSetChanged();
        Log.e(TAG, "onFindCategorieCompleted: categorieSize=" + findCategoriesREST.getCategories().size());

    }

    //    Recupération de la categorie sélectionnée
    @Override
    public void onCategorieAdapterSelected(CategorieParcelable categorieParcelable) {
//        Log.e(TAG, "onCategorieAdapterSelected: label="+categorieParcelable.getLabel()+" content="+categorieParcelable.getPoster().getContent());
        dialogCategorieListener.onCategorieDialogSelected(categorieParcelable);
    }

    public static com.iSales.pages.home.fragment.CategorieProduitFragment newInstance(DialogCategorieListener onDialogCategorieListener, String type) {
//        passage des parametres de la requete au fragment
        mType = type;
        Bundle args = new Bundle();

        com.iSales.pages.home.fragment.CategorieProduitFragment fragment = new com.iSales.pages.home.fragment.CategorieProduitFragment();
        dialogCategorieListener = onDialogCategorieListener;
        fragment.setArguments(args);
        return fragment;
    }

    public CategorieProduitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showLog();
        mDb = AppDatabase.getInstance(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_categorie_produit, container, false);

        mToutescategorie = (RelativeLayout) view.findViewById(R.id.view_catpdt_all);
        mrecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_catpdt);
        mProgressIV = (ImageView) view.findViewById(R.id.iv_progress_catpdt);
        mErrorTV = (TextView) view.findViewById(R.id.tv_error_catpdt);
        mSearchET = (EditText) view.findViewById(R.id.et_search_catpdt);
        mSearchIB = (ImageButton) view.findViewById(R.id.imgbtn_search_catpdt);
        mSearchCancelIB = (ImageButton) view.findViewById(R.id.imgbtn_search_catpdt_cancel);

//        initialisation de la liste des categories
        categorieParcelableList = new ArrayList<>();

        mCategorieAdapter = new CategorieProduitAdapter(getContext(), categorieParcelableList, com.iSales.pages.home.fragment.CategorieProduitFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mrecyclerView.setLayoutManager(mLayoutManager);
        mrecyclerView.setItemAnimator(new DefaultItemAnimator());
        mrecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 66));
        mrecyclerView.setAdapter(mCategorieAdapter);

//        ecoute de la recherche d'un client
        mSearchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mCategorieAdapter.performFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

//                Log.e(TAG, "afterTextChanged: string="+editable.toString() );
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
        mToutescategorie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CategorieParcelable categorieParcelable = new CategorieParcelable();
                categorieParcelable.setId("-1");
                dialogCategorieListener.onCategorieDialogSelected(categorieParcelable);
            }
        });

//        Recupération de la liste des categorie produits sur le serveur
//        executeFindCategorieProducts();
        loadCategories();

        return view;
    }

    @Override
    public void onDestroy() {
//        Arret de la tache de recupération des categories produits a la destruction de la boite de dialogue
        if (mFindCategorieTask != null) {
            mFindCategorieTask.cancel(true);
            mFindCategorieTask = null;
        }

        super.onDestroy();
    }

    @Override
    public void onFindProductsCompleted(FindProductsREST findProductsREST) {
        Log.e(TAG, "onFindProductsCompleted: in categorieProdutiFragment");
//        Recupération de la liste des categorie produits sur le serveur
        loadCategories();
    }

    @Override
    public void onFindAllProductsCompleted() {
        loadCategories();
    }
}
