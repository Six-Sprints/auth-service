package com.sixsprints.auth.util;

import java.util.List;

import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.domain.embedded.ModulePermission;
import com.sixsprints.core.auth.ModuleDefinition;
import com.sixsprints.core.auth.PermissionDefinition;

public class PermissionUtil {

  public static final String ANY = "ANY";

  public static Boolean allAny(ModuleDefinition module, PermissionDefinition permission) {
    return isPermissionAny(permission) && isModuleAny(module);
  }

  public static Boolean hasAccess(AbstractRole role, ModuleDefinition module, PermissionDefinition permission) {
    if (allAny(module, permission)) {
      return true;
    }
    List<ModulePermission> permissions = allPermissions(role);
    for (ModulePermission dto : permissions) {
      if (hasAccess(dto, module, permission)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasAccess(ModulePermission rolePermissions, ModuleDefinition module,
    PermissionDefinition permission) {
    return hasModuleAccess(rolePermissions, module) && hasPermissionAccess(rolePermissions, permission);
  }

  private static boolean hasPermissionAccess(ModulePermission rolePermissions, PermissionDefinition permission) {
    return isPermissionAny(permission) || hasPermissionAccess(rolePermissions.getPermissions(), permission);
  }

  private static boolean hasPermissionAccess(List<PermissionDefinition> permissions, PermissionDefinition permission) {

    for (PermissionDefinition rolePermission : permissions) {
      if (hasPermissionAccess(rolePermission, permission)) {
        return true;
      }
    }
    return false;
  }

  private static boolean hasPermissionAccess(PermissionDefinition rolePermission, PermissionDefinition permission) {
    return isPermissionAny(permission) || isPermissionAny(rolePermission)
      || permission.name().equalsIgnoreCase(rolePermission.name());
  }

  private static boolean hasModuleAccess(ModulePermission rolePermissions, ModuleDefinition module) {
    return isModuleAny(module) || isModuleAny(rolePermissions.getModule())
      || rolePermissions.getModule().name().equalsIgnoreCase(module.name());
  }

  private static List<ModulePermission> allPermissions(AbstractRole role) {
    return role.getModulePermissions();
  }

  private static boolean isModuleAny(ModuleDefinition module) {
    return module == null || ANY.equals(module.name());
  }

  private static boolean isPermissionAny(PermissionDefinition permission) {
    return permission == null || ANY.equals(permission.name());
  }

}