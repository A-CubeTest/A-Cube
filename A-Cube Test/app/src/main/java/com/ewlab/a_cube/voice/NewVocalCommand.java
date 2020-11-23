/*
NewVocalCommand: this activity allows to define the action name that it will be used as label of the class of audio files,
                  which they represent the action
 */
package com.ewlab.a_cube.voice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;

import java.util.List;

public class NewVocalCommand extends AppCompatActivity {

    Button newVocal;
    ImageButton back_btn, info_btn;
    EditText vocalName_et;
    Intent recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vocal_command);

        /* NEW WAY
        setContentView(R.layout.activity_new_vocal);

        //NASCONDO TITLE BAR
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();
        //CAMBIO COLORE STATUS BAR
        getWindow().setStatusBarColor(getResources().getColor(R.color.purple_semitransparent));

        recorder = new Intent(this, VoiceRecorder.class);
        //EDIT TEXT
        vocalName_et = findViewById(R.id.editActionName_et);
        //PULSANTI
        newVocal = findViewById(R.id.newVocal);
        back_btn = findViewById(R.id.back_btn);
        info_btn = findViewById(R.id.info_btn);

        newVocal.setOnClickListener(buttonListener);
        back_btn.setOnClickListener(buttonListener);
        info_btn.setOnClickListener(buttonListener);

         */

        //OLD WAY
        Button newCommand = findViewById(R.id.nuovoVocale);
        final EditText editT = findViewById(R.id.vocal_name);

        final Intent recorder = new Intent(this, VoiceRecorder.class);


        newCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean alreadyExist = false;

                if (TextUtils.isEmpty(editT.getText())) {
                    editT.setError(getString(R.string.insert_name));
                }
                else {

                    //memorizzo il nome dell'azione dell'editText nell'intent
                    String vocalName = editT.getText().toString();
                    List<Action> allActions = MainModel.getInstance().getActions();
                    for (Action a : allActions) {
                        if (a.getName().equals(vocalName)) {
                            editT.setError(getString(R.string.insert_action_error2));
                            alreadyExist = true;
                        }
                    }

                    if (!alreadyExist) {
                        recorder.putExtra("action", editT.getText().toString());
                        startActivity(recorder);
                    }
                }
            }
        });



    }

    /*
    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {

                case R.id.back_btn:
                    break;

                case R.id.info_btn:
                    break;

                case R.id.newVocal:

                    boolean alreadyExist = false;

                    if (vocalName_et.getText().toString().equals("")) {

                        vocalName_et.setError(getString(R.string.insert_name));
                    }
                    else {

                        //memorizzo il nome dell'azione dell'editText nell'intent
                        String vocalName = vocalName_et.getText().toString();
                        List<Action> allActions = MainModel.getInstance().getActions();

                        for (Action a : allActions) {

                            if (a.getName().equals(vocalName)) {

                                vocalName_et.setError(getString(R.string.insert_action_error2));
                                alreadyExist = true;
                                break;
                            }
                        }

                        if (!alreadyExist) {
                            recorder.putExtra("action", vocalName_et.getText().toString());
                            startActivity(recorder);
                        }
                    }

                    break;

            }

        }
    };

     */
}
