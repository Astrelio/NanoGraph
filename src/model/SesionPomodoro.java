package model;

import java.time.LocalDate;

public class SesionPomodoro {
    private String id;
    private int duracionMin;
    private LocalDate fecha;
    private String materiaId;

    public SesionPomodoro() {}

    public SesionPomodoro(String id, int duracionMin, LocalDate fecha, String materiaId) {
        this.id = id;
        this.duracionMin = duracionMin;
        this.fecha = fecha;
        this.materiaId = materiaId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getDuracionMin() { return duracionMin; }
    public void setDuracionMin(int duracionMin) { this.duracionMin = duracionMin; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getMateriaId() { return materiaId; }
    public void setMateriaId(String materiaId) { this.materiaId = materiaId; }
}
