package com.ewlab.a_cube.tab_games;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ewlab.a_cube.R;

public class SelectEventTypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private final NewLink newLink;
    TextView eventType_tv;
    ImageView eventType_iv;

    public SelectEventTypeViewHolder(@NonNull View itemView, NewLink newLink) {

        super(itemView);
        eventType_tv = itemView.findViewById(R.id.eventType_tv);
        eventType_iv = itemView.findViewById(R.id.eventType_iv);
        itemView.setOnClickListener(this);
        this.newLink = newLink;
    }

    public void setCellEventType(String nome) {

        eventType_tv.setText(nome);

        switch(nome){

            case "Tap":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_tap_icon_white_32dp));
                break;

            case "Long Tap - Input length":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_long_tap_input_length_icon_white_32dp));
                break;

            case "Long Tap - ON/OFF":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_long_tap_input_length_icon_white_32dp));
                break;

            case "Long Tap - Timed":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_long_tap_timed_icon_white_32dp));
                break;

            case "Swipe - Left":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_swipe_left_icon_white_32dp));
                break;

            case "Swipe - Up":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_swipe_up_icon_white_32dp));
                break;
            case "Swipe - Down":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_swipe_down_icon_white_32dp));
                break;

            case "Swipe - Right":
                eventType_iv.setImageDrawable(newLink.getDrawable(R.drawable.ic_swipe_right_icon_white_32dp));
                break;

        }

    }

    @Override
    public void onClick(View v) {

        String eventType = eventType_tv.getText().toString();
        newLink.searchEventType.setText(eventType);

        switch(eventType){

            case "Tap":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_tap_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;

            case "Long Tap - Input length":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_long_tap_input_length_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;

            case "Long Tap - ON/OFF":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_long_tap_input_length_icon_white_32dp, 0,0,0);
                //MOSTRO IL BOTTONE PER SELEZIONARE ACTION STOP
                newLink.linearLayoutH_ActionStop.setVisibility(View.VISIBLE);
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                break;

            case "Long Tap - Timed":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_long_tap_timed_icon_white_32dp, 0,0,0);
                //MOSTRO BOTTONE PER SELEZIONARE DURATA DELL'EVENTO
                newLink.linearLayoutH_Duration.setVisibility(View.VISIBLE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);
                break;

            case "Swipe - Up":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_swipe_up_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;
            case "Swipe - Down":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_swipe_down_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;
            case "Swipe - Left":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_swipe_left_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;
            case "Swipe - Right":

                newLink.searchEventType.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_swipe_right_icon_white_32dp, 0,0,0);
                //NASCONDO PER SICUREZZA I BOTTONI CHE NON CENTRANO
                newLink.linearLayoutH_Duration.setVisibility(View.GONE);
                newLink.linearLayoutH_ActionStop.setVisibility(View.GONE);

                break;

        }

        newLink.closeFragmentEventsType();


    }
}
