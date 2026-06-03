package cooperpay.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import cooperpay.fx.PagamentoApiClient;
import cooperpay.util.SemanaUtils;

import cooperpay.dto.PagamentoDTOResponse;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class PagamentosFxController {

    private final PagamentoApiClient pagamentoApiClient = new PagamentoApiClient();
    private final ObservableList<PagamentoDTOResponse> pagamentos = FXCollections.observableArrayList();

    @FXML
    private TextField txtPesquisaMotoboy;

    @FXML
    private TextField txtDataPagamento;

    @FXML
    private Label lblMensagem;

    @FXML
    private TableView<PagamentoDTOResponse> tabelaPagamentos;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colMotoboy;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colPix;

    @FXML
    private TableColumn<PagamentoDTOResponse, BigDecimal> colValor;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colStatus;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colTrabalhou;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colSemana;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colTxid;

    @FXML
    private void initialize() {
        colMotoboy.setCellValueFactory(new PropertyValueFactory<>("nomeMotoboy"));
        colPix.setCellValueFactory(new PropertyValueFactory<>("pixMotoboy"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colTrabalhou.setCellValueFactory(new PropertyValueFactory<>("trabalhouTexto"));
        colSemana.setCellValueFactory(new PropertyValueFactory<>("semanaReferencia"));
        colSemana.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(SemanaUtils.formatarSemanaComDatas(item));
                }
            }
        });
        colTxid.setCellValueFactory(new PropertyValueFactory<>("txidPix"));

        FilteredList<PagamentoDTOResponse> filtrada = new FilteredList<>(pagamentos, item -> true);
        txtPesquisaMotoboy.textProperty().addListener((obs, antigo, novo) -> {
            String filtro = novo == null ? "" : novo.trim().toLowerCase();
            filtrada.setPredicate(item -> item.getNomeMotoboy() != null
                    && item.getNomeMotoboy().toLowerCase().contains(filtro));
        });

        tabelaPagamentos.setItems(filtrada);
        carregarPagamentosFeitos(null);
    }

    @FXML
    private void filtrarPorData() {
        String dataTexto = txtDataPagamento == null || txtDataPagamento.getText() == null
                ? ""
                : txtDataPagamento.getText().trim();

        if (dataTexto.isBlank()) {
            carregarPagamentosFeitos(null);
            return;
        }

        try {
            LocalDate data;
            if (dataTexto.contains("/")) {
                DateTimeFormatter brFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                data = LocalDate.parse(dataTexto, brFormat);
            } else {
                data = LocalDate.parse(dataTexto);
            }
            carregarPagamentosFeitos(data.toString());
        } catch (DateTimeParseException e) {
            lblMensagem.setText("Data invalida. Use dd/MM/yyyy ou yyyy-MM-dd.");
        }
    }

    @FXML
    private void limparData() {
        txtDataPagamento.clear();
        carregarPagamentosFeitos(null);
    }

    @FXML
    private void voltarHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1200, 600));
        stage.show();
    }

    @FXML
    private void sair(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void carregarPagamentosFeitos(String dataIso) {
        try {
            List<PagamentoDTOResponse> lista = pagamentoApiClient.listarPagos(dataIso);
            System.out.println("[PagamentosFeitos] Carregando lista. Total recebido: " + (lista != null ? lista.size() : 0));
            pagamentos.setAll(lista);
            lblMensagem.setText("Total de pagamentos encontrados: " + lista.size());
        } catch (InterruptedException e) {
            System.err.println("[PagamentosFeitos] Erro: Operacao interrompida");
            Thread.currentThread().interrupt();
            pagamentos.clear();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            System.err.println("[PagamentosFeitos] Erro de I/O ao listar: " + e.getMessage());
            pagamentos.clear();
            lblMensagem.setText("Erro ao carregar pagamentos.");
        }
    }
}
