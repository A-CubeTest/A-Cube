package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.model.Game;
import com.ewlab.a_cube.model.MainModel;
import com.ewlab.a_cube.R;

import java.util.List;

public class GamesAdapter extends ArrayAdapter<Game> {
    private static final String TAG = GamesAdapter.class.getName();


    public GamesAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public GamesAdapter(@NonNull Context context, int resource, @NonNull List<Game> objects) {
        super(context, resource, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        View v = convertView;

        if(v == null){
            v = LayoutInflater.from(getContext()).inflate(R.layout.game_element,null);
        }

        Game gi = getItem(position);

        if(gi != null){
            final TextView name = v.findViewById(R.id.game_element_title);
            final ImageView icon = v.findViewById(R.id.app_icon);
            final ImageView imageMatching = v.findViewById(R.id.game_element_matching);

            name.setText(gi.getTitle());

            String i = gi.getIcon();
            Bitmap bm = MainModel.getInstance().StringToBitMap(i);
            icon.setImageBitmap(bm);

            double m = MainModel.getInstance().matchingUpdate(gi.getTitle());
            Log.d(TAG, "match: "+gi.getTitle()+" "+m);

            if(m > 0 && m < 1)
                imageMatching.setImageResource(R.drawable.almost_full);
            else if(m == 1)
                imageMatching.setImageResource(R.drawable.recording);
        }

        return v;

    }

}
