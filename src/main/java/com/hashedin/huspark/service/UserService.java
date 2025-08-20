// src/main/java/com/hashedin/huspark/service/UserService.java
package com.hashedin.huspark.service;

import com.hashedin.huspark.dto.UserDtos.UserResponse;
import com.hashedin.huspark.dto.UserDtos.RoleUpdateRequest;
import com.hashedin.huspark.entity.Role;
import com.hashedin.huspark.entity.User;
import com.hashedin.huspark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hashedin.huspark.util.MaskUtil.maskEmail;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    private static UserResponse toDto(User u) {
        return new UserResponse(u.getId(), u.getFullName(), maskEmail(u.getEmail()), u.getRole().name());
    }

    // ADMIN: change any user's role
    public UserResponse updateRole(Long id, RoleUpdateRequest req) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        u.setRole(req.role());
        return toDto(userRepository.save(u));
    }

    // STAFF (Librarian/Admin): list only MEMBERS with paging/sorting
    @Transactional(readOnly = true)
    public Page<UserResponse> listMembers(Pageable pageable) {
        var page = userRepository.findByRole(Role.MEMBER, pageable);
        return page.map(UserService::toDto);
    }

    // STAFF: get MEMBER by id (404 if not a member)
    @Transactional(readOnly = true)
    public UserResponse getMemberById(Long id) {
        User u = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        if (u.getRole() != Role.MEMBER) throw new RuntimeException("Member not found");
        return toDto(u);
    }
}
