package com.ewlab.a_cube.tab_games;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ewlab.a_cube.R;

public class SelectEventTypeAdapter extends RecyclerView.Adapter<SelectEventTypeViewHolder> {

    private final NewLink newLink;
    private LayoutInflater selectActionInflater;

    public SelectEventTypeAdapter(NewLink newLink) {

        this.selectActionInflater = LayoutInflater.from(newLink);
        this.newLink = newLink;

    }

    @NonNull
    @Override
    public SelectEventTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View view = selectActionInflater.inflate(R.layout.single_event_type_view, parent, false);
        return new SelectEventTypeViewHolder(view, newLink);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectEventTypeViewHolder holder, int i) {

        String[] eventType = newLink.getResources().getStringArray(R.array.spinnerEventType);
        String eType = eventType[i];
        holder.setCellEventType(eType);

    }

    @Override
    public int getItemCount() {

        String[] eventType = newLink.getResources().getStringArray(R.array.spinnerEventType);
        return eventType.length;
    }
}
