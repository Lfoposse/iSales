package com.iSales.pages.home.fragment;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.iSales.adapter.CommandeAdapter;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.database.entry.CommandeEntry;
import com.iSales.database.entry.CommandeLineEntry;
import com.iSales.database.entry.SignatureEntry;
import com.iSales.interfaces.CommandeAdapterListener;
import com.iSales.interfaces.DialogClientListener;
import com.iSales.interfaces.FindOrdersListener;
import com.iSales.model.ClientParcelable;
import com.iSales.model.CommandeParcelable;
import com.iSales.model.ProduitParcelable;
import com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity;
import com.iSales.pages.detailscmde.DetailsCmdeActivity;
import com.iSales.pages.home.dialog.ClientDialog;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Document;
import com.iSales.remote.model.DolPhoto;
import com.iSales.remote.model.Order;
import com.iSales.remote.model.OrderLine;
import com.iSales.remote.rest.FindOrderLinesREST;
import com.iSales.remote.rest.FindOrdersREST;
import com.iSales.task.FindOrderLinesTask;
import com.iSales.task.FindOrderTask;
import com.iSales.task.SendDocumentTask;
import com.iSales.task.SendOrderTask;
import com.iSales.utility.CircleTransform;
import com.iSales.utility.ISalesUtility;
import com.iSales.R;
import com.squareup.picasso.Picasso;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class CommandesFragment extends Fragment implements CommandeAdapterListener, FindOrdersListener, DialogClientListener {

    private static final String TAG = CommandesFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private ImageView mProgressIV, mSelectedClientIV;
    private ImageButton mIBDateDebut, mIBDateFin, mShowClientsDialog;
    private TextView mTVDateDebut, mTVDateFin, mSelectedClientTV;
    private EditText searchET;
    private ArrayList<com.iSales.model.CommandeParcelable> commandeParcelableList;
    private com.iSales.adapter.CommandeAdapter mAdapter;

    private int mCalendrierFiltreYearDebut, mCalendrierFiltreMonthDebut, mCalendrierFiltreDayDebut,
            mCalendrierFiltreYearFin, mCalendrierFiltreMonthFin, mCalendrierFiltreDayFin, todayYear, todayMonth, todayDay;
    private Calendar calFin = null, calDebut = null;

    ProgressDialog mProgressDialog;

    private com.iSales.model.ClientParcelable mClientParcelableSelected = null;

    //    database instance
    private com.iSales.database.AppDatabase mDb;

    //    task de recuperation des produits
    private com.iSales.task.FindOrderTask mFindOrderTask = null;
    private int mLimit = 50;
    private int mPageOrder = 0;
    private long mLastCmdeId = 0;


    //    Recupération de la liste des produits
    private void executeFindOrder() {

//        Si le téléphone n'est pas connecté
        if (!com.iSales.remote.ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        if (mFindOrderTask == null) {
//            Log.e(TAG, "executeFindOrder: executing");

            mFindOrderTask = new FindOrderTask(getContext(), CommandesFragment.this, "date_creation", "asc", mLimit, mPageOrder);
            mFindOrderTask.execute();
        }
    }

    //    Recupération de la liste des lignes d'un produits
    private void executeFindOrderLines(long orderid, long cmdeRef) {

//        Si le téléphone n'est pas connecté
        if (!com.iSales.remote.ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        com.iSales.task.FindOrderLinesTask mFindOrderLinesTask = null;
        Log.e(TAG, "executeFindOrderLines: executing  orderid=" + orderid + " cmdeRef=" + cmdeRef);

        mFindOrderLinesTask = new FindOrderLinesTask(getContext(), orderid, cmdeRef, CommandesFragment.this);
        mFindOrderLinesTask.execute();

    }

    //    Recupere la lsite des clients dans la bd locale
    private void loadCommandes(long dateDebutInMilli, long dateFinInMilli, long clientId) {
        this.commandeParcelableList.clear();

        List<com.iSales.database.entry.CommandeEntry> cmdeEntryList;

        if (dateDebutInMilli <= 0 && dateFinInMilli <= 0 && clientId <= 0) {
            cmdeEntryList = mDb.commandeDao().getAllCmde();
        } else if ((clientId > 0)) {
            if (dateDebutInMilli > 0 && dateFinInMilli > 0) {
                cmdeEntryList = mDb.commandeDao().getAllCmdeOnPeriodByClient(dateDebutInMilli, dateFinInMilli, clientId);
            } else {
                cmdeEntryList = mDb.commandeDao().getAllCmdeByClient(clientId);
            }
        } else {
            cmdeEntryList = mDb.commandeDao().getAllCmdeOnPeriod(dateDebutInMilli, dateFinInMilli);
        }
        Log.e(TAG, "loadCommandes: dateDebutInMilli=" + dateDebutInMilli + " dateFinInMilli=" + dateFinInMilli + " clientId=" + clientId + " size=" + cmdeEntryList.size());
        /*if (commandeParcelableList.size() <= 0) {
            cmdeEntryList = mDb.commandeDao().getCmdesFirstLimit(mLimit);
        } else {
            cmdeEntryList = mDb.commandeDao().getCmdesLimit(commandeParcelableList.get(0).getCommande_id(), mLimit);
        } */
//        List<CommandeEntry> cmdeEntryList = mDb.commandeDao().getAllCmde();

        if (cmdeEntryList.size() <= 0) {
//            Toast.makeText(getContext(), getString(R.string.aucune_commande_trouve), Toast.LENGTH_LONG).show();
//        affichage de l'image d'attente
            showProgress(false);

//            Application du filtre
            perfomFilterCommande();
            return;
        }

//        Affichage de la liste des commandes sur la vue
        ArrayList<com.iSales.model.CommandeParcelable> commandeParcelablesList = new ArrayList<>();
        for (int i = cmdeEntryList.size() - 1; i >= 0; i--) {
            com.iSales.database.entry.CommandeEntry cmdeEntry = cmdeEntryList.get(i);
            /*Log.e(TAG, "onFindOrdersTaskComplete: timestamp=" + cmdeEntry.getDate() +
                    " getCommande_id=" + cmdeEntry.getCommande_id() +
                    " dateCmde=" + cmdeEntry.getDate_commande() +
                    " total=" + cmdeEntry.getTotal_ttc() +
                    " statut=" + cmdeEntry.getStatut()); */
            com.iSales.model.CommandeParcelable cmdeParcelable = new com.iSales.model.CommandeParcelable();
//            client id
            cmdeParcelable.setSocid(cmdeEntry.getSocid());
            cmdeParcelable.setCommande_id(cmdeEntry.getCommande_id());
            cmdeParcelable.setIs_synchro(cmdeEntry.getIs_synchro());
            cmdeParcelable.setStatut(Integer.parseInt(cmdeEntry.getStatut() != null ? cmdeEntry.getStatut() : "1"));
            cmdeParcelable.setMode_reglement(cmdeEntry.getMode_reglement());
            cmdeParcelable.setMode_reglement_code(cmdeEntry.getMode_reglement_code());
            cmdeParcelable.setMode_reglement_id(cmdeEntry.getMode_reglement_id());
            cmdeParcelable.setNote_private(cmdeEntry.getNote_private());
            cmdeParcelable.setNote_public(cmdeEntry.getNote_public());
            cmdeParcelable.setRemise(cmdeEntry.getRemise());
            cmdeParcelable.setRemise_percent(cmdeEntry.getRemise_percent());
            cmdeParcelable.setRemise_absolue(cmdeEntry.getRemise_absolue());

//            cmdeParcelable.setId(cmdeEntry.getId());
            cmdeParcelable.setRef(cmdeEntry.getRef());
            SimpleDateFormat dateFormat = new SimpleDateFormat("'CMD'yyMMdd'-'HHmmss");
//            if (orderItem.getDate() != null && orderItem.getDate() != "") {
            try {
                Date date = dateFormat.parse(cmdeEntry.getRef());
                cmdeParcelable.setDate(date.getTime());
//                Log.e(TAG, "onFindOrdersTaskComplete: order date="+date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                cmdeParcelable.setDate(cmdeEntry.getDate());
            }
//            } else cmdeParcelable.setDate(-1);
//            if (orderItem.getDate_commande() != null && orderItem.getDate_commande() != "") {
            try {
                Date date = dateFormat.parse(cmdeEntry.getRef());
                cmdeParcelable.setDate_commande(date.getTime());
//                Log.e(TAG, "onFindOrdersTaskComplete: order date_commande="+date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                cmdeParcelable.setDate_commande(cmdeEntry.getDate_commande());
            }
//            } else cmdeParcelable.setDate_commande(-1);
            if (cmdeEntry.getDate_livraison() != null) {
                cmdeParcelable.setDate_livraison(cmdeEntry.getDate_livraison());
            } else cmdeParcelable.setDate_livraison(-1);

            cmdeParcelable.setTotal(cmdeEntry.getTotal_ttc());

//            Chargement du client dans la BD
            ClientEntry clientEntry = mDb.clientDao().getClientById(cmdeParcelable.getSocid());
            cmdeParcelable.setClient(new com.iSales.model.ClientParcelable());
            if (clientEntry != null) {
//            modifi les valeur du client
                cmdeParcelable.getClient().setIs_synchro(clientEntry.getIs_synchro());
                cmdeParcelable.getClient().setName(clientEntry.getName());
                cmdeParcelable.getClient().setEmail(clientEntry.getEmail());
                cmdeParcelable.getClient().setPhone(clientEntry.getPhone());
                cmdeParcelable.getClient().setPays(clientEntry.getPays());
                cmdeParcelable.getClient().setDepartement(clientEntry.getDepartement());
                cmdeParcelable.getClient().setRegion(clientEntry.getRegion());
                cmdeParcelable.getClient().setTown(clientEntry.getTown());
                cmdeParcelable.getClient().setAddress(clientEntry.getAddress());
                cmdeParcelable.getClient().setLastname(clientEntry.getLastname());
                cmdeParcelable.getClient().setFirstname(clientEntry.getFirstname());
                cmdeParcelable.getClient().setDate_modification(clientEntry.getDate_modification());
                cmdeParcelable.getClient().setDate_creation(clientEntry.getDate_creation());
                cmdeParcelable.getClient().setId(clientEntry.getId());
                cmdeParcelable.getClient().setLogo(clientEntry.getLogo());

            }

//            Chargement des produit dans la commande
            cmdeParcelable.setProduits(new ArrayList<com.iSales.model.ProduitParcelable>());
            List<com.iSales.database.entry.CommandeLineEntry> cmdeLineEntryList = mDb.commandeLineDao().getAllCmdeLineByCmdeRef(cmdeEntry.getCommande_id());
//            chargement des produits de la commande
            for (com.iSales.database.entry.CommandeLineEntry cmdeLineEntry : cmdeLineEntryList) {
                com.iSales.model.ProduitParcelable pdtParcelable = new ProduitParcelable();

                pdtParcelable.setId(cmdeLineEntry.getId());
                pdtParcelable.setRef(cmdeLineEntry.getRef());
                pdtParcelable.setLabel(cmdeLineEntry.getLabel());
                pdtParcelable.setDescription(cmdeLineEntry.getDescription());
                pdtParcelable.setQty("" + cmdeLineEntry.getQuantity());
                pdtParcelable.setPrice(cmdeLineEntry.getPrice());
                pdtParcelable.setPrice_ttc(cmdeLineEntry.getPrice_ttc());
                pdtParcelable.setSubprice(cmdeLineEntry.getSubprice());
                pdtParcelable.setTotal_ht(cmdeLineEntry.getTotal_ht());
                pdtParcelable.setTotal_tva(cmdeLineEntry.getTotal_tva());
                pdtParcelable.setTotal_ttc(cmdeLineEntry.getTotal_ttc());
                pdtParcelable.setPoster(new DolPhoto());
                pdtParcelable.getPoster().setFilename(com.iSales.utility.ISalesUtility.getImgProduit(cmdeLineEntry.getDescription()));
                pdtParcelable.setRemise(cmdeLineEntry.getRemise());
                pdtParcelable.setRemise_percent(cmdeLineEntry.getRemise_percent());
                pdtParcelable.setTva_tx(cmdeLineEntry.getTva_tx());

                cmdeParcelable.getProduits().add(pdtParcelable);
            }

            commandeParcelablesList.add(cmdeParcelable);
        }

//        incrementation du nombre de page
//        mLastCmdeId = cmdeEntryList.get(cmdeEntryList.size()-1).getCommande_id();

        if (commandeParcelableList.size() > 0) {
            commandeParcelableList.clear();
        }
        this.commandeParcelableList.addAll(commandeParcelablesList);

        perfomFilterCommande();

//        affichage de l'image d'attente
        showProgress(false);
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

    private void perfomFilterCommande() {
//        Getting the sharedPreference value
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
//        String mode = sharedPreferences.getString(getContext().getString(R.string.commande_mode), "online");
        Boolean online = sharedPreferences.getBoolean(getContext().getString(R.string.commande_mode), true);
        Boolean effectuer = sharedPreferences.getBoolean(getContext().getString(R.string.commande_effectuer), true);
        Boolean encours = sharedPreferences.getBoolean(getContext().getString(R.string.commande_encours), true);
        Boolean livrer = sharedPreferences.getBoolean(getContext().getString(R.string.commande_livrer), true);
        Boolean nonpayer = sharedPreferences.getBoolean(getContext().getString(R.string.commande_nonpayer), true);

        int synchro = online ? 1 : 0;

        Log.e(TAG, "perfomFilterCommande: online=" + online + " synchro=" + synchro + " effectuer=" + effectuer + " encours=" + encours + " livrer=" + livrer + " nonpayer=" + nonpayer);

        mAdapter.performFilteringPreference(synchro, effectuer, encours, livrer, nonpayer);
    }

    @Override
    public void onCommandeSelected(com.iSales.model.CommandeParcelable commandeParcelable) {

        Intent intent = new Intent(getContext(), DetailsCmdeActivity.class);
        intent.putExtra("commande", commandeParcelable);
        startActivity(intent);
    }

    @Override
    public void onCommandeReStarted(com.iSales.model.CommandeParcelable commandeParcelable) {

        Intent intent = new Intent(getContext(), BonCmdeSignatureActivity.class);
        intent.putExtra("commande", commandeParcelable);
        startActivity(intent);
    }

    private void fetchCommandeList() {


//        Affichage de la liste des produits sur la vue
        ArrayList<com.iSales.model.CommandeParcelable> commandeParcelables = new ArrayList<>();
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new com.iSales.model.CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());

        commandeParcelableList.addAll(commandeParcelables);

        mAdapter.notifyDataSetChanged();
    }

    //    Envoi de la liste des commandes sur le serveur
    private void executeSendOrderSynchro() {

//        Recuperation des commandes non synchronisées
        List<com.iSales.database.entry.CommandeEntry> commandeEntries = mDb.commandeDao().getAllCmdeNotSynchro();
        Log.e(TAG, "executeSendOrderSynchro: commandeEntries size=" + commandeEntries.size());
        for (int i = 0; i < commandeEntries.size(); i++) {
            com.iSales.database.entry.CommandeEntry cmdeEntryItem = commandeEntries.get(i);
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            com.iSales.remote.model.Order order = new com.iSales.remote.model.Order();

            order.setSocid(""+cmdeEntryItem.getSocid());
            order.setDate_commande(dateFormat.format(cmdeEntryItem.getDate_commande()));
            order.setDate(dateFormat.format(cmdeEntryItem.getDate()));
            order.setDate_livraison(dateFormat.format(cmdeEntryItem.getDate_livraison()));
            order.setRef(cmdeEntryItem.getRef());
            order.setRemise_percent(cmdeEntryItem.getRemise_percent());
            order.setMode_reglement(cmdeEntryItem.getMode_reglement());
            order.setMode_reglement_code(cmdeEntryItem.getMode_reglement_code());
            order.setMode_reglement_id(cmdeEntryItem.getMode_reglement_id());
            order.setNote_public(cmdeEntryItem.getNote_public());
            order.setLines(new ArrayList<com.iSales.remote.model.OrderLine>());

//            Recupération de la listes des produits de la commande
            List<com.iSales.database.entry.CommandeLineEntry> cmdeLineEntryList = mDb.commandeLineDao().getAllCmdeLineByCmdeRef(cmdeEntryItem.getCommande_id());
            List<SignatureEntry> signatureEntries = mDb.signatureDao().getAllSignatureByCmdeRef(cmdeEntryItem.getCommande_id());
            Log.e(TAG, "executeSendOrderSynchro: cmdeLineEntryList size=" + cmdeLineEntryList.size() +
                    " signatureEntries size=" + signatureEntries.size());

//            Ajout des lignes commande dans la commande
            for (int j = 0; j < cmdeLineEntryList.size(); j++) {
                com.iSales.remote.model.OrderLine orderLine = new com.iSales.remote.model.OrderLine();
                com.iSales.database.entry.CommandeLineEntry cmdeLineEntry = cmdeLineEntryList.get(j);

                orderLine.setRef(cmdeLineEntry.getRef());
                orderLine.setFk_product(cmdeLineEntry.getId());
                orderLine.setProduct_ref(cmdeLineEntry.getRef());
                orderLine.setProduct_label(cmdeLineEntry.getLabel());
                orderLine.setLibelle(cmdeLineEntry.getLabel());
                orderLine.setLabel(String.format("%s-%s", cmdeLineEntry.getRef(), cmdeLineEntry.getLabel()));
                orderLine.setProduct_desc(cmdeLineEntry.getDescription());
                orderLine.setQty(String.valueOf(cmdeLineEntry.getQuantity()));
                orderLine.setTva_tx(cmdeLineEntry.getTva_tx());
                orderLine.setSubprice(cmdeLineEntry.getPrice());
                orderLine.setDesc(cmdeLineEntry.getDescription());
                orderLine.setDescription(cmdeLineEntry.getDescription());
                orderLine.setId(String.valueOf(cmdeLineEntry.getId()));
                orderLine.setRowid(String.valueOf(cmdeLineEntry.getId()));
                orderLine.setRemise(cmdeLineEntry.getRemise());
                orderLine.setRemise_percent(cmdeLineEntry.getRemise_percent());

//                Ajout de la ligne dans la commande
                order.getLines().add(orderLine);
            }

//                                creation du document signature client
            com.iSales.remote.model.Document sign1 = new com.iSales.remote.model.Document();
            sign1.setFilecontent(signatureEntries.get(0).getContent());
            sign1.setFilename(signatureEntries.get(0).getName());
            sign1.setFileencoding("base64");
            sign1.setModulepart("commande");

//                                creation du document signature client
            com.iSales.remote.model.Document sign2 = new Document();
            sign2.setFilecontent(signatureEntries.get(1).getContent());
            sign2.setFilename(signatureEntries.get(1).getName());
            sign2.setFileencoding("base64");
            sign2.setModulepart("commande");

//            execution de la task d'envoi de la commande sur le servur
            com.iSales.task.SendOrderTask sendOrderTask = new SendOrderTask(order, getContext());
            sendOrderTask.execute();

//            execution task envoi des signatures
            com.iSales.task.SendDocumentTask sendDocumentTask1 = new com.iSales.task.SendDocumentTask(sign1, getContext());
            sendDocumentTask1.execute();
            com.iSales.task.SendDocumentTask sendDocumentTask2 = new SendDocumentTask(sign2, getContext());
            sendDocumentTask2.execute();

        }

    }

    public CommandesFragment() {
        // Required empty public constructor
    }

    public static CommandesFragment newInstance() {

        Bundle args = new Bundle();
        CommandesFragment fragment = new CommandesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {

        mClientParcelableSelected = clientParcelable;

        if (mClientParcelableSelected != null) {
            Log.e(TAG, "onClientDialogSelected: name=" + clientParcelable.getName());
//        modification du label de la categorie
            mSelectedClientTV.setText(mClientParcelableSelected.getName());

            if (mClientParcelableSelected.getPoster().getContent() != null) {

                File imgFile = new File(mClientParcelableSelected.getPoster().getContent());
                if (imgFile.exists()) {
                    Picasso.with(getContext())
                            .load(imgFile)
                            .transform(new com.iSales.utility.CircleTransform())
                            .into(mSelectedClientIV);

                } else {
//                    chargement de la photo par defaut dans la vue
//                    mSelectedClientIV.setBackgroundResource(R.drawable.default_avatar_client);
                    Picasso.with(getContext())
                            .load(R.drawable.default_avatar_client)
                            .transform(new com.iSales.utility.CircleTransform())
                            .into(mSelectedClientIV);
                }

            } else {
//            chargement de la photo par defaut dans la vue
//                mSelectedClientIV.setBackgroundResource(R.drawable.default_avatar_client);
                Picasso.with(getContext())
                        .load(R.drawable.default_avatar_client)
                        .transform(new CircleTransform())
                        .into(mSelectedClientIV);
            }

//            si la date de fin selectionnée est nulle alors on recupere uniquement les commande du client
            if (calFin == null) {
                loadCommandes(-1, -1, mClientParcelableSelected.getId());
            } else {
                loadCommandes(calDebut.getTimeInMillis(), calFin.getTimeInMillis(), mClientParcelableSelected.getId());
            }

        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDb = AppDatabase.getInstance(getContext().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_commandes, container, false);

//        And in onCreate add this line to make the options appear in your Toolbar
        setHasOptionsMenu(true);

//        referencement des vues
        mRecyclerView = rootView.findViewById(R.id.recyclerview_commandes);
        mProgressIV = rootView.findViewById(R.id.iv_progress_commandes);
        mIBDateDebut = rootView.findViewById(R.id.ib_commande_pickdate_debut);
        mIBDateFin = rootView.findViewById(R.id.ib_commande_pickdate_fin);
        mTVDateDebut = rootView.findViewById(R.id.tv_commande_pickdate_debut);
        mTVDateFin = rootView.findViewById(R.id.tv_commande_pickdate_fin);
        mShowClientsDialog = (ImageButton) rootView.findViewById(R.id.ib_commande_client);
        mSelectedClientIV = (ImageView) rootView.findViewById(R.id.iv_commande_client);
        mSelectedClientTV = (TextView) rootView.findViewById(R.id.tv_commande_client);

//        searchET = rootView.findViewById(R.id.et_search);
        commandeParcelableList = new ArrayList<>();

        mAdapter = new CommandeAdapter(getContext(), commandeParcelableList, CommandesFragment.this);

        GridLayoutManager mLayoutManager = new GridLayoutManager(getContext(), ISalesUtility.calculateNoOfColumnsCmde(getContext()));
        mRecyclerView.setLayoutManager(mLayoutManager);
//        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
//        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL, 36));
        mRecyclerView.setAdapter(mAdapter);


//        Définition des dates courantes
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat yearformat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthformat = new SimpleDateFormat("M");
        SimpleDateFormat dayformat = new SimpleDateFormat("d");
        mCalendrierFiltreYearDebut = Integer.parseInt(yearformat.format(calendar.getTime()));
        mCalendrierFiltreMonthDebut = Integer.parseInt(monthformat.format(calendar.getTime())) - 1;
        mCalendrierFiltreDayDebut = Integer.parseInt(dayformat.format(calendar.getTime()));
        mCalendrierFiltreYearFin = Integer.parseInt(yearformat.format(calendar.getTime()));
        mCalendrierFiltreMonthFin = Integer.parseInt(monthformat.format(calendar.getTime())) - 1;
        mCalendrierFiltreDayFin = Integer.parseInt(dayformat.format(calendar.getTime()));
        todayYear = Integer.parseInt(yearformat.format(calendar.getTime()));
        todayMonth = Integer.parseInt(monthformat.format(calendar.getTime())) - 1;
        todayDay = Integer.parseInt(dayformat.format(calendar.getTime()));

//        initialise le contenu par defaut des dates
        mTVDateDebut.setText("Choisir une date");
        mTVDateFin.setText("Choisir une date");

//        ecoute click de selection de la date de debut
        mIBDateDebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SpinnerDatePickerDialogBuilder()
                        .context(getContext())
                        .callback(new com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                mCalendrierFiltreYearDebut = year;
                                mCalendrierFiltreMonthDebut = monthOfYear;
                                mCalendrierFiltreDayDebut = dayOfMonth;

                                calDebut = Calendar.getInstance();
                                calDebut.set(mCalendrierFiltreYearDebut, mCalendrierFiltreMonthDebut, mCalendrierFiltreDayDebut, 0, 0, 0);

                                Log.e(TAG, " dateDebutTime=" + calDebut.getTimeInMillis());

                                SimpleDateFormat dateformat = new SimpleDateFormat("dd MMMM yyyy ");
                                String stringDateSet = dateformat.format(calDebut.getTime());
                                mTVDateDebut.setText(stringDateSet);

//                                doHistoriqueJour(calDebut.getTimeInMillis(), calFin.getTimeInMillis(), getResources().getString(R.string.code_operations));
                            }
                        })
                        .showTitle(true)
                        .showDaySpinner(true)
                        .defaultDate(mCalendrierFiltreYearDebut, mCalendrierFiltreMonthDebut, mCalendrierFiltreDayDebut)
                        .minDate(1900, 0, 1)
                        .maxDate(todayYear, todayMonth, todayDay)
                        .build()
                        .show();
            }
        });
//        ecoute click de selection de la date de fin
        mIBDateFin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SpinnerDatePickerDialogBuilder()
                        .context(getContext())
                        .callback(new com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                mCalendrierFiltreYearFin = year;
                                mCalendrierFiltreMonthFin = monthOfYear;
                                mCalendrierFiltreDayFin = dayOfMonth;
                                calFin = Calendar.getInstance();
                                calFin.set(mCalendrierFiltreYearFin, mCalendrierFiltreMonthFin, mCalendrierFiltreDayFin, 23, 59, 59);

                                if (calDebut == null) {
                                    Toast.makeText(getContext(), "Veuillez choisir la date de debut.", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (calDebut.getTimeInMillis() > calFin.getTimeInMillis()) {
                                    Toast.makeText(getContext(), "La date de debut doit etre inferieure a la date de fin.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                Log.e(TAG, " dateDebutTime=" + calDebut.getTimeInMillis() + " dateFinTime=" + calFin.getTimeInMillis());

                                SimpleDateFormat dateformat = new SimpleDateFormat("dd MMMM yyyy ");
                                String stringDateSet = dateformat.format(calFin.getTime());
                                mTVDateFin.setText(stringDateSet);

                                showProgress(true);
                                if (mClientParcelableSelected == null) {
                                    loadCommandes(calDebut.getTimeInMillis(), calFin.getTimeInMillis(), -1);
                                } else {
                                    loadCommandes(calDebut.getTimeInMillis(), calFin.getTimeInMillis(), mClientParcelableSelected.getId());
                                }
                            }
                        })
                        .showTitle(true)
                        .showDaySpinner(true)
                        .defaultDate(mCalendrierFiltreYearFin, mCalendrierFiltreMonthFin, mCalendrierFiltreDayFin)
                        .minDate(1900, 0, 1)
                        .maxDate(todayYear, todayMonth, todayDay)
                        .build()
                        .show();
            }
        });

        mShowClientsDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                com.iSales.pages.home.dialog.ClientDialog dialog = com.iSales.pages.home.dialog.ClientDialog.newInstance(CommandesFragment.this);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_up, R.anim.slide_in_down, R.anim.slide_out_down, R.anim.slide_out_up);
                dialog.show(ft, ClientDialog.TAG);
            }
        });

//        Recupération de la liste des commandes sur le serveur
//        executeFindOrder();
        loadCommandes(-1, -1, -1);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.frag_commande_menu, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            //if the menu id correspond to settings, go to settings activity
            case R.id.action_cmde_filtre:
//            Log.e(TAG, "onOptionsItemSelected: action_settings");

//            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
// ...Irrelevant code for customizing the buttons and title
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.layout_commander_pref_dialog, null);
//            dialogBuilder.setView(dialogView);
//            final AlertDialog alertDialog = dialogBuilder.create();
                final AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Filtre Commandes");
                alertDialog.setCancelable(false);
                alertDialog.setView(dialogView);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "APPLIQUER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();

                        perfomFilterCommande();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "ANNULER", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                alertDialog.show();

                return true;
            case R.id.action_fragcommande_sync:

//        Si le téléphone n'est pas connecté
                if (!ConnectionManager.isPhoneConnected(getContext())) {
                    Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                    return true;
                }

//                                Log.e(TAG, "onFindImagesProductsComplete: currOrientation="+currOrientation );
                if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_commandes_recuperer_encours));

                executeSendOrderSynchro();

//                Suppression des commandes dans la BD
                mDb.commandeDao().deleteAllCmde();
                mDb.commandeLineDao().deleteAllCmdeLine();

//            recupere la liste des commandes sur le serveur
                executeFindOrder();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFindOrderLinesTaskComplete(long commande_ref, long commande_id, FindOrderLinesREST findOrderLinesREST) {
//        mFindOrderLinesTask = null;

//            chargement des produits de la commande
        if (findOrderLinesREST.getLines() != null) {

            for (com.iSales.remote.model.OrderLine orderLine : findOrderLinesREST.getLines()) {
                com.iSales.database.entry.CommandeLineEntry cmdeLineEntry = new com.iSales.database.entry.CommandeLineEntry();

                cmdeLineEntry.setId(Long.parseLong(orderLine.getId()));
                cmdeLineEntry.setRef(orderLine.getRef());
                cmdeLineEntry.setLabel(orderLine.getLibelle() != null ? orderLine.getLibelle() : orderLine.getLabel());
                cmdeLineEntry.setDescription(orderLine.getDescription());
                cmdeLineEntry.setQuantity(Integer.parseInt(orderLine.getQty()));
                cmdeLineEntry.setPrice(orderLine.getPrice());
                cmdeLineEntry.setPrice_ttc(orderLine.getPrice());
                cmdeLineEntry.setSubprice(orderLine.getSubprice());
                cmdeLineEntry.setTotal_ht(orderLine.getTotal_ht());
                cmdeLineEntry.setTotal_tva(orderLine.getTotal_tva());
                cmdeLineEntry.setTotal_ttc(orderLine.getTotal_ttc());
                cmdeLineEntry.setCommande_ref(commande_ref);
                cmdeLineEntry.setRemise(orderLine.getRemise());
                cmdeLineEntry.setRemise_percent(orderLine.getRemise_percent());

//                    Log.e(TAG, "onFindOrdersTaskComplete: product name=" + cmdeLineEntry.getLabel());
//            insertion de la commandeLine dans la BD
                mDb.commandeLineDao().insertCmdeLine(cmdeLineEntry);
            }
        }
    }

    @Override
    public void onFindOrdersTaskComplete(FindOrdersREST findOrdersREST) {
        mFindOrderTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findOrdersREST == null) {
            //        Fermeture du loader
            showProgressDialog(false, null, null);
            Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findOrdersREST.getOrders() == null) {
//            Log.e(TAG, "onFindOrdersTaskComplete: findOrderTaskComplete getThirdparties null");
            Objects.requireNonNull(getActivity()).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            //        Fermeture du loader
            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageOrder = 0;

            Toast.makeText(getContext(), getString(R.string.commandes_synchronises), Toast.LENGTH_LONG).show();

            calDebut = null;
            calFin = null;
            loadCommandes(-1, -1, -1);
            return;
        }

        for (Order orderItem : findOrdersREST.getOrders()) {
            com.iSales.database.entry.CommandeEntry cmdeEntry = new CommandeEntry();
//            client id
            cmdeEntry.setSocid(Long.parseLong(orderItem.getSocid()));

            cmdeEntry.setId(Long.parseLong(orderItem.getId()));
            cmdeEntry.setRef(orderItem.getRef());
            cmdeEntry.setStatut(orderItem.getStatut());
            cmdeEntry.setMode_reglement_id(orderItem.getMode_reglement_id());
            cmdeEntry.setMode_reglement(orderItem.getMode_reglement());
            cmdeEntry.setMode_reglement_code(orderItem.getMode_reglement_code());
            cmdeEntry.setNote_private(orderItem.getNote_private());
            cmdeEntry.setNote_public(orderItem.getNote_public());
            cmdeEntry.setRemise_percent(orderItem.getRemise_percent());
            cmdeEntry.setRemise(orderItem.getRemise());
            cmdeEntry.setRemise_absolue(orderItem.getRemise_absolue());

            /* Log.e(TAG, "onFindOrdersTaskComplete: timestamp=" + orderItem.getDate() +
                    " ref=" + orderItem.getRef() +
                    " dateCmde=" + orderItem.getDate_commande() +
                    " total=" + orderItem.getTotal_ttc() +
                    " orderStatut=" + orderItem.getStatut() +
                    " cmdeEntryStatut=" + cmdeEntry.getStatut()); */

            SimpleDateFormat dateFormat = new SimpleDateFormat("'CMD'yyMMdd'-'HHmmss");
//            if (orderItem.getDate() != null && orderItem.getDate() != "") {
            try {
                Date date = dateFormat.parse(orderItem.getRef());
                cmdeEntry.setDate(date.getTime());
//                Log.e(TAG, "onFindOrdersTaskComplete: order date="+date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                cmdeEntry.setDate(Long.parseLong(orderItem.getDate()));
            }
//            } else cmdeParcelable.setDate(-1);
//            if (orderItem.getDate_commande() != null && orderItem.getDate_commande() != "") {
            try {
                Date date = dateFormat.parse(orderItem.getRef());
                cmdeEntry.setDate_commande(date.getTime());
//                Log.e(TAG, "onFindOrdersTaskComplete: order date_commande="+date.toString());
            } catch (ParseException e) {
                e.printStackTrace();
                cmdeEntry.setDate_commande(Long.parseLong(orderItem.getDate_commande()));
            }
//            } else cmdeParcelable.setDate_commande(-1);
            if (orderItem.getDate_livraison() != null && orderItem.getDate_livraison() != "") {
                long timestamp = Long.parseLong(orderItem.getDate_livraison()) * 1000L;
                cmdeEntry.setDate_livraison(timestamp);
            } else cmdeEntry.setDate_livraison((long) 0);

            cmdeEntry.setTotal_ttc(orderItem.getTotal_ttc());
            cmdeEntry.setIs_synchro(1);

            Log.e(TAG, "onFindOrdersTaskComplete: commande date=" + cmdeEntry.getDate() + " Date_commande=" + cmdeEntry.getDate_commande() + " Date_livraison=" + cmdeEntry.getDate_livraison());

//                Log.e(TAG, "onFindOrdersTaskComplete: insert CommandeEntry");
//            insertion du client dans la BD
            long cmdeEntryId = mDb.commandeDao().insertCmde(cmdeEntry);
//                Log.e(TAG, "onFindOrdersTaskComplete: " + cmdeEntryId);

//                executeFindOrderLines(cmdeEntry.getId(), cmdeEntryId);

//            chargement des produits de la commande
            for (OrderLine orderLine : orderItem.getLines()) {
                com.iSales.database.entry.CommandeLineEntry cmdeLineEntry = new CommandeLineEntry();

                cmdeLineEntry.setId(Long.parseLong(orderLine.getId()));
                cmdeLineEntry.setRef(orderLine.getRef());
                cmdeLineEntry.setLabel(orderLine.getLibelle() != null ? orderLine.getLibelle() : orderLine.getLabel());
                cmdeLineEntry.setDescription(orderLine.getDescription());
                cmdeLineEntry.setQuantity(Integer.parseInt(orderLine.getQty()));
                cmdeLineEntry.setPrice(orderLine.getPrice());
                cmdeLineEntry.setPrice_ttc(orderLine.getPrice());
                cmdeLineEntry.setSubprice(orderLine.getSubprice());
                cmdeLineEntry.setTotal_ht(orderLine.getTotal_ht());
                cmdeLineEntry.setTotal_tva(orderLine.getTotal_tva());
                cmdeLineEntry.setTotal_ttc(orderLine.getTotal_ttc());
                cmdeLineEntry.setTva_tx(orderLine.getTva_tx());
                cmdeLineEntry.setCommande_ref(cmdeEntryId);
                cmdeLineEntry.setRemise(orderLine.getRemise());
                cmdeLineEntry.setRemise_percent(orderLine.getRemise_percent());

                    Log.e(TAG, "onFindOrdersTaskComplete: product name=" + cmdeLineEntry.getLabel()+" tva_tx="+orderLine.getTva_tx()
                            +" total_tva="+orderLine.getTotal_tva()
                            +" remise_percent="+orderLine.getRemise_percent());
//            insertion de la commandeLine dans la BD
                mDb.commandeLineDao().insertCmdeLine(cmdeLineEntry);
            }

        }

//        incrementation du nombre de page
        mPageOrder++;

        executeFindOrder();
    }
}
