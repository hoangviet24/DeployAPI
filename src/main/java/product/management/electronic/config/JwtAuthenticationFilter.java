package product.management.electronic.config;


import product.management.electronic.constants.AppConstant;
import product.management.electronic.services.JwtTokenService;
import product.management.electronic.services.UserService;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.text.ParseException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    @Value("${jwt.secret}")
    private String secret;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenService jwtTokenService;

    private boolean checkPathValid(String path) {
        for (String pattern : AppConstant.WHITE_LIST_URL) {
            if (pathMatcher.match(pattern, path) || path.startsWith(pattern.replace("/**", ""))) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        boolean isWhitelisted = checkPathValid(path);
        if (isWhitelisted) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = getJwtFromRequest(request);
        String username;
        try {
            JWTClaimsSet claimsSet = JWTParser.parse(token).getJWTClaimsSet();
            username = (String) claimsSet.getClaim("userName");
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userService.loadUserByUsername(username);
                if (userDetails != null) {
                    if (StringUtils.hasText(token) && jwtTokenService.isValidToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            }
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
