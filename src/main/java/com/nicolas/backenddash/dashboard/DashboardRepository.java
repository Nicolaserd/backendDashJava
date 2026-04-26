package com.nicolas.backenddash.dashboard;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {

	List<Dashboard> findByEmpresaId(UUID empresaId);

	List<Dashboard> findByCreadorId(UUID creadorId);

	List<Dashboard> findByUsuariosAsignados_Id(UUID usuarioId);

	List<Dashboard> findByEmpresaIdAndCreadorId(UUID empresaId, UUID creadorId);

	List<Dashboard> findByEmpresaIdAndUsuariosAsignados_Id(UUID empresaId, UUID usuarioId);
}
