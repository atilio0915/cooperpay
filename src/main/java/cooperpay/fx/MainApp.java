package cooperpay.fx;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

@SpringBootApplication
@ComponentScan(basePackages = "cooperpay")
public class MainApp extends Application {

    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        // Inicia o Spring Boot em segundo plano antes da interface abrir
        springContext = new SpringApplicationBuilder(MainApp.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/welcome.fxml"));
            Parent root = loader.load();

            stage.setTitle("CooperPay - Bem-vindo");
            stage.setScene(new Scene(root, 1200, 600));
            stage.setResizable(false); // Evita problemas de layout ao redimensionar
            stage.show();
        } catch (Exception e) {
            // Se der erro, imprime no console para debug
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        // Fecha o servidor Spring e o banco de dados H2 ao fechar a janela
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
