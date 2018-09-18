package com.rainbow_cl.i_sales.pages.home.dialog;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
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
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.CategorieProduitAdapter;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.interfaces.CategorieProduitAdapterListener;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindCategorieListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.Categorie;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindCategoriesREST;
import com.rainbow_cl.i_sales.task.FindCategorieTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.util.ArrayList;

/**
 * Created by netserve on 04/09/2018.
 */

public class FullScreenCatPdtDialog extends DialogFragment implements FindCategorieListener, CategorieProduitAdapterListener{
    public static String TAG = FullScreenCatPdtDialog.class.getSimpleName();

//    views
    private ImageButton ibClose;
    private RecyclerView mrecyclerView;
    private ImageView mProgressIV;
    private TextView mErrorTV;
    private EditText mSearchET;

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

    //    Recupération de la liste des categories produits
    private void executeFindCategorieProducts() {
//        masquage du formulaire de connexion
        showProgress(true);

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindCategorieTask == null) {

            Log.e(TAG, "executeFindCategorieProducts: type="+mType+" page="+mPage);
            mFindCategorieTask = new FindCategorieTask(FullScreenCatPdtDialog.this, "label", "asc", 100, mPage, mType);
            mFindCategorieTask.execute();
        }
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
        Log.e(TAG, "onFindCategorieCompleted: categorieSize="+findCategoriesREST.getCategories().size());

    }

//    Recupération de la categorie sélectionnée
    @Override
    public void onCategorieSelected(CategorieParcelable categorieParcelable) {
//        Log.e(TAG, "onCategorieSelected: label="+categorieParcelable.getLabel()+" content="+categorieParcelable.getPoster().getContent());
        dialogCategorieListener.onCategorieSelected(categorieParcelable);

//        exit dialog
        dismiss();
    }

    public FullScreenCatPdtDialog() {
    }

    public static FullScreenCatPdtDialog newInstance(DialogCategorieListener onDialogCategorieListener, String type, int page) {
//        passage des parametres de la requete au fragment
        mType = type;
        mPage = page;
        Bundle args = new Bundle();

        FullScreenCatPdtDialog fragment = new FullScreenCatPdtDialog();
        dialogCategorieListener = onDialogCategorieListener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_categories_produits, container, false);

        ibClose = (ImageButton) view.findViewById(R.id.ib_dialog_catpdt_close);
        mrecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_catpdt);
        mProgressIV = (ImageView) view.findViewById(R.id.iv_progress_catpdt);
        mErrorTV = (TextView) view.findViewById(R.id.tv_error_catpdt);
        mSearchET = (EditText) view.findViewById(R.id.et_search_catpdt);

//        initialisation de la liste des categories
        categorieParcelableList = new ArrayList<>();

        mCategorieAdapter = new CategorieProduitAdapter(getContext(), categorieParcelableList, FullScreenCatPdtDialog.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mrecyclerView.setLayoutManager(mLayoutManager);
        mrecyclerView.setItemAnimator(new DefaultItemAnimator());
        mrecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
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

            }
        });

//        Close the modal
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCategorieListener.onCategorieSelected(null);
                dismiss();
            }
        });

//        Recupération de la liste des categorie produits sur le serveur
        executeFindCategorieProducts();

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
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
}
