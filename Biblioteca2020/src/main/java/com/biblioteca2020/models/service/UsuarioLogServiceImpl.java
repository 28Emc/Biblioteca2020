package com.biblioteca2020.models.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.biblioteca2020.models.dao.IUsuarioLogDao;
import com.biblioteca2020.models.entity.UsuarioLog;

@Service
public class UsuarioLogServiceImpl implements IUsuarioLogService {

	@Autowired
	private IUsuarioLogDao usuarioLogDao;

	@Override
	@Transactional(readOnly = true)
	public List<UsuarioLog> findAll() {
		return (List<UsuarioLog>) usuarioLogDao.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public UsuarioLog findById(Long id) {
		return usuarioLogDao.findById(id).get();
	}

	@Override
	@Transactional
	public void save(UsuarioLog usuarioLog) {
		//usuarioLog.setPassword(passwordEncoder.encode(usuarioLog.getPassword()));
		usuarioLogDao.save(usuarioLog);
	}

}
