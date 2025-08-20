package com.hashedin.huspark.dto;

import com.hashedin.huspark.entity.Role;
import jakarta.validation.constraints.NotNull;

public record RoleUpdateRequest(@NotNull Role role) {}
