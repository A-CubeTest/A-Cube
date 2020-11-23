package com.ewlab.a_cube;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.ConfigurationDetail;
import com.ewlab.a_cube.tab_games.ConfigurationList;

import static android.support.v4.content.ContextCompat.startActivity;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        String noti = intent.getStringExtra("noti");
        //Intent per chiudere notification bar
        Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        switch(noti) {

            case "noti_disable":

                Toast.makeText(context,"Disable action tapped",Toast.LENGTH_LONG).show();
                //DISABILITO L'ACC SERVICE
                Intent intent_disable = new Intent(context, RecorderService.class);
                intent_disable.putExtra("disable",true);
                intent_disable.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent_disable);
                //CHIUDO NOTIFICATION BAR
                context.sendBroadcast(it);

                break;

            case "noti_nextConf":

                Toast.makeText(context,"NextConf action tapped",Toast.LENGTH_LONG).show();
                int index = intent.getIntExtra("nextConf",0);
                String game_title = intent.getStringExtra("game_title");
                Log.d("INDEX",""+index + "Game_title: "+ game_title);
                //PASSO ALLA NUOVA CONFIGURAZIONE
                Intent intent_nextConf = new Intent(context,RecorderService.class);
                intent_nextConf.putExtra("nextConf",index);
                intent_nextConf.putExtra("nextConf2",true);
                intent_nextConf.putExtra("nextConf3",game_title);
                intent_nextConf.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(intent_nextConf);
                //CHIUDO NOTIFICATION BAR
                context.sendBroadcast(it);

                break;

            case "noti_modify":

                Toast.makeText(context,"Modify action tapped",Toast.LENGTH_LONG).show();
                String title = intent.getStringExtra("title_noti");
                String confName = intent.getStringExtra("confName_noti");
                Intent intent_modify = new Intent(context, ConfigurationDetail.class);
                intent_modify.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent_modify.putExtra("title_noti",title);
                intent_modify.putExtra("confName_noti",confName);
                startActivity(context,intent_modify,null);
                //CHIUDO NOTIFICATION BAR
                context.sendBroadcast(it);

                break;

            case "noti_addConfig":

                Toast.makeText(context,"Notification no config tapped",Toast.LENGTH_LONG).show();

                String packageName = intent.getStringExtra("packageName");
                String appName = intent.getStringExtra("appName");
                String icon = intent.getStringExtra("icon");
                Log.d("NOTI_ADD_CONFIG","Nome app: "+appName+", icon: "+icon);

                Game newGame = new Game(packageName, appName, icon);
                Game gamesAdded = MainModel.getInstance().addNewGame(newGame);

                if(gamesAdded != null) {

                    MainModel.getInstance().writeGamesJson();
                    MainModel.getInstance().writeConfigurationsJson();

                }

                Intent intent_addConfig = new Intent(context, ConfigurationList.class);
                intent_addConfig.putExtra("appName", appName);
                intent_addConfig.putExtra("icon", icon);
                intent_addConfig.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(context,intent_addConfig,null);
                //CHIUDO NOTIFICATION BAR
                context.sendBroadcast(it);

                break;

            default:
                break;
        }

    }
}
