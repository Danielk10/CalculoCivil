package com.diamon.calculo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Toast;

import com.diamon.calculo.databinding.ActivityMainBinding;
import com.diamon.calculo.engine.OpenSeesExecutor;
import com.diamon.calculo.export.PDFReportGenerator;
import com.diamon.calculo.model.*;
import com.diamon.calculo.renderer.Structural3DRenderer;
import com.diamon.calculo.renderer.StructuralGLSurfaceView;
import com.diamon.calculo.terminal.LinuxTerminalView;
import com.diamon.calculo.terminal.TerminalCommandParser;
import com.diamon.calculo.ui.AboutActivity;
import com.diamon.calculo.ui.PrivacyPolicyActivity;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity with 3-tab professional structural analysis interface.
 * Tab 1: Structural Model & Definition
 * Tab 2: 3D OpenGL ES 3.0 Viewer
 * Tab 3: Linux-Style Terminal & Logger
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // Engine
    private OpenSeesExecutor openSeesExecutor;

    // Model
    private StructuralModel model = new StructuralModel();
    private int nextNodeId = 1;
    private int nextElemId = 1;
    private int nextMatId = 1;
    private int nextLoadPatternId = 1;

    // Renderer
    private StructuralGLSurfaceView glSurfaceView;
    private Structural3DRenderer renderer;
    private boolean glInitialized = false;

    // Terminal
    private LinuxTerminalView terminalView;
    private TerminalCommandParser commandParser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Structural & Seismic Research");
        }

        openSeesExecutor = new OpenSeesExecutor(this);

        setupTabs();
        setupStructuralModelTab();
        setupViewerTab();
        setupTerminalTab();
        checkAndLoadAssets();
    }

    // ==================== TAB SETUP ====================

    private void setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_model));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_viewer));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(R.string.tab_terminal));

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switchTab(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void switchTab(int position) {
        binding.layoutStructuralModel.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
        binding.layoutViewer3D.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        binding.layoutTerminal.setVisibility(position == 2 ? View.VISIBLE : View.GONE);

        if (position == 1 && !glInitialized) {
            initGL();
        }
    }

    // ==================== TAB 1: STRUCTURAL MODEL ====================

    private void setupStructuralModelTab() {
        // Material spinner
        String[] materialTypes = {
                getString(R.string.mat_concrete_c30),
                getString(R.string.mat_steel_a992),
                getString(R.string.mat_steel_a36),
                getString(R.string.mat_elastic)
        };
        ArrayAdapter<String> matAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, materialTypes);
        binding.spinnerMaterial.setAdapter(matAdapter);

        // Load type spinner
        String[] loadTypes = {
                getString(R.string.load_dead),
                getString(R.string.load_live),
                getString(R.string.load_eqx),
                getString(R.string.load_eqy)
        };
        ArrayAdapter<String> loadAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, loadTypes);
        binding.spinnerLoadType.setAdapter(loadAdapter);

        // Add Node button
        binding.btnAddNode.setOnClickListener(v -> addNode());

        // Assign Material button
        binding.btnAssignMaterial.setOnClickListener(v -> assignMaterial());

        // Apply Load button
        binding.btnApplyLoad.setOnClickListener(v -> applyLoad());

        // Script editor buttons
        binding.btnRunTcl.setOnClickListener(v -> runScriptFromEditor("tcl"));
        binding.btnRunPython.setOnClickListener(v -> runScriptFromEditor("py"));
    }

    private void addNode() {
        try {
            String idStr = binding.etNodeId.getText().toString().trim();
            int id = idStr.isEmpty() ? nextNodeId : Integer.parseInt(idStr);
            double x = parseDouble(binding.etNodeX.getText().toString(), 0.0);
            double y = parseDouble(binding.etNodeY.getText().toString(), 0.0);
            double z = parseDouble(binding.etNodeZ.getText().toString(), 0.0);

            StructuralNode node = new StructuralNode(id, x, y, z);
            if (binding.cbFixed.isChecked()) {
                node.setFixed(true, true, true);
                node.fixRZ = true;
            }
            model.addNode(node);
            nextNodeId = id + 1;

            updateNodeList();
            clearNodeInputs();
            updateRendererModel();
            Toast.makeText(this, "Node " + id + " added", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input values", Toast.LENGTH_SHORT).show();
        }
    }

    private void assignMaterial() {
        int pos = binding.spinnerMaterial.getSelectedItemPosition();
        StructuralMaterial mat;
        switch (pos) {
            case 0: mat = StructuralMaterial.concreteC30(nextMatId); break;
            case 1: mat = StructuralMaterial.steelA992(nextMatId); break;
            case 2: mat = StructuralMaterial.steelA36(nextMatId); break;
            default:
                double E = parseDouble(binding.etEModulus.getText().toString(), 2.0e8);
                mat = StructuralMaterial.elastic(nextMatId, E);
                break;
        }
        model.addMaterial(mat);
        nextMatId++;
        Toast.makeText(this, "Material assigned: " + mat.name, Toast.LENGTH_SHORT).show();
    }

    private void applyLoad() {
        try {
            int nodeId = Integer.parseInt(binding.etLoadNodeId.getText().toString().trim());
            double fx = parseDouble(binding.etFx.getText().toString(), 0.0);
            double fy = parseDouble(binding.etFyLoad.getText().toString(), 0.0);
            double fz = parseDouble(binding.etFzLoad.getText().toString(), 0.0);

            String loadTypeName = binding.spinnerLoadType.getSelectedItem().toString();
            LoadPattern lp = new LoadPattern(nextLoadPatternId, loadTypeName, loadTypeName);
            lp.addLoad(nodeId, fx, fy, fz);
            model.addLoadPattern(lp);
            nextLoadPatternId++;

            Toast.makeText(this, "Load applied to Node " + nodeId, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid load values", Toast.LENGTH_SHORT).show();
        }
    }

    private void runScriptFromEditor(String type) {
        String script = binding.etScriptEditor.getText().toString().trim();
        if (script.isEmpty()) {
            Toast.makeText(this, "Script editor is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Switch to terminal tab to show output
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2));

        executor.execute(() -> {
            String result;
            if ("tcl".equals(type)) {
                result = openSeesExecutor.executeTclScriptContent(script);
            } else {
                result = openSeesExecutor.executePyScriptContent(script);
            }
            final String output = result;
            mainHandler.post(() -> {
                if (terminalView != null) {
                    terminalView.appendSystem("=== Script Execution (" + type.toUpperCase() + ") ===");
                    terminalView.appendOutput(output);
                }
            });
        });
    }

    private void updateNodeList() {
        StringBuilder sb = new StringBuilder();
        for (StructuralNode node : model.getNodes()) {
            sb.append(node.toString()).append("\n");
        }
        binding.tvNodeList.setText(sb.length() > 0 ? sb.toString() : "No nodes defined yet.");
    }

    private void clearNodeInputs() {
        binding.etNodeId.setText("");
        binding.etNodeX.setText("");
        binding.etNodeY.setText("");
        binding.etNodeZ.setText("");
        binding.cbFixed.setChecked(false);
    }

    // ==================== TAB 2: 3D VIEWER ====================

    private void setupViewerTab() {
        binding.btnWireframe.setOnClickListener(v -> {
            if (renderer != null) renderer.setShowDeformed(false);
        });

        binding.btnDeformed.setOnClickListener(v -> {
            if (renderer != null) renderer.setShowDeformed(true);
        });

        binding.seekDeformScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = progress / 10.0f;
                binding.tvScaleLabel.setText(String.format(Locale.US, "Deformation Scale: %.1fx", scale));
                if (renderer != null) renderer.setDeformationScale(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void initGL() {
        glSurfaceView = new StructuralGLSurfaceView(this);
        renderer = glSurfaceView.getStructuralRenderer();
        binding.containerGL.addView(glSurfaceView);
        glInitialized = true;
        updateRendererModel();
    }

    // ==================== TAB 3: TERMINAL ====================

    private void setupTerminalTab() {
        terminalView = new LinuxTerminalView(this);
        commandParser = new TerminalCommandParser(getFilesDir(), openSeesExecutor);

        terminalView.setCommandListener(command -> {
            executor.execute(() -> {
                TerminalCommandParser.CommandResult result = commandParser.execute(command);
                mainHandler.post(() -> {
                    if (result.isClear) {
                        terminalView.clearOutput();
                    } else if (result.success) {
                        terminalView.appendOutput(result.output);
                    } else {
                        terminalView.appendError(result.output);
                    }
                });
            });
        });

        binding.containerTerminal.addView(terminalView);

        // Quick action buttons
        binding.btnRunTclTest.setOnClickListener(v -> {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2));
            executor.execute(() -> {
                TerminalCommandParser.CommandResult result = commandParser.execute("run-test-tcl");
                mainHandler.post(() -> terminalView.appendOutput(result.output));
            });
        });

        binding.btnRunPyTest.setOnClickListener(v -> {
            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2));
            executor.execute(() -> {
                TerminalCommandParser.CommandResult result = commandParser.execute("run-test-py");
                mainHandler.post(() -> terminalView.appendOutput(result.output));
            });
        });

        binding.btnExportPdf.setOnClickListener(v -> exportPdf());
    }

    // ==================== PDF EXPORT ====================

    private void exportPdf() {
        File reportsDir = new File(getFilesDir(), "reports");
        if (!reportsDir.exists()) reportsDir.mkdirs();

        String filename = "report_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(reportsDir, filename);

        executor.execute(() -> {
            PDFReportGenerator generator = new PDFReportGenerator();
            boolean success = generator.generateReport(this, model,
                    "Structural Analysis Project", "Engineer", outputFile);

            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "PDF saved: " + outputFile.getAbsolutePath(),
                            Toast.LENGTH_LONG).show();
                    if (terminalView != null) {
                        terminalView.appendSystem("PDF report generated: " + outputFile.getAbsolutePath());
                    }
                } else {
                    Toast.makeText(this, "Failed to generate PDF", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ==================== ASSET LOADING ====================

    private void checkAndLoadAssets() {
        boolean ready = AssetHelper.areAssetsExtracted(this);
        if (ready) {
            binding.layoutLoading.setVisibility(View.GONE);
            binding.layoutMainUI.setVisibility(View.VISIBLE);
            executor.execute(() -> AssetHelper.ensureRuntimeReady(MainActivity.this));
        } else {
            binding.layoutLoading.setVisibility(View.VISIBLE);
            binding.layoutMainUI.setVisibility(View.GONE);

            executor.execute(() -> {
                boolean success = AssetHelper.ensureRuntimeReady(MainActivity.this);
                mainHandler.post(() -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    binding.layoutMainUI.setVisibility(View.VISIBLE);
                    if (success) {
                        Toast.makeText(this, R.string.loading_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.loading_error, Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }

    // ==================== MENU ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_about) {
            startActivity(new Intent(this, AboutActivity.class));
            return true;
        } else if (id == R.id.menu_privacy) {
            startActivity(new Intent(this, PrivacyPolicyActivity.class));
            return true;
        } else if (id == R.id.menu_export_pdf) {
            exportPdf();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onPause() {
        super.onPause();
        if (glSurfaceView != null) glSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (glSurfaceView != null) glSurfaceView.onResume();
    }

    // ==================== HELPERS ====================

    private void updateRendererModel() {
        if (renderer == null || glSurfaceView == null) return;
        
        int nNodes = model.getNodes().size();
        int nElems = model.getElements().size();
        
        float[] nodePos = new float[nNodes * 3];
        float[] nodeCol = new float[nNodes * 4];
        
        int i = 0, c = 0;
        for (StructuralNode node : model.getNodes()) {
            nodePos[i++] = (float) node.x;
            nodePos[i++] = (float) node.y;
            nodePos[i++] = (float) node.z;
            nodeCol[c++] = 1.0f; nodeCol[c++] = 0.5f; nodeCol[c++] = 0.0f; nodeCol[c++] = 1.0f;
        }
        
        float[] elemPos = new float[nElems * 2 * 3];
        float[] elemCol = new float[nElems * 2 * 4];
        
        i = 0; c = 0;
        for (FrameElement elem : model.getElements()) {
            StructuralNode n1 = model.getNode(elem.nodeI);
            StructuralNode n2 = model.getNode(elem.nodeJ);
            if (n1 != null && n2 != null) {
                elemPos[i++] = (float) n1.x; elemPos[i++] = (float) n1.y; elemPos[i++] = (float) n1.z;
                elemPos[i++] = (float) n2.x; elemPos[i++] = (float) n2.y; elemPos[i++] = (float) n2.z;
                for(int k=0; k<2; k++) {
                    elemCol[c++] = 0.0f; elemCol[c++] = 0.8f; elemCol[c++] = 1.0f; elemCol[c++] = 1.0f;
                }
            }
        }
        
        glSurfaceView.queueEvent(() -> {
            renderer.setNodes(nodePos, nodeCol);
            renderer.setElements(elemPos, elemCol);
        });
    }

    private double parseDouble(String text, double defaultValue) {
        if (text == null || text.trim().isEmpty()) return defaultValue;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}