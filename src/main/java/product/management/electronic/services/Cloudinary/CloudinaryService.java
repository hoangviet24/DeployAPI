package product.management.electronic.services.Cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public Map<String, String> uploadImage(MultipartFile file, String folder, String fileName) throws IOException {
        Map<String, Object> params = ObjectUtils.asMap(
                "folder", folder,
                "public_id", fileName,
                "overwrite",true
        );
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);
        return Map.of(
                "url", (String) uploadResult.get("secure_url"),
                "public_id", (String) uploadResult.get("public_id")
        );
    }
}