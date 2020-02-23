package com.biblioteca2020.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.Role;

public interface IRoleService extends CrudRepository<Role, Long> {

}
