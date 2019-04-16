package com.iSales.pages.home.fragment;


import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.iSales.R;
import com.iSales.adapter.ClientsRadioAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.decoration.MyDividerItemDecoration;
import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.interfaces.DialogClientListener;
import com.iSales.model.ClientParcelable;
import com.iSales.remote.model.DolPhoto;
import com.iSales.task.FindThirdpartieTask;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientsRadioFragment extends Fragment implements ClientsAdapterListener {

    public static final String TAG = com.iSales.pages.home.fragment.ClientsRadioFragment.class.getSimpleName();
    //    elements de la vue
    private RecyclerView mRecyclerView;
    private ImageView mProgressIV;
    private EditText searchET;
    private ImageButton searchIB, searchCancelIB;

    private ArrayList<ClientParcelable> clientParcelableList;
    private ArrayList<ClientParcelable> clientParcelableListFiltered;
    private ClientsRadioAdapter mAdapter;

    //    task de recuperation des clients
    private FindThirdpartieTask mFindClientTask = null;

    private static DialogClientListener dialogClientListener;

    private int mLimit = 50;
    private long mLastClientId = 0;

    //    database instance
    private AppDatabase mDb;

    @Override
    public void onClientsSelected(ClientParcelable clientParcelable, int position) {

        dialogClientListener.onClientDialogSelected(clientParcelable, position);
    }

    @Override
    public void onClientsUpdated(ClientParcelable clientParcelable, int position) {

    }

    //    Recupere la lsite des clients dans la bd locale
    private void loadClients() {
        showProgress(true);

        if (clientParcelableList.size() > 0) {
            if (mLimit < clientParcelableList.size()) {
                clientParcelableListFiltered.addAll(clientParcelableList.subList(0, mLimit));
            } else {
                clientParcelableListFiltered.addAll(clientParcelableList.subList(0, clientParcelableList.size()));
            }
            mAdapter.notifyDataSetChanged();
        }

//        affichage de l'image d'attente
        showProgress(false);
    }

    //    Recupere la lsite des clients dans la bd locale
    private void initClients() {
        List<ClientEntry> clientEntries = mDb.clientDao().getAllClient();
        clientParcelableList = new ArrayList<>();

        ArrayList<ClientParcelable> clientParcelables = new ArrayList<>();
        for (ClientEntry clientEntry : clientEntries) {
            if (clientEntry.getId() == null) {
                clientEntry.setId((long) -1);
            }
//            Log.e(TAG, "loadClients: itemClient oid=" + clientEntry.getOid()+" id="+clientEntry.getId());
            ClientParcelable clientParcelable = new ClientParcelable();
            clientParcelable.setName(clientEntry.getName());
            clientParcelable.setFirstname(clientEntry.getFirstname());
            clientParcelable.setLastname(clientEntry.getLastname());
            clientParcelable.setAddress(clientEntry.getAddress());
            clientParcelable.setTown(clientEntry.getTown());
            clientParcelable.setLogo(clientEntry.getLogo());
            clientParcelable.setDate_creation(clientEntry.getDate_creation());
            clientParcelable.setDate_modification(clientEntry.getDate_modification());
            clientParcelable.setId(clientEntry.getId());
            clientParcelable.setClient_id(clientEntry.getClient_id());
            clientParcelable.setOid(clientEntry.getOid());
            clientParcelable.setEmail(clientEntry.getEmail());
            clientParcelable.setPhone(clientEntry.getPhone());
            clientParcelable.setPays(clientEntry.getPays());
            clientParcelable.setRegion(clientEntry.getRegion());
            clientParcelable.setDepartement(clientEntry.getDepartement());
            clientParcelable.setCode_client(clientEntry.getCode_client());
            clientParcelable.setIs_synchro(clientEntry.getIs_synchro());
            clientParcelable.setIs_current(clientEntry.getIs_current());
//            initialisation du poster du client
            clientParcelable.setPoster(new DolPhoto());
            clientParcelable.getPoster().setContent(clientEntry.getLogo_content());
//            produitParcelable.setPoster_name(ISalesUtility.getImgProduit(productItem.getDescription()));

            clientParcelables.add(clientParcelable);
        }

        clientParcelableList = clientParcelables;
    }

    //    arrete le processus de recupération des infos sur le serveur
    private void cancelFind() {
        if (mFindClientTask != null) {
            mFindClientTask.cancel(true);
            mFindClientTask = null;
        }
    }

    //    vide le contenu de la vue
    private void resetClientList() {
        clientParcelableList.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Shows the progress UI and hides.
     */
    private void showProgress(final boolean show) {

        mProgressIV.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public ClientsRadioFragment() {
        // Required empty public constructor
    }

    public static com.iSales.pages.home.fragment.ClientsRadioFragment newInstance(@NonNull DialogClientListener clientListener) {
        dialogClientListener = clientListener;

        Bundle args = new Bundle();
        com.iSales.pages.home.fragment.ClientsRadioFragment fragment = new com.iSales.pages.home.fragment.ClientsRadioFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clients_radio, container, false);

        mDb = AppDatabase.getInstance(getContext().getApplicationContext());

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recyclerview_clientradio);
        mProgressIV = rootView.findViewById(R.id.iv_progress_clientradio);
        searchET = rootView.findViewById(R.id.et_search_clientradio);
        searchIB = rootView.findViewById(R.id.imgbtn_search_clientradio);
        searchCancelIB = rootView.findViewById(R.id.imgbtn_search_clientradio_cancel);

        clientParcelableListFiltered = new ArrayList<>();

        mAdapter = new ClientsRadioAdapter(getContext(), clientParcelableListFiltered, com.iSales.pages.home.fragment.ClientsRadioFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 66));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (dy > 0) {

                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

                    int itemsCount = recyclerView.getAdapter().getItemCount();

                    /*Log.e(TAG, "onScroll: lastPosition=" + lastPosition + " itemsCount=" + itemsCount
                            + " mLastClientId2=" + clientParcelableList.get(lastPosition).getId()
                            + " mLastClientIdItemsCount=" + clientParcelableList.get(itemsCount - 1).getId()
                            + " mLastClientClientId2=" + clientParcelableList.get(lastPosition).getClient_id()
                            + " oid1=" + clientParcelableList.get(lastPosition).getOid()
                            + " mLastClientClientIdItemsCount=" + clientParcelableList.get(itemsCount - 1).getClient_id()
                            + " oid=" + clientParcelableList.get(itemsCount - 1).getOid()); */
                    if (clientParcelableListFiltered.get(lastPosition).getId() == clientParcelableListFiltered.get(itemsCount - 1).getId()
                            && (lastPosition + 1 + mLimit) < clientParcelableList.size()) {
//        affichage de l'image d'attente
                        showProgress(true);
                        clientParcelableListFiltered.addAll(clientParcelableList.subList(lastPosition + 1, lastPosition + 1 + mLimit));
                        mAdapter.notifyDataSetChanged();
                        showProgress(false);
                    }
                }
            }
        });

//        ecoute de la recherche d'un client
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        affichage de l'image d'attente
                showProgress(true);

                String searchString = charSequence.toString();
                Log.e(TAG, "onTextChanged: searchString=" + searchString);

                perfomFiltering(searchString);

            }

            @Override
            public void afterTextChanged(Editable editable) {

//                Log.e(TAG, "afterTextChanged: string="+editable.toString() );
                if (editable.toString().equals("")) {
                    searchCancelIB.setVisibility(View.GONE);
                    searchIB.setVisibility(View.VISIBLE);
                } else {
                    searchCancelIB.setVisibility(View.VISIBLE);
                    searchIB.setVisibility(View.GONE);
                }
            }
        });
        searchCancelIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchET.setText("");
            }
        });

//        recuperation des clients sur le serveur
//        executeFindClients();
//        affichage de l'image d'attente
        showProgress(true);

        initClients();
        loadClients();

        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");

        if (clientParcelableList.size() <= 0) {
            initClients();
            loadClients();
        } else {
            mAdapter.notifyDataSetChanged();
        }

        String searchString = searchET.getText().toString();

        if (!searchString.equals("")) {
            perfomFiltering(searchString);
        }
    }

    void perfomFiltering(String searchString) {

        showProgress(true);
        clientParcelableListFiltered.clear();
        mAdapter.notifyDataSetChanged();

        if (searchString.equals("")) {
            loadClients();
        } else {
            ArrayList<ClientParcelable> filteredList = new ArrayList<>();
            for (ClientParcelable row : clientParcelableList) {

                // name match condition. this might differ depending on your requirement
                // here we are looking for name or phone number match
                if (row.getName().toLowerCase().contains(searchString.toLowerCase())
                        || row.getAddress().toLowerCase().contains(searchString.toLowerCase())) {
                    filteredList.add(row);
                }
            }
            clientParcelableListFiltered.addAll(filteredList);
            mAdapter.notifyDataSetChanged();
        }

        showProgress(false);
    }

    @Override
    public void onDestroy() {
        cancelFind();
        super.onDestroy();
    }
}
