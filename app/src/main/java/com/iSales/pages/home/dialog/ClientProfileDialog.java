package com.iSales.pages.home.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.iSales.interfaces.ClientsAdapterListener;
import com.iSales.interfaces.MyCropImageListener;
import com.iSales.model.ClientParcelable;
import com.iSales.pages.home.fragment.ClientProfileFragment;
import com.iSales.R;

/**
 * Created by netserve on 20/09/2018.
 */

public class ClientProfileDialog extends DialogFragment implements com.iSales.interfaces.ClientsAdapterListener {
    public static String TAG = ClientProfileDialog.class.getSimpleName();

    //    views
    private ImageButton ibClose;

    //    Parametre de recuperation de la liste des categories
    private static com.iSales.model.ClientParcelable mClientParcelable = new com.iSales.model.ClientParcelable();
    private static int mPosition = -1;
    private static com.iSales.interfaces.MyCropImageListener myCropImageListener;
    private static com.iSales.interfaces.ClientsAdapterListener mClientsAdapterListener;

    @Override
    public void onClientsSelected(com.iSales.model.ClientParcelable clientParcelable, int position) {

        mClientParcelable = clientParcelable;
        mPosition = position;
    }

    @Override
    public void onClientsUpdated(com.iSales.model.ClientParcelable clientParcelable, int position) {

    }

    public ClientProfileDialog() {
    }

    public static ClientProfileDialog newInstance(ClientParcelable clientParcelable, int position, MyCropImageListener cropImageListener, ClientsAdapterListener clientsAdapterListener) {
//        passage des parametres de la requete au fragment
        mClientParcelable = clientParcelable;
        mPosition = position;
        myCropImageListener = cropImageListener;
        mClientsAdapterListener = clientsAdapterListener;
        Bundle args = new Bundle();

        ClientProfileDialog fragment = new ClientProfileDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.ClientProfileDialogStyle);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_client_profile, container, false);


        ibClose = (ImageButton) view.findViewById(R.id.ib_dialog_clientprofile_close);

//        inflate fragment profile client on view
        Fragment fragment = ClientProfileFragment.newInstance(mClientParcelable, mPosition, myCropImageListener, mClientsAdapterListener);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_dialog_clientprofile, fragment).commit();

//        Close the modal
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                dialogCategorieListener.onCategorieAdapterSelected(null);
                dismiss();
            }
        });

        return view;

    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
