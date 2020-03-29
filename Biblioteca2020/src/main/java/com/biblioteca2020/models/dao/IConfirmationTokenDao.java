package com.biblioteca2020.models.dao;

import org.springframework.data.repository.CrudRepository;
import com.biblioteca2020.models.entity.ConfirmationToken;

public interface IConfirmationTokenDao extends CrudRepository<ConfirmationToken, Long> {
	public ConfirmationToken findByConfirmationToken(String confirmationToken);
}
