package com.sixsprints.auth.annotation;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.sixsprints.auth.domain.AbstractAuthenticableEntity;
import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.enums.AccessPermission;
import com.sixsprints.auth.service.RoleService;
import com.sixsprints.auth.util.PermissionUtil;
import com.sixsprints.core.exception.BaseException;
import com.sixsprints.core.exception.EntityNotFoundException;
import com.sixsprints.core.exception.NotAuthenticatedException;
import com.sixsprints.core.service.GenericCrudService;
import com.sixsprints.core.utils.AuthUtil;

public abstract class AbstractAuthenticatedArgumentResolver<T extends AbstractAuthenticableEntity>
  implements HandlerMethodArgumentResolver {

  private GenericCrudService<T> userService;

  public AbstractAuthenticatedArgumentResolver(GenericCrudService<T> userService) {
    this.userService = userService;
  }

  @Autowired
  private RoleService roleService;

  @Override
  public T resolveArgument(MethodParameter param, ModelAndViewContainer mavContainer, NativeWebRequest request,
    WebDataBinderFactory binderFactory) throws Exception {
    Annotation[] paramAnns = param.getParameterAnnotations();
    for (Annotation annotation : paramAnns) {
      if (Authenticated.class.isInstance(annotation)) {
        Authenticated authAnnotation = (Authenticated) annotation;
        HttpServletRequest httprequest = (HttpServletRequest) request.getNativeRequest();
        String token = httprequest.getHeader(auhtTokenKey());
        if (StringUtils.isEmpty(token)) {
          token = httprequest.getParameter(auhtTokenKey());
        }
        T user = checkUser(authAnnotation, token);
        postProcessor(user);
        return user;
      }
    }
    return null;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(Authenticated.class);
  }

  protected abstract String auhtTokenKey();

  protected void postProcessor(T user) {
  }

  protected String unauthorisedErrorMessage(T user) {
    return "You are not authorized to take this action.";
  }

  protected String inactiveErrorMessage(T user) {
    return "User account is not active.";
  }

  protected String tokenInvalidErrorMessage() {
    return "Token is invalid!";
  }

  protected String tokenEmptyErrorMessage() {
    return "Token is empty!";
  }

  private T checkUser(Authenticated authAnnotation, String token)
    throws NotAuthenticatedException, EntityNotFoundException {
    Boolean tokenEmpty = checkIfTokenEmpty(authAnnotation, token);
    if (tokenEmpty) {
      return null;
    }
    T user = decodeUser(token, authAnnotation);
    if (user == null) {
      return null;
    }
    checkIfTokenInvalid(user.getInvalidTokens(), token, authAnnotation);
    checkIfActive(user, authAnnotation);
    checkUserPermissions(user, authAnnotation);
    return user;
  }

  private void checkUserPermissions(T user, Authenticated authAnnotation)
    throws NotAuthenticatedException, EntityNotFoundException {
    String entityPermission = authAnnotation.entity();
    AccessPermission accessPermission = authAnnotation.access();
    if (PermissionUtil.allAny(entityPermission, accessPermission)) {
      return;
    }
    Role role = roleService.findBySlug(user.getRoleSlug());
    Boolean hasAccess = PermissionUtil.hasAccess(role, entityPermission, accessPermission);
    if (!hasAccess) {
      throwException(authAnnotation, unauthorisedErrorMessage(user));
    }
  }

  private void checkIfActive(T user, Authenticated authAnnotation) throws NotAuthenticatedException {
    if (!user.getActive()) {
      throwException(authAnnotation, inactiveErrorMessage(user));
    }
  }

  private T decodeUser(String token, Authenticated authAnnotation) throws NotAuthenticatedException {
    T user = null;
    try {
      String userId = AuthUtil.decodeToken(token);
      user = userService.findOne(userId);
    } catch (BaseException ex) {
      throwException(authAnnotation, ex.getMessage());
    }
    return user;
  }

  private void checkIfTokenInvalid(List<String> invalidTokens, String token, Authenticated authAnnotation)
    throws NotAuthenticatedException {
    if (!CollectionUtils.isEmpty(invalidTokens) && invalidTokens.contains(token)) {
      throwException(authAnnotation, tokenInvalidErrorMessage());
    }
  }

  private Boolean checkIfTokenEmpty(Authenticated authAnnotation, String token) throws NotAuthenticatedException {
    if (StringUtils.isEmpty(token)) {
      throwException(authAnnotation, tokenEmptyErrorMessage());
      return true;
    }
    return false;
  }

  private void throwException(Authenticated authAnnotation, String message) throws NotAuthenticatedException {
    if (authAnnotation.required()) {
      throw NotAuthenticatedException.childBuilder().error(message).build();
    }
  }

}