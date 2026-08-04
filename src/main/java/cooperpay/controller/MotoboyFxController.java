package cooperpay.controller;

import java.io.IOException;
import java.util.List;

import cooperpay.fx.MotoboyApiClient;
import cooperpay.fx.MotoboyItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class MotoboyFxController {

    private final MotoboyApiClient apiClient = new MotoboyApiClient();
    private final ObservableList<MotoboyItem> motoboys = FXCollections.observableArrayList();

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TextField txtPesquisa;

    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtPix;

    @FXML
    private Label lblMensagem;

    @FXML
    private TableView<MotoboyItem> tabelaMotoboys;

    @FXML
    private TableColumn<MotoboyItem, Long> colId;

    @FXML
    private TableColumn<MotoboyItem, String> colNome;

    @FXML
    private TableColumn<MotoboyItem, String> colPix;

    @FXML
    private TableColumn<MotoboyItem, String> colTrabalhou;

    @FXML
    private void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPix.setCellValueFactory(new PropertyValueFactory<>("pix"));
        colTrabalhou.setCellValueFactory(new PropertyValueFactory<>("trabalhouTexto"));

        carregarMotoboys();

        FilteredList<MotoboyItem> filtrada = new FilteredList<>(motoboys, item -> true);
        txtPesquisa.textProperty().addListener((obs, antigo, novo) -> {
            String filtro = novo == null ? "" : novo.trim().toLowerCase();
            filtrada.setPredicate(item -> {
                String nome = item.getNome() == null ? "" : item.getNome().toLowerCase();
                String pix = item.getPix() == null ? "" : item.getPix().toLowerCase();
                return nome.contains(filtro) || pix.contains(filtro);
            });
        });

        tabelaMotoboys.setItems(filtrada);
        tabelaMotoboys.getSelectionModel().selectedItemProperty().addListener((obs, antigo, atual) -> {
            if (atual != null) {
                txtNome.setText(atual.getNome());
                txtPix.setText(atual.getPix());
            }
        });
    }

    @FXML
    private void criarMotoboy() {
        String nome = txtNome.getText() == null ? "" : txtNome.getText().trim();
        String pix = txtPix.getText() == null ? "" : txtPix.getText().trim();

        if (nome.isBlank() || pix.isBlank()) {
            lblMensagem.setText("Informe nome e pix.");
            return;
        }

        try {
            apiClient.criar(nome, pix);
            carregarMotoboys();
            txtNome.clear();
            txtPix.clear();
            lblMensagem.setText("Motoboy criado com sucesso.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("HTTP 409")) {
                lblMensagem.setText("Esse motoboy ja existe.");
            } else {
                lblMensagem.setText("Erro ao criar motoboy.");
            }
        }
    }

    @FXML
    private void excluirSelecionado() {
        MotoboyItem selecionado = tabelaMotoboys.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            lblMensagem.setText("Selecione um motoboy para excluir.");
            return;
        }

        if (selecionado.getId() == null) {
            lblMensagem.setText("ID invalido para exclusao.");
            return;
        }

        try {
            apiClient.excluir(selecionado.getId());
            carregarMotoboys();
            lblMensagem.setText("Motoboy excluido com sucesso.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao excluir motoboy.");
        }
    }

    @FXML
    private void atualizarSelecionado() {
        MotoboyItem selecionado = tabelaMotoboys.getSelectionModel().getSelectedItem();
        if (selecionado == null || selecionado.getId() == null) {
            lblMensagem.setText("Selecione um motoboy para atualizar.");
            return;
        }

        String nome = txtNome.getText() == null ? "" : txtNome.getText().trim();
        String pix = txtPix.getText() == null ? "" : txtPix.getText().trim();
        if (nome.isBlank() || pix.isBlank()) {
            lblMensagem.setText("Informe nome e pix.");
            return;
        }

        try {
            apiClient.atualizar(selecionado.getId(), nome, pix);
            carregarMotoboys();
            lblMensagem.setText("Motoboy atualizado com sucesso.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao atualizar motoboy.");
        }
    }

    @FXML
    private void voltarHome(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/home.fxml"));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 600));
        stage.show();
    }

    private void carregarMotoboys() {
        try {
            List<MotoboyItem> lista = apiClient.listar("");
            motoboys.setAll(lista);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            motoboys.clear();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            motoboys.clear();
            lblMensagem.setText("Erro ao carregar motoboys.");
        }
    }
}
