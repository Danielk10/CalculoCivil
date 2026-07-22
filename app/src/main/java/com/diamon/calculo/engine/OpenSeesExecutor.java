package com.diamon.calculo.engine;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Executes OpenSees analysis via TCL and Python binaries.
 * Handles environment setup, process management, and output capture.
 */
public class OpenSeesExecutor {
    private static final String TAG = "OpenSeesExecutor";
    private final File workDir;
    private final File nativeLibDir;
    private final int numCores;

    public interface OnExecutionListener {
        void onOutput(String line);
        void onComplete(int exitCode, String fullOutput);
        void onError(String error);
    }

    public OpenSeesExecutor(Context context) {
        this.workDir = context.getFilesDir();
        this.nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir);
        this.numCores = Runtime.getRuntime().availableProcessors();
    }

    /** Execute a single TCL command interactively via stdin */
    public String executeTclCommand(String tclCommand) {
        File openSeesBinary = findOpenSeesBinary();
        if (openSeesBinary == null) {
            return "Error: OpenSees binary not found";
        }

        List<String> command = new ArrayList<>();
        command.add(openSeesBinary.getAbsolutePath());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            pb.redirectErrorStream(true);
            setupEnvironment(pb.environment());

            Process process = pb.start();

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
            fullLog.append("Exit code: ").append(exitCode);
            return fullLog.toString().trim();

        } catch (Exception e) {
            Log.e(TAG, "Error executing OpenSees: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /** Execute a TCL script file */
    public String executeTclScript(String scriptPath) {
        File openSeesBinary = findOpenSeesBinary();
        if (openSeesBinary == null) {
            return "Error: OpenSees binary not found";
        }

        File scriptFile = new File(scriptPath);
        if (!scriptFile.isAbsolute()) {
            scriptFile = new File(workDir, scriptPath);
        }
        if (!scriptFile.exists()) {
            return "Error: Script not found: " + scriptFile.getAbsolutePath();
        }

        List<String> command = new ArrayList<>();
        command.add(openSeesBinary.getAbsolutePath());
        command.add(scriptFile.getAbsolutePath());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(scriptFile.getParentFile());
            pb.redirectErrorStream(true);
            setupEnvironment(pb.environment());

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return output.toString().trim() + "\nExit code: " + exitCode;

        } catch (Exception e) {
            Log.e(TAG, "Error executing TCL script: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /** Execute a Python/OpenSeesPy script file */
    public String executePythonScript(String scriptPath) {
        File scriptFile = new File(scriptPath);
        if (!scriptFile.isAbsolute()) {
            scriptFile = new File(workDir, scriptPath);
        }
        if (!scriptFile.exists()) {
            return "Error: Script not found: " + scriptFile.getAbsolutePath();
        }

        // Find python binary
        File pythonBin = new File(nativeLibDir, "libpython3_11.so");
        if (!pythonBin.exists()) {
            // Try system python
            pythonBin = new File("/usr/bin/python3");
        }

        List<String> command = new ArrayList<>();
        command.add(pythonBin.getAbsolutePath());
        command.add(scriptFile.getAbsolutePath());

        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(scriptFile.getParentFile());
            pb.redirectErrorStream(true);
            setupEnvironment(pb.environment());

            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            int exitCode = process.waitFor();
            return output.toString().trim() + "\nExit code: " + exitCode;

        } catch (Exception e) {
            Log.e(TAG, "Error executing Python script: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    /** Execute TCL command asynchronously with listener callbacks */
    public void executeTclCommandAsync(final String tclCommand, final OnExecutionListener listener) {
        new Thread(() -> {
            File openSeesBinary = findOpenSeesBinary();
            if (openSeesBinary == null) {
                if (listener != null) listener.onError("OpenSees binary not found");
                return;
            }

            List<String> command = new ArrayList<>();
            command.add(openSeesBinary.getAbsolutePath());

            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.directory(workDir);
                pb.redirectErrorStream(true);
                setupEnvironment(pb.environment());

                Process process = pb.start();

                java.io.OutputStream out = process.getOutputStream();
                out.write((tclCommand + "\nexit\n").getBytes());
                out.flush();
                out.close();

                StringBuilder fullOutput = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        fullOutput.append(line).append("\n");
                        if (listener != null) listener.onOutput(line);
                    }
                }

                int exitCode = process.waitFor();
                if (listener != null) listener.onComplete(exitCode, fullOutput.toString());

            } catch (Exception e) {
                Log.e(TAG, "Async error: " + e.getMessage());
                if (listener != null) listener.onError(e.getMessage());
            }
        }).start();
    }

    /** Write script content to a temporary file and execute it */
    public String executeTclScriptContent(String scriptContent) {
        try {
            File tempScript = new File(workDir, "temp_analysis.tcl");
            FileWriter writer = new FileWriter(tempScript);
            writer.write(scriptContent);
            writer.close();
            return executeTclScript(tempScript.getAbsolutePath());
        } catch (Exception e) {
            return "Error writing temp script: " + e.getMessage();
        }
    }

    public String executePyScriptContent(String scriptContent) {
        try {
            File tempScript = new File(workDir, "temp_analysis.py");
            FileWriter writer = new FileWriter(tempScript);
            writer.write(scriptContent);
            writer.close();
            return executePythonScript(tempScript.getAbsolutePath());
        } catch (Exception e) {
            return "Error writing temp script: " + e.getMessage();
        }
    }

    private File findOpenSeesBinary() {
        // Try jniLibs location first
        File binary = new File(nativeLibDir, "libOpenSees.so");
        if (binary.exists()) return binary;

        // Try usr/bin symlink
        binary = new File(workDir, "usr/bin/OpenSees");
        if (binary.exists()) return binary;

        // Try libopensees_cli.so
        binary = new File(nativeLibDir, "libopensees_cli.so");
        if (binary.exists()) return binary;

        Log.e(TAG, "OpenSees binary not found in any expected location");
        return null;
    }

    private void setupEnvironment(Map<String, String> env) {
        // Multi-threading for solver
        String cores = String.valueOf(numCores);
        env.put("OMP_NUM_THREADS", cores);
        env.put("OPENBLAS_NUM_THREADS", cores);
        env.put("OPENBLAS_CORETYPE", "ARMV8"); // Fixes SIGILL (Exit code 132) on modern Androids
        env.put("MKL_NUM_THREADS", cores);
        env.put("OMP_STACKSIZE", "4M");

        // TCL library path
        File tclLib = new File(workDir, "usr/lib/tcl8.6");
        if (tclLib.exists()) {
            env.put("TCL_LIBRARY", tclLib.getAbsolutePath());
        }

        // Library paths
        String usrLib = new File(workDir, "usr/lib").getAbsolutePath();
        String ldPath = usrLib + ":" + nativeLibDir.getAbsolutePath();
        String existingLd = env.get("LD_LIBRARY_PATH");
        if (existingLd != null && !existingLd.isEmpty()) {
            ldPath += ":" + existingLd;
        }
        env.put("LD_LIBRARY_PATH", ldPath);

        // Python path for opensees.so
        env.put("PYTHONPATH", usrLib + ":" + nativeLibDir.getAbsolutePath());

        // PATH
        String binPath = new File(workDir, "usr/bin").getAbsolutePath();
        String path = env.get("PATH");
        env.put("PATH", binPath + ":" + nativeLibDir.getAbsolutePath() +
                (path != null ? ":" + path : ""));
    }

    public File getWorkDir() {
        return workDir;
    }

    public int getNumCores() {
        return numCores;
    }
}
