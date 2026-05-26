package dao;

import org.json.JSONArray;
import org.json.JSONObject;
import util.GoogleConfig;
import util.GoogleTokenStorage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class GoogleClassroomClient {

    private final HttpClient httpClient;

    public GoogleClassroomClient() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Representa un curso de Google Classroom.
     */
    public static class Curso {
        private final String id;
        private final String nombre;
        private final String seccion;
        private final String estado;

        public Curso(String id, String nombre, String seccion, String estado) {
            this.id = id;
            this.nombre = nombre;
            this.seccion = seccion;
            this.estado = estado;
        }

        public String getId() { return id; }
        public String getNombre() { return nombre; }
        public String getSeccion() { return seccion; }
        public String getEstado() { return estado; }

        @Override
        public String toString() {
            return nombre + (seccion != null && !seccion.isEmpty() ? " - " + seccion : "");
        }
    }

    /**
     * Representa una tarea asignada en Google Classroom.
     */
    public static class TareaClassroom {
        private final String id;
        private final String titulo;
        private final String descripcion;
        private final String fechaEntrega; // formato ISO o null
        private final String estado;

        public TareaClassroom(String id, String titulo, String descripcion, String fechaEntrega, String estado) {
            this.id = id;
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.fechaEntrega = fechaEntrega;
            this.estado = estado;
        }

        public String getId() { return id; }
        public String getTitulo() { return titulo; }
        public String getDescripcion() { return descripcion; }
        public String getFechaEntrega() { return fechaEntrega; }
        public String getEstado() { return estado; }

        @Override
        public String toString() {
            return titulo + (fechaEntrega != null ? "  [" + fechaEntrega + "]" : "  [Sin fecha]");
        }
    }

    /**
     * Obtiene la lista de cursos activos del estudiante.
     */
    public List<Curso> listarCursos() {
        List<Curso> cursos = new ArrayList<>();

        try {
            String token = GoogleTokenStorage.obtenerAccessToken();
            if (token == null) {
                System.err.println("No hay access token disponible.");
                return cursos;
            }

            String url = GoogleConfig.CLASSROOM_BASE_URL + "/courses?courseStates=ACTIVE&pageSize=30";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                if (json.has("courses")) {
                    JSONArray coursesArray = json.getJSONArray("courses");
                    for (int i = 0; i < coursesArray.length(); i++) {
                        JSONObject c = coursesArray.getJSONObject(i);
                        cursos.add(new Curso(
                                c.getString("id"),
                                c.getString("name"),
                                c.optString("section", ""),
                                c.optString("courseState", "ACTIVE")
                        ));
                    }
                }
            } else {
                System.err.println("Error fetching courses: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return cursos;
    }

    /**
     * Obtiene las tareas asignadas de un curso especifico.
     */
    public List<TareaClassroom> listarTareasDeCurso(String courseId) {
        List<TareaClassroom> tareas = new ArrayList<>();

        try {
            String token = GoogleTokenStorage.obtenerAccessToken();
            if (token == null) {
                System.err.println("No hay access token disponible.");
                return tareas;
            }

            String url = GoogleConfig.CLASSROOM_BASE_URL + "/courses/" + courseId + "/courseWork?pageSize=50";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JSONObject json = new JSONObject(response.body());
                if (json.has("courseWork")) {
                    JSONArray workArray = json.getJSONArray("courseWork");
                    for (int i = 0; i < workArray.length(); i++) {
                        JSONObject w = workArray.getJSONObject(i);

                        // Extraer fecha de entrega si existe
                        String fechaEntrega = null;
                        if (w.has("dueDate")) {
                            JSONObject dueDate = w.getJSONObject("dueDate");
                            int year = dueDate.getInt("year");
                            int month = dueDate.getInt("month");
                            int day = dueDate.getInt("day");
                            fechaEntrega = String.format("%04d-%02d-%02d", year, month, day);
                        }

                        tareas.add(new TareaClassroom(
                                w.getString("id"),
                                w.getString("title"),
                                w.optString("description", ""),
                                fechaEntrega,
                                w.optString("state", "PUBLISHED")
                        ));
                    }
                }
            } else {
                System.err.println("Error fetching coursework: " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tareas;
    }
}
