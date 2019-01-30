package com.rainbow_cl.i_sales.pages.detailscmde;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.CmdeProduitAdapter;
import com.rainbow_cl.i_sales.adapter.CommandeAdapter;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.ClientEntry;
import com.rainbow_cl.i_sales.database.entry.SignatureEntry;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.interfaces.FindDocumentListener;
import com.rainbow_cl.i_sales.interfaces.FindThirdpartieListener;
import com.rainbow_cl.i_sales.model.CommandeParcelable;
import com.rainbow_cl.i_sales.model.ProduitParcelable;
import com.rainbow_cl.i_sales.pages.home.fragment.ClientsFragment;
import com.rainbow_cl.i_sales.pages.home.fragment.CommandesFragment;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.model.Thirdpartie;
import com.rainbow_cl.i_sales.remote.rest.FindDolPhotoREST;
import com.rainbow_cl.i_sales.remote.rest.FindThirdpartieREST;
import com.rainbow_cl.i_sales.task.FindDocumentTask;
import com.rainbow_cl.i_sales.task.FindThirdpartieByIdTask;
import com.rainbow_cl.i_sales.task.FindThirdpartieTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DetailsCmdeActivity extends AppCompatActivity implements FindThirdpartieListener,
        FindDocumentListener{
    private static final String TAG = DetailsCmdeActivity.class.getSimpleName();

    private TextView mRefTV, mDateTV, mDateLivraisonTV, mTotalCmdeTV, mClientNom, mClientEmail, mClientAdresse;
    private RecyclerView mRecyclerView;
    private ImageView mSignClientIV, mSignCommIV;

    private CommandeParcelable mCmdeParcelable;

    private CmdeProduitAdapter mAdapter;
    private ArrayList<ProduitParcelable> produitParcelables;

//    task de recuperation des infos du client
    private FindThirdpartieByIdTask findThirdpartieByIdTask;

//    task de recuperation de la signature du client et du commercial
    private FindDocumentTask findSignClientTask, findSignCommTask;

    private DolPhoto signClient, signComm;

    //    database instance
    private AppDatabase mDb;


    @Override
    public void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST) {}

    @Override
    public void onFindThirdpartieByIdCompleted(Thirdpartie thirdpartie) {

        if (thirdpartie != null) {

//            modifi les valeur du client
            mClientNom.setText(thirdpartie.getName());
            mClientAdresse.setText(thirdpartie.getAddress());
            mClientEmail.setText(thirdpartie.getEmail());
        }
    }

    @Override
    public void onFindDocumentComplete(FindDolPhotoREST findDolPhotoREST) {

        if (findDolPhotoREST == null || findDolPhotoREST.getDolPhoto() == null) {
            return;
        }
        if (findDolPhotoREST.getDolPhoto().getFilename().contains("client")) {
            /*
//                    conversion de la photo du Base64 en bitmap
            byte[] decodedString = Base64.decode(findDolPhotoREST.getDolPhoto().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
//            mSignClientIV.setImageBitmap(decodedByte);
            mSignClientIV.setBackground(new BitmapDrawable(getResources(), decodedByte)); */

            inflateSignatureClient(findDolPhotoREST.getDolPhoto().getContent());
        }
        if (findDolPhotoREST.getDolPhoto().getFilename().contains("commercial")) {
            /*
//                    conversion de la photo du Base64 en bitmap
            byte[] decodedString = Base64.decode(findDolPhotoREST.getDolPhoto().getContent(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
//            mSignCommIV.setImageBitmap(decodedByte);
            mSignCommIV.setBackground(new BitmapDrawable(getResources(), decodedByte)); */

            inflateSignatureComm(findDolPhotoREST.getDolPhoto().getContent());
        }
    }

    private void inflateSignatureClient(String fileContent) {
//                    conversion de la photo du Base64 en bitmap
        byte[] decodedString = Base64.decode(fileContent, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
//            mSignClientIV.setImageBitmap(decodedByte);
        mSignClientIV.setBackground(new BitmapDrawable(getResources(), decodedByte));
    }

    private void inflateSignatureComm(String fileContent) {
//                    conversion de la photo du Base64 en bitmap
        byte[] decodedString = Base64.decode(fileContent, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

//                    chargement de la photo dans la vue
//            mSignCommIV.setImageBitmap(decodedByte);
        mSignCommIV.setBackground(new BitmapDrawable(getResources(), decodedByte));
    }

    public void initValues() {
        mRefTV.setText(mCmdeParcelable.getRef());
        mTotalCmdeTV.setText(String.format("%s %s",
                ISalesUtility.amountFormat2(mCmdeParcelable.getTotal()),
                getResources().getString(R.string.symbole_euro)));
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd MMMM yyy");
//        Calendar calendarLiv = Calendar.getInstance();
//        calendarLiv.setTimeInMillis(mCmdeParcelable.getDate_livraison());
        mDateTV.setText(dateFormat.format(mCmdeParcelable.getDate()));
//        mDateLivraisonTV.setText(dateFormat.format(calendarLiv.getTime()));
        if (mCmdeParcelable.getDate_livraison() == 0) {
            mDateLivraisonTV.setText("Non définie");
        } else {
            Calendar livCal = Calendar.getInstance();
            livCal.setTimeInMillis(mCmdeParcelable.getDate_livraison());
            livCal.add(Calendar.DATE, 1);
            mDateLivraisonTV.setText(dateFormat.format(livCal.getTime()));
        }

//        Chargement des produit dans la liste
        produitParcelables.addAll(mCmdeParcelable.getProduits());
//        reafraichissement de l'adapter
        mAdapter.notifyDataSetChanged();
    }
    public void loadClientInfo() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(DetailsCmdeActivity.this)) {
            Toast.makeText(DetailsCmdeActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
//            showProgress(false);
            return;
        }
        if (findThirdpartieByIdTask == null) {

            findThirdpartieByIdTask = new FindThirdpartieByIdTask(DetailsCmdeActivity.this, DetailsCmdeActivity.this, mCmdeParcelable.getSocid());
            findThirdpartieByIdTask.execute();
        }
    }
    private void executeFindSignClient() {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(DetailsCmdeActivity.this)) {
//            Toast.makeText(DetailsCmdeActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
//            showProgress(false);
            return;
        }
        if (findSignClientTask == null) {
            String modulePart = "commande";
            String originalFile = String.format("%s_signature-client.jpeg", mCmdeParcelable.getRef());
            /*Log.e(TAG, "onBindViewHolder: getDownloadImg= "+ApiUtils.getDownloadImg(DetailsCmdeActivity.this, modulePart, originalFile)+" "+mSignClientIV.getLayoutParams()+"|"+mSignClientIV.getLayoutParams().height);
            Picasso.with(DetailsCmdeActivity.this)
                    .load(ApiUtils.getDownloadImg(DetailsCmdeActivity.this, modulePart, originalFile))
                    .placeholder(R.drawable.isales_no_image)
                    .error(R.drawable.isales_no_image)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mSignClientIV.setBackground(new BitmapDrawable(getResources(), bitmap));

                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    }); */
            findSignClientTask = new FindDocumentTask(DetailsCmdeActivity.this, DetailsCmdeActivity.this, modulePart, originalFile);
            findSignClientTask.execute();
        }
    }
    private void executeFindSignComm() {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(DetailsCmdeActivity.this)) {
//            Toast.makeText(DetailsCmdeActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
//            showProgress(false);
            return;
        }
        if (findSignCommTask == null) {
            String modulePart = "commande";
            String originalFile = String.format("%s_signature-commercial.jpeg", mCmdeParcelable.getRef());
            /*Log.e(TAG, "onBindViewHolder: getDownloadImg"+ApiUtils.getDownloadImg(DetailsCmdeActivity.this, modulePart, originalFile));
            Picasso.with(DetailsCmdeActivity.this)
                    .load(ApiUtils.getDownloadImg(DetailsCmdeActivity.this, modulePart, originalFile))
                    .placeholder(R.drawable.isales_no_image)
                    .error(R.drawable.isales_no_image)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            mSignCommIV.setBackground(new BitmapDrawable(getResources(), bitmap));

                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    }); */
            findSignCommTask = new FindDocumentTask(DetailsCmdeActivity.this, DetailsCmdeActivity.this, modulePart, originalFile);
            findSignCommTask.execute();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_cmde);

        if (getIntent().getExtras().getParcelable("commande") != null) {
            mCmdeParcelable = getIntent().getExtras().getParcelable("commande");
            Log.e(TAG, "onCreate: " + mCmdeParcelable.getRef() +
                    " productsSize=" + mCmdeParcelable.getProduits().size() +
                    " clientID=" + mCmdeParcelable.getSocid() +
                    " dateLivraison=" + mCmdeParcelable.getDate_livraison());
        }
        mDb = AppDatabase.getInstance(getApplicationContext());

//        Referencement des vues
        mRefTV = findViewById(R.id.tv_detailscmde_ref);
        mDateTV = findViewById(R.id.tv_detailscmde_date);
        mDateLivraisonTV = findViewById(R.id.tv_detailscmde_datelivraison);
        mTotalCmdeTV = findViewById(R.id.tv_detailscmde_total);
        mClientNom = findViewById(R.id.tv_detailscmde_client_nom);
        mClientEmail = findViewById(R.id.tv_detailscmde_client_email);
        mClientAdresse = findViewById(R.id.tv_detailscmde_client_adresse);
        mRecyclerView = findViewById(R.id.recyclerview_detailscmde_produits);
        mSignClientIV = findViewById(R.id.iv_detailscmde_signclient);
        mSignCommIV = findViewById(R.id.iv_detailscmde_signcomm);

        produitParcelables = new ArrayList<>();
        mAdapter = new CmdeProduitAdapter(DetailsCmdeActivity.this, produitParcelables);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(DetailsCmdeActivity.this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(DetailsCmdeActivity.this, DividerItemDecoration.VERTICAL, 00));
        mRecyclerView.setAdapter(mAdapter);

        initValues();

//        Recupertion du client dans la commande
        if (mCmdeParcelable.getClient() != null) {
//            modifi les valeur du client
            mClientNom.setText(mCmdeParcelable.getClient().getName());
            mClientAdresse.setText(mCmdeParcelable.getClient().getAddress());
            mClientEmail.setText(mCmdeParcelable.getClient().getEmail());

        } else {
            loadClientInfo();
        }

        if (savedInstanceState != null) {
            signClient = (DolPhoto) getIntent().getParcelableExtra("signclient");
            signComm = (DolPhoto) getIntent().getParcelableExtra("signcomm");

            onFindDocumentComplete(new FindDolPhotoREST(signClient));
            onFindDocumentComplete(new FindDolPhotoREST(signComm));
        } else {
//            Rcuperation des signatures dans la BD
            List<SignatureEntry> signatureEntries = mDb.signatureDao().getAllSignatureByCmdeRef(mCmdeParcelable.getCommande_id());
            SignatureEntry signatureEntryClient;
            SignatureEntry signatureEntryComm;

            if (signatureEntries.size() == 2) {
                if (signatureEntries.get(0).getType_signature().equals("CLIENT")) {
                    signatureEntryClient = signatureEntries.get(0);
                    signatureEntryComm = signatureEntries.get(1);
                } else {
                    signatureEntryClient = signatureEntries.get(1);
                    signatureEntryComm = signatureEntries.get(0);
                }

//                affichage des signatures
                inflateSignatureClient(signatureEntryClient.getContent());
                inflateSignatureComm(signatureEntryComm.getContent());
            } else {
//        execution taches de recuperation des signatures
                executeFindSignClient();
                executeFindSignComm();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // get the id of menu item clicked
        int id = item.getItemId();

//        if is toolbar back button then call device back button method
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        Log.e(TAG, "onSaveInstanceState: ");

        outState.putParcelable("commande", mCmdeParcelable);
        outState.putParcelable("signclient", signClient);
        outState.putParcelable("signcomm", signComm);
        super.onSaveInstanceState(outState);
    }
}
