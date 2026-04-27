package com.nicolas.backenddash;

import com.nicolas.backenddash.auth.JwtService;
import com.nicolas.backenddash.dashboard.Dashboard;
import com.nicolas.backenddash.dashboard.DashboardRepository;
import com.nicolas.backenddash.dashboard.DashboardType;
import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioEstado;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import com.nicolas.backenddash.usuario.UsuarioRol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendDashApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmpresaRepository empresaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private DashboardRepository dashboardRepository;

	@Autowired
	private JwtService jwtService;

	@Test
	void contextLoads() {
	}

	@Test
	void registerCreatesDashboardConsumerWithoutBearerToken() throws Exception {
		Empresa empresa = empresaRepository.save(new Empresa("Acme Test"));
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario",
								  "apellidos": "Consumidor",
								  "email": "consumidor-test@example.com",
								  "password": "password123",
								  "empresaId": "%s"
								}
								""".formatted(empresa.getId())))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token", notNullValue()))
				.andExpect(jsonPath("$.usuario.rol").value("DASHBOARD_USUARIO"))
				.andExpect(jsonPath("$.usuario.estado").value("NO_APROBADO"));
	}

	@Test
	void loginFailsWhenUserIsNotApproved() throws Exception {
		Empresa empresa = empresaRepository.save(new Empresa("Empresa Login No Aprobado"));
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "No",
								  "apellidos": "Aprobado",
								  "email": "no-aprobado-login@example.com",
								  "password": "password123",
								  "empresaId": "%s"
								}
								""".formatted(empresa.getId())))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "no-aprobado-login@example.com",
								  "password": "password123"
								}
								"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("User is not approved yet"));
	}

	@Test
	void registerFailsWithClearMessageWhenEmpresaDoesNotExist() throws Exception {
		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "Usuario",
								  "apellidos": "Consumidor",
								  "email": "consumidor-no-empresa@example.com",
								  "password": "password123",
								  "empresaId": "%s"
								}
								""".formatted(UUID.randomUUID())))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("empresaId must belong to an existing empresa"));
	}

	@Test
	void listRegisterEmpresasIsPublic() throws Exception {
		empresaRepository.save(new Empresa("Empresa Registro Publica"));

		mockMvc.perform(get("/api/empresas"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", notNullValue()))
				.andExpect(jsonPath("$[0].nombre", notNullValue()));
	}

	@Test
	void assigningSameConsumerTwiceReturnsConflict() throws Exception {
		Empresa empresa = empresaRepository.save(new Empresa("Empresa Dash Assign"));
		Usuario admin = usuarioRepository.save(new Usuario(
				"Admin",
				"Empresa",
				UsuarioRol.ADMIN,
				"admin-assign-" + UUID.randomUUID() + "@example.com",
				empresa,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario creador = usuarioRepository.save(new Usuario(
				"Creador",
				"Dash",
				UsuarioRol.DASHBOARD_CREADOR,
				"creador-assign-" + UUID.randomUUID() + "@example.com",
				empresa,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario consumidor = usuarioRepository.save(new Usuario(
				"Consumidor",
				"Dash",
				UsuarioRol.DASHBOARD_USUARIO,
				"consumidor-assign-" + UUID.randomUUID() + "@example.com",
				empresa,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Dashboard dashboard = dashboardRepository.save(new Dashboard(
				"Dashboard Assign",
				DashboardType.TEMPLATE,
				"<h1>mock</h1>",
				creador,
				empresa
		));
		String token = jwtService.generateToken(admin);
		String body = """
				{
				  "usuarioId": "%s"
				}
				""".formatted(consumidor.getId());

		mockMvc.perform(post("/api/dashboards/" + dashboard.getId() + "/usuarios")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/dashboards/" + dashboard.getId() + "/usuarios")
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content(body))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Usuario is already assigned to this dashboard"));
	}

	@Test
	void adminCannotChangeDashboardEmpresaButSuperAdminCan() throws Exception {
		Empresa empresaA = empresaRepository.save(new Empresa("Empresa A"));
		Empresa empresaB = empresaRepository.save(new Empresa("Empresa B"));

		Usuario adminEmpresaA = usuarioRepository.save(new Usuario(
				"Admin",
				"A",
				UsuarioRol.ADMIN,
				"admin-empresa-a-" + UUID.randomUUID() + "@example.com",
				empresaA,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario creadorEmpresaA = usuarioRepository.save(new Usuario(
				"Creador",
				"A",
				UsuarioRol.DASHBOARD_CREADOR,
				"creador-empresa-a-" + UUID.randomUUID() + "@example.com",
				empresaA,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario creadorEmpresaB = usuarioRepository.save(new Usuario(
				"Creador",
				"B",
				UsuarioRol.DASHBOARD_CREADOR,
				"creador-empresa-b-" + UUID.randomUUID() + "@example.com",
				empresaB,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario superAdmin = usuarioRepository.save(new Usuario(
				"Super",
				"Admin",
				UsuarioRol.SUPER_ADMIN,
				"super-admin-" + UUID.randomUUID() + "@example.com",
				null,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));

		Dashboard dashboard = dashboardRepository.save(new Dashboard(
				"Dashboard Empresa A",
				DashboardType.TEMPLATE,
				"<h1>a</h1>",
				creadorEmpresaA,
				empresaA
		));

		String adminToken = jwtService.generateToken(adminEmpresaA);
		String superAdminToken = jwtService.generateToken(superAdmin);

		String adminMoveBody = """
				{
				  "nombre": "Dashboard Empresa A",
				  "tipo": "TEMPLATE",
				  "contenido": "<h1>a</h1>",
				  "creadorId": "%s",
				  "empresaId": "%s"
				}
				""".formatted(creadorEmpresaA.getId(), empresaB.getId());

		mockMvc.perform(put("/api/dashboards/" + dashboard.getId())
						.header("Authorization", "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(adminMoveBody))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Only SUPER_ADMIN can change dashboard empresa"));

		String superAdminMoveBody = """
				{
				  "nombre": "Dashboard Empresa B",
				  "tipo": "TEMPLATE",
				  "contenido": "<h1>b</h1>",
				  "creadorId": "%s",
				  "empresaId": "%s"
				}
				""".formatted(creadorEmpresaB.getId(), empresaB.getId());

		mockMvc.perform(put("/api/dashboards/" + dashboard.getId())
						.header("Authorization", "Bearer " + superAdminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content(superAdminMoveBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.empresaId").value(empresaB.getId().toString()))
				.andExpect(jsonPath("$.creadorId").value(creadorEmpresaB.getId().toString()));
	}

	@Test
	void superAdminCanCreateDashboardInAnyEmpresa() throws Exception {
		Empresa empresaA = empresaRepository.save(new Empresa("Empresa SA Create A"));
		Empresa empresaB = empresaRepository.save(new Empresa("Empresa SA Create B"));

		Usuario creatorB = usuarioRepository.save(new Usuario(
				"Creator",
				"B",
				UsuarioRol.DASHBOARD_CREADOR,
				"creator-b-create-" + UUID.randomUUID() + "@example.com",
				empresaB,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		Usuario superAdmin = usuarioRepository.save(new Usuario(
				"Super",
				"Admin",
				UsuarioRol.SUPER_ADMIN,
				"super-admin-create-" + UUID.randomUUID() + "@example.com",
				empresaA,
				"hash",
				true,
				UsuarioEstado.APROBADO
		));
		String superAdminToken = jwtService.generateToken(superAdmin);

		mockMvc.perform(post("/api/dashboards")
						.header("Authorization", "Bearer " + superAdminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "nombre": "SA Dash B",
								  "tipo": "TEMPLATE",
								  "contenido": "<h1>b</h1>",
								  "creadorId": "%s",
								  "empresaId": "%s"
								}
								""".formatted(creatorB.getId(), empresaB.getId())))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.empresaId").value(empresaB.getId().toString()))
				.andExpect(jsonPath("$.creadorId").value(creatorB.getId().toString()));
	}

}
