package com.nicolas.backenddash.dashboard;

import com.nicolas.backenddash.empresa.Empresa;
import com.nicolas.backenddash.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "dashboards")
public class Dashboard {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 120)
	private String nombre;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DashboardType tipo;

	@Column(nullable = false, columnDefinition = "text")
	private String contenido;

	@ManyToOne
	@JoinColumn(name = "creador_id")
	private Usuario creador;

	@ManyToOne
	@JoinColumn(name = "empresa_id")
	private Empresa empresa;

	@ManyToMany
	@JoinTable(
			name = "dashboard_usuarios",
			joinColumns = @JoinColumn(name = "dashboard_id"),
			inverseJoinColumns = @JoinColumn(name = "usuario_id")
	)
	private Set<Usuario> usuariosAsignados = new LinkedHashSet<>();

	protected Dashboard() {
	}

	public Dashboard(String nombre, DashboardType tipo, String contenido, Usuario creador, Empresa empresa) {
		this.nombre = nombre;
		this.tipo = tipo;
		this.contenido = contenido;
		this.creador = creador;
		this.empresa = empresa;
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

	public DashboardType getTipo() {
		return tipo;
	}

	public void setTipo(DashboardType tipo) {
		this.tipo = tipo;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public Usuario getCreador() {
		return creador;
	}

	public void setCreador(Usuario creador) {
		this.creador = creador;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Set<Usuario> getUsuariosAsignados() {
		return usuariosAsignados;
	}

	public void assignUsuario(Usuario usuario) {
		usuariosAsignados.add(usuario);
	}

	public void unassignUsuario(Usuario usuario) {
		usuariosAsignados.remove(usuario);
	}
}
