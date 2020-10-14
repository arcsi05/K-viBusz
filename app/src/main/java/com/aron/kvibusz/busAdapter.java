package com.aron.kvibusz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class busAdapter extends RecyclerView.Adapter<busAdapter.MyViewHolder> {

    ArrayList<String> busStop, lineColor, textColor, lineNumber, direction;
    ArrayList<Long> whenTime;
    Double currentTime;
    Context context;

    public busAdapter(Context ct, ArrayList<String> s1, ArrayList<String> s2, ArrayList<String> s3, ArrayList<String> s4, ArrayList<String> s5, ArrayList<Long> s6, Double s7) {
        context = ct;
        busStop = s1;
        lineNumber = s2;
        lineColor = s3;
        textColor = s4;
        direction = s5;
        whenTime = s6;
        currentTime = s7;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.bus_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.stopTextView.setText(busStop.get(position));
        holder.numberTextView.setText(lineNumber.get(position));
        holder.numberTextView.setBackgroundColor(Color.parseColor(lineColor.get(position)));
        holder.numberTextView.setTextColor(Color.parseColor(textColor.get(position)));
        holder.directionTextView.setText(direction.get(position));
        Long whenMinutes = Math.round((whenTime.get(position) - currentTime) / 60);
        holder.remainingTextView.setText(whenMinutes + "'");

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, activeBusActivity.class);
                intent.putExtra("lineNum", lineNumber.get(position));
                intent.putExtra("lineColor", lineColor.get(position));
                intent.putExtra("busStop", busStop.get(position));
                intent.putExtra("direction", direction.get(position));
                intent.putExtra("timeLeft", (Math.round(whenTime.get(position) - currentTime) / 60));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return busStop.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView stopTextView, numberTextView, directionTextView, remainingTextView;

        ConstraintLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            stopTextView = itemView.findViewById(R.id.busStopTextView);
            numberTextView = itemView.findViewById(R.id.lineNumberTextView);
            directionTextView = itemView.findViewById(R.id.destinationTextView);
            remainingTextView = itemView.findViewById(R.id.timeLeftTextView);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
