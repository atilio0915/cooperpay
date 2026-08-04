package cooperpay.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import cooperpay.fx.LojaApiClient;
import cooperpay.fx.LojaItem;
import cooperpay.fx.MotoboyApiClient;
import cooperpay.fx.MotoboyItem;
import cooperpay.fx.PagamentoApiClient;
import cooperpay.util.SemanaUtils;

import cooperpay.dto.PagamentoDTOResponse;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class HomeFxController {

    private final LojaApiClient lojaApiClient = new LojaApiClient();
    private final MotoboyApiClient motoboyApiClient = new MotoboyApiClient();
    private final PagamentoApiClient pagamentoApiClient = new PagamentoApiClient();
    private final ObservableList<MotoboyItem> motoboys = FXCollections.observableArrayList();
    private final ObservableList<PagamentoDTOResponse> rascunhos = FXCollections.observableArrayList();
    private final String semanaAtual = SemanaUtils.getSemanaAtual();

    @Autowired
    private ApplicationContext applicationContext;

    @FXML
    private TextField txtPesquisaMotoboy;

    @FXML
    private TableView<MotoboyItem> tabelaMotoboys;

    @FXML
    private TableColumn<MotoboyItem, String> colTrabalhou;

    @FXML
    private TableColumn<MotoboyItem, String> colNome;

    @FXML
    private TextField txtPesquisaDireita;

    @FXML
    private MenuButton menuImportarLojaRascunho;

    @FXML
    private TableView<PagamentoDTOResponse> tabelaPagamentosRascunho;

    @FXML
    private TableView<PagamentoDTOResponse> tabelaMotoboysTrabalharam;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colMotoboyDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colPixDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, BigDecimal> colValorDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colStatusDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colSalvoDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colSemanaDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colTxidDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, String> colTrabalhouDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, Void> colPagarDireita;

    @FXML
    private TableColumn<PagamentoDTOResponse, Void> colRemoverDireita;

    @FXML
    private void initialize() {
        colTrabalhou.setCellValueFactory(new PropertyValueFactory<>("trabalhouTexto"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colMotoboyDireita.setCellValueFactory(new PropertyValueFactory<>("nomeMotoboy"));
        colPixDireita.setCellValueFactory(new PropertyValueFactory<>("pixMotoboy"));
        colValorDireita.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colStatusDireita.setCellValueFactory(new PropertyValueFactory<>("status"));
        colSalvoDireita.setCellValueFactory(new PropertyValueFactory<>("salvoTexto"));
        colSemanaDireita.setCellValueFactory(new PropertyValueFactory<>("semanaReferencia"));
        colSemanaDireita.setCellFactory(column -> new TableCell<>() {
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
        colTxidDireita.setCellValueFactory(new PropertyValueFactory<>("txidPix"));
        colTrabalhouDireita.setCellValueFactory(new PropertyValueFactory<>("trabalhouTexto"));

        carregarMenuImportarLojas();
        carregarDados();

        FilteredList<MotoboyItem> esquerda = new FilteredList<>(motoboys, item -> true);
        txtPesquisaMotoboy.textProperty().addListener((obs, antigo, novo) -> {
            String filtro = novo == null ? "" : novo.trim().toLowerCase();
            esquerda.setPredicate(item -> item.getNome() != null && item.getNome().toLowerCase().contains(filtro));
        });
        tabelaMotoboys.setItems(esquerda);
        configurarCliqueTrabalhou();

        FilteredList<PagamentoDTOResponse> direita = new FilteredList<>(rascunhos, item -> true);
        txtPesquisaDireita.textProperty().addListener((obs, antigo, novo) -> {
            String filtro = novo == null ? "" : novo.trim().toLowerCase();
            direita.setPredicate(item -> item.getNomeMotoboy() != null
                    && item.getNomeMotoboy().toLowerCase().contains(filtro));
        });
        obterTabelaDireita().setItems(direita);
        configurarBotoesDireita();
        
        initAutoRefresh();
    }

    private void initAutoRefresh() {
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(5), event -> carregarDados()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();
    }

    @FXML
    private void abrirEditarMotoboy(ActionEvent event) throws IOException {
        trocarCena((Node) event.getSource(), "/editar-motoboy.fxml", 1000, 650);
    }

    @FXML
    private void abrirPagamentos(ActionEvent event) throws IOException {
        trocarCena((Node) event.getSource(), "/pagamentos.fxml", 1200, 600);
    }

    @FXML
    private void abrirLojas(ActionEvent event) throws IOException {
        trocarCena((Node) event.getSource(), "/lojas.fxml", 1200, 600);
    }

    @FXML
    private void sair(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void trocarCena(Node source, String fxml, int largura, int altura) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        loader.setControllerFactory(applicationContext::getBean);
        Parent root = loader.load();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.setScene(new Scene(root, largura, altura));
        stage.show();
    }

    private void carregarDados() {
        carregarMotoboys();
        carregarRascunhos();
    }

    private void carregarMotoboys() {
        try {
            List<MotoboyItem> lista = motoboyApiClient.listar("");
            motoboys.setAll(lista);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            motoboys.clear();
        } catch (IOException e) {
            motoboys.clear();
        }
    }

    private void carregarRascunhos() {
        try {
            List<PagamentoDTOResponse> lista = pagamentoApiClient.listarRascunhos(semanaAtual);
            rascunhos.setAll(lista);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            rascunhos.clear();
        } catch (IOException e) {
            rascunhos.clear();
        }
    }

    private void carregarMenuImportarLojas() {
        if (menuImportarLojaRascunho == null) {
            return;
        }
        try {
            List<LojaItem> lojas = lojaApiClient.listar();
            menuImportarLojaRascunho.getItems().clear();

            for (LojaItem loja : lojas) {
                MenuItem item = new MenuItem(loja.getNome());
                item.setOnAction(event -> importarRascunhosDaLoja(loja));
                menuImportarLojaRascunho.getItems().add(item);
            }
        } catch (Exception ignored) {
            // Mantem fluxo da Home mesmo com falha ao carregar menu de lojas.
        }
    }

    private void importarRascunhosDaLoja(LojaItem loja) {
        if (loja == null || loja.getId() == null) {
            return;
        }
        try {
            List<MotoboyItem> motoboysAtivos = motoboyApiClient.listar("");
            for (MotoboyItem motoboy : motoboysAtivos) {
                if (motoboy == null || motoboy.getId() == null) {
                    continue;
                }
                if (Boolean.TRUE.equals(motoboy.getTrabalhou())) {
                    motoboyApiClient.atualizar(motoboy.getId(), motoboy.getNome(), motoboy.getPix(), false);
                    pagamentoApiClient.removerRascunho(motoboy.getId(), semanaAtual);
                }
            }
            List<MotoboyItem> motoboysLoja = motoboyApiClient.listar("", loja.getId());
            for (MotoboyItem motoboy : motoboysLoja) {
                if (motoboy == null || motoboy.getId() == null) {
                    continue;
                }
                motoboyApiClient.atualizar(
                        motoboy.getId(),
                        motoboy.getNome(),
                        motoboy.getPix(),
                        true,
                        loja.getId());
                pagamentoApiClient.criarRascunho(motoboy.getId(), semanaAtual);
            }
            menuImportarLojaRascunho.setText("Importado: " + loja.getNome());
            carregarDados();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            // Mantem tela responsiva em caso de falha de rede/servidor.
        }
    }

    private void configurarCliqueTrabalhou() {
        Callback<TableColumn<MotoboyItem, String>, TableCell<MotoboyItem, String>> factory = col -> new TableCell<>() {
            {
                setOnMouseClicked(event -> {
                    if (isEmpty()) {
                        return;
                    }
                    MotoboyItem item = getTableRow().getItem();
                    if (item == null || item.getId() == null) {
                        return;
                    }

                    boolean novoValor = !Boolean.TRUE.equals(item.getTrabalhou());
                    try {
                        motoboyApiClient.atualizar(item.getId(), item.getNome(), item.getPix(), novoValor);
                        if (novoValor) {
                            pagamentoApiClient.criarRascunho(item.getId(), semanaAtual);
                        } else {
                            pagamentoApiClient.removerRascunho(item.getId(), semanaAtual);
                        }
                        carregarDados();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        // Mantem a tela responsiva sem quebrar fluxo visual.
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
            }
        };
        colTrabalhou.setCellFactory(factory);
    }

    private void configurarBotoesDireita() {
        colPagarDireita.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Pagar");

            {
                btn.setOnAction(event -> {
                    PagamentoDTOResponse item = getTableView().getItems().get(getIndex());
                    if (item == null || item.getIdPagamento() == null) {
                        return;
                    }
                    try {
                        BigDecimal valorAtual = item.getValor();
                        if (valorAtual == null || valorAtual.compareTo(BigDecimal.ZERO) <= 0) {
                            TextInputDialog dialog = new TextInputDialog();
                            dialog.setTitle("Valor do Pagamento");
                            dialog.setHeaderText("Informe o valor para pagar " + item.getNomeMotoboy());
                            dialog.setContentText("Valor:");
                            String texto = dialog.showAndWait().orElse("").trim().replace(",", ".");
                            if (texto.isBlank()) {
                                return;
                            }
                            BigDecimal novoValor = new BigDecimal(texto);
                            pagamentoApiClient.atualizarValor(item.getIdPagamento(), novoValor);
                        }
                        pagamentoApiClient.pagarRascunho(item.getIdPagamento());
                        carregarDados();
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Erro no Pagamento");
                        alert.setHeaderText("Falha ao processar pagamento via Asaas");
                        alert.setContentText("Motivo: " + e.getMessage());
                        alert.showAndWait();
                        carregarDados();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        colRemoverDireita.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Remover");

            {
                btn.setOnAction(event -> {
                    PagamentoDTOResponse item = getTableView().getItems().get(getIndex());
                    if (item == null || item.getIdMotoboy() == null) {
                        return;
                    }
                    try {
                        pagamentoApiClient.removerRascunho(item.getIdMotoboy(), semanaAtual);
                        MotoboyItem motoboy = encontrarMotoboy(item.getIdMotoboy());
                        if (motoboy != null) {
                            motoboyApiClient.atualizar(motoboy.getId(), motoboy.getNome(), motoboy.getPix(), false);
                        }
                        carregarDados();
                    } catch (Exception ignored) {
                        // Mantem tela funcionando mesmo com falha de rede/validacao.
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private TableView<PagamentoDTOResponse> obterTabelaDireita() {
        return tabelaPagamentosRascunho != null ? tabelaPagamentosRascunho : tabelaMotoboysTrabalharam;
    }

    private MotoboyItem encontrarMotoboy(Long idMotoboy) {
        for (MotoboyItem motoboy : motoboys) {
            if (idMotoboy.equals(motoboy.getId())) {
                return motoboy;
            }
        }
        return null;
    }
}
