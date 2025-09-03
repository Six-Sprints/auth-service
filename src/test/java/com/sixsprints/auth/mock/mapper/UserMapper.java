package com.sixsprints.auth.mock.mapper;

import org.springframework.stereotype.Service;

import com.sixsprints.auth.mock.domain.User;
import com.sixsprints.auth.mock.dto.UserDto;
import com.sixsprints.core.mapper.GenericCrudMapper;

@Service
public class UserMapper extends GenericCrudMapper<User, UserDto> {

  @Override
  public UserDto toDto(User user) {
    return UserDto.builder().email(user.getEmail()).id(user.getId())
        .mobileNumber(user.getMobileNumber()).name(user.getName()).slug(user.getSlug()).build();
  }

  @Override
  public User toDomain(UserDto dto) {
    return User.builder().email(dto.getEmail()).mobileNumber(dto.getMobileNumber())
        .password(dto.getPassword()).name(dto.getName()).build();
  }

}
