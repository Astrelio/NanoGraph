package dao;

import dao.interfaces.IDAO;
import model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO implements IDAO<Usuario> {

    private final SupabaseClient client = SupabaseClient.getInstance();
    private final String endpoint = "/rest/v1/users";

    @Override
    public boolean insertar(Usuario obj) {
        JSONObject json = new JSONObject();
        if (obj.getId() != null) json.put("id", obj.getId());
        json.put("name", obj.getNombre());
        json.put("email", obj.getEmail());

        String response = client.post(endpoint, json.toString());
        return response != null && !response.isEmpty();
    }

    @Override
    public List<Usuario> obtenerTodos() {
        String response = client.get(endpoint + "?select=*");
        List<Usuario> list = new ArrayList<>();
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            for (int i = 0; i < array.length(); i++) {
                list.add(parseJson(array.getJSONObject(i)));
            }
        }
        return list;
    }

    @Override
    public Usuario obtenerPorId(String id) {
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

    public Usuario obtenerPorEmail(String email) {
        String response = client.get(endpoint + "?email=eq." + email + "&select=*");
        if (response != null && !response.isEmpty()) {
            JSONArray array = new JSONArray(response);
            if (array.length() > 0) {
                return parseJson(array.getJSONObject(0));
            }
        }
        return null;
    }

    private Usuario parseJson(JSONObject json) {
        Usuario u = new Usuario();
        u.setId(json.optString("id"));
        u.setNombre(json.optString("name"));
        u.setEmail(json.optString("email"));
        String createdAt = json.optString("createdAt");
        if (createdAt != null && !createdAt.isEmpty()) {
            // "2026-05-18T22:25:21Z" -> we extract the date part
            u.setFechaRegistro(LocalDate.parse(createdAt.substring(0, 10)));
        }
        return u;
    }
}
