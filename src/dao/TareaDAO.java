package dao;

import dao.interfaces.IDAO;
import model.Tarea;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TareaDAO implements IDAO<Tarea> {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final String endpoint = "/rest/v1/tasks";

    @Override
    public boolean insertar(Tarea obj) {
        JSONObject json = new JSONObject();
        if (obj.getId() != null && !obj.getId().isEmpty()) json.put("id", obj.getId());
        json.put("title", obj.getTitulo());
        json.put("description", obj.getDescripcion());
        if (obj.getFechaLimite() != null) {
            json.put("dueDate", obj.getFechaLimite().toString());
        }
        json.put("isCompleted", obj.isCompletada());
        json.put("subjectId", obj.getMateriaId());

        String response = client.post(endpoint, json.toString());
        return response != null && !response.isEmpty();
    }

    @Override
    public List<Tarea> obtenerTodos() {
        String response = client.get(endpoint + "?select=*");
        List<Tarea> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }
    
    public List<Tarea> obtenerPorMateria(String subjectId) {
        String response = client.get(endpoint + "?subjectId=eq." + subjectId + "&select=*");
        List<Tarea> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    @Override
    public Tarea obtenerPorId(String id) {
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
    
    public boolean actualizarEstado(String id, boolean completada) {
        JSONObject json = new JSONObject();
        json.put("isCompleted", completada);
        String response = client.patch(endpoint + "?id=eq." + id, json.toString());
        return response != null && !response.isEmpty();
    }

    private Tarea parseJson(JSONObject json) {
        Tarea t = new Tarea();
        t.setId(json.optString("id"));
        t.setTitulo(json.optString("title"));
        t.setDescripcion(json.optString("description"));
        t.setCompletada(json.optBoolean("isCompleted"));
        t.setMateriaId(json.optString("subjectId"));
        
        String dueDate = json.optString("dueDate");
        if (dueDate != null && !dueDate.isEmpty() && !dueDate.equals("null")) {
            t.setFechaLimite(LocalDate.parse(dueDate.substring(0, 10)));
        }
        
        String createdAt = json.optString("createdAt");
        if (createdAt != null && !createdAt.isEmpty() && !createdAt.equals("null")) {
            t.setFechaCreacion(LocalDate.parse(createdAt.substring(0, 10)));
        }
        
        return t;
    }
}
