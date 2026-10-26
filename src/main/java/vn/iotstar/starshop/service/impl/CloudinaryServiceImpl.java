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
    @org.springframework.beans.factory.annotation.Value("${cloudinary.cloud-name:}") private String cloudName;
    @org.springframework.beans.factory.annotation.Value("${cloudinary.api-key:}") private String apiKey;
    @org.springframework.beans.factory.annotation.Value("${cloudinary.api-secret:}") private String apiSecret;

    @Override
    public CloudinaryUploadResult uploadImage(MultipartFile file)
            throws IOException {

        return upload(file, "image", "starshop/products");
    }

    @Override
    public CloudinaryUploadResult uploadReviewImage(MultipartFile file)
            throws IOException {

        return upload(file, "image", "starshop/reviews");
    }

    @Override
    public CloudinaryUploadResult uploadReviewVideo(MultipartFile file)
            throws IOException {

        return upload(file, "video", "starshop/reviews");
    }

    private CloudinaryUploadResult upload(
            MultipartFile file, String type, String folder)
            throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Tệp tải lên không được để trống"
            );
        }

        if (cloudName.isBlank() || apiKey.isBlank() || apiSecret.isBlank()) {
            throw new IOException("Chưa cấu hình Cloudinary để tải ảnh/video. Vui lòng liên hệ quản trị.");
        }
        Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", type
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
