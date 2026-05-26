package util;

import com.sun.net.httpserver.HttpServer;
import java.awt.Desktop;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class GoogleAuthService {

    private static HttpServer callbackServer;

    /**
     * Inicia el flujo OAuth 2.0: abre el navegador para que el usuario autorice,
     * y devuelve un Future que se completa cuando el usuario termina el flujo.
     * El Future devuelve true si la autorizacion fue exitosa.
     */
    public static CompletableFuture<Boolean> iniciarAutorizacion() {
        CompletableFuture<Boolean> resultado = new CompletableFuture<>();

        try {
            // Detener el servidor anterior si se quedó corriendo (ej: el usuario cerró el navegador sin terminar)
            if (callbackServer != null) {
                try {
                    callbackServer.stop(0);
                    callbackServer = null;
                } catch (Exception e) {}
            }

            // 1. Levantar servidor local para recibir el callback
            callbackServer = HttpServer.create(new InetSocketAddress(8085), 0);

            callbackServer.createContext("/callback", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String code = null;

                    if (query != null) {
                        for (String param : query.split("&")) {
                            String[] pair = param.split("=", 2);
                            if (pair.length == 2 && pair[0].equals("code")) {
                                code = java.net.URLDecoder.decode(pair[1], "UTF-8");
                            }
                        }
                    }

                    if (code != null) {
                        // Mostrar pagina de exito en el navegador
                        String html = "<html><body style='background:#1E1E2E;color:#FDE047;font-family:monospace;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;'>"
                                + "<div style='text-align:center;'>"
                                + "<h1>NanoGraph</h1>"
                                + "<p style='color:#A78BFA;font-size:18px;'>Conexion exitosa con Google Classroom.</p>"
                                + "<p style='color:#F5F5F7;'>Puedes cerrar esta ventana y volver a la aplicacion.</p>"
                                + "</div></body></html>";

                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        byte[] responseBytes = html.getBytes("UTF-8");
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                        exchange.getResponseBody().close();

                        // Intercambiar el codigo por tokens
                        boolean exito = intercambiarCodigoPorTokens(code);
                        resultado.complete(exito);
                    } else {
                        String html = "<html><body style='background:#1E1E2E;color:#e74c3c;font-family:monospace;display:flex;justify-content:center;align-items:center;height:100vh;margin:0;'>"
                                + "<h1>Error de autorizacion</h1></body></html>";

                        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                        byte[] responseBytes = html.getBytes("UTF-8");
                        exchange.sendResponseHeaders(200, responseBytes.length);
                        exchange.getResponseBody().write(responseBytes);
                        exchange.getResponseBody().close();

                        resultado.complete(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    resultado.complete(false);
                } finally {
                    // Apagar el servidor despues de un momento
                    new Thread(() -> {
                        try { Thread.sleep(2000); } catch (Exception ignored) {}
                        callbackServer.stop(0);
                    }).start();
                }
            });

            callbackServer.setExecutor(null);
            callbackServer.start();
            System.out.println("Callback server started on port 8085");

            // 2. Construir la URL de autorizacion y abrir el navegador
            String authUrl = GoogleConfig.AUTH_URI
                    + "?client_id=" + URLEncoder.encode(GoogleConfig.CLIENT_ID, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(GoogleConfig.REDIRECT_URI, "UTF-8")
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode(GoogleConfig.SCOPES, "UTF-8")
                    + "&access_type=offline"
                    + "&prompt=consent";

            System.out.println("Opening browser for Google authorization...");

            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(authUrl));
            } else {
                // Fallback para sistemas sin Desktop support
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + authUrl);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultado.complete(false);
        }

        return resultado;
    }

    private static boolean intercambiarCodigoPorTokens(String code) {
        try {
            String body = "code=" + URLEncoder.encode(code, "UTF-8")
                    + "&client_id=" + URLEncoder.encode(GoogleConfig.CLIENT_ID, "UTF-8")
                    + "&client_secret=" + URLEncoder.encode(GoogleConfig.CLIENT_SECRET, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(GoogleConfig.REDIRECT_URI, "UTF-8")
                    + "&grant_type=authorization_code";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GoogleConfig.TOKEN_URI))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                org.json.JSONObject json = new org.json.JSONObject(response.body());
                String accessToken = json.getString("access_token");
                String refreshToken = json.optString("refresh_token", null);
                long expiresIn = json.getLong("expires_in");

                GoogleTokenStorage.guardarTokens(accessToken, refreshToken, expiresIn);
                System.out.println("Google OAuth tokens saved successfully.");
                return true;
            } else {
                System.err.println("Error exchanging code for tokens: " + response.statusCode() + " - " + response.body());
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
