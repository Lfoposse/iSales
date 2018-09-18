package com.rainbow_cl.i_sales.pages.home.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ProduitsAdapter;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindProductsListener;
import com.rainbow_cl.i_sales.interfaces.ProduitsAdapterListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.pages.home.dialog.FullScreenCatPdtDialog;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindProductsREST;
import com.rainbow_cl.i_sales.remote.model.Product;
import com.rainbow_cl.i_sales.task.FindProductsTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.util.ArrayList;

public class CategoriesFragment extends Fragment implements FindProductsListener, ProduitsAdapterListener, DialogCategorieListener {

    private static final String TAG = CategoriesFragment.class.getSimpleName();

//    task de recuperation des produits
    private FindProductsTask mFindProductsTask = null;

    private EditText mSearchET;
    private RecyclerView mRecyclerViewProduits;
    private ImageView mProgressProduitsTV;
    private TextView mErrorTVProduits, mCategoryTV;
    private ImageButton mShowCategoriesDialog;
    private ImageView mCategoryIV;

//    Adapter des produits
    private ProduitsAdapter mProduitsAdapter;
//    liste des produits affichée sur la vue
    private ArrayList<ProduitParcelable> produitsParcelableList;
//    Categorie des produits selectionée
    private CategorieParcelable mCategorieParcelable;

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

//    Recupération de la liste des produits
    private void executeFindProducts(long categorieId) {
//        masquage du formulaire de connexion
        showProgress(true);

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindProductsTask == null) {

            mFindProductsTask = new FindProductsTask(CategoriesFragment.this, "label", "asc", 100, 0, categorieId);
            mFindProductsTask.execute();
        }
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
//        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        Log.e(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        mRecyclerViewProduits = (RecyclerView) rootView.findViewById(R.id.gridview_produits);
        mProgressProduitsTV = (ImageView) rootView.findViewById(R.id.iv_progress_produits);
        mSearchET = (EditText) rootView.findViewById(R.id.et_search_produits);
        mErrorTVProduits = (TextView) rootView.findViewById(R.id.tv_error_produits);
        mShowCategoriesDialog = (ImageButton) rootView.findViewById(R.id.ib_categories_show);
        mCategoryIV = (ImageView) rootView.findViewById(R.id.iv_selected_categorie);
        mCategoryTV = (TextView) rootView.findViewById(R.id.tv_selected_categorie);

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
                } else {
                    // Scrolling down
//                    Log.e(TAG, "onScrolled: Scrolling down");
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
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mProduitsAdapter.performFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mShowCategoriesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullScreenCatPdtDialog dialog = FullScreenCatPdtDialog.newInstance(CategoriesFragment.this, "product", 0);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, FullScreenCatPdtDialog.TAG);
            }
        });

//        Recupération de la liste des produits sur le serveur
        executeFindProducts(0);

        return rootView;
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onDestroy() {
//        Arret de la tache de recupération des produits a la destruction du fragment
        if (mFindProductsTask != null){
            mFindProductsTask.cancel(true);
            mFindProductsTask = null;
        }
        super.onDestroy();
    }

    @Override
    public void onFindProductsCompleted(FindProductsREST findProductsREST) {
        mFindProductsTask = null;
//        affichage du formulaire de connexion
        showProgress(false);

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findProductsREST == null) {
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findProductsREST.getProducts() == null) {
            if (findProductsREST.getErrorCode() == 404) {
                Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                return;
            }
            if (findProductsREST.getErrorCode() == 401) {
                Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (findProductsREST.getProducts().size() == 0) {
            Toast.makeText(getContext(), getString(R.string.aucun_produit_trouve), Toast.LENGTH_LONG).show();
            return;
        }

//        Affichage de la liste des produits sur la vue
        ArrayList<ProduitParcelable> produitParcelables = new ArrayList<>();
        for (Product productItem : findProductsREST.getProducts()) {
            ProduitParcelable produitParcelable = new ProduitParcelable();
            produitParcelable.setLabel(productItem.getLabel());
            produitParcelable.setPrice(productItem.getPrice());
            produitParcelable.setRef(productItem.getRef());
            produitParcelable.setPoster(new DolPhoto());
            produitParcelable.getPoster().setFilename(ISalesUtility.getImgProduit(productItem.getDescription()));
//            produitParcelable.setPoster(productItem.getPoster());

            produitParcelables.add(produitParcelable);
        }

        this.produitsParcelableList.addAll(produitParcelables);

        this.mProduitsAdapter.notifyDataSetChanged();
//        Log.e(TAG, "onFindProductsCompleted: productSize="+findProductsREST.getProducts().size());
    }

    @Override
    public void onDetailsSelected(ProduitParcelable produitParcelable) {
        Toast.makeText(getContext(), "Detail "+ produitParcelable.getLabel(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onShoppingSelected(ProduitParcelable produitParcelable) {
        Toast.makeText(getContext(), "Shopping "+ produitParcelable.getLabel(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onCategorieSelected(CategorieParcelable categorieParcelable) {
        if (categorieParcelable == null) {
            return;
        }
//        Log.e(TAG, "onCategorieSelected: label="+categorieParcelable.getLabel()+" content="+categorieParcelable.getPoster().getContent());

//        Toast.makeText(getContext(), "Category "+ categorieParcelable.getLabel(), Toast.LENGTH_SHORT).show();

        mCategorieParcelable = categorieParcelable;
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
            mCategoryIV.setBackgroundResource(R.drawable.logo_isales_small);
        }

//        Suppresion des produits dans la vue
        if (this.produitsParcelableList != null) {
            this.produitsParcelableList.clear();
            mProduitsAdapter.notifyDataSetChanged();

        }

//        Mise a jour de la liste des produits
        executeFindProducts(Long.parseLong(mCategorieParcelable.getId()));
    }

    @Override
    public void onDestroyView() {
        cancelFind();
        super.onDestroyView();
    }
}
