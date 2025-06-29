package com.hicode.backend.features.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hicode.backend.features.auth.dto.AuthResponse;
import com.hicode.backend.features.auth.dto.LoginRequest;
import com.hicode.backend.features.auth.dto.RegisterRequest;
import com.hicode.backend.features.auth.dto.ResendOtpRequest;
import com.hicode.backend.features.auth.dto.VerifyRequest;
import com.hicode.backend.model.entity.Role;
import com.hicode.backend.model.entity.User;
import com.hicode.backend.model.enums.UserStatus;
import com.hicode.backend.model.entity.VerificationToken;
import com.hicode.backend.features.user.repository.RoleRepository;
import com.hicode.backend.features.user.repository.UserRepository;
import com.hicode.backend.features.auth.repository.VerificationTokenRepository;
import com.hicode.backend.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;
    @Autowired private VerificationTokenRepository tokenRepository;
    @Autowired private EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Bước 1: User gửi thông tin, hệ thống tạo và gửi OTP.
     */
    @Transactional
    public void requestRegistration(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        // Tạo mã OTP ngẫu nhiên gồm 6 chữ số
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Băm mật khẩu trước khi lưu tạm
        registerRequest.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        try {
            // Chuyển toàn bộ thông tin đăng ký thành chuỗi JSON để lưu tạm
            String registrationInfoJson = objectMapper.writeValueAsString(registerRequest);

            // Tìm xem có token cũ cho email này không để xóa/cập nhật
            tokenRepository.findByEmail(registerRequest.getEmail()).ifPresent(tokenRepository::delete);

            VerificationToken verificationToken = new VerificationToken(otp, registerRequest.getEmail(), registrationInfoJson, 10); // OTP hết hạn sau 10 phút
            tokenRepository.save(verificationToken);

            // Gửi email chứa mã OTP
            String emailBody = "Mã xác thực đăng ký tài khoản của bạn là: " + otp + ". Mã này sẽ hết hạn sau 10 phút.";
            emailService.sendEmail(registerRequest.getEmail(), "Xác thực đăng ký tài khoản hiến máu", emailBody);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing registration data.", e);
        }
    }

    /**
     * Bước 2: User gửi OTP lên để xác thực và hoàn tất đăng ký.
     */
    @Transactional
    public User verifyAndCompleteRegistration(VerifyRequest verifyRequest) {
        VerificationToken token = tokenRepository.findByEmail(verifyRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired verification request. Please register again."));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(token);
            throw new IllegalStateException("OTP has expired. Please register again.");
        }

        if (!token.getToken().equals(verifyRequest.getOtp())) {
            throw new IllegalArgumentException("Invalid OTP code.");
        }

        try {
            // Lấy lại thông tin đăng ký từ chuỗi JSON
            RegisterRequest registrationInfo = objectMapper.readValue(token.getUserRegistrationInfo(), RegisterRequest.class);

            // Tạo User mới từ thông tin đã lưu
            User user = new User();
            user.setFullName(registrationInfo.getFullName());
            user.setEmail(registrationInfo.getEmail());
            user.setUsername(registrationInfo.getEmail());
            user.setPasswordHash(registrationInfo.getPassword()); // Mật khẩu đã được băm
            user.setPhone(registrationInfo.getPhone());
            user.setAddress(registrationInfo.getAddress());
            user.setDateOfBirth(registrationInfo.getDateOfBirth());
            user.setStatus(UserStatus.ACTIVE);
            user.setEmailVerified(true); // Đánh dấu email đã được xác thực

            Role userRole = roleRepository.findByName("Member")
                    .orElseThrow(() -> new RuntimeException("Error: Role 'Member' not found."));
            user.setRole(userRole);

            User savedUser = userRepository.save(user);

            // Xóa token sau khi đã sử dụng
            tokenRepository.delete(token);

            return savedUser;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading registration data.", e);
        }
    }

    // Phương thức đăng nhập giữ nguyên
    public AuthResponse loginUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));
        return new AuthResponse(jwt, user.getId(), user.getEmail(), user.getFullName(), user.getRole().getName());
    }

    /**
     * Bước mới: Gửi lại mã OTP cho một email đã đăng ký tạm thời.
     */
    @Transactional
    public void resendOtp(ResendOtpRequest resendRequest) {
        // 1. Tìm token hiện có của email này
        VerificationToken verificationToken = tokenRepository.findByEmail(resendRequest.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("No pending registration found for this email. Please start over."));

        // 2. Tạo mã OTP mới
        String newOtp = String.format("%06d", new Random().nextInt(999999));

        // 3. Cập nhật token và thời gian hết hạn mới (10 phút)
        verificationToken.setToken(newOtp);
        verificationToken.setExpiryDate(LocalDateTime.now().plusMinutes(10));
        tokenRepository.save(verificationToken);

        // 4. Gửi lại email chứa mã OTP mới
        String emailBody = "Mã xác thực mới của bạn là: " + newOtp + ". Mã này sẽ hết hạn sau 10 phút.";
        emailService.sendEmail(resendRequest.getEmail(), "Yêu cầu gửi lại mã OTP", emailBody);
    }
}