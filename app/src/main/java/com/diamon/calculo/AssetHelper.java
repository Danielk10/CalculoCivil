package com.diamon.calculo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.system.Os;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetHelper {
    private static final String TAG = "AssetHelper";
    private static final String PREFS_NAME = "AssetHelperPrefs";
    private static final String KEY_EXTRACTED = "assets_extracted";
    private static final int BUFFER_SIZE = 8192;

    public static synchronized boolean ensureRuntimeReady(Context context) {
        File usrDir = new File(context.getFilesDir(), "usr");
        boolean alreadyExtracted = areAssetsExtracted(context);

        if (!alreadyExtracted) {
            if (!extractAssets(context, "data/data/com.diamon.calculo/files/usr", usrDir)) {
                return false;
            }
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            prefs.edit().putBoolean(KEY_EXTRACTED, true).apply();
        }

        return ensureNativeToolLinks(context);
    }

    public static boolean areAssetsExtracted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean flagged = prefs.getBoolean(KEY_EXTRACTED, false);
        File libDir = new File(context.getFilesDir(), "usr/lib");
        return flagged && libDir.exists();
    }

    private static boolean extractAssets(Context context, String assetPath, File destDir) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list(assetPath);
            if (files == null || files.length == 0) {
                return copyAssetFile(assetManager, assetPath, destDir);
            } else {
                if (!destDir.exists() && !destDir.mkdirs()) {
                    return false;
                }
                for (String fileName : files) {
                    if (fileName == null || fileName.isEmpty()) continue;
                    String childAssetPath = assetPath + "/" + fileName;
                    File childDestDir = new File(destDir, fileName);
                    String[] subFiles = assetManager.list(childAssetPath);
                    if (subFiles != null && subFiles.length > 0) {
                        if (!extractAssets(context, childAssetPath, childDestDir)) {
                            return false;
                        }
                    } else {
                        if (!copyAssetFile(assetManager, childAssetPath, destDir)) {
                            return false;
                        }
                    }
                }
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error extrayendo assets: " + e.getMessage());
            return false;
        }
    }

    private static boolean copyAssetFile(AssetManager assetManager, String assetPath, File destDir) {
        String fileName = assetPath.substring(assetPath.lastIndexOf('/') + 1);
        File destFile = new File(destDir, fileName);
        if (destFile.exists()) return true;

        if (!destDir.exists() && !destDir.mkdirs()) return false;

        try (InputStream in = assetManager.open(assetPath);
             OutputStream out = new FileOutputStream(destFile)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copiando " + assetPath + ": " + e.getMessage());
            return false;
        }
    }

    private static boolean ensureNativeToolLinks(Context context) {
        File filesDir = context.getFilesDir();
        File nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir);
        File usrBin = new File(filesDir, "usr/bin");
        File usrLib = new File(filesDir, "usr/lib");

        if (!usrBin.exists()) usrBin.mkdirs();
        if (!usrLib.exists()) usrLib.mkdirs();

        boolean ok = true;
        // Binaries
        ok &= linkTool(new File(usrBin, "OpenSees"), new File(nativeLibDir, "libOpenSees.so"));
        ok &= linkTool(new File(usrBin, "opensees.so"), new File(nativeLibDir, "libopensees.so"));
        ok &= linkTool(new File(usrBin, "tclsh"), new File(nativeLibDir, "libtclsh.so"));
        ok &= linkTool(new File(usrBin, "tclsh8.6"), new File(nativeLibDir, "libtclsh8_6.so"));
        ok &= linkTool(new File(usrBin, "wish"), new File(nativeLibDir, "libwish.so"));
        ok &= linkTool(new File(usrBin, "wish8.6"), new File(nativeLibDir, "libwish8_6.so"));
        ok &= linkTool(new File(usrBin, "h5cc"), new File(nativeLibDir, "libh5cc.so"));

        // Libraries
        ok &= linkTool(new File(usrLib, "libhdf5.so"), new File(nativeLibDir, "libhdf5.so"));
        ok &= linkTool(new File(usrLib, "libhdf5.so.1000"), new File(nativeLibDir, "libhdf5_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5.so.1000.0.0"), new File(nativeLibDir, "libhdf5_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so"), new File(nativeLibDir, "libhdf5_hl.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so.1000"), new File(nativeLibDir, "libhdf5_hl_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so.1000.0.0"), new File(nativeLibDir, "libhdf5_hl_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libopenblas.so"), new File(nativeLibDir, "libopenblas.so"));
        ok &= linkTool(new File(usrLib, "libopenblas.so.0"), new File(nativeLibDir, "libopenblas_so_0.so"));
        ok &= linkTool(new File(usrLib, "libopenblasp-r0.3.33.dev.so"), new File(nativeLibDir, "libopenblasp_r0_3_33_dev.so"));
        ok &= linkTool(new File(usrLib, "libtcl8.6.so"), new File(nativeLibDir, "libtcl8_6.so"));
        ok &= linkTool(new File(usrLib, "libtk8.6.so"), new File(nativeLibDir, "libtk8_6.so"));
        ok &= linkTool(new File(usrLib, "libz.so.1"), new File(nativeLibDir, "libz_so_1.so"));

        // X11 and Font Libraries
        ok &= linkTool(new File(usrLib, "libX11.so"), new File(nativeLibDir, "libX11.so"));
        ok &= linkTool(new File(usrLib, "libXext.so"), new File(nativeLibDir, "libXext.so"));
        ok &= linkTool(new File(usrLib, "libXft.so"), new File(nativeLibDir, "libXft.so"));
        ok &= linkTool(new File(usrLib, "libXss.so"), new File(nativeLibDir, "libXss.so"));
        ok &= linkTool(new File(usrLib, "libfontconfig.so"), new File(nativeLibDir, "libfontconfig.so"));
        ok &= linkTool(new File(usrLib, "libfreetype.so"), new File(nativeLibDir, "libfreetype.so"));

        return ok;
    }

    private static boolean linkTool(File linkPath, File target) {
        if (!target.exists()) {
            Log.e(TAG, "Libreria nativa faltante: " + target.getAbsolutePath());
            return false;
        }

        try {
            if (linkPath.exists() && linkPath.getCanonicalPath().equals(target.getCanonicalPath())) {
                return true;
            }
            linkPath.delete();
            Os.symlink(target.getAbsolutePath(), linkPath.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.w(TAG, "Symlink fallo para " + linkPath.getName() + " -> " + target.getName() + ": " + e.getMessage());
            // Fallback a copia si symlink falla
            return copyFile(target, linkPath);
        }
    }

    private static boolean copyFile(File source, File dest) {
        try (InputStream in = new java.io.FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            File binParent = dest.getParentFile();
            if (binParent != null && "bin".equals(binParent.getName())) {
                dest.setExecutable(true, true);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error copiando archivo de fallback: " + e.getMessage());
            return false;
        }
    }
}
