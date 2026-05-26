package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegador {

    public static void cambiarEscena(ActionEvent event, String rutaFxml, String titulo) {
        try {
            Parent root = FXMLLoader.load(Navegador.class.getResource(rutaFxml));
            
            // Obtener la escena actual
            Scene currentScene = ((Node) event.getSource()).getScene();
            
            // Cargar CSS Retro (por si no está cargado)
            String cssPath = Navegador.class.getResource("/view/styles.css").toExternalForm();
            if (!currentScene.getStylesheets().contains(cssPath)) {
                currentScene.getStylesheets().add(cssPath);
            }
            
            // Cambiar solo el contenido interno (root) para no parpadear ni cambiar tamaño
            currentScene.setRoot(root);
            
            Stage stage = (Stage) currentScene.getWindow();
            stage.setTitle(titulo != null ? titulo : "NanoGraph");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar la vista: " + rutaFxml);
        }
    }
}
