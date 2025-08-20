package com.hashedin.huspark.dto;

import com.hashedin.huspark.entity.Role;
import jakarta.validation.constraints.NotNull;

public class UserDtos {
    public record UserResponse(Long id, String fullName, String email, String role) {}
    public record RoleUpdateRequest(@NotNull Role role) {}
}
