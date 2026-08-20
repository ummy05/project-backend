package FYP.project_backend.auth.controller;
import FYP.project_backend.auth.OtpVerification;
import FYP.project_backend.auth.OtpVerificationRepository;
import FYP.project_backend.auth.dto.*;
import FYP.project_backend.auth.jwt.JwtService;
import FYP.project_backend.notification.NotificationService;
import FYP.project_backend.notification.NotificationType;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import FYP.project_backend.user.UserRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

@CrossOrigin("*")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationService notificationService;
    private final OtpVerificationRepository otpRepository;


    // REGISTER

    // ==============================
// REGISTER
// ==============================

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        // ==============================
        // CHECK EMAIL
        // ==============================

        if (userRepository.existsByEmail(request.getEmail())) {

            return ResponseEntity
                    .badRequest()
                    .body("Email already registered.");
        }


        // ==============================
        // CHECK PHONE
        // ==============================

        if (userRepository.existsByPhoneNumber(
                request.getPhoneNumber())) {

            return ResponseEntity
                    .badRequest()
                    .body("Phone number already registered.");
        }


        // ==============================
        // GENERATE BUSINESS NUMBER
        // ==============================

        String businessRegistrationNumber = null;

        if (request.getRole() == Role.BUSINESS_OWNER) {

            businessRegistrationNumber =
                    generateBusinessRegistrationNumber();
        }


        // ==============================
        // CREATE USER
        // ==============================

        User user = User.builder()

                .fullName(request.getFullName())

                .email(request.getEmail())

                .phoneNumber(request.getPhoneNumber())

                .password(
                        passwordEncoder.encode(
                                request.getPassword()
                        )
                )

                .age(request.getAge())

                .gender(request.getGender())

                .address(request.getAddress())

                .nationality(request.getNationality())

                .role(request.getRole())

                // ==============================
                // BUSINESS INFORMATION
                // ==============================

                .businessName(
                        request.getBusinessName()
                )

                .businessType(
                        request.getBusinessType()
                )

                .businessAddress(
                        request.getBusinessAddress()
                )

                .businessRegistrationNumber(
                        businessRegistrationNumber
                )

                .enabled(true)

                .build();


        userRepository.save(user);


        // ==============================
        // WELCOME NOTIFICATION
        // ==============================

        try {

            notificationService.notify(

                    user,

                    "Welcome to ICT-Based Coastal Conservation",

                    "Account Created Successfully",

                    "Welcome to the ICT-Based Coastal Conservation & Revenue Monitoring System. Your account has been created successfully and is now ready to use.",

                    NotificationType.SYSTEM,

                    "Account Role",

                    user.getRole().name(),

                    "Login",

                    "http://localhost:4200/login"

            );

        } catch (Exception ex) {

            ex.printStackTrace();

        }


        return ResponseEntity.ok(
                "Registration successful"
        );
    }


    // LOGIN

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getEmail());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> myProfile(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        return ResponseEntity.ok(user);

    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(

            @RequestBody UserRequest request){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        user.setFullName(request.getFullName());

        user.setPhoneNumber(request.getPhoneNumber());

        user.setAge(request.getAge());

        user.setGender(request.getGender());

        user.setAddress(request.getAddress());

        userRepository.save(user);

        return ResponseEntity.ok(user);

    }

    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(

            @RequestBody ChangePasswordRequest request){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        if(!passwordEncoder.matches(

                request.getCurrentPassword(),

                user.getPassword())){

            return ResponseEntity.badRequest()

                    .body("Current password is incorrect.");

        }

        user.setPassword(

                passwordEncoder.encode(

                        request.getNewPassword()

                )

        );

        userRepository.save(user);

        return ResponseEntity.ok(

                "Password changed successfully."

        );

    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(

            @RequestBody ForgotPasswordRequest request){

        User user = userRepository

                .findByEmail(request.getEmail())

                .orElse(null);

        if(user == null){

            return ResponseEntity.badRequest()

                    .body("Email not found.");

        }

        String otp = generateOtp();

        OtpVerification verification =

                otpRepository

                        .findByEmail(user.getEmail())

                        .orElse(

                                new OtpVerification()

                        );

        verification.setEmail(user.getEmail());

        verification.setOtp(otp);

        verification.setVerified(false);

        verification.setCreatedAt(LocalDateTime.now());

        verification.setExpiresAt(

                LocalDateTime.now().plusMinutes(10)

        );

        otpRepository.save(verification);

        notificationService.notify(

                user,

                "Password Reset Verification",

                "Password Reset",

                "Use the verification code below to reset your password. The code is valid for 10 minutes.",

                NotificationType.SECURITY,

                "OTP Code",

                otp,

                "Verify OTP",

                ""

        );

        return ResponseEntity.ok(

                "OTP sent successfully."

        );

    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(

            @RequestBody VerifyOtpRequest request){

        OtpVerification otp = otpRepository

                .findByEmail(request.getEmail())

                .orElse(null);

        if(otp == null){

            return ResponseEntity.badRequest()

                    .body("OTP not found.");

        }

        if(otp.getExpiresAt().isBefore(LocalDateTime.now())){

            return ResponseEntity.badRequest()

                    .body("OTP expired.");

        }

        if(!otp.getOtp().equals(request.getOtp())){

            return ResponseEntity.badRequest()

                    .body("Invalid OTP.");

        }

        otp.setVerified(true);

        otpRepository.save(otp);

        return ResponseEntity.ok(

                "OTP verified successfully."

        );

    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(

            @RequestBody ResetPasswordRequest request){

        OtpVerification otp = otpRepository

                .findByEmail(request.getEmail())

                .orElse(null);

        if(otp == null){

            return ResponseEntity.badRequest()

                    .body("OTP not found.");

        }

        if(!otp.isVerified()){

            return ResponseEntity.badRequest()

                    .body("OTP not verified.");

        }

        User user = userRepository

                .findByEmail(request.getEmail())

                .orElse(null);

        if(user == null){

            return ResponseEntity.notFound().build();

        }

        user.setPassword(

                passwordEncoder.encode(

                        request.getNewPassword()

                )

        );

        userRepository.save(user);

        otpRepository.delete(otp);

        return ResponseEntity.ok(

                "Password reset successfully."

        );

    }

    private String generateOtp(){

        String digits = "0123456789";

        StringBuilder otp = new StringBuilder();

        java.util.Random random = new java.util.Random();

        while (otp.length() < 6){

            char c = digits.charAt(random.nextInt(digits.length()));

            if(otp.indexOf(String.valueOf(c)) == -1){

                otp.append(c);

            }

        }

        return otp.toString();

    }

    private String generateBusinessRegistrationNumber() {

        String number;

        do {

            int randomNumber =
                    100000 + new java.util.Random()
                            .nextInt(900000);

            number = "CM-BIZ-" + randomNumber;

        } while (
                userRepository
                        .existsByBusinessRegistrationNumber(number)
        );

        return number;
    }

}
