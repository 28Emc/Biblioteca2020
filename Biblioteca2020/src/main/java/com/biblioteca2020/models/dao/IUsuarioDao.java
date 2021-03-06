package com.biblioteca2020.models.dao;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {
	// USADO
	public Usuario findByUsername(String username);

	// USADO
	public Usuario findByNroDocumento(String nroDocumento);

	// USADO
	public Usuario findByNroDocumentoAndEmail(String nroDocumento, String email);

	// USADO
	@Query("select u from Usuario u where u.nroDocumento like ?1 and u.estado = ?2")
	public List<Usuario> findByNroDocumentoAndEstado(String term, Boolean estado);

	// USADO
	public Usuario findByUsernameAndEstado(String username, boolean estado);

	@Query("select u from Usuario u where u.username not in ?1")
	public List<Usuario> findByAnotherUsername(String username);

	public Usuario findByCelular(String celular);

	// VALIDACIÓN EMAIL
	// USADO
	public Usuario findByEmailIgnoreCase(String email);

}
