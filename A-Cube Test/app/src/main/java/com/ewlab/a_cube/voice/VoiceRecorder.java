/*
VoiceRecorder: this activity allows to record the audio file
 */
package com.ewlab.a_cube.voice;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.DialogTutorial;

public class VoiceRecorder extends AppCompatActivity {

    // this is a variable for the asynctask used when user click the rec button
    private AudioRecorder ra;
    String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_recorder);
        //setContentView(R.layout.activity_voice_recorder_2);

        //noise != null if we arrive at this activity from noiseButton in tab_action
        final String noise = getIntent().getStringExtra("noiseRecording");

        final Context context = getApplicationContext();
        //to show a visual feedback of the recording
        final ImageView recordingImage = findViewById(R.id.recordingImage);
        //it gets the name of the sound
        action = getIntent().getStringExtra("action");

        final Button rec = findViewById(R.id.regInizio);

        final Button stopRec = findViewById(R.id.stop);

        //stopRec will be set at true only when the user starts the recording
        stopRec.setEnabled(false);

        rec.setOnClickListener(new View.OnClickListener() {
            @TargetApi(26)
            @Override
            public void onClick(View view) {

                Toast.makeText(context, "Rec...", Toast.LENGTH_SHORT).show();

                if(action != null) {
                    MainModel.getInstance().removeModelWithThisSound(action);
                }
                // it starts the asynctask
                if(noise != null) {
                    ra = new AudioRecorder(context, "Noise");
                }
                else {
                    ra = new AudioRecorder(context, action);
                }

                ra.execute();

                //it changes the imageview for the visual feedback. Green means the asynctask is recording
                recordingImage.setImageResource(R.drawable.recording);

                //it alternates the available button
                stopRec.setEnabled(true);

                rec.setEnabled(false);

            }
        });


        stopRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // it stops the asynctask
                ra.cancel(true);

                stopRec.setEnabled(false);

                recordingImage.setImageResource(R.drawable.no_recording);

                Toast.makeText(context, R.string.rec_end, Toast.LENGTH_SHORT).show();

                Toast.makeText(context, R.string.file_saving, Toast.LENGTH_SHORT).show();

                //it waits for 3 seconds before going to the MainActivity
                CountDownTimer timer = new CountDownTimer(3000,3000) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        Toast.makeText(context, R.string.file_saved, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(context, AudioCommandsList.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("action_name", action);
                        startActivity(intent);
                    }
                }.start();


            }
        });

    }
    /*
    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {

                case R.id.info_btn:

                    break;

                case R.id.back_btn:

                    break;

                case R.id.rec_btn:

                    break;
            }
        }
    }

     */

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
                alertDialog.getData(getString(R.string.new_record));
        }

        return super.onOptionsItemSelected(item);
    }
}
