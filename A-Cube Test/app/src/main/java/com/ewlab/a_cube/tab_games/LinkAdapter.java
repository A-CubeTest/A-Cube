package com.ewlab.a_cube.tab_games;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.util.ArrayList;

public class LinkAdapter extends RecyclerView.Adapter<LinkViewHolder> {

    private final ScreenActivity screenActivity;
    private LayoutInflater linkInflater;

    public LinkAdapter(ScreenActivity screenActivity) {

        this.linkInflater = LayoutInflater.from(screenActivity);
        this.screenActivity = screenActivity;

    }

    @NonNull
    @Override
    public LinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = linkInflater.inflate(R.layout.single_link_view, parent, false);
        return new LinkViewHolder(view, screenActivity);

    }

    @Override
    public void onBindViewHolder(@NonNull LinkViewHolder linkViewHolder, int i) {

        String game_title = ConfigurationDetail.gameName;
        String conf_name = ConfigurationDetail.confName;
        String screenName = ScreenActivity.screenName;

        Configuration configuration = MainModel.getInstance().getConfiguration(game_title,conf_name);
        Screen screen = MainModel.getInstance().getScreenFromConf(configuration, screenName);

        Link link = screen.getLinks().get(i);
        String eventName = link.getEvent().getName();
        String actionName = link.getAction().getName();

        linkViewHolder.setCellLink(eventName, actionName);
        linkViewHolder.setCellColorLink(link.getMarkerColor());

    }

    @Override
    public int getItemCount() {

        String screenName = ScreenActivity.screenName;
        String confName = ScreenActivity.conf_name;
        String gameName = ScreenActivity.game_name;

        Configuration configuration = MainModel.getInstance().getConfiguration(gameName,confName);
        Screen screen = MainModel.getInstance().getScreenFromConf(configuration, screenName);

        return screen.getLinks().size();

    }
}

