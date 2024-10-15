package com.ContactManager.Controller;

import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ContactManager.Dao.UserRepository;
import com.ContactManager.Entities.User;
import com.ContactManager.Service.EmailService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/openForgotPasswordForm")
    public String openEmailForm() {
        return "forgotEmailForm";
    }

    @PostMapping("/sendOTP")
    public String sendOTP(@RequestParam("forgotEmail") String forgotEmail, HttpSession session) {

        Random random = new Random(10000);
        int OTP = random.nextInt(100000);

        System.out.println(forgotEmail + " " + OTP);

        String subject = "OTP from Contact Manager";
        String message = "" +
                "<div style='border:1px solid #e2e2e2;padding:20px'>" +
                "<h1> OTP is <b>" +
                OTP + "</b></h1>" +
                "</div>";
        String to = forgotEmail;

        User user = this.userRepository.getUserByUserName(forgotEmail);

        if (user == null) {
            session.setAttribute("message", "User doesn't exist with this email !!");
            return "forgotEmailForm";
        }

        boolean flag = this.emailService.sendEmail(subject, message, to);

        if (flag) {

            session.setAttribute("OTP", OTP);
            session.setAttribute("email", forgotEmail);

            return "verifyOtp";
        } else {

            session.setAttribute("message", "Check your email !!");

            return "forgotEmailForm";
        }

    }

    @PostMapping("/verifyOTP")
    public String verifyOTP(@RequestParam("OTP") int OTP, HttpSession session) {

        int myOtp = (int) session.getAttribute("OTP");
        String email = (String) session.getAttribute("email");

        if (myOtp == OTP) {
            return "passwordChangeForm";
        } else {
            session.setAttribute("message", "You have entered wrong OTP");
            return "verifyOtp";
        }
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {

        String email = (String) session.getAttribute("email");
        User user = this.userRepository.getUserByUserName(email);
        user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
        this.userRepository.save(user);

        return "redirect:/signin?change=Password changed successfully...";

    }
}
