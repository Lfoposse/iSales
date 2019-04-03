package com.iSales.pages.boncmdesignature;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.kyanogen.signatureview.SignatureView;
import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.database.entry.CommandeEntry;
import com.iSales.database.entry.CommandeLineEntry;
import com.iSales.database.entry.PanierEntry;
import com.iSales.database.entry.PaymentTypesEntry;
import com.iSales.database.entry.SignatureEntry;
import com.iSales.database.entry.UserEntry;
import com.iSales.interfaces.InsertThirdpartieListener;
import com.iSales.model.ClientParcelable;
import com.iSales.model.CommandeParcelable;
import com.iSales.model.ProduitParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Document;
import com.iSales.remote.model.Order;
import com.iSales.remote.model.OrderLine;
import com.iSales.remote.model.Thirdpartie;
import com.iSales.remote.rest.InsertThirdpartieREST;
import com.iSales.task.InsertThirdpartieTask;
import com.iSales.utility.ISalesUtility;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BonCmdeSignatureActivity extends AppCompatActivity implements InsertThirdpartieListener {
    private static final String TAG = com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.class.getSimpleName();
    private Button mAnnulerSignClientBTN, mAnnulerSignCommBTN, mEnregistrerBTN;
    private SignatureView mClientSignatureView, mCommSignatureView;
    private TextView mClientName, mCommName, mDateLivraisonTV, mModeReglementTV;
    private EditText mAcompteET, mRemiseET;
    private Switch mSynchroServeurSW;
    private View mDateLivraisonVIEW;
    private View mModeReglementVIEW;

    private Calendar calLivraison = null;
    private int todayYear, todayMonth, todayDay;
    private int livraisonYear, livraisonMonth, livraisonDay;

    private ClientParcelable mClientParcelableSelected;
    private CommandeParcelable mCmdeParcelable;

    private UserEntry mUserEntry;
    private List<PaymentTypesEntry> paymentTypesEntries;
    private PaymentTypesEntry paymentTypesChoosed = null;

    private double mTotalPanier = 0;
    private AppDatabase mDb;

    //    pousse la commande sur le serveur
    public void pushCommande() {

        final ProgressDialog progressDialog = new ProgressDialog(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this);
        progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.enregistrement_commande_encours)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
        progressDialog.show();

//        recuperation du panier dans la BD
        final List<PanierEntry> panierEntryList = mDb.panierDao().getAllPanier();

        Calendar today = Calendar.getInstance();
        Calendar dateLivraison = Calendar.getInstance();

        dateLivraison.set(livraisonYear, livraisonMonth, livraisonDay);
        Log.e(TAG, "pushCommande: dateCrea=" + today.getTime() + " dateLivraison=" + dateLivraison.getTime() + " livraisonYear=" + livraisonYear + " livraisonMonth=" + livraisonMonth + " livraisonDay=" + livraisonDay);
        final SimpleDateFormat refOrderFormat = new SimpleDateFormat("yyMMdd-HHmmss");
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String refOrder = String.format("CMD%s", refOrderFormat.format(today.getTime()));
//        String dateOrder = String.valueOf(today.getTime());
        double remisePercent = 0;
        if (!mRemiseET.getText().toString().equals("")) {
            remisePercent = Double.parseDouble(mRemiseET.getText().toString().replace(",", "."));
        }
//        today.roll(Calendar.DAY_OF_MONTH, -1);
        String dateOrder = dateFormat.format(today.getTime());
//        String dateLivraisonOrder = dateFormat.format(dateLivraison.getTime());
        String dateLivraisonOrder = dateFormat.format(dateLivraison.getTime());

        final CommandeEntry cmdeEntry = new CommandeEntry();

        cmdeEntry.setSocid(mClientParcelableSelected.getId());
        cmdeEntry.setDate_commande(today.getTimeInMillis());
        cmdeEntry.setDate(today.getTimeInMillis());
        cmdeEntry.setDate_livraison(dateLivraison.getTimeInMillis());
        cmdeEntry.setRef(refOrder);
        cmdeEntry.setMode_reglement(paymentTypesChoosed.getLabel());
        cmdeEntry.setMode_reglement_code(paymentTypesChoosed.getCode());
        cmdeEntry.setMode_reglement_id(paymentTypesChoosed.getId());
        cmdeEntry.setTotal_ttc("" + mTotalPanier);
        cmdeEntry.setIs_synchro(0);
        cmdeEntry.setStatut("1");
        cmdeEntry.setRemise_percent(""+remisePercent);
        if (!mAcompteET.getText().toString().equals("")) {
            cmdeEntry.setNote_public(String.format("%s a donné un acompte de %s %s", mClientParcelableSelected.getName(), mAcompteET.getText().toString(), getResources().getString(R.string.symbole_euro)));
        }

        long cmdeEntryId = mDb.commandeDao().insertCmde(cmdeEntry);
        cmdeEntry.setId(cmdeEntryId);
        final List<CommandeLineEntry> cmdeLineEntryList = new ArrayList<>();

//        s'il s'agit de la relance de la commande
        if (mCmdeParcelable != null) {
            for (ProduitParcelable pdtParcelable : mCmdeParcelable.getProduits()) {

                CommandeLineEntry cmdeLineEntry = new CommandeLineEntry();
                Log.e(TAG, "pushCommande:local label=" + pdtParcelable.getLabel() +
                        " ref=" + pdtParcelable.getRef() +
                        " description=" + pdtParcelable.getDescription());

                double totalHt = Double.parseDouble(pdtParcelable.getPrice()) * Integer.parseInt(pdtParcelable.getQty());
                double totalTttc = Double.parseDouble(pdtParcelable.getPrice_ttc()) * Integer.parseInt(pdtParcelable.getQty());
                cmdeLineEntry.setRef(pdtParcelable.getRef());
                cmdeLineEntry.setLabel(String.format("%s", pdtParcelable.getLabel()));
                cmdeLineEntry.setQuantity(Integer.parseInt(pdtParcelable.getQty()));
                cmdeLineEntry.setSubprice(pdtParcelable.getPrice());
                cmdeLineEntry.setPrice(pdtParcelable.getPrice());
                cmdeLineEntry.setPrice_ttc(pdtParcelable.getPrice_ttc());
                cmdeLineEntry.setTva_tx(pdtParcelable.getTva_tx());
                cmdeLineEntry.setDescription(pdtParcelable.getDescription());
                cmdeLineEntry.setId(pdtParcelable.getId());
                cmdeLineEntry.setCommande_ref(cmdeEntryId);
                cmdeLineEntry.setTotal_ht("" + totalHt);
                cmdeLineEntry.setTotal_ttc("" + totalTttc);
                cmdeLineEntry.setRemise(pdtParcelable.getRemise());
                cmdeLineEntry.setRemise_percent(pdtParcelable.getRemise_percent());

//            Ajout de la ligne dans le panier
                cmdeLineEntryList.add(cmdeLineEntry);
            }
        } else {
            for (PanierEntry entryItem : panierEntryList) {
                CommandeLineEntry cmdeLineEntry = new CommandeLineEntry();
                Log.e(TAG, "pushCommande:local label=" + entryItem.getLabel() +
                        " ref=" + entryItem.getRef() +
                        " description=" + entryItem.getDescription());

                double totalHt = Double.parseDouble(entryItem.getPrice()) * entryItem.getQuantity();
                double totalTttc = Double.parseDouble(entryItem.getPrice_ttc()) * entryItem.getQuantity();
                cmdeLineEntry.setRef(entryItem.getRef());
                cmdeLineEntry.setLabel(String.format("%s", entryItem.getLabel()));
                cmdeLineEntry.setQuantity(entryItem.getQuantity());
                cmdeLineEntry.setSubprice(entryItem.getPrice());
                cmdeLineEntry.setPrice(entryItem.getPrice());
                cmdeLineEntry.setPrice_ttc(entryItem.getPrice_ttc());
                cmdeLineEntry.setTva_tx(entryItem.getTva_tx());
                cmdeLineEntry.setDescription(entryItem.getDescription());
                cmdeLineEntry.setId(entryItem.getId());
                cmdeLineEntry.setCommande_ref(cmdeEntryId);
                cmdeLineEntry.setTotal_ht("" + totalHt);
                cmdeLineEntry.setTotal_ttc("" + totalTttc);
                cmdeLineEntry.setRemise(entryItem.getRemise());
                cmdeLineEntry.setRemise_percent(entryItem.getRemise_percent());

//            Ajout de la ligne dans le panier
                cmdeLineEntryList.add(cmdeLineEntry);
            }
        }


//        Ajout les lignes commande dans la BD
        List<Long> cmdeLineIds = mDb.commandeLineDao().insertAllCmdeLine(cmdeLineEntryList);

        Log.e(TAG, "pushCommande: idCmde=" + cmdeEntryId + " linesIdsSize=" + cmdeLineIds.size());

//        recuperation de la signature client et commercial en bitmap
        Bitmap signClientBitmap = mClientSignatureView.getSignatureBitmap();
        Bitmap signCommBitmap = mCommSignatureView.getSignatureBitmap();

//                                conversion de la signature client et commercial en base64
        ByteArrayOutputStream baosClient = new ByteArrayOutputStream();
        signClientBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosClient);
        byte[] bytesSignClient = baosClient.toByteArray();
        String encodeSignClient = Base64.encodeToString(bytesSignClient, Base64.NO_WRAP);
        String filenameClient = String.format("%s_signature-client.jpeg", refOrder);

        ByteArrayOutputStream baosComm = new ByteArrayOutputStream();
        signCommBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosComm);
        byte[] bytesSignComm = baosComm.toByteArray();
        String encodeSignComm = Base64.encodeToString(bytesSignComm, Base64.NO_WRAP);
        String filenameComm = String.format("%s_signature-commercial.jpeg", refOrder);

//                                creation du document signature client
        List<SignatureEntry> signatureEntries = new ArrayList<>();
        signatureEntries.add(new SignatureEntry(cmdeEntryId, filenameClient, encodeSignClient, "CLIENT"));
        signatureEntries.add(new SignatureEntry(cmdeEntryId, filenameComm, encodeSignComm, "COMM"));

//        insertion des signatures dans la BD
        List<Long> signaturesIds = mDb.signatureDao().insertAllSignature(signatureEntries);
        Log.e(TAG, "pushCommande: idCmde=" + cmdeEntryId + " signaturesIds=" + signaturesIds.size());

//        Suppression du panier
        mDb.panierDao().deleteAllPanier();

        if (!mSynchroServeurSW.isChecked()) {
            progressDialog.dismiss();

            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.commande_enregistre_succes), Toast.LENGTH_LONG).show();

//        retour a la page d'accueil
            finish();
            return;
        }
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this)) {
            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        }

        progressDialog.setMessage("Synchronisation de la commande avec le serveur...");

        Order newOrder = new Order();

        Log.e(TAG, "pushCommande:Serveur refOrder=" + refOrder +
                " date=" + dateFormat.format(today.getTime())+" dateLivraisonOrder="+dateFormat.format(dateLivraison.getTime())+
        " dateOrder="+dateOrder+" dateLivraisonOrder="+dateLivraisonOrder);

//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");

        newOrder.setSocid(String.valueOf(mClientParcelableSelected.getId()));
        newOrder.setDate_commande("" + dateOrder);
        newOrder.setDate("" + dateOrder);
//        newOrder.setDate_livraison(""+(calLivraison != null ? calLivraison.getTimeInMillis() : ""));
        newOrder.setDate_livraison("" + dateLivraisonOrder);
        newOrder.setRef(refOrder);
        newOrder.setRemise_percent("" + remisePercent);
        newOrder.setMode_reglement(paymentTypesChoosed.getLabel());
        newOrder.setMode_reglement_code(paymentTypesChoosed.getCode());
        newOrder.setMode_reglement_id(paymentTypesChoosed.getId());
        if (!mAcompteET.getText().toString().equals("")) {
            newOrder.setNote_public(String.format("%s a donné un acompte de %s %s", mClientParcelableSelected.getName(), mAcompteET.getText().toString(), getResources().getString(R.string.symbole_euro)));
        }
        newOrder.setLines(new ArrayList<OrderLine>());

//        s'il s'agit de la relance de la commande
        if (mCmdeParcelable != null) {
            for (ProduitParcelable pdtParcelable : mCmdeParcelable.getProduits()) {
                OrderLine orderLine = new OrderLine();

                Log.e(TAG, "pushCommande:mCmdeParcelable not null label=" + pdtParcelable.getLabel() +
                        " ref=" + pdtParcelable.getRef() +
                        " description=" + pdtParcelable.getDescription()+
                        " tva_tx=" + pdtParcelable.getTva_tx()+
                        " fk_product=" + pdtParcelable.getId());

                orderLine.setRef(pdtParcelable.getRef());
                orderLine.setFk_product(pdtParcelable.getId());
                orderLine.setProduct_ref(pdtParcelable.getRef());
                orderLine.setProduct_label(pdtParcelable.getLabel());
                orderLine.setLibelle(pdtParcelable.getLabel());
                orderLine.setLabel(String.format("%s-%s", pdtParcelable.getRef(), pdtParcelable.getLabel()));
                orderLine.setProduct_desc(pdtParcelable.getDescription());
                orderLine.setQty(String.valueOf(pdtParcelable.getQty()));
                orderLine.setTva_tx(pdtParcelable.getTva_tx());
                orderLine.setSubprice(pdtParcelable.getPrice());
                orderLine.setDesc(pdtParcelable.getDescription());
                orderLine.setDescription(pdtParcelable.getDescription());
                orderLine.setId(String.valueOf(pdtParcelable.getId()));
                orderLine.setRowid(String.valueOf(pdtParcelable.getId()));
                orderLine.setRemise(pdtParcelable.getRemise());
                orderLine.setRemise_percent(pdtParcelable.getRemise_percent());

//            Ajout de la ligne dans le panier
                newOrder.getLines().add(orderLine);
            }
        } else {
//        final List<CommandeLineEntry> cmdeLineEntryList = new ArrayList<>();
            for (PanierEntry entryItem : panierEntryList) {
                OrderLine orderLine = new OrderLine();
//            CommandeLineEntry cmdeLineEntry = new CommandeLineEntry();
                Log.e(TAG, "pushCommande: label=" + entryItem.getLabel() +
                        " ref=" + entryItem.getRef() +
                        " description=" + entryItem.getDescription() +
                        " tva_tx=" + entryItem.getTva_tx()+
                        " fk_product=" + entryItem.getId()+
                        " remise=" + entryItem.getRemise()+
                        " remise_percent=" + entryItem.getRemise_percent());

                orderLine.setRef(entryItem.getRef());
                orderLine.setFk_product(entryItem.getFk_product());
                orderLine.setProduct_ref(entryItem.getRef());
                orderLine.setProduct_label(entryItem.getLabel());
                orderLine.setLibelle(entryItem.getLabel());
                orderLine.setLabel(String.format("%s", entryItem.getLabel()));
                orderLine.setProduct_desc(entryItem.getDescription());
                orderLine.setQty(String.valueOf(entryItem.getQuantity()));
                orderLine.setTva_tx(entryItem.getTva_tx());
                orderLine.setSubprice(entryItem.getPrice());
                orderLine.setDesc(entryItem.getDescription());
                orderLine.setDescription(entryItem.getDescription());
                orderLine.setId(String.valueOf(entryItem.getId()));
                orderLine.setRowid(String.valueOf(entryItem.getId()));
                orderLine.setRemise(entryItem.getRemise());
                orderLine.setRemise_percent(entryItem.getRemise_percent());

//            Ajout de la ligne dans le panier
                newOrder.getLines().add(orderLine);
            }
        }


//        enregistrement de la commande dans le serveur
        Call<Long> call = ApiUtils.getISalesService(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this).saveCustomerOrder(newOrder);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, final Response<Long> response) {
                if (response.isSuccessful()) {
                    final Long responseBody = response.body();
                    Log.e(TAG, "onResponse: saveCustomerOrder orderId=" + responseBody);

                    progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.validation_commande_encours)));

//                    Validation de la commande sur le serveur
                    Call<Order> callValidate = ApiUtils.getISalesService(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this).validateCustomerOrder(responseBody);
                    callValidate.enqueue(new Callback<Order>() {
                        @Override
                        public void onResponse(Call<Order> call, Response<Order> responseValidate) {
                            if (responseValidate.isSuccessful()) {
                                Order responseValiBody = responseValidate.body();

                                Log.e(TAG, "onResponse: validateCustomerOrder orderRef=" + responseValiBody.getRef() +
                                        " orderId=" + responseValiBody.getId());

//                                Mise a jour mode statut de la commande en local
//                                mDb.commandeDao().updateCmde(cmdeEntry);
//                                mDb.commandeDao().updateStatutCmde(cmdeEntry.getId(), cmdeEntry.getStatut());
                                progressDialog.dismiss();

                                Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.commande_enregistre_succes), Toast.LENGTH_LONG).show();

//                                                        Suppression du panier s'il ne s'agit pas de la relance de commande
                                if (mCmdeParcelable == null) {
//                                                        Suppression du panier
                                    mDb.panierDao().deleteAllPanier();
                                }

//                                                        retour a la page d'accueil
                                finish();

//                                =========== Envoi de la signature du client
                                progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.envoi_signature_client)));
//                                recuperation de la signature client en bitmap
                                Bitmap signClientBitmap = mClientSignatureView.getSignatureBitmap();

//                                conversion de la signature client en base64
                                ByteArrayOutputStream baosClient = new ByteArrayOutputStream();
                                signClientBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosClient);
                                byte[] bytesSignClient = baosClient.toByteArray();
                                String encodeSignClient = Base64.encodeToString(bytesSignClient, Base64.NO_WRAP);
                                String filenameClient = String.format("%s_signature-client.jpeg", refOrder);

//                                creation du document signature client
                                Document signClient = new Document();
                                signClient.setFilecontent(encodeSignClient);
                                signClient.setFilename(filenameClient);
                                signClient.setFileencoding("base64");
                                signClient.setModulepart("commande");

                                Call<String> callUploadSignClient = ApiUtils.getISalesService(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this).uploadDocument(signClient);
                                callUploadSignClient.enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> responseSignClient) {
                                        if (responseSignClient.isSuccessful()) {
                                            String responseSignClientBody = responseSignClient.body();
                                            Log.e(TAG, "onResponse: responseSignClientBody=" + responseSignClientBody);

//                                =========== Envoi de la signature du COMMERCIAL
                                            progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.envoi_signature_commercial)));

//                                            recuperation de la signature commercial en bitmap
                                            Bitmap signCommBitmap = mCommSignatureView.getSignatureBitmap();

//                                            conversion de la signature commercial en base64
                                            ByteArrayOutputStream baosComm = new ByteArrayOutputStream();
                                            signCommBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosComm);
                                            byte[] bytesSignComm = baosComm.toByteArray();
                                            String encodeSignComm = Base64.encodeToString(bytesSignComm, Base64.NO_WRAP);
                                            String filenameComm = String.format("%s_signature-commercial.jpeg", refOrder);

//                                creation du document signature client
                                            Document signComm = new Document();
                                            signComm.setFilecontent(encodeSignComm);
                                            signComm.setFilename(filenameComm);
                                            signComm.setFileencoding("base64");
                                            signComm.setModulepart("commande");

                                            Call<String> callUploadSignComm = ApiUtils.getISalesService(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this).uploadDocument(signComm);
                                            callUploadSignComm.enqueue(new Callback<String>() {
                                                @Override
                                                public void onResponse(Call<String> call, Response<String> responseSignComm) {
                                                    if (responseSignComm.isSuccessful()) {
                                                        String responseSignCommBody = responseSignComm.body();
                                                        Log.e(TAG, "onResponse: responseSignCommBody=" + responseSignCommBody);
                                                        progressDialog.dismiss();

                                                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.commande_enregistre_succes), Toast.LENGTH_LONG).show();

//                                                        Suppression du panier s'il ne s'agit pas de la relance de commande
                                                        if (mCmdeParcelable == null) {
//                                                        Suppression du panier
                                                            mDb.panierDao().deleteAllPanier();
                                                        }

//                                                        retour a la page d'accueil
                                                        finish();
                                                    } else {
                                                        progressDialog.dismiss();

                                                        try {
                                                            Log.e(TAG, "uploadDocument onResponse SignComm err: message=" + responseSignComm.message() +
                                                                    " | code=" + responseSignComm.code() + " | code=" + responseSignComm.errorBody().string());
                                                        } catch (IOException e) {
                                                            Log.e(TAG, "onResponse: message=" + e.getMessage());
                                                        }
                                                        if (responseSignComm.code() == 404) {
                                                            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                        if (responseSignComm.code() == 401) {
                                                            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                                                            return;
                                                        } else {
                                                            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                                            return;
                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onFailure(Call<String> call, Throwable t) {
                                                    Log.e(TAG, "onFailure:uploadDocumentComm Throwable="+t.getMessage() );
                                                    progressDialog.dismiss();

                                                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                                                    return;

                                                }
                                            });

                                        } else {
                                            progressDialog.dismiss();

                                            try {
                                                Log.e(TAG, "uploadDocument onResponse SignClient err: message=" + responseSignClient.message() +
                                                        " | code=" + responseSignClient.code() + " | code=" + responseSignClient.errorBody().string());
                                            } catch (IOException e) {
                                                Log.e(TAG, "onResponse: message=" + e.getMessage());
                                            }
                                            if (responseSignClient.code() == 404) {
                                                Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                            if (responseSignClient.code() == 401) {
                                                Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                                                return;
                                            } else {
                                                Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                                return;
                                            }
                                        }

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {
                                        Log.e(TAG, "onFailure:uploadDocumentCLient Throwable="+t.getMessage() );
                                        progressDialog.dismiss();

                                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                                        return;

                                    }
                                });
                            } else {
                                progressDialog.dismiss();

                                try {
                                    Log.e(TAG, "validateCustomerOrder onResponse err: message=" + responseValidate.message() +
                                            " | code=" + responseValidate.code() + " | code=" + responseValidate.errorBody().string());
                                } catch (IOException e) {
                                    Log.e(TAG, "onResponse: message=" + e.getMessage());
                                }
                                if (responseValidate.code() == 404) {
                                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (responseValidate.code() == 401) {
                                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }

                        }

                        @Override
                        public void onFailure(Call<Order> call, Throwable t) {
                            Log.e(TAG, "onFailure:validateCustomerOrder Throwable="+t.getMessage() );
                            progressDialog.dismiss();

                            Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                            return;
                        }
                    });
                } else {
                    progressDialog.dismiss();

                    try {
                        Log.e(TAG, "pushCommande onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                    }
                    if (response.code() == 404) {
                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.code() == 401) {
                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                Log.e(TAG, "onFailure:saveCustomerOrder Throwable="+t.toString()+" | message="+t.getMessage() );
                progressDialog.dismiss();

                Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                return;
            }
        });
    }


    private CharSequence[] getPaymentsSequence() {
        paymentTypesEntries = mDb.paymentTypesDao().getAllPayments();
//        Log.e(TAG, "onCreate: getPaymentsSequence() serverEntries="+serverEntries.size());
        CharSequence[] items = new String[paymentTypesEntries.size()];
        for (int i = 0; i < paymentTypesEntries.size(); i++) {
            items[i] = paymentTypesEntries.get(i).getLabel();
        }
//        Log.e(TAG, "onCreate: getPaymentsSequence() after serverEntries="+items.length);

        return items;

    }

    private void showPaymentsSelect() {
        final int[] exportChoice = {-1};
        AlertDialog.Builder builder = new AlertDialog.Builder(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this);
        builder.setTitle("Veuillez sélectioner le mode de règlement");
        builder.setSingleChoiceItems(getPaymentsSequence(), -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                exportChoice[0] = item;
//                Toast.makeText(getApplicationContext(), FlashInventoryUtility.getExportFormat()[item], Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("VALIDER",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (exportChoice[0] >= 0) {
                            paymentTypesChoosed = paymentTypesEntries.get(exportChoice[0]);

                            mModeReglementTV.setText(paymentTypesChoosed.getLabel());
//                            mServerET.setSelection(mServerET.getText().toString().length());  696492069
                        }
                    }
                });
        builder.setNegativeButton("ANNULER",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bon_cmde_signature);

        mDb = AppDatabase.getInstance(getApplicationContext());

//        Recuperation du commercial connecte
        List<UserEntry> userEntries = mDb.userDao().getUser();
        if (userEntries == null || userEntries.size() <= 0) {
            finish();
            return;
        }
        mUserEntry = userEntries.get(0);

        if (getIntent().getExtras().getParcelable("commande") != null) {
            mCmdeParcelable = getIntent().getExtras().getParcelable("commande");
            Log.e(TAG, "onCreate: " + mCmdeParcelable.getRef() +
                    " productsSize=" + mCmdeParcelable.getProduits().size() +
                    " clientID=" + mCmdeParcelable.getSocid());

//        Recupertion du client dans la BD
            ClientEntry clientEntry = mDb.clientDao().getClientById(mCmdeParcelable.getSocid());
            mClientParcelableSelected = new ClientParcelable();
            mClientParcelableSelected = mCmdeParcelable.getClient();

            mTotalPanier = 0;
            for (int i = 0; i < mCmdeParcelable.getProduits().size(); i++) {
                ProduitParcelable produitParcelable = mCmdeParcelable.getProduits().get(i);
                Log.e(TAG, "onCreate: price_ttc=" + produitParcelable.getPrice_ttc() + " quantite=" + produitParcelable.getQty());
                mTotalPanier += Double.parseDouble(produitParcelable.getPrice_ttc()) * Integer.parseInt(produitParcelable.getQty());
            }
        } else {
            mClientParcelableSelected = getIntent().getExtras().getParcelable("client");
            mTotalPanier = getIntent().getExtras().getDouble("totalPanier");
            mCmdeParcelable = null;
        }

//        referencement des vues
        mClientSignatureView = (SignatureView) findViewById(R.id.signatureview_boncmde_signature_client);
        mCommSignatureView = (SignatureView) findViewById(R.id.signatureview_boncmde_signature_commercial);
        mAnnulerSignCommBTN = (Button) findViewById(R.id.btn_boncmde_signature_commercial_annuler);
        mAnnulerSignClientBTN = (Button) findViewById(R.id.btn_boncmde_signature_client_annuler);
        mEnregistrerBTN = (Button) findViewById(R.id.btn_boncmde_signature_enregistrer);
        mClientName = (TextView) findViewById(R.id.tv_boncmde_signature_client_name);
        mCommName = (TextView) findViewById(R.id.tv_boncmde_signature_commercial_name);
        mSynchroServeurSW = (Switch) findViewById(R.id.switch_boncmde_signature);
        mDateLivraisonVIEW = findViewById(R.id.view_boncmde_datelivraison);
        mDateLivraisonTV = (TextView) findViewById(R.id.tv_boncmde_datelivraison);
        mModeReglementVIEW = findViewById(R.id.view_boncmde_modereglement);
        mModeReglementTV = (TextView) findViewById(R.id.tv_boncmde_modereglement);
        mAcompteET = (EditText) findViewById(R.id.et_boncmde_acompte);
        mRemiseET = (EditText) findViewById(R.id.et_boncmde_remise);

//        Définition des dates courantes
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat yearformat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthformat = new SimpleDateFormat("M");
        SimpleDateFormat dayformat = new SimpleDateFormat("d");
        todayYear = Integer.parseInt(yearformat.format(calendar.getTime()));
        todayMonth = Integer.parseInt(monthformat.format(calendar.getTime())) - 1;
        todayDay = Integer.parseInt(dayformat.format(calendar.getTime()));

        Log.e(TAG, "onCreate: mSynchroServeurSW=" + mSynchroServeurSW.isChecked());

        mClientName.setText(String.format("%s", mClientParcelableSelected.getName()));
        mCommName.setText(String.format("%s %s", ISalesUtility.strCapitalize(mUserEntry.getFirstname()), mUserEntry.getLastname().toUpperCase()));
        mDateLivraisonTV.setText("Chosir une date ");

//        suppresion de la signature lorsqu'on clique sur le btn annuler
        mAnnulerSignCommBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCommSignatureView.clearCanvas();
            }
        });
        mAnnulerSignClientBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClientSignatureView.clearCanvas();
            }
        });

//        Selection de la date de livraison
        mDateLivraisonVIEW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new SpinnerDatePickerDialogBuilder()
                        .context(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this)
                        .callback(new com.tsongkha.spinnerdatepicker.DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(com.tsongkha.spinnerdatepicker.DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {
                                calLivraison = Calendar.getInstance();
                                calLivraison.set(year, monthOfYear, dayOfMonth);
                                livraisonDay = dayOfMonth;
                                livraisonMonth = monthOfYear;
                                livraisonYear = year;

//                                Log.e(TAG, " dateLivraison=" + calLivraison.getTimeInMillis());

                                SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
                                String stringDateSet = dateformat.format(calLivraison.getTime());
                                mDateLivraisonTV.setText(stringDateSet);
                            }
                        })
                        .showTitle(true)
                        .showDaySpinner(true)
                        .defaultDate(todayYear, todayMonth, todayDay)
                        .minDate(todayYear, todayMonth, todayDay)
                        .build()
                        .show();
            }
        });
        mModeReglementVIEW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPaymentsSelect();
            }
        });

//        Enregistrement de la commande
        mEnregistrerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientSignatureView.getSignatureBitmap() == null) {
                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, "Le client doit renseigner sa signature", Toast.LENGTH_LONG).show();

                    return;
                }
                if (mCommSignatureView.getSignatureBitmap() == null) {
                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, "Le commercial doit renseigner sa signature", Toast.LENGTH_LONG).show();

                    return;
                }
                if (calLivraison == null) {
                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, "Veuillez choisir la date de livraison.", Toast.LENGTH_LONG).show();

                    return;
                }
                if (paymentTypesChoosed == null) {
                    Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, "Veuillez choisir le mode de règlement.", Toast.LENGTH_LONG).show();

                    return;
                }


                if (!mRemiseET.getText().toString().equals("")) {
                    double remisePercent = Double.parseDouble(mRemiseET.getText().toString().replace(",", "."));
                    if (remisePercent > 100) {
                        Toast.makeText(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, getString(R.string.remise_doit_etre_inferieure_cent), Toast.LENGTH_LONG).show();

                        return;
                    }
                }

                if (mClientParcelableSelected.getIs_synchro() == 0) {
                    ClientEntry clientEntry = mDb.clientDao().getClientById(mClientParcelableSelected.getId());
                    Log.e(TAG, "onClick:mEnregistrerBTN saveClient logopath="+clientEntry.getLogo()+" logoContent="+clientEntry.getLogo_content());
//        recuperation du logo en bitmap
//                    Bitmap logoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mClientParcelableSelected.getLogo());;
                    Bitmap logoBitmap = BitmapFactory.decodeFile(mClientParcelableSelected.getLogo());

//        creation du document signature client
                    Document logoClient = null;
                    if (logoBitmap != null) {
                        logoClient = new Document();

                        logoClient.setFilecontent(clientEntry.getLogo_content());
                        logoClient.setFilename(clientEntry.getLogo());
                        logoClient.setFileencoding("base64");
                        logoClient.setModulepart("societe");
                    }

                    Thirdpartie thirdpartie = new Thirdpartie();
                    thirdpartie.setAddress(clientEntry.getAddress());
                    thirdpartie.setTown(clientEntry.getTown());
                    thirdpartie.setRegion(clientEntry.getRegion());
                    thirdpartie.setDepartement(clientEntry.getDepartement());
                    thirdpartie.setPays(clientEntry.getPays());
                    thirdpartie.setPhone(clientEntry.getPhone());
                    thirdpartie.setNote(clientEntry.getNote());
                    thirdpartie.setNote_private(clientEntry.getNote());
                    thirdpartie.setLogo(clientEntry.getLogo());
                    thirdpartie.setEmail(clientEntry.getEmail());
                    thirdpartie.setFirstname(clientEntry.getFirstname());
                    thirdpartie.setName(String.format("%s", clientEntry.getName()));
                    thirdpartie.setCode_client(clientEntry.getCode_client());
                    thirdpartie.setClient("1");
                    thirdpartie.setName_alias("");

//        Si le téléphone n'est pas connecté
                    if (ConnectionManager.isPhoneConnected(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this)) {
                        InsertThirdpartieTask insertThirdpartieTask = new InsertThirdpartieTask(com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, com.iSales.pages.boncmdesignature.BonCmdeSignatureActivity.this, thirdpartie, logoClient);
                        insertThirdpartieTask.execute();
                    }
                } else {
                    Log.e(TAG, "onClick:mEnregistrerBTN directly pushCmde");

                    pushCommande();
                }

            }
        });
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
    public void onInsertThirdpartieCompleted(InsertThirdpartieREST insertThirdpartieREST) {

        if (insertThirdpartieREST != null) {
            Log.e(TAG, "onInsertThirdpartieCompleted: id="+insertThirdpartieREST.getThirdpartie_id() );

            pushCommande();
        }
    }
}
