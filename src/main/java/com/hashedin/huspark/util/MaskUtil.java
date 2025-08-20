// src/main/java/com/hashedin/huspark/util/MaskUtil.java
package com.hashedin.huspark.util;

public class MaskUtil {
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        var parts = email.split("@", 2);
        var local = parts[0];
        if (local.length() <= 2) return "*" + "@" + parts[1];
        return local.charAt(0) + "***" + local.charAt(local.length()-1) + "@" + parts[1];
    }
}
