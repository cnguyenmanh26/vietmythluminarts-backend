package com.social.vietmythluminartsbackend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for handling Cloudinary uploads and deletions
 * Migrated from Node.js utils/uploadUtils.js
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    // File size limits (in bytes)
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024; // 100MB

    // Allowed file types
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final List<String> ALLOWED_VIDEO_TYPES = List.of(
            "video/mp4", "video/webm", "video/quicktime", "video/x-msvideo"
    );

    /**
     * Upload image to Cloudinary
     * @param file Multipart file
     * @param folder Cloudinary folder (e.g., "avatars", "products")
     * @return Upload result with URL and publicId
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) throws IOException {
        validateImageFile(file);

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "vietmythluminarts/" + folder,
                "resource_type", "image",
                "transformation", List.of(
                        Map.of("width", 1200, "height", 1200, "crop", "limit"),
                        Map.of("quality", "auto:good"),
                        Map.of("fetch_format", "auto")
                )
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        log.debug("Image uploaded successfully: {}", uploadResult.get("public_id"));
        
        return Map.of(
                "url", uploadResult.get("secure_url"),
                "publicId", uploadResult.get("public_id"),
                "format", uploadResult.get("format"),
                "width", uploadResult.get("width"),
                "height", uploadResult.get("height")
        );
    }

    /**
     * Upload video to Cloudinary
     * @param file Multipart file
     * @param folder Cloudinary folder
     * @return Upload result with URL, publicId, and thumbnail
     */
    public Map<String, Object> uploadVideo(MultipartFile file, String folder) throws IOException {
        validateVideoFile(file);

        Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", "vietmythluminarts/" + folder,
                "resource_type", "video",
                "transformation", List.of(
                        Map.of("width", 1920, "height", 1080, "crop", "limit"),
                        Map.of("quality", "auto:good")
                )
        );

        Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
        
        // Generate thumbnail URL (at 1 second)
        String publicId = (String) uploadResult.get("public_id");
        
        // Build transformation for video thumbnail using transformation string
        // Format: "w_400,c_scale,so_1" 
        // w_400 = width 400px, c_scale = crop scale, so_1 = start_offset 1 second
        String transformationString = "w_400,c_scale,so_1";
        
        String thumbnailUrl = cloudinary.url()
                .resourceType("video")
                .format("jpg")
                .transformation(new Transformation().rawTransformation(transformationString))
                .generate(publicId);

        log.debug("Video uploaded successfully: {}", publicId);
        
        return Map.of(
                "url", uploadResult.get("secure_url"),
                "publicId", publicId,
                "thumbnail", thumbnailUrl,
                "duration", uploadResult.getOrDefault("duration", 0),
                "format", uploadResult.get("format")
        );
    }

    /**
     * Upload multiple images
     * @param files List of multipart files
     * @param folder Cloudinary folder
     * @return List of upload results
     */
    public List<Map<String, Object>> uploadMultipleImages(List<MultipartFile> files, String folder) throws IOException {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided");
        }

        if (files.size() > 20) {
            throw new IllegalArgumentException("Maximum 20 images allowed");
        }

        List<Map<String, Object>> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadImage(file, folder));
        }

        return results;
    }

    /**
     * Delete media from Cloudinary
     * @param publicId Public ID of the resource
     * @param resourceType Type: "image" or "video"
     */
    public void deleteMedia(String publicId, String resourceType) {
        try {
            Map<String, Object> params = ObjectUtils.asMap("resource_type", resourceType);
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, params);
            
            String resultStatus = (String) result.get("result");
            if ("ok".equals(resultStatus)) {
                log.debug("Media deleted successfully: {}", publicId);
            } else {
                log.warn("Media deletion returned status: {} for publicId: {}", resultStatus, publicId);
            }
        } catch (Exception e) {
            log.error("Failed to delete media from Cloudinary: {}", publicId, e);
            // Don't throw exception - allow operation to continue
        }
    }

    /**
     * Delete multiple images
     * @param publicIds List of public IDs
     */
    public void deleteMultipleImages(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) {
            return;
        }

        for (String publicId : publicIds) {
            deleteMedia(publicId, "image");
        }
    }

    /**
     * Delete video
     * @param publicId Public ID of the video
     */
    public void deleteVideo(String publicId) {
        deleteMedia(publicId, "video");
    }

    /**
     * Extract public ID from Cloudinary URL
     * @param url Cloudinary URL
     * @return Public ID or null
     */
    public String extractPublicId(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        try {
            // URL format: https://res.cloudinary.com/{cloud_name}/{resource_type}/upload/{version}/{publicId}.{format}
            String[] parts = url.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

            String afterUpload = parts[1];
            // Remove version (v123456789/)
            String withoutVersion = afterUpload.replaceFirst("v\\d+/", "");
            // Remove file extension
            int dotIndex = withoutVersion.lastIndexOf('.');
            if (dotIndex > 0) {
                return withoutVersion.substring(0, dotIndex);
            }

            return withoutVersion;
        } catch (Exception e) {
            log.error("Failed to extract publicId from URL: {}", url, e);
            return null;
        }
    }

    // ==================== Validation Methods ====================

    /**
     * Validate image file
     */
    private void validateImageFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only JPEG, PNG, WebP, and GIF images are allowed."
            );
        }

        // Check file size
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File too large. Maximum size: %d MB", MAX_IMAGE_SIZE / (1024 * 1024))
            );
        }
    }

    /**
     * Validate video file
     */
    private void validateVideoFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_VIDEO_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only MP4, WebM, MOV, and AVI videos are allowed."
            );
        }

        // Check file size
        if (file.getSize() > MAX_VIDEO_SIZE) {
            throw new IllegalArgumentException(
                    String.format("File too large. Maximum size: %d MB", MAX_VIDEO_SIZE / (1024 * 1024))
            );
        }
    }
}

