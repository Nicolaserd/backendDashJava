package com.nicolas.backenddash.usuario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	long countByEmpresaId(UUID empresaId);

	List<Usuario> findByEmpresaId(UUID empresaId);

	List<Usuario> findByRol(UsuarioRol rol);
}
