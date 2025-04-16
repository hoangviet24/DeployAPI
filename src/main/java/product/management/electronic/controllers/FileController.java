package product.management.electronic.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import product.management.electronic.response.ApiResponse;
import product.management.electronic.services.Cloudinary.CloudinaryService;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileController {
    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/uploadImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder",
                    defaultValue = "default_value") String folder,
            @RequestParam(value = "fileName",required = false) String fileName) throws IOException {
        cloudinaryService.uploadImage(file, folder, fileName);
        return ResponseEntity.ok(new ApiResponse(HttpStatus.OK.value(), "Image uploaded successfully"));
    }
}