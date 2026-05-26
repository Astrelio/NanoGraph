package controller;

import dao.GoogleClassroomClient;
import dao.GoogleClassroomClient.Curso;
import dao.GoogleClassroomClient.TareaClassroom;
import dao.MateriaDAO;
import dao.TareaDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.Materia;
import model.Tarea;
import util.Alertas;
import util.GoogleAuthService;
import util.GoogleTokenStorage;
import util.Navegador;
import util.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class ClassroomController {

    @FXML private VBox panelConexion;
    @FXML private HBox panelCursos;
    @FXML private Button btnConectar;
    @FXML private Button btnImportar;
    @FXML private Button btnDesconectar;
    @FXML private Label lblEstadoConexion;
    @FXML private Label lblEstadoImport;
    @FXML private ListView<Curso> listCursos;
    @FXML private ListView<TareaClassroom> listTareasClassroom;

    private final GoogleClassroomClient classroomClient = new GoogleClassroomClient();
    private final MateriaDAO materiaDAO = new MateriaDAO();
    private final TareaDAO tareaDAO = new TareaDAO();
    private ObservableList<Curso> cursosLista = FXCollections.observableArrayList();
    private ObservableList<TareaClassroom> tareasClassroomLista = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarListas();

        if (GoogleTokenStorage.tieneTokens()) {
            mostrarPanelCursos();
            cargarCursos();
        } else {
            mostrarPanelConexion();
        }
    }

    private void configurarListas() {
        listCursos.setItems(cursosLista);
        listCursos.setCellFactory(param -> new ListCell<Curso>() {
            @Override
            protected void updateItem(Curso item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });

        // Al seleccionar un curso, cargar sus tareas
        listCursos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarTareasDelCurso(newVal);
            }
        });

        listTareasClassroom.setItems(tareasClassroomLista);
        listTareasClassroom.setCellFactory(param -> new ListCell<TareaClassroom>() {
            @Override
            protected void updateItem(TareaClassroom item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void mostrarPanelConexion() {
        panelConexion.setVisible(true);
        panelConexion.setManaged(true);
        panelCursos.setVisible(false);
        panelCursos.setManaged(false);
        btnDesconectar.setVisible(false);
        btnDesconectar.setManaged(false);
    }

    private void mostrarPanelCursos() {
        panelConexion.setVisible(false);
        panelConexion.setManaged(false);
        panelCursos.setVisible(true);
        panelCursos.setManaged(true);
        btnDesconectar.setVisible(true);
        btnDesconectar.setManaged(true);
    }

    @FXML
    private void conectarGoogle() {
        lblEstadoConexion.setText("Abriendo navegador...");
        btnConectar.setDisable(true);

        new Thread(() -> {
            try {
                GoogleAuthService.iniciarAutorizacion().thenAccept(exito -> {
                    Platform.runLater(() -> {
                        if (exito) {
                            lblEstadoConexion.setText("Conexion exitosa.");
                            mostrarPanelCursos();
                            cargarCursos();
                        } else {
                            lblEstadoConexion.setText("Error en la autorizacion. Intenta de nuevo.");
                            btnConectar.setDisable(false);
                        }
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    lblEstadoConexion.setText("Error: " + e.getMessage());
                    btnConectar.setDisable(false);
                });
            }
        }).start();
    }

    private void cargarCursos() {
        listCursos.setPlaceholder(new Label("CARGANDO CURSOS..."));
        cursosLista.clear();

        new Thread(() -> {
            List<Curso> cursos = classroomClient.listarCursos();
            Platform.runLater(() -> {
                cursosLista.setAll(cursos);
                if (cursos.isEmpty()) {
                    listCursos.setPlaceholder(new Label("No se encontraron cursos activos."));
                }
            });
        }).start();
    }

    private void cargarTareasDelCurso(Curso curso) {
        listTareasClassroom.setPlaceholder(new Label("CARGANDO TAREAS..."));
        tareasClassroomLista.clear();
        lblEstadoImport.setText("");

        new Thread(() -> {
            List<TareaClassroom> tareas = classroomClient.listarTareasDeCurso(curso.getId());
            Platform.runLater(() -> {
                tareasClassroomLista.setAll(tareas);
                if (tareas.isEmpty()) {
                    listTareasClassroom.setPlaceholder(new Label("No hay tareas asignadas."));
                }
            });
        }).start();
    }

    @FXML
    private void importarTareas() {
        Curso cursoSeleccionado = listCursos.getSelectionModel().getSelectedItem();
        if (cursoSeleccionado == null) {
            Alertas.mostrarError("Sin curso", "Selecciona un curso primero.");
            return;
        }

        if (tareasClassroomLista.isEmpty()) {
            Alertas.mostrarError("Sin tareas", "No hay tareas para importar de este curso.");
            return;
        }

        String userId = SessionManager.getInstance().getUsuarioActual().getId();
        btnImportar.setDisable(true);
        lblEstadoImport.setText("Importando...");

        new Thread(() -> {
            try {
                // 1. Buscar si ya existe una materia con el nombre del curso, o crear una nueva
                String nombreMateria = cursoSeleccionado.getNombre();
                List<Materia> materiasExistentes = materiaDAO.obtenerPorUsuario(userId);
                Materia materiaDestino = null;

                for (Materia m : materiasExistentes) {
                    if (m.getNombre().equalsIgnoreCase(nombreMateria)) {
                        materiaDestino = m;
                        break;
                    }
                }

                if (materiaDestino == null) {
                    // Crear nueva materia con el nombre del curso
                    materiaDestino = new Materia(null, nombreMateria, "#A78BFA", userId);
                    if (!materiaDAO.insertar(materiaDestino)) {
                        Platform.runLater(() -> {
                            Alertas.mostrarError("Error", "No se pudo crear la materia para el curso.");
                            btnImportar.setDisable(false);
                            lblEstadoImport.setText("");
                        });
                        return;
                    }

                    // Recargar para obtener el ID asignado por Supabase
                    materiasExistentes = materiaDAO.obtenerPorUsuario(userId);
                    for (Materia m : materiasExistentes) {
                        if (m.getNombre().equalsIgnoreCase(nombreMateria)) {
                            materiaDestino = m;
                            break;
                        }
                    }
                }

                if (materiaDestino == null || materiaDestino.getId() == null) {
                    Platform.runLater(() -> {
                        Alertas.mostrarError("Error", "No se pudo obtener la materia creada.");
                        btnImportar.setDisable(false);
                        lblEstadoImport.setText("");
                    });
                    return;
                }

                // 2. Obtener tareas existentes para evitar duplicados
                List<Tarea> tareasExistentes = tareaDAO.obtenerPorMateria(materiaDestino.getId());

                // 3. Importar cada tarea de Classroom
                int importadas = 0;
                int omitidas = 0;

                for (TareaClassroom tc : tareasClassroomLista) {
                    // Verificar si ya existe una tarea con el mismo titulo
                    boolean duplicada = false;
                    for (Tarea existente : tareasExistentes) {
                        if (existente.getTitulo().equalsIgnoreCase(tc.getTitulo())) {
                            duplicada = true;
                            break;
                        }
                    }

                    if (duplicada) {
                        omitidas++;
                        continue;
                    }

                    // Parsear fecha de entrega
                    LocalDate fechaLimite = null;
                    if (tc.getFechaEntrega() != null && !tc.getFechaEntrega().isEmpty()) {
                        try {
                            fechaLimite = LocalDate.parse(tc.getFechaEntrega());
                        } catch (Exception e) {
                            System.err.println("Error parsing date: " + tc.getFechaEntrega());
                        }
                    }

                    Tarea nueva = new Tarea(
                            null,
                            LocalDate.now(),
                            materiaDestino.getId(),
                            tc.getTitulo(),
                            tc.getDescripcion(),
                            fechaLimite,
                            false
                    );

                    if (tareaDAO.insertar(nueva)) {
                        importadas++;
                    }
                }

                final int totalImportadas = importadas;
                final int totalOmitidas = omitidas;

                Platform.runLater(() -> {
                    String mensaje = totalImportadas + " tareas importadas.";
                    if (totalOmitidas > 0) {
                        mensaje += " " + totalOmitidas + " omitidas (ya existian).";
                    }
                    lblEstadoImport.setText(mensaje);
                    btnImportar.setDisable(false);
                });

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    Alertas.mostrarError("Error", "Error al importar: " + e.getMessage());
                    btnImportar.setDisable(false);
                    lblEstadoImport.setText("");
                });
            }
        }).start();
    }

    @FXML
    private void desconectarGoogle() {
        if (Alertas.pedirConfirmacion("Desconectar", "¿Desconectar tu cuenta de Google?")) {
            GoogleTokenStorage.limpiarTokens();
            cursosLista.clear();
            tareasClassroomLista.clear();
            lblEstadoImport.setText("");
            mostrarPanelConexion();
            lblEstadoConexion.setText("");
            btnConectar.setDisable(false);
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Dashboard.fxml", "Dashboard");
    }
}
