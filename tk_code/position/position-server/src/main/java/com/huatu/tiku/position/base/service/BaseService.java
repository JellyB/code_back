package com.huatu.tiku.position.base.service;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Service基类
 * 
 * @author geeks
 *
 * @param <T>
 *            对象类型
 * @param <ID>
 *            对象主键类型
 */
public interface BaseService<T, ID extends Serializable> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#findAll()
	 */
	List<T> findAll();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.PagingAndSortingRepository#findAll(org.
	 * springframework.data.domain.Sort)
	 */
	List<T> findAll(Sort sort);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.data.repository.CrudRepository#findAll(java.lang.
	 * Iterable)
	 */
	List<T> findAll(Iterable<ID> ids);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.CrudRepository#save(java.lang.Iterable)
	 */
	<S extends T> List<S> save(Iterable<S> entities);

	/**
	 * Flushes all pending changes to the database.
	 */
	void flush();

	/**
	 * Saves an entity and flushes changes instantly.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	<S extends T> S saveAndFlush(S entity);

	/**
	 * Deletes the given entities in a batch which means it will create a single
	 * {@link Query}. Assume that we will clear the
	 * {@link EntityManager} after the call.
	 * 
	 * @param entities
	 */
	void deleteInBatch(Iterable<T> entities);

	/**
	 * Deletes all entities in a batch call.
	 */
	void deleteAllInBatch();

	/**
	 * Returns a reference to the entity with the given identifier.
	 * 
	 * @param id
	 *            must not be {@literal null}.
	 * @return a reference to the entity with the given identifier.
	 * @see EntityManager#getReference(Class, Object)
	 */
	T getOne(ID id);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.
	 * springframework.data.domain.Example)
	 */
	<S extends T> List<S> findAll(Example<S> example);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.data.repository.query.QueryByExampleExecutor#findAll(org.
	 * springframework.data.domain.Example, org.springframework.data.domain.Sort)
	 */
	<S extends T> List<S> findAll(Example<S> example, Sort sort);

	/**
	 * Saves a given entity. Use the returned instance for further operations as the
	 * save operation might have changed the entity instance completely.
	 * 
	 * @param entity
	 * @return the saved entity
	 */
	<S extends T> S save(S entity);

	/**
	 * Retrieves an entity by its id.
	 * 
	 * @param id
	 *            must not be {@literal null}.
	 * @return the entity with the given id or {@literal null} if none found
	 * @throws IllegalArgumentException
	 *             if {@code id} is {@literal null}
	 */
	T findOne(ID id);

	/**
	 * Returns whether an entity with the given id exists.
	 * 
	 * @param id
	 *            must not be {@literal null}.
	 * @return true if an entity with the given id exists, {@literal false}
	 *         otherwise
	 * @throws IllegalArgumentException
	 *             if {@code id} is {@literal null}
	 */
	boolean exists(ID id);

	/**
	 * Returns the number of entities available.
	 * 
	 * @return the number of entities
	 */
	long count();

	/**
	 * Deletes the entity with the given id.
	 * 
	 * @param id
	 *            must not be {@literal null}.
	 * @throws IllegalArgumentException
	 *             in case the given {@code id} is {@literal null}
	 */
	void delete(ID id);

	/**
	 * Deletes a given entity.
	 * 
	 * @param entity
	 * @throws IllegalArgumentException
	 *             in case the given entity is {@literal null}.
	 */
	void delete(T entity);

	void delete(Collection<? extends ID> ids);

	/**
	 * Deletes the given entities.
	 * 
	 * @param entities
	 * @throws IllegalArgumentException
	 *             in case the given {@link Iterable} is {@literal null}.
	 */
	void delete(Iterable<? extends T> entities);

	/**
	 * Deletes all entities managed by the repository.
	 */
	void deleteAll();

	/**
	 * Returns a {@link Page} of entities meeting the paging restriction provided in
	 * the {@code Pageable} object.
	 * 
	 * @param pageable
	 * @return a page of entities
	 */
	Page<T> findAll(Pageable pageable);

}
