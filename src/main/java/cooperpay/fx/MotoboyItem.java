package cooperpay.fx;

public class MotoboyItem {

    private Long id;
    private String nome;
    private String pix;
    private Boolean trabalhou;
    private Long lojaId;
    private String lojaNome;

    public MotoboyItem(Long id, String nome, String pix, Boolean trabalhou) {
        this(id, nome, pix, trabalhou, null, null);
    }

    public MotoboyItem(Long id, String nome, String pix, Boolean trabalhou, Long lojaId, String lojaNome) {
        this.id = id;
        this.nome = nome;
        this.pix = pix;
        this.trabalhou = trabalhou;
        this.lojaId = lojaId;
        this.lojaNome = lojaNome;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPix() {
        return pix;
    }

    public void setPix(String pix) {
        this.pix = pix;
    }

    public Boolean getTrabalhou() {
        return trabalhou;
    }

    public void setTrabalhou(Boolean trabalhou) {
        this.trabalhou = trabalhou;
    }

    public String getTrabalhouTexto() {
        return Boolean.TRUE.equals(trabalhou) ? "Sim" : "Nao";
    }

    public Long getLojaId() {
        return lojaId;
    }

    public void setLojaId(Long lojaId) {
        this.lojaId = lojaId;
    }

    public String getLojaNome() {
        return lojaNome;
    }

    public void setLojaNome(String lojaNome) {
        this.lojaNome = lojaNome;
    }
}
