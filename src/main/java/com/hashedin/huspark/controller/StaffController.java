// src/main/java/com/hashedin/huspark/controller/StaffController.java
package com.hashedin.huspark.controller;

import com.hashedin.huspark.dto.UserDtos.UserResponse;
import com.hashedin.huspark.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import java.util.List;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final UserService userService;

    // LIBRARIAN+ADMIN: list members
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/members")
    public ResponseEntity<?> listMembers(
            @PageableDefault(size = 10, sort = "fullName", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(userService.listMembers(pageable));
    }

    // LIBRARIAN+ADMIN: get member by id
    @PreAuthorize("hasAnyRole('LIBRARIAN','ADMIN')")
    @GetMapping("/members/{id}")
    public ResponseEntity<UserResponse> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getMemberById(id));
    }
}
