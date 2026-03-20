package com.ocms.backend.security;

public class AuthContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long userId() {
        return HOLDER.get() == null ? null : HOLDER.get().getUserId();
    }

    public static String role() {
        return HOLDER.get() == null ? null : HOLDER.get().getRole();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
