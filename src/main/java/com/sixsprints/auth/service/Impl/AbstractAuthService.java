package com.sixsprints.auth.service.Impl;

import org.springframework.stereotype.Service;

import com.sixsprints.auth.dto.AuthResponseDTO;
import com.sixsprints.auth.dto.Authenticable;
import com.sixsprints.auth.service.AuthService;
import com.sixsprints.core.domain.AbstractMongoEntity;
import com.sixsprints.core.exception.EntityAlreadyExistsException;
import com.sixsprints.core.exception.EntityInvalidException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.AbstractCrudService;
import com.sixsprints.core.utils.AuthUtil;

@Service
public abstract class AbstractAuthService<T extends AbstractMongoEntity> extends AbstractCrudService<T>
  implements AuthService<T> {

  @Override
  public AuthResponseDTO<T> register(T domain) throws EntityAlreadyExistsException, EntityInvalidException {
    T create = create(domain);
    return generateToken(create);
  }

  @Override
  public AuthResponseDTO<T> login(Authenticable authenticable) throws NotAuthenticatedException {
    T domain = findByAuthCriteria(authenticable.authId());
    if (domain != null)
      if (isPasscodeSame(domain, authenticable.passcode()))
        return generateToken(domain);
    throw notAuthenticatedException(domain);
    // in response data-> if null then unregistered else credential mismatch
  }

  @Override
  public void resetPassword(Authenticable authenticable) throws EntityNotFoundException {
//    T domain = findByCriteria(loginDTO.getEmail());
//    if (domain == null)
//      throw EntityNotFoundException.childBuilder().error("EMAIL_NOT_FOUND")
//        .arguments(new String[] { loginDTO.getEmail() }).build();
//    domain.setPassword(EncryptionUtil.encrypt(loginDTO.getPassword(), ENCRYPTION_ALGORITHM));
//    repository().save(domain);
  }

  protected abstract T findByAuthCriteria(String criteria);

  protected abstract boolean isPasscodeSame(T domain, String passcode);

  protected abstract NotAuthenticatedException notAuthenticatedException(T domain);

  private AuthResponseDTO<T> generateToken(T domain) {
    return AuthResponseDTO.<T>builder().token(AuthUtil.createToken(domain.getId())).data(domain).build();
  }

}