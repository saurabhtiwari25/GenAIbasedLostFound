package com.my.lostfound.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    private final Cloudinary cloudinary;

    public String saveFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload empty file");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }

        try {

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "lost-found"
                    )
            );

            return result.get("secure_url").toString();

        } catch (IOException e) {

            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }
}
