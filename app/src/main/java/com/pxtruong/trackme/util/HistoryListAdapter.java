package com.pxtruong.trackme.util;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pxtruong.trackme.R;
import com.pxtruong.trackme.data.Session;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HistoryListAdapter extends RecyclerView.Adapter<HistoryListAdapter.HistoryViewHolder> {

    private List<Session> mSession;
    private Context mContext;

    public HistoryListAdapter(Context context) {
        this.mContext = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryListAdapter.HistoryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_record, parent, false);;
        return new HistoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.tvDistance.setText(String.format("%.2f km", mSession.get(position).distance));
        holder.tvAvgSpeed.setText(String.format("%.2f km/h", mSession.get(position).distance));
        holder.ivMap.setImageURI(Uri.parse(mSession.get(position).imgUri));

        long duration = mSession.get(position).duration;
        holder.tvDuration.setText(String.format("%02d:%02d:%02d", duration/3600, (duration % 3600) / 60, duration % 60));
    }

    public void setSessionData(List<Session> sessions){
        mSession = sessions;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSession == null ? 0 : mSession.size();
    }

    /**
     * Data ViewHolder class.
     */
    public static class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView tvDistance;
        private TextView tvAvgSpeed;
        private TextView tvDuration;
        private ImageView ivMap;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            tvDistance = itemView.findViewById(R.id.distanceValue);
            tvAvgSpeed = itemView.findViewById(R.id.avgSpeedValue);
            tvDuration = itemView.findViewById(R.id.durationValue);
            ivMap = itemView.findViewById(R.id.imageView);
        }
    }
}
