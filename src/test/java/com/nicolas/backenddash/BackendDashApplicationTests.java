package com.nicolas.backenddash;

import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.empresa.EmpresaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BackendDashApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private EmpresaRepository empresaRepository;

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
				.andExpect(jsonPath("$.usuario.rol").value("DASHBOARD_USUARIO"));
	}

}
