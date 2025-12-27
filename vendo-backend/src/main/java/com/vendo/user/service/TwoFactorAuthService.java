package com.vendo.user.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.user.dto.request.Verify2FARequest;
import com.vendo.user.dto.response.TwoFactorAuthResponse;
import com.vendo.user.entity.TwoFactorAuth;
import com.vendo.user.entity.User;
import com.vendo.user.repository.TwoFactorAuthRepository;
import com.vendo.user.repository.UserRepository;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.QrUriGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwoFactorAuthService {
    
    private final TwoFactorAuthRepository twoFactorAuthRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    
    @Value("${app.name:Vendo}")
    private String appName;
    
    @Value("${app.2fa.issuer:Vendo}")
    private String issuer;
    
    private static final int BACKUP_CODE_COUNT = 10;
    private static final int BACKUP_CODE_LENGTH = 8;
    
    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrUriGenerator qrUriGenerator = new QrUriGenerator();
    private final QrGenerator qrGenerator = new QrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
    
    /**
     * Enable 2FA for a user
     */
    @Transactional
    public TwoFactorAuthResponse enable2FA(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Check if 2FA is already enabled
        Optional<TwoFactorAuth> existing2FA = twoFactorAuthRepository.findByUserId(userId);
        if (existing2FA.isPresent() && existing2FA.get().getEnabled()) {
            throw new BadRequestException("2FA is already enabled for this user");
        }
        
        // Generate secret key
        String secret = secretGenerator.generate();
        
        // Generate backup codes
        List<String> backupCodes = generateBackupCodes();
        String hashedBackupCodes = hashBackupCodes(backupCodes);
        
        // Generate QR code
        String qrCodeUrl = generateQRCode(user.getEmail(), secret);
        
        // Create or update 2FA record
        TwoFactorAuth twoFactorAuth;
        if (existing2FA.isPresent()) {
            twoFactorAuth = existing2FA.get();
            twoFactorAuth.setSecretKey(secret);
            twoFactorAuth.setBackupCodes(hashedBackupCodes);
            twoFactorAuth.setQrCodeUrl(qrCodeUrl);
            twoFactorAuth.setEnabled(false); // Not enabled until verified
            twoFactorAuth.setVerifiedAt(null);
        } else {
            twoFactorAuth = TwoFactorAuth.builder()
                    .userId(userId)
                    .secretKey(secret)
                    .backupCodes(hashedBackupCodes)
                    .qrCodeUrl(qrCodeUrl)
                    .enabled(false)
                    .build();
        }
        
        twoFactorAuth = twoFactorAuthRepository.save(twoFactorAuth);
        
        log.info("2FA setup initiated for user: {}", userId);
        
        return buildResponse(twoFactorAuth, secret, backupCodes, true);
    }
    
    /**
     * Verify and enable 2FA
     */
    @Transactional
    public TwoFactorAuthResponse verify2FACode(Long userId, Verify2FARequest request) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("2FA", "userId", userId));
        
        if (twoFactorAuth.getEnabled()) {
            throw new BadRequestException("2FA is already enabled and verified");
        }
        
        // Verify TOTP code
        boolean isValid = false;
        if (request.getCode() != null && !request.getCode().isEmpty()) {
            isValid = verifyCode(twoFactorAuth.getSecretKey(), request.getCode());
        }
        
        // If TOTP code is invalid, try backup code
        if (!isValid && request.getBackupCode() != null && !request.getBackupCode().isEmpty()) {
            isValid = verifyBackupCode(twoFactorAuth, request.getBackupCode());
        }
        
        if (!isValid) {
            throw new BadRequestException("Invalid 2FA code");
        }
        
        // Enable 2FA
        twoFactorAuth.setEnabled(true);
        twoFactorAuth.setVerifiedAt(LocalDateTime.now());
        twoFactorAuth = twoFactorAuthRepository.save(twoFactorAuth);
        
        log.info("2FA verified and enabled for user: {}", userId);
        
        return buildResponse(twoFactorAuth, null, null, false);
    }
    
    /**
     * Disable 2FA for a user
     */
    @Transactional
    public void disable2FA(Long userId) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("2FA", "userId", userId));
        
        if (!twoFactorAuth.getEnabled()) {
            throw new BadRequestException("2FA is not enabled for this user");
        }
        
        twoFactorAuth.setEnabled(false);
        twoFactorAuth.setSecretKey(null);
        twoFactorAuth.setBackupCodes(null);
        twoFactorAuth.setQrCodeUrl(null);
        twoFactorAuth.setVerifiedAt(null);
        twoFactorAuthRepository.save(twoFactorAuth);
        
        log.info("2FA disabled for user: {}", userId);
    }
    
    /**
     * Generate new backup codes
     */
    @Transactional
    public TwoFactorAuthResponse generateBackupCodes(Long userId) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("2FA", "userId", userId));
        
        if (!twoFactorAuth.getEnabled()) {
            throw new BadRequestException("2FA is not enabled for this user");
        }
        
        // Generate new backup codes
        List<String> backupCodes = generateBackupCodes();
        String hashedBackupCodes = hashBackupCodes(backupCodes);
        
        twoFactorAuth.setBackupCodes(hashedBackupCodes);
        twoFactorAuth.setLastUsedBackupCode(null);
        twoFactorAuth = twoFactorAuthRepository.save(twoFactorAuth);
        
        log.info("New backup codes generated for user: {}", userId);
        
        return buildResponse(twoFactorAuth, null, backupCodes, true);
    }
    
    /**
     * Verify 2FA code during login
     */
    public boolean verify2FACodeForLogin(Long userId, String code) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElse(null);
        
        if (twoFactorAuth == null || !twoFactorAuth.getEnabled()) {
            return false;
        }
        
        // Verify TOTP code
        if (verifyCode(twoFactorAuth.getSecretKey(), code)) {
            return true;
        }
        
        // Try backup code
        return verifyBackupCode(twoFactorAuth, code);
    }
    
    /**
     * Check if 2FA is enabled for a user
     */
    public boolean is2FAEnabled(Long userId) {
        return twoFactorAuthRepository.findByUserId(userId)
                .map(TwoFactorAuth::getEnabled)
                .orElse(false);
    }
    
    /**
     * Get 2FA status for a user
     */
    @Transactional(readOnly = true)
    public TwoFactorAuthResponse get2FAStatus(Long userId) {
        TwoFactorAuth twoFactorAuth = twoFactorAuthRepository.findByUserId(userId)
                .orElse(null);
        
        if (twoFactorAuth == null) {
            return TwoFactorAuthResponse.builder()
                    .userId(userId)
                    .enabled(false)
                    .build();
        }
        
        return buildResponse(twoFactorAuth, null, null, false);
    }
    
    // Private helper methods
    
    private boolean verifyCode(String secret, String code) {
        try {
            return codeVerifier.isValidCode(secret, code);
        } catch (Exception e) {
            log.error("Error verifying TOTP code", e);
            return false;
        }
    }
    
    private boolean verifyBackupCode(TwoFactorAuth twoFactorAuth, String backupCode) {
        if (twoFactorAuth.getBackupCodes() == null || twoFactorAuth.getBackupCodes().isEmpty()) {
            return false;
        }
        
        try {
            List<String> hashedCodes = objectMapper.readValue(
                    twoFactorAuth.getBackupCodes(),
                    new TypeReference<List<String>>() {}
            );
            
            for (String hashedCode : hashedCodes) {
                if (passwordEncoder.matches(backupCode, hashedCode)) {
                    // Remove used backup code
                    hashedCodes.remove(hashedCode);
                    twoFactorAuth.setBackupCodes(objectMapper.writeValueAsString(hashedCodes));
                    twoFactorAuth.setLastUsedBackupCode(LocalDateTime.now());
                    twoFactorAuthRepository.save(twoFactorAuth);
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("Error verifying backup code", e);
        }
        
        return false;
    }
    
    private List<String> generateBackupCodes() {
        Random random = new Random();
        return IntStream.range(0, BACKUP_CODE_COUNT)
                .mapToObj(i -> {
                    StringBuilder code = new StringBuilder();
                    for (int j = 0; j < BACKUP_CODE_LENGTH; j++) {
                        code.append(random.nextInt(10));
                    }
                    return code.toString();
                })
                .collect(Collectors.toList());
    }
    
    private String hashBackupCodes(List<String> backupCodes) {
        try {
            List<String> hashedCodes = backupCodes.stream()
                    .map(passwordEncoder::encode)
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(hashedCodes);
        } catch (Exception e) {
            log.error("Error hashing backup codes", e);
            throw new BadRequestException("Failed to generate backup codes");
        }
    }
    
    private String generateQRCode(String email, String secret) {
        try {
            QrData data = new QrData.Builder()
                    .label(email)
                    .secret(secret)
                    .issuer(issuer)
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();
            
            String qrUri = qrUriGenerator.getUri(data);
            
            // Generate QR code image as base64
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrUri, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();
            
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new BadRequestException("Failed to generate QR code");
        }
    }
    
    private TwoFactorAuthResponse buildResponse(
            TwoFactorAuth twoFactorAuth,
            String secret,
            List<String> backupCodes,
            boolean includeSetupInfo
    ) {
        TwoFactorAuthResponse.TwoFactorAuthResponseBuilder builder = TwoFactorAuthResponse.builder()
                .id(twoFactorAuth.getId())
                .userId(twoFactorAuth.getUserId())
                .enabled(twoFactorAuth.getEnabled())
                .verifiedAt(twoFactorAuth.getVerifiedAt())
                .createdAt(twoFactorAuth.getCreatedAt())
                .updatedAt(twoFactorAuth.getUpdatedAt());
        
        if (includeSetupInfo) {
            builder.secretKey(secret)
                    .backupCodes(backupCodes)
                    .qrCodeUrl(twoFactorAuth.getQrCodeUrl());
            
            // Build setup instructions
            User user = userRepository.findById(twoFactorAuth.getUserId()).orElse(null);
            String accountName = user != null ? user.getEmail() : "User";
            
            TwoFactorAuthResponse.SetupInstructions instructions = TwoFactorAuthResponse.SetupInstructions.builder()
                    .qrCodeUrl(twoFactorAuth.getQrCodeUrl())
                    .manualEntryKey(secret)
                    .issuer(issuer)
                    .accountName(accountName)
                    .steps(Arrays.asList(
                            "Scan the QR code with your authenticator app (Google Authenticator, Authy, etc.)",
                            "Enter the 6-digit code from your app to verify and enable 2FA",
                            "Save your backup codes in a safe place - you'll need them if you lose access to your authenticator app"
                    ))
                    .build();
            
            builder.setupInstructions(instructions);
        }
        
        return builder.build();
    }
}

