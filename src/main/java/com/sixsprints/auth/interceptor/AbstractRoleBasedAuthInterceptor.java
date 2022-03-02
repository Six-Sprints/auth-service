package com.sixsprints.auth.interceptor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.auth.util.PermissionUtil;
import com.sixsprints.core.annotation.Authenticated;
import com.sixsprints.core.enums.AccessPermission;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.interceptor.AbstractAuthenticationInterceptor;
import com.sixsprints.core.service.GenericCrudService;
import com.sixsprints.core.utils.ApplicationContext;
import com.sixsprints.core.utils.HttpRequestUtil;

public abstract class AbstractRoleBasedAuthInterceptor<T extends AbstractAuthenticableEntity>
  extends AbstractAuthenticationInterceptor<T> {

  private static final String USER = "user";

  @Value("${api.version.prefix:/api/v1/}")
  private String apiVersionPrefix;

  @Autowired
  private RoleService roleService;

  public AbstractRoleBasedAuthInterceptor(GenericCrudService<T> userService) {
    super(userService);
  }

  @Override
  protected void checkUserPermissions(T user, Authenticated authAnnotation)
    throws NotAuthenticatedException, EntityNotFoundException {
    String entityPermission = computeEntityPermission(authAnnotation);
    AccessPermission accessPermission = computeAccess(authAnnotation);
    if (PermissionUtil.allAny(entityPermission, accessPermission)) {
      return;
    }
    Role role = roleService.findBySlug(user.getRoleSlug());
    Boolean hasAccess = PermissionUtil.hasAccess(role, entityPermission, accessPermission);
    if (!hasAccess) {
      throwException(authAnnotation, unauthorisedErrorMessage(user));
    }
  }

  @Override
  protected void checkIfTokenInvalid(T user, String token, Authenticated authAnnotation)
    throws NotAuthenticatedException {
    List<String> invalidTokens = user.getInvalidTokens();
    if (!CollectionUtils.isEmpty(invalidTokens) && invalidTokens.contains(token)) {
      throwException(authAnnotation, tokenInvalidErrorMessage());
    }
  }

  @Override
  protected void postProcessor(T user) {
    super.postProcessor(user);
    if (user != null) {
      MDC.put(USER, user.authId());
    }
  }

  protected AccessPermission computeAccess(Authenticated authAnnotation) {
    return authAnnotation.access();
  }

  protected String computeEntityPermission(Authenticated authAnnotation) {

    String moduleEntity = HttpRequestUtil.extractModuleName(apiVersionPrefix,
      ApplicationContext.getCurrentRequest().getUri());
    return StringUtils.isEmpty(authAnnotation.entity()) ? moduleEntity : authAnnotation.entity();
  }

}