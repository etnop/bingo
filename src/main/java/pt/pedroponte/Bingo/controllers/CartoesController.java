package pt.pedroponte.Bingo.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import pt.pedroponte.Bingo.CartoesPdf;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pt.pedroponte.Bingo.JavaFXApplication;

@Controller
@Scope("prototype")
public class CartoesController {

    @FXML
    private VBox main;
    @FXML
    private Label resumo;
    @FXML
    private Button btnJogar;
    @FXML
    private Button btnImprimir;
    @FXML
    private ComboBox<Path> sessoes;
    @FXML
    private Button btnRenomear;
    @FXML
    private Button btnApagarSessao;
    @FXML
    private Button btnAdicionar;
    @FXML
    private Spinner<Integer> qtdAdicionar;
    @FXML
    private javafx.scene.control.ProgressIndicator aGerar;
    @FXML
    private javafx.scene.control.Pagination paginas;
    private static final int POR_PAGINA = 24;
    @FXML
    private Label vazio;

    private ObservableList<List<List<String>>> cartoes;

    public void initialize() {
        cartoes = FXCollections.observableArrayList();

        sessoes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Path p) {
                return p == null ? "" : nomeSessao(p);
            }

            @Override
            public Path fromString(String s) {
                return null;
            }
        });
        sessoes.getSelectionModel().selectedItemProperty().addListener((o, a, sessao) -> {
            if (sessao == null) return;
            JavaFXApplication.carregarCartoes(sessao);
            // envolve a lista partilhada: apagar aqui apaga também no jogo
            cartoes = FXCollections.observableList(JavaFXApplication.cartoes);
            cartoes.addListener((ListChangeListener<List<List<String>>>) c -> desenhar());
            ligarResumo();
            desenhar();
        });

        qtdAdicionar.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 5));
        var semSessao = sessoes.valueProperty().isNull();
        btnRenomear.disableProperty().bind(semSessao);
        btnApagarSessao.disableProperty().bind(semSessao);
        btnAdicionar.disableProperty().bind(semSessao);

        ligarResumo();
        recarregarSessoes(JavaFXApplication.sessaoAtual); // abre na sessão escolhida no menu, se houver
    }

    private void recarregarSessoes(Path selecionar) {
        sessoes.getItems().setAll(JavaFXApplication.listarSessoes());
        if (selecionar != null && sessoes.getItems().contains(selecionar)) sessoes.getSelectionModel().select(selecionar);
        else sessoes.getSelectionModel().selectFirst();
        if (sessoes.getItems().isEmpty()) {
            cartoes = FXCollections.observableArrayList();
            ligarResumo();
            desenhar();
        }
    }

    @FXML
    public void renomear() {
        Path atual = sessoes.getValue();
        TextInputDialog d = new TextInputDialog(nomeSessao(atual));
        d.setTitle("Renomear sessão");
        d.setHeaderText(null);
        d.setContentText("Nome:");
        d.showAndWait().map(String::trim).filter(n -> !n.isEmpty()).ifPresent(nome -> {
            // nome vira nome de ficheiro: tira caracteres inválidos
            String seguro = nome.replaceAll("[\\\\/:*?\"<>|]", "-");
            Path novo = atual.resolveSibling(seguro + ".txt");
            if (Files.exists(novo)) {
                new Alert(Alert.AlertType.WARNING, "Já existe uma sessão com esse nome.").showAndWait();
                return;
            }
            try {
                Files.move(atual, novo);
                recarregarSessoes(novo);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Não foi possível renomear: " + e.getMessage()).showAndWait();
            }
        });
    }

    @FXML
    public void apagarSessao() {
        Path atual = sessoes.getValue();
        var r = new Alert(Alert.AlertType.CONFIRMATION,
                "Apagar a sessão \"" + nomeSessao(atual) + "\" e todos os seus cartões?",
                ButtonType.YES, ButtonType.NO).showAndWait();
        if (r.orElse(ButtonType.NO) != ButtonType.YES) return;
        try {
            Files.deleteIfExists(atual);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível apagar: " + e.getMessage()).showAndWait();
        }
        recarregarSessoes(JavaFXApplication.sessaoAtual); // abre na sessão escolhida no menu, se houver
    }

    @FXML
    public void adicionar() {
        // spinner editável só guarda o texto escrito com Enter: força a leitura
        try {
            qtdAdicionar.getValueFactory().setValue(Integer.parseInt(qtdAdicionar.getEditor().getText().trim()));
        } catch (NumberFormatException ignored) {
            qtdAdicionar.getEditor().setText(String.valueOf(qtdAdicionar.getValue()));
        }
        int n = qtdAdicionar.getValue();
        var existentes = new java.util.HashSet<>(cartoes);
        javafx.concurrent.Task<List<List<List<String>>>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<List<List<String>>> call() {
                List<List<List<String>>> novos = new java.util.ArrayList<>();
                while (novos.size() < n) {
                    List<List<String>> c = MainController.gerarCartao();
                    if (existentes.add(c)) novos.add(c);
                }
                return novos;
            }
        };
        aGerar.setVisible(true);
        btnAdicionar.disableProperty().unbind();
        btnAdicionar.setDisable(true);
        task.setOnSucceeded(e -> {
            cartoes.addAll(task.getValue()); // um só redesenho
            JavaFXApplication.guardarCartoes();
            terminar();
        });
        task.setOnFailed(e -> {
            terminar();
            new Alert(Alert.AlertType.ERROR, "Erro a gerar cartões: " + task.getException()).showAndWait();
        });
        Thread t = new Thread(task, "gerar-cartoes");
        t.setDaemon(true);
        t.start();
    }

    private void terminar() {
        aGerar.setVisible(false);
        btnAdicionar.disableProperty().bind(sessoes.valueProperty().isNull());
    }

    /** Desenha cada cartão como uma mini-grelha 5x5, igual ao PDF. */
    private void desenhar() {
        vazio.setVisible(cartoes.isEmpty());
        vazio.setManaged(cartoes.isEmpty());
        int atual = paginas.getCurrentPageIndex();
        int total = Math.max(1, (cartoes.size() + POR_PAGINA - 1) / POR_PAGINA);
        paginas.setPageCount(total);
        // só a página visível é desenhada: 24 cartões em vez de todos
        paginas.setPageFactory(this::pagina);
        paginas.setCurrentPageIndex(Math.min(atual, total - 1));
    }

    private javafx.scene.Node pagina(int p) {
        FlowPane grelha = new FlowPane(14, 14);
        grelha.setPadding(new javafx.geometry.Insets(4));
        int fim = Math.min(cartoes.size(), (p + 1) * POR_PAGINA);
        for (int i = p * POR_PAGINA; i < fim; i++) {
            int idx = i;
            Label titulo = new Label("Cartão " + (i + 1));
            titulo.getStyleClass().add("cartao-titulo");
            Button apagar = new Button("✕");
            apagar.getStyleClass().add("cartao-apagar");
            apagar.setOnAction(e -> {
                var r = new Alert(Alert.AlertType.CONFIRMATION, "Apagar o cartão " + (idx + 1) + "?",
                        ButtonType.YES, ButtonType.NO).showAndWait();
                if (r.orElse(ButtonType.NO) != ButtonType.YES) return;
                cartoes.remove(idx);
                JavaFXApplication.guardarCartoes();
            });
            Region esp = new Region();
            HBox.setHgrow(esp, Priority.ALWAYS);
            HBox topo = new HBox(titulo, esp, apagar);
            topo.setAlignment(Pos.CENTER_LEFT);

            GridPane g = new GridPane();
            g.setHgap(3);
            g.setVgap(3);
            for (int c = 0; c < 5; c++) {
                Label l = celula(String.valueOf("BINGO".charAt(c)), "cartao-letra");
                g.add(l, c, 0);
            }
            List<List<String>> grade = cartoes.get(i);
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    String n = grade.get(r).get(c);
                    boolean livre = n.equals("Livre");
                    g.add(celula(livre ? "★" : n, livre ? "cartao-livre" : "cartao-num"), c, r + 1);
                }
            }
            VBox cartao = new VBox(6, topo, g);
            cartao.getStyleClass().add("cartao");
            grelha.getChildren().add(cartao);
        }
        javafx.scene.control.ScrollPane sp = new javafx.scene.control.ScrollPane(grelha);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(javafx.scene.control.ScrollPane.ScrollBarPolicy.NEVER);
        return sp;
    }

    private static Label celula(String texto, String estilo) {
        Label l = new Label(texto);
        l.getStyleClass().add(estilo);
        l.setAlignment(Pos.CENTER);
        l.setMinSize(30, 26);
        l.setPrefSize(30, 26);
        return l;
    }

    private void ligarResumo() {
        resumo.textProperty().bind(Bindings.size(cartoes).asString("%d cartões"));
        btnJogar.disableProperty().bind(Bindings.isEmpty(cartoes));
        btnImprimir.disableProperty().bind(Bindings.isEmpty(cartoes));
    }

    /** "2026-11-13_15-22-05.txt" -> "2026-11-13 15:22:05" */
    static String nomeSessao(Path p) {
        String n = p.getFileName().toString().replaceFirst("\\.txt$", "");
        // nome automático com data/hora fica legível; nomes dados pelo utilizador ficam iguais
        return n.matches("\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2}")
                ? n.replace('_', ' ').replaceFirst("(\\d{2})-(\\d{2})-(\\d{2})$", "$1:$2:$3")
                : n;
    }

    @FXML
    public void imprimir() {
        Path sessao = sessoes.getValue();
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar cartões em PDF");
        fc.setInitialFileName("bingo-" + sessao.getFileName().toString().replace(".txt", ".pdf"));
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        var ficheiro = fc.showSaveDialog(main.getScene().getWindow());
        if (ficheiro == null) return;
        try {
            CartoesPdf.gerar(cartoes, nomeSessao(sessao), ficheiro.toPath());
            new Alert(Alert.AlertType.INFORMATION, "PDF criado:\n" + ficheiro).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Não foi possível criar o PDF: " + e.getMessage()).showAndWait();
        }
    }

    @FXML
    public void voltar() {
        JavaFXApplication.abrir("main.fxml", main);
    }

    @FXML
    public void jogar() {
        JavaFXApplication.abrir("bingo.fxml", main);
    }
}
