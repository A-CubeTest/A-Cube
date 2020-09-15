package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.SVMmodel;
import com.ewlab.a_cube.svm.Features;
import com.ewlab.a_cube.svm.SVM;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import libsvm.svm;
import libsvm.svm_model;

public class ConfigurationDetail extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DialogAddConfig.DialogConfigListener {
    private static final String TAG = ConfigurationDetail.class.getName();

    private String title;
    private String confName;

    private LinkAdapter adapter;
    private ListView listEvent;

    private Configuration thisConf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration_detail);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Context context = getApplicationContext();
        title = getIntent().getStringExtra("title");
        confName = getIntent().getStringExtra("name");

        final Game thisGame = MainModel.getInstance().getGame(title);
        thisConf = MainModel.getInstance().getConfiguration(title, confName);
        this.setTitle(title+" - "+confName);

        TextView name = findViewById(R.id.ConfName);
        ImageButton deleteConfButton = findViewById(R.id.deleteConfButton);
        TextView defEvents = findViewById(R.id.defEvents);
        TextView undefEvents = findViewById(R.id.undefEvents);
        listEvent = findViewById(R.id.event_list);

        name.setText(confName);

        deleteConfButton.setBackgroundResource(R.drawable.rounded_button_false);
        deleteConfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getSupportFragmentManager();
                DialogRemoveConfig alertDialog = new DialogRemoveConfig();
                alertDialog.show(fm, "fragment_alert");
                alertDialog.getData(thisConf);
            }
        });

        //I get arrays containing links
        int defEventsSize = thisConf.definedLinks();
        int undefEventsSize = thisConf.undefinedLinks();
        Log.d(TAG, defEventsSize+" "+undefEventsSize);

        //show on screen the number of defined and undefined Links
        if(defEventsSize>0 & undefEventsSize==0 ) {
            String a = getString(R.string.defined_events, String.valueOf(defEventsSize));
            defEvents.setText(a);
            defEvents.setVisibility(View.VISIBLE);
            undefEvents.setVisibility(View.GONE);
            Log.d(TAG, "A");
        }

        if(defEventsSize==0 & undefEventsSize>0){
            String b = getString(R.string.undefined_events, String.valueOf(undefEventsSize));
            undefEvents.setText(b);
            undefEvents.setVisibility(View.VISIBLE);
            defEvents.setVisibility(View.GONE);
            Log.d(TAG, "B");

        }

        if(defEventsSize>0 && undefEventsSize>0){
            String a = getString(R.string.defined_events, String.valueOf(defEventsSize));
            String b = getString(R.string.undefined_events, String.valueOf(undefEventsSize));
            defEvents.setText(a);
            undefEvents.setText(b);
            defEvents.setVisibility(View.VISIBLE);
            undefEvents.setVisibility(View.VISIBLE);
            Log.d(TAG, "C");

        }

        //        //I update the graphics and the functions of the button that manages the vocal training
        voiceButtonUpdate(thisConf);
        final LinearLayout progBar = findViewById(R.id.linear_layout_progress_bar);
        Button voiceButton = findViewById(R.id.voiceRecognitionButton);

        voiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                if(checkForNoise()){
                    progBar.setVisibility(View.VISIBLE);

                    ConfigurationDetail.OnTaskCompleted OTClistener;

                    ConfigurationDetail.TrainingModel tm = new ConfigurationDetail.TrainingModel();
                    Object[] app = {title,confName};
                    tm.execute(app);

//                }else{
//                    Context context = getApplicationContext();
//                    int duration = Toast.LENGTH_SHORT;
//
//                    Toast toast = Toast.makeText(context, R.string.noise_missing, duration);
//                    toast.show();
//
//                }

            }
        });

        ArrayList<Link> links = thisConf.getLinks();
        final Intent newLink = new Intent(this, NewLink.class);


        if(links.size()>0) {
            setListView(links);


            listEvent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Link item = (Link) listEvent.getItemAtPosition(i);
                    newLink.putExtra("title", title);
                    newLink.putExtra("name", confName);
                    newLink.putExtra("event", item.getEvent().getName());
                    Log.d("NewLink", title + " " + newLink + " " + item.getEvent().getName());
                    startActivity(newLink);
                }
            });

        }else{
            TextView noLinks = findViewById(R.id.noLinksText);
            noLinks.setVisibility(View.VISIBLE);
        }


        FloatingActionButton newEvent =  findViewById(R.id.newEvent);
        newEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newLink.putExtra("title", title);
                newLink.putExtra("name", confName);
                startActivity(newLink);
            }
        });
    }

    private TreeMap<String, ArrayList<String>> startTraining(String title, String config) {

        //thisConfVocalActions are all the vocal actions linked in a specific configuration of a specific game
        Configuration thisConf = MainModel.getInstance().getConfiguration(title, config);
        List<ActionVocal> thisConfVocalActions = thisConf.getVocalActions();


        TreeMap<String, ArrayList<String>> actionsSoundsMap = new TreeMap<>();

        for(ActionVocal aiv : thisConfVocalActions){
            //files are all files of an action linked in this conf of this game
            Set<String> files = aiv.getFiles();


            for(Iterator<String> iterator = files.iterator(); iterator.hasNext();){
                String s =  iterator.next();

                if(actionsSoundsMap.containsKey(aiv.getName())){
                    ArrayList<String> listFilesTemp = actionsSoundsMap.get(aiv.getName());
                    listFilesTemp.add(s);
                    actionsSoundsMap.put(aiv.getName(), listFilesTemp);

                }else{

                    ArrayList<String> listFilesTemp = new ArrayList<>();
                    listFilesTemp.add(s);
                    actionsSoundsMap.put(aiv.getName(), listFilesTemp);
                }
            }

        }

        return actionsSoundsMap;
    }

    @Override
    public void applyTest(String userStr, boolean response) {

    }

    public interface OnTaskCompleted{
        public void onReqCompleted();

    }


    private class TrainingModel extends AsyncTask<Object, Void, Void> {
        ConfigurationDetail.OnTaskCompleted taskCompleted;

        public void setListener(ConfigurationDetail.OnTaskCompleted a){taskCompleted = a; }

        @Override
        protected Void doInBackground(Object... strings) {
            TreeMap<String, ArrayList<String>> actionsSoundsMap = null;

            actionsSoundsMap = startTraining((String)strings[0], (String)strings[1]);

            Features features = new Features(getApplicationContext());//getApplicationContext());

            HashMap<String, HashSet<float[]>> clasFeats = features.getClassifiedFeatures(actionsSoundsMap);

            ArrayList<String> sounds = new ArrayList<>(clasFeats.keySet());
            int time = (int) (System.currentTimeMillis());
            Timestamp tsTemp = new Timestamp(time);
            String ts =  tsTemp.toString();

            String modelName = "ModelN°"+ts;

            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Models";
            File savedModel = new File(path, modelName);

            if(!savedModel.exists()) {
                try {
                    savedModel.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                savedModel.delete();
            }

            SVM svm = new SVM(clasFeats);
            svm.runModel();

            Log.d(TAG,"Precision "+ Double.toString(svm.getPrecision()));
            Log.d(TAG,"Recall " + Double.toString(svm.getRecall()));
            Log.d(TAG,"F1 " + Double.toString(svm.getF1()));

            svm_model model = svm.getModel();
            libsvm.svm classifier = new svm();
            try {
                classifier.svm_save_model(savedModel.getAbsolutePath(),model);

                ArrayList<ActionVocal> vocals = new ArrayList<>();
                for(String sound : sounds){

                    if(sound.equals(SVMmodel.NOISE_NAME)){
                        ActionVocal Noise = new ActionVocal("Noise");
                        vocals.add(Noise);

                    }else{
                        vocals.add((ActionVocal) MainModel.getInstance().getAction(sound));
                    }

                }

                //we set the new model created within the configuration
                if(vocals.size()>1) {
                    SVMmodel newSvmModel = new SVMmodel(modelName, vocals);
                    MainModel.getInstance().addNewModel(newSvmModel);
                    Configuration thisConf = MainModel.getInstance().getConfiguration((String) strings[0], (String) strings[1]);
                    thisConf.setModel(newSvmModel);
                }

                MainModel.getInstance().writeModelJson();
                MainModel.getInstance().writeConfigurationsJson();


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void onPostExecute(Void result) {
            // execution of result of Long time consuming operation
            Button voiceRecognitionButton = findViewById(R.id.voiceRecognitionButton);
            voiceRecognitionButton.setEnabled(false);
            voiceRecognitionButton.setBackgroundResource(R.drawable.rounded_button_true);
            voiceRecognitionButton.setText("Voice Recognition OK");
            LinearLayout progBar = findViewById(R.id.linear_layout_progress_bar);
            progBar.setVisibility(View.GONE);
        }
    }


    /**
     * This method receives a configuration as input and, based on its characteristics, modifies the button that is used to train the vocal model.
     * If the configuration has no link with vocal actions, the button remains invisible.
     * If the configuration has vocal actions but does not have an svmModel and no svmModel saved is suitable for it the button becomes red and clickable.
     * If the configuration already has an associated model or one of the associated models has the same vocal actions present in the configuration the button turns green and is not clickable
     * @param thisConf
     */
    public void voiceButtonUpdate(Configuration thisConf) {

        Button voiceRecognitionButton = findViewById(R.id.voiceRecognitionButton);

        boolean noVocal =  thisConf.getVocalActions().isEmpty();


        if(noVocal){
            voiceRecognitionButton.setVisibility(View.GONE);
            Log.d(TAG, "no vocal found");

        }else{
            Log.d(TAG, "vocal found");

            List<ActionVocal> vocalActionsInThisConf = thisConf.getVocalActions();
            SVMmodel svmModel = MainModel.getInstance().getSVMmodel(vocalActionsInThisConf);

            if (svmModel!=null) {
                thisConf.setModel(svmModel);
                voiceRecognitionButton.setEnabled(false);
                voiceRecognitionButton.setVisibility(View.VISIBLE);
                voiceRecognitionButton.setBackgroundResource(R.drawable.rounded_button_true);
                voiceRecognitionButton.setText(R.string.model_updated);

            } else {
                voiceRecognitionButton.setEnabled(true);
                voiceRecognitionButton.setVisibility(View.VISIBLE);
                voiceRecognitionButton.setBackgroundResource(R.drawable.rounded_button_false);
                voiceRecognitionButton.setText(R.string.model_to_update);
            }
        }

    }

    private void setListView(ArrayList<Link> links) {
        adapter = new LinkAdapter(this, android.R.layout.list_content, links);

        listEvent.setAdapter(adapter);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

//    public boolean checkForNoise(){
//        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Sounds/noise.wav";
//        File noise = new File (filePath);
//
//        return noise.exists();
//    }

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
                alertDialog.getData(getString(R.string.configuration)+"2");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, ConfigurationList.class);
        intent.putExtra("title", title);
        intent.putExtra("name", confName);
        startActivity(intent);
    }

}
