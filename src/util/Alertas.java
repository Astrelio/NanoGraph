package util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class Alertas {

    private static void aplicarEstilo(DialogPane dialogPane) {
        String cssPath = Alertas.class.getResource("/view/styles.css").toExternalForm();
        dialogPane.getStylesheets().add(cssPath);
        // Cargar fuente retro para el diálogo
        javafx.scene.text.Font.loadFont(Alertas.class.getResourceAsStream("/assets/PressStart2P.ttf"), 10);
    }

    public static boolean pedirConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        aplicarEstilo(alert.getDialogPane());

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        aplicarEstilo(alert.getDialogPane());
        
        alert.showAndWait();
    }
    
    public static void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        aplicarEstilo(alert.getDialogPane());
        
        alert.showAndWait();
    }

    public static String pedirTexto(String titulo, String mensaje) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(titulo);
        dialog.setHeaderText(null);
        dialog.setContentText(mensaje);
        
        aplicarEstilo(dialog.getDialogPane());

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }
}
