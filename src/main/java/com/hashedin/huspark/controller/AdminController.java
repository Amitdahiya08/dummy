package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.RoleUpdateRequest;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.hashedin.huspark.service.AuditService;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final AuditService auditService;

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody @Valid RoleUpdateRequest req) {

        User u = userRepository.findById(id).orElse(null);
        if (u == null) return ResponseEntity.notFound().build();

        // preventing direct changing admin roles
        if (req.role() == Role.ADMIN || u.getRole() == Role.ADMIN) {
            return ResponseEntity.badRequest().body("Changing ADMIN roles is not allowed");
        }

        auditService.log("USER_ROLE_UPDATE", "{\"userId\":" + u.getId() + ",\"newRole\":\"" + req.role() + "\"}", false);

        u.setRole(req.role());
        userRepository.save(u);
        return ResponseEntity.ok("Role updated to " + req.role());
    }
}
