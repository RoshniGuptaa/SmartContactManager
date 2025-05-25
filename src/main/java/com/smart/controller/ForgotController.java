package com.smart.controller;


import java.security.SecureRandom;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;


import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
 
	SecureRandom random = new SecureRandom();
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder bcrypt;
	
    @GetMapping("/forgot")
	public String emailForm(HttpSession session,Model model) {
    	Object message = session.getAttribute("message");
		if (message != null) {
	        model.addAttribute("message", message);
	        session.removeAttribute("message");
	    }
		return "forgot_email_form";
	}
    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam("email")String email,HttpSession session) {
		
    	//creating otp
    	 SecureRandom random = new SecureRandom();
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < 5; i++) {
             sb.append(random.nextInt(10)); // Generates a random digit (0-9)
         }
            String otp= sb.toString();
    	
    	//send otp to email code..
    	String subject="OTP from SME";
    	String message=""
    			+"<div style='border:1px solid #e2e2e2; padding:20px'>"
    			+"<h1>"
    			+"OTP is "
    			+"<b>"+otp
    			+"\n"
    			+"</h1>"
    			+"</div>";
    	boolean flag = emailService.sendEmail(message, subject, email);
    	 
    	if(flag)
    	{
    		session.setAttribute("otp", otp);
    		session.setAttribute("email",email );
    		
    		return "verify_otp";
    	}
    	else {
    		session.setAttribute("message", new Message("check your email id !!","success"));
    	return "redirect:/forgot";
    	}
	}
    
    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam("otp")String otp,HttpSession session,Model model)
    {
    	Message message=(Message) session.getAttribute("message");
    	if(message!=null)
    	{
    		model.addAttribute("message",message);
    		session.removeAttribute("message");
    	}
    	String myotp=(String) session.getAttribute("otp");
    	String email=(String) session.getAttribute("email");
    	
    	if(myotp.equals(otp))
    	{
    		User user=this.userRepository.getUserByEmail(email);
    		if(user==null)
    		{
    			//error message
    			session.setAttribute("message",new Message("User do not exist with this email","danger") );
    			return "redirect:/forgot";
    		}
    		return "password_change_form";
    	}
    	else
    	{
    		session.setAttribute("message",new Message("You have entered wrong otp !!","danger") );
    		return "verify_otp";
    	}
    }
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
    {
    	String email=(String) session.getAttribute("email");
    	User user = this.userRepository.getUserByEmail(email);
    	user.setPassword(this.bcrypt.encode(newpassword));
    	this.userRepository.save(user);
    	session.setAttribute("message", new Message("Your password is changed ..","success"));
    	return "redirect:/signin";
    }
    
    
}
