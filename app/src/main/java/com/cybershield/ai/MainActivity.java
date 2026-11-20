package com.cybershield.ai;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cybershield.ai.feature.agents.base.AgentResult;
import com.cybershield.ai.feature.agents.phishing.PhishingDetectorAgent;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    @Inject
    PhishingDetectorAgent phishingDetectorAgent;

    private Button analyzeButton;
    private EditText urlInput;
    private ProgressBar progressBar;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContent());
    }

    @NonNull
    private View createContent() {
        int padding = dp(24);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(padding, padding, padding, padding);
        layout.setBackgroundColor(Color.rgb(10, 25, 47));

        TextView title = new TextView(this);
        title.setText("CyberShield AI");
        title.setTextSize(28);
        title.setTextColor(Color.WHITE);
        title.setGravity(Gravity.CENTER);

        TextView subtitle = new TextView(this);
        subtitle.setText("Private on-device URL risk check");
        subtitle.setTextSize(16);
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, dp(8), 0, dp(32));

        urlInput = new EditText(this);
        urlInput.setHint("Paste a website address");
        urlInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setSingleLine(true);
        urlInput.setTextColor(Color.WHITE);
        urlInput.setHintTextColor(Color.LTGRAY);
        urlInput.setText("https://example.com");

        analyzeButton = new Button(this);
        analyzeButton.setText("Analyze URL");
        analyzeButton.setOnClickListener(view -> analyzeUrl());

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);

        resultView = new TextView(this);
        resultView.setText("Enter a URL to check it on this device.");
        resultView.setTextSize(16);
        resultView.setTextColor(Color.WHITE);
        resultView.setPadding(0, dp(28), 0, 0);

        layout.addView(title, matchWidth());
        layout.addView(subtitle, matchWidth());
        layout.addView(urlInput, matchWidth());
        layout.addView(analyzeButton, matchWidth());
        layout.addView(progressBar, wrapWidth());
        layout.addView(resultView, matchWidth());
        return layout;
    }

    private void analyzeUrl() {
        String url = urlInput.getText().toString().trim();
        if (url.isEmpty()) {
            resultView.setText("Enter a URL before analyzing.");
            resultView.setTextColor(Color.YELLOW);
            return;
        }

        setLoading(true);
        phishingDetectorAgent.analyzeUrl(url, this::showResult);
    }

    private void showResult(@NonNull AgentResult result) {
        setLoading(false);
        int score = Math.round(result.getRiskScore() * 100);
        if (result.isThreatDetected()) {
            resultView.setTextColor(Color.rgb(255, 138, 128));
            resultView.setText("DANGEROUS • " + score + "% risk\n\n" + result.getSummary()
                    + "\n\nRecommended action: Do not open this link. Verify it with the official service.");
        } else {
            resultView.setTextColor(Color.rgb(165, 214, 167));
            resultView.setText("NO HIGH-RISK INDICATORS • " + score + "% risk\n\n"
                    + result.getSummary()
                    + "\n\nRecommended action: Continue cautiously and verify unexpected requests.");
        }
    }

    private void setLoading(boolean isLoading) {
        analyzeButton.setEnabled(!isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    @NonNull
    private LinearLayout.LayoutParams matchWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    @NonNull
    private LinearLayout.LayoutParams wrapWidth() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}