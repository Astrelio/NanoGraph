package Application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.Usuario;
import dao.UsuarioDAO;
import util.LocalStorage;
import util.SessionManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Revisar si hay una sesión guardada localmente
            String savedEmail = LocalStorage.obtenerSesionGuardada();
            boolean sessionValida = false;

            if (savedEmail != null && !savedEmail.isEmpty()) {
                // Intentar recuperar el usuario con ese email
                UsuarioDAO usuarioDAO = new UsuarioDAO();
                Usuario usuario = usuarioDAO.obtenerPorEmail(savedEmail);
                
                if (usuario != null) {
                    SessionManager.getInstance().setUsuarioActual(usuario);
                    sessionValida = true;
                    System.out.println("Sesión restaurada para: " + usuario.getEmail());
                } else {
                    System.out.println("El email guardado ya no existe. Limpiando sesión.");
                    LocalStorage.limpiarSesion();
                }
            }

            // Elegir qué vista cargar dependiendo de si hay sesión válida
            String vistaInicial = sessionValida ? "/view/Dashboard.fxml" : "/view/Login.fxml";
            
            // Cargar fuente retro localmente
            javafx.scene.text.Font.loadFont(getClass().getResourceAsStream("/assets/PressStart2P.ttf"), 10);
            
            Parent root = FXMLLoader.load(getClass().getResource(vistaInicial));
            // Iniciar en 800x600
            Scene scene = new Scene(root, 800, 600);
            
            // Aplicar CSS Retro
            String cssPath = getClass().getResource("/view/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            // Configurar la ventana
            primaryStage.setTitle("NanoGraph");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true); // Permitir redimensionar
            primaryStage.show();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
