package com.smart.controller;

import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
//import com.smart.helper.Message;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	Random random =new Random(1000);
	@Autowired
	 private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository ;
	
	@Autowired
	EmailService emailService;
	
	@GetMapping("/")
	public String home()
	{
		return "home";
	}
	
	@GetMapping("/signup")
	public String signup(Model model,HttpSession session)
	{
		 model.addAttribute("user", new User());
		    Object message = session.getAttribute("message");
		    if (message != null) {
		        model.addAttribute("message", message);
		        session.removeAttribute("message");
		    }
		return "signup";
	}
	
	//Handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,@RequestParam(value = "agreement" ,defaultValue = "false")boolean agreement,Model model,HttpSession session)
	{
		
		try {
			if(!agreement)
			{
				System.out.println("Not signed to terms and conditions");
				throw new Exception("Not signed to terms and conditions");
			}
			if(result.hasErrors())
			{
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("Agreement : "+agreement);
			System.out.println("User:"+user);
			User res = this.userRepository.save(user);
			
			model.addAttribute("user",new User());
			model.addAttribute("message",new Message("Registered Successfully !","alert-success"));
			return "signup";
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			model.addAttribute("user",user);
			model.addAttribute("message",new Message("Something went wrong !.. "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
		
		
	}
	
	@GetMapping("/signin")
	public String customLogin(Model model,HttpSession session)
	{
		Object message = session.getAttribute("message");
		if (message != null) {
	        model.addAttribute("message", message);
	        session.removeAttribute("message");
	    }
		return "signin";
	}
	
	
	@GetMapping("/about")
	public String about()
	{
		return "about";
	}
	
	@PostMapping("/send-otp-verification")
    public ResponseEntity<String> sendOTP(@RequestParam("email")String email,HttpSession session) {
		
		//creating otp
   	 SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(random.nextInt(10)); // Generates a random digit (0-9)
        }
           String otp= sb.toString();
   	
    	
    	//send otp to email code..
    	String subject="OTP from SME";
    	String message="OTP="+otp;
    	boolean flag = emailService.sendEmail(message, subject, email);
    	 
    	if(flag)
    	{
    		session.setAttribute("otp", otp);
    		session.setAttribute("email", email);
    		return ResponseEntity.ok("OTP sent");
    	}
    	else {
    		session.setAttribute("message", new Message("check your email id !!","success"));
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("OTP not sent");
    	}
	}
	
	@PostMapping("/verify-register-otp")
    public ResponseEntity<String> verifyRegisterOtp(@RequestParam String email, @RequestParam String otp,HttpSession session) {
		
        String savedOtp = (String) session.getAttribute("otp");
        System.out.println(savedOtp +" : "+otp);
        if (savedOtp != null && savedOtp.equals(otp)) {
            return ResponseEntity.ok("Verified");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid OTP");
        }
    }
	
	
}
