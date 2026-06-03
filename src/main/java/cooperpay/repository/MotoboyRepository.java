package cooperpay.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cooperpay.domain.Motoboy;

public interface MotoboyRepository extends JpaRepository<Motoboy, Long> {

    List<Motoboy> findByNomeStartingWithIgnoreCase(String nome);

    List<Motoboy> findByNomeStartingWithIgnoreCaseAndLoja_Id(String nome, Long lojaId);

    List<Motoboy> findByLoja_Id(Long lojaId);

    boolean existsByNomeIgnoreCaseAndPixIgnoreCase(String nome, String pix);

    boolean existsByNomeIgnoreCaseAndPixIgnoreCaseAndIdNot(String nome, String pix, Long id);
}
