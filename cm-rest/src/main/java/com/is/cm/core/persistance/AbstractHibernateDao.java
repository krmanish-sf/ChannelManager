package com.is.cm.core.persistance;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractHibernateDao<T extends Serializable> {
	private Class<T> clazz;

	@Autowired
	private SessionFactory sessionFactory;

	public void setClazz(final Class<T> clazzToSet) {
		clazz = clazzToSet;
	}

	public T findOne(final int id) {
		return (T) getCurrentSession().get(clazz, id);
	}

	public List<T> findAll() {
		return getCurrentSession().createQuery("from " + clazz.getName())
				.list();
	}

	public void create(final T entity) {
		getCurrentSession().persist(entity);
	}

	public T update(final T entity) {
		return (T) getCurrentSession().merge(entity);
	}

	public void delete(final T entity) {
		getCurrentSession().delete(entity);
	}

	public void deleteById(final int id) {
		final T entity = findOne(id);
		delete(entity);
	}

	protected final Session getCurrentSession() {
		return sessionFactory.getCurrentSession();
	}
}
