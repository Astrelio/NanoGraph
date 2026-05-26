package dao;

import util.SupabaseConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseClient {
    private static SupabaseClient instance;
    private final HttpClient httpClient;

    private SupabaseClient() {
        this.httpClient = HttpClient.newBuilder().build();
    }

    public static SupabaseClient getInstance() {
        if (instance == null) {
            instance = new SupabaseClient();
        }
        return instance;
    }

    private HttpRequest.Builder createRequestBuilder(String endpoint) {
        String url = SupabaseConfig.SUPABASE_URL + endpoint;
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", SupabaseConfig.SUPABASE_KEY)
                .header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation");
    }

    public String get(String endpoint) {
        try {
            HttpRequest request = createRequestBuilder(endpoint).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String post(String endpoint, String body) {
        try {
            HttpRequest request = createRequestBuilder(endpoint)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                System.err.println("Error POST " + endpoint + ": " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String patch(String endpoint, String body) {
        try {
            HttpRequest request = createRequestBuilder(endpoint)
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            } else {
                System.err.println("Error PATCH " + endpoint + ": " + response.statusCode() + " - " + response.body());
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(String endpoint) {
        try {
            HttpRequest request = createRequestBuilder(endpoint).DELETE().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
