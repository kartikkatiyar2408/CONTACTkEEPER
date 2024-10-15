package com.ContactManager.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.ContactManager.Helper.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ContactManager.Dao.UserRepository;
import com.ContactManager.Entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;

	//@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "Home - Smart Contact Manager");

		return "home";
	}

	@GetMapping("/about")
	public String about(Model model) {
		model.addAttribute("title", "About - Smart Contact Manager");

		return "about";
	}

	@GetMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title", "Register - Smart Contact Manager");
		model.addAttribute("user", new User());

		return "signup";
	}

	@PostMapping("/doRegister")
	public String doRegister(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") Boolean agreement, Model model,
			HttpSession session) {
		try {
			if (!agreement) {
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			if (result.hasErrors()) {
				System.out.println("Error is : " + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}

			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			System.out.println(agreement);
			System.out.println(user);

			this.userRepository.save(user);

			model.addAttribute("user", new User());

			session.setAttribute("message", new Message("Successfully registered !!", "alert-success"));
			return "signup";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !! " + e.getMessage(), "alert-danger"));
			return "signup";
		}

	}

	@GetMapping("/signin")
	public String customLogin(Model model) {

		model.addAttribute("title", "Login page");
		return "login";
	}

	@GetMapping("/logout")
	public String Logout(Model model, HttpSession session) {

		if (session != null) {
			SecurityContextHolder.clearContext();
			session.invalidate();
		}
		model.addAttribute("title", "Login page");
		model.addAttribute("message", "Logout Successfully");

		return "login";
	}

}
