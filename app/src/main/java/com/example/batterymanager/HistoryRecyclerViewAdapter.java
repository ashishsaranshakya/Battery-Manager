package com.example.batterymanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder> {
    private ArrayList<HistoryEntry> arrayList=new ArrayList<>();
    Context context;

    public void setArrayList(ArrayList<HistoryEntry> arrayList) {
        this.arrayList = arrayList;
        notifyDataSetChanged();
    }

    public HistoryRecyclerViewAdapter(Context context) {
        this.context=context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.history_view,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.status.setText(arrayList.get(position).getStatus());
        //interval format II%-FF% or III%-FF% or II%-FFF%
        String interval=arrayList.get(position).getChangeInterval();
        int initialRange=Integer.parseInt(interval.split("%")[0]);
        int finalRange=Integer.parseInt(interval.split("%")[1].substring(1));
        holder.changeInterval.setText(arrayList.get(position).getChangeInterval());
        if (initialRange>finalRange) holder.change.setTextColor(context.getResources().getColor(R.color.red));
        else holder.change.setTextColor(context.getResources().getColor(R.color.green));
        holder.changeInterval.setTextColor(context.getResources().getColor(R.color.dgrey));
        holder.screenOn_avgWattage.setText(arrayList.get(position).getScreenOn_avgWattage());
        holder.change.setText(arrayList.get(position).getChange());
        holder.time.setText(arrayList.get(position).getTime());
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO to activity with window according to interval selected
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView status;
        TextView changeInterval;
        TextView screenOn_avgWattage;
        TextView time;
        TextView change;
        ConstraintLayout layout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            status=itemView.findViewById(R.id.status);
            changeInterval=itemView.findViewById(R.id.changeInterval);
            screenOn_avgWattage=itemView.findViewById(R.id.screenOn_avgWattage);
            time=itemView.findViewById(R.id.time);
            change=itemView.findViewById(R.id.change);
            layout=itemView.findViewById(R.id.constraintLayout);
        }
    }

}
