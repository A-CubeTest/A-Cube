package com.ewlab.a_cube.tab_games;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class ScreenPosition extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;

    private final static int PERMISSION_GRANTED = 1;

    private static final String TAG = ScreenPosition.class.getName();


    private static RelativeLayout rl;

    Intent intent1 = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);


        setContentView(R.layout.screen_position);

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
        rl = findViewById(R.id.relativelayout);

        Intent intentImg = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intentImg, RESULT_LOAD_IMAGE);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //File imgFile = new File(PATH);
        final Context context = getApplicationContext();
        Bitmap myBitmap = null;
        int orientation = -1;
        Matrix matrix = new Matrix();

        intent1 = new Intent(context, NewLink.class);

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String a = String.valueOf(selectedImage);
            intent1.putExtra("img", a);

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


        final String title = getIntent().getStringExtra("title");
        final String nameConfig = getIntent().getStringExtra("name");
        final String event = getIntent().getStringExtra("event");
        final String provisionalName = getIntent().getStringExtra("provisionalName");
        final String provisionalEventType = getIntent().getStringExtra("provisionalEventType");
        final String provisionalAction = getIntent().getStringExtra("provisionalAction");
        final String provisionalActionStop = getIntent().getStringExtra("provisionalActionStop");
        final String provisionalDurationTime = getIntent().getStringExtra("provisionalDurationTime");



        intent1.putExtra("title", title);
        intent1.putExtra("name", nameConfig);
        intent1.putExtra("event", event);

        if (provisionalName != null) {
            intent1.putExtra("provisionalName", provisionalName);
        }

        if (provisionalEventType != null) {
            intent1.putExtra("provisionalEventType", provisionalEventType);
        }

        if (provisionalAction != null) {
            intent1.putExtra("provisionalAction", provisionalAction);
        }

        if (provisionalActionStop != null) {
            intent1.putExtra("provisionalActionStop", provisionalActionStop);
        }

        if (provisionalDurationTime != null) {
            intent1.putExtra("provisionalDurationTime", provisionalDurationTime);
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

            } else {
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

                    Log.d("Dimensioni screen", width+" "+height+" "+point.x+" "+point.y);

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

                            intent1.putExtra("x", "" + x / width);
                            intent1.putExtra("y", "" + y / height);// height);
                            startActivity(intent1);

                            break;
                    }

                    return true;
                }
            });

            imageView.setScaleType(ImageView.ScaleType.FIT_XY);

            rl.addView(imageView);
        }else{
            startActivity(intent1);
        }
    }

    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
        startActivity(intent1);
    }

}

