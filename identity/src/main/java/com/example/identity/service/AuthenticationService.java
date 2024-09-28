package com.example.identity.service;

import com.example.identity.dto.request.auth.AuthenticationRequest;
import com.example.identity.dto.request.auth.IntrospectRequest;
import com.example.identity.dto.request.auth.LogoutRequest;
import com.example.identity.dto.request.auth.RefreshRequest;
import com.example.identity.dto.response.auth.AuthenticationResponse;
import com.example.identity.dto.response.auth.IntrospectResponse;
import com.example.identity.entity.InvalidatedToken;
import com.example.identity.entity.User;
import com.example.identity.exception.AppException;
import com.example.identity.exception.ErrorCode;
import com.example.identity.repository.InvalidatedTokenRepository;
import com.example.identity.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    PasswordEncoder passwordEncoder;

    @NonFinal       // khong dua vao constructor
    @Value("${app.jwt.secret-key}")
    String SECRET_KEY;

    @NonFinal
    @Value("${app.jwt.token-valid-duration}")
    long VALID_DURATION;

    @NonFinal
    @Value("${app.jwt.refreshable-duration}")
    long REFRESH_VALID_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    public IntrospectResponse introspect(IntrospectRequest request) {
        verifyToken(request.getToken(), false);

        return IntrospectResponse.builder()
                .valid(true)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException {
        /*
        thời hạn verify của logout lấy theo thời hạn refresh, vì khi token chưa hoặc đã hết hạn mà vẫn còn trong
        thời hạn refresh thì có thể dùng token cũ để refresh token mới (đơn giản là token vẫn dùng được vào việc khác)
        */
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryDate = signedJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jwtId)
                .expiryDate(expiryDate)
                .build();

        invalidatedTokenRepository.save(invalidatedToken);
    }

    public AuthenticationResponse refresh(RefreshRequest request) throws ParseException {
        /* verify */
        SignedJWT signedJWT = verifyToken(request.getToken(), true);
        /* chuyển token cũ thành hết hạn */
        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryDate = signedJWT.getJWTClaimsSet().getExpirationTime();
        invalidatedTokenRepository.save(InvalidatedToken.builder()
                                            .id(jwtId)
                                            .expiryDate(expiryDate)
                                    .build());

        /* tạo token mới */
        String username = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String newToken = generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .authenticated(true)
                .build();
    }

    private String generateToken(User user) {
        try {
            // Create HMAC signer
            JWSSigner jwsSigner = new MACSigner(SECRET_KEY.getBytes());
            // Create Header
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            // Create payload
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issuer("example.com")
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(
                            Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                    ))
                    .claim("scope", buildScope(user))
                    .build();

            Payload payload = new Payload(jwtClaimsSet.toJSONObject());

            // Prepare JWS object with payload and header
            JWSObject jwsObject = new JWSObject(header, payload);

            // Apply the HMAC
            jwsObject.sign(jwsSigner);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.CAN_NOT_CREATE_TOKEN);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");

        user.getRoles().forEach(
                role -> {
                    stringJoiner.add("ROLE_" + role.getName());
                    role.getPermissions().forEach(
                            permission -> stringJoiner.add(permission.getName())
                    );
                }
        );

        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) {
        try {
            // Parse
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verifier
            JWSVerifier jwsVerifier = new MACVerifier(SECRET_KEY.getBytes());

            // verify
            boolean verified = signedJWT.verify(jwsVerifier);

            // check date expired
            Date expiedDate = isRefresh ?
                    new Date(signedJWT.getJWTClaimsSet().getExpirationTime()
                            .toInstant().plus(REFRESH_VALID_DURATION, ChronoUnit.SECONDS)
                            .toEpochMilli())
                    :signedJWT.getJWTClaimsSet().getExpirationTime();

            // trả về lỗi nếu không hợp lệ và hết hạn
            if(!(verified && expiedDate.after(new Date())))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            // kiểm tra token đã bị logout
            if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))
                throw new AppException(ErrorCode.UNAUTHENTICATED);

            return signedJWT;

        } catch (ParseException | JOSEException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
    }
}
