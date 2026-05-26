package model;

public class Materia {
    private String id;
    private String nombre;
    private String color; // hex
    private String usuarioId;

    public Materia() {}

    public Materia(String id, String nombre, String color, String usuarioId) {
        this.id = id;
        this.nombre = nombre;
        this.color = color;
        this.usuarioId = usuarioId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
}
