package com.sixsprints.auth.util;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.PermissionDto;
import com.sixsprints.core.enums.AccessPermission;

public class PermissionUtil {

  public static final String ANY = "ANY";

  public static Boolean allAny(String entityPermission, AccessPermission accessPermission) {
    entityPermission = sanitize(entityPermission);
    return AccessPermission.ANY.equals(accessPermission) && ANY.equals(entityPermission);
  }

  public static Boolean hasAccess(Role role, String entityPermission, AccessPermission accessPermission) {
    if (allAny(entityPermission, accessPermission)) {
      return true;
    }
    List<PermissionDto> permissions = allPermissions(role);
    for (PermissionDto dto : permissions) {
      if (hasAccess(dto, entityPermission, accessPermission)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasAccess(PermissionDto dto, String entityPermission,
    AccessPermission accessPermission) {
    entityPermission = sanitize(entityPermission);
    return (ANY.equals(entityPermission) || ANY.equals(dto.getEntityPermission())
      || entityPermission.equals(dto.getEntityPermission()))
      && (AccessPermission.ANY.equals(accessPermission) || mapToAccessPermissions(dto).contains(AccessPermission.ANY)
        || mapToAccessPermissions(dto).contains(accessPermission));
  }

  private static List<AccessPermission> mapToAccessPermissions(PermissionDto dto) {
    return dto.getAccesses().stream().map(acc -> acc.getAccessPermission()).collect(Collectors.toList());
  }

  private static List<PermissionDto> allPermissions(Role role) {
    return role.getPermissions();
  }

  private static String sanitize(String entityPermission) {
    return StringUtils.isBlank(entityPermission) ? ANY : entityPermission;
  }

}