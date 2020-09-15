package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ewlab.a_cube.R;

public class DialogTutorial extends DialogFragment {

    String title;

    public DialogTutorial (){

    }

    public static DialogTutorial newInstance(String title) {
        DialogTutorial frag = new DialogTutorial();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    public void getData(String title){
        this.title = title;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tutorial_template, container);
    }

    @Override
    public AlertDialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.tutorial_template, null);
        builder.setView(v);

        builder.setTitle(getString(R.string.tutorial)+": "+title);

        TextView tutorialTextBox1 = v.findViewById(R.id.tutorialTextBox1);
        TextView tutorialTextBox2 = v.findViewById(R.id.tutorialTextBox2);
        TextView tutorialTextBox3 = v.findViewById(R.id.tutorialTextBox3);

        if(title.equals(getString(R.string.actions))){
            tutorialTextBox1.setText(R.string.tutorial_tab_actions1);
            tutorialTextBox2.setText(R.string.tutorial_tab_actions2);

        }else if(title.equals(getString(R.string.games))){
            tutorialTextBox1.setText(R.string.tutorial_tab_games1);
            tutorialTextBox2.setText(R.string.tutorial_tab_games2);
            tutorialTextBox3.setText(R.string.tutorial_tab_games3);

        }else if(title.equals(getString(R.string.new_record))){
            tutorialTextBox1.setText(R.string.tutorial_audio);
            tutorialTextBox2.setText(R.string.audio_recording_tutorial);

        }else if(title.equals(getString(R.string.new_button_tutorial))){
            tutorialTextBox1.setText(R.string.tutorial_buttons);

        }else if(title.equals(getString(R.string.recordings))){
            tutorialTextBox1.setText(R.string.tutorial_recordings1);
            tutorialTextBox2.setText(R.string.tutorial_recordings2);
            tutorialTextBox3.setText(R.string.tutorial_recordings3);

        }else if(title.equals(getString(R.string.configuration))){
            tutorialTextBox1.setText(R.string.tutorial_configuration_list1);
            tutorialTextBox2.setText(R.string.tutorial_configuration_list2);
            tutorialTextBox3.setText(R.string.tutorial_configuration_list3);

        }else if(title.equals(getString(R.string.configuration)+"2")){
            tutorialTextBox1.setText(R.string.tutorial_configuration_detail1);
            tutorialTextBox2.setText(R.string.tutorial_configuration_detail2);
            tutorialTextBox3.setText(R.string.tutorial_configuration_detail3);
        }


        if(tutorialTextBox1.getText().length()>0){
            tutorialTextBox1.setVisibility(View.VISIBLE);
        }
        if(tutorialTextBox2.getText().length()>0){
            tutorialTextBox2.setVisibility(View.VISIBLE);
        }
        if(tutorialTextBox3.getText().length()>0){
            tutorialTextBox3.setVisibility(View.VISIBLE);
        }

        builder.setPositiveButton(R.string.continue_dialog, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int id) {
                Dialog dialogObj = Dialog.class.cast(dialog);
                dialog.dismiss();
            }
        }
        );
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
