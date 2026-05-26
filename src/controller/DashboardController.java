package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import model.Materia;
import model.Tarea;
import model.Usuario;
import dao.MateriaDAO;
import dao.TareaDAO;
import util.Alertas;
import util.Navegador;
import util.SessionManager;

import java.util.List;

public class DashboardController {

    @FXML private ListView<Materia> listMaterias;
    @FXML private Label lblResumenTareas;
    @FXML private Button btnModuloTareas;
    @FXML private Button btnModuloNotas;
    @FXML private Button btnModuloPomodoro;

    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final TareaDAO tareaDAO = new TareaDAO();
    private ObservableList<Materia> materiasLista = FXCollections.observableArrayList();
    private java.util.Map<String, String> infoMaterias = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        configurarListaMaterias();
        cargarResumen();
    }

    private void configurarListaMaterias() {
        listMaterias.setItems(materiasLista);
        listMaterias.setCellFactory(param -> {
            ListCell<Materia> cell = new ListCell<Materia>() {
                @Override
                protected void updateItem(Materia item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText(item.getNombre());

                        ContextMenu ctxMenu = new ContextMenu();
                        MenuItem delItem = new MenuItem("Eliminar Materia");
                        delItem.setOnAction(e -> {
                            if (Alertas.pedirConfirmacion("Eliminar", "¿Eliminar materia y todo su contenido?")) {
                                if (materiaDAO.eliminar(item.getId())) {
                                    cargarResumen();
                                } else {
                                    Alertas.mostrarError("Error", "No se pudo eliminar la materia.");
                                }
                            }
                        });
                        ctxMenu.getItems().add(delItem);
                        setContextMenu(ctxMenu);
                    }
                }
            };

            // Tooltip flotante que sigue al mouse
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            
            cell.setOnMouseMoved(e -> {
                if (!cell.isEmpty() && cell.getItem() != null) {
                    String info = infoMaterias.get(cell.getItem().getId());
                    if (info != null) {
                        tooltip.setText(info);
                        if (!tooltip.isShowing()) {
                            tooltip.show(cell, e.getScreenX() + 15, e.getScreenY() + 15);
                        } else {
                            tooltip.setX(e.getScreenX() + 15);
                            tooltip.setY(e.getScreenY() + 15);
                        }
                    } else {
                        tooltip.hide();
                    }
                }
            });
            
            cell.setOnMouseExited(e -> tooltip.hide());

            return cell;
        });
    }

    private void cargarResumen() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario != null) {
            lblResumenTareas.setText("CARGANDO DATOS...");
            
            new Thread(() -> {
                List<Materia> materias = materiaDAO.obtenerPorUsuario(usuario.getId());
                
                int totalTareasPendientes = 0;
                for (Materia m : materias) {
                    List<Tarea> tareasMateria = tareaDAO.obtenerPorMateria(m.getId());
                    List<model.Nota> notasMateria = new dao.NotaDAO().obtenerPorMateria(m.getId());
                    
                    int activas = 0;
                    for (Tarea t : tareasMateria) {
                        if (!t.isCompletada()) {
                            activas++;
                            totalTareasPendientes++;
                        }
                    }
                    
                    String info = "Tareas pendientes: " + activas + "\nNotas guardadas: " + notasMateria.size();
                    infoMaterias.put(m.getId(), info);
                }
                
                final int pending = totalTareasPendientes;
                
                javafx.application.Platform.runLater(() -> {
                    materiasLista.setAll(materias);
                    lblResumenTareas.setText(pending + " tareas pendientes hoy");
                });
            }).start();
        }
    }

    @FXML
    private void crearNuevaMateria() {
        String nombre = Alertas.pedirTexto("Nueva Materia", "Ingresa el nombre de la materia:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            Usuario usuario = SessionManager.getInstance().getUsuarioActual();
            Materia m = new Materia(null, nombre.trim(), "#3498db", usuario.getId());
            if (materiaDAO.insertar(m)) {
                cargarResumen();
            } else {
                Alertas.mostrarError("Error", "No se pudo crear la materia.");
            }
        }
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        if (Alertas.pedirConfirmacion("Cerrar Sesión", "¿Seguro que deseas salir?")) {
            util.LocalStorage.limpiarSesion();
            SessionManager.getInstance().cerrarSesion();
            Navegador.cambiarEscena(event, "/view/Login.fxml", "Login");
        }
    }

    @FXML
    private void irATareas(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Tareas.fxml", "Módulo de Tareas");
    }

    @FXML
    private void irANotas(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Notas.fxml", "Módulo de Notas");
    }

    @FXML
    private void irAPomodoro(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Pomodoro.fxml", "Módulo Pomodoro");
    }

    @FXML
    private void irAClassroom(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Classroom.fxml", "Google Classroom");
    }
}
