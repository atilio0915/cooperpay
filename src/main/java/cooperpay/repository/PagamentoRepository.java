package cooperpay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cooperpay.domain.EnumStatus;
import cooperpay.domain.Pagamento;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    boolean existsByMotoboyIdAndSemanaReferenciaAndStatus(Long idMotoboy, String semanaReferencia, EnumStatus status);
    Optional<Pagamento> findByMotoboyIdAndSemanaReferenciaAndStatus(Long idMotoboy, String semanaReferencia, EnumStatus status);
    List<Pagamento> findBySemanaReferenciaAndStatus(String semanaReferencia, EnumStatus status);
    
    Optional<Pagamento> findByTrasactinIdPix(String trasactinIdPix);

    @Query("""
    select p from Pagamento p
    where (:motoboyId is null or p.motoboy.id = :motoboyId)
      and (:semana is null or p.semanaReferencia = :semana)
      and (:status is null or p.status = :status)
    """)
    List<Pagamento> filtrar(
            @Param("motoboyId") Long motoboyId,
            @Param("semana") String semanaReferencia,
            @Param("status") EnumStatus status);
}
