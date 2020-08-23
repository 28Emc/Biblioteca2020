package com.biblioteca2020.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.PrestamoLog;

public interface IPrestamoLogDao extends CrudRepository<PrestamoLog, Long> {

}
