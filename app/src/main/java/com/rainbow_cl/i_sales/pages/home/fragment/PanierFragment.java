package com.rainbow_cl.i_sales.pages.home.fragment;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.PanierProduitAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.AppExecutors;
import com.rainbow_cl.i_sales.database.entry.PanierEntry;
import com.rainbow_cl.i_sales.interfaces.DialogClientListener;
import com.rainbow_cl.i_sales.interfaces.PanierProduitAdapterListener;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.pages.boncmdeverification.BonCmdeVerificationActivity;
import com.rainbow_cl.i_sales.pages.home.dialog.ClientDialog;
import com.rainbow_cl.i_sales.pages.home.viewmodel.PanierViewModel;
import com.rainbow_cl.i_sales.utility.CircleTransform;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PanierFragment extends Fragment implements PanierProduitAdapterListener, DialogClientListener {
    private static final String TAG = PanierFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private ImageView mProgressIV, mPosterClient;
    private TextView mPanierTotal, mNameClient;
    private ImageButton mShowClientsDialog;
    private Button mCommanderBtn;

    private PanierProduitAdapter mAdapter;
    private ArrayList<PanierEntry> panierEntriesList;
    private ClientParcelable mClientParcelableSelected = null;

    private double mTotalPanier = 0;

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
        final PanierViewModel viewModel = ViewModelProviders.of(this).get(PanierViewModel.class);
        viewModel.getAllPanierEntries().observe(this, new Observer<List<PanierEntry>>() {
            @Override
            public void onChanged(@Nullable List<PanierEntry> panierEntries) {

//        ajout des clientParcelables dans la liste
                if (panierEntriesList != null) {
                    panierEntriesList.clear();
                }
                panierEntriesList.addAll(panierEntries);

//        Mise a jour du montant total du panier
                setMontantTotalPanier();

                // rafraichissement du recyclerview
                mAdapter.notifyDataSetChanged();
                mProgressIV.setVisibility(View.GONE);
            }
        });
    }

    private void setMontantTotalPanier() {
        double total = 0;
        for (int i = 0; i < panierEntriesList.size(); i++) {
            double totalRow = Double.valueOf(panierEntriesList.get(i).getPrice_ttc()) * panierEntriesList.get(i).getQuantity();

            total += totalRow;
        }

        mTotalPanier = total;

        mPanierTotal.setText(String.format("%s %s", ISalesUtility.amountFormat2(""+total),
                ISalesUtility.CURRENCY));
    }

    @Override
    public void onRemoveItemPanier(final PanierEntry panierEntry, final int position) {
        final AlertDialog alertDialog = new  AlertDialog.Builder(getContext()).create();
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
    public void onChangeQuantityItemPanier(final int position, int quantity) {
        Log.e(TAG, "onChangeQuantityItemPanier: getQuantity="+panierEntriesList.get(position).getQuantity()+" quantity="+quantity);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                // insert new panier
//                NB: la valeude la quantite est automatiquement mise a jour dans l'adapter
                mDb.panierDao().updatePanier(panierEntriesList.get(position));

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//        Mise a jour du montant total du panier
                        setMontantTotalPanier();
                    }
                });
            }
        });
    }

    @Override
    public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {

        mClientParcelableSelected = clientParcelable;

        if (mClientParcelableSelected != null) {
            Log.e(TAG, "onClientDialogSelected: name="+clientParcelable.getName());
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

    public static PanierFragment newInstance() {

        Bundle args = new Bundle();
        PanierFragment fragment = new PanierFragment();
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
        mCommanderBtn = (Button) rootView.findViewById(R.id.btn_panier_commander);

        panierEntriesList = new ArrayList<>();

        mAdapter = new PanierProduitAdapter(getContext(), panierEntriesList, PanierFragment.this);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mRecyclerView.setAdapter(mAdapter);

        mShowClientsDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClientDialog dialog = ClientDialog.newInstance(PanierFragment.this);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, ClientDialog.TAG);
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

                Intent intent = new Intent(getContext(), BonCmdeVerificationActivity.class);
//                Mise a null de la photo du client pour éviter que l'application ne crash
                ClientParcelable clientParcelable = mClientParcelableSelected;
                clientParcelable.getPoster().setContent(null);
                intent.putExtra("client", clientParcelable);
                intent.putExtra("totalPanier", mTotalPanier);
                Log.e(TAG, "onClick: before start activity mClientParcelableSelected"+mClientParcelableSelected.getPoster().getContent() );
                startActivity(intent);
            }
        });

//        recuperation des clients sur le serveur
        loadPanier();

        return rootView;
    }
}
