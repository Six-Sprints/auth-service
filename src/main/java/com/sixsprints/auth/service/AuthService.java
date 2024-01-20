package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Otp;
import com.sixsprints.auth.dto.AuthResponseDto;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.GenericCrudService;

public interface AuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO> extends GenericCrudService<T> {

  AuthResponseDto<DETAIL_DTO> register(DTO dto) throws EntityAlreadyExistsException, EntityInvalidException;

  AuthResponseDto<DETAIL_DTO> login(Authenticable authenticable)
    throws NotAuthenticatedException, EntityNotFoundException, EntityInvalidException;

  AuthResponseDto<DETAIL_DTO> validateToken(T user);

  Otp sendOtp(String authId) throws EntityNotFoundException;

  Otp validateOtp(String authId, String otp) throws EntityInvalidException;

  void resetPassword(String authId, String otp, String newPassword)
    throws EntityInvalidException, EntityNotFoundException;

  void logout(T user, String token);

}