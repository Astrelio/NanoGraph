package model;

import java.time.LocalDate;

public class Nota extends Contenido {
    private String titulo;
    private String contenido;

    public Nota() {}

    public Nota(String id, LocalDate fechaCreacion, String materiaId, String titulo, String contenido) {
        super(id, fechaCreacion, materiaId);
        this.titulo = titulo;
        this.contenido = contenido;
    }

    @Override
    public String getTipo() {
        return "NOTA";
    }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
}
