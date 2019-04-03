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

import com.iSales.R;
import com.iSales.interfaces.DialogCategorieListener;
import com.iSales.model.CategorieParcelable;
import com.iSales.pages.home.fragment.CategorieProduitFragment;

/**
 * Created by netserve on 04/09/2018.
 */

public class FullScreenCatPdtDialog extends DialogFragment implements DialogCategorieListener{
    public static String TAG = com.iSales.pages.home.dialog.FullScreenCatPdtDialog.class.getSimpleName();

//    views
    private ImageButton ibClose;

    //    Listener de sortie apres selection d'une categorie
    private static DialogCategorieListener dialogCategorieListener;

//    Parametre de recuperation de la liste des categories
    private static String mType = "";

//    Recupération de la categorie sélectionnée
    @Override
    public void onCategorieDialogSelected(CategorieParcelable categorieParcelable) {
        dialogCategorieListener.onCategorieDialogSelected(categorieParcelable);

//        exit dialog
        dismiss();
    }

    public FullScreenCatPdtDialog() {
    }

    public static com.iSales.pages.home.dialog.FullScreenCatPdtDialog newInstance(DialogCategorieListener onDialogCategorieListener, String type) {
//        passage des parametres de la requete au fragment
        mType = type;
        Bundle args = new Bundle();

        com.iSales.pages.home.dialog.FullScreenCatPdtDialog fragment = new com.iSales.pages.home.dialog.FullScreenCatPdtDialog();
        dialogCategorieListener = onDialogCategorieListener;
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
        View view = inflater.inflate(R.layout.dialog_categories_produits, container, false);

        ibClose = (ImageButton) view.findViewById(R.id.ib_dialog_catpdt_close);

//        inflate fragment profile client on view
        Fragment fragment = CategorieProduitFragment.newInstance(com.iSales.pages.home.dialog.FullScreenCatPdtDialog.this, mType);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.content_dialog_catpdt, fragment).commit();

//        Close the modal
        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogCategorieListener.onCategorieDialogSelected(null);
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
