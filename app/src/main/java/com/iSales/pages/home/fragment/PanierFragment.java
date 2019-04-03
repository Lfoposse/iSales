package com.iSales.pages.home.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.iSales.R;
import com.iSales.adapter.PanierProduitAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.AppExecutors;
import com.iSales.database.entry.ClientEntry;
import com.iSales.database.entry.PanierEntry;
import com.iSales.interfaces.DialogClientListener;
import com.iSales.interfaces.PanierProduitAdapterListener;
import com.iSales.model.ClientParcelable;
import com.iSales.pages.boncmdeverification.BonCmdeVerificationActivity;
import com.iSales.pages.home.dialog.ClientDialog;
import com.iSales.remote.model.DolPhoto;
import com.iSales.utility.CircleTransform;
import com.iSales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PanierFragment extends Fragment implements PanierProduitAdapterListener, DialogClientListener {
    private static final String TAG = com.iSales.pages.home.fragment.PanierFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ImageView mProgressIV, mPosterClient;
    private TextView mPanierTotal, mNameClient, mCountProduits;
    private ImageButton mShowClientsDialog;
    private Button mCommanderBtn, mValiderPanierBtn, mViderPanierBtn;

    private PanierProduitAdapter mAdapter;
    private ArrayList<PanierEntry> panierEntriesList;
    private ClientParcelable mClientParcelableSelected = null;

    private boolean isPanierValidated;
    private double mTotalPanier = 0;

    ProgressDialog mProgressDialog;
    private AppDatabase mDb;

    /**
     * fetches json by making http calls
     */
    private void initPanier() {
        ArrayList<PanierEntry> panierEntries = new ArrayList<>();
        panierEntries.add(new PanierEntry("Cacahuetes choco", "15", "15", 54));
        panierEntries.add(new PanierEntry("Hamburgre", "5", "5", 54));
        panierEntries.add(new PanierEntry("Sandwich épicé", "18", "18", 54));
        panierEntries.add(new PanierEntry("Frites de pomme sauté", "100", "100", 54));
        panierEntries.add(new PanierEntry("Sauce vinaigrée", "68", "68", 54));

//        ajout des clientParcelables dans la liste
        if (panierEntriesList != null) {
            panierEntriesList.clear();
        }
        panierEntriesList.addAll(panierEntries);

        // rafraichissement recycler view
        mAdapter.notifyDataSetChanged();
    }

    //    recuperation des produits du panier
    private void loadPanier() {
        mProgressIV.setVisibility(View.VISIBLE);
        List<PanierEntry> panierEntries = mDb.panierDao().getAllPanier();
//        ajout des clientParcelables dans la liste
        if (panierEntriesList != null) {
            panierEntriesList.clear();
            mAdapter.notifyDataSetChanged();
        }
        panierEntriesList.addAll(panierEntries);

//        Mise a jour du montant total du panier
        setMontantTotalPanier();

        mCountProduits.setText(String.format("%s produit(s) dans le panier", panierEntriesList.size()));
        // rafraichissement du recyclerview
        mAdapter.notifyDataSetChanged();
        mProgressIV.setVisibility(View.GONE);
    }

    private void setMontantTotalPanier() {
        double total = 0;
        for (int i = 0; i < panierEntriesList.size(); i++) {
            double totalRow = Double.valueOf(panierEntriesList.get(i).getPrice_ttc()) * panierEntriesList.get(i).getQuantity();

            total += totalRow;
        }

        mTotalPanier = total;

        mPanierTotal.setText(String.format("%s %s", ISalesUtility.amountFormat2("" + total),
                ISalesUtility.CURRENCY));
    }

    @Override
    public void onRemoveItemPanier(final PanierEntry panierEntry, final int position) {
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
        alertDialog.setTitle("Confirmation");
        alertDialog.setMessage("Voulez-vous vraiment retirer ce produit du panier ?");
        alertDialog.setCancelable(false);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OUI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        // insert new panier
                        mDb.panierDao().deletePanier(panierEntry);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                suppression du produit dans la liste
                                panierEntriesList.remove(position);

//                mise a jour de la vue
                                mAdapter.notifyDataSetChanged();
                                mCountProduits.setText(String.format("%s produit(s) dans le panier", panierEntriesList.size()));
                            }
                        });
                    }
                });
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NON", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    @Override
    public void onChangeQuantityItemPanier(final int position, final int quantity) {
        Log.e(TAG, "onChangeQuantityItemPanier: getQuantity=" + panierEntriesList.get(position).getQuantity() + " quantity=" + quantity);

//        Mise a jour du montant total du panier
        setMontantTotalPanier();
        this.isPanierValidated = false;
        mValiderPanierBtn.setEnabled(true);
        /*AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // insert new panier
//                NB: la valeude la quantite est automatiquement mise a jour dans l'adapter
                mDb.panierDao().updateQuantite(panierEntriesList.get(position).getId(), quantity);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//        Mise a jour du montant total du panier
                        setMontantTotalPanier();
                    }
                });
            }
        }); */
    }

    @Override
    public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {

        mClientParcelableSelected = clientParcelable;

        if (mClientParcelableSelected != null) {
            Log.e(TAG, "onClientDialogSelected: name=" + clientParcelable.getName());
//        modification du label de la categorie
            mNameClient.setText(mClientParcelableSelected.getName());

            if (mClientParcelableSelected.getPoster().getContent() != null) {

                File imgFile = new File(mClientParcelableSelected.getPoster().getContent());
                if (imgFile.exists()) {
                    Picasso.with(getContext())
                            .load(imgFile)
                            .transform(new CircleTransform())
                            .into(mPosterClient);

                } else {
//                    chargement de la photo par defaut dans la vue
//                    mPosterClient.setBackgroundResource(R.drawable.default_avatar_client);
                    Picasso.with(getContext())
                            .load(R.drawable.default_avatar_client)
                            .transform(new CircleTransform())
                            .into(mPosterClient);
                }

            } else {
//            chargement de la photo par defaut dans la vue
//                mPosterClient.setBackgroundResource(R.drawable.default_avatar_client);
                Picasso.with(getContext())
                        .load(R.drawable.default_avatar_client)
                        .transform(new CircleTransform())
                        .into(mPosterClient);
            }
        }
    }

    public PanierFragment() {
        // Required empty public constructor
    }

    public static com.iSales.pages.home.fragment.PanierFragment newInstance() {

        Bundle args = new Bundle();
        com.iSales.pages.home.fragment.PanierFragment fragment = new com.iSales.pages.home.fragment.PanierFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_panier, container, false);

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recyclerview_panier_produits);
        mProgressIV = rootView.findViewById(R.id.iv_panier_progress_produits);
        mPanierTotal = rootView.findViewById(R.id.tv_panier_total);
        mShowClientsDialog = (ImageButton) rootView.findViewById(R.id.ib_clientsradio_show);
        mPosterClient = (ImageView) rootView.findViewById(R.id.iv_selected_client);
        mNameClient = (TextView) rootView.findViewById(R.id.tv_selected_client);
        mCountProduits = (TextView) rootView.findViewById(R.id.tv_panier_count);
        mViderPanierBtn = (Button) rootView.findViewById(R.id.btn_panier_vider);
        mValiderPanierBtn = (Button) rootView.findViewById(R.id.btn_panier_valider);
        mCommanderBtn = (Button) rootView.findViewById(R.id.btn_panier_commander);

        panierEntriesList = new ArrayList<>();

        mAdapter = new PanierProduitAdapter(getContext(), panierEntriesList, com.iSales.pages.home.fragment.PanierFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mRecyclerView.setAdapter(mAdapter);

        mShowClientsDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientDialog dialog = ClientDialog.newInstance(com.iSales.pages.home.fragment.PanierFragment.this);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, ClientDialog.TAG);
            }
        });

        mViderPanierBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Panier");
                alertDialog.setMessage("Voulez-vous vraiment vider le panier ?");
                alertDialog.setCancelable(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDb.panierDao().deleteAllPanier();

                        loadPanier();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
        mValiderPanierBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePanierQuntity();
            }
        });
//        ecoute du clique sur le bouton commander
        mCommanderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (panierEntriesList.size() <= 0) {
                    Toast.makeText(getContext(), "Votre panier est vide. Veuillez choisir des produits", Toast.LENGTH_LONG).show();
                    return;
                }
                if (mClientParcelableSelected == null) {
                    Toast.makeText(getContext(), "Veuillez choisir un client pour le panier.", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!isPanierValidated) {
                    Toast.makeText(getContext(), "Veuillez valider le panier.", Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(getContext(), BonCmdeVerificationActivity.class);
//                Mise a null de la photo du client pour éviter que l'application ne crash
                ClientParcelable clientParcelable = mClientParcelableSelected;
                clientParcelable.getPoster().setContent(null);
                intent.putExtra("client", clientParcelable);
                intent.putExtra("totalPanier", mTotalPanier);
                Log.e(TAG, "onClick: before start activity mClientParcelableSelected" + mClientParcelableSelected.getPoster().getContent());
                startActivity(intent);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

//        recuperation des clients sur le serveur
        loadPanier();

        this.isPanierValidated = true;
//        recuperation du client courrant
        ClientEntry clientEntry = mDb.clientDao().getCurrentClient(1);
        if (clientEntry != null) {

            ClientParcelable clientParcelable = new ClientParcelable();
            clientParcelable.setName(clientEntry.getName());
            clientParcelable.setFirstname(clientEntry.getFirstname());
            clientParcelable.setLastname(clientEntry.getLastname());
            clientParcelable.setAddress(clientEntry.getAddress());
            clientParcelable.setTown(clientEntry.getTown());
            clientParcelable.setLogo(clientEntry.getName_alias());
            clientParcelable.setDate_creation(clientEntry.getDate_creation());
            clientParcelable.setDate_modification(clientEntry.getDate_modification());
            clientParcelable.setId(clientEntry.getId());
            clientParcelable.setEmail(clientEntry.getEmail());
            clientParcelable.setPhone(clientEntry.getPhone());
            clientParcelable.setPays(clientEntry.getPays());
            clientParcelable.setRegion(clientEntry.getRegion());
            clientParcelable.setDepartement(clientEntry.getDepartement());
            clientParcelable.setIs_synchro(clientEntry.getIs_synchro());

//            initialisation du poster du client
            clientParcelable.setPoster(new DolPhoto());
            clientParcelable.getPoster().setContent(clientEntry.getLogo_content());

            Log.e(TAG, "onResume: client is_synchro=" + clientParcelable.getIs_synchro() +
                    " name=" + clientParcelable.getName());
            onClientDialogSelected(clientParcelable, 0);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    void changePanierQuntity() {
        showProgressDialog(true, "Panier", "Mises a jour du panier");
        List<PanierEntry> panierEntries = mAdapter.getPanierItems();
//        Log.e(TAG, "onPause:panierEntries size="+panierEntries.size());

        for (PanierEntry item : panierEntries) {
            Log.e(TAG, "onPause:panierEntries Item quantite=" + item.getQuantity() +
                    " label=" + item.getLabel() +
                    " id=" + item.getId());
            mDb.panierDao().updateQuantite(item.getId(), item.getQuantity());
        }

//        loadPanier();

        mAdapter.notifyDataSetChanged();
        this.isPanierValidated = true;
        mValiderPanierBtn.setEnabled(false);
        showProgressDialog(false, null, null);
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
}
