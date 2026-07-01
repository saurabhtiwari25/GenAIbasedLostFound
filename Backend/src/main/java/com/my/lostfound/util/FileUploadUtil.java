package com.my.lostfound.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.my.lostfound.exception.BadRequestException;
import com.my.lostfound.exception.FileStorageException;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FileUploadUtil {

    private final Cloudinary cloudinary;

    public String saveFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new BadRequestException("Cannot upload empty file");
        }

        if (file.getSize() > 5 * 1024 * 1024) { // 5MB limit
            throw new BadRequestException("File size must not exceed 5MB");
        }

        String contentType = file.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
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

            throw new FileStorageException("Failed to upload image to Cloudinary", e);
        }
    }
}
