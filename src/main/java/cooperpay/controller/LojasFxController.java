package cooperpay.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cooperpay.fx.LojaApiClient;
import cooperpay.fx.LojaItem;
import cooperpay.fx.MotoboyApiClient;
import cooperpay.fx.MotoboyItem;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class LojasFxController {

    private final LojaApiClient lojaApiClient = new LojaApiClient();
    private final MotoboyApiClient motoboyApiClient = new MotoboyApiClient();
    private final ObservableList<MotoboyItem> motoboys = FXCollections.observableArrayList();
    private final FilteredList<MotoboyItem> motoboysFiltrados = new FilteredList<>(motoboys, item -> true);
    private List<LojaItem> lojasDisponiveis = new ArrayList<>();

    private Long lojaSelecionadaId;
    private String nomeLojaSelecionada;

    @FXML
    private TextField txtPesquisaMotoboy;

    @FXML
    private TextField txtNovaLoja;

    @FXML
    private TextField txtIdLojaExcluir;

    @FXML
    private MenuButton menuLojas;

    @FXML
    private Label lblLojaSelecionada;

    @FXML
    private Label lblMensagem;

    @FXML
    private TableView<MotoboyItem> tabelaMotoboysLoja;

    @FXML
    private TableColumn<MotoboyItem, String> colNome;

    @FXML
    private TableColumn<MotoboyItem, String> colPix;

    @FXML
    private TableColumn<MotoboyItem, Long> colLojaId;

    @FXML
    private TableColumn<MotoboyItem, Void> colAlterarLoja;

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colPix.setCellValueFactory(new PropertyValueFactory<>("pix"));
        colLojaId.setCellValueFactory(new PropertyValueFactory<>("lojaId"));
        configurarColunaAlterarLoja();

        tabelaMotoboysLoja.setItems(motoboysFiltrados);

        txtPesquisaMotoboy.textProperty().addListener((obs, antigo, novo) -> aplicarFiltroLocal());

        carregarLojasNoMenu();
        selecionarTodasLojas();
    }

    @FXML
    private void mostrarTodasAsLojas() {
        selecionarTodasLojas();
    }

    @FXML
    private void criarLoja() {
        String nome = txtNovaLoja == null || txtNovaLoja.getText() == null
                ? ""
                : txtNovaLoja.getText().trim();
        if (nome.isBlank()) {
            lblMensagem.setText("Informe o nome da loja.");
            return;
        }
        try {
            lojaApiClient.criar(nome);
            txtNovaLoja.clear();
            carregarLojasNoMenu();
            lblMensagem.setText("Loja criada com sucesso.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao criar loja.");
        }
    }

    @FXML
    private void deletarLoja() {
        String textoId = txtIdLojaExcluir == null || txtIdLojaExcluir.getText() == null
                ? ""
                : txtIdLojaExcluir.getText().trim();
        if (textoId.isBlank()) {
            lblMensagem.setText("Informe o ID da loja para excluir.");
            return;
        }

        Long id;
        try {
            id = Long.parseLong(textoId);
        } catch (NumberFormatException e) {
            lblMensagem.setText("ID da loja invalido.");
            return;
        }

        try {
            lojaApiClient.excluir(id);
            txtIdLojaExcluir.clear();
            carregarLojasNoMenu();
            if (lojaSelecionadaId != null && lojaSelecionadaId.equals(id)) {
                selecionarTodasLojas();
            } else {
                carregarTodosMotoboys();
            }
            lblMensagem.setText("Loja excluida com sucesso.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao excluir loja.");
        }
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

    private void carregarLojasNoMenu() {
        try {
            List<LojaItem> lojas = lojaApiClient.listar();
            lojasDisponiveis = lojas;
            menuLojas.getItems().clear();
            for (LojaItem loja : lojas) {
                MenuItem item = new MenuItem(loja.getNome());
                item.setOnAction(event -> selecionarLoja(loja));
                menuLojas.getItems().add(item);
            }
            tabelaMotoboysLoja.refresh();
            lblMensagem.setText(lojas.isEmpty() ? "Nenhuma loja cadastrada." : "Selecione uma loja para listar os motoboys.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao carregar lojas.");
        }
    }

    private void configurarColunaAlterarLoja() {
        colAlterarLoja.setCellFactory(col -> new TableCell<>() {
            private final MenuButton menu = new MenuButton("Vincular");

            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                MotoboyItem motoboy = getTableView().getItems().get(getIndex());
                if (motoboy == null || motoboy.getId() == null) {
                    setGraphic(null);
                    return;
                }

                menu.setText(motoboy.getLojaNome() == null || motoboy.getLojaNome().isBlank()
                        ? "Sem loja"
                        : motoboy.getLojaNome());
                menu.getItems().clear();

                MenuItem semLoja = new MenuItem("Sem loja");
                semLoja.setOnAction(event -> atualizarLojaMotoboy(motoboy, 0L, "Sem loja"));
                menu.getItems().add(semLoja);

                for (LojaItem loja : lojasDisponiveis) {
                    MenuItem lojaItem = new MenuItem(loja.getNome());
                    lojaItem.setOnAction(event -> atualizarLojaMotoboy(motoboy, loja.getId(), loja.getNome()));
                    menu.getItems().add(lojaItem);
                }

                setGraphic(menu);
            }
        });
    }

    private void atualizarLojaMotoboy(MotoboyItem motoboy, Long lojaId, String lojaNome) {
        try {
            motoboyApiClient.atualizar(motoboy.getId(), motoboy.getNome(), motoboy.getPix(), motoboy.getTrabalhou(), lojaId);
            if (lojaId != null && lojaId > 0) {
                motoboy.setLojaId(lojaId);
                motoboy.setLojaNome(lojaNome);
            } else {
                motoboy.setLojaId(null);
                motoboy.setLojaNome("");
            }
            tabelaMotoboysLoja.refresh();
            lblMensagem.setText("Loja atualizada para o motoboy " + motoboy.getNome() + ".");
            if (lojaSelecionadaId != null) {
                carregarMotoboysDaLoja();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            lblMensagem.setText("Erro ao vincular loja ao motoboy.");
        }
    }

    private void selecionarLoja(LojaItem loja) {
        lojaSelecionadaId = loja.getId();
        nomeLojaSelecionada = loja.getNome();
        menuLojas.setText(loja.getNome());
        lblLojaSelecionada.setText("Loja selecionada: " + loja.getNome() + " (ID " + loja.getId() + ")");
        carregarMotoboysDaLoja();
    }

    private void selecionarTodasLojas() {
        lojaSelecionadaId = null;
        nomeLojaSelecionada = "Todas as lojas";
        menuLojas.setText("Lojas");
        lblLojaSelecionada.setText("Loja selecionada: todas");
        carregarTodosMotoboys();
    }

    private void carregarMotoboysDaLoja() {
        if (lojaSelecionadaId == null) {
            carregarTodosMotoboys();
            return;
        }
        try {
            List<MotoboyItem> lista = motoboyApiClient.listar("", lojaSelecionadaId);
            motoboys.setAll(lista);
            aplicarFiltroLocal();
            lblMensagem.setText("Exibindo " + lista.size() + " motoboy(s) da loja " + nomeLojaSelecionada + ".");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            motoboys.clear();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            motoboys.clear();
            lblMensagem.setText("Erro ao carregar motoboys da loja.");
        }
    }

    private void carregarTodosMotoboys() {
        try {
            List<MotoboyItem> lista = motoboyApiClient.listar("");
            motoboys.setAll(lista);
            aplicarFiltroLocal();
            lblMensagem.setText("Exibindo " + lista.size() + " motoboy(s) de todas as lojas.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            motoboys.clear();
            lblMensagem.setText("Operacao interrompida.");
        } catch (IOException e) {
            motoboys.clear();
            lblMensagem.setText("Erro ao carregar motoboys.");
        }
    }

    private void aplicarFiltroLocal() {
        String filtro = txtPesquisaMotoboy.getText() == null ? "" : txtPesquisaMotoboy.getText().trim().toLowerCase();
        motoboysFiltrados.setPredicate(item -> {
            if (item == null) {
                return false;
            }
            String nome = item.getNome() == null ? "" : item.getNome().toLowerCase();
            return nome.contains(filtro);
        });
    }
}
