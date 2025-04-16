package product.management.electronic.services.impl;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import product.management.electronic.dto.User.UserDto;
import product.management.electronic.services.JwtTokenService;
import product.management.electronic.exceptions.BadRequestException;
import product.management.electronic.exceptions.ForbiddenException;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.time.*;
import java.util.Base64;
import java.util.UUID;
import java.time.LocalDateTime;

import product.management.electronic.services.UserService;

import static product.management.electronic.constants.MessageConstant.*;

@Service
@RequiredArgsConstructor
public class JwtTokenServiceImpl implements JwtTokenService {
    @Value("${jwt.secret}")
    private String secret;
    private static final String JWT_HEADER = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private final UserService userService;

    public String createToken(String userName) {
        UserDto user = userService.findByUsername(userName);
        LocalDateTime nowInVietnam = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusHours(1);
        long epochSeconds = nowInVietnam.toEpochSecond(ZoneOffset.ofHours(7));
        JSONObject payload = new JSONObject();
        payload.put("userName", user.getUsername());
        String role = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next();
        payload.put("role", role);
        payload.put("exp", epochSeconds);
        payload.put("jwtId", UUID.randomUUID().toString());
        String signature = hmacSha256(encode(JWT_HEADER.getBytes()) + "." + encode(payload.toString().getBytes()), this.secret);
        return encode(JWT_HEADER.getBytes()) + "." + encode(payload.toString().getBytes()) + "." + signature;
    }

    public String extractUserNameFromJWT(String token) {
        String username = StringUtils.EMPTY;
        try {
            if (verifyExpiration(token)) {
                JWT jwt = JWTParser.parse(token);
                JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
                username = (String) claimsSet.getClaim("userName");
            }
        } catch (ParseException e) {
            throw new BadRequestException(TOKEN_INVALID);
        }
        return username;
    }

    public String createRefreshToken(String token) {
        String userName;
        if (!verifyExpiration(token)) {
            throw new ForbiddenException(REFRESH_TOKEN_EXPIRED);
        }
        try {
            JWT jwt = JWTParser.parse(token);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            userName = (String) claimsSet.getClaim("userName");
        } catch (ParseException e) {
            throw new BadRequestException(FIELD_INVALID);
        }
        return refreshToken(userName);
    }

    public boolean verifyExpiration(String authToken) {
        try {
            JWT jwt = JWTParser.parse(authToken);
            JWTClaimsSet claimsSet = jwt.getJWTClaimsSet();
            Instant expiryTime = claimsSet.getExpirationTime().toInstant();
            Instant nowVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant();
            return expiryTime.isAfter(nowVietnam);
        } catch (ParseException e) {
            throw new BadRequestException(TOKEN_INVALID);
        }

    }

    public static String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hmacSha256(String data, String secret) {
        if (secret == null || secret.isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be null or empty");
        }
        try {
            byte[] hash = secret.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("HMAC SHA256 algorithm error", ex);
        }
    }

    public String refreshToken(String userName) {
        UserDto user = userService.findByUsername(userName);
        ZoneId vietnamZoneId = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime nowInVietnam = LocalDateTime.now(vietnamZoneId).plusHours(24);
        long epochSeconds = nowInVietnam.toEpochSecond(ZoneOffset.ofHours(7));
        JSONObject payload = new JSONObject();
        payload.put("userName", user.getUsername());
        String role = user.getRoles().isEmpty() ? "USER" : user.getRoles().iterator().next();
        payload.put("role", role);
        payload.put("exp", epochSeconds);
        if (this.secret == null || this.secret.isEmpty()) {
            throw new IllegalStateException("JWT_SECRET is not configured.");
        }
        String header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String encodedHeader = encode(header.getBytes());
        String encodedPayload = encode(payload.toString().getBytes());
        String signature = hmacSha256(encodedHeader + "." + encodedPayload, this.secret);

        return encodedHeader + "." + encodedPayload + "." + signature;
    }

    public boolean isValidToken(String token, UserDetails userDetails) {
        String userName = extractUserNameFromJWT(token);
        return userName.equals(userDetails.getUsername()) && verifyExpiration(token);
    }
}
