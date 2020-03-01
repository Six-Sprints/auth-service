package com.sixsprints.auth.transformer;

import org.springframework.stereotype.Service;

import com.sixsprints.auth.domain.mock.User;
import com.sixsprints.auth.dto.UserDto;
import com.sixsprints.core.transformer.GenericTransformer;

@Service
public class UserMapper extends GenericTransformer<User, UserDto> {

  @Override
  public UserDto toDto(User user) {
    return UserDto.builder().email(user.getEmail()).id(user.getId()).mobileNumber(user.getMobileNumber())
      .name(user.getName()).slug(user.getSlug()).build();
  }

  @Override
  public User toDomain(UserDto dto) {
    return User.builder().email(dto.getEmail()).mobileNumber(dto.getMobileNumber()).password(dto.getPassword())
      .name(dto.getName()).build();
  }

}
