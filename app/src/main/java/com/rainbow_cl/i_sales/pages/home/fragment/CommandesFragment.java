package com.rainbow_cl.i_sales.pages.home.fragment;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
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

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.CommandeAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.ClientEntry;
import com.rainbow_cl.i_sales.database.entry.CommandeEntry;
import com.rainbow_cl.i_sales.database.entry.CommandeLineEntry;
import com.rainbow_cl.i_sales.interfaces.CommandeAdapterListener;
import com.rainbow_cl.i_sales.interfaces.DialogClientListener;
import com.rainbow_cl.i_sales.interfaces.FindOrdersListener;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.model.CommandeParcelable;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.pages.boncmdesignature.BonCmdeSignatureActivity;
import com.rainbow_cl.i_sales.pages.detailscmde.DetailsCmdeActivity;
import com.rainbow_cl.i_sales.pages.home.dialog.ClientDialog;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Order;
import com.rainbow_cl.i_sales.remote.model.OrderLine;
import com.rainbow_cl.i_sales.remote.rest.FindOrderLinesREST;
import com.rainbow_cl.i_sales.remote.rest.FindOrdersREST;
import com.rainbow_cl.i_sales.task.FindOrderLinesTask;
import com.rainbow_cl.i_sales.task.FindOrderTask;
import com.rainbow_cl.i_sales.utility.CircleTransform;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
    private ArrayList<CommandeParcelable> commandeParcelableList;
    private CommandeAdapter mAdapter;

    private int mCalendrierFiltreYearDebut, mCalendrierFiltreMonthDebut, mCalendrierFiltreDayDebut,
            mCalendrierFiltreYearFin, mCalendrierFiltreMonthFin, mCalendrierFiltreDayFin, todayYear, todayMonth, todayDay;
    private Calendar calFin = null, calDebut = null;

    ProgressDialog mProgressDialog;

    private ClientParcelable mClientParcelableSelected = null;

    //    database instance
    private AppDatabase mDb;

    //    task de recuperation des produits
    private FindOrderTask mFindOrderTask = null;
    private int mLimit = 8;
    private int mPageOrder = 0;
    private long mLastCmdeId = 0;


    //    Recupération de la liste des produits
    private void executeFindOrder() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
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
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        FindOrderLinesTask mFindOrderLinesTask = null;
        Log.e(TAG, "executeFindOrderLines: executing  orderid=" + orderid + " cmdeRef=" + cmdeRef);

        mFindOrderLinesTask = new FindOrderLinesTask(getContext(), orderid, cmdeRef, CommandesFragment.this);
        mFindOrderLinesTask.execute();

    }

    //    Recupere la lsite des clients dans la bd locale
    private void loadCommandes(long dateDebutInMilli, long dateFinInMilli, long clientId) {
        this.commandeParcelableList.clear();

        List<CommandeEntry> cmdeEntryList;

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
        ArrayList<CommandeParcelable> commandeParcelablesList = new ArrayList<>();
        for (int i = cmdeEntryList.size() - 1; i >= 0; i--) {
            CommandeEntry cmdeEntry = cmdeEntryList.get(i);
            Log.e(TAG, "onFindOrdersTaskComplete: timestamp=" + cmdeEntry.getDate() +
                    " getCommande_id=" + cmdeEntry.getCommande_id() +
                    " dateCmde=" + cmdeEntry.getDate_commande() +
                    " total=" + cmdeEntry.getTotal_ttc() +
                    " statut=" + cmdeEntry.getStatut());
            CommandeParcelable cmdeParcelable = new CommandeParcelable();
//            client id
            cmdeParcelable.setSocid(cmdeEntry.getSocid());
            cmdeParcelable.setCommande_id(cmdeEntry.getCommande_id());
            cmdeParcelable.setIs_synchro(cmdeEntry.getIs_synchro());
            cmdeParcelable.setStatut(Integer.parseInt(cmdeEntry.getStatut() != null ? cmdeEntry.getStatut() : "1"));

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
            cmdeParcelable.setClient(new ClientParcelable());
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
            cmdeParcelable.setProduits(new ArrayList<ProduitParcelable>());
            List<CommandeLineEntry> cmdeLineEntryList = mDb.commandeLineDao().getAllCmdeLineByCmdeRef(cmdeEntry.getCommande_id());
//            chargement des produits de la commande
            for (CommandeLineEntry cmdeLineEntry : cmdeLineEntryList) {
                ProduitParcelable pdtParcelable = new ProduitParcelable();

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
                pdtParcelable.getPoster().setFilename(ISalesUtility.getImgProduit(cmdeLineEntry.getDescription()));

                cmdeParcelable.getProduits().add(pdtParcelable);
            }

            commandeParcelablesList.add(cmdeParcelable);
        }

//        incrementation du nombre de page
//        mLastCmdeId = cmdeEntryList.get(cmdeEntryList.size()-1).getCommande_id();

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
    public void onCommandeSelected(CommandeParcelable commandeParcelable) {

        Intent intent = new Intent(getContext(), DetailsCmdeActivity.class);
        intent.putExtra("commande", commandeParcelable);
        startActivity(intent);
    }

    @Override
    public void onCommandeReStarted(CommandeParcelable commandeParcelable) {

        Intent intent = new Intent(getContext(), BonCmdeSignatureActivity.class);
        intent.putExtra("commande", commandeParcelable);
        startActivity(intent);
    }

    private void fetchCommandeList() {


//        Affichage de la liste des produits sur la vue
        ArrayList<CommandeParcelable> commandeParcelables = new ArrayList<>();
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());
        commandeParcelables.add(new CommandeParcelable());

        commandeParcelableList.addAll(commandeParcelables);

        mAdapter.notifyDataSetChanged();
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
                            .transform(new CircleTransform())
                            .into(mSelectedClientIV);

                } else {
//                    chargement de la photo par defaut dans la vue
//                    mSelectedClientIV.setBackgroundResource(R.drawable.default_avatar_client);
                    Picasso.with(getContext())
                            .load(R.drawable.default_avatar_client)
                            .transform(new CircleTransform())
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
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {

                    int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastVisibleItemPosition();

                    int itemsCount = recyclerView.getAdapter().getItemCount();

//                    Log.e(TAG, "onScroll: lastPosition=" + lastPosition + " itemsCount=" + itemsCount);
                    if (lastPosition > 0 && (lastPosition + 2) >= itemsCount) {
//                        executeFindOrder();
//                        loadCommandes();
                    }
                } else if (dy < 0) {
                }
            }
        });


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
                ClientDialog dialog = ClientDialog.newInstance(CommandesFragment.this);
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

                mDb.commandeDao().deleteAllCmde();
                mDb.commandeLineDao().deleteAllCmdeLine();

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_commandes_recuperer_encours));

//            recupere la liste des clients sur le serveur
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
        if (findOrderLinesREST.getLines() != null ) {

            for (OrderLine orderLine : findOrderLinesREST.getLines()) {
                CommandeLineEntry cmdeLineEntry = new CommandeLineEntry();

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
            CommandeEntry cmdeEntry = new CommandeEntry();
//            client id
            cmdeEntry.setSocid(Long.parseLong(orderItem.getSocid()));

            cmdeEntry.setId(Long.parseLong(orderItem.getId()));
            cmdeEntry.setRef(orderItem.getRef());
            cmdeEntry.setStatut(orderItem.getStatut());

            Log.e(TAG, "onFindOrdersTaskComplete: timestamp=" + orderItem.getDate() +
                    " ref=" + orderItem.getRef() +
                    " dateCmde=" + orderItem.getDate_commande() +
                    " total=" + orderItem.getTotal_ttc() +
                    " orderStatut=" + orderItem.getStatut() +
                    " cmdeEntryStatut=" + cmdeEntry.getStatut());

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
                cmdeEntry.setDate_livraison(Long.parseLong(orderItem.getDate_livraison()));
            } else cmdeEntry.setDate_livraison(Long.parseLong("-1"));

            cmdeEntry.setTotal_ttc(orderItem.getTotal_ttc());
            cmdeEntry.setIs_synchro(1);

            Log.e(TAG, "onFindOrdersTaskComplete: commande date=" + cmdeEntry.getDate() + " Date_commande=" + cmdeEntry.getDate_commande() + " Date_livraison=" + cmdeEntry.getDate_livraison());
            CommandeEntry testCmde = mDb.commandeDao().getCmdesById(cmdeEntry.getId());
            if (testCmde == null) {
                Log.e(TAG, "onFindOrdersTaskComplete: insert CommandeEntry");
//            insertion du client dans la BD
                long cmdeEntryId = mDb.commandeDao().insertCmde(cmdeEntry);
//                Log.e(TAG, "onFindOrdersTaskComplete: " + cmdeEntryId);

                executeFindOrderLines(cmdeEntry.getId(), cmdeEntryId);

            } else {
                Log.e(TAG, "onFindOrdersTaskComplete: CommandeEntry already exist " + cmdeEntry.getRef());
//            insertion du client dans la BD
                mDb.commandeDao().updateCmde(cmdeEntry);

                executeFindOrderLines(cmdeEntry.getId(), testCmde.getId());
            }


        }

//        incrementation du nombre de page
        mPageOrder++;

        executeFindOrder();
    }
}
