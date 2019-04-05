package com.iSales.pages.synchronisation;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.CategorieEntry;
import com.iSales.database.entry.ClientEntry;
import com.iSales.database.entry.CommandeEntry;
import com.iSales.database.entry.CommandeLineEntry;
import com.iSales.database.entry.ProduitEntry;
import com.iSales.database.entry.SignatureEntry;
import com.iSales.interfaces.FindCategorieListener;
import com.iSales.interfaces.FindOrdersListener;
import com.iSales.interfaces.FindProductsListener;
import com.iSales.interfaces.FindThirdpartieListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Categorie;
import com.iSales.remote.model.Document;
import com.iSales.remote.model.Order;
import com.iSales.remote.model.OrderLine;
import com.iSales.remote.model.Product;
import com.iSales.remote.model.Thirdpartie;
import com.iSales.remote.rest.FindCategoriesREST;
import com.iSales.remote.rest.FindOrderLinesREST;
import com.iSales.remote.rest.FindOrdersREST;
import com.iSales.remote.rest.FindProductsREST;
import com.iSales.remote.rest.FindThirdpartieREST;
import com.iSales.task.FindCategorieTask;
import com.iSales.task.FindOrderLinesTask;
import com.iSales.task.FindOrderTask;
import com.iSales.task.FindProductsTask;
import com.iSales.task.FindThirdpartieTask;
import com.iSales.task.SendDocumentTask;
import com.iSales.task.SendOrderTask;
import com.iSales.utility.ISalesUtility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SynchronisationActivity extends AppCompatActivity
        implements FindThirdpartieListener, FindProductsListener, FindCategorieListener, FindOrdersListener {

    private static final String TAG = com.iSales.pages.synchronisation.SynchronisationActivity.class.getSimpleName();
    private Button mSyncClientBtn, mSyncProduitsBtn, mSyncCmdeRecupererBtn, mSyncCmdePousserBtn;

    ProgressDialog mProgressDialog;

    //    task de recuperation des clients
    private FindThirdpartieTask mFindClientTask = null;
    //    task de recuperation des produits
    private FindProductsTask mFindProductsTask = null;
    //    task de recuperation des categories
    private FindCategorieTask mFindCategorieTask = null;
    //    task de recuperation des commandes
    private FindOrderTask mFindOrderTask = null;
    private FindOrderLinesTask mFindOrderLinesTask = null;

    private int mLimit = 10;
    private int mPageClient = 0;
    private int mPageCategorie = 0;
    private int mPageOrder = 0;

    //    position courante de la requete de recuperation des produit
    private int mCurrentPdtQuery = 0;
    private int mTotalPdtQuery = 0;

    //    database instance
    private AppDatabase mDb;

    /**
     * Shows the progress UI and hides.
     */
    private void showProgressDialog(boolean show, String title, String message) {

        if (show) {
            Log.e(TAG, "showProgressDialog: show dialog");
            mProgressDialog = new ProgressDialog(com.iSales.pages.synchronisation.SynchronisationActivity.this);
            if (title != null) mProgressDialog.setTitle(title);
            if (message != null) mProgressDialog.setMessage(message);

            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
            mProgressDialog.show();
        } else {
            Log.e(TAG, "showProgressDialog: not show dialog");
            mProgressDialog.dismiss();
        }
    }

    //    Recupération de la liste des produits
    private void executeFindClients() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

//        Suppression des images des clients en local
        ISalesUtility.deleteClientsImgFolder();

        if (mFindClientTask == null) {

            mFindClientTask = new FindThirdpartieTask(com.iSales.pages.synchronisation.SynchronisationActivity.this, com.iSales.pages.synchronisation.SynchronisationActivity.this, mLimit, mPageClient, ApiUtils.THIRDPARTIE_CLIENT);
            mFindClientTask.execute();
        }
    }

    //    Recupération de la liste des produits
    private void executeFindProducts() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }

//        Suppression des images des produits en local
        ISalesUtility.deleteProduitsImgFolder();

        List<CategorieEntry> categorieEntries = mDb.categorieDao().getAllCategories();
//            modification de la position de la requete totale de recupération des produits
        mTotalPdtQuery = categorieEntries.size();

        Log.e(TAG, "executeFindProducts: categorieEntries=" + categorieEntries.size() +
                " mTotalPdtQuery=" + mTotalPdtQuery);
        int i = 0;
        while (i < categorieEntries.size()) {
//            modification de la position de la requete courante de recupération des produits
            mCurrentPdtQuery = i;

            CategorieEntry categorieEntry = categorieEntries.get(i);

            Log.e(TAG, "executeFindProducts: mCurrentPdtQuery=" + mCurrentPdtQuery +
                    " categorieID=" + categorieEntry.getId() +
                    " categorieLabel=" + categorieEntry.getLabel());

            FindProductsTask findProductsTask = new FindProductsTask(com.iSales.pages.synchronisation.SynchronisationActivity.this, com.iSales.pages.synchronisation.SynchronisationActivity.this, "label", "asc", 0, -1, categorieEntry.getId());
            findProductsTask.execute();

            i++;
        }

        /*
        if (mFindProductsTask == null) {

            mFindProductsTask = new FindProductsTask(SynchronisationActivity.this, SynchronisationActivity.this, "label", "asc", mLimit, mPageProduit, -1);
            mFindProductsTask.execute();
        } */
    }

    //    Recupération de la liste des categories produits
    private void executeFindCategorieProducts() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindCategorieTask == null) {

            Log.e(TAG, "executeFindCategorieProducts: page=" + mPageCategorie);
            mFindCategorieTask = new FindCategorieTask(com.iSales.pages.synchronisation.SynchronisationActivity.this, com.iSales.pages.synchronisation.SynchronisationActivity.this, "label", "asc", mLimit, mPageCategorie, "product");
            mFindCategorieTask.execute();
        }
    }

    //    Recupération de la liste des produits
    private void executeFindOrder() {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mFindOrderTask == null) {
            Log.e(TAG, "executeFindOrder: executing");

            mFindOrderTask = new FindOrderTask(com.iSales.pages.synchronisation.SynchronisationActivity.this, com.iSales.pages.synchronisation.SynchronisationActivity.this, "date_creation", "asc", mLimit, mPageOrder);
            mFindOrderTask.execute();
        }
    }

    //    Recupération de la liste des lignes d'un produits
    private void executeFindOrderLines(long orderid, long cmdeRef) {

//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            showProgressDialog(false, null, null);
            return;
        }

        if (mFindOrderLinesTask == null) {
//            Log.e(TAG, "executeFindOrderLines: executing");

            mFindOrderLinesTask = new FindOrderLinesTask(com.iSales.pages.synchronisation.SynchronisationActivity.this, orderid, cmdeRef, com.iSales.pages.synchronisation.SynchronisationActivity.this);
            mFindOrderTask.execute();
        }
    }
    //    Envoi de la liste des commandes sur le serveur
    private void executeSendOrder() {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.synchronisation.SynchronisationActivity.this)) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }

//        Recuperation des commandes non synchronisées
        List<CommandeEntry> commandeEntries = mDb.commandeDao().getAllCmdeNotSynchro();
        Log.e(TAG, "executeSendOrder: commandeEntries size=" + commandeEntries.size());
        for (int i = 0; i < commandeEntries.size(); i++) {
            CommandeEntry cmdeEntryItem = commandeEntries.get(i);
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Order order = new Order();

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
            order.setLines(new ArrayList<OrderLine>());

//            Recupération de la listes des produits de la commande
            List<CommandeLineEntry> cmdeLineEntryList = mDb.commandeLineDao().getAllCmdeLineByCmdeRef(cmdeEntryItem.getCommande_id());
            List<SignatureEntry> signatureEntries = mDb.signatureDao().getAllSignatureByCmdeRef(cmdeEntryItem.getCommande_id());
            Log.e(TAG, "executeSendOrder: cmdeLineEntryList size=" + cmdeLineEntryList.size() +
                    " signatureEntries size=" + signatureEntries.size());

//            Ajout des lignes commande dans la commande
            for (int j = 0; j < cmdeLineEntryList.size(); j++) {
                OrderLine orderLine = new OrderLine();
                CommandeLineEntry cmdeLineEntry = cmdeLineEntryList.get(j);

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
            Document sign1 = new Document();
            sign1.setFilecontent(signatureEntries.get(0).getContent());
            sign1.setFilename(signatureEntries.get(0).getName());
            sign1.setFileencoding("base64");
            sign1.setModulepart("commande");

//                                creation du document signature client
            Document sign2 = new Document();
            sign2.setFilecontent(signatureEntries.get(1).getContent());
            sign2.setFilename(signatureEntries.get(1).getName());
            sign2.setFileencoding("base64");
            sign2.setModulepart("commande");

//            execution de la task d'envoi de la commande sur le servur
            SendOrderTask sendOrderTask = new SendOrderTask(order, com.iSales.pages.synchronisation.SynchronisationActivity.this);
            sendOrderTask.execute();

//            execution task envoi des signatures
            SendDocumentTask sendDocumentTask1 = new SendDocumentTask(sign1, com.iSales.pages.synchronisation.SynchronisationActivity.this);
            sendDocumentTask1.execute();
            SendDocumentTask sendDocumentTask2 = new SendDocumentTask(sign2, com.iSales.pages.synchronisation.SynchronisationActivity.this);
            sendDocumentTask2.execute();

            mDb.commandeDao().deleteCmde(cmdeEntryItem);
//            fermeture du loader
            if ((i + 1) == commandeEntries.size()) {
                Log.e(TAG, "executeSendOrder: i="+i);

                Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
//                Mise a jour des données en local
                executeFindOrder();

                showProgressDialog(false, null, null);
            }
        }

    }


    @Override
    public void onFindThirdpartieCompleted(FindThirdpartieREST findThirdpartieREST) {
        mFindClientTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findThirdpartieREST == null) {
            Log.e(TAG, "onFindThirdpartieCompleted: findThirdpartieREST getThirdparties null");
            //        Fermeture du loader
            showProgressDialog(false, null, null);
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findThirdpartieREST.getThirdparties() == null) {
            Log.e(TAG, "onFindThirdpartieCompleted: findThirdpartieREST getThirdparties null");
            //        Fermeture du loader
            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageClient = 0;

            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.comptes_clients_synchronises), Toast.LENGTH_LONG).show();

            return;
        }

        Log.e(TAG, "onFindThirdpartieCompleted: getThirdparties size=" + findThirdpartieREST.getThirdparties().size());
        for (Thirdpartie thirdpartie : findThirdpartieREST.getThirdparties()) {
            ClientEntry clientEntry = new ClientEntry();

            clientEntry.setName(thirdpartie.getName());
            clientEntry.setName_alias(thirdpartie.getName_alias());
            clientEntry.setFirstname(thirdpartie.getFirstname());
            clientEntry.setLastname(thirdpartie.getLastname());
            clientEntry.setAddress(thirdpartie.getAddress());
            clientEntry.setTown(thirdpartie.getTown());
            clientEntry.setLogo(thirdpartie.getName_alias());
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
            clientEntry.setNote(thirdpartie.getNote());
            clientEntry.setNote_private(thirdpartie.getNote_private());
            clientEntry.setNote_public(thirdpartie.getNote_public());

            if (mDb.clientDao().getClientById(clientEntry.getId()) == null) {
                Log.e(TAG, "onFindThirdpartieCompleted: insert clientEntry");
//            insertion du client dans la BD
                mDb.clientDao().insertClient(clientEntry);
            } else {
                Log.e(TAG, "onFindThirdpartieCompleted: update clientEntry");
//            mise a jour du client dans la BD
                mDb.clientDao().updateClient(clientEntry);
            }
        }

        Log.e(TAG, "onFindThirdpartieCompleted: mPageClient=" + mPageClient);
//        incrementation du nombre de page
        mPageClient++;

        executeFindClients();
    }

    @Override
    public void onFindThirdpartieByIdCompleted(Thirdpartie thirdpartie) {

    }

    @Override
    public void onFindProductsCompleted(FindProductsREST findProductsREST) {
        /* mFindProductsTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findProductsREST == null) {
            //        Fermeture du loader
            showProgressDialog(false, null, null);
            Toast.makeText(SynchronisationActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findProductsREST.getProducts() == null) {
            Log.e(TAG, "onFindProductsCompleted: FindProductsREST getThirdparties null");
            //        Fermeture du loader
            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageProduit = 0;

            Toast.makeText(SynchronisationActivity.this, getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();

            return;
        } */

        if (findProductsREST != null && findProductsREST.getProducts() != null) {
            Log.e(TAG, "onFindProductsCompleted: saving product categorie=" + findProductsREST.getCategorie_id() + " pdtSize=" + findProductsREST.getProducts().size());
            for (Product productItem : findProductsREST.getProducts()) {
                ProduitEntry produitEntry = new ProduitEntry();
                produitEntry.setId(Long.parseLong(productItem.getId()));
                produitEntry.setCategorie_id(findProductsREST.getCategorie_id());
                produitEntry.setLabel(productItem.getLabel());
                produitEntry.setPrice(productItem.getPrice());
                produitEntry.setPrice_ttc(productItem.getPrice_ttc());
                produitEntry.setRef(productItem.getRef());
                produitEntry.setStock_reel(productItem.getStock_reel());
                produitEntry.setDescription(productItem.getDescription());
                produitEntry.setNote(productItem.getNote());
                produitEntry.setNote_public(productItem.getNote_public());
                produitEntry.setNote_private(productItem.getNote_private());

                Log.e(TAG, "onFindProductsCompleted: product name=" + produitEntry.getLabel());
                if (mDb.produitDao().getProduitById(produitEntry.getId()) == null) {
                    Log.e(TAG, "onFindThirdpartieCompleted: insert produitEntry");
//            insertion du client dans la BD
                    mDb.produitDao().insertProduit(produitEntry);
                } else {
                    Log.e(TAG, "onFindThirdpartieCompleted: update produitEntry");
//            mise a jour du client dans la BD
                    mDb.produitDao().updateProduit(produitEntry);
                }
            }
            Log.e(TAG, "onFindProductsCompleted: mPage=" + mCurrentPdtQuery);

            if (mCurrentPdtQuery >= mTotalPdtQuery - 1) {

//        Fermeture du loader
                showProgressDialog(false, null, null);
            }
        } else {

            Log.e(TAG, "onFindProductsCompleted: FindProductsREST getThirdparties null");

            if (mCurrentPdtQuery >= mTotalPdtQuery - 1) {

//        Fermeture du loader
                showProgressDialog(false, null, null);
            }

            //        Fermeture du loader
            showProgressDialog(false, null, null);

            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();

            return;
        }

    }

    @Override
    public void onFindAllProductsCompleted() {

    }

    @Override
    public void onFindCategorieCompleted(FindCategoriesREST findCategoriesREST) {
        mFindCategorieTask = null;

//        Si la recupération echoue, on renvoi un message d'erreur
        if (findCategoriesREST == null) {
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findCategoriesREST.getCategories() == null) {
            Log.e(TAG, "onFindProductsCompleted: findCategoriesREST findCategoriesREST null");
//            reinitialisation du nombre de page
            mPageCategorie = 0;

            executeFindProducts();

//            Toast.makeText(SynchronisationActivity.this, getString(R.string.liste_produits_synchronises), Toast.LENGTH_LONG).show();

            return;
        }

        for (Categorie categorieItem : findCategoriesREST.getCategories()) {
            CategorieEntry categorieEntry = new CategorieEntry();
            categorieEntry.setId(Long.parseLong(categorieItem.getId()));
            categorieEntry.setLabel(categorieItem.getLabel());
            categorieEntry.setDescription(categorieItem.getDescription());
            categorieEntry.setPoster_name(ISalesUtility.getImgProduit(categorieItem.getDescription()));

            if (mDb.categorieDao().getCategorieById(categorieEntry.getId()) == null) {
                Log.e(TAG, "onFindThirdpartieCompleted: insert categorieEntry");
//            insertion du client dans la BD
                mDb.categorieDao().insertCategorie(categorieEntry);
            } else {
                Log.e(TAG, "onFindThirdpartieCompleted: update categorieEntry");
//            mise a jour du client dans la BD
                mDb.categorieDao().updateCategorie(categorieEntry);
            }
        }
        Log.e(TAG, "onFindCategorieCompleted: mPage=" + mPageCategorie);

        mPageCategorie++;
        executeFindCategorieProducts();
    }

    @Override
    public void onFindOrderLinesTaskComplete(long commande_ref, long commande_id, FindOrderLinesREST findOrderLinesREST) {
        mFindOrderLinesTask = null;

//            chargement des produits de la commande
        if (findOrderLinesREST.getLines() != null) {

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
            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (findOrdersREST.getOrders() == null) {
//            Log.e(TAG, "onFindOrdersTaskComplete: findOrderTaskComplete getThirdparties null");
            //        Fermeture du loader
            showProgressDialog(false, null, null);
//            reinitialisation du nombre de page
            mPageOrder = 0;

            Toast.makeText(com.iSales.pages.synchronisation.SynchronisationActivity.this, getString(R.string.commandes_synchronises), Toast.LENGTH_LONG).show();

            return;
        }

        for (Order orderItem : findOrdersREST.getOrders()) {
            CommandeEntry cmdeEntry = new CommandeEntry();
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
                long timestamp = Long.parseLong(orderItem.getDate_livraison()) * 1000L;
                cmdeEntry.setDate_livraison(timestamp);
            } else cmdeEntry.setDate_livraison((long) 0);

            cmdeEntry.setTotal_ttc(orderItem.getTotal_ttc());
            cmdeEntry.setIs_synchro(1);

            CommandeEntry testCmde = mDb.commandeDao().getCmdesById(cmdeEntry.getId());
            if (testCmde == null) {
//                Log.e(TAG, "onFindOrdersTaskComplete: insert CommandeEntry");
//            insertion du client dans la BD
                long cmdeEntryId = mDb.commandeDao().insertCmde(cmdeEntry);
//                Log.e(TAG, "onFindOrdersTaskComplete: " + cmdeEntryId);

//                executeFindOrderLines(cmdeEntry.getId(), cmdeEntryId);

//            chargement des produits de la commande
                for (OrderLine orderLine : orderItem.getLines()) {
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
                    cmdeLineEntry.setCommande_ref(cmdeEntryId);

//                    Log.e(TAG, "onFindOrdersTaskComplete: product name=" + cmdeLineEntry.getLabel());
//            insertion de la commandeLine dans la BD
                    mDb.commandeLineDao().insertCmdeLine(cmdeLineEntry);
                }
            } else {
//                Log.e(TAG, "onFindOrdersTaskComplete: CommandeEntry already exist " + cmdeEntry.getRef());
//            insertion du client dans la BD
                mDb.commandeDao().updateCmde(cmdeEntry);

//                executeFindOrderLines(cmdeEntry.getId(), testCmde.getId());

//            chargement des produits de la commande
                for (OrderLine orderLine : orderItem.getLines()) {
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
                    cmdeLineEntry.setCommande_ref(cmdeEntry.getId());

//                    Log.e(TAG, "onFindOrdersTaskComplete: product name=" + cmdeLineEntry.getLabel());
//            insertion de la commandeLine dans la BD
                    CommandeLineEntry testCmdeLine = mDb.commandeLineDao().getCmdeLineById(cmdeLineEntry.getId());
                    if (testCmdeLine == null) {
                        mDb.commandeLineDao().insertCmdeLine(cmdeLineEntry);
                    } else {
                        mDb.commandeLineDao().updateCmdeLine(cmdeLineEntry);
                    }
                }
            }


        }

//        incrementation du nombre de page
        mPageOrder++;

        executeFindOrder();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronisation);

        mDb = AppDatabase.getInstance(getApplicationContext());

//        referencement des vues
        mSyncClientBtn = (Button) findViewById(R.id.btn_synchro_client);
        mSyncProduitsBtn = (Button) findViewById(R.id.btn_synchro_produits);
        mSyncCmdeRecupererBtn = (Button) findViewById(R.id.btn_recuperer_cmde);
        mSyncCmdePousserBtn = (Button) findViewById(R.id.btn_pousser_cmde);

        mSyncClientBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDb.clientDao().deleteAllClient();

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_comptes_cient_encours));

                executeFindClients();
            }
        });

        mSyncProduitsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDb.categorieDao().deleteAllCategorie();
                mDb.produitDao().deleteAllProduit();

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_produits_encours));

                executeFindCategorieProducts();
            }
        });

        mSyncCmdeRecupererBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDb.commandeDao().deleteAllCmde();
                mDb.commandeLineDao().deleteAllCmdeLine();

//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_commandes_recuperer_encours));

                executeFindOrder();
            }
        });

        mSyncCmdePousserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                affichage du loader dialog
                showProgressDialog(true, null, getString(R.string.synchro_commandes_envoi_encours));

                executeSendOrder();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
