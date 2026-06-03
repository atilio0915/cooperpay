package cooperpay.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "motoboy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Motoboy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    private String pix;

    private Boolean trabalhou = false;

    @ManyToOne
    @JoinColumn(name = "loja_id")
    private Loja loja;
}
