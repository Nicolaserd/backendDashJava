package com.nicolas.backenddash.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	long countByEmpresaId(UUID empresaId);

	List<Usuario> findByEmpresaId(UUID empresaId);

	List<Usuario> findByRol(UsuarioRol rol);

	@Modifying
	@Query("update Usuario u set u.estado = :estado where u.estado is null")
	int backfillNullEstado(@Param("estado") UsuarioEstado estado);
}
