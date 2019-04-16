package com.iSales.pages.home.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.iSales.database.AppDatabase;
import com.iSales.database.entry.ClientEntry;
import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.interfaces.DialogClientListener;
import com.iSales.interfaces.MyCropImageListener;
import com.iSales.model.ClientParcelable;
import com.iSales.remote.ApiUtils;
import com.iSales.remote.ConnectionManager;
import com.iSales.remote.model.Document;
import com.iSales.remote.model.Thirdpartie;
import com.iSales.task.FindProductCustomerPriceTask;
import com.iSales.utility.BlurBuilder;
import com.iSales.utility.ISalesUtility;
import com.iSales.R;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class ClientProfileFragment extends Fragment implements DialogClientListener {
    public static String TAG = ClientProfileFragment.class.getSimpleName();

    //    Parametre de recuperation de la liste des categories
    private static com.iSales.model.ClientParcelable mClientParcelable;
    private static int mPosition;

    //    view elements
    private EditText mNomEntreprise, mAdresse, mEmail, mPhone, mPays, mRegion, mDepartement, mVille, mNote;
    private TextView mCodeClient, mDatecreation, mDatemodification;
    private ImageView mPoster, mPosterBlurry, mCallIV, mMapIV, mMailIV;
    private RadioButton mRadioBtnCurrent;
    private View mModifierView, mAnnulerView;
    private FloatingActionButton mLogoFloatingBtn;

    private static com.iSales.interfaces.MyCropImageListener myCropImageListener;
    private static com.iSales.interfaces.ClientsAdapterListener mClientsAdapterListener;

    //    database instance
    private com.iSales.database.AppDatabase mDb;

    public ClientProfileFragment() {
        // Required empty public constructor
    }

    public static ClientProfileFragment newInstance(com.iSales.model.ClientParcelable clientParcelable, int position, MyCropImageListener cropImageListener, ClientsAdapterListener clientsAdapterListener) {
//        passage des parametres de la requete au fragment
        mClientParcelable = clientParcelable;
        mPosition = position;
        myCropImageListener = cropImageListener;
        mClientsAdapterListener = clientsAdapterListener;
        Bundle args = new Bundle();

        ClientProfileFragment fragment = new ClientProfileFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_client_profile, container, false);

        mDb = AppDatabase.getInstance(getContext().getApplicationContext());

        mPoster = (ImageView) rootView.findViewById(R.id.user_profile_avatar);
        mCallIV = (ImageView) rootView.findViewById(R.id.iv_clientprofile_call);
        mMapIV = (ImageView) rootView.findViewById(R.id.iv_clientprofile_map);
        mMailIV = (ImageView) rootView.findViewById(R.id.iv_clientprofile_mail);
        mPosterBlurry = (ImageView) rootView.findViewById(R.id.user_profile_avatar_blurry);
        mNomEntreprise = (EditText) rootView.findViewById(R.id.et_clientprofile_nom_entreprise);
        mAdresse = (EditText) rootView.findViewById(R.id.et_clientprofile_adresse);
        mEmail = (EditText) rootView.findViewById(R.id.et_clientprofile_email);
        mPhone = (EditText) rootView.findViewById(R.id.et_clientprofile_telephone);
        mNote = (EditText) rootView.findViewById(R.id.et_clientprofile_note);
        mPays = (EditText) rootView.findViewById(R.id.et_clientprofile_pays);
        mRegion = (EditText) rootView.findViewById(R.id.et_clientprofile_region);
        mDepartement = (EditText) rootView.findViewById(R.id.et_clientprofile_departement);
        mVille = (EditText) rootView.findViewById(R.id.et_clientprofile_ville);
        mRadioBtnCurrent = (RadioButton) rootView.findViewById(R.id.rb_clientprofile_current);
        mCodeClient = (TextView) rootView.findViewById(R.id.tv_clientprofile_code);
        mDatecreation = (TextView) rootView.findViewById(R.id.tv_clientprofile_datecreation);
        mDatemodification = (TextView) rootView.findViewById(R.id.tv_clientprofile_datemodification);
        mLogoFloatingBtn = (FloatingActionButton) rootView.findViewById(R.id.floatingbtn_clientprofile_logo);
        mModifierView = (View) rootView.findViewById(R.id.view_enregistrer_client);
        mAnnulerView = (View) rootView.findViewById(R.id.view_annuler_client);

        if (savedInstanceState != null) {
            mClientParcelable = getActivity().getIntent().getParcelableExtra("client");

        }
        if (mClientParcelable != null) initViewContent();

        mCallIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientParcelable.getPhone() == null || mClientParcelable.getPhone().equals("")) {
                    Toast.makeText(getContext(), "Numéro de téléphone invalide.", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialPhoneNumber(mClientParcelable.getPhone());
            }
        });

        mMapIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientParcelable.getAddress() == null) {
                    Toast.makeText(getContext(), "Adresse invalide.", Toast.LENGTH_SHORT).show();
                    return;
                }

                mapAddress(mClientParcelable.getAddress());
            }
        });

        mMailIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientParcelable.getEmail() == null) {
                    Toast.makeText(getContext(), "Adresse mail invalide.", Toast.LENGTH_SHORT).show();
                    return;
                }

                sendMail(mClientParcelable.getEmail());
            }
        });
//        ecoute du click pour upload du logo du client
        mLogoFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crop.pickImage(getContext(), ClientProfileFragment.this);
            }
        });
//        ecoute du click pour la ,odification des informations du user
        mModifierView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateForm();
            }
        });
        mAnnulerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initViewContent();
            }
        });

        mRadioBtnCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientParcelable != null) {
//                    Log.e(TAG, "onCheckedChanged: BEFORE checked=" + mRadioBtnCurrent.isChecked() +
//                            " is_current=" + mClientParcelable.getIs_current() +
//                            " idclient=" + mClientParcelable.getId());

                    if (mClientParcelable.getIs_current() == 0) {
                        mRadioBtnCurrent.setChecked(true);
                        mClientParcelable.setIs_current(1);

                        mDb.clientDao().updateAllCurrentClient();
                        mDb.clientDao().updateCurrentClient(1, mClientParcelable.getId());

//        Si le téléphone est connecté et le client synchronise avec le serveur
                        if (com.iSales.remote.ConnectionManager.isPhoneConnected(getContext()) && mClientParcelable.getIs_synchro() == 1) {
                            com.iSales.task.FindProductCustomerPriceTask task = new FindProductCustomerPriceTask(getContext(), mClientParcelable.getId(), null);
                            task.execute();
                        }
                    } else {
                        mRadioBtnCurrent.setChecked(false);
                        mClientParcelable.setIs_current(0);

                        mDb.clientDao().updateAllCurrentClient();
                        mDb.productCustPriceDao().deleteAllProductCustPrice();
                    }
//                    Log.e(TAG, "onCheckedChanged: AFTER checked=" + mRadioBtnCurrent.isChecked()  + " is_current=" + mClientParcelable.getIs_current());
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
//        Log.e(TAG, "onSaveInstanceState: ");

        outState.putParcelable("client", mClientParcelable);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(TAG, "onActivityResult: ");
        switch (requestCode) {
            case Crop.REQUEST_PICK:
                Log.e(TAG, "onActivityResult: REQUEST_PICK resultCode=" + resultCode + " resultCodeAc=" + getActivity().RESULT_OK);
                if (resultCode == getActivity().RESULT_OK) {
                    Log.e(TAG, "onActivityResult: RESULT_OK");
                    beginCrop(data.getData());
                }
                break;
            case Crop.REQUEST_CROP:
//                handleCrop(resultCode, data);
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {
//        passage des parametres de la requete au fragment
        mClientParcelable = clientParcelable;
        mPosition = position;
        if (mClientParcelable == null) {
            Log.e(TAG, "onClientDialogSelected: mClientParcelable nulll");
            initViewContent_null();

        } else {
            Log.e(TAG, "onClientDialogSelected: name=" + mClientParcelable.getName() + " mPosition=" + mPosition);

            initViewContent();
        }
    }

    public void dialPhoneNumber(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Impossible de passer un appel. Veuillez installer une application d'appel.", Toast.LENGTH_LONG).show();
        }
    }

    public void mapAddress(String address) {
        String map = "http://maps.google.co.in/maps?q=" + address;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "Impossible de naviguer a cette adresse. Veuillez installer l'application Google Map.", Toast.LENGTH_LONG).show();
        }
    }

    public void sendMail(String email) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
//emailIntent.putExtra(Intent.EXTRA_HTML_TEXT, body); //If you are using HTML in your body text

        if (emailIntent.resolveActivity(getContext().getPackageManager()) != null) {

            startActivity(Intent.createChooser(emailIntent, ""));
        } else {
            Toast.makeText(getContext(), "Impossible d'envoyer un mail. Veuillez installer lune application de messagerie.", Toast.LENGTH_LONG).show();
        }
    }

    private void initViewContent() {
//        Log.e(TAG, "initViewContent: poster="+mClientParcelable.getPoster().getContent());


        if (mClientParcelable.getPoster().getContent() != null) {
//            si le fichier existe dans la memoire locale
            File imgFile = new File(mClientParcelable.getPoster().getContent());
            if (imgFile.exists()) {

                /* Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

//                    chargement de la photo dans la vue
                mPoster.setBackground(new BitmapDrawable(myBitmap));

                Bitmap blurredBitmap = BlurBuilder.blur(getContext(), myBitmap);
                mPosterBlurry.setBackground(new BitmapDrawable(getResources(), blurredBitmap)); */

                Picasso.with(getContext())
                        .load(imgFile)
                        .into(mPoster, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                                if (getContext() != null) {
                                    Bitmap imageBitmap = ((BitmapDrawable) mPoster.getDrawable()).getBitmap();
                                    Bitmap blurredBitmap = com.iSales.utility.BlurBuilder.blur(getContext(), imageBitmap);
                                    mPosterBlurry.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });

            } else {

                Picasso.with(getContext())
                        .load(R.drawable.isales_user_profile)
                        .into(mPoster, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                                if (getContext() != null) {
                                    Bitmap imageBitmap = ((BitmapDrawable) mPoster.getDrawable()).getBitmap();
                                    Bitmap blurredBitmap = com.iSales.utility.BlurBuilder.blur(getContext(), imageBitmap);
                                    mPosterBlurry.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                }
                            }

                            @Override
                            public void onError() {

                            }
                        });
            }
        } else {
            String original_file = mClientParcelable.getLogo();
            String module_part = "societe";
            Picasso.with(getContext())
                    .load(com.iSales.remote.ApiUtils.getDownloadImg(getContext(), module_part, original_file))
                    .placeholder(R.drawable.isales_user_profile)
                    .error(R.drawable.isales_user_profile)
                    .into(mPoster, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                            if (getContext() != null) {
                                Bitmap imageBitmap = ((BitmapDrawable) mPoster.getDrawable()).getBitmap();
                                Bitmap blurredBitmap = BlurBuilder.blur(getContext(), imageBitmap);
                                mPosterBlurry.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }


        mNomEntreprise.setText(mClientParcelable.getName());
        mAdresse.setText(mClientParcelable.getAddress());
        mEmail.setText(mClientParcelable.getEmail());
        mPhone.setText(mClientParcelable.getPhone());
        mNote.setText(mClientParcelable.getNote());
        mPays.setText(mClientParcelable.getPays());
        mRegion.setText(mClientParcelable.getRegion());
        mDepartement.setText(mClientParcelable.getDepartement());
        mVille.setText(mClientParcelable.getTown());
        mCodeClient.setText(mClientParcelable.getCode_client());

        Log.e(TAG, "initViewContent: clientId="+mClientParcelable.getId()+" test is_synchro="+mClientParcelable.getIs_synchro() );


//        masque le radio de sélection du client s'il n'est pas synchronisé avec le serveur
        if (mClientParcelable.getIs_synchro() == 0) {
            mRadioBtnCurrent.setEnabled(false);
        } else {
            mRadioBtnCurrent.setEnabled(true);
        }

        mRadioBtnCurrent.setChecked(false);
        ClientEntry clientEntry = mDb.clientDao().getCurrentClient(1);
        if (clientEntry != null) {
            if (mClientParcelable.getId() == clientEntry.getId()) {
                mRadioBtnCurrent.setChecked(true);
            }
        }

//        date de creation et de modification du client
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss");
        if (mClientParcelable.getDate_creation() != null) {
            Date dateCreation = new Date(mClientParcelable.getDate_creation());

            mDatecreation.setText(dateFormat.format(dateCreation));
        } else {
            mDatecreation.setText("");
        }

        if (mClientParcelable.getDate_modification() != null) {
            Date dateModif = new Date(mClientParcelable.getDate_modification());

            mDatemodification.setText(dateFormat.format(dateModif));
        } else {
            mDatemodification.setText("");
        }
    }

    private void initViewContent_null() {
//        Log.e(TAG, "initViewContent: poster="+mClientParcelable.getPoster().getContent());

        Picasso.with(getContext())
                .load(R.drawable.isales_user_profile)
                .error(R.drawable.isales_user_profile)
                .into(mPoster, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
//                        Log.e(TAG, "onSuccess: Picasso loadin img");
                        if (getContext() != null) {
                            Bitmap imageBitmap = ((BitmapDrawable) mPoster.getDrawable()).getBitmap();
                            Bitmap blurredBitmap = BlurBuilder.blur(getContext(), imageBitmap);
                            mPosterBlurry.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                        }
                    }

                    @Override
                    public void onError() {

                    }
                });


        mNomEntreprise.setText("");
        mAdresse.setText("");
        mEmail.setText("");
        mPhone.setText("");
        mNote.setText("");
        mPays.setText("");
        mRegion.setText("");
        mDepartement.setText("");
        mVille.setText("");
        mCodeClient.setText("");

        mRadioBtnCurrent.setChecked(false);

//        masque le radio de sélection du client s'il n'est pas synchronisé avec le serveur
        mRadioBtnCurrent.setEnabled(false);

//        date de creation et de modification du client
        mDatecreation.setText("");

        mDatemodification.setText("");
    }

    private void beginCrop(Uri source) {
        try {
            Bitmap logoBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), source);
//            File file = new File(source.getPath());

//            Fait roter le bitmap de -90 deg
//            bitmap = SprintPayFunctionsUtils.rotateBitmap(bitmap, ExifInterface.ORIENTATION_ROTATE_90);
            Log.e(TAG, "beginCrop: logo size=" + com.iSales.utility.ISalesUtility.bitmapByteSizeOf(logoBitmap) +
                    " getName=" + com.iSales.utility.ISalesUtility.getFilename(getContext(), source));
            updateLogoClient(logoBitmap);

        } catch (IOException e) {
            Log.e(TAG, "beginCrop: logo err message=" + e.getMessage());
            return;
        }
    }

    //    modifi le logo du client sur le serveur
    private void updateLogoClient(Bitmap logoBitmap) {
        //        Si le téléphone n'est pas connecté
        if (!com.iSales.remote.ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setTitle("Transfert d'Argent");
        progressDialog.setMessage(com.iSales.utility.ISalesUtility.strCapitalize(getString(R.string.enregistrement_encours)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
        progressDialog.show();

//        conversion du logo en base64
        ByteArrayOutputStream baosLogo = new ByteArrayOutputStream();
        logoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baosLogo);
        byte[] bytesSignComm = baosLogo.toByteArray();

        Date today = new Date();
        final SimpleDateFormat logoFormat = new SimpleDateFormat("yyMMdd-HHmmss");
        final String logoName = String.format("client_logo_%s", logoFormat.format(today));
        final String encodeLogoClient = Base64.encodeToString(bytesSignComm, Base64.NO_WRAP);
        String filenameComm = String.format("%s.jpeg", logoName);
//        creation du document signature client
        com.iSales.remote.model.Document logoClient = new Document();
        logoClient.setFilecontent(encodeLogoClient);
        logoClient.setFilename(filenameComm);
        logoClient.setFileencoding("base64");
        logoClient.setModulepart("societe");

        Call<String> callUploadLogoClient = com.iSales.remote.ApiUtils.getISalesService(getContext()).uploadDocument(logoClient);
        callUploadLogoClient.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> responseSignComm) {
                if (responseSignComm.isSuccessful()) {
                    String responseLogoClientBody = responseSignComm.body();
                    Log.e(TAG, "onResponse: responseSignCommBody=" + responseLogoClientBody);

                    com.iSales.remote.model.Thirdpartie queryBody = new com.iSales.remote.model.Thirdpartie();
                    queryBody.setName_alias(responseLogoClientBody);

                    Call<com.iSales.remote.model.Thirdpartie> callSaveClient = com.iSales.remote.ApiUtils.getISalesService(getContext()).updateThirdpartie(mClientParcelable.getId(), queryBody);
                    callSaveClient.enqueue(new Callback<com.iSales.remote.model.Thirdpartie>() {
                        @Override
                        public void onResponse(Call<com.iSales.remote.model.Thirdpartie> call, Response<com.iSales.remote.model.Thirdpartie> response) {
                            if (response.isSuccessful()) {
                                progressDialog.dismiss();
                                mClientParcelable.getPoster().setContent(encodeLogoClient);

                                myCropImageListener.onClientLogoChange(mClientParcelable, mPosition);
//                                Actualisation des infos du client
                                initViewContent();
                            } else {
                                progressDialog.dismiss();

                                try {
                                    Log.e(TAG, "doEvaluationTransfert onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                                } catch (IOException e) {
                                    Log.e(TAG, "onResponse: message=" + e.getMessage());
                                }
                                if (response.code() == 404) {
                                    Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (response.code() == 401) {
                                    Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                                    return;
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                                    return;
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<com.iSales.remote.model.Thirdpartie> call, Throwable t) {
                            progressDialog.dismiss();

                            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                            return;
                        }
                    });

                } else {
                    progressDialog.dismiss();

                    try {
                        Log.e(TAG, "uploadDocument onResponse SignComm err: message=" + responseSignComm.message() +
                                " | code=" + responseSignComm.code() + " | code=" + responseSignComm.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                    }
                    if (responseSignComm.code() == 404) {
                        Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (responseSignComm.code() == 401) {
                        Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                }

            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                progressDialog.dismiss();

                Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                return;

            }
        });
    }


    private void validateForm() {

        // Reset errors.
        mNomEntreprise.setError(null);
        mAdresse.setError(null);
        mEmail.setError(null);
        mPhone.setError(null);
        mPays.setError(null);
        mRegion.setError(null);
        mDepartement.setError(null);
        mVille.setError(null);

        // Store values at the time of the login attempt.
        String nomEntreprise = mNomEntreprise.getText().toString();
        String adresse = mAdresse.getText().toString();
        String email = mEmail.getText().toString();
        String telephone = mPhone.getText().toString();
        String note = mNote.getText().toString();
        String ville = mVille.getText().toString();
        String departement = mDepartement.getText().toString();
        String region = mRegion.getText().toString();
        String pays = mPays.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Test de validité du nom
        if (TextUtils.isEmpty(nomEntreprise)) {
            mNomEntreprise.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mNomEntreprise;
            cancel = true;
        }
        // Test de validité de l'adresse
        /*if (TextUtils.isEmpty(adresse) && !cancel) {
            mAdresse.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mAdresse;
            cancel = true;
        }
        // Test de validité de l'email
        if (TextUtils.isEmpty(email) && !cancel) {
            mEmail.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mEmail;
            cancel = true;
        }
        if (!com.iSales.utility.ISalesUtility.isValidEmail(email) && !cancel) {
            mEmail.setError(getString(R.string.adresse_mail_invalide));
            focusView = mEmail;
            cancel = true;
        }
        // Test de validité du telephone
        if (TextUtils.isEmpty(telephone) && !cancel) {
            mPhone.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mPhone;
            cancel = true;
        }
        // Test de validité du pays
        if (TextUtils.isEmpty(pays) && !cancel) {
            mPays.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mPays;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(region) && !cancel) {
            mRegion.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mRegion;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(departement) && !cancel) {
            mDepartement.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mDepartement;
            cancel = true;
        }
        // Test de validité de la ville
        if (TextUtils.isEmpty(ville) && !cancel) {
            mVille.setError(getString(R.string.veuillez_remplir_ce_champs));
            focusView = mVille;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            updateClient(nomEntreprise, adresse, email, telephone, note, pays, region, departement, ville);
        }
    }

    //    enregistre un client dans le serveur
    private void updateClient(final String nomEntreprise, final String adresse, final String email, final String telephone, final String note, final String pays, final String region, final String departement, final String ville) {
//        Si le téléphone n'est pas connecté
        if (!ConnectionManager.isPhoneConnected(getContext())) {
            Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setTitle("Transfert d'Argent");
        progressDialog.setMessage(ISalesUtility.strCapitalize(getString(R.string.enregistrement_encours)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.circular_progress_view));
        progressDialog.show();

        com.iSales.remote.model.Thirdpartie queryBody = new com.iSales.remote.model.Thirdpartie();
        queryBody.setAddress(adresse);
        queryBody.setTown(ville);
        queryBody.setRegion(region);
        queryBody.setDepartement(departement);
        queryBody.setPays(pays);
        queryBody.setPhone(telephone);
        queryBody.setNote(note);
        queryBody.setEmail(email);
        queryBody.setName(String.format("%s", nomEntreprise));

        Call<com.iSales.remote.model.Thirdpartie> callUpdateClient = ApiUtils.getISalesService(getContext()).updateThirdpartie(mClientParcelable.getId(), queryBody);
        callUpdateClient.enqueue(new Callback<com.iSales.remote.model.Thirdpartie>() {
            @Override
            public void onResponse(Call<com.iSales.remote.model.Thirdpartie> call, Response<com.iSales.remote.model.Thirdpartie> response) {
                if (response.isSuccessful()) {
                    progressDialog.dismiss();

                    com.iSales.remote.model.Thirdpartie responseBody = response.body();

                    mClientParcelable.setAddress(responseBody.getAddress());
                    mClientParcelable.setTown(responseBody.getTown());
                    mClientParcelable.setRegion(responseBody.getRegion());
                    mClientParcelable.setDepartement(responseBody.getDepartement());
                    mClientParcelable.setPays(responseBody.getPays());
                    mClientParcelable.setPhone(responseBody.getPhone());
                    mClientParcelable.setNote(responseBody.getNote());
                    mClientParcelable.setEmail(responseBody.getEmail());
                    mClientParcelable.setName(responseBody.getName());

                    mClientsAdapterListener.onClientsUpdated(mClientParcelable, mPosition);
//                                Actualisation des infos du client
                    initViewContent();

                    Toast.makeText(getContext(), getString(R.string.informations_client_modifie), Toast.LENGTH_LONG).show();
                } else {
                    progressDialog.dismiss();

                    try {
                        Log.e(TAG, "doEvaluationTransfert onResponse err: message=" + response.message() + " | code=" + response.code() + " | code=" + response.errorBody().string());
                    } catch (IOException e) {
                        Log.e(TAG, "onResponse: message=" + e.getMessage());
                    }
                    if (response.code() == 404) {
                        Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (response.code() == 401) {
                        Toast.makeText(getContext(), getString(R.string.echec_authentification), Toast.LENGTH_LONG).show();
                        return;
                    } else {
                        Toast.makeText(getContext(), getString(R.string.service_indisponible), Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Call<Thirdpartie> call, Throwable t) {
                progressDialog.dismiss();

                Toast.makeText(getContext(), getString(R.string.erreur_connexion), Toast.LENGTH_LONG).show();
                return;
            }
        });

    }

}
