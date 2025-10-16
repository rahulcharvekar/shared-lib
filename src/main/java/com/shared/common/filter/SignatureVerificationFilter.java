package com.shared.common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SignatureVerificationFilter implements Filter {
    @Value("${app.signature.secret:secret}")
    private String secret;

    private static final String SIGNATURE_HEADER = "X-Signature";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String signature = httpRequest.getHeader(SIGNATURE_HEADER);
        if (signature != null) {
            String payload = httpRequest.getMethod() + httpRequest.getRequestURI();
            String expectedSignature = generateHmac(payload, secret);
            if (!expectedSignature.equals(signature)) {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Invalid signature\"}");
                ((jakarta.servlet.http.HttpServletResponse) response).setStatus(401);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private String generateHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate HMAC", e);
        }
    }
}
