package com.diamon.calculo;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.diamon.calculo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private OpenSeesExecutor openSeesExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        openSeesExecutor = new OpenSeesExecutor(this);

        // Configurar pestañas
        binding.btnTabBasic.setOnClickListener(v -> {
            binding.layoutBasicUI.setVisibility(View.VISIBLE);
            binding.layoutConsole.setVisibility(View.GONE);
        });

        binding.btnTabConsole.setOnClickListener(v -> {
            binding.layoutBasicUI.setVisibility(View.GONE);
            binding.layoutConsole.setVisibility(View.VISIBLE);
        });

        // Configurar Log copiable
        binding.tvLog.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("OpenSees Log", binding.tvLog.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Log copiado al portapapeles", Toast.LENGTH_SHORT).show();
        });

        // Configurar botón UI básica
        binding.btnTestSum.setOnClickListener(v -> {
            binding.tvBasicResult.setText("Calculando...");
            executor.execute(() -> {
                String result = openSeesExecutor.executeTclCommand("expr 2 + 2");
                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.tvBasicResult.setText("Resultado: \n" + result);
                });
            });
        });

        // Configurar Consola
        binding.btnSend.setOnClickListener(v -> {
            String cmd = binding.etCommand.getText().toString();
            if (cmd.trim().isEmpty()) return;
            
            binding.etCommand.setText("");
            binding.tvLog.append("\nEjecutando: " + cmd + "\n");
            scrollLogDown();
            
            executor.execute(() -> {
                String result = openSeesExecutor.executeTclCommand(cmd);
                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.tvLog.append(result + "\n");
                    scrollLogDown();
                });
            });
        });

        checkAndLoadAssets();
    }
    
    private void scrollLogDown() {
        binding.scrollLog.post(() -> binding.scrollLog.fullScroll(View.FOCUS_DOWN));
    }

    private void checkAndLoadAssets() {
        boolean ready = AssetHelper.areAssetsExtracted(this);
        if (ready) {
            // Ya extraidos, solo asegurar links en background
            binding.layoutLoading.setVisibility(View.GONE);
            binding.layoutMainUI.setVisibility(View.VISIBLE);
            executor.execute(() -> {
                AssetHelper.ensureRuntimeReady(MainActivity.this);
            });
        } else {
            // Mostrar loading
            binding.layoutLoading.setVisibility(View.VISIBLE);
            binding.layoutMainUI.setVisibility(View.GONE);
            binding.tvLoadingText.setText("Copiando y enlazando binarios de OpenSees...");
            
            executor.execute(() -> {
                boolean success = AssetHelper.ensureRuntimeReady(MainActivity.this);
                new Handler(Looper.getMainLooper()).post(() -> {
                    binding.layoutLoading.setVisibility(View.GONE);
                    binding.layoutMainUI.setVisibility(View.VISIBLE);
                    if (success) {
                        Toast.makeText(MainActivity.this, "Binarios configurados con éxito", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error al configurar binarios", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }
}