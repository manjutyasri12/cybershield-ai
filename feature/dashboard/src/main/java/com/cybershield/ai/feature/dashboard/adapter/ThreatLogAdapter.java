package com.cybershield.ai.feature.dashboard.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cybershield.ai.core.database.entity.ThreatEvent;
import com.cybershield.ai.feature.dashboard.databinding.ItemThreatLogBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThreatLogAdapter extends RecyclerView.Adapter<ThreatLogAdapter.ViewHolder> {

    private final List<ThreatEvent> threatList = new ArrayList<>();
    private final OnMitigateClickListener mitigateListener;

    public interface OnMitigateClickListener {
        void onMitigateClick(ThreatEvent threat);
    }

    public ThreatLogAdapter(OnMitigateClickListener mitigateListener) {
        this.mitigateListener = mitigateListener;
    }

    public void setThreats(List<ThreatEvent> threats) {
        threatList.clear();
        if (threats != null) {
            threatList.addAll(threats);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemThreatLogBinding binding = ItemThreatLogBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThreatEvent threat = threatList.get(position);
        holder.bind(threat, mitigateListener);
    }

    @Override
    public int getItemCount() {
        return threatList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemThreatLogBinding binding;

        public ViewHolder(ItemThreatLogBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(ThreatEvent threat, OnMitigateClickListener listener) {
            binding.tvThreatType.setText(threat.getThreatType());
            binding.tvAgentName.setText(threat.getAgentName());
            binding.tvSummary.setText(threat.getSummary());
            binding.tvRiskScore.setText(String.format("Risk: %.2f", threat.getRiskScore()));

            String dateStr = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(new Date(threat.getTimestamp()));
            binding.tvTimestamp.setText(dateStr);

            if (threat.isMitigated()) {
                binding.btnMitigate.setText("Mitigated");
                binding.btnMitigate.setEnabled(false);
            } else {
                binding.btnMitigate.setText("Mitigate");
                binding.btnMitigate.setEnabled(true);
                binding.btnMitigate.setOnClickListener(v -> listener.onMitigateClick(threat));
            }
        }
    }
}
