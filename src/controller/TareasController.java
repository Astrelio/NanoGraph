package controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.util.StringConverter;
import model.Materia;
import model.Tarea;
import dao.MateriaDAO;
import dao.TareaDAO;
import util.Alertas;
import util.Navegador;
import util.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class TareasController {

    @FXML private TableView<Tarea> tableTareas;
    @FXML private TableColumn<Tarea, String> colTitulo;
    @FXML private TableColumn<Tarea, String> colFecha;
    @FXML private TableColumn<Tarea, Boolean> colCompletada;

    @FXML private TextField txtTitulo;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpFechaLimite;
    @FXML private ComboBox<Materia> cbMateria;
    @FXML private CheckBox chkCompletada;

    private final TareaDAO tareaDAO = new TareaDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();
    private ObservableList<Tarea> tareasLista = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        cargarMaterias();
    }

    private void configurarTabla() {
        if (tableTareas.getColumns().isEmpty()) {
            colTitulo = new TableColumn<>("Título");
            colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
            colTitulo.setPrefWidth(200);

            colFecha = new TableColumn<>("Fecha Límite");
            colFecha.setCellValueFactory(cellData -> {
                LocalDate date = cellData.getValue().getFechaLimite();
                return new SimpleStringProperty(date != null ? date.toString() : "Sin fecha");
            });
            colFecha.setPrefWidth(100);

            colCompletada = new TableColumn<>("Hecho");
            // Fix del Checkbox:
            colCompletada.setCellValueFactory(cellData -> {
                Tarea tarea = cellData.getValue();
                BooleanProperty prop = new SimpleBooleanProperty(tarea.isCompletada());
                prop.addListener((obs, oldVal, newVal) -> {
                    tarea.setCompletada(newVal);
                    // Actualizar BD instantáneamente
                    tareaDAO.actualizarEstado(tarea.getId(), newVal);
                });
                return prop;
            });
            colCompletada.setCellFactory(CheckBoxTableCell.forTableColumn(colCompletada));
            colCompletada.setPrefWidth(80);

            tableTareas.getColumns().addAll(colTitulo, colFecha, colCompletada);
        }
        
        tableTareas.setItems(tareasLista);
        tableTareas.setEditable(true); // Necesario para que funcione el checkbox

        // Agregar Menú de Clic Derecho (Context Menu)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem eliminarItem = new MenuItem("Eliminar Tarea");
        eliminarItem.setOnAction(event -> {
            Tarea tareaSel = tableTareas.getSelectionModel().getSelectedItem();
            if (tareaSel != null) {
                boolean confirmar = Alertas.pedirConfirmacion("Eliminar", "¿Seguro que deseas eliminar esta tarea?");
                if (confirmar) {
                    if (tareaDAO.eliminar(tareaSel.getId())) {
                        tareasLista.remove(tareaSel);
                    } else {
                        Alertas.mostrarError("Error", "No se pudo eliminar de la base de datos.");
                    }
                }
            }
        });
        contextMenu.getItems().add(eliminarItem);

        // Asociar el menú y el tooltip flotante a la fila
        tableTareas.setRowFactory(tv -> {
            TableRow<Tarea> row = new TableRow<>();
            
            // Tooltip flotante que sigue al mouse
            javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
            tooltip.setWrapText(true);
            tooltip.setMaxWidth(300);
            
            row.setOnMouseMoved(e -> {
                if (!row.isEmpty() && row.getItem() != null) {
                    String desc = row.getItem().getDescripcion();
                    if (desc != null && !desc.trim().isEmpty()) {
                        tooltip.setText(desc);
                        if (!tooltip.isShowing()) {
                            tooltip.show(row, e.getScreenX() + 15, e.getScreenY() + 15);
                        } else {
                            tooltip.setX(e.getScreenX() + 15);
                            tooltip.setY(e.getScreenY() + 15);
                        }
                    } else {
                        tooltip.hide();
                    }
                }
            });
            
            row.setOnMouseExited(e -> tooltip.hide());

            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                .then((ContextMenu) null)
                .otherwise(contextMenu)
            );
            return row;
        });
    }

    private void cargarMaterias() {
        String userId = SessionManager.getInstance().getUsuarioActual().getId();
        cbMateria.setPromptText("CARGANDO...");
        
        new Thread(() -> {
            List<Materia> materias = materiaDAO.obtenerPorUsuario(userId);
            
            javafx.application.Platform.runLater(() -> {
                cbMateria.setItems(FXCollections.observableArrayList(materias));
                cbMateria.setConverter(new StringConverter<Materia>() {
                    @Override
                    public String toString(Materia object) { return object != null ? object.getNombre() : ""; }
                    @Override
                    public Materia fromString(String string) { return null; }
                });
                cbMateria.setPromptText("SELECCIONAR MATERIA");
                cbMateria.setOnAction(e -> cargarTareasTabla());
            });
        }).start();
    }

    private void cargarTareasTabla() {
        Materia seleccionada = cbMateria.getValue();
        if (seleccionada != null) {
            tableTareas.setPlaceholder(new javafx.scene.control.Label("CARGANDO TAREAS..."));
            tareasLista.clear();
            
            new Thread(() -> {
                List<Tarea> tareas = tareaDAO.obtenerPorMateria(seleccionada.getId());
                javafx.application.Platform.runLater(() -> {
                    tareasLista.setAll(tareas);
                    tableTareas.setPlaceholder(new javafx.scene.control.Label("No hay tareas."));
                });
            }).start();
        }
    }

    @FXML
    private void guardarTarea() {
        Materia mat = cbMateria.getValue();
        String titulo = txtTitulo.getText();
        String desc = txtDescripcion.getText();
        LocalDate fecha = dpFechaLimite.getValue();
        boolean completada = chkCompletada.isSelected();

        if (mat == null || titulo.isEmpty()) {
            Alertas.mostrarError("Campos incompletos", "Debes seleccionar materia y poner un título.");
            return;
        }

        Tarea t = new Tarea(null, LocalDate.now(), mat.getId(), titulo, desc, fecha, completada);
        if (tareaDAO.insertar(t)) {
            txtTitulo.clear();
            txtDescripcion.clear();
            dpFechaLimite.setValue(null);
            chkCompletada.setSelected(false);
            cargarTareasTabla(); 
        } else {
            Alertas.mostrarError("Error", "No se pudo guardar la tarea.");
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Dashboard.fxml", "Dashboard");
    }
}
