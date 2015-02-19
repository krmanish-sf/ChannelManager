package com.is.cm.core.persistance;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import salesmachine.hibernatedb.Reps;
import salesmachine.hibernatehelper.SessionManager;
import salesmachine.util.StringHandle;

public class UserRepositoryDB implements UserRepository {
	private static final Logger LOG = LoggerFactory
			.getLogger(UserRepositoryDB.class);

	@Override
	public Reps login(String userName, String password) {
		if (StringHandle.isNullOrEmpty(userName)
				|| StringHandle.isNullOrEmpty(password)) {
			return null;
		}
		LOG.debug("Authenticating user {}", userName);
		Session dbSession = SessionManager.currentSession();
		return validateLogin(dbSession, userName, password);
	}

	static Reps validateLogin(Session dbSession, String login, String password) {
		salesmachine.hibernatedb.Reps r = null;
		try {
			Criteria createCriteria = dbSession
					.createCriteria(salesmachine.hibernatedb.Reps.class);
			createCriteria.add(Expression.eq("login", login).ignoreCase());
			createCriteria.add(Expression.eq("password", password));
			r = (salesmachine.hibernatedb.Reps) createCriteria.uniqueResult();
			LOG.info("User {} {} authenticated.", login, r != null ? "is"
					: "not");
		} catch (RuntimeException e) {
			LOG.error("Error in getting Login details for {}", login);
		}
		return r;
	}
}
