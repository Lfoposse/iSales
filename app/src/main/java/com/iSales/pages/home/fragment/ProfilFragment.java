package com.iSales.pages.home.fragment;


import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iSales.R;
import com.iSales.database.AppDatabase;
import com.iSales.database.entry.UserEntry;
import com.iSales.pages.home.viewmodel.UserViewModel;
import com.iSales.pages.parametres.ParametresActivity;
import com.iSales.pages.synchronisation.SynchronisationActivity;
import com.iSales.utility.ISalesUtility;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ProfilFragment extends Fragment {
    private TextView mNameTV, mAdressTV;

    private LinearLayout mDeconnexionView, mSynchroView, mParametresView;

    private AppDatabase mDb;

    private UserEntry mUserEntry;

    //    recuperation du user connecté dans la BD
    private void loadUser() {
        final UserViewModel viewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        viewModel.getUserEntry().observe(this, new Observer<List<UserEntry>>() {
            @Override
            public void onChanged(@Nullable List<UserEntry> userEntries) {

        //        S'il n'y a aucun user alors on supprime la BD
                if (userEntries == null || userEntries.size() <= 0) {
                    getActivity().finish();
                    return;
                }

                mUserEntry = userEntries.get(0);

                initUserValues();
            }
        });
    }

    public void initUserValues() {
        String firstName = !mUserEntry.getFirstname().equals("") ? mUserEntry.getFirstname() : "";
        mNameTV.setText(String.format("%s %s", ISalesUtility.strCapitalize(firstName), mUserEntry.getLastname().toUpperCase()));
//        mAdressTV.setText(String.format("%s, %s", mUserEntry.getTown(), mUserEntry.getCountry()));
        mAdressTV.setText(String.format("%s", mUserEntry.getAddress()));
    }

    public ProfilFragment() {
        // Required empty public constructor
    }

    public static com.iSales.pages.home.fragment.ProfilFragment newInstance() {

        Bundle args = new Bundle();
        com.iSales.pages.home.fragment.ProfilFragment fragment = new com.iSales.pages.home.fragment.ProfilFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profil, container, false);

        mDb = AppDatabase.getInstance(getActivity().getApplicationContext());

//        Referencement des vues
        mNameTV = rootView.findViewById(R.id.tv_user_profile_name);
        mAdressTV = rootView.findViewById(R.id.tv_user_profile_address);
        mParametresView = rootView.findViewById(R.id.view_user_profile_parametre);
        mSynchroView = rootView.findViewById(R.id.view_user_profile_synchronisation);
        mDeconnexionView = rootView.findViewById(R.id.view_user_profile_deconnecter);

        mSynchroView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SynchronisationActivity.class);
                startActivity(intent);
            }
        });

        mParametresView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ParametresActivity.class);
                startActivity(intent);
            }
        });

        mDeconnexionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog alertDialog = new  AlertDialog.Builder(getContext()).create();
                alertDialog.setTitle("Déconnexion");
                alertDialog.setMessage("Voulez-vous vraiment vous déconnecter ?");
                alertDialog.setCancelable(false);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OUI", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                Suppression des données dans la BD
                        mDb.tokenDao().deleteAllToken();
                        mDb.userDao().deleteAllUser();
                        mDb.panierDao().deleteAllPanier();
                        mDb.serverDao().updateActiveAllserver(false);
                        mDb.clientDao().deleteAllClient();
                        mDb.commandeDao().deleteAllCmde();
                        mDb.produitDao().deleteAllProduit();
                        mDb.commandeLineDao().deleteAllCmdeLine();
                        mDb.signatureDao().deleteAllSignature();
                        mDb.categorieDao().deleteAllCategorie();

//                Retour a la page de Login
                        getActivity().finish();
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "NON", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        alertDialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

//        recuperation du user et chargement de ses infos
        loadUser();

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(0, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }
}
