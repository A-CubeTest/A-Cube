package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;


public class DialogAddConfig extends DialogFragment  {

    public String nameConf;
    public String title;

    public DialogConfigListener listener;

    public DialogAddConfig() {
    }

    public static DialogAddConfig newInstance(String title) {
        DialogAddConfig frag = new DialogAddConfig();
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
        return inflater.inflate(R.layout.config_name_template, container);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.config_name_template, null));

        builder.setTitle(R.string.name_conf);
        builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Dialog dialogObj =Dialog.class.cast(dialog);
                        EditText etUsr=(EditText) dialogObj.findViewById(R.id.nameConfig);
                        String userStr = etUsr.getText().toString();

                        if(userStr.length()>0) {
                            Game game = MainModel.getInstance().getGame(title);
                            Log.d("dialogConfig", game.getTitle());

                            Configuration newConf = new Configuration(userStr, game);
                            boolean response = MainModel.getInstance().addNewConfiguration(newConf);

                            if (response) {
                                listener.applyTest(userStr, response);
                                MainModel.getInstance().writeConfigurationsJson();

                            } else {
                                listener.applyTest(userStr, response);
                            }
                            dialog.dismiss();
                        }else{
                            etUsr.setError("");
                        }
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

        listener = (DialogConfigListener) context;
    }

    public interface DialogConfigListener{
        void applyTest(String userStr, boolean response);
    }
}