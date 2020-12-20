package com.sixsprints.auth.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import com.sixsprints.auth.domain.Role;
import com.sixsprints.auth.dto.AccessDto;
import com.sixsprints.auth.dto.PermissionDto;
import com.sixsprints.core.enums.AccessPermission;

public class PermissionUtilTest {

  private static final String USER = "USER";
  private static final String COMPANY = "COMPANY";

  @Test
  public void shouldHaveAccess() {
    Role role = mockRoleWithUserCreateAndReadAll();
    Assert.isTrue(PermissionUtil.hasAccess(role, USER, AccessPermission.CREATE), "Must be true");
    Assert.isTrue(PermissionUtil.hasAccess(role, USER, AccessPermission.READ), "Must be true");
    Assert.isTrue(PermissionUtil.hasAccess(role, PermissionUtil.ANY, AccessPermission.READ),
      "Must be true for Any Entity");
    Assert.isTrue(PermissionUtil.hasAccess(role, USER, AccessPermission.ANY),
      "Must be true for Any Access");

    Assert.isTrue(PermissionUtil.hasAccess(role, COMPANY, AccessPermission.DELETE),
      "Must be true for Any Access");
  }

  @Test
  public void shouldNotHaveAccess() {
    Role role = mockRoleWithUserCreateAndReadAll();
    Assert.isTrue(!PermissionUtil.hasAccess(role, USER, AccessPermission.DELETE),
      "Must not be true for User Delete All");
    Assert.isTrue(!PermissionUtil.hasAccess(role, USER, AccessPermission.UPDATE),
      "Must not be true for User Update All");
  }

  private Role mockRoleWithUserCreateAndReadAll() {
    List<PermissionDto> permissions = new ArrayList<>();

    permissions.add(
      PermissionDto.builder().entityPermission(USER)
        .access(AccessDto.builder().accessPermission(AccessPermission.CREATE).build())
        .access(AccessDto.builder().accessPermission(AccessPermission.READ).build()).build());

    permissions.add(
      PermissionDto.builder().entityPermission(COMPANY).access(
        AccessDto.builder().accessPermission(AccessPermission.ANY).build()).build());

    return Role.builder().name("R1").permissions(permissions).build();
  }

}
