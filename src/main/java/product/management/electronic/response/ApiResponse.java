package product.management.electronic.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private String message;
    private T data;

    public ApiResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.message = "Successfully";
    }
}

