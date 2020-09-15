package com.ewlab.a_cube.tab_games;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.R;
import com.ewlab.a_cube.model.Link;

import java.util.List;

public class LinkAdapter extends ArrayAdapter<Link> {

  public LinkAdapter(@NonNull Context context, int resource) {
    super(context, resource);
  }

  public LinkAdapter(@NonNull Context context, int resource, @NonNull List<Link> objects) {
    super(context, resource, objects);
  }

  @NonNull
  @Override
  public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

    View v = convertView;

    if(v == null){
      v = LayoutInflater.from(getContext()).inflate(R.layout.action_event_item,null);
    }

    Link item = getItem(position);

    if(item != null){
      final TextView event = v.findViewById(R.id.link_textview);
      final TextView action = v.findViewById(R.id.link_textview2);
      final ImageView linked = v.findViewById(R.id.link_imageView);
      event.setText(item.getEvent().getName());

      if(item.getAction()==null) {
        linked.setVisibility(View.INVISIBLE);
      }else{
        action.setText(item.getAction().getName());
        linked.setVisibility(View.VISIBLE);
      }

    }

    return v;
  }

}
