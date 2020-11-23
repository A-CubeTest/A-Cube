/*
NewLink: this activity allows the user to link the event with an action
 */
package com.ewlab.a_cube.tab_games;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.model.Action;
import com.ewlab.a_cube.model.ActionButton;
import com.ewlab.a_cube.model.ActionVocal;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Screen;
import com.thebluealliance.spectrum.SpectrumDialog;

import java.util.ArrayList;

public class NewLink extends AppCompatActivity {

    private static final String TAG = NewLink.class.getName();

    //COORDINATE X,Y UTILIZZATE PER IL MARKER
    double x,y;

    int marker_size = 50;
    int marker_color = -65536;

    boolean OnOff = false;
    boolean timed = false;

    EditText eventName, durationTime;
    Button searchEventType, searchAction, searchActionStop, markerColor;
    ImageButton back_btn;
    FloatingActionButton buttonSave;
    ImageView screenImage_iv, marker_iv;
    SeekBar markerSizeBar;
    TextView errorCoordinate, linkTitle, linkTitle2, tapOnImage_tv;
    RelativeLayout screen_relativeLayout;
    LinearLayout linearLayoutH_Duration, linearLayoutH_ActionStop, linearLayoutH_MarkerColor, linearLayoutH_MarkerSize;

    public static Configuration configuration;
    public static Screen screen;
    Link oldLink;
    Event oldEvent;
    Configuration thisConfig;

    ArrayList<Screen> screenshots = new ArrayList<>();

    boolean portrait;

    String screenName, screenImage, confName, gameName;
    String title, event, screen_img, title2;
    String nameConfig;
    Bitmap screenBitmap;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("LIFE_CYCLE","OnCreate NewLink");
        //setContentView(R.layout.activity_new_link);
        setContentView(R.layout.activity_new_link2);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //NASCONDO BARRA DEL TITOLO
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();

        //PRENDO GLI ELEMENTI DALL'INTENT DELLA SCHERMATA SCREENACTIVITY NEL CASO AddEvent
        screenName = getIntent().getStringExtra("screenName");
        screenImage = getIntent().getStringExtra("screenImage");
        portrait = getIntent().getBooleanExtra("portrait",true);
        gameName = getIntent().getStringExtra("gameName");
        confName = getIntent().getStringExtra("confName");
        event = getIntent().getStringExtra("event");
        //PRENDO I RIFERIMENTI DI TUTTI GLI ELEMENTI GRAFICI CHE MI SERVONO
        back_btn = findViewById(R.id.back_btn);
        linkTitle = findViewById(R.id.linkTitle);
        linkTitle2 = findViewById(R.id.linkTitle2);
        //EDIT TEXT NOME EVENTO
        eventName = findViewById(R.id.eventName);
        //BOTTONE AZIONE
        searchAction = findViewById(R.id.searchAction);
        //BOTTONE TIPO EVENTO
        searchEventType = findViewById(R.id.searchEventType);
        //SEZIONE DURATA EVENTO SE SELEZIONO COME TIPO EVENTO LONG_TAP_TIMED
        linearLayoutH_Duration = findViewById(R.id.LinearLayoutH_Duration);
        linearLayoutH_Duration.setVisibility(View.GONE);
        durationTime = findViewById(R.id.durationTime);
        //SEZIONE AZIONE STOP SE SELEZIONO COME TIPO EVENTO LONG_TAP_ON/OFF
        linearLayoutH_ActionStop = findViewById(R.id.LinearLayoutH_ActionStop);
        linearLayoutH_ActionStop.setVisibility(View.GONE);
        searchActionStop = findViewById(R.id.searchActionStop);
        //SEZIONE RELATIVA L'IMMAGINE IN CUI DEFINIRE L'EVENTO
        screen_relativeLayout = findViewById(R.id.screen_relativeLayout);
        tapOnImage_tv = findViewById(R.id.tapOnImage_tv);
        screenImage_iv = findViewById(R.id.screenImage_iv);
        marker_iv = findViewById(R.id.marker_iv);
        marker_iv.setVisibility(View.GONE);
        errorCoordinate = findViewById(R.id.errorCoordinate);
        //SEZIONE COLORE MARKER
        linearLayoutH_MarkerColor = findViewById(R.id.LinearLayoutH_markerColor);
        markerColor =findViewById(R.id.markerColor);
        //SEZIONE DIMENSIONE MARKER
        linearLayoutH_MarkerSize = findViewById(R.id.LinearLayoutH_markerSize);
        markerSizeBar = findViewById(R.id.markerSizeBar);
        //BOTTONE SALVA LINK
        buttonSave = findViewById(R.id.floatButtonSave);
        //AGGIUNGO I LISTENER AI PULSANTI
        back_btn.setOnClickListener(buttonListener);
        searchAction.setOnClickListener(buttonListener);
        searchEventType.setOnClickListener(buttonListener);
        searchActionStop.setOnClickListener(buttonListener);
        screenImage_iv.setOnClickListener(buttonListener);
        markerColor.setOnClickListener(buttonListener);
        buttonSave.setOnClickListener(buttonListener);

        //SETTO LE VISTE IN BASE AI DATI OTTENUTI DALLA SCHERMATA PRECEDENTE

        //VISTE CHE NON CAMBIANO SIA CHE ARRIVO DA UN LINK ESISTENTE CHE DA UNO NUOVO
        //SOTTO-TITOLO
        title2 = confName.concat(" | ").concat(gameName);
        linkTitle2.setText(title2);
        //IMMAGINE DELLO SCREEN
        screenBitmap = MainModel.getInstance().stringToBitMap(screenImage);
        screenImage_iv.setImageBitmap(screenBitmap);

        //VARIABILI NECESSARIE
        configuration = MainModel.getInstance().getConfiguration(gameName,confName);
        screen = MainModel.getInstance().getScreenFromConf(configuration, screenName);

        //SE oldEvent ESISTE ALLORA STO MODIFICANDO UN LINK PREESISTENTE
        oldLink = screen.getLink(event);
        oldEvent = oldLink.getEvent();

        //SE IL LINK CHE HO SELEZIONATO ESISTE GIà POPOLO I CAMPI CON I SUOI DATI
        if (oldEvent != null) {

            Log.d("NEW_LINK_ACTIVITY", "You are modifying an existing link");

            //TITOLO
            linkTitle.setText(getString(R.string.editLink_title,screenName));
            //NOME EVENTO
            eventName.setHint(oldEvent.getName());
            //ACTION
            setSearchAction(oldLink.getAction());
            //TIPO EVENTO
            setSearchEventTypeButton(oldEvent.getType());
            //SETTO L'IMMAGINE E TV SOPRA DI ESSA
            setScreenImage(screenImage);
            //TODO GESTIRE LANDSCAPE E PORTRAIT
            portrait = oldEvent.getPortrait();
            //SETTO LE VIEW RELATIVE AL MARKER
            setMarkerView(oldLink, oldEvent);
            //ACTION STOP SE EVENTO DI TIPO LONG_TAP_ON/OFF
            if(oldEvent.getType().equals(Event.LONG_TAP_ON_OFF_TYPE))
                setSearchActionStopButton(oldLink.getAction());
            //VIEW SE EVENTO LONG_TAP_TIMED
            if (oldEvent.getType().equals(Event.LONG_TAP_TIMED_TYPE))
                setDurationButton(String.valueOf(oldLink.getDuration()));

        }
        else { //STO CREANDO UN NUOVO LINK

            Log.d("NEW_LINK_ACTIVITY", "Creating new link");

            //TITOLO
            linkTitle.setText(getString(R.string.newLink_title,screenName));
            //NASCONDO SEZIONI MARKER PERCHè STO DEFINENDO UN NUOVO LINK
            linearLayoutH_MarkerColor.setVisibility(View.GONE);
            linearLayoutH_MarkerSize.setVisibility(View.GONE);

        }

        //CONTROLLO SE ARRIVO QUI DOPO AVER SELEZIONATO UN PUNTO IN CUI ESEGUIRE L'EVENTO
        if (getIntent().getStringExtra("x") != null) {

            Log.d("NEW_LINK_ACTIVITY", "Dentro if new link dopo tap su immagine");

            x = Float.parseFloat(getIntent().getStringExtra("x"));
            y = Float.parseFloat(getIntent().getStringExtra("y"));
            screenName = getIntent().getStringExtra("screenName");

            //RISETTO I CAMPI CHE ERANO STATI INSERITI PRIMA DEL TAP SULL'IMMAGINE
            if(getIntent().getStringExtra("provisionalEventName") != null)
                eventName.setText(getIntent().getStringExtra("provisionalEventName"));
            if(getIntent().getStringExtra("provisionalAction") != null)
                setSearchAction(MainModel.getInstance().getAction(getIntent().getStringExtra("provisionalAction")));
            if(getIntent().getStringExtra("provisionalEventType") != null)
                setSearchEventTypeButton(getIntent().getStringExtra("provisionalEventType"));
            if(getIntent().getStringExtra("provisionalActionStop") != null)
                setSearchActionStopButton(MainModel.getInstance().getAction(getIntent().getStringExtra("provisionalActionStop")));
            if(getIntent().getStringExtra("provisionalDurationTime") != null)
                setDurationButton(getIntent().getStringExtra("provisionalDurationTime"));


            if(oldEvent != null) {

                //TITOLO
                linkTitle.setText(getString(R.string.editLink_title,screenName));

            }
            else {

                //TITOLO
                linkTitle.setText(getString(R.string.newLink_title,screenName));

            }

            linearLayoutH_MarkerColor.setVisibility(View.VISIBLE);
            linearLayoutH_MarkerSize.setVisibility(View.VISIBLE);

            screenBitmap = MainModel.getInstance().stringToBitMap(screenImage);

            ViewGroup.LayoutParams lpScreen = screenImage_iv.getLayoutParams();

            marker_iv.setVisibility(View.VISIBLE);
            seekbar();
            //SETTO IL PRIMO COLORE DISPONIBILE PER IL NUOVO MARKER
            marker_color = availableColors()[0];
            Drawable roundDrawable = getResources().getDrawable(R.drawable.button_link_background);
            roundDrawable.setColorFilter(marker_color, PorterDuff.Mode.SRC_ATOP);
            markerColor.setBackground(roundDrawable);
            marker_iv.setImageTintList(ColorStateList.valueOf(marker_color));

            setMarker(marker_size);
            markerSizeBar.setProgress(marker_size);
            lpScreen.width = screenBitmap.getWidth();
            lpScreen.height = screenBitmap.getHeight();

            setBitmapPosition(screenBitmap);

        }

    }

    //this method manages the seekbar operation
    private void seekbar() {
        //SeekBar seekBar = findViewById(R.id.markerSizeBar);
        markerSizeBar.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        setMarker(progress);

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        setMarker(seekBar.getProgress());
                    }
                }
        );
    }

    //This method set the dimensions and the position of the MARKER
    public void setMarker(int progress) {

        marker_iv.requestLayout();
        marker_iv.getLayoutParams().height = progress * 4;
        marker_iv.getLayoutParams().width = progress * 4;
        marker_iv.setScaleType(ImageView.ScaleType.FIT_XY);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        int width, height;

        width = size.x;
        height = size.y;

        if(portrait){

            marker_iv.setX((int) (width * x) - (progress * 2));
            marker_iv.setY((int) (height * y) - (progress * 2));
        }
        else {

            marker_iv.setY((int) (height * x) - (progress * 2));
            marker_iv.setX((int) (width * y) - (progress * 2));
        }

        marker_size = progress;

        Log.d("COORDINATES","WIDTH: "+ width + " | HEIGHT: "+ height);
        Log.d("COORDINATES","x: "+ x + " | y: "+ y);

    }

    public void openColorPicker() {

        final SpectrumDialog.Builder bu = new SpectrumDialog.Builder(this);
        bu.setTitle(getString(R.string.colorPickerTitle));
        //RICHIAMO LA FUNZIONE PER MOSTRARE I COLORI DISPONIBILI
        bu.setColors(availableColors());
        //SE STO MODIFICANDO UN LINK ESISTENTE
        if(markerColor != null)
            bu.setSelectedColor(marker_color);

        bu.setOnColorSelectedListener(new SpectrumDialog.OnColorSelectedListener() {
            @Override
            public void onColorSelected(boolean positiveResult, @ColorInt int color) {

                marker_color = color;
                //CHANGE THE BACKGROUND COLOR OF THE MARKER COLOR BUTTON
                Drawable roundDrawable = getResources().getDrawable(R.drawable.button_link_background);
                roundDrawable.setColorFilter(marker_color, PorterDuff.Mode.SRC_ATOP);
                markerColor.setBackground(roundDrawable);
                marker_iv.setImageTintList(ColorStateList.valueOf(marker_color));
            }
        });

        bu.build().show(getSupportFragmentManager(),"colorPicker");

    }

    //FUNZIONE CHE RITORNA I COLORI CHE NON SONO GIà STATI UTILIZZATI IN ALTRI LINK DELLA STESSA CONFIGURAZIONE
    public int[] availableColors() {

        ArrayList<Integer> colorsList = new ArrayList<>();
        ArrayList<Integer> colorList_temp = new ArrayList<>();

        colorsList.add(getColor(R.color.emerald));
        colorsList.add(getColor(R.color.purple));
        colorsList.add(getColor(R.color.han_purple));
        colorsList.add(getColor(R.color.blue));
        colorsList.add(getColor(R.color.red));
        colorsList.add(getColor(R.color.orange));
        colorsList.add(getColor(R.color.porpora));

        for(Link l : screen.getLinks()) {

            if(screen.getLinks().size() != 0) {

                colorsList.remove(Integer.valueOf(l.getMarkerColor()));

            }

        }

        int[] colors= new int[colorsList.size()];

        for(int i = 0; i < colorsList.size(); i++)
            colors[i] = colorsList.get(i);

        return colors;

    }

    private double[] getScreenDimension() {

        WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        Point point = new Point (1000, 1000);
        display.getRealSize(point);
        int width = point.x;
        int height = point.y;
        Log.d(TAG, width + " detected width");
        Log.d(TAG, height + " detected height");

        double[] screenInformation = new double[2];
        screenInformation[0] = width;
        screenInformation[1] = height;
        return screenInformation;

    }


    public void setBitmapPosition(Bitmap bm) {

        if (bm.getWidth() > bm.getHeight()) {

            Log.d(TAG, "più larga che alta " + bm.getWidth() + " " + bm.getHeight());

            portrait = false;
            Bitmap bOutput;
            float degrees = 90;//rotation degree
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees);
            bOutput = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);

            screenImage_iv.getLayoutParams().width = bm.getHeight();
            screenImage_iv.getLayoutParams().height = bm.getWidth();
            screenImage_iv.setImageBitmap(bOutput);

        }
        else {

            Log.d(TAG, "più alta che larga");
            portrait = true;

            screenImage_iv.getLayoutParams().width = bm.getWidth();
            screenImage_iv.getLayoutParams().height = bm.getHeight();

            screenImage_iv.setImageBitmap(bm);

        }
    }

    /*
    public void setBitmapPosition(Bitmap bm) {

        if (bm.getWidth() > bm.getHeight()) {

            Log.d("POSIZIONE_BITMAP", "più larga che alta " + bm.getWidth() + " " + bm.getHeight());

            screenImage_iv.getLayoutParams().width = screenImage_iv.getLayoutParams().height;
            screenImage_iv.getLayoutParams().height = screenImage_iv.getLayoutParams().width;

        }

    }

     */

    public View.OnClickListener buttonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            switch(v.getId()) {

                case R.id.back_btn:

                    onBackPressed();
                    break;

                case R.id.floatButtonSave:

                    String eventS = eventName.getText().toString();
                    String eventS_hint = eventName.getHint().toString();
                    String eventType = searchEventType.getText().toString();
                    String actionName = searchAction.getText().toString();
                    String actionStopName = "";
                    String dTime = "";
                    double durationT = 0.0;


                    if (searchActionStop.getText() != null) {//spinnerActionStop.getSelectedItem() != null) {
                        actionStopName = String.valueOf(searchActionStop.getText()); //spinnerActionStop.getSelectedItem().toString();
                    }
                    String dtime = durationTime.getText() != null ? durationTime.getText().toString() :
                            durationTime.getHint().toString();
                    Log.d("LINK","DURATION TIME: "+dtime);

                    if(durationTime.getText() != null && ! durationTime.getText().equals(getString(R.string.Link_DurationTime_et))) {
                        if (durationTime.getText().length() > 0)
                            durationT = Double.parseDouble(durationTime.getText().toString());
                    }

                    int problemCounter = 0;

                    //CONTROLLO NOME EVENTO
                    if(eventS.equals("") && ! eventS_hint.equals(getString(R.string.Link_EventName_et)))
                        eventS = eventS_hint;

                    if (eventS.length() == 0) {

                        eventName.setError(getString(R.string.no_name));
                        problemCounter++;
                    }
                    else
                        eventName.setError(null);

                    //CONTROLLO TIPO EVENTO
                    if (eventType.equals(getString(R.string.Link_SearchEventType_btn))) {

                        searchEventType.setFocusable(true);
                        searchEventType.setFocusableInTouchMode(true);
                        searchEventType.requestFocus();
                        searchEventType.setError(getString(R.string.no_event_type));
                        problemCounter++;
                    }
                    else
                        searchEventType.setError(null);

                    //CONTROLLO AZIONE
                    if (actionName.equals(getString(R.string.Link_SearchAction_btn))) {

                        searchAction.setFocusable(true);
                        searchAction.setFocusableInTouchMode(true);
                        searchAction.requestFocus();
                        searchAction.setError(getString(R.string.no_action));
                        problemCounter++;
                    }
                    else
                        searchAction.setError(null);

                    //CONTROLLO AZIONE STOP SE SERVE
                    if(linearLayoutH_ActionStop.getVisibility() == View.VISIBLE) {

                        actionStopName = searchActionStop.getText().toString();
                        if(actionStopName.equals(getString(R.string.Link_SearchActionStop_btn))) {

                            searchActionStop.setFocusable(true);
                            searchActionStop.setFocusableInTouchMode(true);
                            searchActionStop.requestFocus();
                            searchActionStop.setError(getString(R.string.no_stop_action));
                            problemCounter++;
                        }
                        else
                            searchActionStop.setError(null);
                    }

                    //CONTROLLO DURATA SE SERVE
                    if(linearLayoutH_Duration.getVisibility() == View.VISIBLE) {

                        dTime = durationTime.getText().toString();
                        String dTime_hint = durationTime.getHint().toString();

                        if(! dTime_hint.equals(getString(R.string.Link_DurationTime_et)))
                            dTime = dTime_hint;

                        if(dTime.length() == 0 || dTime.equals(getString(R.string.Link_DurationTime_et))) {

                            durationTime.setError(getString(R.string.no_duration));
                            problemCounter++;
                        }
                        else {

                            durationT = Double.parseDouble(dTime);

                            if(durationT <= 0) {

                                durationTime.setError(getString(R.string.no_duration));
                                problemCounter++;
                            }
                            else
                                durationTime.setError(null);

                            Log.d("DURATION","Duration: "+durationT);
                        }
                    }
                    //CONTROLLO SE HO SELEZIONATO UN PUNTO DELLO SCREEN
                    if (x == 0 | y == 0) {

                        errorCoordinate.setVisibility(View.VISIBLE);
                        errorCoordinate.setText(getString(R.string.Link_NoPointSelected));
                        errorCoordinate.setError(getString(R.string.Link_NoPointSelected));

                        problemCounter++;
                    }
                    else {

                        errorCoordinate.setError(null);
                        errorCoordinate.setVisibility(View.GONE);
                    }

                    Log.d(TAG, problemCounter + " problems accurred saving the new link");

                    if (problemCounter == 0) {

                        Log.d(TAG, "x - y " + x + " " + y);

                        Event newEvent = new Event(eventS, eventType, x, y, screenImage, portrait);

                        boolean eventSaved = MainModel.getInstance().saveEvent(gameName, oldEvent, newEvent);

                        if (!eventSaved) {

                            Toast.makeText(getApplicationContext(), R.string.event_exists, Toast.LENGTH_SHORT).show();
                            //TODO ALERTDIALOG PER SPIEGARE CHE ESISTE GIA' QUESTO EVENTO QUINDI NON PUO' ESSERE INSERITO
                        }
                        else
                            MainModel.getInstance().writeGamesJson();

                        Action actionItem = MainModel.getInstance().getAction(actionName);

                        Link newLink = new Link(newEvent, actionItem, marker_color, marker_size);

                        //se l'utente ha scelto modalità on/off e ha definito un'azione-stop la aggiungiamo al link appena salvato
                        if (actionName.length() > 0 && actionStopName.length() > 0) {
                            Action actionStop = MainModel.getInstance().getAction(actionStopName);
                            newLink.setActionStop(actionStop);
                        }
                        //se l'utente ha scelto modalità timed e ha definito una durata la aggiungiamo al link appena salvato
                        if (actionName.length() > 0 && durationT > 0) {
                            newLink.setDuration(durationT);
                            Log.d("saveLink3","duration: "+durationT);
                        }

                        Log.d("saveLink3","oldLink:" + oldLink.toString());
                        //OLDWAY
                        //boolean linkSaved = MainModel.getInstance().saveLink2(gameName, confName, screenName, oldLink, newLink);
                        //NEWwAY
                        boolean linkSaved = MainModel.getInstance().saveLink3(configuration, screenName, oldLink, newLink);

                        if (!linkSaved) {
                            Toast.makeText(getApplicationContext(), R.string.link_exists, Toast.LENGTH_SHORT).show();
                            //TODO ALERTDIALOG AVVISARE L'UTENTE DEL PERCHè NON PUò SALVARE QUESTO LINK
                        }
                        else {

                            MainModel.getInstance().writeConfigurationsJson();

                            Toast.makeText(getApplicationContext(), R.string.link_added, Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(getApplicationContext(), ScreenActivity.class);

                            intent.putExtra("game_name", gameName);
                            intent.putExtra("conf_name", confName);
                            intent.putExtra("portrait", portrait);
                            intent.putExtra("screenName", screenName);
                            intent.putExtra("screenImage", screenImage);
                            intent.putExtra("fromActivity", "NewLink");

                            startActivity(intent);

                        }
                    }
                    else {
                        Log.d("problemCounter","PROBLEM K:"+problemCounter);
                    }

                    break;

                case R.id.searchAction:

                    toFragmentActions();
                    break;

                case R.id.searchActionStop:
                    toFragmentActionsStop();
                    break;

                case R.id.searchEventType:

                    toFragmentEventsType();
                    break;

                case R.id.screenImage_iv:

                    Intent intent = new Intent(getApplicationContext(), ScreenPosition.class);

                    intent.putExtra("gameName", gameName);
                    intent.putExtra("confName", confName);
                    intent.putExtra("screenImage",screenImage);
                    intent.putExtra("screenName",screenName);
                    intent.putExtra("portrait",portrait);
                    intent.putExtra("event", event);

                    //ORA CONTROLLO SE HO GIà SETTATO ALTRI CAMPI E SE COSì FOSSE ME LI PORTO DIETRO PER DOPO
                    String nameEvent = eventName.getText().toString();
                    if (nameEvent.length() > 0) {
                        intent.putExtra("provisionalEventName", nameEvent);
                    }

                    String typeEvent = searchEventType.getText().toString();
                    if (! typeEvent.equals(getString(R.string.Link_SearchEventType_btn))) {
                        intent.putExtra("provisionalEventType", typeEvent);
                    }

                    String actionEvent = searchAction.getText().toString();
                    if (! actionEvent.equals(getString(R.string.Link_SearchAction_btn))) {
                        intent.putExtra("provisionalAction", actionEvent);
                    }

                    String actionStopEvent = searchActionStop.getText().toString();
                    if (! actionStopEvent.equals(getString(R.string.Link_SearchActionStop_btn))) {
                        intent.putExtra("provisionalActionStop", actionStopEvent);
                    }

                    String durationEvent = durationTime.getText().toString();
                    if (durationEvent.length() > 0) {
                        intent.putExtra("provisionalDurationTime", durationEvent);
                    }

                    String markerColor = String.valueOf(marker_color);
                    intent.putExtra("provisionalMarkerColor", markerColor);
                    String markerSize = String.valueOf(marker_size);
                    intent.putExtra("provisionalMarkerSize", markerSize);

                    startActivity(intent);

                    break;

                case R.id.markerColor:

                    openColorPicker();
                    break;

            }
        }
    };

    public void addFragmentActions(Fragment fragment, boolean addToBackStack, String tag) {

        Bundle bundle = new Bundle();
        bundle.putString("fromBtn", "searchAction");
        //Controllo se ho già inserito un'azione nel campo seleziona azioneStop
        if(! searchActionStop.getText().toString().equals(getString(R.string.Link_SearchActionStop_btn)))
            bundle.putString("searchActionStop",searchActionStop.getText().toString());

        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_selectAction, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void toFragmentActions() {
        addFragmentActions(new FragmentSelectAction(), false, "fragmentActions");
    }

    public void closeFragmentActions() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("fragmentActions");
        FragmentTransaction transaction2 = fragmentManager.beginTransaction();
        transaction2.remove(fragment).commit();

    }

    public void addFragmentActionsStop(Fragment fragment, boolean addToBackStack, String tag) {

        Bundle bundle = new Bundle();
        bundle.putString("fromBtn", "searchActionStop");
        //Controllo se ho già inserito un'azione nel campo seleziona azione
        if(! searchAction.getText().toString().equals(getString(R.string.Link_SearchAction_btn)))
            bundle.putString("searchAction",searchAction.getText().toString());

        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_selectAction, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void toFragmentActionsStop() {
        addFragmentActionsStop(new FragmentSelectAction(), false, "fragmentActions");
    }

    public void addFragmentEventsType(Fragment fragment, boolean addToBackStack, String tag) {

        Bundle bundle = new Bundle();
        bundle.putString("screenName", screenName);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_selectAction, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void toFragmentEventsType() {
        addFragmentEventsType(new FragmentSelectEventType(), false, "fragmentEventsType");
    }

    public void closeFragmentEventsType() {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag("fragmentEventsType");
        FragmentTransaction transaction2 = fragmentManager.beginTransaction();
        transaction2.remove(fragment).commit();

    }

/** ----- Metodi per settare le viste se sto modificando un link esistente -------------------------------------*/

    //METODO PER SETTARE CORRETTAMENTE IL BOTTONE PER SELEZIONARE UN'AZIONE
    public void setSearchAction(Action action) {

        if (action != null) {

            searchAction.setText(action.getName());
            //SETTO L'ICONA IN BASE AL TIPO DI AZIONE
            if(action instanceof ActionVocal)
                searchAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_white_24dp,0,0,0);
            else if(action instanceof ActionButton)
                searchAction.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_buttons_white_24dp,0,0,0);

        }

    }

    //METODO PER SETTARE CORRETTAMENTE IL BOTTONE PER SELEZIONARE UN TIPO DI EVENTO
    public void setSearchEventTypeButton(String eventType) {

        searchEventType.setText(eventType);

        switch(eventType) {

            case "Tap":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tap_icon_white_32dp,0,0,0);
                break;

            case "Long Tap - Input length":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_long_tap_input_length_icon_white_32dp,0,0,0);
                break;

            case "Long Tap - ON/OFF":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_long_tap_input_length_icon_white_32dp,0,0,0);
                break;

            case "Long Tap - Timed":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_long_tap_timed_icon_white_32dp,0,0,0);
                break;

            case "Swipe - Left":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_swipe_left_icon_white_32dp,0,0,0);
                break;

            case "Swipe - Up":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_swipe_up_icon_white_32dp,0,0,0);
                break;
            case "Swipe - Down":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_swipe_down_icon_white_32dp,0,0,0);
                break;

            case "Swipe - Right":
                searchEventType.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_swipe_right_icon_white_32dp,0,0,0);
                break;

        }

    }

    //METODO PER SETTARE LE VISTE RELATIVE AL MARKER
    public void setMarkerView(Link link, Event evento) {

        //RENDO VISIBILI VISTE IMPOSTAZIONI MARKER
        linearLayoutH_MarkerColor.setVisibility(View.VISIBLE);
        linearLayoutH_MarkerSize.setVisibility(View.VISIBLE);
        //GRANDEZZA E POSIZIONE MARKER
        markerSizeBar.setProgress(link.getMarkerSize());
        marker_size = link.getMarkerSize();
        x = evento.getX();
        y = evento.getY();
        setMarker(marker_size);
        marker_iv.setVisibility(View.VISIBLE);
        //COLORE MARKER
        marker_color = link.getMarkerColor();
        //CHANGE THE BACKGROUND COLOR OF THE MARKER COLOR BUTTON
        Drawable roundDrawable = getResources().getDrawable(R.drawable.button_link_background);
        roundDrawable.setColorFilter(marker_color, PorterDuff.Mode.SRC_ATOP);
        markerColor.setBackground(roundDrawable);
        marker_iv.setImageTintList(ColorStateList.valueOf(marker_color));

    }

    //METODO PER SETTARE CORRETTAMENTE BOTTONE PER SELEZIONARE UN'AZIONE DI STOP
    public void setSearchActionStopButton(Action actionStop) {

        if (actionStop != null) {

            linearLayoutH_ActionStop.setVisibility(View.VISIBLE);
            searchActionStop.setText(actionStop.getName());
            //METTO L'ICONA IN BASE AL TIPO DI AZIONE
            if(actionStop instanceof ActionVocal)
                searchActionStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mic_white_24dp,0,0,0);
            else if(actionStop instanceof ActionButton)
                searchActionStop.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_buttons_white_24dp,0,0,0);

        }

    }

    //METODO PER SETTARE CORRETTAMENTE BOTTONE DURATA AZIONE SE EVENTO LONG_TAP_TIMED
    public void setDurationButton(String duration) {

        linearLayoutH_Duration.setVisibility(View.VISIBLE);
        durationTime.setHint(duration);

    }

    //METODO PER SETTARE L'IMMAGINE DELLO SCREEN IN CUI DEFINISCO L'EVENTO
    public void setScreenImage(String image) {

        tapOnImage_tv.setText(R.string.Link_TapOnImageToEditMarker);
        screenBitmap = MainModel.getInstance().stringToBitMap(image);
        //SE SCREEN IN LANDSCAPE QUESTA FUNZIONE CAMBIA LA DIMENSIONE DELL'IMAGEVIEW
        setBitmapPosition(screenBitmap);

    }

/**--------------------------------------------------------------------------------------------------------------*/

    @Override
    public void onBackPressed() {

        Context context = getApplicationContext();

        Intent intent = new Intent(context, ScreenActivity.class);
        intent.putExtra("game_name", gameName);
        intent.putExtra("conf_name", confName);
        intent.putExtra("screenName",screenName);
        intent.putExtra("screenImage",screenImage);
        intent.putExtra("fromActivity","NewLink");
        startActivity(intent);

    }


}

