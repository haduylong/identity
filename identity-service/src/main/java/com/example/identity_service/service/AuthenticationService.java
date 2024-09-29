package com.example.identity_service.service;

import com.example.identity_service.dto.request.AuthenticationRequest;
import com.example.identity_service.dto.request.IntrospectRequest;
import com.example.identity_service.dto.request.LogoutRequest;
import com.example.identity_service.dto.request.RefreshRequest;
import com.example.identity_service.dto.response.AuthenticationResponse;
import com.example.identity_service.dto.response.IntrospectResponse;
import com.example.identity_service.entity.InvalidatedToken;
import com.example.identity_service.entity.User;
import com.example.identity_service.exception.AppException;
import com.example.identity_service.exception.ErrorCode;
import com.example.identity_service.repository.InvalidateTokenRepository;
import com.example.identity_service.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    InvalidateTokenRepository invalidateTokenRepository;

    @NonFinal // không đưa vào constructer
    @Value("${app.jwt.secret}")
    String JWT_SECRET;

    @NonFinal
    @Value("${app.jwt.valid-duration}")
    long VALID_DURATION;

    @NonFinal
    @Value("${app.jwt.refreshable-duration}")
    long REFRESHABLE_DURATION;

    public AuthenticationResponse authenticate(AuthenticationRequest request){
        // check username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        // check password
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if(!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    /* verify token */
    public IntrospectResponse introspect(IntrospectRequest request)
            throws ParseException, JOSEException {
            String token = request.getToken();
            boolean isValid = true;

            try {
                verifyToken(token, false);
            } catch (AppException e){
                isValid = false;
            }

            return IntrospectResponse.builder()
                    .valid(isValid)
                    .build();
    }

    /*
        lưu token hết hạn vào database
    */
    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        /*
            thời hạn verify của logout lấy theo thời hạn refresh, vì khi token chưa hoặc đã hết hạn mà vẫn còn trong
            thời hạn refresh thì có thể dùng token cũ để refresh token mới (đơn giản là token vẫn dùng được vào việc khác)
        */
        try {
            SignedJWT signedToken = verifyToken(request.getToken(), true);

            String jwtId = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryDate = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jwtId)
                    .expiryTime(expiryDate)
                    .build();

            invalidateTokenRepository.save(invalidatedToken);
        } catch (AppException exception) {
            log.error("Token already expired");
        }
    }


    public AuthenticationResponse refreshToken(RefreshRequest request)
            throws ParseException, JOSEException {
        /* chuyển token cũ thành hết hạn */
        SignedJWT signedJWT = verifyToken(request.getToken(), true);

        String jwtId = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        invalidateTokenRepository.save(InvalidatedToken.builder()
                        .id(jwtId)
                        .expiryTime(expiryTime)
                .build());

        /* cấp token mới */
        var username = signedJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username).orElseThrow(() ->
                new AppException(ErrorCode.UNAUTHENTICATED));

        String newToken = generateToken(user);

        return AuthenticationResponse.builder()
                .token(newToken)
                .authenticated(true)
                .build();
    }

    /*
      xác minh token
      throw ra exception nếu hết hạn và nếu đã logout;
      return: SignedJWT
    */
    private SignedJWT verifyToken (String token, boolean isRefresh) throws ParseException, JOSEException {
        // parse
        SignedJWT signedJWT = SignedJWT.parse(token);
        // verifier
        JWSVerifier jwsVerifier = new MACVerifier(JWT_SECRET.getBytes());

        // date expiration
        Date expiredDate = isRefresh ?
                new Date(signedJWT.getJWTClaimsSet().getExpirationTime()
                        .toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli())                                // thời hạn refresh token
                :signedJWT.getJWTClaimsSet().getExpirationTime();       // time token hết hạn

        boolean verified = signedJWT.verify(jwsVerifier);                       // verify

        if(!(verified && expiredDate.after(new Date())))                        // trả vè lỗi nếu token hết hạn
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        if(invalidateTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID()))    // trả về lỗi nếu đã logout
            throw new AppException(ErrorCode.UNAUTHENTICATED);                              // tức nễu đã logout thì token vô dụng

        return signedJWT;
    }


    private String generateToken(User user){
        try {
            // Create HMAC signer
            JWSSigner signer = new MACSigner(JWT_SECRET.getBytes());

            // create header
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

            /* create pay load */
            // 1. create body
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .issuer("example.com")
                    .subject(user.getUsername())
                    .issueTime(new Date())
                    .expirationTime(new Date(
                            Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                    ))
                    .claim("scope", buildScope(user)) // cách bổ xung thêm một số field; scope sẽ phục vụ cho việc phân quyền trong Spring
                    .jwtID(UUID.randomUUID().toString())    // jwt ID
                    .build();
            // 2. create pay load
            Payload payload = new Payload(jwtClaimsSet.toJSONObject());

            /* create JWT object */
            JWSObject jwsObject = new JWSObject(header, payload);
            // sign
            jwsObject.sign(signer);

            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Can not create token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user){
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());   // thêm role vào SCOPE

                if(!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());     // thêm các permission của role vào SCOPE
                    });
                }

            }); // stringJoiner::add <=> role -> stringJoiner.add(role)
        }

        return stringJoiner.toString();
    }
}
