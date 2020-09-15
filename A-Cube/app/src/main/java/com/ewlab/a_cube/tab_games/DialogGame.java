package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ewlab.a_cube.R;

public class DialogGame extends DialogFragment {

    public String title;
    public DialogGameListener listener;


    public DialogGame() {
    }

    public static DialogGame newInstance(String title) {
        DialogGame frag = new DialogGame();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void getData(String title){
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_title_template, container);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.game_title_template, null));

        builder.setTitle(getString(R.string.game)+": "+title);
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Dialog dialogObj = Dialog.class.cast(dialog);
                listener.applyTest(title);

                dialog.dismiss();
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dialog.dismiss();

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        listener = (DialogGameListener) context;
    }

    public interface DialogGameListener{
        void applyTest(String userStr);
    }
}
