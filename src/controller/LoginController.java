package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Usuario;
import dao.UsuarioDAO;
import util.LocalStorage;
import util.Navegador;
import util.SessionManager;

import java.io.IOException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private CheckBox chkRecordar;
    @FXML private Label lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void initialize() {
        // Al iniciar, limpiar posible mensaje de error
        lblError.setText("");
    }

    @FXML
    private void ingresar(ActionEvent event) {
        String email = txtEmail.getText().trim();
        
        if (email.isEmpty()) {
            lblError.setText("Por favor, ingresa tu email.");
            return;
        }

        lblError.setText("Validando...");

        Usuario usuario = usuarioDAO.obtenerPorEmail(email);

        if (usuario != null) {
            // Usuario encontrado -> Iniciar sesión
            SessionManager.getInstance().setUsuarioActual(usuario);
            
            // Recordar sesión si el checkbox está marcado
            if (chkRecordar.isSelected()) {
                LocalStorage.guardarSesion(email);
            } else {
                LocalStorage.limpiarSesion();
            }

            // Cambiar pantalla al Dashboard
            irAlDashboard(event);
        } else {
            lblError.setText("El email no existe en la base de datos.");
        }
    }

    private void irAlDashboard(ActionEvent event) {
        Navegador.cambiarEscena(event, "/view/Dashboard.fxml", "Dashboard");
    }
}
