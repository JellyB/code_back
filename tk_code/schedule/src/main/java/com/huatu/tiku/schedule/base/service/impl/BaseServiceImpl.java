package com.huatu.tiku.schedule.base.service.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import com.huatu.tiku.schedule.base.repository.BaseRepository;
import com.huatu.tiku.schedule.base.service.BaseService;

public class BaseServiceImpl<T, ID extends Serializable> implements BaseService<T, ID> {
	
	@Autowired
	private BaseRepository<T, ID> repository;

	@Override
	public List<T> findAll() {
		return repository.findAll();
	}

	@Override
	public List<T> findAll(Sort sort) {
		return repository.findAll();
	}

	@Override
	public List<T> findAll(Iterable<ID> ids) {
		return repository.findAll(ids);
	}

	@Override
	public <S extends T> List<S> save(Iterable<S> entities) {
		return repository.save(entities);
	}

	@Override
	public void flush() {
		repository.flush();
	}

	@Override
	public <S extends T> S saveAndFlush(S entity) {
		return repository.saveAndFlush(entity);
	}

	@Override
	public void deleteInBatch(Iterable<T> entities) {
		repository.deleteInBatch(entities);
	}

	@Override
	public void deleteAllInBatch() {
		repository.deleteAllInBatch();
	}

	@Override
	public T getOne(ID id) {
		return repository.getOne(id);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example) {
		return repository.findAll(example);
	}

	@Override
	public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
		return repository.findAll(example, sort);
	}

	@Override
	public <S extends T> S save(S entity) {
		return repository.save(entity);
	}

	@Override
	public T findOne(ID id) {
		return repository.findOne(id);
	}

	@Override
	public boolean exists(ID id) {
		return repository.exists(id);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void delete(ID id) {
		repository.delete(id);
	}

	@Override
	public void delete(T entity) {
		repository.delete(entity);
	}

	@Override
	@Transactional
	public void delete(Collection<? extends ID> ids) {
		ids.forEach(id -> {
			repository.delete(id);
		});
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		repository.delete(entities);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public Page<T> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

}
