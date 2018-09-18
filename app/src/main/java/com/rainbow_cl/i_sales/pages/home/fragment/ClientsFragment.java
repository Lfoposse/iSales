package com.rainbow_cl.i_sales.pages.home.fragment;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ClientsAdapter;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.interfaces.ClientsAdapterListener;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindThirdpartieListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.pages.addcustomer.AddCustomerActivity;
import com.rainbow_cl.i_sales.pages.home.dialog.FullScreenCatPdtDialog;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;
import com.rainbow_cl.i_sales.task.FindThirdpartieTask;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ClientsFragment extends Fragment implements ClientsAdapterListener, FindThirdpartieListener, DialogCategorieListener {
    private static final String TAG = ClientsFragment.class.getSimpleName();

//    elements de la vue
    private RecyclerView mRecyclerView;
    private ImageView mProgressIV, mCategoryIV;
    private EditText searchET;
    private FloatingActionButton mAddClientFB;
    private ImageButton mShowCategoriesDialog;
    private TextView mCategoryTV;

    private ArrayList<ClientParcelable> clientParcelableList;
    private ClientsAdapter mAdapter;

    //    task de recuperation des clients
    private FindThirdpartieTask mFindClientTask = null;

    //    Recupération de la liste des produits
    private void executeFindClients() {
//        masquage du formulaire de connexion
        showProgress(true);

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindClientTask == null) {

            mFindClientTask = new FindThirdpartieTask(ClientsFragment.this, 100, 0, ApiUtils.THIRDPARTIE_CLIENT);
            mFindClientTask.execute();
        }
    }

    //    arrete le processus de recupération des infos sur le serveur
    private void cancelFind() {
        if (mFindClientTask != null) {
            mFindClientTask.cancel(true);
            mFindClientTask = null;
        }
    }
    /**
     * Shows the progress UI and hides.
     */
    private void showProgress(final boolean show) {

        mProgressIV.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClientsSelected(ClientParcelable clientParcelable) {
        Toast.makeText(getContext(), "Selected: " + clientParcelable.getName() + ", " + clientParcelable.getAddress(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST) {
        mFindClientTask = null;
//        affichage du formulaire de connexion
        showProgress(false);

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findThirdpartieREST == null) {
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findThirdpartieREST.getThirdparties() == null && getContext() != null) {
            if (findThirdpartieREST.getErrorCode() == 404) {
                Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                return;
            }
            if (findThirdpartieREST.getErrorCode() == 401) {
                Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                return;
            } else {
                Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                return;
            }
        }
        if (findThirdpartieREST.getThirdparties() != null
                && findThirdpartieREST.getThirdparties().size() == 0
                && getContext() != null) {
            Toast.makeText(getContext(), getString(R.string.aucun_produit_trouve), Toast.LENGTH_LONG).show();
            return;
        }

//        Affichage de la liste des produits sur la vue
        ArrayList<ClientParcelable> clientParcelables = new ArrayList<>();
        for (Thirdpartie thirdpartie : findThirdpartieREST.getThirdparties()) {
            ClientParcelable clientParcelable = new ClientParcelable();
            clientParcelable.setName(thirdpartie.getName());
            clientParcelable.setAddress(thirdpartie.getAddress());
            clientParcelable.setLogo(thirdpartie.getLogo());
            clientParcelable.setId(Long.parseLong(thirdpartie.getId()));
//            initialisation du poster du client
            clientParcelable.setPoster(new DolPhoto());
//            produitParcelable.setPoster_name(ISalesUtility.getImgProduit(productItem.getDescription()));

            clientParcelables.add(clientParcelable);
        }

        clientParcelableList.addAll(clientParcelables);

        mAdapter.notifyDataSetChanged();
//        Log.e(TAG, "onFindProductsCompleted: productSize="+findProductsREST.getProducts().size());

    }

    @Override
    public void onCategorieSelected(CategorieParcelable categorieParcelable) {

    }

    /**
     * fetches json by making http calls
     */
    private void fetchContacts() {
        ArrayList<ClientParcelable> clientParcelables = new ArrayList<>();
        clientParcelables.add(new ClientParcelable("Bernard garnier", "Carrefour, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Bonnet Duvaal", "Spar, Marseille", "thumb"));
        clientParcelables.add(new ClientParcelable("David Dumond", "Bio c' bon, Marseille", "thumb"));
        clientParcelables.add(new ClientParcelable("Dubois faure", "City, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Martin Legrand", "Naturalia, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Simon Perrin", "Casino, Nice", "thumb"));
        clientParcelables.add(new ClientParcelable("Bernard garnier", "Carrefour, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Bonnet Duvaal", "Spar, Marseille", "thumb"));
        clientParcelables.add(new ClientParcelable("David Dumond", "Bio c' bon, Marseille", "thumb"));
        clientParcelables.add(new ClientParcelable("Dubois faure", "City, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Martin Legrand", "Naturalia, Paris", "thumb"));
        clientParcelables.add(new ClientParcelable("Simon Perrin", "Casino, Nice", "thumb"));

//        ajout des clientParcelables dans la liste
        if (clientParcelableList != null) {
            clientParcelableList.clear();
        }
        clientParcelableList.addAll(clientParcelables);

        // rafraichissement recycler view
        mAdapter.notifyDataSetChanged();
    }


    public ClientsFragment() {
        // Required empty public constructor
    }

    public static ClientsFragment newInstance() {

        Bundle args = new Bundle();
        ClientsFragment fragment = new ClientsFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_clients, container, false);

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mProgressIV = rootView.findViewById(R.id.iv_progress);
        searchET = rootView.findViewById(R.id.et_search_client);
        mAddClientFB = rootView.findViewById(R.id.floatingbtn_add_client);
        mShowCategoriesDialog = (ImageButton) rootView.findViewById(R.id.ib_categories_client_show);
        mCategoryIV = (ImageView) rootView.findViewById(R.id.iv_selected_categorie_client);
        mCategoryTV = (TextView) rootView.findViewById(R.id.tv_selected_categorie_client);

        clientParcelableList = new ArrayList<>();

        mAdapter = new ClientsAdapter(getContext(), clientParcelableList, ClientsFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 66));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mAddClientFB.getVisibility() == View.VISIBLE) {
                    mAddClientFB.hide();
                } else if (dy < 0 && mAddClientFB.getVisibility() != View.VISIBLE) {
                    mAddClientFB.show();
                }
            }});

//        ecoute de la recherche d'un client
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mAdapter.performFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        ecoute du click de selection de la categorie de client
        mShowCategoriesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FullScreenCatPdtDialog dialog = FullScreenCatPdtDialog.newInstance(ClientsFragment.this, "customer", 0);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, FullScreenCatPdtDialog.TAG);
            }
        });
//        ecoute du click pour ajout d'un client
        mAddClientFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddCustomerActivity.class);
                startActivity(intent);
            }
        });

//        recuperation des clients sur le serveur
        executeFindClients();

        return rootView;
    }

    @Override
    public void onDestroy() {
        cancelFind();
        super.onDestroy();
    }
}
