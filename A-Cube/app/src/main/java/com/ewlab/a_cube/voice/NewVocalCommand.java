/*
NewVocalCommand: this activity allows to define the action name that it will be used as label of the class of audio files,
                  which they represent the action
 */
package com.ewlab.a_cube.voice;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.DialogTutorial;

import java.util.List;

public class NewVocalCommand extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_vocal_command);

        Button newCommand = findViewById(R.id.nuovoVocale);
        final EditText editT = findViewById(R.id.vocal_name);

        final Intent recorder = new Intent(this, VoiceRecorder.class);

        newCommand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean alreadyExist = false;

                if (TextUtils.isEmpty(editT.getText())) {
                    editT.setError(getString(R.string.insert_name));
                } else {
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
}
