package com.rainbow_cl.i_sales.pages.home.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rainbow_cl.i_sales.R;
import com.rainbow_cl.i_sales.adapter.CategorieProduitAdapter;
import com.rainbow_cl.i_sales.decoration.MyDividerItemDecoration;
import com.rainbow_cl.i_sales.interfaces.CategorieProduitAdapterListener;
import com.rainbow_cl.i_sales.interfaces.ClientsAdapterListener;
import com.rainbow_cl.i_sales.interfaces.DialogCategorieListener;
import com.rainbow_cl.i_sales.interfaces.FindCategorieListener;
import com.rainbow_cl.i_sales.interfaces.MyCropImageListener;
import com.rainbow_cl.i_sales.model.CategorieParcelable;
import com.rainbow_cl.i_sales.model.ClientParcelable;
import com.rainbow_cl.i_sales.pages.home.fragment.ClientProfileFragment;
import com.rainbow_cl.i_sales.remote.ConnectionManager;
import com.rainbow_cl.i_sales.remote.model.Categorie;
import com.rainbow_cl.i_sales.remote.model.DolPhoto;
import com.rainbow_cl.i_sales.remote.rest.FindCategoriesREST;
import com.rainbow_cl.i_sales.task.FindCategorieTask;
import com.rainbow_cl.i_sales.utility.ISalesUtility;

import java.util.ArrayList;

/**
 * Created by netserve on 20/09/2018.
 */

public class ClientProfileDialog extends DialogFragment implements ClientsAdapterListener {
    public static String TAG = ClientProfileDialog.class.getSimpleName();

    //    views
    private ImageButton ibClose;

    //    Parametre de recuperation de la liste des categories
    private static ClientParcelable mClientParcelable = new ClientParcelable();
    private static int mPosition = -1;
    private static MyCropImageListener myCropImageListener;
    private static ClientsAdapterListener mClientsAdapterListener;

    @Override
    public void onClientsSelected(ClientParcelable clientParcelable, int position) {

        mClientParcelable = clientParcelable;
        mPosition = position;
    }

    @Override
    public void onClientsUpdated(ClientParcelable clientParcelable, int position) {

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
