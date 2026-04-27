package com.nicolas.backenddash.empresa;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmpresaActivaInitializer implements ApplicationRunner {

	private final EmpresaRepository empresaRepository;

	public EmpresaActivaInitializer(EmpresaRepository empresaRepository) {
		this.empresaRepository = empresaRepository;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		empresaRepository.backfillNullActiva();
	}
}
