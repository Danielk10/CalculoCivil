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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.view.MotionEvent;
import android.os.Environment;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    // File picker launcher
    private final ActivityResultLauncher<String[]> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes);
                        binding.etScriptEditor.setText(new String(bytes));
                        Toast.makeText(this, "Script importado exitosamente", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error leyendo archivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
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
        preloadExample();
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
        binding.btnImport.setOnClickListener(v -> filePickerLauncher.launch(new String[]{"*/*"}));
        binding.btnExport.setOnClickListener(v -> exportScriptToFile());

        // Internal scroll for Script Editor
        binding.etScriptEditor.setOnTouchListener((v, event) -> {
            if (v.hasFocus()) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
            }
            return false;
        });
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
            Toast.makeText(this, "Nodo " + id + " agregado", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores de entrada inválidos", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(this, "Material asignado: " + mat.name, Toast.LENGTH_SHORT).show();
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

            updateRendererModel();
            Toast.makeText(this, "Carga aplicada al Nodo " + nodeId, Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valores de carga inválidos", Toast.LENGTH_SHORT).show();
        }
    }

    private void runScriptFromEditor(String type) {
        String script = binding.etScriptEditor.getText().toString().trim();
        if (script.isEmpty()) {
            Toast.makeText(this, "El editor de script está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.tvScriptOutput.setVisibility(View.VISIBLE);
        binding.tvScriptOutput.setText("▶ Ejecutando script (" + type.toUpperCase() + ")...");

        executor.execute(() -> {
            String result;
            if ("tcl".equals(type)) {
                result = openSeesExecutor.executeTclScriptContent(script);
            } else {
                result = openSeesExecutor.executePyScriptContent(script);
            }
            final String output = result;
            mainHandler.post(() -> {
                binding.tvScriptOutput.setText(output);
            });
        });
    }

    private void exportScriptToFile() {
        String text = binding.etScriptEditor.getText().toString();
        if (text.isEmpty()) {
            Toast.makeText(this, "El editor está vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();
        File outputFile = new File(downloadsDir, "script_" + System.currentTimeMillis() + ".tcl");
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(text);
            Toast.makeText(this, "Script guardado en Descargas:\n" + outputFile.getName(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar script: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNodeList() {
        StringBuilder sb = new StringBuilder();
        for (StructuralNode node : model.getNodes()) {
            sb.append(node.toString()).append("\n");
        }
        binding.tvNodeList.setText(sb.length() > 0 ? sb.toString() : "Sin nodos definidos aún.");
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
        binding.btnCalculate.setOnClickListener(v -> calculateModel());
        binding.btnExportPdfViewer.setOnClickListener(v -> exportPdf());

        binding.btnWireframe.setOnClickListener(v -> {
            if (renderer != null) renderer.setShowDeformed(false);
        });

        binding.btnDeformed.setOnClickListener(v -> {
            if (renderer != null) renderer.setShowDeformed(true);
        });

        binding.btnDiagrams.setOnClickListener(v -> {
            if (renderer != null) {
                boolean show = !renderer.isShowDiagrams();
                renderer.setShowDiagrams(show);
                Toast.makeText(this, show ? "Diagramas M/V activados" : "Diagramas desactivados", Toast.LENGTH_SHORT).show();
            }
        });

        binding.seekDeformScale.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = progress / 10.0f;
                binding.tvScaleLabel.setText(String.format(Locale.US, "Escala de Deformación: %.1fx", scale));
                if (renderer != null) renderer.setDeformationScale(scale);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void calculateModel() {
        Toast.makeText(this, "⚡ Calculando estructura con OpenSees TCL...", Toast.LENGTH_SHORT).show();
        executor.execute(() -> {
            String tclScript = model.generateTclScript();
            String rawOutput = openSeesExecutor.executeTclScriptContent(tclScript);
            AnalysisResult result = AnalysisResult.parseOpenSeesOutput(rawOutput);
            model.setResult(result);

            mainHandler.post(() -> {
                updateRendererModel();
                Toast.makeText(this, String.format(Locale.US, "✅ Cálculo completado!\nMax Disp: %.4e m", result.getMaxDisplacement()), Toast.LENGTH_LONG).show();
            });
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
    }

    // ==================== PDF EXPORT ====================

    private void exportPdf() {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloadsDir.exists()) downloadsDir.mkdirs();

        String filename = "Reporte_Estructural_" + System.currentTimeMillis() + ".pdf";
        File outputFile = new File(downloadsDir, filename);

        executor.execute(() -> {
            PDFReportGenerator generator = new PDFReportGenerator();
            boolean success = generator.generateReport(this, model,
                    "Análisis Estructural SAP2000", "Ingeniero Calculista", outputFile);

            mainHandler.post(() -> {
                if (success) {
                    Toast.makeText(this, "📄 Reporte PDF guardado en Descargas:\n" + outputFile.getName(), Toast.LENGTH_LONG).show();
                    if (terminalView != null) {
                        terminalView.appendSystem("Reporte PDF generado en Descargas: " + outputFile.getAbsolutePath());
                    }
                } else {
                    Toast.makeText(this, "Error al generar reporte PDF", Toast.LENGTH_SHORT).show();
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

    private void preloadExample() {
        // Preload nodes
        model.addNode(new StructuralNode(1, 0, 0, 0));
        model.addNode(new StructuralNode(2, 0, 3, 0));
        model.addNode(new StructuralNode(3, 4, 3, 0));
        model.addNode(new StructuralNode(4, 4, 0, 0));
        
        StructuralNode n1 = model.getNode(1);
        if (n1 != null) { n1.setFixed(true, true, true); n1.fixRZ = true; }
        StructuralNode n4 = model.getNode(4);
        if (n4 != null) { n4.setFixed(true, true, true); n4.fixRZ = true; }
        
        // Preload material & section
        model.addMaterial(StructuralMaterial.concreteC30(1));
        model.addSection(SectionProfile.rectColumn300(1));
        
        // Preload elements
        model.addElement(new FrameElement(1, 1, 2, 1, 1));
        model.addElement(new FrameElement(2, 2, 3, 1, 1));
        model.addElement(new FrameElement(3, 3, 4, 1, 1));
        
        // Preload load pattern
        LoadPattern lp = new LoadPattern(1, "Live", "Live");
        lp.addLoad(2, 50.0, -10.0, 0);
        model.addLoadPattern(lp);
        
        updateNodeList();
        
        // Preload script
        String sampleTcl = "# Simple Portal Frame\n" +
                "wipe\n" +
                "model BasicBuilder -ndm 2 -ndf 3\n" +
                "node 1 0 0\n" +
                "node 2 0 3\n" +
                "node 3 4 3\n" +
                "node 4 4 0\n" +
                "fix 1 1 1 1\n" +
                "fix 4 1 1 1\n" +
                "geomTransf Linear 1\n" +
                "element elasticBeamColumn 1 1 2 0.12 2e8 0.0016 1\n" +
                "element elasticBeamColumn 2 2 3 0.12 2e8 0.0016 1\n" +
                "element elasticBeamColumn 3 3 4 0.12 2e8 0.0016 1\n" +
                "timeSeries Linear 1\n" +
                "pattern Plain 1 1 {\n" +
                "  load 2 50.0 -10.0 0.0\n" +
                "}\n" +
                "system BandGeneral\n" +
                "numberer RCM\n" +
                "constraints Plain\n" +
                "integrator LoadControl 1.0\n" +
                "algorithm Linear\n" +
                "analysis Static\n" +
                "analyze 1\n" +
                "puts \"Node 2 Ux: [nodeDisp 2 1]\"\n" +
                "puts \"Node 3 Ux: [nodeDisp 3 1]\"\n";
        binding.etScriptEditor.setText(sampleTcl);
    }

    private void updateRendererModel() {
        if (renderer == null || glSurfaceView == null) return;

        int nNodes = model.getNodes().size();
        int nElems = model.getElements().size();

        // 1. Nodes (Orange)
        float[] nodePos = new float[nNodes * 3];
        float[] nodeCol = new float[nNodes * 4];
        int i = 0, c = 0;
        for (StructuralNode node : model.getNodes()) {
            nodePos[i++] = (float) node.x;
            nodePos[i++] = (float) node.y;
            nodePos[i++] = (float) node.z;
            nodeCol[c++] = 1.0f; nodeCol[c++] = 0.5f; nodeCol[c++] = 0.0f; nodeCol[c++] = 1.0f;
        }

        // 2. Elements (Light Blue)
        float[] elemPos = new float[nElems * 2 * 3];
        float[] elemCol = new float[nElems * 2 * 4];
        i = 0; c = 0;
        for (FrameElement elem : model.getElements()) {
            StructuralNode n1 = model.getNode(elem.nodeI);
            StructuralNode n2 = model.getNode(elem.nodeJ);
            if (n1 != null && n2 != null) {
                elemPos[i++] = (float) n1.x; elemPos[i++] = (float) n1.y; elemPos[i++] = (float) n1.z;
                elemPos[i++] = (float) n2.x; elemPos[i++] = (float) n2.y; elemPos[i++] = (float) n2.z;
                for (int k = 0; k < 2; k++) {
                    elemCol[c++] = 0.0f; elemCol[c++] = 0.8f; elemCol[c++] = 1.0f; elemCol[c++] = 1.0f;
                }
            }
        }

        // 3. Load vectors (Red/Yellow force arrows)
        List<Float> loadLines = new ArrayList<>();
        List<Float> loadColors = new ArrayList<>();
        for (LoadPattern lp : model.getLoadPatterns()) {
            if (lp.loads != null) {
                for (NodeLoad nl : lp.loads) {
                    StructuralNode node = model.getNode(nl.nodeId);
                    if (node != null) {
                        float startX = (float) node.x;
                        float startY = (float) node.y;
                        float startZ = (float) node.z;
                        float endX = startX + (float) (nl.fx * 0.02);
                        float endY = startY + (float) (nl.fy * 0.02);
                        float endZ = startZ + (float) (nl.fz * 0.02);

                        loadLines.add(startX); loadLines.add(startY); loadLines.add(startZ);
                        loadLines.add(endX); loadLines.add(endY); loadLines.add(endZ);

                        for (int k = 0; k < 2; k++) {
                            loadColors.add(1.0f); loadColors.add(0.2f); loadColors.add(0.0f); loadColors.add(1.0f);
                        }
                    }
                }
            }
        }
        float[] loadPos = new float[loadLines.size()];
        for (int k = 0; k < loadLines.size(); k++) loadPos[k] = loadLines.get(k);
        float[] loadCol = new float[loadColors.size()];
        for (int k = 0; k < loadColors.size(); k++) loadCol[k] = loadColors.get(k);

        // 4. Deformed shape (Green/Cyan) & Moment Diagrams (Magenta)
        AnalysisResult res = model.getResult();
        float[] defPos = null;
        float[] defCol = null;
        float[] diagPos = null;
        float[] diagCol = null;

        if (res != null) {
            Map<Integer, double[]> disps = res.getNodeDisplacements();
            if (disps != null && !disps.isEmpty()) {
                defPos = new float[nElems * 2 * 3];
                defCol = new float[nElems * 2 * 4];
                int di = 0, dc = 0;
                float scale = 5.0f;
                for (FrameElement elem : model.getElements()) {
                    StructuralNode n1 = model.getNode(elem.nodeI);
                    StructuralNode n2 = model.getNode(elem.nodeJ);
                    if (n1 != null && n2 != null) {
                        double[] d1 = disps.get(n1.id);
                        double[] d2 = disps.get(n2.id);
                        float dx1 = d1 != null ? (float) d1[0] * scale : 0f;
                        float dy1 = d1 != null ? (float) d1[1] * scale : 0f;
                        float dz1 = d1 != null ? (float) d1[2] * scale : 0f;
                        float dx2 = d2 != null ? (float) d2[0] * scale : 0f;
                        float dy2 = d2 != null ? (float) d2[1] * scale : 0f;
                        float dz2 = d2 != null ? (float) d2[2] * scale : 0f;

                        defPos[di++] = (float) n1.x + dx1;
                        defPos[di++] = (float) n1.y + dy1;
                        defPos[di++] = (float) n1.z + dz1;

                        defPos[di++] = (float) n2.x + dx2;
                        defPos[di++] = (float) n2.y + dy2;
                        defPos[di++] = (float) n2.z + dz2;

                        for (int k = 0; k < 2; k++) {
                            defCol[dc++] = 0.0f; defCol[dc++] = 1.0f; defCol[dc++] = 0.5f; defCol[dc++] = 1.0f;
                        }
                    }
                }
            }

            Map<Integer, double[]> forces = res.getElementForces();
            if (forces != null && !forces.isEmpty()) {
                List<Float> diagLines = new ArrayList<>();
                List<Float> diagColors = new ArrayList<>();

                for (FrameElement elem : model.getElements()) {
                    StructuralNode n1 = model.getNode(elem.nodeI);
                    StructuralNode n2 = model.getNode(elem.nodeJ);
                    double[] f = forces.get(elem.id);
                    if (n1 != null && n2 != null && f != null) {
                        double M = f.length > 4 ? f[4] : (f.length > 2 ? f[2] : 0.0);
                        float offset = (float) (M * 0.005);

                        float L = (float) Math.hypot(n2.x - n1.x, n2.y - n1.y);
                        if (L > 1e-4) {
                            float nx = (float) (-(n2.y - n1.y) / L) * offset;
                            float ny = (float) ((n2.x - n1.x) / L) * offset;

                            diagLines.add((float) n1.x); diagLines.add((float) n1.y); diagLines.add((float) n1.z);
                            diagLines.add((float) n1.x + nx); diagLines.add((float) n1.y + ny); diagLines.add((float) n1.z);

                            diagLines.add((float) n1.x + nx); diagLines.add((float) n1.y + ny); diagLines.add((float) n1.z);
                            diagLines.add((float) n2.x + nx); diagLines.add((float) n2.y + ny); diagLines.add((float) n2.z);

                            diagLines.add((float) n2.x + nx); diagLines.add((float) n2.y + ny); diagLines.add((float) n2.z);
                            diagLines.add((float) n2.x); diagLines.add((float) n2.y); diagLines.add((float) n2.z);

                            for (int k = 0; k < 6; k++) {
                                diagColors.add(1.0f); diagColors.add(0.0f); diagColors.add(1.0f); diagColors.add(1.0f);
                            }
                        }
                    }
                }
                diagPos = new float[diagLines.size()];
                for (int k = 0; k < diagLines.size(); k++) diagPos[k] = diagLines.get(k);
                diagCol = new float[diagColors.size()];
                for (int k = 0; k < diagColors.size(); k++) diagCol[k] = diagColors.get(k);
            }
        }

        final float[] finalDefPos = defPos;
        final float[] finalDefCol = defCol;
        final float[] finalDiagPos = diagPos;
        final float[] finalDiagCol = diagCol;

        glSurfaceView.queueEvent(() -> {
            renderer.setNodes(nodePos, nodeCol);
            renderer.setElements(elemPos, elemCol);
            renderer.setLoads(loadPos, loadCol);
            if (finalDefPos != null) renderer.setDeformedShape(finalDefPos, finalDefCol);
            if (finalDiagPos != null) renderer.setDiagrams(finalDiagPos, finalDiagCol);
        });

        updateViewerOverlay();
    }

    private void updateViewerOverlay() {
        if (binding.tvViewerOverlay == null) return;
        int nodesCount = model.getNodes().size();
        int elemCount = model.getElements().size();
        AnalysisResult res = model.getResult();

        StringBuilder sb = new StringBuilder();
        sb.append("SAP2000 Structural Summary\n");
        sb.append(String.format("Nodes: %d | Elements: %d\n", nodesCount, elemCount));
        if (res != null) {
            sb.append(String.format(Locale.US, "Max Disp: %.4e m\n", res.getMaxDisplacement()));
            sb.append(String.format(Locale.US, "Max Moment: %.2f kN·m\n", res.getMaxMoment()));
            sb.append("Status: Calculated (OpenSees)");
        } else {
            sb.append("Status: Ready (Click CALCULAR)");
        }
        binding.tvViewerOverlay.setText(sb.toString());
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