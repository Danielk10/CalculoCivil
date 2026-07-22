package com.diamon.calculo.ui;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;

/**
 * About screen showing software information, licenses, and disclaimers.
 */
public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#121212"));

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 48, 48, 48);

        // Title
        TextView title = createText("Structural and Seismic Research", 22, true, Color.WHITE);
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        TextView subtitle = createText("Professional Structural Analysis powered by OpenSees", 14, false, Color.parseColor("#90CAF9"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 32);
        layout.addView(subtitle);

        // Version
        layout.addView(createCard("Version", "v1.0.0\nBuild: Android NDK ARM64-v8a\nOpenGL ES 3.0 Rendering Engine"));

        // About OpenSees
        layout.addView(createCard("About OpenSees",
                "OpenSees (Open System for Earthquake Engineering Simulation) is a " +
                "software framework for simulating the seismic response of structural " +
                "and geotechnical systems.\n\n" +
                "Developed at the Pacific Earthquake Engineering Research Center (PEER) " +
                "at the University of California, Berkeley.\n\n" +
                "Version: 3.8.0\n" +
                "Interpreters: TCL 8.6 & Python 3.11 (OpenSeesPy)"));

        // License
        layout.addView(createCard("License",
                "OpenSees is distributed under the BSD license:\n\n" +
                "Copyright (c) 1999-2024 The Regents of the University of California.\n" +
                "All Rights Reserved.\n\n" +
                "Redistribution and use in source and binary forms, with or without " +
                "modification, are permitted provided that the following conditions are met:\n\n" +
                "1. Redistributions of source code must retain the above copyright notice.\n" +
                "2. Redistributions in binary form must reproduce the above copyright notice.\n" +
                "3. Neither the name of the University nor the names of its contributors " +
                "may be used to endorse or promote products derived from this software."));

        // Disclaimer
        layout.addView(createCard("Disclaimer",
                "This application is an independent project and is NOT officially " +
                "affiliated with, endorsed by, or sponsored by the University of California, " +
                "Berkeley, PEER, or the OpenSees development team.\n\n" +
                "The engineer of record is solely responsible for verifying all analysis " +
                "results produced by this software.\n\n" +
                "USE AT YOUR OWN RISK. No warranty is provided."));

        // Contact
        layout.addView(createCard("Developer",
                "Diamon Engineering\n" +
                "GitHub: github.com/diamon\n" +
                "Contact: https://diamon.com"));

        scrollView.addView(layout);
        setContentView(scrollView);
    }

    private LinearLayout createCard(String title, String content) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(Color.parseColor("#1E1E1E"));
        card.setPadding(32, 24, 32, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 16, 0, 0);
        card.setLayoutParams(params);

        TextView tvTitle = createText(title, 16, true, Color.parseColor("#64B5F6"));
        tvTitle.setPadding(0, 0, 0, 12);
        card.addView(tvTitle);

        TextView tvContent = createText(content, 13, false, Color.parseColor("#E0E0E0"));
        card.addView(tvContent);

        return card;
    }

    private TextView createText(String text, int sizeSp, boolean bold, int color) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        return tv;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
