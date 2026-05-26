package model;

import java.time.LocalDate;

public class Tarea extends Contenido {
    private String titulo;
    private String descripcion;
    private LocalDate fechaLimite;
    private boolean completada;

    public Tarea() {}

    public Tarea(String id, LocalDate fechaCreacion, String materiaId, String titulo, String descripcion, LocalDate fechaLimite, boolean completada) {
        super(id, fechaCreacion, materiaId);
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fechaLimite = fechaLimite;
        this.completada = completada;
    }

    @Override
    public String getTipo() {
        return "TAREA";
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public boolean isCompletada() { return completada; }
    public void setCompletada(boolean completada) { this.completada = completada; }
}
