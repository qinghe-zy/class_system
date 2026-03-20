package com.ocms.backend.util;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class UploadPathUtil {

    private UploadPathUtil() {
    }

    public static Path projectRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        if (current.getFileName() != null && "backend".equalsIgnoreCase(current.getFileName().toString())) {
            return current.getParent();
        }
        return current;
    }

    public static Path uploadRoot() {
        return projectRoot().resolve("uploads").normalize();
    }

    public static Path resolveAttachmentUrl(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        String normalized = attachmentUrl.trim().replace("\\", "/");
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        return uploadRoot().resolve(normalized).normalize();
    }
}