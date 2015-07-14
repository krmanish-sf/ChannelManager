package com.is.cm.core.persistance;


public interface UserRepository {

	salesmachine.hibernatedb.Reps login(String userName,String password);
}
