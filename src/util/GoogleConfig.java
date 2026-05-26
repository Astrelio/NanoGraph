package util;

public class GoogleConfig {
    public static final String CLIENT_ID;
    public static final String CLIENT_SECRET;
    public static final String REDIRECT_URI = "http://localhost:8085/callback";
    public static final String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    public static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    public static final String CLASSROOM_BASE_URL = "https://classroom.googleapis.com/v1";
    public static final String SCOPES = "https://www.googleapis.com/auth/classroom.courses.readonly https://www.googleapis.com/auth/classroom.coursework.me.readonly";

    static {
        String id = System.getenv("GOOGLE_CLIENT_ID");
        String secret = System.getenv("GOOGLE_CLIENT_SECRET");

        if (id == null || secret == null) {
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
                            if (k.equals("GOOGLE_CLIENT_ID") && id == null) id = v;
                            if (k.equals("GOOGLE_CLIENT_SECRET") && secret == null) secret = v;
                        }
                    }
                    reader.close();
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not read .env file for Google config: " + e.getMessage());
            }
        }

        if (id == null || secret == null) {
            System.err.println("WARNING: GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET not configured. Google Classroom integration will not work.");
        }

        CLIENT_ID = id != null ? id : "";
        CLIENT_SECRET = secret != null ? secret : "";
    }
}
