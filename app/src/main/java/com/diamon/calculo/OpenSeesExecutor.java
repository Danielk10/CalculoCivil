package com.diamon.calculo;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenSeesExecutor {
    private static final String TAG = "OpenSeesExecutor";
    private final File workDir;
    private final File nativeLibDir;

    public OpenSeesExecutor(Context context) {
        this.workDir = context.getFilesDir();
        this.nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir);
    }

    public String executeTclCommand(String tclCommand) {
        File openSeesBinary = new File(nativeLibDir, "libOpenSees.so");
        if (!openSeesBinary.exists()) {
            return "Error: No se encontro el binario " + openSeesBinary.getAbsolutePath();
        }

        List<String> command = new ArrayList<>();
        command.add(openSeesBinary.getAbsolutePath());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            pb.redirectErrorStream(true);

            Map<String, String> env = pb.environment();
            env.put("TCL_LIBRARY", new File(workDir, "usr/lib/tcl8.6").getAbsolutePath());
            env.put("LD_LIBRARY_PATH", new File(workDir, "usr/lib").getAbsolutePath() + ":" + nativeLibDir.getAbsolutePath());
            String path = env.get("PATH");
            String binPath = new File(workDir, "usr/bin").getAbsolutePath();
            env.put("PATH", binPath + ":" + nativeLibDir.getAbsolutePath() + (path != null ? ":" + path : ""));

            Process process = pb.start();
            
            // Send TCL command to stdin
            java.io.OutputStream out = process.getOutputStream();
            out.write((tclCommand + "\nexit\n").getBytes());
            out.flush();
            out.close();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            String result = output.toString().trim();
            StringBuilder fullLog = new StringBuilder();
            fullLog.append("> ").append(tclCommand).append("\n");
            if (!result.isEmpty()) {
                fullLog.append(result).append("\n");
            }
            fullLog.append("Codigo de salida: ").append(exitCode);
            
            return fullLog.toString().trim();

        } catch (Exception e) {
            Log.e(TAG, "Error ejecutando OpenSees: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }
}
