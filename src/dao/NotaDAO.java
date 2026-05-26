package dao;

import dao.interfaces.IDAO;
import model.Nota;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NotaDAO implements IDAO<Nota> {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final String endpoint = "/rest/v1/notes";

    @Override
    public boolean insertar(Nota obj) {
        JSONObject json = new JSONObject();
        if (obj.getId() != null && !obj.getId().isEmpty()) json.put("id", obj.getId());
        json.put("title", obj.getTitulo());
        json.put("content", obj.getContenido());
        json.put("subjectId", obj.getMateriaId());

        String response = client.post(endpoint, json.toString());
        return response != null && !response.isEmpty();
    }

    @Override
    public List<Nota> obtenerTodos() {
        String response = client.get(endpoint + "?select=*");
        List<Nota> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    public List<Nota> obtenerPorMateria(String subjectId) {
        String response = client.get(endpoint + "?subjectId=eq." + subjectId + "&select=*");
        List<Nota> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    @Override
    public Nota obtenerPorId(String id) {
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

    private Nota parseJson(JSONObject json) {
        Nota n = new Nota();
        n.setId(json.optString("id"));
        n.setTitulo(json.optString("title"));
        n.setContenido(json.optString("content"));
        n.setMateriaId(json.optString("subjectId"));
        
        String createdAt = json.optString("createdAt");
        if (createdAt != null && !createdAt.isEmpty() && !createdAt.equals("null")) {
            n.setFechaCreacion(LocalDate.parse(createdAt.substring(0, 10)));
        }
        return n;
    }
}
