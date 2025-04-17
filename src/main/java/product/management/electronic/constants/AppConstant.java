package product.management.electronic.constants;

public class AppConstant {
    public static final String[] WHITE_LIST_URL = {
            "https://deployapi-xw5c.onrender.com",
            "/api/v1/auth/**", "/v2/api-docs", "/v3/api-docs",
            "/v3/api-docs/**", "/swagger-resources", "/swagger-resources/**", "/configuration/ui",
            "/configuration/security", "/swagger-ui/**", "/webjars/**", "/swagger-ui.html", "/api/auth/**",
            "/api/test/**", "/authenticate", "/api/v1/auth/login", "/api/v1/auth/register","/api/users/forgotPassword",
            "/api/categories/getAllCategories","/api/categories/getCategoriesByType/**",
            "/api/products/getAllProducts","/api/products/getById/**"

    };
    private AppConstant() {
    }
}
