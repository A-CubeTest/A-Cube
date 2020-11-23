package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;

public class DialogRemoveConfig extends DialogFragment {

    public Configuration confToRemove;

    public DialogRemoveConfig() {
    }

    public static DialogRemoveConfig newInstance(Configuration confToRemove) {
        DialogRemoveConfig frag = new DialogRemoveConfig();
        Bundle args = new Bundle();
        args.putString("title", confToRemove.getConfName());
        frag.setArguments(args);
        return frag;
    }

    public void getData(Configuration confToRemove){
        this.confToRemove = confToRemove;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.configuration_delete_template, container);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.configuration_delete_template, null));

        builder.setTitle(getString(R.string.configuration)+": "+confToRemove.getConfName());
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Dialog dialogObj = Dialog.class.cast(dialog);

                String text = getString(R.string.configuration_deleted);
                MainModel.getInstance().removeConfiguration(confToRemove);

                MainModel.getInstance().writeConfigurationsJson();
                MainModel.getInstance().writeGamesJson();
                Intent intent = new Intent(getContext(), ConfigurationList.class);
                intent.putExtra("title", confToRemove.getGame().getTitle());
                dialog.dismiss();

                startActivity(intent);
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

    public interface DialogConfigListener{
        void applyTest(Configuration confToRemove, boolean response);
    }
}
