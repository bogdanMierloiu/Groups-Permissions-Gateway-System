package com.bogdan_mierloiu.permissions_system.mapper;

import com.bogdan_mierloiu.permissions_system.dto.user.UserResponse;
import com.bogdan_mierloiu.permissions_system.entity.User;

public class UserMapper {

    private UserMapper() {
    }

    public static UserResponse entityToDto(User user) {
        return UserResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .surname(user.getSurname())
                .uuid(user.getUuid())
                .build();
    }
}
