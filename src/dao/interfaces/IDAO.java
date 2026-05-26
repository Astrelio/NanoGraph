package dao.interfaces;

import java.util.List;

public interface IDAO<T> {
    boolean insertar(T obj);
    List<T> obtenerTodos();
    T obtenerPorId(String id);
    boolean eliminar(String id);
}
