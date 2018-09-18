package com.rainbow_cl.i_sales.pages.addcustomer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.rainbow_cl.i_sales.R;

public class AddCustomerActivity extends AppCompatActivity {
    private View mEnregistrerView;
    private EditText mNom, mPrenom, mAdresse, mVille, mPays;

    private void validateForm() {

        // Reset errors.
        mNom.setError(null);
        mPrenom.setError(null);
        mAdresse.setError(null);
        mVille.setError(null);
        mPays.setError(null);

        // Store values at the time of the login attempt.
        String nom = mNom.getText().toString();
        String prenom = mPrenom.getText().toString();
        String adresse = mAdresse.getText().toString();
        String ville = mVille.getText().toString();
        String pays = mPays.getText().toString();

        boolean cancel = false;
        View focusView = null;

        mNom.setError("Champs invalide");
        focusView = mNom;
        focusView.requestFocus();
        // Teste de validité du login
        /*if (!isUsernameValid(username)) {
            mUsernameView.setError(getString(R.string.veuillez_remplir_username));
            focusView = mUsernameView;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(password) && !cancel) {
            mPasswordView.setError(getString(R.string.veuillez_remplir_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            executeLogin(username, password);
        } */
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

//        reference des vues
        mNom = (EditText) findViewById(R.id.et_client_nom);
        mPrenom = (EditText) findViewById(R.id.et_client_prenom);
        mAdresse = (EditText) findViewById(R.id.et_client_adresse);
        mVille = (EditText) findViewById(R.id.et_client_ville);
        mPays = (EditText) findViewById(R.id.et_client_pays);
        mEnregistrerView = (LinearLayout) findViewById(R.id.view_enregistrer_client);

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
