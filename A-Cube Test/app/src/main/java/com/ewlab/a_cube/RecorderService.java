package com.ewlab.a_cube;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.JsonManager;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.SVMmodel;
import com.ewlab.a_cube.model.Screen;


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
    private static final String TAG2 = "RecorderService_Gesture";

    private static final String CHANNEL_ID = "A-Cube_Notification";
    private static final int ONGOING_NOTIFICATION_ID = 30;

    public Configuration thisConf;
    public ArrayList<Configuration> configurations;
    public Game thisGame;

    StringBuilder linksNames;
    String appPackage, appPackageWithNoConfig, appName;
    private PackageManager packageManager = null;
    ApplicationInfo infoApp;

    public Notification notification_service, notification_config;

    public static String UP = "Up";
    public static String DOWN = "Down";
    public static String RIGHT = "Right";
    public static String LEFT = "Left";

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

    //Intercetto qui gli intent provenienti dal NotificationReceiver
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("LIFECYCLE_SERVICE","onStartCommand");
        super.onStartCommand(intent, flags, startId);
        String Permits="";

        if(intent != null && intent.getExtras() != null) {

            if(intent.getBooleanExtra("disable",false)) {

                this.disableSelf();
                stopForeground(true);

            }

            int index_newConf = intent.getIntExtra("nextConf",0);
            boolean from_notification = intent.getBooleanExtra("nextConf2",false);
            String game_title = intent.getStringExtra("nextConf3");
            //Controllo di avere un indice valido per la config e di provenire dal tasto della notifica
            if (index_newConf != -1 && from_notification) {

                configurations = MainModel.getInstance().getConfigurationsFromGame(game_title);
                Configuration nextConf = configurations.get(index_newConf);
                thisConf = nextConf;
                Log.d("NEW_CONF","Setto nuova configurazione come attiva, index: "+index_newConf);
                MainModel.getInstance().setSelectedConfiguration(nextConf);
                JsonManager.writeConfigurations(MainModel.getInstance().getConfigurations());
                notificaActiveConfig();
                customizeToastNextConf();

            }

            if (intent.getExtras().containsKey("Permits")) {

                Permits = intent.getStringExtra("Permits");

                if (Permits.equals("Denied"))
                    this.disableSelf();

            }

        }

        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onServiceConnected() {

        Log.d("LIFECYCLE_SERVICE","onServiceConnected");
        super.onServiceConnected();
        /*
        QUESTE LINEE DI CODICE NON SERVONO PERCHè HO IL FILE CONFIGURAZIONE IN XML GIà SETTATO
        AccessibilityServiceInfo info = getServiceInfo();
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        this.setServiceInfo(info);
        Log.d("SERVICE_INFO", "info: "+info.toString());
         */

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            Log.d("PERMISSIONINREC", "Permission denied");
            Intent intent = new Intent(this, Permissions.class);
            startActivity(intent);

        }
        else {

            Log.d("PERMISSIONINREC", "Permission accomplished");

            createNotificationChannel();
            notificaServiceActive();
            /*
            Intent notificationIntent = new Intent(this, NotificationReceiver.class);
            notificationIntent.putExtra("noti","noti_disable");
            PendingIntent pendingIntent =
                    PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);



            RemoteViews notificationView = new RemoteViews(getPackageName(),R.layout.notification_layout);
            notificationView.setOnClickPendingIntent(R.id.notification_bar_disable_btn, pendingIntent);

            Notification.Action action = new Notification.Action(
                    R.drawable.baseline_cancel_white_24dp,"DISABILITA",pendingIntent
            );

            Notification notification =
                    new Notification.Builder(this, CHANNEL_ID)
                            //.setCustomContentView(notificationView)
                            .setContentTitle(getText(R.string.app_name))
                            .setContentText(getText(R.string.acc_service_description))
                            .setSmallIcon(R.drawable.ic_launcher)
                            .addAction(action)
                            //.setContentIntent(pendingIntent)
                            .build();

            startForeground(ONGOING_NOTIFICATION_ID, notification);

             */

        }

    }

    // metodo per gestire gli eventi che sono intercettati dal servizio
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {

        final int eventType = accessibilityEvent.getEventType();
        Log.d("LIFECYCLE_SERVICE","onAccessibilityEvent evento: "+ accessibilityEvent.getPackageName().toString());

        //Popolo il MainModel con tutti i dati presi dai file .json
        MainModel.getInstance();
        MainModel.getInstance().setActions();
        MainModel.getInstance().setGames();
        MainModel.getInstance().setSvmModels();
        MainModel.getInstance().setConfigurations();

        // memorizzo il nome dell'applicazione
        appPackage = (String) accessibilityEvent.getPackageName();

        Log.d("NOME_APP",""+appPackage);

        /*
        if (appPackage != null) {
            if (appPackage.contains("a_cube")) {
                Log.d(TAG, "disabled");
                this.disableSelf();
            }
        }
        */
        thisGame = MainModel.getInstance().getGameFromBundleId(appPackage);

        List<Configuration> allConfigurations = MainModel.getInstance().getConfigurations();

        if (thisGame != null) {

            thisConf = MainModel.getInstance().getActiveConfigurationFromGame(thisGame.getTitle());

            //CONTROLLO CHE IL GIOCO NON SIA STATO LANCIATO DI RECENTE COSì NON MOSTRO IL TOAST
            if (MainModel.getInstance().lastGame != null && thisGame != null
                    && thisGame.getTitle().equals(MainModel.getInstance().lastGame.getTitle())) {

                thisConf = MainModel.getInstance().getActiveConfigurationFromGame(thisGame.getTitle());
                StringBuilder azioni = new StringBuilder();
                StringBuilder eventi = new StringBuilder();
                //OLD WAY
                //ArrayList<Link> links = thisConf.getLinks();
                //NEW WAY
                ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(thisConf);
                ArrayList<Link> links = new ArrayList<>();
                for(Screen s : screens) {
                    links.addAll(s.getLinks());
                }

                linksNames = new StringBuilder("");

                for (int i = 0; i < links.size(); i++) {

                    String azione = links.get(i).getAction().getName();
                    String evento = links.get(i).getEvent().getName();

                    if (i == links.size() - 1) {

                        azioni.append("- ").append(azione);
                        eventi.append("- ").append(evento);
                        linksNames.append("Azione: ").append(azione).append(" | ")
                                .append("Evento: ").append(evento);

                    } else {

                        azioni.append("- ").append(azione).append(",").append("\n");
                        eventi.append("- ").append(evento).append(",").append("\n");
                        linksNames.append("Azione: ").append(azione).append(" | ")
                                .append("Evento: ").append(evento).append("\n");

                    }
                }

                notificaActiveConfig();

            }
            else {

                thisConf = MainModel.getInstance().getActiveConfigurationFromGame(thisGame.getTitle());

                for (Configuration conf : allConfigurations) {

                    if (conf.getGame().equals(thisGame) && conf.getSelected()) {

                        StringBuilder azioni = new StringBuilder();
                        StringBuilder eventi = new StringBuilder();

                        thisConf = conf;
                        //OLD WAY
                        //ArrayList<Link> links = conf.getLinks();
                        //NEW WAY
                        ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(thisConf);
                        ArrayList<Link> links = new ArrayList<>();
                        for(Screen s : screens) {
                            links.addAll(s.getLinks());
                        }

                        //se non è mai stato usato viene inizializzato
                        if (links.size() >= 1) {

                            linksNames = new StringBuilder("");

                            //for(Link l : links){}
                            for (int i = 0; i < links.size(); i++) {

                                String azione = links.get(i).getAction().getName();
                                String evento = links.get(i).getEvent().getName();

                                if(i == links.size()-1) {

                                    azioni.append("- ").append(azione);
                                    eventi.append("- ").append(evento);
                                    linksNames.append("Azione: ").append(azione).append(" | ")
                                            .append("Evento: ").append(evento);

                                }
                                else {

                                    azioni.append("- ").append(azione).append(",").append("\n");
                                    eventi.append("- ").append(evento).append(",").append("\n");
                                    linksNames.append("Azione: ").append(azione).append(" | ")
                                            .append("Evento: ").append(evento).append("\n");

                                }
                            }

                        }

                        //Toast per mostrare dettagli configurazione per il gioco aperto
                        customizeToast(azioni, eventi);
                        //Notifica con dettagli config attiva per il gioco aperto
                        notificaActiveConfig();

                        MainModel.getInstance().lastGame = thisGame;
                    }
                }

            }

        }
        //se non trovo un gioco valido resetto linksNames
        else {
            linksNames = new StringBuilder();
            //LANCIO NOTIFICA PER APP NON CONFIGURATE O SEMPLICEMENTE PER SEGNALARE CHE A-CUBE ATTIVO
            if (appPackage != null) {
                if (! (appPackage.contains("settings") || appPackage.contains("systemui")
                        || appPackage.contains("a_cube") || appPackage.contains("gametools")))
                    notificaActiveConfig();
                if(appPackage.contains("launcher"))
                    notificaServiceActive();
            }
        }

        //check if asynctask for recognition voice is actived
        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && vcl != null) {
            Log.d("RECORDER_SERVICE","Check asyntask recognition voice active");
            vcl.cancel(true);
            vcl = null;

        }
        else {
            Log.d("RECORDER_SERVICE","vcl not active");
        }

        Log.d("RECORDER_SERVICE","GameFromBundleId: "+ MainModel.getInstance().getGameFromBundleId(appPackage));

        if(MainModel.getInstance().getGameFromBundleId(appPackage) != null)
            Log.d("RECORDER_SERVICE","GameFromBundleId: "+ MainModel.getInstance().getGameFromBundleId(appPackage).getTitle());


        if (eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                MainModel.getInstance().getGameFromBundleId(appPackage) != null) {

            Log.v(TAG, String.format(
                    "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s [text] %s",
                    AccessibilityEvent.eventTypeToString(eventType), accessibilityEvent.getClassName(), accessibilityEvent.getPackageName(),
                    accessibilityEvent.getEventTime(), thisGame.getTitle()));

            List<ActionButton> devices = thisConf.getButtonActions();
            //String device = configurations.getLinkedDevice(app);
            if (devices.size() > 0)
                Log.d("+++++++++ PRIMO DISPOSITIVO DELLA CONFUNO", devices.get(0).getName());

            //controllo se esiste il file games.json e quanti giochi ha al suo interno
            List<Game> games = MainModel.getInstance().getGames();
            if (games.size() > 0)
                Log.d("DIMENSIONE LIST DI GAMES -----> ", "" + games.size());

            //controllo se esiste il file actions.json e quante azioni ha al suo interno
            List<Action> actions = MainModel.getInstance().getActions();
            if (actions.size() > 0)
                Log.d("DIMENSIONE LIST DI ACTIONS -----> ", "" + actions.size());

            //controllo se esiste il file models.json e quanti modelli ha al suo interno
            List<SVMmodel> svMmodels = MainModel.getInstance().getSvmModels();
            if (svMmodels.size() > 0)
                Log.d("DIMENSIONE LIST DI MODELS -----> ", "" + svMmodels.size());

            //controllo se esiste il file configurations.json e quante configurazioni e link ha al suo interno
            List<Configuration> configurations = MainModel.getInstance().getConfigurations();

            if (configurations.size() > 0) {

                Log.d("DIMENSIONE LIST DI CONFIGURATIONS -----> ", "" + configurations.size());

                for (Configuration conf : configurations) {

                    List<Link> links = conf.getLinks();
                    Log.d("DIMENSIONE LIST DI LINK IN " + conf.getGame().getTitle() + " -----> ", "" + thisConf.getLinks().size());

                }
            }
            else {

                Log.d("DIMENSIONE LIST DI CONFIGURATIONS -----> ", "Non entro in if");
            }


            Log.d("RECORDER_SERVICE", "thisConf.getVocalActions(): "+ thisConf.getVocalActions().size());
            Log.d("RECORDER_SERVICE", "thisConf.getModel: "+ thisConf.getModel());
            Log.d("RECORDER_SERVICE", "MainModel...getActionsVoclsFromConf: "+
                    MainModel.getInstance().getActionsVocalFromConf(thisConf).size());

            //controlla se ci sono azioni vocali e se la configurazione ha un modello
            //OLDWAY
            //if (thisConf.getVocalActions().size() > 0 && thisConf.getModel() != null) {
            //NEWWAY
            if (MainModel.getInstance().getActionsVocalFromConf(thisConf).size() > 0 && thisConf.getModel() != null) {

                Log.d("RECORDER_SERVICE", "vocal action founded");
                String[] paramString = new String[]{thisConf.getGame().getTitle()};
                vcl = new VoiceCommandListener();
                vcl.execute(paramString);

            }
        }
        else {
            Log.d("RECORDER_SERVICE", "Non entro in if");
        }
    }

    @Override
    public void onInterrupt() {
        Log.v("LIFECYCLE_SERVICE", "onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("LIFECYCLE_SERVICE", "onUnbind: Disconnesso");
        Log.d("LIFECYCLE_SERVICE", "onUnbind: thisConf: "+thisConf);
        stopForeground(true);
        return super.onUnbind(intent);
    }

    //metodo override per gestire le interazioni con il dispositivo
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        Log.d("LIFECYCLE_SERVICE","onKeyEvent");

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

            // ricavo dal codice dell'evento l'azione dell'utente, sia alla pressione del bottone (azioneDown),
            // che al rilascio dello stesso (azioneUp)
            List<ActionButton> actionButtons = MainModel.getInstance().getButtonActions();
            for (ActionButton actionButton : actionButtons) {

                if (actionButton.getKeyId().equals(String.valueOf(keyCode)) && keyAction == 0) {
                    azioneDown = actionButton.getName();
                }
                else if (actionButton.getKeyId().equals(String.valueOf(keyCode))) {
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

                }
                else if (thisLinkStop != null) {

                    Log.d(TAG, "evento di tipo On/Off interrotto");

                    Event evento = thisLinkStop.getEvent();

                    //se l'evento associato a questa azione è di tipo input length vorrà  dire che, al rilascio del bottone, l'evento va interrotto
                    doActionUp();

                    return true;
                }
                return true;

                //al rilascio di un tasto l'utente genera una azioneUp

            }
            else if (azioneUp.length() > 0 & !lastEventType.equals(Event.LONG_TAP_TIMED_TYPE) & !lastEventType.equals(Event.LONG_TAP_ON_OFF_TYPE)) {

                Log.d(TAG, "actionUp reset");
                doActionUp();

                if (thisConf.getLinkFromAction(azioneUp) != null) {

                    Link thisLink = thisConf.getLinkFromAction(azioneUp);
                    Event evento = thisLink.getEvent();

                    //se l'evento associato a questa azione è di tipo input length vorrà  dire che, al rilascio del bottone, l'evento va interrotto
                    if (evento.getType().equals(Event.LONG_TAP_INPUT_LENGHT_TYPE)) {
                        doActionUp();
                    }

                    return true;
                }

            }
            else {
                return false;
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        Log.d(TAG,"getVocalActions size: "+thisConf.getVocalActions().size());

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

        }
        else {

            newCoord[0] = (int) (coordinates[0] * getScreenDimension()[0]);//720);
            newCoord[1] = (int) (coordinates[1] * getScreenDimension()[1]);//1440);
            Log.d(TAG, "new coord x" + newCoord[0] + " y " + newCoord[1]);

        }

        switch (event.getType()) {

            case "Tap":
                Log.d(TAG2, "Ho fatto un tap");
                generaTap(newCoord);
                //result = true;
                break;

            case "Swipe - Up":
                Log.d(TAG2, "Ho fatto uno swipe in su");
                generaSwipe(newCoord, UP);
//                doActionUp();
                break;

            case "Swipe - Down":
                Log.d(TAG2, "Ho fatto uno swipe in giù");
                generaSwipe(newCoord, DOWN);
                doActionUp();
                break;

            case "Swipe - Right":
                Log.d(TAG2, "Ho fatto uno swipe a destra");
                generaSwipe(newCoord, RIGHT);
                doActionUp();
                break;

            case "Swipe - Left":
                Log.d(TAG2, "Ho fatto uno swipe a sinistra");
                generaSwipe(newCoord, LEFT);
                doActionUp();
                break;

            case "Long Tap - input length":
                Log.d(TAG2, "Ho fatto un long tap con durata gestita dal mio tocco");
                generaLongTapInterruptible(newCoord);
                break;

            case "Long Tap - ON/OFF":
                Log.d(TAG2, "Ho fatto un long tap che può essere fermato da " + thisLink.getActionStop());
                generaLongTapInterruptible(newCoord);
                break;

            case "Long Tap - timed":
                Log.d(TAG2, "Ho fatto un long tap con durata di " + thisLink.getDuration() + " seconds");
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
                Log.d(TAG2, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG2, "Gesture non completata");
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
                Log.d(TAG2, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG2, "Gesture non completata");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    private void generaLongTapInterruptible(int[] coord) {
        Log.d(TAG2, "sei in long tap interruptible");
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
                Log.d(TAG2, "Gesture completata");
                super.onCompleted(gestureDescription);
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                Log.d(TAG2, "Gesture non completata");
                super.onCancelled(gestureDescription);
            }
        }, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void generaSwipe(int[] coord, final String direction) {

        int startX = coord[0];
        int startY = coord[1];

        Log.d(TAG2, startX + " " + startY);

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        if (direction.equals(UP)) {
            Log.d(TAG2, "Swipe - Up");
            path.moveTo(startX, startY);
            path.lineTo(startX, startY - 300);


        } else if (direction.equals(DOWN)) {
            Log.d(TAG2, "Swipe - Down");
            path.moveTo(startX, startY);
            path.lineTo(startX, startY + 300);


        } else if (direction.equals(RIGHT)) {
            Log.d(TAG2, "Swipe - Right");

            path.moveTo(startX, startY);
            path.lineTo(startX + 300, startY);


        } else if (direction.equals(LEFT)) {
            Log.d(TAG2, "Swipe - Left");
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
                                        //OLDWAY
                                        //Link thisLink = thisConf.getLinkFromAction(labelClass);
                                        //NEWWAY
                                        Link thisLink = MainModel.getInstance().getLinkFromAction(thisConf,labelClass);

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

    public void customizeToast(StringBuilder azioni, StringBuilder eventi) {

        //Variabili per il Toast Customizzato
        View view = View.inflate(this, R.layout.toast_layout, null);
        TextView tv_configuration = view.findViewById(R.id.toast_configuration);
        TextView tv_actions = view.findViewById(R.id.toast_actions2);
        TextView tv_events = view.findViewById(R.id.toast_events2);
        tv_configuration.setText(thisConf.getConfName());
        tv_actions.setText(azioni);
        tv_events.setText(eventi);
        //SETTO L'IMMAGINE DEL GIOCO
        appPackageWithNoConfig = appPackage;
        packageManager = getPackageManager();
        try {

            infoApp = packageManager.getApplicationInfo(appPackage, 0);
            appName = (String)packageManager.getApplicationLabel(infoApp);

        } catch (PackageManager.NameNotFoundException e) {

            infoApp = null;
            e.printStackTrace();

        }
        ImageView icon_image = view.findViewById(R.id.toast_image);
        icon_image.setImageDrawable(infoApp.loadIcon(packageManager));

        //Mostro il Toast customizzato
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();

    }

    public void customizeToastNextConf() {

        View view = View.inflate(this, R.layout.toast_layout_next_conf, null);
        TextView tv_nextConf = view.findViewById(R.id.toast_nextConf_text);
        tv_nextConf.setText(thisConf.getConfName());

        //Mostro il Toast customizzato
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public  void notificaServiceActive() {

        Intent notificationIntent = new Intent(this, NotificationReceiver.class);
        notificationIntent.putExtra("noti","noti_disable");
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action action = new Notification.Action(
                R.drawable.baseline_cancel_white_24dp,"DISABILITA",pendingIntent
        );

        notification_service =
                    new Notification.Builder(this, CHANNEL_ID)
                            //.setCustomContentView(notificationView)
                            .setContentTitle(getText(R.string.noti_service_active_title))
                            .setContentText(getText(R.string.noti_service_active_text))
                            .setSmallIcon(R.drawable.ic_launcher)
                            .addAction(action)
                            .setColor(getResources().getColor(R.color.han_purple))
                            //.setContentIntent(pendingIntent)
                            .build();

        /*
        RemoteViews notificationView = new RemoteViews(getPackageName(),R.layout.notification_layout);
        notificationView.setOnClickPendingIntent(R.id.notification_bar_disable_btn, pendingIntent);
        notificationView.setTextViewText(R.id.notification_bar_tv1, getText(R.string.noti_service_active_title));
        notificationView.setTextViewText(R.id.notification_bar_tv2, getText(R.string.noti_service_active_text));
        notificationView.setTextViewText(R.id.notification_bar_disable_btn, "Disabilita");

        notification_service =
                new Notification.Builder(this, CHANNEL_ID)
                        .setCustomContentView(notificationView)
                        //.setContentTitle(getText(R.string.noti_service_active_title))
                        //.setContentText(getText(R.string.noti_service_active_text))
                        .setSmallIcon(R.drawable.ic_launcher)
                        //.addAction(action)
                        //.setColor(getResources().getColor(R.color.han_purple))
                        //.setContentIntent(pendingIntent)
                        .build();
        */

        startForeground(ONGOING_NOTIFICATION_ID, notification_service);

    }

    public void notificaActiveConfig() {

        String game_title = thisGame != null ? thisGame.getTitle() : "";
        String conf_name = thisConf != null ? thisConf.getConfName() : "";
        Game confGame = thisConf != null ? thisConf.getGame() : new Game();

        //Intent con variabili necessarie per action "DISABILITA"
        Intent notificationIntent_disable = new Intent(this, NotificationReceiver.class);
        notificationIntent_disable.putExtra("noti","noti_disable");
        PendingIntent pendingIntent_disable =
                PendingIntent.getBroadcast(this, 0, notificationIntent_disable, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action action_disable = new Notification.Action(
                R.drawable.baseline_check_white_24dp,"Disabilita",pendingIntent_disable);

        //Intent con variabili necessarie per action "CONF SUCCESSIVA"
        Intent notificationIntent_nextConf = new Intent(this, NotificationReceiver.class);
        notificationIntent_nextConf.putExtra("noti","noti_nextConf");

        //Intent con variabili necessarie per action "MODIFICA"
        Intent notificationIntent_modify = new Intent(this, NotificationReceiver.class);
        notificationIntent_modify.putExtra("noti","noti_modify");
        notificationIntent_modify.putExtra("title_noti",game_title);
        notificationIntent_modify.putExtra("confName_noti",conf_name);
        PendingIntent pendingIntent_modify =
                PendingIntent.getBroadcast(this, 2, notificationIntent_modify, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action action_modify = new Notification.Action(
                R.drawable.baseline_check_white_24dp,"Modifica",pendingIntent_modify);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            if(confGame == thisGame) {

                if(MainModel.getInstance().getConfigurationsFromGame(game_title).size() > 1) {

                    Log.d("GAME_CONFIG","Questo gioco ha più di una configurazione, size: "+MainModel.getInstance().getConfigurationsFromGame(thisGame.getTitle()).size());
                    configurations = MainModel.getInstance().getConfigurationsFromGame(thisGame.getTitle());
                    //PRENDO LA POSIZIONE DELLA CONFIGURAZIONE SUCCESSIVA E ME LA PORTO DIETRO
                    int index = 0;
                    for(Configuration c : configurations)
                        if(c == thisConf)
                            index = configurations.indexOf(c)+1;
                    if (index >= MainModel.getInstance().getConfigurationsFromGame(game_title).size())
                        index = 0;

                    notificationIntent_nextConf.putExtra("nextConf",index);
                    notificationIntent_nextConf.putExtra("game_title",thisGame.getTitle());
                    PendingIntent pendingIntent_nextConf =
                            PendingIntent.getBroadcast(this, 1, notificationIntent_nextConf, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Action action_nextConf = new Notification.Action(
                            R.drawable.baseline_check_white_24dp,"Conf Successiva",pendingIntent_nextConf);

                    String title = thisConf.getConfName();

                    notification_config =
                            new Notification.Builder(this, CHANNEL_ID)
                                    //.setCustomContentView(notificationView)
                                    .setContentTitle(title)
                                    .setContentText(linksNames)
                                    .setStyle(new Notification.BigTextStyle().bigText(linksNames))
                                    .addAction(action_nextConf)
                                    .addAction(action_modify)
                                    .addAction(action_disable)
                                    .setColor(getResources().getColor(R.color.han_purple))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .build();

                }
                else {

                    Log.d("GAME_CONFIG","Questo gioco ha solo una configurazione");

                    notification_config =
                            new Notification.Builder(this, CHANNEL_ID)
                                    //.setCustomContentView(notificationView)
                                    .setContentTitle(conf_name)
                                    .setContentText(linksNames)
                                    .setStyle(new Notification.BigTextStyle().bigText(linksNames))
                                    .addAction(action_modify)
                                    .addAction(action_disable)
                                    .setColor(getResources().getColor(R.color.han_purple))
                                    .setSmallIcon(R.drawable.ic_launcher)
                                    .build();

                }

            }
            else {

                String icon = "";
                appPackageWithNoConfig = appPackage;
                Log.d("NOME_APP_SENZA_CONFIG",""+appPackageWithNoConfig);
                packageManager = getPackageManager();

                try {

                    infoApp = packageManager.getApplicationInfo(appPackageWithNoConfig, 0);
                    appName = (String)packageManager.getApplicationLabel(infoApp);
                    Drawable appIcon = infoApp.loadIcon(packageManager);
                    icon = MainModel.getInstance().getBitmapFromDrawable(appIcon);

                } catch (PackageManager.NameNotFoundException e) {

                    infoApp = null;
                    e.printStackTrace();

                }

                //Intent con variabili necessarie per aggiungere una configurazione al gioco che non ne ha
                Intent notificationIntent_addConfig = new Intent(this, NotificationReceiver.class);
                notificationIntent_addConfig.putExtra("noti","noti_addConfig");
                notificationIntent_addConfig.putExtra("packageName", appPackageWithNoConfig);
                notificationIntent_addConfig.putExtra("appName",appName);
                notificationIntent_addConfig.putExtra("icon",icon);

                PendingIntent pendingIntent_addConfig =
                        PendingIntent.getBroadcast(this, 2, notificationIntent_addConfig, PendingIntent.FLAG_UPDATE_CURRENT);

                notification_config =
                        new Notification.Builder(this, CHANNEL_ID)
                                //.setCustomContentView(notificationView)
                                .setContentTitle(getText(R.string.notification_no_config_title)+" "+appName)
                                .setContentText(getText(R.string.notification_no_config_text))
                                .setStyle(new Notification.BigTextStyle().bigText(getText(R.string.notification_no_config_text)))
                                .addAction(action_disable)
                                .setContentIntent(pendingIntent_addConfig)
                                .setColor(getResources().getColor(R.color.han_purple))
                                .setSmallIcon(R.drawable.ic_launcher)
                                .build();

            }

            startForeground(ONGOING_NOTIFICATION_ID, notification_config);

        }

    }

}
