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

import com.iSales.interfaces.DialogClientListener;
import com.iSales.model.ClientParcelable;
import com.iSales.pages.home.fragment.ClientsRadioFragment;
import com.iSales.R;

/**
 * Created by netserve on 26/09/2018.
 */

public class ClientDialog extends DialogFragment implements com.iSales.interfaces.DialogClientListener {
    public static String TAG = ClientDialog.class.getSimpleName();

    //    views
    private ImageButton ibClose;

    //    Listener de sortie apres selection d'une categorie
    private static com.iSales.interfaces.DialogClientListener dialogClientListener;

    //    Recupération du client sélectionnée
    @Override
    public void onClientDialogSelected(ClientParcelable clientParcelable, int position) {
        dialogClientListener.onClientDialogSelected(clientParcelable, position);

//        exit dialog
        dismiss();
    }

    public ClientDialog() {
    }

    public static ClientDialog newInstance(DialogClientListener onDialogClientListener) {
        Bundle args = new Bundle();

        ClientDialog fragment = new ClientDialog();
        dialogClientListener = onDialogClientListener;
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullScreenDialogStyle);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_clientradio, container, false);

        ibClose = (ImageButton) view.findViewById(R.id.ib_dialog_clientradio_close);

//        inflate fragment profile client on view
        Fragment fragment = ClientsRadioFragment.newInstance(ClientDialog.this);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_dialog_clientradio, fragment).commit();

//        Close the modal
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogClientListener.onClientDialogSelected(null, -1);
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
