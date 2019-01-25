package com.rainbow_cl.i_sales.pages.login;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.database.AppDatabase;
import com.rainbow_cl.i_sales.database.entry.ServerEntry;
import com.rainbow_cl.i_sales.database.entry.TokenEntry;
import com.rainbow_cl.i_sales.database.entry.UserEntry;
import com.rainbow_cl.i_sales.interfaces.OnInternauteLoginComplete;
import com.rainbow_cl.i_sales.pages.home.HomeActivity;
import com.rainbow_cl.i_sales.remote.ApiUtils;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.Internaute;
import com.rainbow_cl.i_sales.remote.model.User;
import com.rainbow_cl.i_sales.remote.rest.LoginREST;
import com.rainbow_cl.i_sales.task.InternauteLoginTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity implements OnInternauteLoginComplete {
    private static final String TAG = LoginActivity.class.getSimpleName();

    private InternauteLoginTask mAuthTask = null;

    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 96;

    // UI references.
    private AutoCompleteTextView mUsernameET;
    private EditText mPasswordET, mServerET;
    private ImageView mServerIV;
    private View mProgressView;
    private View mLoginFormView;

    private String mServer;
    private String mUsername;
    private String mPassword;

    private List<ServerEntry> serverEntries;
    private ServerEntry mServerChoose;

    private AppDatabase mDb;


    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }
    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
    public void showLog() {

        if ( isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/iSalesLog" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "login_logcat" + System.currentTimeMillis() + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch ( IOException e ) {
                e.printStackTrace();
            }

        } else if ( isExternalStorageReadable() ) {
            // only readable
            Log.e(TAG, "onCreate: isExternalStorageReadable" );
        } else {
            // not accessible
            Log.e(TAG, "onCreate: non isExternalStorageReadable" );
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupActionBar();

        showLog();

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Log.e(TAG, "onCreate:PhoneMetrics density=" + metrics.density + " densityDpi=" + metrics.densityDpi);

        mDb = AppDatabase.getInstance(getApplicationContext());

//        Ajout les serveurs dans la BD
        initServerUrl();

// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(LoginActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(LoginActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(LoginActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_CONTACTS);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

//        si il y a deja un user alors on va directement a l'accueil
            if (mDb.userDao().getUser().size() > 0) {
//        aller a la page d'accueil
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        }

        mServerIV = (ImageView) findViewById(R.id.iv_login_server);
        mServerET = (EditText) findViewById(R.id.et_login_server);
        mServerET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mUsernameET.setSelection(0);
                    return true;
                }
                return false;
            }
        });
        mServerIV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showServersSelect();
            }
        });

        // Set up the login form.
        mUsernameET = (AutoCompleteTextView) findViewById(R.id.username);
        mUsernameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_NEXT) {
                    mPasswordET.setSelection(0);
                    return true;
                }
                return false;
            }
        });

        mPasswordET = (EditText) findViewById(R.id.password);
        mPasswordET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
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
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: grant");
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Log.e(TAG, "onRequestPermissionsResult: denied");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

//        si il y a deja un user alors on va directement a l'accueil
                if (mDb.userDao().getUser().size() > 0) {
//        aller a la page d'accueil
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                }

                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mServerET.setError(null);
        mUsernameET.setError(null);
        mPasswordET.setError(null);

        // Store values at the time of the login attempt.
//        mServer = mServerET.getText().toString();
        mUsername = mUsernameET.getText().toString();
        mPassword = mPasswordET.getText().toString();

        boolean cancel = false;
        View focusView = null;

        /*
        // Teste de validité de l'adresse du serveur
        if (!isFieldValid(mServer)) {
            mServerET.setError(getString(R.string.veuillez_remplir_url_serveur));
            focusView = mServerET;
            cancel = true;
        }
        if (!URLUtil.isValidUrl(mServer)) {
            mServerET.setError(getString(R.string.url_invalide));
            focusView = mServerET;
            cancel = true;
        } */

        // Teste de validité du login
        if (!isFieldValid(mUsername)) {
            mUsernameET.setError(getString(R.string.veuillez_remplir_username));
            focusView = mUsernameET;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (!isPasswordValid(mPassword) && !cancel) {
            mPasswordET.setError(getString(R.string.veuillez_remplir_password));
            focusView = mPasswordET;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            saveServerurl();
            executeLogin(mUsername, mPassword);
        }
    }

    private boolean isFieldValid(String username) {
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

    private CharSequence[] getServersSequence() {
        serverEntries = mDb.serverDao().getAllServers();
//        Log.e(TAG, "onCreate: getServersSequence() serverEntries="+serverEntries.size());
        CharSequence[] items = new String[serverEntries.size()];
        for (int i = 0; i < serverEntries.size(); i++) {
            items[i] = serverEntries.get(i).getTitle();
        }
//        Log.e(TAG, "onCreate: getServersSequence() after serverEntries="+items.length);

        return items;

    }

    private void showServersSelect() {
        final int[] exportChoice = {-1};
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Serveurs précédemment utilisés");
        builder.setSingleChoiceItems(getServersSequence(), -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                exportChoice[0] = item;
//                Toast.makeText(getApplicationContext(), FlashInventoryUtility.getExportFormat()[item], Toast.LENGTH_SHORT).show();
            }
        });

        builder.setPositiveButton("VALIDER",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (exportChoice[0] >= 0) {
                            mServerChoose = serverEntries.get(exportChoice[0]);

                            mServerET.setText(mServerChoose.getTitle());
//                            mServerET.setSelection(mServerET.getText().toString().length());
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

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void saveServerurl() {
//        Log.e(TAG, "saveServerurl: serverurl=" + mServerChoose.getHostname() + " title=" + mServerChoose.getTitle() + " id=" + mServerChoose.getId() + " is_active=" + mServerChoose.getIs_active());
//        desactivation de tous les serveurs en local
        mDb.serverDao().updateActiveAllserver(false);

        mDb.serverDao().updateActiveServer(mServerChoose.getId(), true);
    }

    private void initServerUrl() {
//        desactivation de tous les serveurs en local
        List<ServerEntry> serverEntries = new ArrayList<>();
//        serverEntries.add(new ServerEntry("Serveur de test Dolibarr Bananafw", "http://dolibarr.bananafw.com/api/index.php", false));
        serverEntries.add(new ServerEntry("France Food Compagny", "http://food.apps-dev.fr:80/api/index.php", false));
        serverEntries.add(new ServerEntry("Test France Food Compagny", "http://82.253.71.109/prod/test_francefood/api/index.php", false));
        serverEntries.add(new ServerEntry("Test Unknow Compagny", "http://82.253.71.109/prod/soif_express/api/index.php", false));

        for (ServerEntry serverItem : serverEntries ) {
            if (mDb.serverDao().getServerByHostname(serverItem.getHostname()) == null) {
                mDb.serverDao().insertServer(serverItem);
            }
        }
    }

    private void executeLogin(String username, String password) {
//        masquage du formulaire de connexion
        showProgress(true);

        if (!ConnectionManager.isPhoneConnected(LoginActivity.this)) {
            Toast.makeText(LoginActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();

//           masquage du formulaire de connexion
            showProgress(false);
        }
        if (mAuthTask == null) {
            Internaute internaute = new Internaute(username, password);

            mAuthTask = new InternauteLoginTask(LoginActivity.this, LoginActivity.this, internaute);
            mAuthTask.execute();
        }
    }

    @Override
    public void onInternauteLoginTaskComplete(LoginREST loginREST) {
        mAuthTask = null;

//        Si la connexion echoue, on renvoi un message d'authentification
        if (loginREST == null) {
            Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();

//        masquage du formulaire de connexion
            showProgress(false);
            return;
        }
        if (loginREST.getInternauteSuccess() == null) {
            if (loginREST.getErrorCode() == 404) {
                Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();

//        masquage du formulaire de connexion
                showProgress(false);
                return;
            } else {
                Toast.makeText(LoginActivity.this, getString(R.string.parametres_connexion_incorrect), Toast.LENGTH_LONG).show();

//        masquage du formulaire de connexion
                showProgress(false);
                return;
            }
        }

//      ======  Connexion reussie
//        Suppression du token
        mDb.tokenDao().deleteAllToken();
//        Enregistrement du token dans la BD local
        TokenEntry tokenEntry = new TokenEntry(
                loginREST.getInternauteSuccess().getSuccess().getToken(),
                loginREST.getInternauteSuccess().getSuccess().getMessage());
        mDb.tokenDao().insertToken(tokenEntry);

        String sqlfilter = "login=\"" + mUsername + "\"";
//        Log.e(TAG, "onInternauteLoginTaskComplete: sqlfilter=" + sqlfilter +
//                " token=" + tokenEntry.getToken() +
//                " message=" + tokenEntry.getMessage());
        Call<ArrayList<User>> callUser = ApiUtils.getISalesService(LoginActivity.this).findUserByLogin(sqlfilter);
        callUser.enqueue(new Callback<ArrayList<User>>() {
            @Override
            public void onResponse(Call<ArrayList<User>> call, Response<ArrayList<User>> response) {
                if (response.isSuccessful()) {
                    ArrayList<User> responseBody = response.body();
                    User user = responseBody.get(0);

//                    Enregistrement du user dans la BD
                    UserEntry userEntry = new UserEntry();
                    userEntry.setAddress(user.getAddress());
                    userEntry.setBirth(user.getBirth());
                    userEntry.setCountry(user.getCountry());
                    userEntry.setDatec(user.getDatec());
                    userEntry.setDateemployment(user.getDateemployment());
                    userEntry.setDatelastlogin(user.getDatelastlogin());
                    userEntry.setDatem(user.getDatem());
                    userEntry.setEmail(user.getEmail());
                    userEntry.setEmployee(user.getEmployee());
                    userEntry.setFirstname(user.getFirstname());
                    userEntry.setGender(user.getGender());
                    userEntry.setId(Long.parseLong(user.getId()));
                    userEntry.setLastname(user.getLastname());
                    userEntry.setLogin(user.getLogin());
                    userEntry.setName(user.getName());
                    userEntry.setPhoto(user.getPhoto());
                    userEntry.setStatut(user.getStatut());
                    userEntry.setTown(user.getTown());

//                    suppresion du user
                    mDb.userDao().deleteAllUser();
//                    insertion du user dans la BD
                    mDb.userDao().insertUser(userEntry);

//                affichage du formulaire de connexion
                    showProgress(false);

//        Log.e(TAG, "doInBackground: internauteSuccess="+loginREST.getInternauteSuccess().getSuccess().getToken());
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                } else {

//                affichage du formulaire de connexion
                    showProgress(false);
                    try {
                        Log.e(TAG, "uploadDocument onResponse SignComm err: message=" + response.message() +
                                " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                    }
                    if (response.code() == 404) {
                        Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this, getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<ArrayList<User>> call, Throwable t) {

//                affichage du formulaire de connexion
                showProgress(false);
                Toast.makeText(LoginActivity.this, getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                return;
            }
        });
    }

}

