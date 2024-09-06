package com.sixsprints.auth.util;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.sixsprints.auth.domain.AbstractRole;
import com.sixsprints.auth.domain.embedded.ModulePermission;
import com.sixsprints.auth.mock.domain.Role;
import com.sixsprints.core.auth.BasicPermissionEnum;
import com.sixsprints.core.auth.ModuleDefinition;

public class PermissionUtilTest {

  private static enum MockModule implements ModuleDefinition {
    ANY, USER, COMPANY;
  }

  @Test
  public void shouldHaveAccess() {
    AbstractRole role = mockRoleWithUserCreateAndReadAll();
    Assert.isTrue(PermissionUtil.hasAccess(role, MockModule.USER, BasicPermissionEnum.ADD), "Must be true");
    Assert.isTrue(PermissionUtil.hasAccess(role, MockModule.USER, BasicPermissionEnum.VIEW), "Must be true");
    Assert.isTrue(PermissionUtil.hasAccess(role, MockModule.ANY, BasicPermissionEnum.VIEW),
      "Must be true for Any Entity");
    Assert.isTrue(PermissionUtil.hasAccess(role, MockModule.USER, BasicPermissionEnum.ANY),
      "Must be true for Any Access");

    Assert.isTrue(PermissionUtil.hasAccess(role, MockModule.COMPANY, BasicPermissionEnum.DELETE),
      "Must be true for Any Access");
  }

  @Test
  public void shouldNotHaveAccess() {
    AbstractRole role = mockRoleWithUserCreateAndReadAll();
    Assert.isTrue(!PermissionUtil.hasAccess(role, MockModule.USER, BasicPermissionEnum.DELETE),
      "Must not be true for User Delete All");
    Assert.isTrue(!PermissionUtil.hasAccess(role, MockModule.USER, BasicPermissionEnum.EDIT),
      "Must not be true for User Update All");
  }

  private AbstractRole mockRoleWithUserCreateAndReadAll() {

    return Role.builder().name("R1")
      .modulePermission(ModulePermission.builder()
        .module(MockModule.USER)
        .permission(BasicPermissionEnum.ADD)
        .permission(BasicPermissionEnum.VIEW)
        .build())

      .modulePermission(ModulePermission.builder()
        .module(MockModule.COMPANY)
        .permission(BasicPermissionEnum.ANY)
        .build())
      .build();
  }

}
