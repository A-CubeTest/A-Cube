package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.tab_action.ActionViewHolder;

import java.util.List;

public class GamesAdapter extends RecyclerView.Adapter<GameViewHolder> {

    private static final String TAG = GamesAdapter.class.getName();
    private final MainActivity mainActivity;
    private LayoutInflater gameInflater;
    private PackageManager packageManager;


    public GamesAdapter(MainActivity mainActivity) {

        this.gameInflater = LayoutInflater.from(mainActivity);
        this.mainActivity = mainActivity;

    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = gameInflater.inflate(R.layout.single_game_view, parent, false);
        return new GameViewHolder(view, mainActivity);

    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder gameViewHolder, int i) {

        Game game = MainModel.getInstance().getGames().get(i);
        String nome = game.getTitle();

        //PRENDO L'ICONA DEL GIOCO DA ApplicationInfo
        ApplicationInfo ai = new ApplicationInfo();
        packageManager = mainActivity.getApplicationContext().getPackageManager();

        try {
            ai = packageManager.getApplicationInfo(game.getBundleId(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //double m = MainModel.getInstance().matchingUpdate(game.getTitle());
        double m = MainModel.getInstance().matchingUpdate2(game.getTitle());
        //Controllo per sapere quale cerchio mostrare attorno all'icona del gioco
        if(m > 0 && m < 1)
            gameViewHolder.setCellGameOrange(nome, ai, packageManager);
        else if(m == 1)
            gameViewHolder.setCellGameGreen(nome, ai, packageManager);
        else
            gameViewHolder.setCellGameRed(nome, ai, packageManager);

    }

    @Override
    public int getItemCount() {
        return MainModel.getInstance().getGames().size();
    }
}
