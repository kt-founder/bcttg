package com.bcttg.security;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.ApiException;
import com.bcttg.common.AuditDetailUtil;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.dashboard.service.SystemAuditTrailService;
import com.bcttg.module.user.repository.UserAccountRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "Dang nhap va cap JWT")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final SecurityProperties properties;
    private final SystemAuditTrailService auditTrailService;
    private final UserAccountRepository userAccountRepository;

    public AuthController(
        AuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider,
        SecurityProperties properties,
        SystemAuditTrailService auditTrailService,
        UserAccountRepository userAccountRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.properties = properties;
        this.auditTrailService = auditTrailService;
        this.userAccountRepository = userAccountRepository;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody AuthRequest request, HttpServletRequest httpServletRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getPhone(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            String failureReason = resolveFailureReason(request.getPhone(), ex);
            auditTrailService.record(
                request.getPhone(),
                "LOGIN",
                "AUTH",
                request.getPhone(),
                buildLoginDetail(httpServletRequest, failureReason),
                "FAILED"
            );
            if ("User is inactive".equals(failureReason)) {
                throw new ApiException(ErrorCode.FORBIDDEN, HttpStatus.FORBIDDEN, failureReason);
            }
            throw ex;
        }
        String token = tokenProvider.generateToken(authentication);
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();
        auditTrailService.record(
            request.getPhone(),
            "LOGIN",
            "AUTH",
            request.getPhone(),
            buildLoginDetail(httpServletRequest, null),
            "SUCCESS"
        );
        return ApiResponse.success(new AuthResponse(token, "Bearer", properties.getJwt().getAccessTokenTtlMinutes(), roles));
    }

    private String buildLoginDetail(HttpServletRequest request, String failureReason) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("ip", resolveClientIp(request));
        values.put("device", request.getHeader("User-Agent"));
        values.put("failure_reason", failureReason);
        return AuditDetailUtil.encode(values);
    }

    private String resolveFailureReason(String phone, AuthenticationException ex) {
        return userAccountRepository.findByPhoneAndDeletedAtIsNull(phone)
            .filter(account -> !Boolean.TRUE.equals(account.getIsActive()))
            .map(account -> "User is inactive")
            .orElseGet(ex::getMessage);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
