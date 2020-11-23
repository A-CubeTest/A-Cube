package com.ewlab.a_cube.tab_games;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

public class ScreenPosition extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;

    private final static int PERMISSION_GRANTED = 1;

    private static final String TAG = ScreenPosition.class.getName();

    String gameName, confName, screenImage, screenName, name_event;
    boolean portrait;

    Configuration configuration;
    Screen screen;
    Bitmap bitmap;

    RelativeLayout rl;

    Intent intent_backToNewLink;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.d("LIFECYCLE", "onCreate ScreenPosition");
        super.onCreate(savedInstanceState);
        //NASCONDO IL TITOLO
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //NASCONDO BARRA DEL TITOLO
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        //ABILITO FULL SCREEN
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //setContentView(R.layout.activity_main);
        setContentView(R.layout.screen_position);

        View decorView = getWindow().getDecorView();
        //NASCONDO LA STATUS BAR
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        rl = findViewById(R.id.relativelayout);

        intent_backToNewLink = new Intent(this, NewLink.class);
        //CREO UN'IMAGEVIEW
        ImageView imageView = new ImageView(this);
        //SETTO LA POSIZIONE DELL'IMAGEVIEW
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT));

        //PRENDO TUTTI I DATI DALL'INTENT DELLA SCHERMATA PRECEDENTE
        gameName = getIntent().getStringExtra("gameName");
        confName = getIntent().getStringExtra("confName");
        screenName = getIntent().getStringExtra("screenName");
        screenImage = getIntent().getStringExtra("screenImage");
        portrait = getIntent().getBooleanExtra("portrait",true);
        name_event = getIntent().getStringExtra("event");
        //DATI DEL LINK CHE STAVO CONFIGURANDO
        final String provisionalEventName = getIntent().getStringExtra("provisionalEventName");
        final String provisionalEventType = getIntent().getStringExtra("provisionalEventType");
        final String provisionalAction = getIntent().getStringExtra("provisionalAction");
        final String provisionalActionStop = getIntent().getStringExtra("provisionalActionStop");
        final String provisionalDurationTime = getIntent().getStringExtra("provisionalDurationTime");
        final String provisionalMarkerColor = getIntent().getStringExtra("provisionalMarkerColor");
        final String provisionalMarkerSize = getIntent().getStringExtra("provisionalMarkerSize");
        //SE SONO DIVERSI DA NULL LI RIPASSO INDIETRO QUANDO TORNO ALLA SCHERMATA NEWLINK DOPO LO SCREENSHOT
        if (provisionalEventName != null) {
            intent_backToNewLink.putExtra("provisionalEventName", provisionalEventName);
        }

        if (provisionalEventType != null) {
            intent_backToNewLink.putExtra("provisionalEventType", provisionalEventType);
        }

        if (provisionalAction != null) {
            intent_backToNewLink.putExtra("provisionalAction", provisionalAction);
        }

        if (provisionalActionStop != null) {
            intent_backToNewLink.putExtra("provisionalActionStop", provisionalActionStop);
        }

        if (provisionalDurationTime != null) {
            intent_backToNewLink.putExtra("provisionalDurationTime", provisionalDurationTime);
        }

        if(provisionalMarkerColor != null)
            intent_backToNewLink.putExtra("provisionalMarkerColor",provisionalMarkerColor);
        if(provisionalMarkerSize != null)
            intent_backToNewLink.putExtra("provisionalMarkerSize",provisionalMarkerSize);

        configuration = MainModel.getInstance().getConfiguration(gameName, confName);
        screen = MainModel.getInstance().getScreenFromConf(configuration, screenName);

        bitmap = MainModel.getInstance().stringToBitMap(screenImage);

        Log.d("LIFECYCLE", "BM_WIDTH:  " + bitmap.getWidth() + " BM_HEIGHT: " + bitmap.getHeight());
        //CONTROLLO SE HO UNO SCREEN LANDSCAPE OPPURE PORTRAIT
        if (bitmap.getWidth() > bitmap.getHeight()) {

            Log.d("BITMAP_SIZE", "wider than high " + bitmap.getWidth() + " " + bitmap.getHeight());

            Bitmap bOutput;
            float degrees = 90;//rotation degree
            Matrix matrix1 = new Matrix();
            matrix1.setRotate(degrees);
            bOutput = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix1, true);
            //SETTO L'IMAGEVIEW CON ALTEZZA PARI ALLA LARGHEZZA E VICEVERSA
            imageView.getLayoutParams().width = bitmap.getHeight();
            imageView.getLayoutParams().height = bitmap.getWidth();
            imageView.setImageBitmap(bOutput);

        }
        else {

            Log.d("BITMAP_SIZE", "higher than wide");
            //SETTO L'IMAGEVIEW NORMALMENTE PERCHè SONO NEL CASO DI SCREEN IN PORTRAIT
            imageView.getLayoutParams().width = bitmap.getWidth();
            imageView.getLayoutParams().height = bitmap.getHeight();

            imageView.setImageBitmap(bitmap);

        }

        //TODO DISEGNARE GLI ALTRI LINK DEFINITI IN QUESTO SCREENSHOT ECCETTO QUELLO CHE STO DEFINENDO
        //TODO NO PIUTTOSTO POTREI FAR VISUALIZZARE (NEL CASO IN CUI SIA GIà DEFINITO) IL PUNTO IN CUI VIENE ESEGUITA ORA L'AZIONE
        /*
        bitmap = bitmap.copy(bitmap.getConfig(), true);     //lets bmp to be mutable

        Game game = configuration.getGame();
        ArrayList<Event> events = game.getEvents();

        for(Event e : events) {
            for(Link l : configuration.getLinks()) {

                if (e.getScreenshot().equals(screen.getScreenshot()) && l.getEvent() == e && ! e.getName().equals(name_event)) {

                    drawCircle(imageView,bitmap,l.getMarkerColor(),(float)e.getX(),(float)e.getY());
                    Log.d("DRAW_CIRCLES","Cerchio position, x: "+ (float)e.getX() + "; y: "+(float)e.getY());
                }
            }
        }

         */
        //-----------------------------------------------------------------------------------------------------------------

        Toast.makeText(this, R.string.screen_position, Toast.LENGTH_LONG).show();

        intent_backToNewLink.putExtra("gameName", gameName);
        intent_backToNewLink.putExtra("confName", confName);
        intent_backToNewLink.putExtra("screenName",screenName);
        intent_backToNewLink.putExtra("screenImage",screenImage);
        intent_backToNewLink.putExtra("portrait",portrait);
        intent_backToNewLink.putExtra("event",name_event);

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                double x = motionEvent.getX();
                double y = motionEvent.getY();

                //TODO: mentre getWidth ritorna 720 (valore corretto) getHeight ritorna 1340 al posto di 1400
                WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                Display display = window.getDefaultDisplay();

                Point point = new Point(1000, 1000);
                display.getRealSize(point);

                int width, height;

                if(portrait) {
                    width = point.x;
                    height = point.y;
                }
                else {
                    width = point.y;
                    height = point.x;
                }

                Log.d("DIMENSIONI_SCREEN", "Dimensioni: w "+width+" h "+height+" | " +
                        "point.x : "+point.x+" ; point.y : "+point.y);

                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "touched down --> (x,y) = (" + x / width + "," + y / height + ")");
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.d(TAG, "moving: (" + x / width + ", " + y / height + ")");
                        break;
                    case MotionEvent.ACTION_UP:

                        if(portrait) {

                            Log.d(TAG, "Portrait touched up --> (x,y) = (" + x / width + "," + y / height + ")");
                            Log.d(TAG, "y: "+ y + " | height: " + height);
                            Log.d(TAG, "x: "+ x + " | width: " + width);
                            intent_backToNewLink.putExtra("x", "" + x / width);
                            intent_backToNewLink.putExtra("y", "" + y / height);
                        }
                        else {
                            Log.d(TAG, "Landscape touched up --> (x,y) = (" + y / width + "," + x / height + ")");
                            Log.d(TAG, "y: "+ x + " | height: " + height);
                            Log.d(TAG, "x: "+ y + " | width: " + width);
                            intent_backToNewLink.putExtra("x", "" + y / width);
                            intent_backToNewLink.putExtra("y", "" + x / height);
                        }

                        startActivity(intent_backToNewLink);

                        break;
                }

                return true;
            }
        });

        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        rl.addView(imageView);

    }

    /*
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //File imgFile = new File(PATH);
        final Context context = getApplicationContext();
        Bitmap myBitmap = null;
        int orientation = -1;
        Matrix matrix = new Matrix();

        //intent1 = new Intent(context, NewLink.class);

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            String a = String.valueOf(selectedImage);
            //intent1.putExtra("img", a);

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

        }

        //PRENDO TUTTI I DATI DALL'INTENT DELLA SCHERMATA PRECEDENTE
        final String title = getIntent().getStringExtra("title");
        final String nameConfig = getIntent().getStringExtra("name");
        final String event = getIntent().getStringExtra("event");
        final String provisionalName = getIntent().getStringExtra("provisionalName");
        final String provisionalEventType = getIntent().getStringExtra("provisionalEventType");
        final String provisionalAction = getIntent().getStringExtra("provisionalAction");
        final String provisionalActionStop = getIntent().getStringExtra("provisionalActionStop");
        final String provisionalDurationTime = getIntent().getStringExtra("provisionalDurationTime");

        //intent1.putExtra("title", title);
        //intent1.putExtra("name", nameConfig);
        //intent1.putExtra("event", event);

        //SE SONO DIVERSI DA NULL LI RIPASSO INDIETRO QUANDO TORNO ALLA SCHERMATA NEWLINK DOPO LO SCREENSHOT
        if (provisionalName != null) {
            //intent1.putExtra("provisionalName", provisionalName);
        }

        if (provisionalEventType != null) {
            //intent1.putExtra("provisionalEventType", provisionalEventType);
        }

        if (provisionalAction != null) {
            //intent1.putExtra("provisionalAction", provisionalAction);
        }

        if (provisionalActionStop != null) {
            //intent1.putExtra("provisionalActionStop", provisionalActionStop);
        }

        if (provisionalDurationTime != null) {
            //intent1.putExtra("provisionalDurationTime", provisionalDurationTime);
        }

        //ImageView Setup
        ImageView imageView = new ImageView(this);
        //setting image position
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        if (myBitmap != null) {

            if (myBitmap.getWidth() > myBitmap.getHeight()) {

                Log.d(TAG, "wider than high " + myBitmap.getWidth() + " " + myBitmap.getHeight());

                Bitmap bOutput;
                float degrees = 90;//rotation degree
                Matrix matrix1 = new Matrix();
                matrix1.setRotate(degrees);
                bOutput = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix1, true);

                imageView.getLayoutParams().width = myBitmap.getHeight();
                imageView.getLayoutParams().height = myBitmap.getWidth();
                imageView.setImageBitmap(bOutput);

            }
            else {

                Log.d(TAG, "higher than wide");

                imageView.getLayoutParams().width = myBitmap.getWidth();
                imageView.getLayoutParams().height = myBitmap.getHeight();

                imageView.setImageBitmap(myBitmap);

            }


            Toast.makeText(this, R.string.screen_position, Toast.LENGTH_LONG).show();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            myBitmap.compress(Bitmap.CompressFormat.PNG, 1, baos);
            byte[] b = baos.toByteArray();
            final String img = Base64.encodeToString(b, Base64.DEFAULT);

            imageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    double x = motionEvent.getX();
                    double y = motionEvent.getY();

                    //TODO: mentre getWidth ritorna 720 (valore corretto) getHeight ritorna 1340 al posto di 1400
                    WindowManager window = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    Display display = window.getDefaultDisplay();

                    Point point = new Point(1000, 1000);
                    display.getRealSize(point);
                    int width = point.x;
                    int height = point.y;

                    Log.d("DIMENSIONI_SCREEN", width+" "+height+" "+point.x+" "+point.y);

                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            Log.d(TAG, "touched down --> (x,y) = (" + x / width + "," + y / height + ")");
                            break;
                        case MotionEvent.ACTION_MOVE:
                            Log.d(TAG, "moving: (" + x / width + ", " + y / height + ")");
                            break;
                        case MotionEvent.ACTION_UP:
                            Log.d(TAG, "touched up --> (x,y) = (" + x / width + "," + y / height + ")");
                            Log.d(TAG, y + "   " + height);

                            //intent1.putExtra("x", "" + x / width);
                            //intent1.putExtra("y", "" + y / height);// height);
                            //startActivity(intent1);

                            break;
                    }

                    return true;
                }
            });

            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            rl.addView(imageView);
        }
        else{
            //startActivity(intent1);
        }
    }

     */
    /*
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

     */

    @Override
    protected void onStart() {
        super.onStart();
        //verifica dei permessi
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_GRANTED);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_GRANTED:
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                } else {
                    Toast.makeText(this, R.string.permissions_denied, Toast.LENGTH_LONG).show();
                    finish();
                }
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(intent_backToNewLink);
    }

}

