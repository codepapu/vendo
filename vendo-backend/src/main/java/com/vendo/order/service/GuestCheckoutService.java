package com.vendo.order.service;

import com.vendo.common.exception.BadRequestException;
import com.vendo.common.exception.ResourceNotFoundException;
import com.vendo.order.dto.request.GuestOrderRequest;
import com.vendo.order.dto.response.GuestOrderResponse;
import com.vendo.order.dto.response.OrderResponse;
import com.vendo.order.entity.Order;
import com.vendo.order.entity.OrderAddress;
import com.vendo.order.entity.OrderHistory;
import com.vendo.order.entity.OrderItem;
import com.vendo.order.repository.OrderRepository;
import com.vendo.user.entity.User;
import com.vendo.user.repository.UserRepository;
import com.vendo.user.service.AuthService;
import com.vendo.user.service.EmailService;
import com.vendo.user.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for handling guest checkout (non-authenticated users)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GuestCheckoutService {
    
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final com.vendo.user.service.EmailService emailService;
    private final com.vendo.common.email.service.EmailService commonEmailService;
    private final com.vendo.user.service.EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    
    @Value("${guest.session.token.expiration-hours:168}") // 7 days default
    private int sessionTokenExpirationHours;
    
    /**
     * Process guest checkout and create order
     * 
     * @param request Guest order request with email and order details
     * @return GuestOrderResponse with order details and session token
     */
    @Transactional
    public GuestOrderResponse processGuestCheckout(GuestOrderRequest request) {
        log.info("Processing guest checkout for email: {}", request.getEmail());
        
        // Validate email doesn't already exist as registered user
        boolean emailExists = userRepository.existsByEmail(request.getEmail());
        if (emailExists && request.getCreateAccount() == null) {
            throw new BadRequestException(
                "An account with this email already exists. Please login to continue."
            );
        }
        
        // Generate temporary session token for guest order access
        String sessionToken = generateSessionToken();
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusHours(sessionTokenExpirationHours);
        
        // Create order (userId will be null for guest orders)
        OrderResponse orderResponse = createGuestOrder(request, sessionToken);
        
        // Create account if requested
        boolean accountCreated = false;
        if (Boolean.TRUE.equals(request.getCreateAccount()) && request.getPassword() != null) {
            try {
                accountCreated = createAccountFromGuestOrder(request, orderResponse.getId());
                log.info("Account created for guest checkout: email={}, orderId={}", 
                    request.getEmail(), orderResponse.getId());
            } catch (Exception e) {
                log.error("Failed to create account for guest checkout: {}", e.getMessage());
                // Continue without account creation - order is still created
            }
        }
        
        // Send order confirmation email
        sendGuestOrderConfirmationEmail(request.getEmail(), orderResponse);
        
        // Build response
        return GuestOrderResponse.builder()
                .orderId(orderResponse.getId())
                .orderNumber(orderResponse.getCode())
                .email(request.getEmail())
                .status(orderResponse.getStatus())
                .sessionToken(sessionToken)
                .sessionTokenExpiresAt(tokenExpiresAt)
                .accountCreated(accountCreated)
                .message(accountCreated 
                    ? "Order created successfully. Your account has been created. Please check your email for login credentials."
                    : "Order created successfully. Please check your email for order confirmation.")
                .orderDetails(orderResponse)
                .build();
    }
    
    /**
     * Create order for guest user (without userId)
     */
    private OrderResponse createGuestOrder(GuestOrderRequest request, String sessionToken) {
        // Convert GuestOrderRequest to CreateOrderRequest format
        com.vendo.order.dto.request.CreateOrderRequest createOrderRequest = 
            com.vendo.order.dto.request.CreateOrderRequest.builder()
                .items(request.getItems())
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .shippingMethod(request.getShippingMethod())
                .couponCode(request.getCouponCode())
                .notes((request.getNotes() != null ? request.getNotes() + " | " : "") + 
                       "Guest Order - Session Token: " + sessionToken + " | Email: " + request.getEmail())
                .build();
        
        // Use OrderService to create guest order
        OrderResponse orderResponse = orderService.createGuestOrder(createOrderRequest, request.getEmail());
        
        // Store session token in order notes for retrieval
        Order order = orderRepository.findById(orderResponse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderResponse.getId()));
        String updatedNotes = (order.getNotes() != null ? order.getNotes() : "") + 
                             " | Session Token: " + sessionToken;
        order.setNotes(updatedNotes);
        orderRepository.save(order);
        
        return orderService.mapToResponse(order);
    }
    
    /**
     * Create user account from guest order
     */
    private boolean createAccountFromGuestOrder(GuestOrderRequest request, Long orderId) {
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Account already exists for email: {}", request.getEmail());
            return false;
        }
        
        try {
            // Create user account
            User user = User.builder()
                    .email(request.getEmail())
                    .username(request.getEmail().split("@")[0] + "_" + System.currentTimeMillis())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phone(request.getPhone())
                    .userType("CUSTOMER")
                    .isActive(true)
                    .isVerified(false) // Email verification required
                    .superUser(false)
                    .build();
            
            user = userRepository.save(user);
            
            // Link order to user
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
            order.setUser(user);
            orderRepository.save(order);
            
            // Send email verification
            emailVerificationService.sendVerificationEmail(user.getId());
            
            log.info("Account created and linked to order: userId={}, orderId={}", user.getId(), orderId);
            return true;
        } catch (Exception e) {
            log.error("Failed to create account from guest order: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate temporary session token for guest order access
     */
    private String generateSessionToken() {
        return "GUEST_" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
    
    /**
     * Send order confirmation email to guest
     */
    private void sendGuestOrderConfirmationEmail(String email, OrderResponse order) {
        try {
            // Use common email service with templates
            String customerName = order.getShippingAddress() != null ? 
                order.getShippingAddress().getName() : "Customer";
            commonEmailService.sendOrderConfirmationEmail(email, customerName, order);
            log.info("Order confirmation email sent to guest: {}", email);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to guest: {}", email, e);
            // Don't throw - order is still created
        }
    }
    
    /**
     * Get guest order by session token
     */
    @Transactional(readOnly = true)
    public OrderResponse getGuestOrderByToken(String sessionToken) {
        // Find order by session token in notes
        List<Order> orders = orderRepository.findAll().stream()
                .filter(order -> order.getNotes() != null && order.getNotes().contains("Session Token: " + sessionToken))
                .filter(order -> order.getUser() == null) // Guest order
                .toList();
        
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("Guest order", "sessionToken", sessionToken);
        }
        
        // Return most recent order
        Order order = orders.stream()
                .max(java.util.Comparator.comparing(Order::getCreatedAt))
                .orElseThrow(() -> new ResourceNotFoundException("Guest order", "sessionToken", sessionToken));
        
        return orderService.mapToResponse(order);
    }
    
    /**
     * Convert guest order to user account (link order to user after account creation)
     */
    @Transactional
    public boolean linkGuestOrderToUser(String sessionToken, Long userId) {
        try {
            OrderResponse orderResponse = getGuestOrderByToken(sessionToken);
            Order order = orderRepository.findById(orderResponse.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderResponse.getId()));
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            
            order.setUser(user);
            orderRepository.save(order);
            
            log.info("Guest order linked to user: orderId={}, userId={}", order.getId(), userId);
            return true;
        } catch (Exception e) {
            log.error("Failed to link guest order to user: {}", e.getMessage());
            return false;
        }
    }
}
