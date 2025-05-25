package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.websocket.Session;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ContactRepository contactRepository;
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		//Get username
				String userName=principal.getName();
				System.out.println("USErNAME :"+userName);
				//Get user using username
				User user = userRepository.getUserByEmail(userName);
				System.out.println(user);
				model.addAttribute("user",user);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal, HttpSession session)
	{
		//Message
		Object message = session.getAttribute("message");
		if (message != null) {
	        model.addAttribute("message", message);
	        session.removeAttribute("message");
	    }
		return "normal/user_dashboard";
	}
	
	//open add form handler
	
	@GetMapping("/add-contact")
	public String apenAddContactForm(Model model)
	{
	    model.addAttribute("contact",new Contact());
	    return "normal/add_contact_form";
	}
	//processing add contact form
	@PostMapping("/process-contact")
	public String processAddContact(@Valid @ModelAttribute Contact contact,BindingResult result,
			                     Principal principal,Model model,@RequestParam("profileImage") MultipartFile file){
		try {
			if(result.hasErrors())
			{
				model.addAttribute("contact",contact);
				return "normal/add_contact_form";
			}
			String name=principal.getName();
			User user=this.userRepository.getUserByEmail(name);
			
			//processing and uploading file
			if(file.isEmpty())
			{
				System.out.println("Image not given");
			}
			else {
				//add file
				contact.setImage(file.getOriginalFilename());
				File file2 = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(file2.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(),path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("File Uploaded");
			}
			//message
			model.addAttribute("contact",new Contact());
			model.addAttribute("message",new Message("Person added successfully in your contact !","alert-success"));
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			System.out.println(user.getName()+" contact added : "+contact);
			
			
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("ERROR"+e.getMessage());
			model.addAttribute("message",new Message("Something Went wrong ! try again ..","alert-danger"));
		}
		return "normal/add_contact_form";
	   }
		//Show Contacts handler
		//per page 5[n]
	//current page=0
	@GetMapping("/show-contacts/{page}")
		public String showContacts(@PathVariable("page") Integer page, Principal principal,Model model,HttpSession session) {
		
		Message message = (Message)session.getAttribute("message");
		if(message!=null)
		{
			model.addAttribute(message);
			session.removeAttribute("message");
		}
		String userEmail = principal.getName();
		User user = userRepository.getUserByEmail(userEmail);
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contactsByUser = contactRepository.findContactsByUserId(user.getId(),pageable);
		model.addAttribute("contacts",contactsByUser);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages", contactsByUser.getTotalPages());
		
		
			return "normal/show_contacts";
		}
	
	//Showing particular contact
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal,HttpSession session)
	{
		Object message = session.getAttribute("message");
		if (message != null) {
	        model.addAttribute("message", message);
	        session.removeAttribute("message");
	    }
		Optional<Contact> contactbyId = this.contactRepository.findById(cId);
		Contact contact=contactbyId.get();
		
		String userName=principal.getName();
		User user=this.userRepository.getUserByEmail(userName);
		if(user.getId()==contact.getUser().getId())
		{
			model.addAttribute("contact", contact);
			model.addAttribute("title",contact.getName());
		}
		
		return "normal/contact_detail";
	}
	
	//Delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId,Model model,Principal principal,HttpSession session)
	{
		String userName=principal.getName();
		User user=this.userRepository.getUserByEmail(userName);
		
		
		Optional<Contact> byId = this.contactRepository.findById(cId);
		Contact contact=byId.get();
		System.out.println(user.getId() +"  .. "+contact.getUser().getId());
		if(user.getId()==contact.getUser().getId()) {
			System.out.println("Contact belongs to user. Deleting...");
			try {
				String imagePath = new ClassPathResource("static/img/" + contact.getImage()).getFile().getAbsolutePath();

	            File file = new File(imagePath);
	            if (file.exists()) {
	                file.delete();
	                System.out.println("Image file deleted: " + imagePath);
	            }
	        } catch (Exception e) {
	            System.out.println("Error deleting image: " + e.getMessage());
	        }
			contact.setUser(null);
		   this.contactRepository.delete(contact);
		   session.setAttribute("message", new Message("contact deleted successfully","success"));
		}
		else
			System.out.println("Contact does NOT belong to user. Deletion skipped.");
		return "redirect:/user/show-contacts/0";
		
	}
	// update contact form
	
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid,Model model) {
	    Contact contact=this.contactRepository.findById(cid).get() ;
		model.addAttribute("contact",contact);
		return "normal/update_contact";
		
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,Model model,@RequestParam("profileImage") MultipartFile file,Principal principal,HttpSession session)
	{
		try {
			//get Old contact detail
			Contact contactOld=this.contactRepository.findById(contact.getcId()).get();
			//image..
			if(!file.isEmpty())
			{
				//delete old photo
				String imagePath = new ClassPathResource("static/img/" + contactOld.getImage()).getFile().getAbsolutePath();

	            File fileOld = new File(imagePath);
	            if (fileOld.exists()) {
	                fileOld.delete();
	                System.out.println("Old Image file deleted: " + imagePath);
	            }
	            
	            //update new photo
	            File saveFile=new ClassPathResource("static/img").getFile();
	            Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
	            Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
	            
	            contact.setImage(file.getOriginalFilename());
			}
			else {
				contact.setImage(contactOld.getImage());
			}
			User user=this.userRepository.getUserByEmail( principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated","success"));
		} catch (Exception e) { 
			// TODO: handle exception
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model,Principal principal) {
		String email = principal.getName();
		User user = this.userRepository.getUserByEmail(email);
		model.addAttribute("user",user);
		return "normal/profile";
	}
	
	// settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return"normal/settings";
	}
	//change password handler
	@PostMapping("/change_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		  
		String email = principal.getName();
		User user =this.userRepository.getUserByEmail(email);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword()))
		{
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your password is changed successfully !", "success"));
		}
		else {
			//error..
			session.setAttribute("message", new Message("Please enter correct old password ! !", "alert"));
		}
		return "redirect:/user/index";
	}
	
	
}
