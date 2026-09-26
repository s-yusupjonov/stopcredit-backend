package uz.agrobank.stopcredit.service;

import java.io.InputStream;

public interface FileStorage {

    void put(String objectKey, InputStream content, long size, String contentType);

    InputStream get(String objectKey);

    void delete(String objectKey);
}
