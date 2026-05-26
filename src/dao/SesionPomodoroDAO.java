package dao;

import dao.interfaces.IDAO;
import model.SesionPomodoro;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SesionPomodoroDAO implements IDAO<SesionPomodoro> {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final String endpoint = "/rest/v1/studySessions";

    @Override
    public boolean insertar(SesionPomodoro obj) {
        JSONObject json = new JSONObject();
        if (obj.getId() != null && !obj.getId().isEmpty()) json.put("id", obj.getId());
        json.put("durationMinutes", obj.getDuracionMin());
        json.put("subjectId", obj.getMateriaId());
        
        // Se omite sessionDate para que Postgres use DEFAULT NOW() y evitar errores de formato TIMESTAMP WITH TIME ZONE
        
        String response = client.post(endpoint, json.toString());
        return response != null && !response.isEmpty();
    }

    @Override
    public List<SesionPomodoro> obtenerTodos() {
        String response = client.get(endpoint + "?select=*");
        List<SesionPomodoro> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }
    
    public List<SesionPomodoro> obtenerPorMateria(String subjectId) {
        String response = client.get(endpoint + "?subjectId=eq." + subjectId + "&select=*");
        List<SesionPomodoro> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    @Override
    public SesionPomodoro obtenerPorId(String id) {
        String response = client.get(endpoint + "?id=eq." + id + "&select=*");
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            if (array.length() > 0) {
                return parseJson(array.getJSONObject(0));
            }
        }
        return null;
    }

    @Override
    public boolean eliminar(String id) {
        return client.delete(endpoint + "?id=eq." + id);
    }

    private SesionPomodoro parseJson(JSONObject json) {
        SesionPomodoro s = new SesionPomodoro();
        s.setId(json.optString("id"));
        s.setDuracionMin(json.optInt("durationMinutes"));
        s.setMateriaId(json.optString("subjectId"));
        
        String sessionDate = json.optString("sessionDate");
        if (sessionDate != null && !sessionDate.isEmpty() && !sessionDate.equals("null")) {
            s.setFecha(LocalDate.parse(sessionDate.substring(0, 10)));
        }
        return s;
    }
}
