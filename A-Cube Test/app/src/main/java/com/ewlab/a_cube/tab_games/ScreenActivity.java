package com.ewlab.a_cube.tab_games;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ScreenActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;

    public static String game_name, conf_name, screenName, img, starting_class;
    public static int screen_index;
    public static boolean portrait;

    public static LinkAdapter linkAdapter;

    RecyclerView recyclerView_link;
    ImageButton back_btn, info_btn;
    ImageView imageScreen;
    TextView screenTitle, screenTitle2, tapOnImageTv, eventTv, eventListTv2;
    Button newEvent, editScreen, deleteScreen;
    ConstraintLayout cardView2;

    ArrayList<Screen> screens = new ArrayList<>();
    ArrayList<Link> links = new ArrayList<>();
    ArrayList<Event> events = new ArrayList<>();
    Configuration configuration;
    Screen screen;
    Game game;
    Bitmap bitmap;
    String title1, fromActivity;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("LIFE_CYCLE","OnCreate ScreenActivity");
        //NASCONDO BARRA DEL TITOLO
        if (this.getSupportActionBar() != null)
            getSupportActionBar().hide();
        //DI DEFAULT SETTO IL LAYOUT PER UNO SCREEN IN PORTRAIT
        setContentView(R.layout.activity_screen);
        //VARIABILI PER AVERE UN RIFERIMENTO AL GIOCO E ALLA CONFIGURAZIONE
        game_name = getIntent().getStringExtra("game_name");
        conf_name = getIntent().getStringExtra("conf_name");
        //VARIABILE PER CAPIRE DA DOVE ARRIVO
        fromActivity = getIntent().getStringExtra("fromActivity");
        //PRENDO I RIFERIMENTI AGLI ELEMENTI GRAFICI DELLA SCHERMATA
        getViewsReferences();
        //PRENDO IL RIFERIMENTO ALLA CONFIGURAZIONE E GLI SCREEN CHE NE FANNO PARTE
        configuration = MainModel.getInstance().getConfiguration(game_name, conf_name);
        screens = MainModel.getInstance().getScreensFromConf(configuration);

        //SWITCH CHE CONTROLLA DA DOVE ARRIVO, in base a questa informazione faccio determinate cose
        switch(fromActivity) {

            case "ConfDetail_newScreen":

                //AGGIORNO LA VARIABILE DA STAMPARE POI NEL TITOLO DI QUESTA ACTIVITY
                int screens_size = getIntent().getIntExtra("screens_size",0);
                screen_index = screens_size;
                title1 = String.valueOf(screens_size + 1);
                screenName = this.getString(R.string.screenActivityTitle,title1);
                //FUNZIONE CHE SETTA LE VISTE NEL CASO IN CUI VOGLIO INSERIRE UN NUOVO SCREEN
                changeViewsForNewScreen();

                break;

            case "Gallery":

                img = getIntent().getStringExtra("img");
                screenName = getIntent().getStringExtra("screenName");
                portrait = getIntent().getBooleanExtra("portrait",true);

                //ORA FACCIO PARTIRE IL FRAGMENT DI RIEPILOGO DELLO SCREEN SELEZIONATO DALLA GALLERIA
                toFragmentEditScreen();

                break;

            case "ConfDetail_existingScreen":

                screenName = getIntent().getStringExtra("screenName");
                portrait = getIntent().getBooleanExtra("portrait",true);
                //PRENDO IL RIFERIMENTO A QUESTO SCREEN, AL GIOCO, AGLI EVENTI DEL GIOCO E AI LINK DELLA SCHERMATA
                screen = MainModel.getInstance().getScreenFromConf(configuration,screenName);
                game = configuration.getGame();
                events = game.getEvents();
                //SETTO LA VARIABILE img CON IL VALORE DELL'IMMAGINE DELLO SCREEN
                img = screen.getImage();
                //SETTO LA BITMAP IN BASE ALL'IMMAGINE DELLO SCREEN
                bitmap = MainModel.getInstance().stringToBitMap(img);
                //SE LO SCREEN E' IN PORTRAIT SETTO LE VISTE IN UN DETERMINATO MODO, ALTRIMENTI IN UN ALTRO
                if(portrait)
                    changeViewsForExistingPortraitScreen();
                else
                    changeViewsForExistingLandscapeScreen();

                break;

            case "NewLink":

                screenName = getIntent().getStringExtra("screenName");
                img = getIntent().getStringExtra("screenImage");
                game = configuration.getGame();
                events = game.getEvents();
                screen = MainModel.getInstance().getScreenFromConf(configuration,screenName);
                //SETTO LA BITMAP IN BASE ALL'IMMAGINE DELLO SCREEN
                bitmap = MainModel.getInstance().stringToBitMap(img);

                //SETTO IL LAYOUT
                if(portrait)
                    changeViewsForExistingPortraitScreen();
                else
                    changeViewsForExistingLandscapeScreen();

                break;

        }

        //SETTO IL TITOLO CORRETTAMENTE
        screenTitle.setText(screenName);
        //AGGIUNGO I LISTENER AI PULSANTI
        info_btn.setOnClickListener(buttonListener);
        back_btn.setOnClickListener(buttonListener);
        newEvent.setOnClickListener(buttonListener);

    }

    public View.OnClickListener buttonListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch(v.getId()) {

                case R.id.back_btn:

                    onBackPressed();
                    break;

                case R.id.info_btn:

                    //MOSTRO ALERTDIALOG INFO
                    alertDialogInfoBtn();

                    break;

                case R.id.newEvent:

                    Intent newLink = new Intent(getApplicationContext(), NewLink.class);

                    newLink.putExtra("screenName",screenName);
                    newLink.putExtra("screenImage",img);
                    newLink.putExtra("portrait",portrait);
                    newLink.putExtra("confName",conf_name);
                    newLink.putExtra("gameName",game_name);
                    newLink.putExtra("existingLink",false);

                    startActivity(newLink);

                    break;

                case R.id.imageScreen:

                    //FACCIO SELEZIONARE UN'IMMAGINE DALLA GALLERIA
                    final Intent intentImg = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intentImg, RESULT_LOAD_IMAGE);

                    break;

                case R.id.editScreen:

                    //VADO AL FRAGMENT DI MODIFICA DELLO SCREEN
                    toFragmentEditScreen();

                    break;

                case R.id.deleteScreen:

                    //MOSTRO ALERT DIALOG PRIMA DI ELIMINARE LO SCREEN
                    alertDialogDeleteScreen();

                    break;

            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        Bitmap myBitmap;
        int orientation = -1;
        Matrix matrix = new Matrix();

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String a = String.valueOf(selectedImage);

            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            try {
                orientation = new ExifInterface(new File(cursor.getString(cursor.getColumnIndex(filePathColumn[0]))).getAbsolutePath())
                        .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

                switch (orientation) {

                    case ExifInterface.ORIENTATION_ROTATE_90:

                        matrix.postRotate(90);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_180:

                        matrix.postRotate(180);
                        break;

                    case ExifInterface.ORIENTATION_ROTATE_270:

                        matrix.postRotate(270);
                        break;

                    default:
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            String filePath = cursor.getString(columnIndex);
            myBitmap = BitmapFactory.decodeFile(filePath);
            cursor.close();

            if (myBitmap != null) {

                final String img = MainModel.getInstance().bitmapToString(myBitmap);
                Log.d("BITMAP_TO_STRING","img: "+img);

                //SETTO LA VARIABILE PER CAPIRE SE HO LANDSCAPE O PORTRAIT
                portrait = myBitmap.getWidth() <= myBitmap.getHeight();

                Intent intent = new Intent(this, ScreenActivity.class);

                intent.putExtra("game_name",game_name);
                intent.putExtra("conf_name",conf_name);
                intent.putExtra("img",img);
                intent.putExtra("screenName",screenName);
                intent.putExtra("portrait",portrait);
                intent.putExtra("fromActivity","Gallery");

                startActivity(intent);

                Toast.makeText(this,"Immagine caricata correttamente",Toast.LENGTH_LONG).show();

            }
            else {

                Toast.makeText(this,"Errore nel caricare l'immagine",Toast.LENGTH_LONG).show();

            }

        }
    }

    public void addFragmentEditScreen(Fragment fragment, boolean addToBackStack, String tag) {

        Bundle bundle = new Bundle();
        bundle.putString("screenName", screenName);
        bundle.putString("img",img);
        bundle.putBoolean("portrait",portrait);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_editScreen, fragment, tag);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void toFragmentEditScreen() {
        addFragmentEditScreen(new FragmentEditScreen(), false, "fragmentEditScreen");
    }

    //METODO CHE CONTROLLA QUANTI MARKER STAMPARE PER UNO SCREEN
    public void linksToDraw(ArrayList<Link> linkArrayList, ArrayList<Event> eventArrayList, Bitmap bitmap) {

        //VARIABILE PER CONTROLLARE CHE IL CICLO SOTTO SIA GIUSTO
        int k = 0;

        for(Event e : eventArrayList) {
            for(Link l : linkArrayList) {

                if (e.getScreenImage().equals(screen.getImage()) && l.getEvent() == e) {

                    k++;
                    drawCircles(bitmap, l.getMarkerColor(), (float) e.getX(), (float) e.getY());
                    Log.d("DRAW_CIRCLES", "Cerchio position, x: " + (float) e.getX() + "; y: " + (float) e.getY());

                }

            }
        }

        Log.d("DRAW_CIRCLES","Cerchi da disegnare: "+ k);

    }

    //METODO CHE DISEGNA I MARKER SULL'IMMAGINE
    public void drawCircles(Bitmap bitmap, int circle_color, float x, float y) {

        //PRENDO I RIFERIMENTI DEL DISPLAY PER MOSTRARE IL CERCHIO NEL PUNTO GIUSTO
        WindowManager window = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = window.getDefaultDisplay();

        Point point = new Point(1000, 1000);
        display.getRealSize(point);
        int width = point.x;
        int height = point.y;
        //MODIFICO X E Y PER MOSTRARE I MARKER NEL PUNTO GIUSTO
        x = x * width;
        y = y * height;
        //DISEGNO IL CERCHIO PER RAPPRESENTARE IL MARKER
        Canvas canvas = new Canvas(bitmap);                 //draw a canvas in defined bmp

        Paint paint = new Paint();                          //define paint and paint color
        paint.setColor(circle_color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth(0.5f);
        paint.setAntiAlias(true);        //smooth edges

        imageScreen.setImageBitmap(bitmap);
        canvas.drawCircle(x, y, 50, paint);

        //Invalidate to update Bitmap in ImageView
        imageScreen.invalidate();

        Log.d("DRAW_CIRCLES","Cerchio position, x: "+ x / width + "; y: "+ y / height);

    }

    //FUNZIONE PER PRENDERE I RIFERIMENTI ALLE VISTE DEL LAYOUT DELLA SCREEN_ACTIVITY
    public void getViewsReferences() {

        screenTitle = findViewById(R.id.screenTitle);
        screenTitle2 = findViewById(R.id.screenTitle_2);
        cardView2 = findViewById(R.id.cardView2);
        imageScreen = findViewById(R.id.imageScreen);
        tapOnImageTv = findViewById(R.id.tapOnImage_tv);
        editScreen = findViewById(R.id.editScreen);
        deleteScreen = findViewById(R.id.deleteScreen);
        eventTv = findViewById(R.id.eventTv);
        eventListTv2 = findViewById(R.id.eventList_tv2);
        recyclerView_link = findViewById(R.id.recycler_view_link);
        info_btn = findViewById(R.id.info_btn);
        back_btn = findViewById(R.id.back_btn);
        newEvent = findViewById(R.id.newEvent);

    }

    //FUNZIONE PER CAMBIARE LE VISTE NEL CASO IN CUI HO FATTO TAP NELLA SCHERMATA PRECEDENTE PER INSERIRE UN NUOVO SCREEN
    public void changeViewsForNewScreen() {

        //PRENDO I RIFERIMENTI ALLE VISTE (QUESTO PER SICUREZZA LO RICHIAMO OGNI VOLTA CHE UTILIZZO QUESTE FUNZIONI)
        getViewsReferences();
        //SETTO IL TITOLO
        screenTitle.setText(screenName);
        //SETTO IL SOTTOTITOLO CHE è SEMPRE UGUALE PER QUALSIASI SCREEN DI QUESTA CONFIGURAZIONE DI QUESTO GIOCO
        String title2 = conf_name.concat(" | ").concat(game_name);
        screenTitle2.setText(title2);
        //SETTO LE ALTRE VISTE DI QUESTA SCHERMATA
        eventTv.setText(R.string.eventTv_no_screen);
        tapOnImageTv.setVisibility(View.VISIBLE);
        editScreen.setVisibility(View.GONE);
        deleteScreen.setVisibility(View.GONE);
        imageScreen.setOnClickListener(buttonListener);
        recyclerView_link.setVisibility(View.GONE);
        //AGGIUNGO I LISTENER AI PULSANTI
        info_btn.setOnClickListener(buttonListener);
        back_btn.setOnClickListener(buttonListener);
        newEvent.setOnClickListener(buttonListener);
        newEvent.setBackground(this.getDrawable(R.drawable.btn_vocal_rec_state_ok_background));
        newEvent.setEnabled(false);

    }

    //METODO PER SETTARE LE VISTE QUANDO LO SCREEN ESISTE GIA' ED E' LANDSCAPE
    public void changeViewsForExistingPortraitScreen() {

        //PRENDO I RIFERIMENTI ALLE VISTE (QUESTO PER SICUREZZA LO RICHIAMO OGNI VOLTA CHE UTILIZZO QUESTE FUNZIONI)
        getViewsReferences();
        //SETTO TITOLO
        screenTitle.setText(screenName);
        //SETTO SOTTO_TITOLO
        String title2 = conf_name.concat(" | ").concat(game_name);
        screenTitle2.setText(title2);
        //SETTO LE ALTRE VISTE
        tapOnImageTv.setVisibility(View.GONE);
        cardView2.setBackground(null);
        imageScreen.setOnClickListener(null);
        bitmap = MainModel.getInstance().stringToBitMap(img);
        setBitmapPosition(bitmap);
        imageScreen.setImageBitmap(bitmap);
        editScreen.setVisibility(View.VISIBLE);
        deleteScreen.setVisibility(View.VISIBLE);
        editScreen.setOnClickListener(buttonListener);
        deleteScreen.setOnClickListener(buttonListener);
        //CONTROLLO SE QUESTA SCHERMATA HA LINK DA MOSTRARE
        links = MainModel.getInstance().getScreenFromConf(configuration,screenName).getLinks();

        if(links.size() != 0) {

            //RECYCLER_VIEW
            recyclerView_link.setVisibility(View.VISIBLE);
            recyclerView_link.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,
                    false));
            linkAdapter = new LinkAdapter(this);
            recyclerView_link.setAdapter(linkAdapter);
            linkAdapter.notifyDataSetChanged();
            //ALTRE VISTE
            eventTv.setVisibility(View.GONE);
            eventListTv2.setText(R.string.eventList_tv2);
            //RENDO LA BITMAP MUTABLE PER DISEGNARCI SOPRA
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            //DISEGNO I LINK SOPRA LA BITMAP
            linksToDraw(links,events,bitmap);
        }
        else {

            recyclerView_link.setVisibility(View.GONE);
            eventTv.setText(R.string.eventTv_add_event);
            eventTv.setVisibility(View.VISIBLE);
            eventListTv2.setText(R.string.eventList_tv2_empty);

        }
        //AGGIUNGO I LISTENER AI PULSANTI
        info_btn.setOnClickListener(buttonListener);
        back_btn.setOnClickListener(buttonListener);
        //INFINE ABILITO IL PULSANTE AGGIUNGI EVENTO VISTO CHE HO UNA SCHERMATA DEFINITA
        newEvent.setBackground(this.getDrawable(R.drawable.btn_add_event_background));
        newEvent.setEnabled(true);
        newEvent.setOnClickListener(buttonListener);

    }

    //METODO PER SETTARE LE VISTE QUANDO LO SCREEN ESISTE GIA' ED E' LANDSCAPE
    public void changeViewsForExistingLandscapeScreen() {

        //SETTO IL LAYOUT PER LANDSCAPE SCREEN
        setContentView(R.layout.activity_screen_for_landscape_image);
        //PRENDO I RIFERIMENTI ALLE VISTE
        getViewsReferences();
        //SETTO TITOLO
        screenTitle.setText(screenName);
        //SETTO SOTTO_TITOLO
        String title2 = conf_name.concat(" | ").concat(game_name);
        screenTitle2.setText(title2);
        //SETTO LE ALTRE VISTE
        tapOnImageTv.setVisibility(View.GONE);
        cardView2.setBackground(null);
        imageScreen.setOnClickListener(null);
        bitmap = MainModel.getInstance().stringToBitMap(img);
        setBitmapPosition(bitmap);
        imageScreen.setImageBitmap(bitmap);
        editScreen.setVisibility(View.VISIBLE);
        deleteScreen.setVisibility(View.VISIBLE);
        editScreen.setOnClickListener(buttonListener);
        deleteScreen.setOnClickListener(buttonListener);
        //CONTROLLO SE QUESTA SCHERMATA HA LINK DA MOSTRARE
        links = MainModel.getInstance().getScreenFromConf(configuration,screenName).getLinks();

        if(links.size() != 0) {

            recyclerView_link.setVisibility(View.VISIBLE);
            eventTv.setVisibility(View.GONE);
            eventListTv2.setText(R.string.eventList_tv2);
            //RENDO LA BITMAP MUTABLE PER DISEGNARCI SOPRA
            bitmap = bitmap.copy(bitmap.getConfig(), true);
            //DISEGNO I LINK SOPRA LA BITMAP
            linksToDraw(links,events,bitmap);

        }
        else {

            recyclerView_link.setVisibility(View.GONE);
            recyclerView_link.setVisibility(View.GONE);
            eventTv.setText(R.string.eventTv_add_event);
            eventTv.setVisibility(View.VISIBLE);
            eventListTv2.setText(R.string.eventList_tv2_empty);

        }
        //AGGIUNGO I LISTENER AI PULSANTI
        info_btn.setOnClickListener(buttonListener);
        back_btn.setOnClickListener(buttonListener);
        //INFINE ABILITO IL PULSANTE AGGIUNGI EVENTO VISTO CHE HO UNA SCHERMATA DEFINITA
        newEvent.setBackground(this.getDrawable(R.drawable.btn_add_event_background));
        newEvent.setEnabled(true);
        newEvent.setOnClickListener(buttonListener);

    }

    //METODO PER MOSTRARE ALERTDIALOG ELIMINA SCREEN
    public void alertDialogDeleteScreen() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScreenActivity.this);
        LayoutInflater inflater = ScreenActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_remove_screen, null);

        Button delete = dialogView.findViewById(R.id.deleteScreen_btn);
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

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO MODIFICARE QUA
                //MainModel.getInstance().removeScreen(configuration,screenName);
                //RISCRIVO FILE configurations.json PER TOGLIERE LO SCREEN DA QUESTA CONFIGURAZIONE
                MainModel.getInstance().writeConfigurationsJson();
                //RISCRIVO FILE games.json PER TOGLIERE GLI EVENTI IN QUESTA SCHERMATA DAL GIOCO
                MainModel.getInstance().writeGamesJson();

                alertDialog.dismiss();

                ConfigurationDetail.screenAdapter.notifyDataSetChanged();

                Intent toConfDetail = new Intent(getApplicationContext(),ConfigurationDetail.class);
                toConfDetail.putExtra("title",game_name);
                toConfDetail.putExtra("name",conf_name);
                startActivity(toConfDetail);

            }
        });

    }

    //METODO PER MOSTRARE ALERTDIALOG INFO
    public void alertDialogInfoBtn() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ScreenActivity.this);
        LayoutInflater inflater = ScreenActivity.this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alert_dialog_info_screen_activity, null);

        Button back = dialogView.findViewById(R.id.backScreen_btn);

        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        alertDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });


    }

    //METODO CHE GESTISCE QUANDO FACCIO INDIETRO CON I COMANDI DEL DISPOSITIVO
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(this, ConfigurationDetail.class);
        intent.putExtra("title", game_name);
        intent.putExtra("name", conf_name);
        startActivity(intent);

    }

    //METODO CHE SETTA LA POSIZIONE DELLA BITMAP
    public void setBitmapPosition(Bitmap bm) {

        if (bm.getWidth() > bm.getHeight()) {

            Log.d("POSIZIONE_BITMAP", "più larga che alta " + bm.getWidth() + " " + bm.getHeight());

            imageScreen.getLayoutParams().width = imageScreen.getLayoutParams().height;
            imageScreen.getLayoutParams().height = imageScreen.getLayoutParams().width;

        }

    }
}
