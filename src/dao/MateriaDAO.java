package dao;

import dao.interfaces.IDAO;
import model.Materia;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MateriaDAO implements IDAO<Materia> {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final String endpoint = "/rest/v1/subjects";

    @Override
    public boolean insertar(Materia obj) {
        JSONObject json = new JSONObject();
        if (obj.getId() != null) json.put("id", obj.getId());
        json.put("name", obj.getNombre());
        json.put("color", obj.getColor());
        json.put("userId", obj.getUsuarioId());

        String response = client.post(endpoint, json.toString());
        return response != null && !response.isEmpty();
    }

    @Override
    public List<Materia> obtenerTodos() {
        String response = client.get(endpoint + "?select=*");
        List<Materia> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    public List<Materia> obtenerPorUsuario(String userId) {
        String response = client.get(endpoint + "?userId=eq." + userId + "&select=*");
        List<Materia> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    @Override
    public Materia obtenerPorId(String id) {
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

    private Materia parseJson(JSONObject json) {
        Materia m = new Materia();
        m.setId(json.optString("id"));
        m.setNombre(json.optString("name"));
        m.setColor(json.optString("color"));
        m.setUsuarioId(json.optString("userId"));
        return m;
    }
}
