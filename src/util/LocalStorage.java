package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class LocalStorage {
    
    private static final String CONFIG_FILE = "session.properties";
    private static final String KEY_EMAIL = "saved_email";

    public static void guardarSesion(String email) {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_EMAIL, email);
            FileOutputStream fos = new FileOutputStream(new File(CONFIG_FILE));
            props.store(fos, "NanoGraph Session");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String obtenerSesionGuardada() {
        try {
            File file = new File(CONFIG_FILE);
            if (!file.exists()) return null;
            
            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(file);
            props.load(fis);
            fis.close();
            
            return props.getProperty(KEY_EMAIL);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void limpiarSesion() {
        try {
            File file = new File(CONFIG_FILE);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
