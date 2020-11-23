package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.SVMmodel;
import com.ewlab.a_cube.model.Screen;

import java.util.ArrayList;


public class LinkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ScreenActivity screenActivity;
    private TextView name_event, name_action_tv,name_action;
    private ImageView action_icon;
    private ImageButton delete_link;
    private ConstraintLayout link_view;

    public LinkViewHolder(@NonNull View itemView, ScreenActivity screenActivity) {

        super(itemView);

        link_view = itemView.findViewById(R.id.link_view);
        name_event = itemView.findViewById(R.id.name_event);
        name_action = itemView.findViewById(R.id.name_action);
        name_action_tv = itemView.findViewById(R.id.action_name_tv);
        action_icon = itemView.findViewById(R.id.action_icon_iv);
        delete_link = itemView.findViewById(R.id.delete_link);

        itemView.setOnClickListener(this);
        this.screenActivity = screenActivity;
    }

    @Override
    public void onClick(View v) {

        String event_name = name_event.getText().toString();
        String gameName = ScreenActivity.game_name;
        String confName = ScreenActivity.conf_name;
        String screenImage = ScreenActivity.img;
        String screenName = ScreenActivity.screenName;

        Toast.makeText(screenActivity.getApplicationContext(),"Tap on event: " + event_name,Toast.LENGTH_LONG).show();

        Intent intent_newLink = new Intent(screenActivity.getApplicationContext(), NewLink.class);

        intent_newLink.putExtra("gameName", gameName);
        intent_newLink.putExtra("confName", confName);
        intent_newLink.putExtra("screenImage", screenImage);
        intent_newLink.putExtra("screenName", screenName);
        intent_newLink.putExtra("event", event_name);
        intent_newLink.putExtra("existingLink",true);

        screenActivity.startActivity(intent_newLink);

        Log.d("NewLink", "Link for game: "+gameName + " of conf: " + confName);

    }

    public void setCellLink(String nE, String nA) {

        name_event.setText(nE);
        name_action_tv.setText(nA);
        //CONTROLLO IL TIPO DI AZIONE PER MOSTRARE L'ICON CORRETTA
        Action action = MainModel.getInstance().getAction(nA);
        if(action instanceof  ActionVocal)
            action_icon.setImageDrawable(screenActivity.getResources().getDrawable(R.drawable.ic_mic_white_24dp));
        else if(action instanceof ActionButton)
            action_icon.setImageDrawable(screenActivity.getResources().getDrawable(R.drawable.ic_buttons_white_24dp));

        delete_link.setOnClickListener(delete_linkListener);

    }

    public void setCellColorLink(int color) {

        GradientDrawable bgShape = (GradientDrawable)link_view.getBackground();
        bgShape.setColor(color);

    }

    public View.OnClickListener delete_linkListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Toast.makeText(screenActivity.getApplicationContext(), "Delete link: " + getAdapterPosition(),
                    Toast.LENGTH_LONG).show();

            final String gameName = ScreenActivity.game_name;
            final String confName = ScreenActivity.conf_name;
            final String screenName = ScreenActivity.screenName;

            String event_name = name_event.getText().toString();
            String action_name = name_action_tv.getText().toString();
            //action_name = action_name.substring(action_name.indexOf(' ')).trim();
            //Log.d("LINK","Nome action: "+action_name.substring(action_name.indexOf(' ')).trim());

            final Configuration configuration = MainModel.getInstance().getConfiguration(gameName, confName);
            final Screen screen = MainModel.getInstance().getScreenFromConf(configuration,screenName);
            ArrayList<Link> links = screen.getLinks();
            //ARRAY DI LINK DI GRANDEZZA 1 IN CUI INSERIRE IL LINK DA ELIMINARE
            final Link[] link_toRemove = new Link[1];
            final Action action;

            Log.d("LINK","NUMERO LINK PER QUESTO SCREEN: "+screen.getLinks().size());

            for (final Link l : links) {

                if (l.getEvent().getName().equals(event_name) && l.getAction().getName().equals(action_name)) {

                    final boolean[] removed = {false};
                    link_toRemove[0] = l;
                    action = l.getAction();

                    //ALERT DIALOG
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(screenActivity);
                    LayoutInflater inflater = screenActivity.getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.alert_dialog_remove_link, null);

                    Button delete = dialogView.findViewById(R.id.deleteLink_btn);
                    Button back = dialogView.findViewById(R.id.backLink_btn);

                    dialogBuilder.setView(dialogView);
                    final AlertDialog alertDialog = dialogBuilder.create();

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

                            removed[0] = screen.removeLink(link_toRemove[0]);

                            MainModel.getInstance().writeConfigurationsJson();
                            MainModel.getInstance().writeGamesJson();

                            Toast.makeText(screenActivity.getApplicationContext(), R.string.link_deleted, Toast.LENGTH_LONG).show();
                            /*
                            Intent intent = new Intent(screenActivity.getApplicationContext(), ScreenActivity.class);
                            intent.putExtra("gameName", gameName);
                            intent.putExtra("name", confName);
                            screenActivity.startActivity(intent);
                             */
                            ScreenActivity.linkAdapter.notifyDataSetChanged();

                            if (removed[0]) {
                                if (action instanceof ActionVocal) {

                                    SVMmodel newModel = MainModel.getInstance().getSVMmodel(screen.getVocalActions());
                                    //TODO MI SA CHE DA ORA IL MODEL LO DEVO SETTARE SULLO SCREEN E NON SULLA CONFIGURAZIONE
                                    configuration.setModel(newModel);
                                }
                            }

                            alertDialog.dismiss();
                        }
                    });
                    break;

                }

            }

        }
    };
}
