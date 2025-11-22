package com.social.back_java.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface ICloudinaryService {
    Map<String, Object> uploadImage(MultipartFile file) throws IOException;
    Map<String, Object> uploadVideo(MultipartFile file) throws IOException;
    void deleteFile(String publicId) throws IOException;
}
