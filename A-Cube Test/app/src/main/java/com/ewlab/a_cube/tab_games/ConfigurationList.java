package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class ConfigurationList extends AppCompatActivity {

    private static final String TAG = ConfigurationList.class.getName();

    public static ConfigurationAdapter confAdapter;

    RecyclerView recyclerView_conf;
    ImageView app_icon;
    CardView cardView;
    TextView name_conf,text_conf, conf_list_title_tv, conf_list_tv1, conf_list_tv2;
    ImageButton back_btn, delete_conf;
    RadioButton selected_conf;
    ConstraintLayout constraintLayout2;

    public static String game_name, game_icon;
    //STRING PER CAPIRE SE STO ASSEGNANDO UN NUOVO NOME ALLA CONF, O LO STO MODIFICANDO
    public static  String operation;
    //STRING PER PORTARMI DIETRO IL NOME DELLA CONF NEL CASO DI MODIFICA DI UNA CONF ESISTENTE
    public static  String cName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("LIFE_CYCLE","OnCreate ConfigurationList");
        //rimuovo barra con il titolo dell'app
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_configuration_list);

        conf_list_title_tv = findViewById(R.id.conf_list_title_tv);
        conf_list_tv1 = findViewById(R.id.conf_list_tv1);
        conf_list_tv2 = findViewById(R.id.conf_list_tv2);

        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(back_btnListener);

        //TODO RecycleView configurations
        name_conf = findViewById(R.id.name_conf);
        text_conf = findViewById(R.id.text_conf);
        delete_conf = findViewById(R.id.delete_conf);
        selected_conf = findViewById(R.id.selected_conf);

        TextView noConfText = findViewById(R.id.noConfigurationsFounded);

        if(getIntent().getStringExtra("appName") != null
                && getIntent().getStringExtra("icon") != null) {

            game_name = getIntent().getStringExtra("appName");
            Log.d("NOTI_ADD_CONFIG","Nome app: "+ game_name);
        }
        else {

            Toast.makeText(this,getIntent().getStringExtra("game_title"),Toast.LENGTH_SHORT).show();
            game_name = getIntent().getStringExtra("game_title");

        }

        conf_list_title_tv.setText(game_name);

        recyclerView_conf = findViewById(R.id.recycle_view_configuration);
        recyclerView_conf.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        confAdapter = new ConfigurationAdapter(this);
        recyclerView_conf.setAdapter(confAdapter);
        confAdapter.notifyDataSetChanged();

        if(MainModel.getInstance().getConfigurationsFromGame(game_name).size() == 0)
            noConfText.setVisibility(View.VISIBLE);
        else
            noConfText.setVisibility(View.GONE);

        FloatingActionButton newConf = findViewById(R.id.newConfiguration);
        newConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                operation = "newConf";
                cName = "";
                toFragmentAddConf();

            }
        });
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d("LIFE_CYCLE","OnResume ConfigurationList");
        //rimuovo barra con il titolo dell'app
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_configuration_list);

        conf_list_title_tv = findViewById(R.id.conf_list_title_tv);
        conf_list_tv1 = findViewById(R.id.conf_list_tv1);
        conf_list_tv2 = findViewById(R.id.conf_list_tv2);

        back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(back_btnListener);

        //TODO RecycleView configurations
        name_conf = findViewById(R.id.name_conf);
        text_conf = findViewById(R.id.text_conf);
        delete_conf = findViewById(R.id.delete_conf);
        selected_conf = findViewById(R.id.selected_conf);

        TextView noConfText = findViewById(R.id.noConfigurationsFounded);

        if(getIntent().getStringExtra("appName") != null
                && getIntent().getStringExtra("icon") != null) {

            game_name = getIntent().getStringExtra("appName");
            Log.d("NOTI_ADD_CONFIG","Nome app: "+ game_name);
        }
        else {

            Toast.makeText(this,getIntent().getStringExtra("game_title"),Toast.LENGTH_SHORT).show();
            game_name = getIntent().getStringExtra("game_title");

        }

        conf_list_title_tv.setText(game_name);

        recyclerView_conf = findViewById(R.id.recycle_view_configuration);
        recyclerView_conf.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                false));
        confAdapter = new ConfigurationAdapter(this);
        recyclerView_conf.setAdapter(confAdapter);
        confAdapter.notifyDataSetChanged();

        if(MainModel.getInstance().getConfigurationsFromGame(game_name).size() == 0)
            noConfText.setVisibility(View.VISIBLE);
        else
            noConfText.setVisibility(View.GONE);

        FloatingActionButton newConf = findViewById(R.id.newConfiguration);
        newConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                operation = "newConf";
                cName = "";
                toFragmentAddConf();

            }
        });
    }

    @Override
    public void onBackPressed() {

        Log.d("LIFE_CYCLE","OnBackPressed in ConfigurationList");
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("tabToActivate", "2");
        startActivity(intent);

    }

    public void addFragmentAddConf(Fragment fragment, boolean addToBackStack, String tag) {

        Bundle bundle = new Bundle();
        bundle.putString("gameName", game_name);
        bundle.putString("operation",operation);
        bundle.putString("cName",cName);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_addConf, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void toFragmentAddConf() {
        addFragmentAddConf(new FragmentAddConfiguration(), false, "fragmentAddConf");
    }

    public View.OnClickListener back_btnListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            Intent back_intent = new Intent(getApplicationContext(), MainActivity.class);
            back_intent.putExtra("tabToActivate", "2");
            startActivity(back_intent);

        }
    };

}
