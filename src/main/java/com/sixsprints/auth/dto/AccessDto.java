package com.sixsprints.auth.dto;

import java.io.Serializable;

import com.sixsprints.core.enums.AccessPermission;
import com.sixsprints.core.enums.Restriction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private AccessPermission accessPermission;

  private Restriction restriction;

}
