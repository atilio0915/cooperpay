package cooperpay.fx;

import javafx.application.Application;

public class MainLauncher {
    public static void main(String[] args) {
        // Esta classe serve apenas para contornar problemas de inicialização do JavaFX com Maven/Spring Boot
        Application.launch(MainApp.class, args);
    }
}