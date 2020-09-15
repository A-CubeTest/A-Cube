package com.ewlab.a_cube.accessibilityservice;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.ewlab.a_cube.accessibilityservice.model.Action;
import com.ewlab.a_cube.accessibilityservice.model.ActionButton;
import com.ewlab.a_cube.accessibilityservice.model.ActionVocal;
import com.ewlab.a_cube.accessibilityservice.model.Configuration;
import com.ewlab.a_cube.accessibilityservice.model.Event;
import com.ewlab.a_cube.accessibilityservice.model.Game;
import com.ewlab.a_cube.accessibilityservice.model.Link;
import com.ewlab.a_cube.accessibilityservice.model.MainModel;
import com.ewlab.a_cube.accessibilityservice.model.SVMmodel;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.mfcc.MFCC;
import libsvm.svm;
import libsvm.svm_node;

public class RecorderService extends AccessibilityService {
    private static final String TAG = "RecorderService";

    public Configuration thisConf;
    public Game thisGame;

    StringBuilder linksNames;

    public static String UP = "Up";
    public static String DOWN = "Down";
    public static String RIGHT = "Right";
    public static String LEFT = "Left";

    private static AccessibilityServiceInfo info = new AccessibilityServiceInfo();

    private VoiceCommandListener vcl = null;

    GestureDescription.StrokeDescription interruptibleStroke = null;

    public String lastEventType = "";


    private AccessibilityNodeInfo root;

    // metodo per leggere il nome dell'applicazione che si appoggia al servizio
    private String getEventText(AccessibilityEvent event) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence s : event.getText()) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        String Permits="";
        if(intent.getExtras()!=null) {
            if (intent.getExtras().containsKey("Permits")) {
                Permits = intent.getStringExtra("Permits");

                if (Permits.equals("Denied")) {
                    this.disableSelf();
                }
            }
        }
        return START_STICKY;
    }

    // metodo per gestire gli eventi che sono intercettati dal servizio
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        final int eventType = accessibilityEvent.getEventType();
        // memorizzo il nome dell'applicazione

        MainModel.getInstance();
        MainModel.getInstance().setActions();
        MainModel.getInstance().setGames();
        MainModel.getInstance().setSvmModels();
        MainModel.getInstance().setConfigurations();

        String appPackage = (String) accessibilityEvent.getPackageName();

        if (appPackage != null) {
            if (appPackage.contains("a_cube")) {
                Log.d(TAG, "disabled");
                this.disableSelf();
            }
        }
        thisGame = MainModel.getInstance().getGameFromBundleId(appPackage);

        List<Configuration> allConfigurations = MainModel.getInstance().getConfigurations();
        if (thisGame != null && thisGame != MainModel.getInstance().lastGame) {

            if (MainModel.getInstance().lastGame != null && thisGame != null && thisGame.getTitle().equals(MainModel.getInstance().lastGame.getTitle())) {
                Log.d("ThisAndLast:", thisGame.getTitle() + " " + MainModel.getInstance().lastGame.getTitle());

            }else{
                for (Configuration conf : allConfigurations) {
                    if (conf.getGame().equals(thisGame) && conf.getSelected()) {

                        thisConf = conf;
                        ArrayList<Link> links = conf.getLinks();

                        //se non Ã¨ mai stato usato viene inizializzato
                        if (links.size() >= 1) {
                            linksNames = new StringBuilder("Configuration found: " + conf.getConfName() + "\n");
                            for (Link l : links) {
                                linksNames.append(" Action: ").append(l.getAction().getName()).append(" Event: ").append(l.getEvent().getName()).append("\n");
                            }
                        }

                        Toast.makeText(getApplicationContext(), linksNames.toString(), Toast.LENGTH_LONG).show();

                        MainModel.getInstance().lastGame = thisGame;
                    }
                }

            }

            //se non trovo un gioco valido resetto linksNames
        }else{
            linksNames = new StringBuilder();
        }


        //check if asynctask for recognition voice is actived
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && vcl != null) {
            vcl.cancel(true);
            vcl = null;
        }

        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && MainModel.getInstance().getGameFromBundleId(appPackage) != null) {
            Log.v(TAG, String.format(
                    "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                    AccessibilityEvent.eventTypeToString(eventType), accessibilityEvent.getClassName(), accessibilityEvent.getPackageName(),
                    accessibilityEvent.getEventTime(), thisGame.getTitle()));


            List<ActionButton> devices = thisConf.getButtonActions();
            //String device = configurations.getLinkedDevice(app);
            if (devices.size() > 0) {
                Log.d("+++++++++ PRIMO DISPOSITIVO DELLA CONFUNO", devices.get(0).getName());
            }

            //controllo se esiste il file games.json e quanti giochi ha al suo interno
            List<Game> games = MainModel.getInstance().getGames();
            if (games.size() > 0) {
                Log.d("DIMENSIONE LIST DI GAMES -----> ", "" + games.size());
            }

            //controllo se esiste il file actions.json e quante azioni ha al suo interno
            List<Action> actions = MainModel.getInstance().getActions();
            if (actions.size() > 0) {
                Log.d("DIMENSIONE LIST DI ACTIONS -----> ", "" + actions.size());
            }

            //controllo se esiste il file models.json e quanti modelli ha al suo interno
            List<SVMmodel> svMmodels = MainModel.getInstance().getSvmModels();
            if (svMmodels.size() > 0) {
                Log.d("DIMENSIONE LIST DI MODELS -----> ", "" + svMmodels.size());
            }

            //controllo se esiste il file configurations.json e quante configurazioni e link ha al suo interno
            List<Configuration> configurations = MainModel.getInstance().getConfigurations();
            if (configurations.size() > 0) {
                Log.d("DIMENSIONE LIST DI CONFIGURATIONS -----> ", "" + configurations.size());
                for (Configuration conf : configurations) {
                    List<Link> links = conf.getLinks();
                    Log.d("DIMENSIONE LIST DI LINK IN " + conf.getGame().getTitle() + " -----> ", "" + thisConf.getLinks().size());
                }
            }

            //controlla se ci sono azioni vocali e se la configurazione ha un modello
            if (thisConf.getVocalActions().size() > 0 && thisConf.getModel() != null) {
                Log.d(TAG, "vocal action founded");
                String[] paramString = new String[]{thisConf.getGame().getTitle()};
                vcl = new VoiceCommandListener();
                vcl.execute(paramString);
            }
        }
    }

    @Override
    protected void onServiceConnected() {

        super.onServiceConnected();

        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        setServiceInfo(info);

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISSIONINREC", "Permission denied");

            Intent intent = new Intent(this, Permissions.class);
            startActivity(intent);
        }else{
            Log.d("PERMISSIONINREC", "Permission accomplished");
        }
    }

    //metodo override per gestire le interazioni con il dispositivo
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        try {
            final int keyCode = event.getKeyCode();
            final int keyAction = event.getAction();
            String azioneDown = "";
            String azioneUp = "";

            MainModel.getInstance();
            MainModel.getInstance().setActions();
            MainModel.getInstance().setGames();
            MainModel.getInstance().setSvmModels();
            MainModel.getInstance().setConfigurations();

            Log.d(TAG, "keycode - keyaction " + keyCode + " " + keyAction);

            // ricavo dal codide dell'evento l'azione dell'utente, sia alla pressione del bottone (azioneDown), che al rilascio dello stesso (azioneUp)
            List<ActionButton> actionButtons = MainModel.getInstance().getButtonActions();
            for (ActionButton actionButton : actionButtons) {
                if (actionButton.getKeyId().equals(String.valueOf(keyCode)) && keyAction == 0) {
                    azioneDown = actionButton.getName();

                } else if (actionButton.getKeyId().equals(String.valueOf(keyCode))) {
                    azioneUp = actionButton.getName();
                }
            }

            if (azioneDown.length() > 0) {
                Log.d(TAG, "Configurazione" + thisConf.getConfName());
                Log.d(TAG, "Azione D " + azioneDown);
                Log.d(TAG, "Azione U " + azioneUp);

                //ricavo il link partendo dall'azione generata
                //l'azione potrebbe essere associata sia ad un evento standard che a uno di tipo long tap inputLength in questo caso troveremo thisLinkStop
                Link thisLink = thisConf.getLinkFromAction(azioneDown);
                Link thisLinkStop = thisConf.getLinkFromActionStop(azioneDown);

                //se l'evento era di tipo normale avremo trovato thisLink!=null
                if (thisLink != null) {
                    Event evento = thisLink.getEvent();
                    lastEventType = evento.getType();

                    // ricavo le coordinate dall'evento associato a thisLink
                    double coordinateX = evento.getX();
                    double coordinateY = evento.getY();

                    Log.d(TAG, evento.getName() + " X - Y : " + coordinateX + " - " + coordinateY);
                    double[] coordinate = {coordinateX, coordinateY};

                    doActionDown(evento, coordinate);
                    //se l'evento era di tipo long tap input length thisLinkStop!=null
                } else if (thisLinkStop != null) {
                    Log.d(TAG, "evento di tipo On/Off interrotto");

                    Event evento = thisLinkStop.getEvent();

                    //se l'evento associato a questa azione Ã¨ di tipo input length vorrÃ  dire che, al rilascio del bottone, l'evento va interrotto
                    doActionUp();

                    return true;
                }
                return true;

                //al rilascio di un tasto l'utente genera una azioneUp

            } else if (azioneUp.length() > 0 & !lastEventType.equals(Event.LONG_TAP_TIMED_TYPE) & !lastEventType.equals(Event.LONG_TAP_ON_OFF_TYPE)) {
                Log.d(TAG, "actionUp reset");
                doActionUp();

                if (thisConf.getLinkFromAction(azioneUp) != null) {

                    Link thisLink = thisConf.getLinkFromAction(azioneUp);
                    Event evento = thisLink.getEvent();

                    //se l'evento associato a questa azione Ã¨ di tipo input length vorrÃ  dire che, al rilascio del bottone, l'evento va interrotto
                    if (evento.getType().equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                        doActionUp();
                    }

                    return true;
                }

            } else {
                return false;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        if (thisConf.getVocalActions().size() > 0 && thisConf.getModel() != null) {
            Log.d(TAG, "vocal action founded");
            String[] paramString = new String[]{thisConf.getGame().getTitle()};
            vcl = new VoiceCommandListener();
            vcl.execute(paramString);
        }


        return false;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void doActionDown(Event event, double[] coordinates) { //ArrayList<int[]> coordinates
        Link thisLink = thisConf.getLink(event.getName());

        int[] newCoord = new int[2];
        if (!event.getPortrait()) {
            newCoord[0] = (int) ((coordinates[1]) * getScreenDimension()[0]);//1440);
            newCoord[1] = (int) ((1 - (coordinates[0])) * getScreenDimension()[1]);
            Log.d(TAG, "new coord x" + newCoord[0] + " y " + getScreenDimension()[1]);//newCoord[1]);
        } else {
            newCoord[0] = (int) (coordinates[0] * getScreenDimension()[0]);//720);
            newCoord[1] = (int) (coordinates[1] * getScreenDimension()[1]);//1440);
            Log.d(TAG, "new coord x" + newCoord[0] + " y " + newCoord[1]);
        }

        switch (event.getType()) {
            case "Tap":
                Log.d(TAG, "Ho fatto un tap");
                generaTap(newCoord);
                //result = true;
                break;
            case "Swipe - Up":
                Log.d(TAG, "Ho fatto uno swipe in su");
                generaSwipe(newCoord, UP);
//                doActionUp();
                break;
            case "Swipe - Down":
                Log.d(TAG, "Ho fatto uno swipe in giÃ¹");
                generaSwipe(newCoord, DOWN);
                doActionUp();
                break;
            case "Swipe - Right":
                Log.d(TAG, "Ho fatto uno swipe a destra");
                generaSwipe(newCoord, RIGHT);
                doActionUp();
                break;
            case "Swipe - Left":
                Log.d(TAG, "Ho fatto uno swipe a sinistra");
                generaSwipe(newCoord, LEFT);
                doActionUp();
                break;
            case "Long Tap - input length":
                Log.d(TAG, "Ho fatto un long tap con durata gestita dal mio tocco");
                generaLongTapInterruptible(newCoord);
                break;
            case "Long Tap - ON/OFF":
                Log.d(TAG, "Ho fatto un long tap che puÃ² essere fermato da " + thisLink.getActionStop());
                generaLongTapInterruptible(newCoord);
                break;
            case "Long Tap - timed":
                Log.d(TAG, "Ho fatto un long tap con durata di " + thisLink.getDuration() + " seconds");
                generaLongTapTimed(newCoord, thisLink.getDuration()); //generaLongTapTimed
                break;
        }

    }

    //genera un tap in una porzione inesistente dello schermo causando un'interruzione della gesture precedente
    private void doActionUp() {
        Log.d(TAG, "sei in action Up");
        int[] newCoord = new int[2];
        newCoord[0] = 10000;
        newCoord[1] = 10000;

        generaTap(newCoord);
    }


    @Override
    public void onInterrupt() {
        Log.v(TAG, "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "Disconesso");
        return super.onUnbind(intent);
    }

    // il metodo permette di generare due tipi di click a seconda del valore che il parametro booleano ha impostato
    // se false faccio un click della durata di 1 ms sufficiente per il sistema per generare questo tipo di gesture
    // se true faccio un click della durata di 1 min per simulare un long click
    private void generaTap(int[] coord) {

        int X = coord[0];
        int Y = coord[1];

        GestureDescription.Builder gestureB = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
        GestureDescription.StrokeDescription stroke = null;
        stroke = new GestureDescription.StrokeDescription(clickPath, 0, 1); //duration modificata

        gestureB.addStroke(stroke);
        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture non completata");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private void generaLongTapTimed(int[] coord, double duration) {

        int X = coord[0];
        int Y = coord[1];

        GestureDescription.Builder gestureB = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
        GestureDescription.StrokeDescription stroke = null;
        stroke = new GestureDescription.StrokeDescription(clickPath, 0, (int) duration * 1000);

        gestureB.addStroke(stroke);
        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture non completata");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private void generaLongTapInterruptible(int[] coord) {
        Log.d(TAG, "sei in long tap interruptible");
        int X = coord[0];
        int Y = coord[1];

        GestureDescription.Builder gestureB = new GestureDescription.Builder();
        Path clickPath = new Path();
        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
        interruptibleStroke = new GestureDescription.StrokeDescription(clickPath, 0, (int) 60000);

        gestureB.addStroke(interruptibleStroke);
        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG, "Gesture non completata");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generaSwipe(int[] coord, final String direction) {
        int startX = coord[0];
        int startY = coord[1];

        Log.d(TAG, startX + " " + startY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        if (direction.equals(UP)) {
            Log.d(TAG, "Swipe - Up");
            path.moveTo(startX, startY);
            path.lineTo(startX, startY - 300);


        } else if (direction.equals(DOWN)) {
            Log.d(TAG, "Swipe - Down");
            path.moveTo(startX, startY);
            path.lineTo(startX, startY + 300);


        } else if (direction.equals(RIGHT)) {
            Log.d(TAG, "Swipe - Right");

            path.moveTo(startX, startY);
            path.lineTo(startX + 300, startY);


        } else if (direction.equals(LEFT)) {
            Log.d(TAG, "Swipe - Left");
            path.moveTo(startX, startY);
            path.lineTo(startX - 300, startY);

        }


        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, 100, true);
        gestureBuilder.addStroke(strokeDescription);
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
    }


    private class VoiceCommandListener extends AsyncTask<String, Void, Void> {
        private int bufferSize = 0;
        private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        private static final int RECORDER_SAMPLERATE = 44100;
        private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;

        //campiono la voce a 16 KHz (dipende da come ho campionato il segnale quando lo acquisito)
        private int sampleRate;
        //campioni per frame
        private int sampleForFrame; //(320 -> 20 ms; 512 -> 32 ms)
        //overlapping dei frame del 50%
        private int bufferOverlap;
        //dimensione di ogni campione in termini di bits
        private int bits;
        //audio mono-channel
        private int channel;
        //numero di features estratte da ogni frame
        private int melCoefficients;//13; (se non consodero il primo coefficiente)
        //numero di filtri da applicare per estrarre le features
        private int melFilterBank;
        //minima frequenza di interesse
        private int lowFilter;
        //massima frequenza di interesse
        private int highFilter;
        //range di valori che puÃ² assumere ogni campione (0-255 -> unsigned; -127-+128 -> signed)
        private boolean signed;
        //modo in cui vengono memorizzati i bits
        private boolean big_endian;
        //dimensione dei vettori
        private int vectorDim;

        private int label, predLabel, u, noise, counter;

        double wa, wn, pa, pn;

        private String previousAction;
        private int[] counterPred;

        private int[] noiseCounter = new int[1];
        private int[] timeCounter = new int[1];
        private String[] lastEventType = new String[1];

        public VoiceCommandListener() {
            super();
            sampleRate = 44100;
            sampleForFrame = 1024;
            bufferOverlap = 512;
            bits = 16;
            channel = 2;
            melCoefficients = 21;
            melFilterBank = 32;
            lowFilter = 30;
            highFilter = 3000;
            signed = true;
            big_endian = false;
            vectorDim = 20;
            label = -1;
            predLabel = -1;
            previousAction = "";

            noiseCounter[0] = 0;
            timeCounter[0] = 0;
            lastEventType[0] = "";
        }

        @Override
        protected Void doInBackground(String... strings) {
            final ArrayList<String> predictedActions = new ArrayList<>();
            final ArrayList<Calendar> timePredictedActions = new ArrayList<>();
            final ArrayList<String> subRange4Actions = new ArrayList<>();
            final ArrayList<double[]> probabilityActions = new ArrayList<>();
            final LinkedList<Integer> slidWind = new LinkedList<>();
            final LinkedList<double[]> probSlidWind = new LinkedList<>();
            counter = 0;

            try {

                SVMmodel thisModel = thisConf.getModel();
                Log.d(TAG, thisModel.getName());

                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube/Models";
                File fileSVM = new File(path, thisModel.getName());

                final svm svm = new svm();
                final libsvm.svm_model model = libsvm.svm.svm_load_model(fileSVM.getAbsolutePath());

                final ArrayList<String> svmClasses = new ArrayList<>();

                for (ActionVocal actionVocal : thisModel.getSounds()) {
                    svmClasses.add(actionVocal.getName());
                }

                for (String cls : svmClasses)
                    Log.d("Classe svm -----> ", cls);

                counterPred = new int[svmClasses.size()];

                if (fileSVM.exists()) {

                    //definizione della dimensione del buffer con i parametri definiti inizialmente
                    bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, RECORDER_AUDIO_ENCODING);
                    //oggetto recorder per l'acuisizione dell'audio da microfono
                    //POSSO USARE MIC QUANDO USO DIRETTAMENTE IL MICROFONO
                    final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

                    Log.d("++++++++++++++++++", "INIZIO A REGISTRARE!!!!!!");

                    recorder.startRecording();

                    //buffer per la lettura dell'audio da microfono
                    byte data[] = new byte[bufferSize];

                    int read = 0;

                    while (!isCancelled()) {
                        //acquisizione del file audio
                        read = recorder.read(data, 0, bufferSize);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {

                            InputStream is = new ByteArrayInputStream(data);
                            final AudioDispatcher dispatcher = new AudioDispatcher(new UniversalAudioInputStream(is, new TarsosDSPAudioFormat(sampleRate, bits, channel, signed, big_endian)), sampleForFrame, bufferOverlap);
                            final MFCC mfcc = new MFCC(sampleForFrame, sampleRate, melCoefficients, melFilterBank, lowFilter, highFilter);
                            dispatcher.addAudioProcessor(mfcc);
                            dispatcher.addAudioProcessor(new AudioProcessor() {
                                @RequiresApi(api = Build.VERSION_CODES.O)
                                @Override
                                public boolean process(AudioEvent audioEvent) {
                                    timePredictedActions.add(Calendar.getInstance());
                                    float[] audio_float = new float[21];
                                    mfcc.process(audioEvent);
                                    audio_float = mfcc.getMFCC();

                                    float[] temp = new float[vectorDim];
                                    //rimuovo il primo coefficiente della window perchÃ¨ rappresenta l'RMS (= info sulla potenza della finestra)
                                    for (int i = 1, k = 0; i < audio_float.length; i++, k++) {// i = 1
                                        temp[k] = audio_float[i];
                                    }

                                    float[] normVector = normalize(temp);

                                    svm_node[] node = new svm_node[vectorDim];
                                    for (int i = 0; i < vectorDim; i++) {
                                        svm_node nodeT = new svm_node();
                                        nodeT.index = i;
                                        nodeT.value = normVector[i];
                                        node[i] = nodeT;
                                    }

                                    double[] probability = new double[svmClasses.size()];

                                    predLabel = (int) libsvm.svm.svm_predict_probability(model, node, probability);

                                    Log.d("LABEL PREDETTA ----> ", String.valueOf(predLabel));

                                    //timePredictedActions.add(new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime()));
                                    /***********
                                     BLOCCO DI CODICE UTILE PER LA GENERAZIONE DEI DATI DEL CSV
                                     USO IL CODICE PER MODIFICARE LA TECNICA DI CORREZIONE DEL CLASSIFICATORE
                                     ***********/
                                    String labelClass = svmClasses.get(predLabel);
                                    predictedActions.add(labelClass);
                                    probabilityActions.add(probability);

                                    Event event = null;
                                    boolean stopAction = false;

                                    if (!labelClass.equals("Noise")) {

                                        Link thisLink = thisConf.getLinkFromAction(labelClass);

                                        if (thisLink==null) {
                                            Link thisLinkStop = thisConf.getLinkFromActionStop(labelClass);
                                            event = thisLinkStop.getEvent();
                                            lastEventType[0] = Event.LONG_TAP_ON_OFF_TYPE;
                                            stopAction = true;

                                            Log.d(TAG, "da ora stop");

                                        }else{

                                            event = thisLink.getEvent();
                                            lastEventType[0] = event.getType();
                                        }

                                    } else {
                                        noiseCounter[0] = 1 + noiseCounter[0];

                                    }

                                    timeCounter[0] = 1 + timeCounter[0];

                                    if(timeCounter[0] == 30){
                                        timeCounter[0] = 0;
                                        noiseCounter[0] = 0;
                                    }


                                    if (slidWind.size() == 0) {
                                        if (!labelClass.equals("Noise")) {
                                            Log.d(TAG, "G");

                                            //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                            if (timeCounter[0] == 29 && noiseCounter[0] > 15 && lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                doActionUp();
                                                timeCounter[0] = 0;
                                                noiseCounter[0] = 0;
                                            }

                                            probSlidWind.add(probability);
                                            slidWind.add(predLabel);

                                            pa = probability[predLabel];

                                            if (pa > 0.95) {
                                                Log.d(TAG, "F");

                                                //se ricevo un'azione diversa da "Noise" ma stopAction == true vuol dire che quella Azione == stop e blocco la gesture
                                                if(!stopAction) {
                                                    double[] coordinate = {event.getX(), event.getY()};
                                                    doActionDown(event, coordinate);

                                                    Log.d("Azione ", "1");
                                                }else{
                                                    doActionUp();
                                                }

                                                previousAction = labelClass;
                                            } else {
                                                // else it does nothing,
                                                //subRange4Actions.add("Noise");
                                                previousAction = "Noise";
                                            }
                                            pa = 0;

                                        } else {
                                            Log.d(TAG, "E");
                                            //subRange4Actions.add("Noise")

                                            //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                            if (timeCounter[0] == 29 && noiseCounter[0] > 15 && lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                doActionUp();
                                                timeCounter[0] = 0;
                                                noiseCounter[0] = 0;
                                            }

                                            previousAction = "Noise";
                                        }
                                    } else {
                                        //blocco if per gestire quando iniziare a considerare la sliding windows
                                        if (!labelClass.equals("Noise") && probability[predLabel] > 0.95 && previousAction.equals("Noise")) { //!labelClass.equals("Noise") && probability[predLabel] > 0.95 && previousAction.equals("Noise")
                                            //controllare se tutto il blocco Ã¨ corretto
                                            Log.d(TAG, "D");

                                            //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                            if (timeCounter[0] == 29 && noiseCounter[0] > 15 && !lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                doActionUp();
                                                timeCounter[0] = 0;
                                                noiseCounter[0] = 0;
                                            }

                                            //se ricevo un'azione diversa da "Noise" ma stopAction Ã¨ true vuol dire che quella Azione Ã¨ di tipo stop e blocco la gesture
                                            if(!stopAction) {

                                                if(!event.getType().equals("Tap")){
                                                    double[] coordinate = {event.getX(), event.getY()};
                                                    doActionDown(event, coordinate);

                                                    Log.d("Azione ", "2");
                                                }
                                            }else{
                                                doActionUp();
                                            }

                                            probSlidWind.remove();
                                            slidWind.remove();
                                            probSlidWind.add(probability);
                                            slidWind.add(predLabel);
                                            previousAction = labelClass;

                                        } else {


                                            if (slidWind.size() == 6) {
                                                probSlidWind.remove();
                                                slidWind.remove();
                                                probSlidWind.add(probability);
                                                slidWind.add(predLabel);
                                            } else {
                                                probSlidWind.add(probability);
                                                slidWind.add(predLabel);
                                            }

                                            String actionToAct = svmClasses.get(slidWind.get(0));

                                            for (int i = 0; i < slidWind.size(); i++) {
                                                String predictedAction = svmClasses.get(slidWind.get(i));
                                                if (!predictedAction.equals("Noise") && predictedAction.equals(actionToAct)) {
                                                    pa += probSlidWind.get(i)[predLabel];
                                                } else {
                                                    pn += probSlidWind.get(i)[predLabel];
                                                }
                                            }

                                            double fa = 1 + (0.1 * (slidWind.size() - 1));
                                            double fn = 0.5 + (0.1 * (slidWind.size() - 1));
                                            wa = pa / slidWind.size() * fa;
                                            wn = pn / slidWind.size() * fn;
                                            Log.d(TAG, "C " + wa+" "+wn);

                                            if (wa > 0.95) {
                                                Log.d(TAG, "B " + previousAction);
                                                if (previousAction.equals("Noise")) {

                                                    //se ricevo un'azione diversa da "Noise" ma stopAction Ã¨ true vuol dire che quella Azione Ã¨ di tipo stop e blocco la gesture
                                                    if(!stopAction) {
                                                        if(!event.getType().equals("Tap")){
                                                            double[] coordinate = {event.getX(), event.getY()};
                                                            doActionDown(event, coordinate);
                                                            Log.d("Azione ", "3");
                                                        }

                                                    }else{
                                                        doActionUp();
                                                    }

                                                    //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                                    if (timeCounter[0] == 29 && noiseCounter[0] > 15 && !lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                        doActionUp();
                                                        timeCounter[0] = 0;
                                                        noiseCounter[0] = 0;
                                                    }

                                                    previousAction = labelClass;
                                                }

                                            } else if (wn > 0.9 && wa < 0.95) {
                                                Log.d(TAG, "A " + previousAction);
                                                //subRange4Actions.add("Noise");
                                                if (!previousAction.equals("Noise")) {
                                                    Log.d(TAG, "label class " + labelClass);

                                                    //se ricevo un'azione diversa da "Noise" ma stopAction Ã¨ true vuol dire che quella Azione Ã¨ di tipo stop e blocco la gesture
                                                    if(!stopAction) {
                                                        //TODO: ho tolto l'attivazione dell'Azione 4, verificare il funzionamento delle altre action
//                                                        Link thisLink = thisConf.getLinkFromAction(previousAction);
//                                                        Event event1 = thisLink.getEvent();
//                                                        double X = event1.getX();
//                                                        double Y = event1.getY();
//
//                                                        double[] coordinate = {X, Y};
//                                                        doActionDown(event1, coordinate);
//
//                                                        Log.d("Azione ", "4");
                                                    }else{
                                                        doActionUp();
                                                    }

                                                    //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                                    if (timeCounter[0] == 29 && noiseCounter[0] > 15 && lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                        doActionUp();
                                                        timeCounter[0] = 0;
                                                        noiseCounter[0] = 0;
                                                    }

                                                    //doActioUp(event.getName(), coordinate);
                                                    probSlidWind.clear();
                                                    slidWind.clear();
                                                    previousAction = "Noise";
                                                }else{

                                                    //se l'evento Ã¨ di tipo input lenght e nelle ultime 29 label predette piÃ¹ della metÃ  ri riferiscono al suono "Noise" stoppo la gesture
                                                    if (timeCounter[0] == 29 && noiseCounter[0] > 15 && lastEventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                                                        doActionUp();
                                                        timeCounter[0] = 0;
                                                        noiseCounter[0] = 0;
                                                    }
                                                }
                                            }
                                            pa = 0;
                                            pn = 0;
                                        }
                                    }
                                    /**************************/


                                    //************************
                                    //CODICE DA USARE OGGI
                                    /*subRange4Actions.add(svmClasses.get(predLabel));
                                    counter++;
                                    if (counter < 3) {
                                        counterPred[predLabel] += 1;
                                    } else if (counter == 3) {
                                        counterPred[predLabel] += 1;

                                        int max = -1;
                                        int index = -1;
                                        int countArray = 0;
                                        for (int i = 0; i < (counterPred.length - 1); i++) {
                                            if (i == 0)
                                                index = i;
                                            for (int j = i + 1; j < counterPred.length; j++) {
                                                if (counterPred[i] < counterPred[j]) {
                                                    index = j;
                                                    i = index;
                                                    break;
                                                }
                                            }
                                        }

                                        String action = svmClasses.get(index);

                                        /*for(String sb : subRange4Actions){
                                            try {
                                                osw.write(sb + "---------->" + action + "\n");
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            //Log.d(sb + "---------->",action);
                                        }*/



                                        /*counter = 0;
                                        subRange4Actions.clear();
                                        for (int i = 0; i < counterPred.length; i++) {
                                            counterPred[i] = 0;
                                        }

                                        if (!action.equals(previousAction) && !previousAction.equals("")) {
                                            //Log.d("**********", Integer.toString(predLabel));
                                            //Log.d("**********", Integer.toString(label));
                                            if (!action.equals("Noise")) {
                                                String descrizione = linksMap.get(action);
                                                String evento = games.ottieniEvento(descrizione);
                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
                                                doActionDown(evento, coordinate);
                                            } else {
                                                String descrizione = linksMap.get(previousAction);
                                                String evento = games.ottieniEvento(descrizione);
                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
                                                doActioUp(evento, coordinate);
                                            }
                                            previousAction = action;
                                        } else if (!action.equals(previousAction) && previousAction.equals("")) {
                                            previousAction = action;
                                        }
                                    }*/
                                    /*******************
                                     *
                                     *
                                     */


                                    //eseguo azione
                                    /*if(label != -1){
                                        if(predLabel != label) {

                                            if (!svmClasses.get(predLabel).equals("Noise")) {
                                                String descrizione = linksMap.get(svmClasses.get(predLabel));
                                                String evento = games.ottieniEvento(descrizione);
                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
                                                doActionDown(evento, coordinate);
                                            } else {
                                                String descrizione = linksMap.get(svmClasses.get(label));
                                                String evento = games.ottieniEvento(descrizione);
                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
                                                doActioUp(evento, coordinate);
                                            }
                                            label = predLabel;
                                        }
                                    }else{
                                        label = predLabel;
                                    }*/
                                    //return true;

                                    //}


                                    return true;
                                }

                                @Override
                                public void processingFinished() {

                                }
                            });
                            dispatcher.run();
                        }
                    }
                    recorder.stop();
                    recorder.release();


                    //SCRIVO IL RISULTATO DELLA PREDIZIONE IN UN FILE TXT
                    /*File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(directory, "predicted_action_A_E_with_probability.txt");
                    if(!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
                    for(int i = 0; i < predictedActions.size(); i++){
                        double[] probs = probabilityActions.get(i);
                        Calendar currentTime = timePredictedActions.get(i);
                        int hours = currentTime.get(Calendar.HOUR_OF_DAY);
                        int minutes = currentTime.get(Calendar.MINUTE);
                        int seconds = currentTime.get(Calendar.SECOND);
                        int milliseconds = currentTime.get(Calendar.MILLISECOND);
                        //String time = "(" + ((i * 23)/2) + " - " + (((i * 23)/2) + 23) + " ) ";
                        //String sentence = timePredictedActions.get(i) + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ]\n";
                        String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ] " + subRange4Actions.get(i) + "\n";
                        osw.write(sentence);;
                    }*/


                    //SCRIVO IL RISULTATO DELLA PREDIZIONE IN UN FILE CSV
                    /*File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    File file = new File(directory, "predicted_action_A_with_probability_5.csv");
                    if(!file.exists()) {
                        file.createNewFile();
                    }
                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
                    osw.write("Time; Prediction; Class A Probability; Class Noise Probability; Prediction with sliding windows\n");
                    osw.flush();

                    for(int i = 0; i < predictedActions.size(); i++){
                        double[] probs = probabilityActions.get(i);
                        Calendar currentTime = timePredictedActions.get(i);
                        int hours = currentTime.get(Calendar.HOUR_OF_DAY);
                        int minutes = currentTime.get(Calendar.MINUTE);
                        int seconds = currentTime.get(Calendar.SECOND);
                        int milliseconds = currentTime.get(Calendar.MILLISECOND);
                        //String time = "(" + ((i * 23)/2) + " - " + (((i * 23)/2) + 23) + " ) ";
                        //String sentence = timePredictedActions.get(i) + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ]\n";
                        //String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ] " + subRange4Actions.get(i) + "\n";
                        String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + ";" +predictedActions.get(i) + ";" + String.format(Locale.ITALIAN,"%.6f",probs[0]) + ";" + String.format(Locale.ITALIAN,"%.6f",probs[1]) + ";" + subRange4Actions.get(i)+"\n";
                        osw.write(sentence);;
                    }*/

                    /*int counter = 0, time = 0, start = 0;
                    int a = 0,noise = 0;
                    String[] actions = new String[3];
                    for(String predA: predictedActions){
                        if(counter < 2){
                            actions[counter++] = predA;
                        }else{
                            actions[counter] = predA;

                            for(String acts : actions){
                                if(acts.equals("a"))
                                    a++;
                                else
                                    noise++;
                            }
                            if(a > noise){
                                osw.write(actions[0] + " -----> a\n");
                            }else{
                                osw.write(actions[0] + " -----> Noise\n");
                            }
                            actions[0] = actions[1];
                            actions[1] = actions[2];
                            a = 0;
                            noise = 0;

                        }
                    }*/

                    //osw.close();

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

    private float[] normalize(float[] f) {
        float[] normArray = new float[f.length];

        float sum = 0;

        for (int i = 0; i < f.length; i++) {
            sum += Math.pow(f[i], 2);
        }

        for (int i = 0; i < f.length; i++) {
            normArray[i] = f[i] / (float) Math.sqrt(sum);
        }

        return normArray;
    }

    private double[] getScreenDimension() {

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();

        double[] screenInformation = new double[2];
        screenInformation[0] = width;
        screenInformation[1] = height;
        String toastMSG = (""+height+"   "+width);

//        Toast.makeText(this, toastMSG, Toast.LENGTH_LONG).show();

        return screenInformation;
    }
}


//package com.example.daniele.accessibilityservice_qaad;
//
//import android.Manifest;
//import android.accessibilityservice.AccessibilityService;
//import android.accessibilityservice.AccessibilityServiceInfo;
//import android.accessibilityservice.GestureDescription;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Path;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.os.AsyncTask;
//import android.os.Build;
//import android.os.Environment;
//import android.support.annotation.RequiresApi;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.KeyEvent;
//import android.view.accessibility.AccessibilityEvent;
//import android.view.accessibility.AccessibilityNodeInfo;
//import android.widget.RelativeLayout;
//
//
//import com.example.daniele.accessibilityservice_qaad.model.Action;
//import com.example.daniele.accessibilityservice_qaad.model.ActionButton;
//import com.example.daniele.accessibilityservice_qaad.model.ActionVocal;
//import com.example.daniele.accessibilityservice_qaad.model.Configuration;
//import com.example.daniele.accessibilityservice_qaad.model.Event;
//import com.example.daniele.accessibilityservice_qaad.model.Game;
//import com.example.daniele.accessibilityservice_qaad.model.Link;
//import com.example.daniele.accessibilityservice_qaad.model.MainModel;
//import com.example.daniele.accessibilityservice_qaad.model.SVMmodel;
//
//import java.io.ByteArrayInputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.LinkedList;
//import java.util.List;
//
//import be.tarsos.dsp.AudioDispatcher;
//import be.tarsos.dsp.AudioEvent;
//import be.tarsos.dsp.AudioProcessor;
//import be.tarsos.dsp.io.TarsosDSPAudioFormat;
//import be.tarsos.dsp.io.UniversalAudioInputStream;
//import be.tarsos.dsp.mfcc.MFCC;
//import libsvm.svm;
//import libsvm.svm_node;
//
//public class RecorderService extends AccessibilityService {
//    private static final String TAG = "RecorderService";
//
//    public Configuration thisConf;
//    public Game thisGame;
//
//    public static String UP = "Up";
//    public static String DOWN = "Down";
//    public static String RIGHT = "Right";
//    public static String LEFT = "Left";
//
//    public String lastEventType = "";
//
//    private static AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//
//    private VoiceCommandListener vcl = null;
//
//    GestureDescription.StrokeDescription interruptibleStroke = null;
//
//    private AccessibilityNodeInfo root;
//
//    RelativeLayout relativeLayout;
//
////    @Override
////    public void onCreate(){
////
////        relativeLayout = new RelativeLayout(getApplicationContext());
////        relativeLayout.setId(Integer.parseInt("12345"));
////        ImageView marker = new ImageView(getApplicationContext());
////        marker.setImageDrawable(getDrawable(R.drawable.pointer));
////        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(100, 100);
////        marker.setLayoutParams(lp);
////
////        WindowManager.LayoutParams topButtonParams = new WindowManager.LayoutParams(
////                (int)getScreenDimension()[0], //The width of the screen
////                (int)getScreenDimension()[1], //The height of the screen
////                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
////                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
////                PixelFormat.TRANSLUCENT);
////
////        //Just trust me, this alpha thing is important.  I know it's weird on a "translucent" view.
////        topButtonParams.alpha = 100;
////
////        marker.setLayoutParams(topButtonParams);
////        relativeLayout.setLayoutParams(topButtonParams);
////        relativeLayout.addView(marker);
////
////
////
////        WindowManager a = new WindowManager() {
////            @Override
////            public Display getDefaultDisplay() {
////                return null;
////            }
////
////            @Override
////            public void removeViewImmediate(View view) {
////
////            }
////
////            @Override
////            public void addView(View view, ViewGroup.LayoutParams params) {
////
////            }
////
////            @Override
////            public void updateViewLayout(View view, ViewGroup.LayoutParams params) {
////
////            }
////
////            @Override
////            public void removeView(View view) {
////
////            }
////        };
////
////        a.addView(relativeLayout, topButtonParams);
////    }
//
//    // metodo per leggere il nome dell'applicazione che si appoggia al servizio
//    private String getEventText(AccessibilityEvent event) {
//        StringBuilder sb = new StringBuilder();
//        for (CharSequence s : event.getText()) {
//            sb.append(s);
//        }
//        return sb.toString();
//    }
//
//
//    // metodo per gestire gli eventi che sono intercettati dal servizio
//    @Override
//    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
//
//        MainModel.getInstance();
//        MainModel.getInstance().setGames();
//        MainModel.getInstance().setActions();
//        MainModel.getInstance().setSvmModels();
//        MainModel.getInstance().setConfigurations();
//
//
//        final int eventType = accessibilityEvent.getEventType();
//        // memorizzo il nome dell'applicazione
//        String appPackage = (String) accessibilityEvent.getPackageName();
//        thisGame = MainModel.getInstance().getGameFromBundleId(appPackage);
//
//        if (appPackage != null) {
//            if (appPackage.equals("com.carlo.a_cube")) {
//                Log.d(TAG, "disabled");
//                this.disableSelf();
//            }
//            //root = this.getRootInActiveWindow();
//            //root.getChild(0).performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS);
//
////            Log.d(TAG, "root: "+root.toString());
//        }
//
//        List<Configuration> allConfigurations = MainModel.getInstance().getConfigurations();
//        if (thisGame != null) {
//            for (Configuration conf : allConfigurations) {
//                if (conf.getGame().equals(thisGame) && conf.getSelected()) {
//                    Log.d(TAG, "CONF TROVATA " + conf.getConfName());
//                    //root.getChild(0).addChild(relativeLayout);
//                    //Log.d(TAG, "123prova "+ root.getChild(0).toString());
//                    //root.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SHOW_ON_SCREEN);
//                    thisConf = conf;
//                }
//            }
//        }
//
//
//        //check if asynctask for recognition voice is actived
//        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && vcl != null) {
//            vcl.cancel(true);
//            vcl = null;
//        }
//
//        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && MainModel.getInstance().getGameFromBundleId(appPackage) != null) {
//            Log.v(TAG, String.format(
//                    "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
//                    AccessibilityEvent.eventTypeToString(eventType), accessibilityEvent.getClassName(), accessibilityEvent.getPackageName(),
//                    accessibilityEvent.getEventTime(), thisGame.getTitle()));
//
//
//            List<ActionButton> devices = thisConf.getButtonActions();
//            //String device = configurations.getLinkedDevice(app);
//            if (devices.size() > 0) {
//                Log.d("+++++++++ PRIMO DISPOSITIVO DELLA CONFUNO", devices.get(0).getName());
//            }
//
//            //controllo se esiste il file games.json e quanti giochi ha al suo interno
//            List<Game> games = MainModel.getInstance().getGames();
//            if (games.size() > 0) {
//                Log.d("DIMENSIONE LIST DI GAMES -----> ", "" + games.size());
//            }
//
//            //controllo se esiste il file actions.json e quante azioni ha al suo interno
//            List<Action> actions = MainModel.getInstance().getActions();
//            if (actions.size() > 0) {
//                Log.d("DIMENSIONE LIST DI ACTIONS -----> ", "" + actions.size());
//            }
//
//            //controllo se esiste il file models.json e quanti modelli ha al suo interno
//            List<SVMmodel> svMmodels = MainModel.getInstance().getSvmModels();
//            if (svMmodels.size() > 0) {
//                Log.d("DIMENSIONE LIST DI MODELS -----> ", "" + svMmodels.size());
//            }
//
//            //controllo se esiste il file configurations.json e quante configurazioni e link ha al suo interno
//            List<Configuration> configurations = MainModel.getInstance().getConfigurations();
//            if (configurations.size() > 0) {
//                Log.d("DIMENSIONE LIST DI CONFIGURATIONS -----> ", "" + configurations.size());
//                for (Configuration conf : configurations) {
//                    List<Link> links = conf.getLinks();
//                    Log.d("DIMENSIONE LIST DI LINK IN " + conf.getGame().getTitle() + " -----> ", "" + thisConf.getLinks().size());
//                }
//            }
//
//            //controlla se ci sono azioni vocali e se la configurazione ha un modello
//            if (thisConf.getVocalActions().size() > 0 && thisConf.getModel() != null) {
//                Log.d(TAG, "vocal action founded");
//                String[] paramString = new String[]{thisConf.getGame().getTitle()};
//                vcl = new VoiceCommandListener();
//                vcl.execute(paramString);
//            }
//        }
//    }
//
//    @Override
//    protected void onServiceConnected() {
//
//        super.onServiceConnected();
//
//        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        setServiceInfo(info);
//
//        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
//                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            Intent intent = new Intent(this, Permissions.class);
//            startActivity(intent);
//        }
//    }
//
//    //metodo override per gestire le interazioni con il dispositivo
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    protected boolean onKeyEvent(KeyEvent event) {
//
//        try {
//            final int keyCode = event.getKeyCode();
//            final int keyAction = event.getAction();
//            String azioneDown = "";
//            String azioneUp = "";
//
//            MainModel.getInstance();
//            MainModel.getInstance().setActions();
//            MainModel.getInstance().setGames();
//            MainModel.getInstance().setSvmModels();
//            MainModel.getInstance().setConfigurations();
//
//            Log.d(TAG, "keycode - keyaction " + keyCode + " " + keyAction);
//
//            // ricavo dal codide dell'evento l'azione dell'utente, sia alla pressione del bottone (azioneDown), che al rilascio dello stesso (azioneUp)
//            List<ActionButton> actionButtons = MainModel.getInstance().getButtonActions();
//            for (ActionButton actionButton : actionButtons) {
//                if (actionButton.getKeyId().equals(String.valueOf(keyCode)) && keyAction == 0) {
//                    azioneDown = actionButton.getName();
//
//                } else if (actionButton.getKeyId().equals(String.valueOf(keyCode))) {
//                    azioneUp = actionButton.getName();
//                }
//            }
//
//            if (azioneDown.length() > 0) {
//                Log.d(TAG, "Configurazione" + thisConf.getConfName());
//                Log.d(TAG, "Azione D " + azioneDown);
//                Log.d(TAG, "Azione U " + azioneUp);
//
//                //ricavo il link partendo dall'azione generata
//                //l'azione potrebbe essere associata sia ad un evento standard che a uno di tipo long tap inputLength in questo caso troveremo thisLinkStop
//                Link thisLink = thisConf.getLinkFromAction(azioneDown);
//                Link thisLinkStop = thisConf.getLinkFromActionStop(azioneDown);
//
//                //se l'evento era di tipo normale avremo trovato thisLink!=null
//                if (thisLink != null) {
//                    Event evento = thisLink.getEvent();
//                    lastEventType = evento.getType();
//
//                    // ricavo le coordinate dall'evento associato a thisLink
//                    double coordinateX = evento.getX();
//                    double coordinateY = evento.getY();
//
//                    Log.d(TAG, evento.getName() + " X - Y : " + coordinateX + " - " + coordinateY);
//                    double[] coordinate = {coordinateX, coordinateY};
//
//                    doActionDown(evento, coordinate);
//
//                    //se l'evento era di tipo long tap input length thisLinkStop!=null
//                } else if (thisLinkStop != null) {
//                    Log.d(TAG, "evento di tipo On/Off interrotto");
//
//                    Event evento = thisLinkStop.getEvent();
//
//                    //se l'evento associato a questa azione Ã¨ di tipo input length vorrÃ  dire che, al rilascio del bottone, l'evento va interrotto
//                    doActionUp();
//
//                    return true;
//                }
//                return true;
//
//                //al rilascio di un tasto l'utente genera una azioneUp
//
//            } else if (azioneUp.length() > 0 & !lastEventType.equals(Event.LONG_TAP_TIMED_TYPE)) {
//                Log.d(TAG, "actionUp reset");
//                doActionUp();
//
////                if (thisConf.getLinkFromAction(azioneUp) != null) {
////
////                    Link thisLink = thisConf.getLinkFromAction(azioneUp);
////                    Event evento = thisLink.getEvent();
////
////                    //se l'evento associato a questa azione Ã¨ di tipo input length vorrÃ  dire che, al rilascio del bottone, l'evento va interrotto
////                    if (evento.getType().equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
////                        doActionUp();
////                    }
////
////                    return true;
////                }
//
//            } else {
//                return false;
//            }
//
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        }
//
//        return false;
//    }
//
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void doActionDown(Event event, double[] coordinates) { //ArrayList<int[]> coordinates
//        Link thisLink = thisConf.getLink(event.getName());
//
//        int[] newCoord = new int[2];
//        if (!event.getPortrait()) {
//            newCoord[0] = (int) ((coordinates[1]) * getScreenDimension()[0]);//1440);
//            newCoord[1] = (int) ((1 - (coordinates[0])) * 720);
//            Log.d(TAG, "new coord x" + newCoord[0] + " y " + getScreenDimension()[1]);//newCoord[1]);
//        } else {
//            newCoord[0] = (int) (coordinates[0] * getScreenDimension()[0]);//720);
//            newCoord[1] = (int) (coordinates[1] * getScreenDimension()[1]);//1440);
//            Log.d(TAG, "new coord x" + newCoord[0] + " y " + newCoord[1]);
//        }
//
//        switch (event.getType()) {
//            case "Tap":
//                Log.d(TAG, "Ho fatto un tap");
//                generaTap(newCoord);
//                //result = true;
//                break;
//            case "Swipe - Up":
//                Log.d(TAG, "Ho fatto uno swipe in su");
//                generaSwipe(newCoord, UP);
//                break;
//            case "Swipe - Down":
//                Log.d(TAG, "Ho fatto uno swipe in giÃ¹");
//                generaSwipe(newCoord, DOWN);
//                break;
//            case "Swipe - Right":
//                Log.d(TAG, "Ho fatto uno swipe a destra");
//                generaSwipe(newCoord, RIGHT);
//                break;
//            case "Swipe - Left":
//                Log.d(TAG, "Ho fatto uno swipe a sinistra");
//                generaSwipe(newCoord, LEFT);
//                break;
//            case "Long Tap - input length":
//                Log.d(TAG, "Ho fatto un long tap con durata gestita dal mio tocco");
//                generaLongTapInterruptible(newCoord);
//                break;
//            case "Long Tap - ON/OFF":
//                Log.d(TAG, "Ho fatto un long tap che puÃ² essere fermato da " + thisLink.getActionStop());
//                generaLongTapInterruptible(newCoord);
//                break;
//            case "Long Tap - timed":
//                Log.d(TAG, "Ho fatto un long tap con durata di " + thisLink.getDuration() + " seconds");
//                generaLongTapTimed(newCoord, thisLink.getDuration()); //generaLongTapTimed
//                break;
//        }
//
//    }
//
//    private void doActionUp() {
//        Log.d(TAG, "sei in action Up");
//        int[] newCoord = new int[2];
//        newCoord[0] = 10000;
//        newCoord[1] = 10000;
//
//        generaTap(newCoord);
//    }
//
//
//    @Override
//    public void onInterrupt() {
//        Log.v(TAG, "onInterrupt");
//    }
//
//    @Override
//    public boolean onUnbind(Intent intent) {
//        Log.d(TAG, "Disconesso");
//        return super.onUnbind(intent);
//    }
//
//    // il metodo permette di generare due tipi di click a seconda del valore che il parametro booleano ha impostato
//    // se false faccio un click della durata di 1 ms sufficiente per il sistema per generare questo tipo di gesture
//    // se true faccio un click della durata di 1 min per simulare un long click
//    private void generaTap(int[] coord) {
//
//        int X = coord[0];
//        int Y = coord[1];
//
//        GestureDescription.Builder gestureB = new GestureDescription.Builder();
//        Path clickPath = new Path();
//        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
//        GestureDescription.StrokeDescription stroke = null;
//        stroke = new GestureDescription.StrokeDescription(clickPath, 0, 1); //duration modificata
//
//        gestureB.addStroke(stroke);
//        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
//            @Override
//            public void onCompleted(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture completata");
//                super.onCompleted(gestureDescription);
//            }
//
//            @Override
//            public void onCancelled(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture non completata");
//                super.onCancelled(gestureDescription);
//            }
//        }, null);
//    }
//
//    private void generaLongTapTimed(int[] coord, double duration) {
//
//        int X = coord[0];
//        int Y = coord[1];
//
//        GestureDescription.Builder gestureB = new GestureDescription.Builder();
//        Path clickPath = new Path();
//        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
//        GestureDescription.StrokeDescription stroke = null;
//        stroke = new GestureDescription.StrokeDescription(clickPath, 0, (int) duration * 1000);
//
//        gestureB.addStroke(stroke);
//        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
//            @Override
//            public void onCompleted(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture completata");
//                super.onCompleted(gestureDescription);
//            }
//
//            @Override
//            public void onCancelled(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture non completata");
//                super.onCancelled(gestureDescription);
//            }
//        }, null);
//    }
//
//    private void generaLongTapInterruptible(int[] coord) {
//        Log.d(TAG, "sei in long tap interruptible");
//        int X = coord[0];
//        int Y = coord[1];
//
//        GestureDescription.Builder gestureB = new GestureDescription.Builder();
//        Path clickPath = new Path();
//        clickPath.moveTo(X, Y);//clickPath.moveTo(coordinate[0], coordinate[1]);
//        interruptibleStroke = new GestureDescription.StrokeDescription(clickPath, 0, (int) 60000);
//
//        gestureB.addStroke(interruptibleStroke);
//        boolean result = this.dispatchGesture(gestureB.build(), new GestureResultCallback() {
//            @Override
//            public void onCompleted(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture completata");
//                super.onCompleted(gestureDescription);
//            }
//
//            @Override
//            public void onCancelled(GestureDescription gestureDescription) {
//                Log.d(TAG, "Gesture non completata");
//                super.onCancelled(gestureDescription);
//            }
//        }, null);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    private void generaSwipe(int[] coord, final String direction) {
//        int startX = coord[0];
//        int startY = coord[1];
//
//        Log.d(TAG, startX + " " + startY);
//
//        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
//        Path path = new Path();
//
//        if (direction.equals(UP)) {
//            Log.d(TAG, "Swipe - Up");
//            path.moveTo(startX, startY);
//            path.lineTo(startX, startY - 100);
//
//
//        } else if (direction.equals(DOWN)) {
//            Log.d(TAG, "Swipe - Down");
//            path.moveTo(startX, startY);
//            path.lineTo(startX, startY + 100);
//
//
//        } else if (direction.equals(RIGHT)) {
//            Log.d(TAG, "Swipe - Right");
//
//            path.moveTo(startX, startY);
//            path.lineTo(startX + 100, startY);
//
//
//        } else if (direction.equals(LEFT)) {
//            Log.d(TAG, "Swipe - Left");
//            path.moveTo(startX, startY);
//            path.lineTo(startX - 100, startY);
//
//        }
//
//
//        final GestureDescription.StrokeDescription strokeDescription = new GestureDescription.StrokeDescription(path, 0, 25, true);
//        gestureBuilder.addStroke(strokeDescription);
//        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
//            @Override
//            public void onCompleted(GestureDescription gestureDescription) {
//                super.onCompleted(gestureDescription);
//            }
//        }, null);
//    }
//
//
//    private class VoiceCommandListener extends AsyncTask<String, Void, Void> {
//        private int bufferSize = 0;
//        private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
//        private static final int RECORDER_SAMPLERATE = 44100;
//        private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
//
//        //campiono la voce a 16 KHz (dipende da come ho campionato il segnale quando lo acquisito)
//        private int sampleRate;
//        //campioni per frame
//        private int sampleForFrame; //(320 -> 20 ms; 512 -> 32 ms)
//        //overlapping dei frame del 50%
//        private int bufferOverlap;
//        //dimensione di ogni campione in termini di bits
//        private int bits;
//        //audio mono-channel
//        private int channel;
//        //numero di features estratte da ogni frame
//        private int melCoefficients;//13; (se non consodero il primo coefficiente)
//        //numero di filtri da applicare per estrarre le features
//        private int melFilterBank;
//        //minima frequenza di interesse
//        private int lowFilter;
//        //massima frequenza di interesse
//        private int highFilter;
//        //range di valori che puÃ² assumere ogni campione (0-255 -> unsigned; -127-+128 -> signed)
//        private boolean signed;
//        //modo in cui vengono memorizzati i bits
//        private boolean big_endian;
//        //dimensione dei vettori
//        private int vectorDim;
//
//        private int label, predLabel, u, noise, counter;
//
//        double wa, wn, pa, pn;
//
//        private String previousAction;
//        private int[] counterPred;
//
//        public VoiceCommandListener() {
//            super();
//            sampleRate = 44100;
//            sampleForFrame = 1024;
//            bufferOverlap = 512;
//            bits = 16;
//            channel = 2;
//            melCoefficients = 21;
//            melFilterBank = 32;
//            lowFilter = 30;
//            highFilter = 3000;
//            signed = true;
//            big_endian = false;
//            vectorDim = 20;
//            label = -1;
//            predLabel = -1;
//            previousAction = "";
//        }
//
//        @Override
//        protected Void doInBackground(String... strings) {
//            final ArrayList<String> predictedActions = new ArrayList<>();
//            final ArrayList<Calendar> timePredictedActions = new ArrayList<>();
//            final ArrayList<String> subRange4Actions = new ArrayList<>();
//            final ArrayList<double[]> probabilityActions = new ArrayList<>();
//            final LinkedList<Integer> slidWind = new LinkedList<>();
//            final LinkedList<double[]> probSlidWind = new LinkedList<>();
//            counter = 0;
//            final int[] noiseCounter = {0};
//            final int[] counter = {0};
//            final String[] eventType = {""};
//            try {
//
//                SVMmodel thisModel = thisConf.getModel();
//                Log.d(TAG, thisModel.getName());
//
//                String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/A-Cube/Models";
//                File fileSVM = new File(path, thisModel.getName());
//
//                final svm svm = new svm();
//                final libsvm.svm_model model = libsvm.svm.svm_load_model(fileSVM.getAbsolutePath());
//
//                final ArrayList<String> svmClasses = new ArrayList<>();
//
//                for (ActionVocal actionVocal : thisModel.getSounds()) {
//                    svmClasses.add(actionVocal.getName());
//                }
//
//                for (String cls : svmClasses)
//                    Log.d("Classe svm -----> ", cls);
//
//                counterPred = new int[svmClasses.size()];
//
//                if (fileSVM.exists()) {
//
//                    //definizione della dimensione del buffer con i parametri definiti inizialmente
//                    bufferSize = AudioRecord.getMinBufferSize(16000, AudioFormat.CHANNEL_IN_MONO, RECORDER_AUDIO_ENCODING);
//                    //oggetto recorder per l'acuisizione dell'audio da microfono
//                    //POSSO USARE MIC QUANDO USO DIRETTAMENTE IL MICROFONO
//                    final AudioRecord recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
//
//                    Log.d("++++++++++++++++++", "INIZIO A REGISTRARE!!!!!!");
//                    recorder.startRecording();
//
//                    //buffer per la lettura dell'audio da microfono
//                    byte data[] = new byte[bufferSize];
//
//                    int read = 0;
//
//                    while (!isCancelled()) {
//                        //acquisizione del file audio
//                        read = recorder.read(data, 0, bufferSize);
//                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
//
//                            InputStream is = new ByteArrayInputStream(data);
//                            final AudioDispatcher dispatcher = new AudioDispatcher(new UniversalAudioInputStream(is, new TarsosDSPAudioFormat(sampleRate, bits, channel, signed, big_endian)), sampleForFrame, bufferOverlap);
//                            final MFCC mfcc = new MFCC(sampleForFrame, sampleRate, melCoefficients, melFilterBank, lowFilter, highFilter);
//                            dispatcher.addAudioProcessor(mfcc);
//                            dispatcher.addAudioProcessor(new AudioProcessor() {
//                                @RequiresApi(api = Build.VERSION_CODES.O)
//                                @Override
//                                public boolean process(AudioEvent audioEvent) {
//                                    timePredictedActions.add(Calendar.getInstance());
//                                    float[] audio_float = new float[21];
//                                    mfcc.process(audioEvent);
//                                    audio_float = mfcc.getMFCC();
//
//                                    float[] temp = new float[vectorDim];
//                                    //rimuovo il primo coefficiente della window perchÃ¨ rappresenta l'RMS (= info sulla potenza della finestra)
//                                    for (int i = 1, k = 0; i < audio_float.length; i++, k++) {// i = 1
//                                        temp[k] = audio_float[i];
//                                    }
//
//                                    float[] normVector = normalize(temp);
//
//                                    svm_node[] node = new svm_node[vectorDim];
//                                    for (int i = 0; i < vectorDim; i++) {
//                                        svm_node nodeT = new svm_node();
//                                        nodeT.index = i;
//                                        nodeT.value = normVector[i];
//                                        node[i] = nodeT;
//                                    }
//
//                                    double[] probability = new double[svmClasses.size()];
//
//                                    predLabel = (int) libsvm.svm.svm_predict_probability(model, node, probability);
//
//                                    Log.d("LABEL PREDETTA ----> ", String.valueOf(predLabel));
//
//
//                                    //Log.d(TAG, "counter: "+counter[0]+" noiseCounter: "+noiseCounter[0]);
//
//                                    //timePredictedActions.add(new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss").format(Calendar.getInstance().getTime()));
//                                    /***********
//                                     BLOCCO DI CODICE UTILE PER LA GENERAZIONE DEI DATI DEL CSV
//                                     USO IL CODICE PER MODIFICARE LA TECNICA DI CORREZIONE DEL CLASSIFICATORE
//                                     ***********/
//                                    String labelClass = svmClasses.get(predLabel);
//                                    predictedActions.add(labelClass);
//                                    probabilityActions.add(probability);
//
//                                    counter[0] = counter[0] + 1;
//
//
//                                    if (!labelClass.equals("Noise")) {
//                                        Log.d(TAG, labelClass+"          "+noiseCounter[0] + " "+ counter[0]);
//                                        noiseCounter[0] += 1;
//                                    }
//
//                                    if (counter[0] == 30) {
//                                        counter[0] = 0;
//                                        noiseCounter[0] = 0;
//                                    }
//
//                                    if (!labelClass.equals("Noise")) {
//                                        Link thisLink = thisConf.getLinkFromAction(labelClass);
//
//                                        if (thisLink == null) {
//                                            Link stopLink = thisConf.getLinkFromActionStop(labelClass);
//
//                                            if (stopLink != null) {
//                                                doActionUp();
//                                            }
//
//                                        } else {
//
//                                            Event event = thisLink.getEvent();
//                                            eventType[0] = event.getType();
//                                            Log.d(TAG, "A:  "+event.getName()+" "+eventType[0]);
//                                        }
//                                    }
//
//                                    if (slidWind.size() == 0) {
//                                        Log.d(TAG, "B:  ");
//
//                                        if (!labelClass.equals("Noise")) {
//                                            Log.d(TAG, "B: 1 ");
//                                            Link thisLink = thisConf.getLinkFromAction(labelClass);
//                                            if (thisLink == null) {
//                                                Link stopLink = thisConf.getLinkFromActionStop(labelClass);
//
//                                                if (stopLink != null) {
//                                                    doActionUp();
//                                                }
//
//                                            } else {
//                                                Log.d(TAG, "B:  2");
//                                                Event event = thisLink.getEvent();
//
//                                                double X = event.getX();
//                                                double Y = event.getY();
//
//                                                double[] coordinate = {X, Y};
//
//                                                probSlidWind.add(probability);
//                                                slidWind.add(predLabel);
//
//                                                pa = probability[predLabel];
//
//                                                if (pa > 0.95) {
//                                                    Log.d(TAG, "B:  3");
//                                                    doActionDown(event, coordinate);
//
//                                                    previousAction = labelClass;
//                                                } else {
//                                                    Log.d(TAG, "B:  4");
//                                                    // else it does nothing,
//                                                    //subRange4Actions.add("Noise");
//                                                    previousAction = "Noise";
//                                                }
//                                                pa = 0;
//                                            }
//
//                                        } else {
//                                            Log.d(TAG, "B:  5");
//
//                                            if (counter[0] == 29 && noiseCounter[0] > 15 && eventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
//                                                doActionUp();
//                                                counter[0] = 0;
//                                                noiseCounter[0] = 0;
//                                            }
//
//
//                                            //subRange4Actions.add("Noise");
//                                            previousAction = "Noise";
//                                        }
//                                    } else {
//                                        Log.d(TAG, "C:  ");
//                                        //blocco if per gestire quando iniziare a considerare la sliding windows
//                                        Log.d(TAG, !labelClass.equals("Noise")+" "+ probability[predLabel]+" "+previousAction.equals("Noise"));
//                                        if (!labelClass.equals("Noise") && probability[predLabel] > 0.95 && previousAction.equals("Noise")) { //!labelClass.equals("Noise") && probability[predLabel] > 0.95 && previousAction.equals("Noise")
//                                            //controllare se tutto il blocco Ã¨ corretto
//                                            Log.d(TAG, "C: 1");
//
//                                            Link thisLink = thisConf.getLinkFromAction(labelClass);
//                                            if (thisLink == null) {
//                                                Link stopLink = thisConf.getLinkFromActionStop(labelClass);
//
//                                                if (stopLink != null) {
//                                                    doActionUp();
//                                                }
//
//                                            } else {
//                                                Log.d(TAG, "C: 2 ");
//                                                Event event = thisLink.getEvent();
//                                                double X = event.getX();
//                                                double Y = event.getY();
//
//                                                double[] coordinate = {X, Y};
//                                                doActionDown(event, coordinate);
//                                            }
//
//                                            probSlidWind.remove();
//                                            slidWind.remove();
//                                            probSlidWind.add(probability);
//                                            slidWind.add(predLabel);
//                                            previousAction = labelClass;
//
//                                        } else {
//
//                                            if (slidWind.size() == 6) {
//                                                probSlidWind.remove();
//                                                slidWind.remove();
//                                                probSlidWind.add(probability);
//                                                slidWind.add(predLabel);
//                                            } else {
//                                                probSlidWind.add(probability);
//                                                slidWind.add(predLabel);
//                                            }
//
//                                            String actionToAct = svmClasses.get(slidWind.get(0));
//
//                                            for (int i = 0; i < slidWind.size(); i++) {
//                                                String predictedAction = svmClasses.get(slidWind.get(i));
//                                                if (!predictedAction.equals("Noise") && predictedAction.equals(actionToAct)) {
//                                                    pa += probSlidWind.get(i)[predLabel];
//                                                } else {
//                                                    pn += probSlidWind.get(i)[predLabel];
//                                                }
//                                            }
//
//                                            double fa = 1 + (0.1 * (slidWind.size() - 1));
//                                            double fn = 0.5 + (0.1 * (slidWind.size() - 1));
//                                            wa = pa / slidWind.size() * fa;
//                                            wn = pn / slidWind.size() * fn;
//
//                                            if (wa > 0.95) {
//                                                if (previousAction.equals("Noise")) {
//                                                    Log.d(TAG, "D:  ");
//
//                                                    Link thisLink = thisConf.getLinkFromAction(labelClass);
//                                                    if (thisLink == null) {
//                                                        Link stopLink = thisConf.getLinkFromActionStop(labelClass);
//
//                                                        if (stopLink != null) {
//                                                            doActionUp();
//                                                        }
//                                                    } else {
//                                                        Log.d(TAG, "D: 1 ");
//                                                        Event event = thisLink.getEvent();
//                                                        double X = event.getX();
//                                                        double Y = event.getY();
//
//                                                        double[] coordinate = {X, Y};
//                                                        doActionDown(event, coordinate);
//                                                    }
//                                                    previousAction = labelClass;
//                                                }
//
//                                            } else if (wn > 0.9 && wa < 0.95) {
//                                                //subRange4Actions.add("Noise");
//                                                if (!previousAction.equals("Noise")) {
//                                                    Log.d(TAG, "D:  2");
//                                                    doActionUp();
//
//
//                                                    Log.d(TAG, "label class " + labelClass);
//                                                    Link thisLink = thisConf.getLinkFromAction(previousAction);
//                                                    if (thisLink == null) {
//                                                        Link stopLink = thisConf.getLinkFromActionStop(labelClass);
//
//                                                        if (stopLink != null) {
//                                                            doActionUp();
//                                                            noiseCounter[0]=0;
//                                                            counter[0]=0;
//                                                        }
//                                                    } else {
//                                                        Log.d(TAG, "D:  3");
////                                                        Event event = thisLink.getEvent();
////                                                        double screenX = getScreenDimension()[0];
////                                                        double screenY = getScreenDimension()[1];
////                                                        double X = event.getX();
////                                                        double Y = event.getY();
////
////                                                        int[] coordinate = {(int) (screenX * X), (int) (screenY * Y)};
//                                                        //doActionDown(event, coordinate);
//
//                                                        //doActioUp(event.getName(), coordinate);
//                                                    }
//                                                    probSlidWind.clear();
//                                                    slidWind.clear();
//                                                    previousAction = "Noise";
//                                                }
//                                            }
//                                            pa = 0;
//                                            pn = 0;
//
//                                            if (counter[0] == 29 && noiseCounter[0] < 7 && eventType[0].equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
//                                                doActionUp();
//                                                counter[0] = 0;
//                                                noiseCounter[0] = 0;
//                                            }
//                                        }
//                                    }
//                                    /**************************/
//
//
//                                    //************************
//                                    //CODICE DA USARE OGGI
//                                    /*subRange4Actions.add(svmClasses.get(predLabel));
//                                    counter++;
//                                    if (counter < 3) {
//                                        counterPred[predLabel] += 1;
//                                    } else if (counter == 3) {
//                                        counterPred[predLabel] += 1;
//
//                                        int max = -1;
//                                        int index = -1;
//                                        int countArray = 0;
//                                        for (int i = 0; i < (counterPred.length - 1); i++) {
//                                            if (i == 0)
//                                                index = i;
//                                            for (int j = i + 1; j < counterPred.length; j++) {
//                                                if (counterPred[i] < counterPred[j]) {
//                                                    index = j;
//                                                    i = index;
//                                                    break;
//                                                }
//                                            }
//                                        }
//
//                                        String action = svmClasses.get(index);
//
//                                        /*for(String sb : subRange4Actions){
//                                            try {
//                                                osw.write(sb + "---------->" + action + "\n");
//                                            } catch (IOException e) {
//                                                e.printStackTrace();
//                                            }
//                                            //Log.d(sb + "---------->",action);
//                                        }*/
//
//
//
//                                        /*counter = 0;
//                                        subRange4Actions.clear();
//                                        for (int i = 0; i < counterPred.length; i++) {
//                                            counterPred[i] = 0;
//                                        }
//
//                                        if (!action.equals(previousAction) && !previousAction.equals("")) {
//                                            //Log.d("**********", Integer.toString(predLabel));
//                                            //Log.d("**********", Integer.toString(label));
//                                            if (!action.equals("Noise")) {
//                                                String descrizione = linksMap.get(action);
//                                                String evento = games.ottieniEvento(descrizione);
//                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
//                                                doActionDown(evento, coordinate);
//                                            } else {
//                                                String descrizione = linksMap.get(previousAction);
//                                                String evento = games.ottieniEvento(descrizione);
//                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
//                                                doActioUp(evento, coordinate);
//                                            }
//                                            previousAction = action;
//                                        } else if (!action.equals(previousAction) && previousAction.equals("")) {
//                                            previousAction = action;
//                                        }
//                                    }*/
//                                    /*******************
//                                     *
//                                     *
//                                     */
//
//
//                                    //eseguo azione
//                                    /*if(label != -1){
//                                        if(predLabel != label) {
//
//                                            if (!svmClasses.get(predLabel).equals("Noise")) {
//                                                String descrizione = linksMap.get(svmClasses.get(predLabel));
//                                                String evento = games.ottieniEvento(descrizione);
//                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
//                                                doActionDown(evento, coordinate);
//                                            } else {
//                                                String descrizione = linksMap.get(svmClasses.get(label));
//                                                String evento = games.ottieniEvento(descrizione);
//                                                ArrayList<int[]> coordinate = eventsMap.get(descrizione);
//                                                doActioUp(evento, coordinate);
//                                            }
//                                            label = predLabel;
//                                        }
//                                    }else{
//                                        label = predLabel;
//                                    }*/
//                                    //return true;
//
//                                    //}
//                                    return true;
//                                }
//
//                                @Override
//                                public void processingFinished() {
//
//                                }
//                            });
//                            dispatcher.run();
//                        }
//                    }
//                    recorder.stop();
//                    recorder.release();
//
//
//                    //SCRIVO IL RISULTATO DELLA PREDIZIONE IN UN FILE TXT
//                    /*File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    File file = new File(directory, "predicted_action_A_E_with_probability.txt");
//                    if(!file.exists()) {
//                        file.createNewFile();
//                    }
//                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
//                    for(int i = 0; i < predictedActions.size(); i++){
//                        double[] probs = probabilityActions.get(i);
//                        Calendar currentTime = timePredictedActions.get(i);
//                        int hours = currentTime.get(Calendar.HOUR_OF_DAY);
//                        int minutes = currentTime.get(Calendar.MINUTE);
//                        int seconds = currentTime.get(Calendar.SECOND);
//                        int milliseconds = currentTime.get(Calendar.MILLISECOND);
//                        //String time = "(" + ((i * 23)/2) + " - " + (((i * 23)/2) + 23) + " ) ";
//                        //String sentence = timePredictedActions.get(i) + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ]\n";
//                        String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ] " + subRange4Actions.get(i) + "\n";
//                        osw.write(sentence);;
//                    }*/
//
//
//                    //SCRIVO IL RISULTATO DELLA PREDIZIONE IN UN FILE CSV
//                    /*File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
//                    File file = new File(directory, "predicted_action_A_with_probability_5.csv");
//                    if(!file.exists()) {
//                        file.createNewFile();
//                    }
//                    OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(file));
//                    osw.write("Time; Prediction; Class A Probability; Class Noise Probability; Prediction with sliding windows\n");
//                    osw.flush();
//
//                    for(int i = 0; i < predictedActions.size(); i++){
//                        double[] probs = probabilityActions.get(i);
//                        Calendar currentTime = timePredictedActions.get(i);
//                        int hours = currentTime.get(Calendar.HOUR_OF_DAY);
//                        int minutes = currentTime.get(Calendar.MINUTE);
//                        int seconds = currentTime.get(Calendar.SECOND);
//                        int milliseconds = currentTime.get(Calendar.MILLISECOND);
//                        //String time = "(" + ((i * 23)/2) + " - " + (((i * 23)/2) + 23) + " ) ";
//                        //String sentence = timePredictedActions.get(i) + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ]\n";
//                        //String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + " " +predictedActions.get(i) + " -----> [ A : " + String.format(Locale.ITALIAN,"%.6f",probs[0]) + " , Noise : " + String.format(Locale.ITALIAN,"%.6f",probs[1]) + " ] " + subRange4Actions.get(i) + "\n";
//                        String sentence = hours + ":" + minutes + ":" + seconds + ":" + milliseconds + ";" +predictedActions.get(i) + ";" + String.format(Locale.ITALIAN,"%.6f",probs[0]) + ";" + String.format(Locale.ITALIAN,"%.6f",probs[1]) + ";" + subRange4Actions.get(i)+"\n";
//                        osw.write(sentence);;
//                    }*/
//
//                    /*int counter = 0, time = 0, start = 0;
//                    int a = 0,noise = 0;
//                    String[] actions = new String[3];
//                    for(String predA: predictedActions){
//                        if(counter < 2){
//                            actions[counter++] = predA;
//                        }else{
//                            actions[counter] = predA;
//
//                            for(String acts : actions){
//                                if(acts.equals("a"))
//                                    a++;
//                                else
//                                    noise++;
//                            }
//                            if(a > noise){
//                                osw.write(actions[0] + " -----> a\n");
//                            }else{
//                                osw.write(actions[0] + " -----> Noise\n");
//                            }
//                            actions[0] = actions[1];
//                            actions[1] = actions[2];
//                            a = 0;
//                            noise = 0;
//
//                        }
//                    }*/
//
//                    //osw.close();
//
//                }
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return null;
//        }
//    }
//
//    private float[] normalize(float[] f) {
//        float[] normArray = new float[f.length];
//
//        float sum = 0;
//
//        for (int i = 0; i < f.length; i++) {
//            sum += Math.pow(f[i], 2);
//        }
//
//        for (int i = 0; i < f.length; i++) {
//            normArray[i] = f[i] / (float) Math.sqrt(sum);
//        }
//
//        return normArray;
//    }
//
//    private double[] getScreenDimension() {
//
//        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
//        int width = displayMetrics.widthPixels;
//        int height = displayMetrics.heightPixels;
//
////        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
////        Display display = window.getDefaultDisplay();
////        int width = display.getWidth(); //1440;
////        int height = display.getHeight();
//
//        Log.d(TAG, width + " detected width");
//        Log.d(TAG, height + " detected height");
//
//        double[] screenInformation = new double[2];
//        screenInformation[0] = width;
//        screenInformation[1] = height;
//        return screenInformation;
//    }
//}
//
