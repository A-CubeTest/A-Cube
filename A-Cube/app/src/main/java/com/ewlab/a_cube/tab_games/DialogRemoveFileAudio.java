package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.voice.AudioCommandsList;

public class DialogRemoveFileAudio extends DialogFragment {

    String fileName;

    public DialogRemoveFileAudio() {
    }

    public static DialogRemoveFileAudio newInstance(String title) {
        DialogRemoveFileAudio frag = new DialogRemoveFileAudio();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void getData(String fileName){
        this.fileName = fileName;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.file_audio_delete_template, container);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.file_audio_delete_template, null));

        builder.setTitle(getString(R.string.file)+": "+fileName);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Dialog dialogObj = Dialog.class.cast(dialog);

                String text = getString(R.string.file_deleted);

                final String label = MainModel.getInstance().findVocalActionFromFile(fileName);

                boolean deleted = MainModel.getInstance().removeFileVocalAction(label, fileName);
                MainModel.getInstance().removeModelWithThisSound(label);


                if(deleted) {
                    MainModel.getInstance().writeActionsJson();
                    MainModel.getInstance().writeConfigurationsJson();
                    MainModel.getInstance().writeModelJson();
                    Toast.makeText(getContext(), R.string.rec_deleted, Toast.LENGTH_SHORT).show();


                    Action action = MainModel.getInstance().getAction(label);
                    Intent intent;
                    if(action == null) {
                        intent = new Intent(getContext(), MainActivity.class);
                    }else{
                        intent = new Intent(getContext(), AudioCommandsList.class);
                        intent.putExtra("action_name", label);
                    }
                    getContext().startActivity(intent);
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
}
