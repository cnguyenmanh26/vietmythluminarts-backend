package com.social.vietmythluminartsbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email service for sending emails
 * Migrated from Node.js email functionality
 * 
 * Note: Requires Spring Mail configuration in application.yml
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Send password reset email
     * @param toEmail Recipient email
     * @param resetToken Reset token
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Password Reset Request - VietMyThLuminArts");
            message.setText(buildPasswordResetEmailBody(resetUrl));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
            // Don't throw exception - allow operation to continue
            // In production, you might want to queue the email for retry
        }
    }

    /**
     * Send welcome email
     * @param toEmail Recipient email
     * @param userName User's name
     */
    public void sendWelcomeEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to VietMyThLuminArts!");
            message.setText(buildWelcomeEmailBody(userName));

            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }

    /**
     * Send order confirmation email
     * @param toEmail Recipient email
     * @param orderNumber Order number
     * @param totalAmount Total order amount
     */
    public void sendOrderConfirmationEmail(String toEmail, String orderNumber, Double totalAmount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmation - " + orderNumber);
            message.setText(buildOrderConfirmationEmailBody(orderNumber, totalAmount));

            mailSender.send(message);
            log.info("Order confirmation email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to: {}", toEmail, e);
        }
    }

    // ==================== Email Body Builders ====================

    private String buildPasswordResetEmailBody(String resetUrl) {
        return String.format("""
                Hello,
                
                You requested to reset your password. Please click the link below to reset your password:
                
                %s
                
                This link will expire in 10 minutes.
                
                If you did not request this password reset, please ignore this email.
                
                Best regards,
                VietMyThLuminArts Team
                """, resetUrl);
    }

    private String buildWelcomeEmailBody(String userName) {
        return String.format("""
                Hello %s,
                
                Welcome to VietMyThLuminArts! We're excited to have you on board.
                
                You can now explore our collection of beautiful wooden toys and crafts.
                
                If you have any questions, feel free to contact us.
                
                Best regards,
                VietMyThLuminArts Team
                """, userName);
    }

    private String buildOrderConfirmationEmailBody(String orderNumber, Double totalAmount) {
        return String.format("""
                Hello,
                
                Thank you for your order!
                
                Order Number: %s
                Total Amount: $%.2f
                
                We will process your order and send you updates via email.
                
                You can track your order status in your account dashboard.
                
                Best regards,
                VietMyThLuminarts Team
                """, orderNumber, totalAmount);
    }
}

