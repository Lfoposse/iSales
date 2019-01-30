package com.rainbow_cl.i_sales.pages.home.fragment;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.ClientsAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.ClientEntry;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.helper.RecyclerItemTouchHelper;
import com.rainbow_cl.i_sales.interfaces.ClientsAdapterListener;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.DialogClientListener;
import com.rainbow_cl.i_sales.interfaces.FindThirdpartieListener;
import com.rainbow_cl.i_sales.interfaces.MyCropImageListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.pages.addcategorie.AddCategorieActivity;
import com.rainbow_cl.i_sales.pages.addcustomer.AddCustomerActivity;
import com.rainbow_cl.i_sales.pages.home.dialog.ClientProfileDialog;
import com.rainbow_cl.i_sales.pages.home.dialog.FullScreenCatPdtDialog;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;
import com.rainbow_cl.i_sales.task.FindThirdpartieTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.util.ArrayList;
import java.util.List;

public class ClientsFragment extends Fragment implements ClientsAdapterListener,
        DialogCategorieListener, RecyclerItemTouchHelper.RecyclerItemTouchHelperListener, MyCropImageListener, FindThirdpartieListener {
    private static final String TAG = ClientsFragment.class.getSimpleName();

    //    elements de la vue
    private RecyclerView mRecyclerView;
    private ImageView mProgressIV, mCategoryIV;
    private EditText searchET;
    private FloatingActionMenu mMenuFAM;
    private FloatingActionButton mItemClientFAB, mItemRafraichirFAB;
    private ImageButton mShowCategoriesDialog, searchIB, searchCancelIB;
    private TextView mCategoryTV;

    private ArrayList<ClientParcelable> clientParcelableList;
    private ClientsAdapter mAdapter;

    //    task de recuperation des clients
    private FindThirdpartieTask mFindClientTask = null;
    ProgressDialog mProgressDialog;

    private int mLimit = 10;
    private int mPageClient = 0;
    private long mLastClientId = 0;
    private int mLastPosition;

    //    database instance
    private AppDatabase mDb;

    private static boolean isTabletMode;

    private static DialogClientListener dialogClientListener;


    //    Recupération de la liste des produits
    private void executeFindClients() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        if (mFindClientTask == null) {

            mFindClientTask = new FindThirdpartieTask(getContext(), ClientsFragment.this, mLimit, mPageClient, ApiUtils.THIRDPARTIE_CLIENT);
            mFindClientTask.execute();
        }
    }

    //    Recupere la lsite des clients dans la bd locale
    private void loadClients(List<ClientEntry> clientEntryList) {
        List<ClientEntry> clientEntries;
        if (clientEntryList == null) {
            clientEntries = mDb.clientDao().getClientsLimit(mLastClientId, mLimit);
        } else {
            clientEntries = clientEntryList;
        }

        if (clientEntries.size() <= 0) {
//            Toast.makeText(getContext(), getString(R.string.aucun_produit_trouve), Toast.LENGTH_LONG).show();

//        affichage de l'image d'attente
            showProgress(false);

//        Suppression des images des clients en local
            ISalesUtility.deleteClientsImgFolder();

//            recupere la liste des clients sur le serveur
//            executeFindClients();

            return;
        }

//        Affichage de la liste des produits sur la vue
        ArrayList<ClientParcelable> clientParcelables = new ArrayList<>();
        for (ClientEntry clientEntry : clientEntries) {
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
            clientParcelable.setEmail(clientEntry.getEmail());
            clientParcelable.setPhone(clientEntry.getPhone());
            clientParcelable.setPays(clientEntry.getPays());
            clientParcelable.setRegion(clientEntry.getRegion());
            clientParcelable.setDepartement(clientEntry.getDepartement());
            clientParcelable.setCode_client(clientEntry.getCode_client());
            clientParcelable.setIs_synchro(clientEntry.getIs_synchro());
//            initialisation du poster du client
            clientParcelable.setPoster(new DolPhoto());
            clientParcelable.getPoster().setContent(clientEntry.getLogo_content());
//            produitParcelable.setPoster_name(ISalesUtility.getImgProduit(productItem.getDescription()));

            clientParcelables.add(clientParcelable);
        }

        Log.e(TAG, "onFindThirdpartieCompleted: mLastClientId=" + mLastClientId);
//        incrementation du nombre de page
        mLastClientId = clientEntries.get(clientEntries.size() - 1).getId();

        if (clientParcelableList.size() > 0 && mLastClientId <= 0) {
            clientParcelableList.clear();
        }

        clientParcelableList.addAll(clientParcelables);

        mAdapter.notifyDataSetChanged();

//        affichage de l'image d'attente
        showProgress(false);
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

    @Override
    public void onClientsSelected(ClientParcelable clientParcelable, int position) {
//        Toast.makeText(getContext(), "Selected: " + clientParcelable.getName() + ", " + clientParcelable.getAddress(), Toast.LENGTH_SHORT).show();

//        ferme le fab menu
        mMenuFAM.close(true);

        if (isTabletMode && dialogClientListener != null) {
            dialogClientListener.onClientDialogSelected(clientParcelable, position);
        } else {
            ClientProfileDialog dialog = ClientProfileDialog.newInstance(clientParcelable, position, ClientsFragment.this, ClientsFragment.this);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
            dialog.show(ft, ClientProfileDialog.TAG);
        }
    }

    @Override
    public void onClientsUpdated(ClientParcelable clientParcelable, int position) {
        clientParcelableList.set(position, clientParcelable);
        mAdapter.notifyItemChanged(position, clientParcelable);
    }

    @Override
    public void onCategorieDialogSelected(CategorieParcelable categorieParcelable) {

    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof ClientsAdapter.ClientsViewHolder) {
            // get the removed item name to display it in snack bar
            String name = clientParcelableList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final ClientParcelable deletedItem = clientParcelableList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setMessage(String.format("%s \n %s", getResources().getString(R.string.voulez_vous_supprimer_client), name))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // remove the item from recycler view
                            mAdapter.removeItem(viewHolder.getAdapterPosition());
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    })
                    .create();
            dialog.show();

        }
    }

    @Override
    public void onClientLogoChange(ClientParcelable clientParcelable, int position) {
        clientParcelableList.set(position, clientParcelable);
        mAdapter.notifyItemChanged(position, clientParcelable);

    }

    @Override
    public void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST) {
        mFindClientTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findThirdpartieREST == null) {
            //        Fermeture du loader
            showProgressDialog(false, null, null);
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findThirdpartieREST.getThirdparties() == null) {
            Log.e(TAG, "onFindThirdpartieCompleted: findThirdpartieREST getThirdparties null");
            //        Fermeture du loader
            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageClient = 0;
            Toast.makeText(getContext(), getString(R.string.comptes_clients_synchronises), Toast.LENGTH_LONG).show();

            mLastClientId = 0;
            loadClients(null);

            return;
        }

//        Log.e(TAG, "onFindThirdpartieCompleted: getThirdparties size=" + findThirdpartieREST.getThirdparties().size());
        for (Thirdpartie thirdpartie : findThirdpartieREST.getThirdparties()) {
            ClientEntry clientEntry = new ClientEntry();

            String logo = thirdpartie.getName_alias().equals("") ? thirdpartie.getLogo() : thirdpartie.getName_alias();
            Log.e(TAG, "onFindThirdpartieCompleted: logo="+logo+" getName_alias="+thirdpartie.getName_alias()+" getLogo="+thirdpartie.getLogo());
            clientEntry.setName(thirdpartie.getName());
            clientEntry.setName_alias(thirdpartie.getName_alias());
            clientEntry.setFirstname(thirdpartie.getFirstname());
            clientEntry.setLastname(thirdpartie.getLastname());
            clientEntry.setAddress(thirdpartie.getAddress());
            clientEntry.setTown(thirdpartie.getTown());
            clientEntry.setLogo(logo);
            clientEntry.setDate_creation(thirdpartie.getDate_creation());
            clientEntry.setDate_modification(thirdpartie.getDate_modification());
            clientEntry.setId(Long.parseLong(thirdpartie.getId()));
            clientEntry.setEmail(thirdpartie.getEmail());
            clientEntry.setPhone(thirdpartie.getPhone());
            clientEntry.setPays(thirdpartie.getPays());
            clientEntry.setRegion(thirdpartie.getRegion());
            clientEntry.setDepartement(thirdpartie.getDepartement());
            clientEntry.setCode_client(thirdpartie.getCode_client());
            clientEntry.setIs_synchro(1);

            if (mDb.clientDao().getClientById(clientEntry.getId()) == null) {
//                Log.e(TAG, "onFindThirdpartieCompleted: insert clientEntry");
//            insertion du client dans la BD
                mDb.clientDao().insertClient(clientEntry);
            } else {
//                Log.e(TAG, "onFindThirdpartieCompleted: update clientEntry");
//            mise a jour du client dans la BD
                mDb.clientDao().updateClient(clientEntry);
            }
        }

//        Log.e(TAG, "onFindThirdpartieCompleted: mPageClient=" + mPageClient);
//        incrementation du nombre de page
        mPageClient++;

        executeFindClients();
    }

    @Override
    public void onFindThirdpartieByIdCompleted(Thirdpartie thirdpartie) {

    }


    public ClientsFragment() {
        // Required empty public constructor
    }

    public static ClientsFragment newInstance(@NonNull DialogClientListener clientListener, boolean tabletMode) {
        dialogClientListener = clientListener;
        isTabletMode = tabletMode;

        Bundle args = new Bundle();
        ClientsFragment fragment = new ClientsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mDb = AppDatabase.getInstance(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_clients, container, false);

        setHasOptionsMenu(true);
        mLastClientId = 0;

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recycler_view);
        mProgressIV = rootView.findViewById(R.id.iv_progress);
        searchET = rootView.findViewById(R.id.et_search_client);
        searchIB = rootView.findViewById(R.id.imgbtn_search_client);
        searchCancelIB = rootView.findViewById(R.id.imgbtn_search_client_cancel);
//        mAddClientFB = rootView.findViewById(R.id.floatingbtn_add_client);

        mMenuFAM = (FloatingActionMenu) rootView.findViewById(R.id.fab_menu_client);
        mItemClientFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_item_client);
        mItemRafraichirFAB = (FloatingActionButton) rootView.findViewById(R.id.fab_item_rafraichir);

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

                if (dy > 0) {

                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

                    int itemsCount = recyclerView.getAdapter().getItemCount();

                    Log.e(TAG, "onScroll: lastPosition=" + lastPosition + " itemsCount=" + itemsCount + " mLastClientId=" + mLastClientId);
                    if (lastPosition > 0 && (lastPosition + 2) >= itemsCount) {
//        affichage de l'image d'attente
                        showProgress(true);
                        loadClients(null);
                    }
                }
            }
        });
        // adding item touch helper
        // only ItemTouchHelper.LEFT added to detect Right to Left swipe
        // if you want both Right -> Left and Left -> Right
        // add pass ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT as param
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);

//        ecoute de la recherche d'un client
        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String searchString = charSequence.toString();

                List<ClientEntry> clientEntries = mDb.clientDao().getClientsLikeLimit(mLimit, searchString);
                loadClients(clientEntries);
//                Log.e(TAG, "onTextChanged: searchString="+searchString);
                mAdapter.performFiltering(searchString);

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

//        ecoute du click de selection de la categorie de client
        mShowCategoriesDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//        ferme le fab menu
                mMenuFAM.close(true);

                FullScreenCatPdtDialog dialog = FullScreenCatPdtDialog.newInstance(ClientsFragment.this, "customer");
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, FullScreenCatPdtDialog.TAG);
            }
        });

//        ecoute du click pour ajout d'un client
        /*mAddClientFB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddCustomerActivity.class);
                startActivity(intent);
            }
        });*/
        mItemClientFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), AddCustomerActivity.class);
                startActivity(intent);
                mMenuFAM.close(true);
            }
        });

//        ecoute du click pour rafraichir la liste des clients
        mItemRafraichirFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetClientList();

//        recuperation des clients sur le serveur
                mLastClientId = 0;
                loadClients(null);
                mMenuFAM.close(true);
            }
        });


//        if (savedInstanceState != null) {
//            mClientParcelable = getActivity().getIntent().getParcelableExtra("client");
//
//        }
//        recuperation des clients sur le serveur
        loadClients(null);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        Log.e(TAG, "onSaveInstanceState: ");

//        outState.putInt("page", mLastClientId);
//        outState.putInt("lastPosition", mLastPosition);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: ");

        if (clientParcelableList.size() <= 0) {
            loadClients(null);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frag_client_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//        redirige le user vers la page de synchronisation
            case R.id.action_fragclient_sync:
                /*
                Intent intent = new Intent(getContext(), SynchronisationActivity.class);
                startActivity(intent); */

//        Si le téléphone n'est pas connecté
                if (!ConnectionManager.isPhoneConnected(getContext())) {
                    Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                    return true;
                }

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_comptes_cient_encours));

                mDb.clientDao().deleteAllClient();

//        Suppression des images des clients en local
                ISalesUtility.deleteClientsImgFolder();

//            recupere la liste des clients sur le serveur
                executeFindClients();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, "onActivityResult: ");
    }

    @Override
    public void onDestroy() {
        cancelFind();
        super.onDestroy();
    }
}
