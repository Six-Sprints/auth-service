package com.sixsprints.auth.mock.domain;

import org.springframework.data.mongodb.core.mapping.Document;

import com.sixsprints.auth.domain.AbstractRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document
public class Role extends AbstractRole {

  private static final long serialVersionUID = 1L;

  private String orgId;
}
