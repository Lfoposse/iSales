package com.rainbow_cl.i_sales.pages.login;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.interfaces.OnInternauteLoginComplete;
import com.rainbow_cl.i_sales.pages.home.HomeActivity;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.Internaute;
import com.rainbow_cl.i_sales.remote.rest.LoginREST;
import com.rainbow_cl.i_sales.task.InternauteLoginTask;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements OnInternauteLoginComplete {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private InternauteLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.e(TAG, "onCreate:PhoneMetrics density="+metrics.density+" densityDpi="+metrics.densityDpi);

//        aller a la page d'accueil
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);

        // Set up the login form.
        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);;
        mUsernameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordView.setSelection(0);
                    return true;
                }
                return false;
            }
        });

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mLoginFormView = findViewById(R.id.login_form);
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Show the Up button in the action bar.
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Teste de validité du login
        if (!isUsernameValid(username)) {
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
        }
    }

    private boolean isUsernameValid(String username) {
        if (TextUtils.isEmpty(username)) {
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (TextUtils.isEmpty(password)) {
            return false;
        }
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void executeLogin(String username, String password) {
//        masquage du formulaire de connexion
        showProgress(true);

        if (!ConnectionManager.isPhoneConnected(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
        }
        if (mAuthTask == null) {
            Internaute internaute = new Internaute(username, password);

            mAuthTask = new InternauteLoginTask(LoginActivity.this, internaute);
            mAuthTask.execute();
        }
    }

    @Override
    public void onInternauteLoginTaskComplete(LoginREST loginREST) {
        mAuthTask = null;
//        affichage du formulaire de connexion
        showProgress(false);

//        Si la connexion echoue, on renvoi un message d'authentification
        if (loginREST == null) {
            Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
            return;
        }
        if (loginREST.getInternauteSuccess() == null) {
            if (loginREST.getErrorCode() == 404) {
                Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();

                return;
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.parametres_connexion_incorrect), Toast.LENGTH_LONG).show();

                return;
            }
        }

//        Connexion reussie
//        Log.e(TAG, "doInBackground: internauteSuccess="+loginREST.getInternauteSuccess().getSuccess().getToken());
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }

}

