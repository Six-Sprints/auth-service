package com.sixsprints.auth.service;

import com.sixsprints.auth.dto.AuthResponseDTO;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.core.domain.AbstractMongoEntity;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;

/**
 * Authentication Service API to do general authentication operations like
 * {@code login, registration, password reset, logout} using CRUD operations defined in mongo-core API.
 * <p>
 * The implementer of this interface should provide definition of below mentioned methods<br><br>
 * <code>GenericRepository<User> repository()</code><br>
 * <code>boolean isInvalid(User domain)</code><br>
 * <code>protected User findDuplicate(User entity)</code><br>
 * <code>protected void preCreate(User entity)</code><br>
 * <code>protected User findByAuthCriteria(String authId)</code><br>
 * <code>protected boolean isPasscodeSame(User domain, String passcode)</code><br>
 * <code>MetaData<User> metaData(User entity)</code>
 * 
 * @param <T> the type of domain entity which should either be {@code AbstractMongoEntity} or it's child
 */
public interface AuthService<T extends AbstractMongoEntity> {

  AuthResponseDTO<T> register(T domain) throws EntityAlreadyExistsException, EntityInvalidException;

  AuthResponseDTO<T> login(Authenticable authenticable) throws NotAuthenticatedException;

  void resetPassword(Authenticable authenticable) throws EntityNotFoundException;

//  boolean tokenValidation(String token);

//  <D extends AbstractDomain> void logout(D domain, String token);

}
