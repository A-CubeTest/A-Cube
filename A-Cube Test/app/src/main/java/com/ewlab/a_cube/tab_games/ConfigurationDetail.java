package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.SVMmodel;
import com.ewlab.a_cube.model.Screen;
import com.ewlab.a_cube.svm.Features;
import com.ewlab.a_cube.svm.SVM;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

import libsvm.svm;
import libsvm.svm_model;

public class ConfigurationDetail extends AppCompatActivity implements AdapterView.OnItemSelectedListener, DialogAddConfig.DialogConfigListener {
    private static final String TAG = ConfigurationDetail.class.getName();

    public static String gameName;
    public static String confName;

    public static ScreenAdapter screenAdapter;

    RecyclerView recyclerView_screen;
    TextView conf_name_title, game_name_title, screen_name;
    ImageView screen_image;
    ImageButton button_back;
    Button vocal_rec_btn;
    FloatingActionButton newScreen;
    ConstraintLayout constraint_no_screens;
    AlertDialog.Builder dialogBuilderTraining;
    AlertDialog alertDialogTraining;

    private Configuration thisConf;
    ArrayList<Screen> screens = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("LIFE_CYCLE","OnCreate ConfigurationDetail");
        //NASCONDO BARRA DEL TITOLO
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();

        setContentView(R.layout.activity_configuration_detail2);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(getIntent().getStringExtra("title_noti") != null
                && getIntent().getStringExtra("confName_noti") != null) {

            gameName = getIntent().getStringExtra("title_noti");
            confName = getIntent().getStringExtra("confName_noti");

        }
        else {

            gameName = getIntent().getStringExtra("title");
            confName = getIntent().getStringExtra("name");

        }

        conf_name_title = findViewById(R.id.conf_name_title);
        conf_name_title.setText(confName);
        game_name_title = findViewById(R.id.game_name_title);
        game_name_title.setText(gameName);

        constraint_no_screens = findViewById(R.id.constraint_no_screens);

        thisConf = MainModel.getInstance().getConfiguration(gameName, confName);

        button_back = findViewById(R.id.back_btn);
        button_back.setOnClickListener(buttonListener);

        vocal_rec_btn = findViewById(R.id.vocal_rec_btn);

        screen_name = findViewById(R.id.name_screen);
        screen_image = findViewById(R.id.image_screen);

        recyclerView_screen = findViewById(R.id.recycle_view_screen);
        recyclerView_screen.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        //RIDUCO LO SPAZIO FRA LE COLONNE DI QUESTA RECYCLER VIEW ORIZZONTALE
        int spaceInPixels = 5;
        recyclerView_screen.addItemDecoration(new RecyclerViewScreenItemDecorator(spaceInPixels));
        screenAdapter = new ScreenAdapter(this);
        recyclerView_screen.setAdapter(screenAdapter);
        screenAdapter.notifyDataSetChanged();

        screens = MainModel.getInstance().getScreensFromConf(thisConf);
        //CONTROLLO SE CI SONO SCREEN DA MOSTRARE
        if(screens.size() == 0) {

            recyclerView_screen.setVisibility(View.GONE);
            constraint_no_screens.setVisibility(View.VISIBLE);

        }
        else {

            constraint_no_screens.setVisibility(View.GONE);
            recyclerView_screen.setVisibility(View.VISIBLE);
            //CONTROLLO SE HO LANDSCAPE SCREEN COSì CAMBIO L'ORIENTAMENTO DELLA LISTA
            if(! screens.get(0).isPortrait()) {

                recyclerView_screen.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));

            }

        }

        //UPDATE THE BUTTON FOR THE STATE OF THE VOICE RECOGNITION
        voiceButtonUpdate(thisConf);

        //FAB ADD SCREEN BUTTON
        newScreen = findViewById(R.id.newScreen);
        newScreen.setOnClickListener(buttonListener);
        vocal_rec_btn.setOnClickListener(buttonListener);
    }

    @Override
    protected void onResume() {

        super.onResume();
        Log.d("LIFE_CYCLE","OnResume ConfigurationDetail");
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();

        //setContentView(R.layout.activity_configuration_detail);
        setContentView(R.layout.activity_configuration_detail2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if(getIntent().getStringExtra("title_noti") != null
                && getIntent().getStringExtra("confName_noti") != null) {

            gameName = getIntent().getStringExtra("title_noti");
            confName = getIntent().getStringExtra("confName_noti");

        }
        else {

            gameName = getIntent().getStringExtra("title");
            confName = getIntent().getStringExtra("name");

        }

        conf_name_title = findViewById(R.id.conf_name_title);
        conf_name_title.setText(confName);
        game_name_title = findViewById(R.id.game_name_title);
        game_name_title.setText(gameName);

        constraint_no_screens = findViewById(R.id.constraint_no_screens);

        thisConf = MainModel.getInstance().getConfiguration(gameName, confName);

        button_back = findViewById(R.id.back_btn);
        button_back.setOnClickListener(buttonListener);

        vocal_rec_btn = findViewById(R.id.vocal_rec_btn);

        screen_name = findViewById(R.id.name_screen);
        screen_image = findViewById(R.id.image_screen);

        recyclerView_screen = findViewById(R.id.recycle_view_screen);
        recyclerView_screen.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        //RIDUCO LO SPAZIO FRA LE COLONNE DI QUESTA RECYCLER VIEW ORIZZONTALE
        int spaceInPixels = 5;
        recyclerView_screen.addItemDecoration(new RecyclerViewScreenItemDecorator(spaceInPixels));

        screenAdapter = new ScreenAdapter(this);
        recyclerView_screen.setAdapter(screenAdapter);
        screenAdapter.notifyDataSetChanged();

        screens = MainModel.getInstance().getScreensFromConf(thisConf);
        //CONTROLLO SE CI SONO SCREEN DA MOSTRARE
        if(screens.size() == 0) {

            recyclerView_screen.setVisibility(View.GONE);
            constraint_no_screens.setVisibility(View.VISIBLE);

        }
        else {

            constraint_no_screens.setVisibility(View.GONE);
            recyclerView_screen.setVisibility(View.VISIBLE);
            //CONTROLLO SE HO LANDSCAPE SCREEN COSì CAMBIO L'ORIENTAMENTO DELLA LISTA
            if(! screens.get(0).isPortrait()) {

                recyclerView_screen.setLayoutManager(new LinearLayoutManager(this,
                        LinearLayoutManager.VERTICAL, false));

            }

        }

        //UPDATE THE BUTTON FOR THE STATE OF THE VOICE RECOGNITION
        voiceButtonUpdate(thisConf);

        //FAB ADD SCREEN BUTTON
        newScreen = findViewById(R.id.newScreen);
        newScreen.setOnClickListener(buttonListener);
        vocal_rec_btn.setOnClickListener(buttonListener);

    }

    private TreeMap<String, ArrayList<String>> startTraining(String title, String config) {

        //thisConfVocalActions are all the vocal actions linked in a specific configuration of a specific game
        Configuration thisConf = MainModel.getInstance().getConfiguration(title, config);
        //List<ActionVocal> thisConfVocalActions = thisConf.getVocalActions();
        List<ActionVocal> thisConfVocalActions = MainModel.getInstance().getVocalActionsFromConf(thisConf);

        TreeMap<String, ArrayList<String>> actionsSoundsMap = new TreeMap<>();

        for(ActionVocal aiv : thisConfVocalActions){
            //files are all files of an action linked in this conf of this game
            Set<String> files = aiv.getFiles();


            for(Iterator<String> iterator = files.iterator(); iterator.hasNext();) {

                String s =  iterator.next();

                if(actionsSoundsMap.containsKey(aiv.getName())) {

                    ArrayList<String> listFilesTemp = actionsSoundsMap.get(aiv.getName());
                    listFilesTemp.add(s);
                    actionsSoundsMap.put(aiv.getName(), listFilesTemp);

                }
                else {

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

    public interface OnTaskCompleted {

        public void onReqCompleted();

    }


    private class TrainingModel extends AsyncTask<Object, Void, Void> {

        ConfigurationDetail.OnTaskCompleted taskCompleted;

        public void setListener(ConfigurationDetail.OnTaskCompleted a) { taskCompleted = a; }

        @Override
        protected Void doInBackground(Object... strings) {

            Log.d("TrainingModel","sto facendo il training...");

            TreeMap<String, ArrayList<String>> actionsSoundsMap = null;
            Log.d("TrainingModel","String[0] : "+ (String)strings[0]);
            Log.d("TrainingModel","String[1] : "+ (String)strings[1]);
            actionsSoundsMap = startTraining((String)strings[0], (String)strings[1]);

            Features features = new Features(getApplicationContext());//getApplicationContext());

            HashMap<String, HashSet<float[]>> clasFeats = features.getClassifiedFeatures(actionsSoundsMap);

            ArrayList<String> sounds = new ArrayList<>(clasFeats.keySet());
            int time = (int) (System.currentTimeMillis());
            Timestamp tsTemp = new Timestamp(time);
            String ts =  tsTemp.toString();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault());
            String currentDateTime = simpleDateFormat.format(new Date());

            //String modelName = "ModelN°"+ts;
            String modelName = "Model_"+currentDateTime;

            //TODO C'è DA ELIMINARE GLI ALTRI MODEL CHE RIMANGONO IN MEMORIA
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/A-Cube/Models";
            File savedModel = new File(path, modelName);

            if(!savedModel.exists()) {

                try {
                    savedModel.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            else{
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

                    if(sound.equals(SVMmodel.NOISE_NAME)) {

                        ActionVocal Noise = new ActionVocal("Noise");
                        vocals.add(Noise);

                    }
                    else {
                        vocals.add((ActionVocal) MainModel.getInstance().getAction(sound));
                    }

                }

                //we set the new model created within the configuration
                if(vocals.size() > 1) {

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

            /*
            // execution of result of Long time consuming operation
            Button voiceRecognitionButton = findViewById(R.id.voiceRecognitionButton);
            voiceRecognitionButton.setEnabled(false);
            voiceRecognitionButton.setBackgroundResource(R.drawable.rounded_button_true);
            voiceRecognitionButton.setText("Voice Recognition OK");
            LinearLayout progBar = findViewById(R.id.linear_layout_progress_bar);
            progBar.setVisibility(View.GONE);


            relative_container.setEnabled(false);
            models_progress_bar.setVisibility(View.GONE);
            progress_bar_image.setImageDrawable(getApplicationContext().getResources()
                    .getDrawable(R.drawable.ic_updated));
            models_progress_bar_text.setText(R.string.model_updated);

             */
            voiceButtonUpdate(thisConf);
            alertDialogTraining.dismiss();

            Log.d("TrainingModel","finito training");


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

        vocal_rec_btn = findViewById(R.id.vocal_rec_btn);

        //boolean noVocal =  thisConf.getVocalActions().isEmpty();
        boolean noVocal =  MainModel.getInstance().getVocalActionsFromConf(thisConf).isEmpty();

        Log.d("VOCAL_RECOGNITION", "function voiceButtonUpdate started");

        if(noVocal) {

            vocal_rec_btn.setEnabled(false);
            vocal_rec_btn.setBackground(getDrawable(R.drawable.btn_vocal_rec_state_not_ok_background));
            vocal_rec_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_update_not_possible_white,
                    0,0,0);
            vocal_rec_btn.setText(R.string.btn_vocal_rec_state_text_not_possible);

            Log.d("VOCAL_RECOGNITION", "no vocal found");

        }
        else {

            Log.d("VOCAL_RECOGNITION", "vocal found");

            List<ActionVocal> vocalActionsInThisConf = MainModel.getInstance().getVocalActionsFromConf(thisConf);
            //SVMmodel svmModel = MainModel.getInstance().getSVMmodel(vocalActionsInThisConf);
            //TODO FORSE DOVREI SETTARE L'SVM MODEL TROVATO NELLA LINEA SOPRA A QUESTA CONF UNA VOLTA CHE LO TROVO
            SVMmodel svmModel = thisConf.getModel();

            if (svmModel != null) {

                Log.d("VOCAL_RECOGNITION", "vocal recognition ok");
                thisConf.setModel(svmModel);

                vocal_rec_btn.setEnabled(false);
                vocal_rec_btn.setBackgroundResource(R.drawable.btn_vocal_rec_state_ok_background);
                vocal_rec_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_updated_white,
                        0,0,0);
                vocal_rec_btn.setText(R.string.btn_vocal_rec_state_text_ok);

            }
            else {

                Log.d("VOCAL_RECOGNITION", "vocal recognition to update");
                vocal_rec_btn.setEnabled(true);
                vocal_rec_btn.setBackgroundResource(R.drawable.btn_vocal_rec_state_update_background);
                vocal_rec_btn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_update_white,
                        0,0,0);
                vocal_rec_btn.setText(R.string.btn_vocal_rec_state_text_update);

            }
        }

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
    public void onBackPressed() {

        //String icon = MainModel.getInstance().getGame(gameName).getIcon();
        Intent intent = new Intent(this, ConfigurationList.class);
        intent.putExtra("game_title", gameName);
        //intent.putExtra("game_icon", icon);
        startActivity(intent);
    }

    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {

                case R.id.back_btn:

                    onBackPressed();
                    break;

                case R.id.newScreen:

                    final Intent newScreen_intent = new Intent(getApplicationContext(), ScreenActivity.class);

                    newScreen_intent.putExtra("game_name", gameName);
                    newScreen_intent.putExtra("conf_name",confName);
                    newScreen_intent.putExtra("screens_size",screens.size());
                    newScreen_intent.putExtra("fromActivity","ConfDetail_newScreen");

                    startActivity(newScreen_intent);

                    break;

                case R.id.vocal_rec_btn:

                    if(gameName != null && confName != null) {
                        Object [] app = {gameName,confName};
                        new TrainingModel().execute(app);
                        //MOSTRO DIALOG DEL TRAINING IN CORSO
                        alertDialogTraining();
                    }


                    break;

                case R.id.info_btn:

                    break;

            }

        }
    };

    //METODO PER MOSTRARE ALERTDIALOG INFO
    public void alertDialogInfoBtn() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ConfigurationDetail.this);
        LayoutInflater inflater = ConfigurationDetail.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_info_screen_activity, null);

        Button back = dialogView.findViewById(R.id.backScreen_btn);

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


    }

    //METODO PER MOSTRARE ALERTDIALOG TRAINING DEL MODELLO SVM
    public void alertDialogTraining() {

        dialogBuilderTraining = new AlertDialog.Builder(ConfigurationDetail.this);
        LayoutInflater inflater = ConfigurationDetail.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_training_model, null);

        dialogBuilderTraining.setView(dialogView);
        alertDialogTraining = dialogBuilderTraining.create();

        alertDialogTraining.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialogTraining.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        //COSI' NON PUO' ESSERE DISABILITATO L'AlertDialog
        alertDialogTraining.setCanceledOnTouchOutside(false);
        alertDialogTraining.show();

    }

/*
    public void toScreenshots() {
        Intent intent = new Intent(this, ScreenActivity.class);
        intent.putExtra("game_name", gameName);
        intent.putExtra("conf_name",confName);
        startActivity(intent);
    }

 */

}
