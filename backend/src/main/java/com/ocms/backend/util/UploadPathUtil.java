package com.ocms.backend.util;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UploadPathUtil {

    private static final Pattern OPEN_URL_PATTERN = Pattern.compile("[?&]url=([^&]+)");

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
        String uploadDir = System.getProperty("app.upload.dir");
        if (uploadDir == null || uploadDir.isBlank()) {
            uploadDir = System.getenv("APP_UPLOAD_DIR");
        }
        if (uploadDir != null && !uploadDir.isBlank()) {
            return Paths.get(uploadDir).toAbsolutePath().normalize();
        }
        return projectRoot().resolve("uploads").normalize();
    }

    public static Path resolveAttachmentUrl(String attachmentUrl) {
        if (attachmentUrl == null || attachmentUrl.isBlank()) {
            return null;
        }
        String normalized = extractRawAttachmentPath(attachmentUrl.trim());
        if (normalized == null || normalized.isBlank()) {
            return null;
        }

        try {
            normalized = URLDecoder.decode(normalized, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignore) {
            // 部分历史数据可能包含不完整编码，保持原值继续走后续路径校验
        }
        normalized = normalized.replace("\\", "/");

        if (normalized.startsWith("/api/files/open")) {
            Matcher matcher = OPEN_URL_PATTERN.matcher(normalized);
            if (matcher.find()) {
                return resolveAttachmentUrl(matcher.group(1));
            }
        }

        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            try {
                URI uri = URI.create(normalized);
                normalized = uri.getPath() == null ? "" : uri.getPath();
            } catch (IllegalArgumentException ignore) {
                // 保持原字符串，后续由标准路径校验兜底
            }
        }

        if (normalized.startsWith("/api/files/open")) {
            Matcher matcher = OPEN_URL_PATTERN.matcher(normalized);
            if (matcher.find()) {
                return resolveAttachmentUrl(matcher.group(1));
            }
        }

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("uploads/")) {
            normalized = normalized.substring("uploads/".length());
        }
        if (normalized.isBlank()) {
            return null;
        }
        return uploadRoot().resolve(normalized).normalize();
    }

    private static String extractRawAttachmentPath(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return null;
        }
        Matcher matcher = OPEN_URL_PATTERN.matcher(rawUrl);
        if (rawUrl.contains("/api/files/open") && matcher.find()) {
            return matcher.group(1);
        }
        return rawUrl;
    }
}
