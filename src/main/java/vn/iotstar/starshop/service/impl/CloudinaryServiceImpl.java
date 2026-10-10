package vn.iotstar.starshop.service.impl;

import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;
import vn.iotstar.starshop.service.CloudinaryService;
import vn.iotstar.starshop.service.CloudinaryUploadResult;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult uploadImage(MultipartFile file)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "File ảnh không được để trống"
            );
        }

        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "starshop/products",
                        "resource_type", "image"
                )
        );

        String url = result.get("secure_url").toString();
        String publicId = result.get("public_id").toString();

        return new CloudinaryUploadResult(
                url,
                publicId
        );
    }

    @Override
    public void deleteImage(String publicId)
            throws IOException {

        if (publicId == null || publicId.isBlank()) {
            return;
        }

        cloudinary.uploader().destroy(
                publicId,
                ObjectUtils.emptyMap()
        );
    }
}