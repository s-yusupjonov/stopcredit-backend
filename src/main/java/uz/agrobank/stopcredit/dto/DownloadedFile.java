package uz.agrobank.stopcredit.dto;

import java.io.InputStream;

public record DownloadedFile(String fileName, long sizeBytes, InputStream content) {
}
