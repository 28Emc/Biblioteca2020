package com.biblioteca2020.models.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Usuario;

public interface IUsuarioDao extends CrudRepository<Usuario, Long> {

	public Optional<Usuario> findById(Long id);

	public Usuario findByUsername(String username);

	public Usuario findByNroDocumento(String nroDocumento);

	public Usuario findByUsernameAndEstado(String username, boolean estado);

	@Query("select u from Usuario u join fetch u.roles r where r.authority='ROLE_EMPLEADO'")
	public List<Usuario> fetchByIdWithRoles();

}
