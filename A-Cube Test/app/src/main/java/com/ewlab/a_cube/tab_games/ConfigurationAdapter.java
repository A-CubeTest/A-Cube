package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationAdapter extends RecyclerView.Adapter<ConfigurationViewHolder> {

    private final ConfigurationList configurationList;
    private LayoutInflater confInflater;

    String name_game = ConfigurationList.game_name;
    ArrayList<Configuration> configurations;

    public ConfigurationAdapter(ConfigurationList configurationList) {

        this.confInflater = LayoutInflater.from(configurationList);
        this.configurationList = configurationList;

    }

    @NonNull
    @Override
    public ConfigurationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = confInflater.inflate(R.layout.single_configuration_view, parent, false);
        return new ConfigurationViewHolder(view, configurationList);
    }

    @Override
    public void onBindViewHolder(@NonNull ConfigurationViewHolder configurationViewHolder, int i) {

        name_game = ConfigurationList.game_name;
        configurations = MainModel.getInstance().getConfigurationsFromGame(name_game);
        Configuration configuration = configurations.get(i);

        configurationViewHolder.setCellConfiguration(configuration.getConfName());

    }

    @Override
    public int getItemCount() {

        return MainModel.getInstance().getConfigurationsFromGame(name_game).size();

    }
}