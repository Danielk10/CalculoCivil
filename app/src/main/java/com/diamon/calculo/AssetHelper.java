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

        // === Binarios principales ===
        ok &= linkTool(new File(usrBin, "OpenSees"), new File(nativeLibDir, "libOpenSees.so"));
        ok &= linkTool(new File(usrBin, "opensees.so"), new File(nativeLibDir, "libopensees.so"));
        ok &= linkTool(new File(usrBin, "tclsh"), new File(nativeLibDir, "libtclsh.so"));
        ok &= linkTool(new File(usrBin, "tclsh8.6"), new File(nativeLibDir, "libtclsh8_6.so"));
        ok &= linkTool(new File(usrBin, "wish"), new File(nativeLibDir, "libwish.so"));
        ok &= linkTool(new File(usrBin, "wish8.6"), new File(nativeLibDir, "libwish8_6.so"));

        // === Herramientas HDF5 ===
        ok &= linkTool(new File(usrBin, "h5clear"), new File(nativeLibDir, "libh5clear.so"));
        ok &= linkTool(new File(usrBin, "h5copy"), new File(nativeLibDir, "libh5copy.so"));
        ok &= linkTool(new File(usrBin, "h5debug"), new File(nativeLibDir, "libh5debug.so"));
        ok &= linkTool(new File(usrBin, "h5delete"), new File(nativeLibDir, "libh5delete.so"));
        ok &= linkTool(new File(usrBin, "h5diff"), new File(nativeLibDir, "libh5diff.so"));
        ok &= linkTool(new File(usrBin, "h5dump"), new File(nativeLibDir, "libh5dump.so"));
        ok &= linkTool(new File(usrBin, "h5format_convert"), new File(nativeLibDir, "libh5format_convert.so"));
        ok &= linkTool(new File(usrBin, "h5import"), new File(nativeLibDir, "libh5import.so"));
        ok &= linkTool(new File(usrBin, "h5jam"), new File(nativeLibDir, "libh5jam.so"));
        ok &= linkTool(new File(usrBin, "h5ls"), new File(nativeLibDir, "libh5ls.so"));
        ok &= linkTool(new File(usrBin, "h5mkgrp"), new File(nativeLibDir, "libh5mkgrp.so"));
        ok &= linkTool(new File(usrBin, "h5perf_serial"), new File(nativeLibDir, "libh5perf_serial.so"));
        ok &= linkTool(new File(usrBin, "h5repack"), new File(nativeLibDir, "libh5repack.so"));
        ok &= linkTool(new File(usrBin, "h5repart"), new File(nativeLibDir, "libh5repart.so"));
        ok &= linkTool(new File(usrBin, "h5stat"), new File(nativeLibDir, "libh5stat.so"));
        ok &= linkTool(new File(usrBin, "h5unjam"), new File(nativeLibDir, "libh5unjam.so"));
        ok &= linkTool(new File(usrBin, "h5watch"), new File(nativeLibDir, "libh5watch.so"));

        // === Librerías HDF5 ===
        ok &= linkTool(new File(usrLib, "libhdf5.so"), new File(nativeLibDir, "libhdf5.so"));
        ok &= linkTool(new File(usrLib, "libhdf5.so.1000"), new File(nativeLibDir, "libhdf5_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5.so.1000.0.0"), new File(nativeLibDir, "libhdf5_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so"), new File(nativeLibDir, "libhdf5_hl.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so.1000"), new File(nativeLibDir, "libhdf5_hl_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl.so.1000.0.0"), new File(nativeLibDir, "libhdf5_hl_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_cpp.so"), new File(nativeLibDir, "libhdf5_cpp.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_cpp.so.1000"), new File(nativeLibDir, "libhdf5_cpp_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_cpp.so.1000.0.0"), new File(nativeLibDir, "libhdf5_cpp_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_tools.so"), new File(nativeLibDir, "libhdf5_tools.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_tools.so.1000"), new File(nativeLibDir, "libhdf5_tools_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_tools.so.1000.0.0"), new File(nativeLibDir, "libhdf5_tools_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_f90cstub.so"), new File(nativeLibDir, "libhdf5_f90cstub.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_f90cstub.so.1000"), new File(nativeLibDir, "libhdf5_f90cstub_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_f90cstub.so.1000.0.0"), new File(nativeLibDir, "libhdf5_f90cstub_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_fortran.so"), new File(nativeLibDir, "libhdf5_fortran.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_fortran.so.1000"), new File(nativeLibDir, "libhdf5_fortran_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_fortran.so.1000.0.0"), new File(nativeLibDir, "libhdf5_fortran_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_cpp.so"), new File(nativeLibDir, "libhdf5_hl_cpp.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_cpp.so.1000"), new File(nativeLibDir, "libhdf5_hl_cpp_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_cpp.so.1000.0.0"), new File(nativeLibDir, "libhdf5_hl_cpp_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_f90cstub.so"), new File(nativeLibDir, "libhdf5_hl_f90cstub.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_f90cstub.so.1000"), new File(nativeLibDir, "libhdf5_hl_f90cstub_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_f90cstub.so.1000.0.0"), new File(nativeLibDir, "libhdf5_hl_f90cstub_so_1000_0_0.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_fortran.so"), new File(nativeLibDir, "libhdf5_hl_fortran.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_fortran.so.1000"), new File(nativeLibDir, "libhdf5_hl_fortran_so_1000.so"));
        ok &= linkTool(new File(usrLib, "libhdf5_hl_fortran.so.1000.0.0"), new File(nativeLibDir, "libhdf5_hl_fortran_so_1000_0_0.so"));

        // === OpenBLAS ===
        ok &= linkTool(new File(usrLib, "libopenblas.so"), new File(nativeLibDir, "libopenblas.so"));
        ok &= linkTool(new File(usrLib, "libopenblas.so.0"), new File(nativeLibDir, "libopenblas_so_0.so"));
        ok &= linkTool(new File(usrLib, "libopenblasp-r0.3.34.dev.so"), new File(nativeLibDir, "libopenblasp_r0_3_34_dev.so"));

        // === Tcl/Tk ===
        ok &= linkTool(new File(usrLib, "libtcl8.6.so"), new File(nativeLibDir, "libtcl8_6.so"));
        ok &= linkTool(new File(usrLib, "libtk8.6.so"), new File(nativeLibDir, "libtk8_6.so"));

        // === Zlib ===
        ok &= linkTool(new File(usrLib, "libz.so"), new File(nativeLibDir, "libz.so"));
        ok &= linkTool(new File(usrLib, "libz.so.1"), new File(nativeLibDir, "libz_so_1.so"));
        ok &= linkTool(new File(usrLib, "libz.so.1.3.2"), new File(nativeLibDir, "libz_so_1_3_2.so"));

        // === Python ===
        ok &= linkTool(new File(usrLib, "libpython3.11.so"), new File(nativeLibDir, "libpython3_11.so"));
        ok &= linkTool(new File(usrLib, "libpython3.11.so.1.0"), new File(nativeLibDir, "libpython3_11_so_1_0.so"));

        // === AEC / SZlib ===
        ok &= linkTool(new File(usrLib, "libaec.so"), new File(nativeLibDir, "libaec.so"));
        ok &= linkTool(new File(usrLib, "libaec.so.0"), new File(nativeLibDir, "libaec_so_0.so"));
        ok &= linkTool(new File(usrLib, "libaec.so.0.1.7"), new File(nativeLibDir, "libaec_so_0_1_7.so"));
        ok &= linkTool(new File(usrLib, "libsz.so"), new File(nativeLibDir, "libsz.so"));
        ok &= linkTool(new File(usrLib, "libsz.so.2"), new File(nativeLibDir, "libsz_so_2.so"));
        ok &= linkTool(new File(usrLib, "libsz.so.2.0.1"), new File(nativeLibDir, "libsz_so_2_0_1.so"));

        // === Bzip2 ===
        ok &= linkTool(new File(usrLib, "libbz2.so"), new File(nativeLibDir, "libbz2.so"));
        ok &= linkTool(new File(usrLib, "libbz2.so.1.0"), new File(nativeLibDir, "libbz2_so_1_0.so"));
        ok &= linkTool(new File(usrLib, "libbz2.so.1.0.8"), new File(nativeLibDir, "libbz2_so_1_0_8.so"));

        // === Expat ===
        ok &= linkTool(new File(usrLib, "libexpat.so"), new File(nativeLibDir, "libexpat.so"));
        ok &= linkTool(new File(usrLib, "libexpat.so.1"), new File(nativeLibDir, "libexpat_so_1.so"));
        ok &= linkTool(new File(usrLib, "libexpat.so.1.12.2"), new File(nativeLibDir, "libexpat_so_1_12_2.so"));

        // === Brotli ===
        ok &= linkTool(new File(usrLib, "libbrotlicommon.so"), new File(nativeLibDir, "libbrotlicommon.so"));
        ok &= linkTool(new File(usrLib, "libbrotlidec.so"), new File(nativeLibDir, "libbrotlidec.so"));

        // === PNG ===
        ok &= linkTool(new File(usrLib, "libpng16.so"), new File(nativeLibDir, "libpng16.so"));

        // === X11 y Fuentes ===
        ok &= linkTool(new File(usrLib, "libX11.so"), new File(nativeLibDir, "libX11.so"));
        ok &= linkTool(new File(usrLib, "libX11.so.6"), new File(nativeLibDir, "libX11_so_6.so"));
        ok &= linkTool(new File(usrLib, "libXau.so"), new File(nativeLibDir, "libXau.so"));
        ok &= linkTool(new File(usrLib, "libxcb.so"), new File(nativeLibDir, "libxcb.so"));
        ok &= linkTool(new File(usrLib, "libXdmcp.so"), new File(nativeLibDir, "libXdmcp.so"));
        ok &= linkTool(new File(usrLib, "libXext.so"), new File(nativeLibDir, "libXext.so"));
        ok &= linkTool(new File(usrLib, "libXft.so"), new File(nativeLibDir, "libXft.so"));
        ok &= linkTool(new File(usrLib, "libXrender.so"), new File(nativeLibDir, "libXrender.so"));
        ok &= linkTool(new File(usrLib, "libXss.so"), new File(nativeLibDir, "libXss.so"));
        ok &= linkTool(new File(usrLib, "libfontconfig.so"), new File(nativeLibDir, "libfontconfig.so"));
        ok &= linkTool(new File(usrLib, "libfreetype.so"), new File(nativeLibDir, "libfreetype.so"));

        // === Android Support ===
        ok &= linkTool(new File(usrLib, "libandroid-support.so"), new File(nativeLibDir, "libandroid_support.so"));

        // === C++ Standard Library ===
        ok &= linkTool(new File(usrLib, "libc++_shared.so"), new File(nativeLibDir, "libc++_shared.so"));

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
