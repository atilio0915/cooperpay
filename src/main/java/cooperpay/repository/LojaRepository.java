package cooperpay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cooperpay.domain.Loja;

public interface LojaRepository extends JpaRepository<Loja, Long> {
}
