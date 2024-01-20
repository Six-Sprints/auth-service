package com.sixsprints.auth.service;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;

public interface OtpBasedAuthService<T extends AbstractAuthenticableEntity, DTO, DETAIL_DTO> extends AuthService<T, DTO, DETAIL_DTO> {

  DTO sendOtpForAuth(String authId)
    throws EntityNotFoundException, EntityAlreadyExistsException, EntityInvalidException;
}