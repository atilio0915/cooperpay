package cooperpay.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblMensagem;

    @FXML
    private void entrar() {
        // Placeholder action to avoid null handler errors
        String usuario = txtUsuario != null ? txtUsuario.getText() : "";
        if (lblMensagem != null) {
            lblMensagem.setText("Bem-vindo, " + usuario + "!");
        }
    }
}
