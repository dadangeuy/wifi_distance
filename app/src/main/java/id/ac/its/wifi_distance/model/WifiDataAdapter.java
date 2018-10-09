package id.ac.its.wifi_distance.model;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import id.ac.its.wifi_distance.R;

public class WifiDataAdapter extends RecyclerView.Adapter<WifiDataAdapter.ViewHolder> {
    private final List<WifiData> data;

    public WifiDataAdapter(List<WifiData> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.layout_wifi_row, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WifiData wifiData = data.get(position);
        holder.summary.setText(wifiData.getSummary());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView summary;

        ViewHolder(View itemView) {
            super(itemView);
            summary = itemView.findViewById(R.id.WifiSummaryView);
        }
    }
}
