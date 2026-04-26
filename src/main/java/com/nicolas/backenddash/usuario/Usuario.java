package com.nicolas.backenddash.usuario;

import com.nicolas.backenddash.empresa.Empresa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 80)
	private String nombre;

	@Column(nullable = false, length = 120)
	private String apellidos;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private UsuarioRol rol;

	@Column(nullable = false, unique = true, length = 180)
	private String email;

	@ManyToOne
	@JoinColumn(name = "empresa_id")
	private Empresa empresa;

	@Column(nullable = false, length = 255)
	private String passwordHash;

	@Column(nullable = false)
	private Boolean activo;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime updatedAt;

	protected Usuario() {
	}

	public Usuario(String nombre, String apellidos, UsuarioRol rol, String email, String passwordHash, Boolean activo) {
		this(nombre, apellidos, rol, email, null, passwordHash, activo);
	}

	public Usuario(
			String nombre,
			String apellidos,
			UsuarioRol rol,
			String email,
			Empresa empresa,
			String passwordHash,
			Boolean activo
	) {
		this.nombre = nombre;
		this.apellidos = apellidos;
		this.rol = rol;
		this.email = email;
		this.empresa = empresa;
		this.passwordHash = passwordHash;
		this.activo = activo != null ? activo : Boolean.TRUE;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		createdAt = now;
		updatedAt = now;
		if (activo == null) {
			activo = Boolean.TRUE;
		}
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = LocalDateTime.now();
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

	public String getApellidos() {
		return apellidos;
	}

	public void setApellidos(String apellidos) {
		this.apellidos = apellidos;
	}

	public UsuarioRol getRol() {
		return rol;
	}

	public void setRol(UsuarioRol rol) {
		this.rol = rol;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Boolean getActivo() {
		return activo;
	}

	public void setActivo(Boolean activo) {
		this.activo = activo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}
