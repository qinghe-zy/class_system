package com.ocms.backend.util;

import com.ocms.backend.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    // 与前端保持一致的资源类型白名单：优先覆盖学习场景常用格式。
    private static final Set<String> ALLOW_EXT = Set.of(
            "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "pdf", "txt", "mp4",
            "png", "jpg", "jpeg", "gif", "webp"
    );
    private static final long MAX_SIZE_BYTES = 500L * 1024 * 1024;

    public Map<String, Object> storeTeachingFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : "unknown";
        String ext = StringUtils.getFilenameExtension(originName);
        if (!StringUtils.hasText(ext) || !ALLOW_EXT.contains(ext.toLowerCase())) {
            throw new BizException("仅支持上传 Word/PPT/Excel/PDF/TXT/图片/MP4 文件");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BizException("文件大小不能超过500MB");
        }

        String dayPath = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path uploadDir = UploadPathUtil.uploadRoot().resolve(dayPath);

        try {
            Files.createDirectories(uploadDir);
            String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext.toLowerCase();
            Path target = uploadDir.resolve(fileName);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("fileName", originName);
            result.put("attachmentName", originName);
            result.put("attachmentType", ext.toLowerCase());
            result.put("attachmentUrl", "/uploads/" + dayPath + "/" + fileName);
            result.put("size", file.getSize());
            result.put("previewType", previewType(ext));
            return result;
        } catch (IOException e) {
            throw new BizException("文件上传失败: " + e.getMessage());
        }
    }

    private String previewType(String ext) {
        String lower = ext.toLowerCase();
        if ("mp4".equals(lower)) {
            return "video";
        }
        if ("pdf".equals(lower)) {
            return "pdf";
        }
        if (Set.of("png", "jpg", "jpeg", "gif", "webp").contains(lower)) {
            return "image";
        }
        if ("txt".equals(lower)) {
            return "text";
        }
        return "document";
    }
}
