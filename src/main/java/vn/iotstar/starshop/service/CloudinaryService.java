package vn.iotstar.starshop.service;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface CloudinaryService {

    CloudinaryUploadResult uploadImage(MultipartFile file)
            throws IOException;

    void deleteImage(String publicId)
            throws IOException;
}