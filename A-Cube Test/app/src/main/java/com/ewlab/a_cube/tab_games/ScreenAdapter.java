package com.ewlab.a_cube.tab_games;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.util.ArrayList;

public class ScreenAdapter extends RecyclerView.Adapter<ScreenViewHolder> {

    private final ConfigurationDetail configurationDetail;
    private LayoutInflater screenInflater;

    String name_game = ConfigurationDetail.gameName;
    String name_conf = ConfigurationDetail.confName;
    ArrayList<Screen> screens = new ArrayList<>();

    public ScreenAdapter(ConfigurationDetail configurationDetail) {

        this.screenInflater = LayoutInflater.from(configurationDetail);
        this.configurationDetail = configurationDetail;

    }


    @NonNull
    @Override
    public ScreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = screenInflater.inflate(R.layout.single_screen_view, parent, false);

        if(! screens.get(0).isPortrait())
            view = screenInflater.inflate(R.layout.single_screen_landscape_view, parent, false);

        return new ScreenViewHolder(view, configurationDetail);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenViewHolder screenViewHolder, int i) {

        Configuration configuration = MainModel.getInstance().getConfiguration(name_game,name_conf);
        screens = MainModel.getInstance().getScreensFromConf(configuration);

        Screen screen = screens.get(i);

        screenViewHolder.setCellScreenshot(screen.getName(), screen.getImage());

        //PRENDO L'IMMAGINE DELLO SCREEN E LA RENDO MUTABLE PER DISEGNARCI SOPRA
        Bitmap bitmap = MainModel.getInstance().stringToBitMap(screen.getImage());
        bitmap = bitmap.copy(bitmap.getConfig(), true);     //lets bmp to be mutable
        //PRENDO I RIFERIMENTI A LINK E EVENTI DI QUESTA SCHERMATA
        Game game = configuration.getGame();
        ArrayList<Event> events = game.getEvents();
        ArrayList<Link> links = screen.getLinks();
        //DISEGNO I MARKER DEI LINK PRESENTI SU QUESTA SCHERMATA
        screenViewHolder.linksToDraw(links,events,bitmap);
        

    }



    @Override
    public int getItemCount() {

        Configuration configuration = MainModel.getInstance().getConfiguration(name_game,name_conf);
        screens = MainModel.getInstance().getScreensFromConf(configuration);

        return screens.size();
    }
}
