package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import model.Materia;
import model.Nota;
import dao.MateriaDAO;
import dao.NotaDAO;
import util.Navegador;
import util.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class NotasController {

    @FXML private ComboBox<Materia> cbMateriaFiltro;
    @FXML private ListView<Nota> listNotas;
    @FXML private TextField txtTitulo;
    @FXML private TextArea txtEditor;

    private final NotaDAO notaDAO = new NotaDAO();
    private final MateriaDAO materiaDAO = new MateriaDAO();
    private ObservableList<Nota> notasLista = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarMaterias();
        configurarListaNotas();
    }

    private void cargarMaterias() {
        String userId = SessionManager.getInstance().getUsuarioActual().getId();
        cbMateriaFiltro.setPromptText("CARGANDO...");
        
        new Thread(() -> {
            List<Materia> materias = materiaDAO.obtenerPorUsuario(userId);
            
            javafx.application.Platform.runLater(() -> {
                cbMateriaFiltro.setItems(FXCollections.observableArrayList(materias));
                cbMateriaFiltro.setConverter(new StringConverter<Materia>() {
                    @Override
                    public String toString(Materia object) { return object != null ? object.getNombre() : ""; }
                    @Override
                    public Materia fromString(String string) { return null; }
                });
                cbMateriaFiltro.setPromptText("SELECCIONAR MATERIA");
                cbMateriaFiltro.setOnAction(e -> cargarNotas());
            });
        }).start();
    }

    private void configurarListaNotas() {
        listNotas.setItems(notasLista);
        listNotas.setCellFactory(param -> {
            ListCell<Nota> cell = new ListCell<Nota>() {
                @Override
                protected void updateItem(Nota item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setContextMenu(null);
                    } else {
                        setText(item.getTitulo() != null ? item.getTitulo() : "Sin título");
                        
                        javafx.scene.control.ContextMenu ctxMenu = new javafx.scene.control.ContextMenu();
                        javafx.scene.control.MenuItem delItem = new javafx.scene.control.MenuItem("Eliminar Nota");
                        delItem.setOnAction(e -> {
                            if (util.Alertas.pedirConfirmacion("Eliminar", "¿Eliminar nota?")) {
                                if (notaDAO.eliminar(item.getId())) {
                                    notasLista.remove(item);
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
                    String contenido = cell.getItem().getContenido();
                    if (contenido != null && !contenido.trim().isEmpty()) {
                        tooltip.setText(contenido);
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

        // Evitar que la nota se seleccione y se copie al editor para prevenir duplicados al guardar
        listNotas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                javafx.application.Platform.runLater(() -> listNotas.getSelectionModel().clearSelection());
            }
        });
    }

    private void cargarNotas() {
        Materia mat = cbMateriaFiltro.getValue();
        if (mat != null) {
            listNotas.setPlaceholder(new javafx.scene.control.Label("CARGANDO NOTAS..."));
            notasLista.clear();
            
            new Thread(() -> {
                List<Nota> notas = notaDAO.obtenerPorMateria(mat.getId());
                javafx.application.Platform.runLater(() -> {
                    notasLista.setAll(notas);
                    txtTitulo.clear();
                    txtEditor.clear();
                    listNotas.setPlaceholder(new javafx.scene.control.Label("No hay notas."));
                });
            }).start();
        }
    }

    @FXML
    private void guardarNota() {
        Materia mat = cbMateriaFiltro.getValue();
        String titulo = txtTitulo.getText();
        String contenido = txtEditor.getText();

        if (mat == null || contenido.isEmpty()) {
            return;
        }

        Nota n = new Nota(null, LocalDate.now(), mat.getId(), titulo, contenido);
        if (notaDAO.insertar(n)) {
            cargarNotas();
            txtTitulo.clear();
            txtEditor.clear();
        }
    }

    @FXML
    private void volver(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Dashboard.fxml", "Dashboard");
    }
}
