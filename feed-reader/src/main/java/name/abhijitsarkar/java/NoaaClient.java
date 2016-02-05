package name.abhijitsarkar.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * @author Abhijit Sarkar
 */
public class NoaaClient {
    private static final File BASEDIR = new File(System.getProperty("java.io.tmpdir"), "current_conditions");

    public static String currentConditionsPath(boolean overwrite) {
        if ((BASEDIR.exists()) && !overwrite) return BASEDIR.getAbsolutePath();

        BASEDIR.delete();
        BASEDIR.mkdirs();

        File zipFile = download();
        extract(zipFile);
        zipFile.delete();

        return BASEDIR.getAbsolutePath();
    }

    private static File download() {
        try {
            URL url = new URL("http://w1.weather.gov/xml/current_obs/all_xml.zip");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(10 * 1000); // 10 s

            File dest = new File(BASEDIR, "cc.zip");
            InputStream is = conn.getInputStream();
            Files.copy(is, dest.toPath());

            is.close();

            return dest;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void extract(File src) {
        try (ZipFile zipFile = new ZipFile(src)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();

                if (e.isDirectory()) {
                    new File(BASEDIR, e.getName()).mkdirs();
                } else {
                    Files.copy(zipFile.getInputStream(e), new File(BASEDIR, e.getName()).toPath(), REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
