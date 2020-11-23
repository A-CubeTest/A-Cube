package com.ewlab.a_cube.voice;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.DialogGame;
import com.ewlab.a_cube.tab_games.DialogRemoveConfig;
import com.ewlab.a_cube.tab_games.DialogRemoveFileAudio;

import java.io.IOException;
import java.util.List;

public class AudiosAdapter extends ArrayAdapter<String>{

    private MediaPlayer mediaP;

    private Context context;

    public AudiosAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public AudiosAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if(v == null){
            v = LayoutInflater.from(getContext()).inflate(R.layout.audio_item,null);
        }

        final String file = getItem(position);

        final String label = MainModel.getInstance().findVocalActionFromFile(file);


        final ImageButton play = v.findViewById(R.id.playButton);
        final ImageButton pause = v.findViewById(R.id.pauseButton);
        pause.setEnabled(false);

        final ImageButton delete = v.findViewById(R.id.deleteButton);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setEnabled(false);
                pause.setEnabled(true);
                delete.setEnabled(false);
                playAudio(file);
            }
        });

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play.setEnabled(true);
                pause.setEnabled(false);
                delete.setEnabled(true);
                stopAudio();
            }
        });

        final View finalV = v;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentActivity activity = (FragmentActivity)(context);
                FragmentManager fm = activity.getSupportFragmentManager();
                DialogRemoveFileAudio alertDialog = new DialogRemoveFileAudio();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(file);
            }
        });

        mediaP = new MediaPlayer();
        mediaP.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                play.setEnabled(true);
                pause.setEnabled(false);
                delete.setEnabled(true);
            }
        });


        if(file != null){
            final TextView fileName = (TextView) v.findViewById(R.id.audio_item);

            fileName.setText(file);

        }

        return v;
    }

    private void playAudio(String fileN){
        Uri path_audio = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube/Sounds/" + fileN );

        mediaP.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaP.reset();
            mediaP.setDataSource(String.valueOf(path_audio));
            mediaP.prepare();
            mediaP.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopAudio(){
        mediaP.stop();
        mediaP.reset();
    }
}
