package com.sixsprints.auth.interceptor;

import java.util.List;

import org.slf4j.MDC;
import org.springframework.util.CollectionUtils;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.service.AbstractRoleService;
import com.sixsprints.auth.util.PermissionUtil;
import com.sixsprints.core.auth.ModuleDefinition;
import com.sixsprints.core.auth.PermissionDefinition;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.interceptor.AbstractAuthenticationInterceptor;
import com.sixsprints.core.service.GenericCrudService;

public abstract class AbstractRoleBasedAuthInterceptor<T extends AbstractAuthenticableEntity, ROLE extends AbstractRole>
  extends AbstractAuthenticationInterceptor<T> {

  private static final String USER = "user";

  private final AbstractRoleService<ROLE> roleService;

  public AbstractRoleBasedAuthInterceptor(GenericCrudService<T> userService, AbstractRoleService<ROLE> roleService) {
    super(userService);
    this.roleService = roleService;
  }

  @Override
  protected void checkUserPermissions(T user, ModuleDefinition module, PermissionDefinition permission,
    boolean required)
    throws NotAuthenticatedException, EntityNotFoundException {
    if (PermissionUtil.allAny(module, permission)) {
      return;
    }
    ROLE role = roleService.findBySlug(user.getRoleSlug());
    Boolean hasAccess = PermissionUtil.hasAccess(role, module, permission);
    if (!hasAccess) {
      throwException(required, unauthorisedErrorMessage(user));
    }
  }

  @Override
  protected void checkIfTokenInvalid(T user, String token, boolean required) throws NotAuthenticatedException {
    List<String> invalidTokens = user.getInvalidTokens();
    if (!CollectionUtils.isEmpty(invalidTokens) && invalidTokens.contains(token)) {
      throwException(required, tokenInvalidErrorMessage());
    }
  }

  @Override
  protected void postProcessor(T user) {
    super.postProcessor(user);
    if (user != null) {
      MDC.put(USER, user.authId());
    }
  }

}