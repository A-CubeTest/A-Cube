package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.MainActivity;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;

import java.util.ArrayList;

public class ConfigurationList extends AppCompatActivity implements DialogAddConfig.DialogConfigListener {

    private static final String TAG = ConfigurationList.class.getName();

    private ConfigurationAdapter confAdapter;

    private ListView listView;

    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_list);

        listView = findViewById(R.id.configuration_list);
        TextView noConfText = findViewById(R.id.noConfigurationsFounded);

        title = getIntent().getStringExtra("title");
        this.setTitle(title + " - "+getString(R.string.configuration));

        ArrayList<Configuration> allConfigurations = MainModel.getInstance().getConfigurations();
        ArrayList<Configuration> confToShow = new ArrayList<>();

        for(Configuration conf : allConfigurations){
            if(conf.getGame().getTitle().equals(title)){
                confToShow.add(conf);
            }
        }

        setListView(confToShow);

        if(confToShow.size()==0){
            noConfText.setVisibility(View.VISIBLE);
        }else{
            noConfText.setVisibility(View.GONE);
        }

        final Intent confDetails = new Intent(this, ConfigurationDetail.class);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Configuration item = (Configuration) listView.getItemAtPosition(i);
                confDetails.putExtra("title", title);
                confDetails.putExtra("name", item.getConfName());
                startActivity(confDetails);
            }
        });

        FloatingActionButton newConf = findViewById(R.id.newConfiguration);
        newConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                DialogAddConfig alertDialog = new DialogAddConfig();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(title);
            }
        });
    }

    @Override
    public void applyTest(String userStr, boolean response) {
        String text;

        Log.d(TAG, "Dialog: "+response+userStr);

        if(response){
            text =  getString(R.string.new_conf);
            Configuration confAdded = MainModel.getInstance().getConfiguration(title, userStr);
            confAdapter.add(confAdded);
            confAdapter.notifyDataSetChanged();
            TextView noConfText = findViewById(R.id.noConfigurationsFounded);
            noConfText.setVisibility(View.GONE);

        }else{
            text =  getString(R.string.new_conf_error);
        }

        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

    }

    private void setListView(ArrayList<Configuration> configurations) {
        confAdapter = new ConfigurationAdapter(this, android.R.layout.list_content, configurations);

        listView.setAdapter(confAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_info:

                FragmentManager fm = getSupportFragmentManager();
                DialogTutorial alertDialog = new DialogTutorial();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(getString(R.string.configuration));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("tabToActivate", "2");
        startActivity(intent);
    }
}
