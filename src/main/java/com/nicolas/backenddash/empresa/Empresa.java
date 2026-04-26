package com.nicolas.backenddash.empresa;

import com.nicolas.backenddash.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "empresas")
public class Empresa {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 160)
	private String nombre;

	@Column(nullable = false)
	private Integer numeroEmpleados;

	@OneToMany(mappedBy = "empresa")
	private Set<Usuario> usuarios = new LinkedHashSet<>();

	protected Empresa() {
	}

	public Empresa(String nombre) {
		this.nombre = nombre;
		this.numeroEmpleados = 0;
	}

	public UUID getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getNumeroEmpleados() {
		return numeroEmpleados;
	}

	public void setNumeroEmpleados(Integer numeroEmpleados) {
		this.numeroEmpleados = numeroEmpleados;
	}

	public Set<Usuario> getUsuarios() {
		return usuarios;
	}
}
