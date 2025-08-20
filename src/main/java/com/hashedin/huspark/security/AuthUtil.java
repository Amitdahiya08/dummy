// src/main/java/com/hashedin/huspark/security/AuthUtil.java
package com.hashedin.huspark.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuthUtil {
    public static String currentEmail() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        return (a == null) ? null : a.getName();
    }
}
