package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
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
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;

public class GameViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final MainActivity mainActivity;
    private ImageView image_game;
    private TextView name_game, info_game_tv;
    private ImageButton delete_game;
    private ConstraintLayout constraintLayout_appIconCircle;
    private CardView cardView_game;

    public GameViewHolder(@NonNull View itemView, MainActivity mainActivity) {
        super(itemView);
        image_game = itemView.findViewById(R.id.image_game);
        name_game = itemView.findViewById(R.id.name_game);
        info_game_tv = itemView.findViewById(R.id.info_game_tv);
        delete_game = itemView.findViewById(R.id.delete_game);
        constraintLayout_appIconCircle = itemView.findViewById(R.id.app_icon_circle);
        cardView_game = itemView.findViewById(R.id.cardView_game_image);
        itemView.setOnClickListener(this);
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {

        String nome = name_game.getText().toString();
        Game game = MainModel.getInstance().getGame(nome);
        String icon = game.getIcon();

        Intent confList = new Intent(v.getContext(), ConfigurationList.class);
        confList.putExtra("game_title", nome);
        confList.putExtra("game_icon", icon);
        mainActivity.startActivity(confList);

    }

    public void setCellGameGreen(String n, ApplicationInfo ai, PackageManager packageManager) {

        name_game.setText(n);
        image_game.setImageDrawable(ai.loadIcon(packageManager));
        info_game_tv.setText(R.string.singleGameView_green_tv);
        info_game_tv.setTextColor(mainActivity.getColor(R.color.emerald));
        constraintLayout_appIconCircle.setBackground(mainActivity.getDrawable(R.drawable.app_icon_circle_green));
        delete_game.setOnClickListener(delete_gameListener);

    }

    public void setCellGameOrange(String n, ApplicationInfo ai, PackageManager packageManager) {

        name_game.setText(n);
        image_game.setImageDrawable(ai.loadIcon(packageManager));
        info_game_tv.setText(R.string.singleGameView_orange_tv);
        info_game_tv.setTextColor(mainActivity.getColor(R.color.orange));
        constraintLayout_appIconCircle.setBackground(mainActivity.getDrawable(R.drawable.app_icon_circle_orange));
        delete_game.setOnClickListener(delete_gameListener);

    }

    public void setCellGameRed(String n, ApplicationInfo ai, PackageManager packageManager) {

        name_game.setText(n);
        image_game.setImageDrawable(ai.loadIcon(packageManager));
        info_game_tv.setText(R.string.singleGameView_red_tv);
        info_game_tv.setTextColor(mainActivity.getColor(R.color.red));
        constraintLayout_appIconCircle.setBackground(mainActivity.getDrawable(R.drawable.app_icon_circle_red));
        delete_game.setOnClickListener(delete_gameListener);

    }

    //Listener che al click del pulsante elimina di fianco ad un gioco lo elimina
    public View.OnClickListener delete_gameListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Game gameToRemove = MainModel.getInstance().getGames().get(getAdapterPosition());
            final String game_name = gameToRemove.getTitle();

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mainActivity);
            LayoutInflater inflater = mainActivity.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.alert_dialog_remove_game, null);

            Button delete = dialogView.findViewById(R.id.deleteGame_btn);
            Button back = dialogView.findViewById(R.id.backGame_btn);

            dialogBuilder.setView(dialogView);
            final AlertDialog alertDialog = dialogBuilder.create();

            if(alertDialog.getWindow() != null)
                Log.d("ALERT_DIALOG","no null, game to remove: "+game_name);
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

                    MainModel.getInstance().removeGame(game_name);
                    MainModel.getInstance().writeConfigurationsJson();
                    MainModel.getInstance().writeGamesJson();
                    alertDialog.dismiss();
                    TabGames.gamesAdapter.notifyDataSetChanged();
                }
            });

        }
    };

    public Bitmap stringToBitmap(String encodedString){
        try{
            byte [] encodeByte = Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        }
        catch(Exception e){
            e.getMessage();
            return null;
        }
    }
}
