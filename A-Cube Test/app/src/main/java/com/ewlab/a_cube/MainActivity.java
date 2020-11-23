package com.ewlab.a_cube;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.tab_games.DialogGame;
import com.ewlab.a_cube.tab_games.TabGames;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_MULTIPLE = 1;
    private static final String TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("LIFE_CYCLE","OnCreate MainActivity");
        setTheme(R.style.AppThemeMainActivity);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.actions));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.games));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter(getSupportFragmentManager(),getApplicationContext());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setupWithViewPager(viewPager);

        String numberTab;
        numberTab = getIntent().getStringExtra("tabToActivate");

        if(numberTab != null) {

            switch (numberTab) {

                case "1":
                    TabLayout.Tab tab = tabLayout.getTabAt(0);
                    tab.select();
                    Log.d("TAB_SELECTED","Action");
                    break;

                case "2":
                    TabLayout.Tab tab1 = tabLayout.getTabAt(1);
                    tab1.select();
                    Log.d("TAB_SELECTED","Game");
                    break;
            }
        }

    }


    @Override
    protected void onStart() {

        super.onStart();
        Log.d("LIFE_CYCLE","OnStart MainActivity");

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO}, PERMISSION_MULTIPLE);

            }
            else {

                setFolders();

            }
        }


    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case PERMISSION_MULTIPLE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setFolders();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,R.string.permissions_denied, Toast.LENGTH_SHORT).show();

                }

                break;


            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void setFolders() {

        File dir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "A-Cube");

        if (!dir.exists()) {

            Log.d("LIFE_CYCLE","Dentro if setFolders()");

            File dir2 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "A-cube"), "Models");
            File dir3 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + File.separator + "A-cube"), "Sounds");
            dir2.mkdirs();
            dir3.mkdirs();

            JSONObject Jgames = new JSONObject();
            JSONObject Jactions = new JSONObject();
            JSONObject Jconfigurations = new JSONObject();
            JSONObject Jmodels = new JSONObject();

            JSONArray games = new JSONArray();
            JSONArray actions = new JSONArray();
            JSONArray configurations = new JSONArray();
            JSONArray models = new JSONArray();

            try {

                Jactions.put("actions", actions);
                Jgames.put("games", games);
                Jconfigurations.put("configurations", configurations);
                Jmodels.put("models", models);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        MainModel.getInstance().setActions();
        MainModel.getInstance().setGames();
        MainModel.getInstance().writeActionsJson();
        MainModel.getInstance().writeGamesJson();
        MainModel.getInstance().setSvmModels();
        MainModel.getInstance().setConfigurations();
        MainModel.getInstance().writeModelJson();
        MainModel.getInstance().writeConfigurationsJson();

        Log.d(TAG, getPackageName());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO: gestisci il tasto info: AlertDialog al posto di questa cosa qua del menù

        switch (item.getItemId()) {

            case R.id.menu_info:
                Context context = getApplicationContext();
                CharSequence text = "Hai premuto il tasto Info!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /*
    @Override
    public void applyTest(String userStr) {
        String text;

        Log.d("applyTest", "" + userStr);

        text = R.string.configuration_deleted + userStr;
        Game gameRemove = MainModel.getInstance().getGame(userStr);
        MainModel.getInstance().removeGame(gameRemove.getTitle());

        MainModel.getInstance().writeConfigurationsJson();
        MainModel.getInstance().writeGamesJson();
        //TabGames.gamesAdapter.remove(gameRemove);
        //TabGames.gamesAdapter.notifyDataSetChanged();
    }

     */
}
