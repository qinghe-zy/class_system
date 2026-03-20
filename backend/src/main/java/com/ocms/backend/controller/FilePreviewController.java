package com.ocms.backend.controller;

import com.ocms.backend.common.BizException;
import com.ocms.backend.util.UploadPathUtil;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.sl.extractor.SlideShowExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FilePreviewController {

    @GetMapping("/open")
    public ResponseEntity<Resource> open(@RequestParam("url") String attachmentUrl) throws IOException {
        Path path = safeResolve(attachmentUrl);
        Resource resource = new FileSystemResource(path);
        String filename = path.getFileName().toString();
        MediaType mediaType = detectMediaType(path);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(filename).build().toString())
                .body(resource);
    }

    @GetMapping(value = "/preview-text", produces = "text/html;charset=UTF-8")
    public String previewText(@RequestParam("url") String attachmentUrl) throws IOException {
        Path path = safeResolve(attachmentUrl);
        String ext = extension(path.getFileName().toString());
        String text = switch (ext) {
            case "doc" -> extractDocText(path);
            case "docx" -> extractDocxText(path);
            case "ppt" -> extractPptText(path);
            case "pptx" -> extractPptxText(path);
            case "txt" -> Files.readString(path);
            default -> throw new BizException("当前文件类型暂不支持文本预览");
        };

        if (!StringUtils.hasText(text)) text = "当前文档已读取成功，但未提取到可显示文本。";

        String escaped = HtmlUtils.htmlEscape(text).replace("\r\n", "\n").replace("\r", "\n").replace("\n", "<br/>");

        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                  <title>资源预览</title>
                  <style>
                    body {margin:0;padding:24px;background:#f6f8ff;color:#1c2339;font-family:"Microsoft YaHei","PingFang SC",sans-serif;}
                    .wrap {max-width:980px;margin:0 auto;background:#fff;border:1px solid #dbe4ff;border-radius:20px;box-shadow:0 10px 30px rgba(43,106,255,0.06);padding:24px;}
                    .title {font-size:22px;font-weight:700;margin-bottom:16px;}
                    .tip {color:#5f6b84;margin-bottom:20px;line-height:1.7;}
                    .content {line-height:1.9;white-space:normal;word-break:break-word;}
                  </style>
                </head>
                <body>
                  <div class="wrap">
                    <div class="title">Office 文档文本预览</div>
                    <div class="tip">当前为浏览器兼容预览模式，优先展示可提取的正文文本内容，便于管理员审核与学生阅读。</div>
                    <div class="content">%s</div>
                  </div>
                </body>
                </html>
                """.formatted(escaped);
    }

    private Path safeResolve(String attachmentUrl) {
        Path path = UploadPathUtil.resolveAttachmentUrl(attachmentUrl);
        if (path == null || !Files.exists(path) || !Files.isRegularFile(path)) {
            throw new BizException("文件不存在: " + attachmentUrl);
        }
        Path uploadRoot = UploadPathUtil.uploadRoot().normalize();
        if (!path.normalize().startsWith(uploadRoot)) {
            throw new BizException("非法文件路径");
        }
        return path;
    }

    private MediaType detectMediaType(Path path) throws IOException {
        String ext = extension(path.getFileName().toString());
        return switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "mp4" -> MediaType.parseMediaType("video/mp4");
            case "doc" -> MediaType.parseMediaType("application/msword");
            case "docx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "ppt" -> MediaType.parseMediaType("application/vnd.ms-powerpoint");
            case "pptx" -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            default -> {
                String probe = Files.probeContentType(path);
                yield probe != null ? MediaType.parseMediaType(probe) : MediaType.APPLICATION_OCTET_STREAM;
            }
        };
    }

    private String extension(String filename) {
        int idx = filename.lastIndexOf('.');
        return idx >= 0 ? filename.substring(idx + 1).toLowerCase() : "";
    }

    private String extractDocText(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             HWPFDocument document = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractDocxText(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             XWPFDocument document = new XWPFDocument(in);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    private String extractPptText(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             HSLFSlideShow slideShow = new HSLFSlideShow(in);
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(slideShow)) {
            return extractor.getText();
        }
    }

    private String extractPptxText(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             XMLSlideShow slideShow = new XMLSlideShow(in);
             SlideShowExtractor<?, ?> extractor = new SlideShowExtractor<>(slideShow)) {
            return extractor.getText();
        }
    }
}