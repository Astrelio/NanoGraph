package model;

import java.time.LocalDate;

public abstract class Contenido {
    private String id;
    private LocalDate fechaCreacion;
    private String materiaId;

    public Contenido() {}

    public Contenido(String id, LocalDate fechaCreacion, String materiaId) {
        this.id = id;
        this.fechaCreacion = fechaCreacion;
        this.materiaId = materiaId;
    }

    public abstract String getTipo();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDate getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDate fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }
}
