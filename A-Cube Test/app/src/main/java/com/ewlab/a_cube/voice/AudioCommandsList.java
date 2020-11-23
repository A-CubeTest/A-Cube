/*
AudioCommandsList: this activity shows, using a listview, all the file linked to the action
                    (where action is the label that represents the class of the audio)
 */
package com.ewlab.a_cube.voice;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.DialogTutorial;

public class AudioCommandsList extends AppCompatActivity {

    private ListView listview;

    private AudiosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        final String actionName = getIntent().getStringExtra("action_name");
        this.setTitle(actionName+" - "+getString(R.string.recordings));
        final Intent addAudio = new Intent(this, VoiceRecorder.class);
        final Intent back = new Intent(this, MainActivity.class);


        listview = findViewById(R.id.lv_audio_commands);

        Button deleteAction = findViewById(R.id.deleteVocalAction);

        deleteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Action remove = MainModel.getInstance().removeAction(actionName);

                if(remove != null) {

                    MainModel.getInstance().writeActionsJson();
                    MainModel.getInstance().writeModelJson();
                    MainModel.getInstance().writeConfigurationsJson();
                    Toast.makeText(AudioCommandsList.this, R.string.action_deleted, Toast.LENGTH_SHORT).show();
                    startActivity(back);

                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.newAudio);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAudio.putExtra("action",actionName);
                startActivity(addAudio);
            }
        });

        setListView(actionName);
    }

    public void setListView(String title) {

        AudioCommands audios = new AudioCommands(title);
        adapter = new AudiosAdapter(this,android.R.layout.list_content, audios);
        listview.setAdapter(adapter);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_info:

                FragmentManager fm = getSupportFragmentManager();
                DialogTutorial alertDialog = new DialogTutorial();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(getString(R.string.recordings));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        final Intent back = new Intent(this, MainActivity.class);
        startActivity(back);
    }
}
