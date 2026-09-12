package sv.edu.udb.cfc.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import sv.edu.udb.cfc.client.entity.Cliente;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long>, JpaSpecificationExecutor<Cliente> {

    boolean existsByNit(String nit);

    boolean existsByNitAndIdNot(String nit, Long id);

    boolean existsByDui(String dui);

    boolean existsByDuiAndIdNot(String dui, Long id);

    Optional<Cliente> findByNit(String nit);
}