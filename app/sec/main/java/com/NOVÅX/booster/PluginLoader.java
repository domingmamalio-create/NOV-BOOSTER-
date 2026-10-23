package com.novax.booster;

import android.content.Context;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class PluginLoader {

    public static String importZip(Context ctx, Uri uri) {
        File tmp = new File(ctx.getCacheDir(), "plugin.zip");
        try (InputStream in = ctx.getContentResolver().openInputStream(uri);
             FileOutputStream out = new FileOutputStream(tmp)) {
            if (in == null) return "import failed - cannot read uri";
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        } catch (Exception e) {
            return "import failed - " + e.getMessage();
        }

        try (ZipFile zip = new ZipFile(tmp)) {
            ZipEntry man = zip.getEntry("manifest.json");
            if (man == null) return "import failed - no manifest.json";

            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(zip.getInputStream(man)))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
            String json = sb.toString();

            String id = extract(json, "id");
            String name = extract(json, "name");
            String type = extract(json, "type");

            if (id == null || id.isEmpty()) return "import failed - manifest missing id";

            File pluginsDir = new File(ctx.getFilesDir(), "plugins");
            pluginsDir.mkdirs();

            ZipEntry entry = zip.getEntry("profile.json");
            if (entry != null) {
                try (InputStream in = zip.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(
                             new File(pluginsDir, id + ".json"))) {
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
            }

            return "loaded: " + (name != null ? name : id) + " (" + (type != null ? type : "?") + ")";

        } catch (Exception e) {
            return "import failed - " + e.getMessage();
        }
    }

    private static String extract(String json, String key) {
        String pattern = "\"" + key + "\"";
        int i = json.indexOf(pattern);
        if (i < 0) return null;
        int colon = json.indexOf(':', i + pattern.length());
        if (colon < 0) return null;
        int quote1 = json.indexOf('"', colon + 1);
        if (quote1 < 0) return null;
        int quote2 = json.indexOf('"', quote1 + 1);
        if (quote2 < 0) return null;
        return json.substring(quote1 + 1, quote2);
    }
}
