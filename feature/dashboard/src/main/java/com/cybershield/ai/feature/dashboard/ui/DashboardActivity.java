package com.cybershield.ai.feature.dashboard.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cybershield.ai.feature.dashboard.adapter.ThreatLogAdapter;
import com.cybershield.ai.feature.dashboard.databinding.ActivityDashboardBinding;
import com.cybershield.ai.feature.dashboard.viewmodel.DashboardViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardActivity extends AppCompatActivity {

    private ActivityDashboardBinding binding;
    private DashboardViewModel viewModel;
    private ThreatLogAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerView();
        observeViewModel();
        setupListeners();

        viewModel.runOrchestratorEvaluation();
    }

    private void setupRecyclerView() {
        adapter = new ThreatLogAdapter(threat -> {
            viewModel.mitigateThreat(threat.getId());
            Toast.makeText(this, "Mitigating threat...", Toast.LENGTH_SHORT).show();
        });
        binding.recyclerViewThreats.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewThreats.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getThreatLogs().observe(this, threats -> {
            adapter.setThreats(threats);
            if (threats != null && !threats.isEmpty()) {
                binding.tvActiveThreatsCount.setText(String.valueOf(threats.size()));
            } else {
                binding.tvActiveThreatsCount.setText("0");
            }
        });

        viewModel.getLatestScan().observe(this, scan -> {
            if (scan != null) {
                binding.tvScanStatus.setText("Status: " + scan.getScanStatus());
                binding.tvItemsScanned.setText("Items Scanned: " + scan.getItemsScanned());
            } else {
                binding.tvScanStatus.setText("Status: Not Scanned");
            }
        });

        viewModel.getActiveStrategy().observe(this, strategy -> {
            binding.tvOrchestratorStrategy.setText("Active Strategy: " + strategy);
        });

        viewModel.getSystemMetrics().observe(this, metrics -> {
            if (metrics != null) {
                binding.tvCpuUsage.setText(String.format("CPU: %.1f%%", metrics.getCpuUsagePercent()));
                binding.tvRamUsage.setText(String.format("RAM: %.1f%%", metrics.getRamUsagePercent()));
                binding.tvBatteryLevel.setText("Battery: " + metrics.getBatteryLevelPercent() + "%");
            }
        });
    }

    private void setupListeners() {
        binding.btnRunAudit.setOnClickListener(v -> {
            Toast.makeText(this, "Executing Full Multi-Agent Security Audit...", Toast.LENGTH_SHORT).show();
            viewModel.triggerFullAudit(result -> {
                if (result.isSuccess()) {
                    Toast.makeText(this, "Security Audit Completed Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Audit Error: " + result.message, Toast.LENGTH_LONG).show();
                }
            });
        });

        binding.swipeRefresh.setOnRefreshListener(() -> {
            viewModel.runOrchestratorEvaluation();
            binding.swipeRefresh.setRefreshing(false);
        });
    }
}
