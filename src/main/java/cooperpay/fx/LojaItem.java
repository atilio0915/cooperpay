package cooperpay.fx;

public class LojaItem {
    private final Long id;
    private final String nome;

    public LojaItem(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
