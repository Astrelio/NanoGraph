package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class GoogleTokenStorage {

    private static final String TOKEN_FILE = "google_tokens.properties";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";

    public static void guardarTokens(String accessToken, String refreshToken, long expiresInSeconds) {
        try {
            Properties props = new Properties();
            props.setProperty(KEY_ACCESS_TOKEN, accessToken);
            if (refreshToken != null) {
                props.setProperty(KEY_REFRESH_TOKEN, refreshToken);
            }
            long expiresAt = System.currentTimeMillis() + (expiresInSeconds * 1000);
            props.setProperty(KEY_EXPIRES_AT, String.valueOf(expiresAt));

            FileOutputStream fos = new FileOutputStream(new File(TOKEN_FILE));
            props.store(fos, "NanoGraph Google OAuth Tokens");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String obtenerAccessToken() {
        try {
            File file = new File(TOKEN_FILE);
            if (!file.exists()) return null;

            Properties props = new Properties();
            FileInputStream fis = new FileInputStream(file);
            props.load(fis);
            fis.close();

            String expiresAtStr = props.getProperty(KEY_EXPIRES_AT);
            if (expiresAtStr != null) {
                long expiresAt = Long.parseLong(expiresAtStr);
                if (System.currentTimeMillis() >= expiresAt) {
                    // Token expirado, intentar refrescar
                    String refreshToken = props.getProperty(KEY_REFRESH_TOKEN);
                    if (refreshToken != null) {
                        return refrescarToken(refreshToken);
                    }
                    return null;
                }
            }

            return props.getProperty(KEY_ACCESS_TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean tieneTokens() {
        File file = new File(TOKEN_FILE);
        if (!file.exists()) return false;

        // Verificar que el archivo tiene contenido válido
        String token = obtenerAccessToken();
        return token != null && !token.isEmpty();
    }

    private static String refrescarToken(String refreshToken) {
        try {
            String body = "client_id=" + java.net.URLEncoder.encode(GoogleConfig.CLIENT_ID, "UTF-8")
                    + "&client_secret=" + java.net.URLEncoder.encode(GoogleConfig.CLIENT_SECRET, "UTF-8")
                    + "&refresh_token=" + java.net.URLEncoder.encode(refreshToken, "UTF-8")
                    + "&grant_type=refresh_token";

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(GoogleConfig.TOKEN_URI))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                    .build();

            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                org.json.JSONObject json = new org.json.JSONObject(response.body());
                String newAccessToken = json.getString("access_token");
                long expiresIn = json.getLong("expires_in");

                // Guardar el nuevo token (mantener el mismo refresh token)
                guardarTokens(newAccessToken, refreshToken, expiresIn);
                System.out.println("Google access token refreshed successfully.");
                return newAccessToken;
            } else {
                System.err.println("Error refreshing Google token: " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void limpiarTokens() {
        try {
            File file = new File(TOKEN_FILE);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
