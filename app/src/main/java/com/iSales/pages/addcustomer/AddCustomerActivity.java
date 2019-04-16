package com.iSales.pages.addcustomer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.interfaces.AddCustomerListener;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Document;
import com.iSales.remote.model.Thirdpartie;
import com.iSales.utility.ISalesUtility;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCustomerActivity extends AppCompatActivity {
    private static final String TAG = com.iSales.pages.addcustomer.AddCustomerActivity.class.getSimpleName();
    private View mEnregistrerView;
    private EditText mNomEntrepriseET, mAdresseET, mEmailET, mTelephoneET, mNoteET, mVilleET, mDepartementET, mRegionET, mPaysET;
    private ImageView mSelectLogoIV;
    private TextView mLogoNameTV;
    private Switch mSwitchSynchro;
    private Bitmap mLogoBitmap;
    private String mLogoBitmapPath;

    private AppDatabase mDb;

    //    add customer listener
    private static AddCustomerListener addCustomerListener = new AddCustomerListener() {
        @Override
        public void onCustomerAdded() {

        }
    };

    private void validateForm() {

        // Reset errors.
        mNomEntrepriseET.setError(null);
        mAdresseET.setError(null);
        mEmailET.setError(null);
        mTelephoneET.setError(null);
        mVilleET.setError(null);
        mDepartementET.setError(null);
        mRegionET.setError(null);
        mPaysET.setError(null);

        // Store values at the time of the login attempt.
        String nom = mNomEntrepriseET.getText().toString();
        String adresse = mAdresseET.getText().toString();
        String email = mEmailET.getText().toString();
        String telephone = mTelephoneET.getText().toString();
        String note = mNoteET.getText().toString();
        String ville = mVilleET.getText().toString();
        String departement = mDepartementET.getText().toString();
        String region = mRegionET.getText().toString();
        String pays = mPaysET.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Test de validité du nom
        if (TextUtils.isEmpty(nom)) {
            mNomEntrepriseET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mNomEntrepriseET;
            cancel = true;
        }
        // Test de validité de l'adresse
        /*if (TextUtils.isEmpty(adresse) && !cancel) {
            mAdresseET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mAdresseET;
            cancel = true;
        }
        // Test de validité de l'email
        if (TextUtils.isEmpty(email) && !cancel) {
            mEmailET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mEmailET;
            cancel = true;
        }
        if (!ISalesUtility.isValidEmail(email) && !cancel) {
            mEmailET.setError(getString(R.string.adresse_mail_invalide));
            focusView = mEmailET;
            cancel = true;
        }
        // Test de validité du telephone
        if (TextUtils.isEmpty(telephone) && !cancel) {
            mTelephoneET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mTelephoneET;
            cancel = true;
        }
        // Test de validité du pays
        if (TextUtils.isEmpty(pays) && !cancel) {
            mPaysET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mPaysET;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(region) && !cancel) {
            mRegionET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mRegionET;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(departement) && !cancel) {
            mDepartementET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mDepartementET;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(ville) && !cancel) {
            mVilleET.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mVilleET;
            cancel = true;
        } */

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        }
//        si le user n'a pas sélectionné le logo
        /*if (mLogoBitmap == null) {
            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.veuillez_choisir_logo), Toast.LENGTH_LONG).show();
            return;
        } else { */
        saveOfflineClient(nom, adresse, email, telephone, note, pays, region, departement, ville);

        if (!mSwitchSynchro.isChecked()) {

            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.client_creee_local_succes), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        saveOnlineClient(nom, adresse, email, telephone, note, pays, region, departement, ville);
//        }
    }

    //    enregistre un client dans le serveur
    private void saveOfflineClient(final String nom, final String adresse, final String email, final String telephone, final String note, final String pays, final String region, final String departement, final String ville) {

        final Date today = new Date();
        final SimpleDateFormat logoFormat = new SimpleDateFormat("yyMMdd-HHmmss");
        String encodeContent = "";
        String filename = "";

        if (mLogoBitmap != null) {
//        recuperation du logo en bitmap
            Bitmap logoBitmap = mLogoBitmap;

//        conversion du logo en base64
            ByteArrayOutputStream baosLogo = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosLogo);
            byte[] bytesSignComm = baosLogo.toByteArray();

            final String logoName = String.format("client_logo_%s", logoFormat.format(today));
            encodeContent = Base64.encodeToString(bytesSignComm, Base64.NO_WRAP);
            filename = String.format("%s.jpeg", logoName);
        }

        ClientEntry clientEntry = new ClientEntry();
        clientEntry.setAddress(adresse);
        clientEntry.setCode_client("");
        clientEntry.setDate_creation(today.getTime());
        clientEntry.setDepartement(departement);
        clientEntry.setEmail(email);
        clientEntry.setFirstname(nom);
        clientEntry.setIs_current(0);
        clientEntry.setIs_synchro(0);
        clientEntry.setLogo(filename);
        clientEntry.setLogo_content(encodeContent);
        clientEntry.setName(nom);
        clientEntry.setPhone(telephone);
        clientEntry.setNote(note);
        clientEntry.setNote_public(note);
        clientEntry.setPays(pays);
        clientEntry.setTown(ville);
        clientEntry.setRegion(region);

        long inserted = mDb.clientDao().insertClient(clientEntry);

        Log.e(TAG, "saveOfflineClient: name=" + clientEntry.getName() +
                " logo=" + clientEntry.getLogo() +
                " logoContent=" + clientEntry.getLogo_content() +
                " inserted=" + inserted);
    }

    //    enregistre un client dans le serveur
    private void saveOnlineClient(final String nom, final String adresse, final String email, final String telephone, final String note, final String pays, final String region, final String departement, final String ville) {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(com.iSales.pages.addcustomer.AddCustomerActivity.this)) {
            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(com.iSales.pages.addcustomer.AddCustomerActivity.this);
//        progressDialog.setTitle("Transfert d'Argent");
        progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.enregistrement_encours)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
        progressDialog.show();

        final Date today = new Date();
        final SimpleDateFormat logoFormat = new SimpleDateFormat("yyMMdd-HHmmss");
//        final String filenameComm = "";

        if (mLogoBitmap != null) {
//            Log.e(TAG, "saveOnlineClient: mLogoBitmap not null" );
//        recuperation du logo en bitmap
            Bitmap logoBitmap = mLogoBitmap;

//        conversion du logo en base64
            ByteArrayOutputStream baosLogo = new ByteArrayOutputStream();
            logoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosLogo);
            byte[] bytesSignComm = baosLogo.toByteArray();

            final String logoName = String.format("client_logo_%s", logoFormat.format(today));
            String encodeSignComm = Base64.encodeToString(bytesSignComm, Base64.NO_WRAP);
            final String filenameComm = String.format("%s.jpeg", logoName);

//        creation du document signature client
            Document logoClient = new Document();
            logoClient.setFilecontent(encodeSignComm);
            logoClient.setFilename(filenameComm);
            logoClient.setFileencoding("base64");
            logoClient.setModulepart("societe");

            Call<String> callUploadLogoClient = ApiUtils.getISalesService(com.iSales.pages.addcustomer.AddCustomerActivity.this).uploadDocument(logoClient);
            callUploadLogoClient.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> responseLogoClient) {
                    if (responseLogoClient.isSuccessful()) {
                        String responseLogoClientBody = responseLogoClient.body();
//                        Log.e(TAG, "onResponse: responseLogoClient=" + responseLogoClientBody);

//                    Date today = new Date();
                        final SimpleDateFormat refOrderFormat = new SimpleDateFormat("yyMMdd-HHmmss");
                        final String codeCLient = String.format("CU%s", refOrderFormat.format(today));

                        Thirdpartie queryBody = new Thirdpartie();
                        queryBody.setAddress(adresse);
                        queryBody.setTown(ville);
                        queryBody.setRegion(region);
                        queryBody.setDepartement(departement);
                        queryBody.setPays(pays);
                        queryBody.setPhone(telephone);
                        queryBody.setNote(note);
                        queryBody.setLogo(filenameComm);
                        queryBody.setNote_private(note);
                        queryBody.setEmail(email);
                        queryBody.setFirstname(nom);
                        queryBody.setName(String.format("%s", nom));
                        queryBody.setCode_client(codeCLient);
                        queryBody.setClient("1");
//                    queryBody.setName_alias(responseLogoClientBody);659331009
                        queryBody.setName_alias("");

                        Call<Long> callSaveClient = ApiUtils.getISalesService(com.iSales.pages.addcustomer.AddCustomerActivity.this).saveThirdpartie(queryBody);
                        callSaveClient.enqueue(new Callback<Long>() {
                            @Override
                            public void onResponse(Call<Long> call, Response<Long> response) {
                                if (response.isSuccessful()) {
                                    progressDialog.dismiss();

                                    Long responseBody = response.body();
//                                    Log.e(TAG, "onResponse:callSaveClient responseBody="+responseBody);
                                    mDb.clientDao().updateSynchroClient(1, responseBody);
                                    Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.client_creee_succes), Toast.LENGTH_LONG).show();
                                    finish();
                                } else {
                                    progressDialog.dismiss();

                                    try {
                                        Log.e(TAG, "doEvaluationTransfert onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                                    } catch (IOException e) {
                                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                                    }
                                    if (response.code() == 404) {
                                        Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    if (response.code() == 401) {
                                        Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                                        return;
                                    } else {
                                        Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<Long> call, Throwable t) {
                                progressDialog.dismiss();

                                Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                                return;
                            }
                        });

                    } else {

                        try {
                            String errBody = responseLogoClient.body();
                            Log.e(TAG, "uploadDocument onResponse LogoClient err: message=" + responseLogoClient.message() +
                                    " | code=" + responseLogoClient.code() + " | code=" + responseLogoClient.errorBody().string() + " errBody=" + errBody);
                        } catch (IOException e) {
                            Log.e(TAG, "onResponse: message=" + e.getMessage());
                        }

//                    if (responseLogoClient.code() == 404) {
//                        Toast.makeText(AddCustomerActivity.this, getString(R.string.echec_envoi_logo_client), Toast.LENGTH_LONG).show();
//                        return;
//                    }
                        if (responseLogoClient.code() == 401) {
                            progressDialog.dismiss();

                            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.echec_envoi_logo_client), Toast.LENGTH_LONG).show();
                        }
                    }

                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    progressDialog.dismiss();

                    Log.e(TAG, "onFailure: message=" + t.getMessage());
                    Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                    return;

                }
            });
        } else {
//            Log.e(TAG, "saveOnlineClient: mLogoBitmap null" );
//                    Date today = new Date();
            final SimpleDateFormat refOrderFormat = new SimpleDateFormat("yyMMdd-HHmmss");
            final String codeCLient = String.format("CU%s", refOrderFormat.format(today));

            Thirdpartie queryBody = new Thirdpartie();
            queryBody.setAddress(adresse);
            queryBody.setTown(ville);
            queryBody.setRegion(region);
            queryBody.setDepartement(departement);
            queryBody.setPays(pays);
            queryBody.setPhone(telephone);
            queryBody.setNote(note);
            queryBody.setLogo("");
            queryBody.setNote_private(note);
            queryBody.setEmail(email);
            queryBody.setFirstname(nom);
            queryBody.setName(String.format("%s", nom));
            queryBody.setCode_client(codeCLient);
            queryBody.setClient("1");
//                    queryBody.setName_alias(responseLogoClientBody);659331009
            queryBody.setName_alias("");

            Call<Long> callSaveClient = ApiUtils.getISalesService(com.iSales.pages.addcustomer.AddCustomerActivity.this).saveThirdpartie(queryBody);
            callSaveClient.enqueue(new Callback<Long>() {
                @Override
                public void onResponse(Call<Long> call, Response<Long> response) {
                    if (response.isSuccessful()) {
                        progressDialog.dismiss();

                        Long responseBody = response.body();
                        Log.e(TAG, "onResponse:callSaveClient responseBody="+responseBody);
                        mDb.clientDao().updateSynchroClient(1, responseBody);
                        Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.client_creee_succes), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        progressDialog.dismiss();

                        try {
                            Log.e(TAG, "doEvaluationTransfert onResponse err: message=" + response.message() + " | code=" + response.code() + " | errorString=" + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "onResponse: message=" + e.getMessage());
                        }
                        if (response.code() == 404) {
                            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (response.code() == 401) {
                            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                            return;
                        } else {
                            Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }

                @Override
                public void onFailure(Call<Long> call, Throwable t) {
                    progressDialog.dismiss();

                    Toast.makeText(com.iSales.pages.addcustomer.AddCustomerActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                    return;
                }
            });
        }

    }

    private void beginCrop(Uri source) {
//        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
//        Crop.of(source, destination).withMaxSize(600, 800).withAspect(1, 1).start(this);
        try {
            mLogoBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), source);
            mLogoBitmapPath = source.getPath();

//            Fait roter le bitmap de -90 deg
//            bitmap = SprintPayFunctionsUtils.rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_90);
            /* Log.e(TAG, "beginCrop: logo size=" + ISalesUtility.bitmapByteSizeOf(mLogoBitmap) +
                    " getName=" + ISalesUtility.getFilename(AddCustomerActivity.this, source) +
                    " getPath=" + mLogoBitmapPath); */

            mLogoNameTV.setText(ISalesUtility.getFilename(com.iSales.pages.addcustomer.AddCustomerActivity.this, source));

        } catch (IOException e) {
            Log.e(TAG, "beginCrop: logo err message=" + e.getMessage());
            mLogoBitmap = null;
            mLogoNameTV.setText(getString(R.string.aucune_photo_selectionnee));
            return;
        }
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
//            inflateDialogUserAvatar(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        mDb = AppDatabase.getInstance(getApplicationContext());

//        reference des vues
        mNomEntrepriseET = (EditText) findViewById(R.id.et_client_nom_entreprise);
        mAdresseET = (EditText) findViewById(R.id.et_client_adresse);
        mEmailET = (EditText) findViewById(R.id.et_client_email);
        mTelephoneET = (EditText) findViewById(R.id.et_client_telephone);
        mNoteET = (EditText) findViewById(R.id.et_client_note);
        mVilleET = (EditText) findViewById(R.id.et_client_ville);
        mDepartementET = (EditText) findViewById(R.id.et_client_departement);
        mRegionET = (EditText) findViewById(R.id.et_client_region);
        mPaysET = (EditText) findViewById(R.id.et_client_pays);
        mSelectLogoIV = (ImageView) findViewById(R.id.iv_client_select_logo);
        mLogoNameTV = (TextView) findViewById(R.id.tv_client_logo_name);
        mSwitchSynchro = (Switch) findViewById(R.id.switch_client);
        mEnregistrerView = (LinearLayout) findViewById(R.id.view_enregistrer_client);

//        ecoute du click de l'enregistrement du client
        mEnregistrerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
            }
        });

//        ecoute du click pour upload du logo du client
        mSelectLogoIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crop.pickImage(com.iSales.pages.addcustomer.AddCustomerActivity.this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Crop.REQUEST_PICK:
                if (resultCode == RESULT_OK) {
                    beginCrop(data.getData());
                }
                break;
            case Crop.REQUEST_CROP:
                handleCrop(resultCode, data);
                break;
        }
    }
}
