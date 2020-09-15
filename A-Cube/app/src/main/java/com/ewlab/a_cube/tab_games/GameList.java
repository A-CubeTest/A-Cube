package com.ewlab.a_cube.tab_games;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;
import java.util.List;

public class GameList extends ListActivity {

    private PackageManager packageManager = null;
    private List<ApplicationInfo> applist = null;
    private GameInstalledAdapter listadapter = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        packageManager = getPackageManager();

        new LoadApplications().execute();

        EditText search = findViewById(R.id.gameSearch);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                listadapter.getFilter().filter(s);

            }

            @Override
            public void afterTextChanged(Editable s) {
            listadapter.notifyDataSetChanged();
            }
        });

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id){
        super.onListItemClick(l,v,position,id);

        //prendo la posizione dell'item cliccato
        ApplicationInfo app = listadapter.getItem(position);

        //prendo il packagename, il titolo e l'icona del gioco che andrò a salvare nel Json
        String appPackageName = app.packageName;
        String appTitle = (String) app.loadLabel(packageManager);
        Drawable appIcon = app.loadIcon(packageManager);

        String Icon = MainModel.getInstance().getBitmapFromDrawable(appIcon);
        Game newGame = new Game(appPackageName, appTitle, Icon);
        Game gamesAdded = MainModel.getInstance().addNewGame(newGame);

        if(gamesAdded!=null){
            Toast.makeText(this, R.string.new_game, Toast.LENGTH_LONG).show();

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("tabToActivate", "2");
            startActivity(intent);
            MainModel.getInstance().writeGamesJson();
            MainModel.getInstance().writeConfigurationsJson();

        }else{
            Toast.makeText(this, R.string.game_exists, Toast.LENGTH_LONG).show();
        }

    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list){

        ArrayList<ApplicationInfo> applist = new ArrayList<ApplicationInfo>();

        for(ApplicationInfo info : list){

            try{
                if(packageManager.getLaunchIntentForPackage(info.packageName) != null){
                    applist.add(info);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return applist;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void>{

        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... voids) {

            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listadapter = new GameInstalledAdapter(GameList.this, R.layout.game_list_item, applist);


            return null;
        }

        protected void onPostExecute(Void result){
            setListAdapter(listadapter);
            progress.dismiss();
            super.onPostExecute(result);
        }

        protected void onPreExecute(){
            progress = ProgressDialog.show(GameList.this, null, getString(R.string.app_info));
            super.onPreExecute();
        }
    }
}
