package com.nicolas.backenddash.usuario;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UsuarioEstadoInitializer implements ApplicationRunner {

	private final UsuarioRepository usuarioRepository;

	public UsuarioEstadoInitializer(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		usuarioRepository.backfillNullEstado(UsuarioEstado.APROBADO);
	}
}
