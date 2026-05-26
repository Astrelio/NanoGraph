package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import javafx.util.StringConverter;
import model.Materia;
import model.SesionPomodoro;
import dao.MateriaDAO;
import dao.SesionPomodoroDAO;
import util.Navegador;
import util.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class PomodoroController {

    @FXML private Circle circleTimer;
    @FXML private Label lblTiempo;
    @FXML private Button btnIniciar;
    @FXML private Button btnPausar;
    @FXML private Button btnReset;
    @FXML private ComboBox<Materia> cbMateriaActiva;
    @FXML private ListView<SesionPomodoro> listHistorial;

    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final SesionPomodoroDAO sesionDAO = new SesionPomodoroDAO();
    private ObservableList<SesionPomodoro> historialLista = FXCollections.observableArrayList();

    private javafx.animation.Timeline timeline;
    private int duracionActualMin = 25;
    private int tiempoRestante = 25 * 60; // 25 minutos en segundos
    private boolean corriendo = false;

    @FXML
    public void initialize() {
        cargarMaterias();
        configurarHistorial();
        actualizarEtiquetaTiempo();
    }

    private void cargarMaterias() {
        String userId = SessionManager.getInstance().getUsuarioActual().getId();
        cbMateriaActiva.setPromptText("CARGANDO...");
        
        new Thread(() -> {
            List<Materia> materias = materiaDAO.obtenerPorUsuario(userId);
            
            javafx.application.Platform.runLater(() -> {
                cbMateriaActiva.setItems(FXCollections.observableArrayList(materias));
                cbMateriaActiva.setConverter(new StringConverter<Materia>() {
                    @Override
                    public String toString(Materia object) { return object != null ? object.getNombre() : ""; }
                    @Override
                    public Materia fromString(String string) { return null; }
                });
                cbMateriaActiva.setPromptText("SELECCIONAR MATERIA");
                cbMateriaActiva.setOnAction(e -> cargarHistorial());
            });
        }).start();
    }

    private void configurarHistorial() {
        listHistorial.setItems(historialLista);
        listHistorial.setCellFactory(param -> new ListCell<SesionPomodoro>() {
            @Override
            protected void updateItem(SesionPomodoro item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setContextMenu(null);
                } else {
                    setText("Sesión de " + item.getDuracionMin() + " min - " + item.getFecha());
                    
                    javafx.scene.control.ContextMenu ctxMenu = new javafx.scene.control.ContextMenu();
                    javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Eliminar Sesión");
                    delItem.setOnAction(e -> {
                        if (util.Alertas.pedirConfirmacion("Eliminar", "¿Eliminar sesión del historial?")) {
                            if (sesionDAO.eliminar(item.getId())) {
                                historialLista.remove(item);
                            }
                        }
                    });
                    ctxMenu.getItems().add(delItem);
                    setContextMenu(ctxMenu);
                }
            }
        });
    }

    private void cargarHistorial() {
        Materia mat = cbMateriaActiva.getValue();
        if (mat != null) {
            listHistorial.setPlaceholder(new javafx.scene.control.Label("CARGANDO..."));
            historialLista.clear();
            
            new Thread(() -> {
                List<SesionPomodoro> sesiones = sesionDAO.obtenerPorMateria(mat.getId());
                javafx.application.Platform.runLater(() -> {
                    historialLista.setAll(sesiones);
                    listHistorial.setPlaceholder(new javafx.scene.control.Label("No hay sesiones."));
                });
            }).start();
        }
    }

    @FXML
    private void editarTiempo() {
        if (corriendo) {
            util.Alertas.mostrarError("Error", "Pausa el temporizador antes de cambiar el tiempo.");
            return;
        }
        
        String input = util.Alertas.pedirTexto("Editar Tiempo", "Ingresa los minutos (mínimo 10):");
        if (input != null && !input.trim().isEmpty()) {
            try {
                int nuevosMinutos = Integer.parseInt(input.trim());
                if (nuevosMinutos < 10) {
                    util.Alertas.mostrarError("Error", "El tiempo mínimo es de 10 minutos.");
                } else {
                    duracionActualMin = nuevosMinutos;
                    resetTimer(); // Reinicia el timer con el nuevo valor
                }
            } catch (NumberFormatException ex) {
                util.Alertas.mostrarError("Error", "Por favor ingresa un número válido.");
            }
        }
    }

    @FXML
    private void iniciarTimer() {
        if (corriendo) return;
        
        if (cbMateriaActiva.getValue() == null) {
            System.err.println("Selecciona una materia primero.");
            return;
        }

        corriendo = true;
        if (timeline == null) {
            timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                tiempoRestante--;
                actualizarEtiquetaTiempo();
                
                if (tiempoRestante <= 0) {
                    finalizarSesion();
                }
            }));
            timeline.setCycleCount(Timeline.INDEFINITE);
        }
        timeline.play();
    }

    @FXML
    private void pausarTimer() {
        if (timeline != null && corriendo) {
            timeline.pause();
            corriendo = false;
        }
    }

    @FXML
    private void resetTimer() {
        if (timeline != null) timeline.stop();
        corriendo = false;
        tiempoRestante = duracionActualMin * 60;
        actualizarEtiquetaTiempo();
    }

    private void actualizarEtiquetaTiempo() {
        int min = tiempoRestante / 60;
        int sec = tiempoRestante % 60;
        lblTiempo.setText(String.format("%02d:%02d", min, sec));
    }

    private void finalizarSesion() {
        resetTimer();
        Materia mat = cbMateriaActiva.getValue();
        if (mat != null) {
            SesionPomodoro sp = new SesionPomodoro(null, duracionActualMin, LocalDate.now(), mat.getId());
            if (sesionDAO.insertar(sp)) {
                cargarHistorial();
                System.out.println("Sesión Pomodoro guardada exitosamente.");
            }
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        if (timeline != null) timeline.stop();
        Navegador.cambiarEscena(event, "/view/Dashboard.fxml", "Dashboard");
    }
}
