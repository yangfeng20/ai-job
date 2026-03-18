package com.maple.ai.job.hunting.utils;

import java.nio.charset.StandardCharsets;

/**
 * 文件类型检测器
 *
 * @author gaoping
 * @since 2025/02/21
 */
public class FileTypeDetector {

    public static String detectFileType(byte[] data) {
        if (data == null || data.length == 0) {
            return "unknown";
        }

        // 检查 PNG (8 bytes)
        if (matches(data, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
            return "png";
        }

        // 检查 JPG (3 bytes)
        if (matches(data, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "jpg";
        }

        // 检查 PDF (5 bytes: "%PDF-")
        if (data.length >= 5 && matches(data, "%PDF-".getBytes(StandardCharsets.US_ASCII))) {
            return "pdf";
        }

        // 检查 DOC (OLE2格式，8 bytes)
        if (matches(data, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1})) {
            return "doc";
        }

        // 检查 DOCX (ZIP格式 + 特定文件结构，这里仅检查 ZIP 头)
        if (matches(data, new byte[]{0x50, 0x4B, 0x03, 0x04})) {
            // 进一步验证 ZIP 中的内容（可选）
            return "docx";
        }

        return "unknown";
    }

    // 辅助方法：比较字节数组前缀
    private static boolean matches(byte[] data, byte[] signature) {
        if (data == null || data.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if (data[i] != signature[i]) {
                return false;
            }
        }
        return true;
    }
}