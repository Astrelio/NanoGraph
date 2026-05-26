package util;

public class SupabaseConfig {
    // Load from environment variables or .env file
    public static final String SUPABASE_URL;
    public static final String SUPABASE_KEY;

    static {
        // Try environment variables first, then fall back to .env file
        String url = System.getenv("SUPABASE_URL");
        String key = System.getenv("SUPABASE_KEY");

        if (url == null || key == null) {
            // Try loading from .env file in working directory
            try {
                java.io.File envFile = new java.io.File(".env");
                if (envFile.exists()) {
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(envFile));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#")) continue;
                        int eq = line.indexOf('=');
                        if (eq > 0) {
                            String k = line.substring(0, eq).trim();
                            String v = line.substring(eq + 1).trim();
                            if (k.equals("SUPABASE_URL") && url == null) url = v;
                            if (k.equals("SUPABASE_KEY") && key == null) key = v;
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not read .env file: " + e.getMessage());
            }
        }

        if (url == null || key == null) {
            System.err.println("ERROR: SUPABASE_URL and SUPABASE_KEY must be set via environment variables or a .env file.");
        }

        SUPABASE_URL = url != null ? url : "";
        SUPABASE_KEY = key != null ? key : "";
    }
}
