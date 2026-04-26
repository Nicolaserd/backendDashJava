package com.nicolas.backenddash.empresa;

import com.nicolas.backenddash.usuario.Usuario;
import com.nicolas.backenddash.usuario.UsuarioRepository;
import com.nicolas.backenddash.usuario.UsuarioRol;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MotherCompanyInitializer implements ApplicationRunner {

	private final EmpresaRepository empresaRepository;
	private final UsuarioRepository usuarioRepository;

	public MotherCompanyInitializer(EmpresaRepository empresaRepository, UsuarioRepository usuarioRepository) {
		this.empresaRepository = empresaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		Empresa mother = empresaRepository.findByNombreIgnoreCase(EmpresaRules.MOTHER_COMPANY_NAME)
				.orElseGet(() -> empresaRepository.save(new Empresa(EmpresaRules.MOTHER_COMPANY_NAME)));

		List<Usuario> superAdmins = usuarioRepository.findByRol(UsuarioRol.SUPER_ADMIN);
		for (Usuario usuario : superAdmins) {
			if (usuario.getEmpresa() == null || !mother.getId().equals(usuario.getEmpresa().getId())) {
				usuario.setEmpresa(mother);
			}
		}
	}
}
