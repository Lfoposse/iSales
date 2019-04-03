package com.iSales.pages.addcategorie;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Categorie;
import com.iSales.utility.ISalesUtility;
import com.iSales.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddCategorieActivity extends AppCompatActivity {
    private static final String TAG = AddCategorieActivity.class.getSimpleName();
    private View mEnregistrerView;
    private EditText mLabelET, mDescriptionET;
    private Spinner mTypecategorieSpinner;

//    selected type categorie item
    private String mTypecategorieSelected;
    private int mTypecategorieSelectedPos;

    private void validateForm() {

        // Reset errors.
        mLabelET.setError(null);
        mDescriptionET.setError(null);

        // Store values at the time of the login attempt.
        String label = mLabelET.getText().toString();
        String description = mDescriptionET.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Teste de validité du type de categorie
        if (TextUtils.isEmpty(mTypecategorieSelected)) {
            Toast.makeText(AddCategorieActivity.this, "Veuillez choisir le type de categorie.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Teste de validité du label
        if (TextUtils.isEmpty(label)) {
            mLabelET.setError(getString(R.string.veuillez_remplir_libelle));
            focusView = mLabelET;
            cancel = true;
        }

        // Teste de validité de la description
        /*if (TextUtils.isEmpty(description) && !cancel) {
            mDescriptionET.setError(getString(R.string.veuillez_remplir_description));
            focusView = mDescriptionET;
            cancel = true;
        } */

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return;
        } else {
            saveCategorie(label, description, mTypecategorieSelected);
        }
    }

//    enregistrer la categorie sur le serveur
    private void saveCategorie(String label, String description, String type) {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(AddCategorieActivity.this)) {
            Toast.makeText(AddCategorieActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(AddCategorieActivity.this);
//        progressDialog.setTitle("Transfert d'Argent");
        progressDialog.setMessage(com.iSales.utility.ISalesUtility.strCapitalize(getString(R.string.enregistrement_encours)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
        progressDialog.show();

        String typeId;
        switch (type.toLowerCase()) {
            case "client":
                typeId = "2";
                break;
            default:
                typeId = "0";
                break;
        }
        com.iSales.remote.model.Categorie queryBody = new Categorie();
        queryBody.setLabel(label);
        queryBody.setDescription(com.iSales.utility.ISalesUtility.ENCODE_DESC+label+ ISalesUtility.ENCODE_DESC);
        queryBody.setType(typeId);

        Call<Long> call = ApiUtils.getISalesService(AddCategorieActivity.this).saveCategorie(queryBody);
        call.enqueue(new Callback<Long>() {
            @Override
            public void onResponse(Call<Long> call, Response<Long> response) {
                if (response.isSuccessful()) {
                    progressDialog.dismiss();

                    Long responseBody = response.body();
                    Toast.makeText(AddCategorieActivity.this, getString(R.string.categorie_creee_succes), Toast.LENGTH_LONG).show();
                    finish();
                }
                else {
                    progressDialog.dismiss();

                    try {
                        Log.e(TAG, "doEvaluationTransfert onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message="+e.getMessage());
                    }
                    if (response.code() == 404) {
                        Toast.makeText(AddCategorieActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.code() == 401) {
                        Toast.makeText(AddCategorieActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(AddCategorieActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<Long> call, Throwable t) {
                progressDialog.dismiss();

                Toast.makeText(AddCategorieActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                return;
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_categorie);

//        reference des vues
        mLabelET = (EditText) findViewById(R.id.et_categorie_label);
        mDescriptionET = (EditText) findViewById(R.id.et_categorie_description);
        mEnregistrerView = (LinearLayout) findViewById(R.id.view_enregistrer_categorie);

        mTypecategorieSpinner = (Spinner) findViewById(R.id.spinner_categorie_type);

//        init type categorie spinner values
        List<String> motifs = Arrays.asList(getResources().getStringArray(R.array.liste_types_categorie));
        ArrayAdapter<String> motifsAdapter = new ArrayAdapter<String>(AddCategorieActivity.this, android.R.layout.simple_spinner_item, motifs);
        motifsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mTypecategorieSpinner.setAdapter(motifsAdapter);
        mTypecategorieSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mTypecategorieSelected = (String) adapterView.getItemAtPosition(i);
                mTypecategorieSelectedPos = i;
//                Log.e("TransfertArgActiv", "onItemSelected: mTransactionMotifOperation="+mTransactionMotifOperation
//                        +" adapterPosi="+(String) adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


//        ecoute du click de l'enregistrement du client
        mEnregistrerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
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
