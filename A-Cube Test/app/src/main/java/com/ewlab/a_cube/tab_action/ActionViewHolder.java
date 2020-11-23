package com.ewlab.a_cube.tab_action;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.external_devices.KeyDetails;
import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.TabGames;
import com.ewlab.a_cube.voice.AudioCommandsList;

public class ActionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final MainActivity mainActivity;
    private ImageView image_action;
    private TextView name_action;
    private ImageButton delete_action;
    private CardView cardView_action_image;

    public ActionViewHolder(@NonNull View itemView, MainActivity mainActivity) {

        super(itemView);
        image_action = itemView.findViewById(R.id.image_action);
        name_action = itemView.findViewById(R.id.name_action);
        delete_action = itemView.findViewById(R.id.delete_action);
        cardView_action_image = itemView.findViewById(R.id.cardView_action_image);
        itemView.setOnClickListener(this);
        this.mainActivity = mainActivity;
    }

    public void setCellActionButton(String n) {

        image_action.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_buttons_white_24dp));
        int color = mainActivity.getApplicationContext().getResources().getColor(R.color.blue);
        cardView_action_image.setCardBackgroundColor(color);
        name_action.setText(n);
        delete_action.setOnClickListener(delete_actionListener);

    }

    public void setCellActionVocal(String n) {

        image_action.setImageDrawable(mainActivity.getResources().getDrawable(R.drawable.ic_mic_white_24dp));
        int color = mainActivity.getApplicationContext().getResources().getColor(R.color.purple);
        cardView_action_image.setCardBackgroundColor(color);
        delete_action.setOnClickListener(delete_actionListener);
        name_action.setText(n);

    }

    @Override
    public void onClick(View v) {

        String nome = name_action.getText().toString();
        Action action = MainModel.getInstance().getAction(nome);
        Intent toActionDetail;

        if(action instanceof ActionButton) {

            toActionDetail = new Intent(v.getContext(), KeyDetails.class);
            toActionDetail.putExtra("action_name",nome);
            mainActivity.startActivity(toActionDetail);
            Log.d("ACTION_TYPE","AZIONE VOCALE, nome: "+ getAdapterPosition());

        }
        else if(action instanceof ActionVocal) {

            toActionDetail = new Intent(v.getContext(), AudioCommandsList.class);
            toActionDetail.putExtra("action_name",nome);
            mainActivity.startActivity(toActionDetail);
            Log.d("ACTION_TYPE","AZIONE VOCALE, nome: "+ getAdapterPosition());
        }
        else {
            Toast.makeText(mainActivity.getApplicationContext(),"Azione undefined",Toast.LENGTH_SHORT).show();
        }

    }

    //Listener che al click del pulsante elimina di fianco ad un'azione la elimina
    public View.OnClickListener delete_actionListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Action actionToRemove = MainModel.getInstance().getActions().get(getAdapterPosition());
            final String action_name = actionToRemove.getName();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
            LayoutInflater inflater = mainActivity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_remove_action, null);

            Button delete = dialogView.findViewById(R.id.deleteAction_btn);
            Button back = dialogView.findViewById(R.id.backAction_btn);

            dialogBuilder.setView(dialogView);
            final AlertDialog alertDialog = dialogBuilder.create();

            if(alertDialog.getWindow() != null)
                Log.d("ALERT_DIALOG","no null, action to remove: "+action_name);
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

                    MainModel.getInstance().removeAction(action_name);
                    MainModel.getInstance().writeActionsJson();
                    MainModel.getInstance().writeModelJson();
                    MainModel.getInstance().writeConfigurationsJson();

                    alertDialog.dismiss();
                    TabAction.actionAdapter.notifyDataSetChanged();
                }
            });

        }
    };
}
