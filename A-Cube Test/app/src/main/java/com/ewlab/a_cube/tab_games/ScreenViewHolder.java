package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Configuration;
import com.ewlab.a_cube.model.Event;
import com.ewlab.a_cube.model.Link;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.model.Screen;

import java.util.ArrayList;

public class ScreenViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final ConfigurationDetail configurationDetail;
    private TextView screen_name;
    private ImageView screen_image;

    Configuration configuration = MainModel.getInstance().getConfiguration(ConfigurationDetail.gameName,
            ConfigurationDetail.confName);
    ArrayList<Screen> screens = MainModel.getInstance().getScreensFromConf(configuration);

    public ScreenViewHolder(@NonNull View itemView, ConfigurationDetail configurationDetail) {

        super(itemView);
        screen_name = itemView.findViewById(R.id.name_screen);
        screen_image = itemView.findViewById(R.id.image_screen);
        itemView.setOnClickListener(this);
        this.configurationDetail = configurationDetail;
    }

    public void setCellScreenshot(String name, String image) {

        screen_name.setText(name);
        screen_image.setImageBitmap(MainModel.getInstance().stringToBitMap(image));

    }

    //METODO CHE CONTROLLA QUANTI MARKER STAMPARE PER UNO SCREEN
    public void linksToDraw(ArrayList<Link> linkArrayList, ArrayList<Event> eventArrayList, Bitmap bitmap) {

        Screen screen = screens.get(getAdapterPosition());
        //VARIABILE PER CONTROLLARE CHE IL CICLO SOTTO SIA GIUSTO
        int k = 0;
        //IL CONTROLLO è GIUSTO
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

    public void drawCircles(Bitmap bitmap, int circle_color, float x, float y) {

        //PRENDO I RIFERIMENTI DEL DISPLAY PER MOSTRARE IL CERCHIO NEL PUNTO GIUSTO
        WindowManager window = (WindowManager) configurationDetail.getSystemService(Context.WINDOW_SERVICE);
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

        screen_image.setImageBitmap(bitmap);
        canvas.drawCircle(x, y, 50, paint);

        //Invalidate to update Bitmap in ImageView
        screen_image.invalidate();

        Log.d("DRAW_CIRCLES","Cerchio position, x: "+ x / width + "; y: "+ y / height);

    }

    @Override
    public void onClick(View v) {

        Intent screen_intent = new Intent(configurationDetail.getApplicationContext(), ScreenActivity.class);

        Screen screen = screens.get(getAdapterPosition());
        String screenName = screen.getName();
        boolean portrait = screen.isPortrait();

        screen_intent.putExtra("game_name", ConfigurationDetail.gameName);
        screen_intent.putExtra("conf_name", ConfigurationDetail.confName);
        screen_intent.putExtra("screen_index", getAdapterPosition());
        screen_intent.putExtra("screenName", screenName);
        screen_intent.putExtra("portrait", portrait);
        screen_intent.putExtra("fromActivity","ConfDetail_existingScreen");

        configurationDetail.startActivity(screen_intent);

    }

}
