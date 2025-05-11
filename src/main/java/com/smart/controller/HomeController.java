package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
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

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	 private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository ;
	
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
	public String customLogin(Model model)
	{
		return "signin";
	}
	
	
	@GetMapping("/about")
	public String about()
	{
		return "about";
	}
	
	
}
