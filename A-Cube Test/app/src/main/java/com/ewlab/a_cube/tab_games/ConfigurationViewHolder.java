package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.util.ArrayList;

public class ConfigurationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ConfigurationList configurationList;
    private TextView name_conf, text_conf, definedEvents_tv, definedScreens_tv;
    private ImageButton delete_conf, edit_conf;
    private RadioButton selected_conf;
    ArrayList<Configuration> configurations;

    public ConfigurationViewHolder(@NonNull View itemView, ConfigurationList configurationList) {

        super(itemView);
        name_conf = itemView.findViewById(R.id.name_conf);
        text_conf = itemView.findViewById(R.id.text_conf);
        definedEvents_tv = itemView.findViewById(R.id.definedEvents_tv);
        definedScreens_tv = itemView.findViewById(R.id.definedScreens_tv);
        edit_conf = itemView.findViewById(R.id.edit_name_conf);
        delete_conf = itemView.findViewById(R.id.delete_conf);
        selected_conf = itemView.findViewById(R.id.selected_conf);
        itemView.setOnClickListener(this);
        this.configurationList = configurationList;
    }

    @Override
    public void onClick(View v) {

        String confName = name_conf.getText().toString();
        String game_title = ConfigurationList.game_name;

        Intent intent_confDetail = new Intent(v.getContext(),ConfigurationDetail.class);
        intent_confDetail.putExtra("title", game_title);
        intent_confDetail.putExtra("name", confName);
        configurationList.startActivity(intent_confDetail);

    }

    public void setCellConfiguration(String n) {

        name_conf.setText(n);
        configurations = MainModel.getInstance().getConfigurationsFromGame(ConfigurationList.game_name);
        int events_number = 0;
        int screens_number = 0;
        for(Configuration c : configurations) {

            if(c.getConfName().equals(n)) {

                ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(c);

                for(Screen s : screens) {
                    events_number = events_number + s.getLinks().size();
                    screens_number++;
                }
                //events_number = c.getScreens().getLinks().size();
                if(c.getSelected())
                    selected_conf.setChecked(true);
                else
                    selected_conf.setChecked(false);
            }

        }

        definedEvents_tv.setText(configurationList.getString(R.string.singleConfigurationView_tv_events,events_number));
        definedScreens_tv.setText(configurationList.getString(R.string.singleConfigurationView_tv_screens,screens_number));
        /*
        //Controllo per dire quanti eventi (links) e screens sono definiti per una certa configurazione
        switch(events_number) {

            case 0:
                text_conf.setText(R.string.config_list_cell_no_defined_events);
                break;

            case 1:
                text_conf.setText(R.string.config_list_cell_one_defined_event);
                break;

            default:
                text_conf.setText(configurationList.getApplicationContext().getString(
                        R.string.config_list_cell_more_defined_events,events_number));
                break;

        }

         */

        delete_conf.setOnClickListener(buttonListener);
        edit_conf.setOnClickListener(buttonListener);
        selected_conf.setOnClickListener(buttonListener);

    }

    private View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            String app_name = ConfigurationList.game_name;
            String confName = name_conf.getText().toString();
            Configuration configuration = MainModel.getInstance().getConfiguration(app_name,confName);

            switch(view.getId()) {

                case R.id.delete_conf:

                    //MOSTRO ALERT DIALOG PRIMA DI ELIMINARE LA CONFIGURAZIONE
                    alertDialogDeleteConf();

                    break;

                case R.id.edit_name_conf:

                    ConfigurationList.operation = "editConf";
                    ConfigurationList.cName = confName;
                    configurationList.toFragmentAddConf();
                    break;

                case R.id.selected_conf:

                    MainModel.getInstance().setSelectedConfiguration(configuration);
                    ConfigurationList.confAdapter.notifyDataSetChanged();
                    MainModel.getInstance().writeConfigurationsJson();

                    break;
            }

        }
    };

    public void alertDialogDeleteConf() {

        String app_name = ConfigurationList.game_name;
        String confName = name_conf.getText().toString();

        Configuration configuration = MainModel.getInstance().getConfiguration(app_name,confName);
        final String nameGame = configuration.getGame().getTitle();
        final String nameConf = configuration.getConfName();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(configurationList);
        LayoutInflater inflater = configurationList.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_remove_conf, null);

        Button delete = dialogView.findViewById(R.id.deleteConf_btn);
        Button back = dialogView.findViewById(R.id.backConf_btn);

        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        if(alertDialog.getWindow() != null)
            Log.d("ALERT_DIALOG","no null, conf to remove: " + nameConf);
        else
            Log.d("ALERT_DIALOG","null");

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean confRemoved = MainModel.getInstance().removeConfiguration(nameGame,nameConf);

                if(confRemoved) {

                    //RISCRIVO IL FILE DELLE CONFIGURAZIONI COSì DA TOGLIERE LA CONF APPENA ELIMINATA
                    MainModel.getInstance().writeConfigurationsJson();
                    //RISCRIVO IL FILE DEI GIOCHI COSì DA TOGLIERE GLI EVENTI ASSOCIATI AI LINK CHE FACEVANO PARTE DI QUELLA CONF
                    MainModel.getInstance().writeGamesJson();
                    //RICHIAMO L'ADAPTER PER AGGIORNARE LA RECYCLERVIEW
                    ConfigurationList.confAdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(configurationList, "Error occurred deleting configuration", Toast.LENGTH_SHORT).show();
                }

                //CHIUDO L'AlertDialog QUANDO HO FINITO
                alertDialog.dismiss();

            }
        });



    }
}
