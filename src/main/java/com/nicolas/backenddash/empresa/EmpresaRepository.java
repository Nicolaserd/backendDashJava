package com.nicolas.backenddash.empresa;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmpresaRepository extends JpaRepository<Empresa, UUID> {

	Optional<Empresa> findByNombreIgnoreCase(String nombre);

	boolean existsByNombreIgnoreCase(String nombre);

	boolean existsByIdAndActivaTrue(UUID id);

	@Modifying
	@Query("update Empresa e set e.activa = true where e.activa is null")
	int backfillNullActiva();
}
