package com.ContactManager.Controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.*;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.ContactManager.Dao.ContactRepository;
import com.ContactManager.Dao.MyOrderRepository;
import com.ContactManager.Dao.UserRepository;
import com.ContactManager.Entities.Contact;
import com.ContactManager.Entities.MyOrder;
import com.ContactManager.Entities.User;
import com.ContactManager.Helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private MyOrderRepository myOrderRepository;

    @InitBinder("contact")
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("image");
    }

    // this runs everytime to add common data to response
    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {

        String userName = principal.getName();
        // System.out.println(userName);

        User user = userRepository.getUserByUserName(userName);

        // System.out.println(user);
        model.addAttribute("user", user);

    }

    @RequestMapping("/index")
    public String dashboard(Model model, Principal principal) {

        model.addAttribute("title", "User DashBoard");

        return "normal/userDashboard";
    }

    // open add form handler
    @GetMapping("/addContact")
    public String openAddContactForm(Model model) {

        model.addAttribute("title", "Contact Page");
        model.addAttribute("contact", new Contact());
        System.out.println("Inside user controller");
        return "normal/addContactForm";
    }

    @PostMapping("/processContact")
    public String processContact(@ModelAttribute Contact contact, @RequestParam("image") MultipartFile file,
            Principal principal, HttpSession session) {

        try {
            String name = principal.getName();
            User user = this.userRepository.getUserByUserName(name);

            if (file.isEmpty()) {
                contact.setImage("default.png");
                session.setAttribute("message", new Message("Your contact is added!! Add more..", "success"));
                System.out.println("It is empty");
            } else {
                contact.setImage(file.getOriginalFilename());

                File file1 = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("File is uploaded");
                session.setAttribute("message", new Message("Your contact is added!! Add more..", "success"));

            }

            user.getContacts().add(contact);

            contact.setUser(user);

            userRepository.save(user);

            System.out.println("Added to database");

            // System.out.println(contact);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("message", new Message("Something went wrong !!", "danger"));
        }

        return "normal/addContactForm";
    }

    @GetMapping("/showContacts/{page}")
    public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
        model.addAttribute("title", "Show Contacts");

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

        model.addAttribute("contacts", contacts);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", contacts.getTotalPages());

        return "normal/showContacts";
    }

    @GetMapping("/{cId}/contact")
    public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {

        System.out.println("CID : " + cId);

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);

        Contact contact = contactOptional.get();

        String userName = principal.getName();
        User user = this.userRepository.getUserByUserName(userName);

        if (user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }

        return "normal/showContactDetails";
    }

    @GetMapping("/deleteContact/{cId}")
    public String deleteContact(@PathVariable("cId") int cId, Model model, HttpSession session, Principal principal) {

        Optional<Contact> contactOptional = this.contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        User user = this.userRepository.getUserByUserName(principal.getName());
        user.getContacts().remove(contact);
        this.userRepository.save(user);

        session.setAttribute("message", new Message("Contact deleted successfully !!", "success"));

        return "redirect:/user/showContacts/0";
    }

    @PostMapping("/updateContact/{cId}")
    public String updateForm(@PathVariable("cId") Integer cId, Model model) {
        model.addAttribute("title", "Update Contact");

        Contact contact = this.contactRepository.findById(cId).get();

        model.addAttribute("contact", contact);

        return "normal/updateContact";
    }

    @PostMapping("/processUpdate")
    public String processUpdate(@ModelAttribute Contact contact, Model model, @RequestParam("image") MultipartFile file,
            HttpSession session, Principal principal) {
        System.out.println(contact.getcId());
        try {

            Contact oldContact = this.contactRepository.findById(contact.getcId()).get();

            if (!file.isEmpty()) {

                // deleting old
                File deleteFile = new ClassPathResource("static/img").getFile();
                File file2 = new File(deleteFile, oldContact.getImage());
                file2.delete();

                // adding new
                contact.setImage(file.getOriginalFilename());

                File file1 = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(file1.getAbsolutePath() + File.separator + file.getOriginalFilename());
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());

                System.out.println("File is uploaded");
                session.setAttribute("message", new Message("Your contact is updated !!", "success"));

            } else {
                contact.setImage(oldContact.getImage());
            }

            User user = this.userRepository.getUserByUserName(principal.getName());

            contact.setUser(user);
            this.contactRepository.save(contact);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/user/" + contact.getcId() + "/contact";
    }

    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Your Profile");

        return "normal/profile";
    }

    @GetMapping("/openSettings")
    public String openSettings(){
        return "normal/settings";
    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession session){
        
        String userName = principal.getName();
        User currUser = this.userRepository.getUserByUserName(userName);

        if(this.bCryptPasswordEncoder.matches(oldPassword, currUser.getPassword())){

            currUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
            this.userRepository.save(currUser);

            session.setAttribute("message",new Message("Password changed successfully..","success"));

        }
        else{
            session.setAttribute("message",new Message("Please enter correct old password","danger"));

            return "redirect:/user/openSettings";
        }

        return "redirect:/user/index";
    }

    @ResponseBody
    @PostMapping("/createOrder")
    public String createOrder(@RequestBody Map<String, Object> data,Principal principal) throws Exception{
        System.out.println("Kya dekh raha hai");
        System.out.println(data);

        int amount = Integer.parseInt(data.get("amount").toString());

        var razorpayClient = new RazorpayClient("rzp_test_5ac7GHpxal0BcP","qGoVsLoy0FuLQi7Pef5h28D4");

        JSONObject ob = new JSONObject();
        ob.put("amount", amount*100);
        ob.put("currency","INR");
        ob.put("receipt","txn-51324");

        //creating order

        Order order = razorpayClient.orders.create(ob);
        System.out.println(order);

        //saving in database
        MyOrder myOrder = new MyOrder();
        myOrder.setAmount(order.get("amount")+"");
        myOrder.setOrderId(order.get("id"));
        myOrder.setPaymentId(null);
        myOrder.setStatus("created");
        myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
        myOrder.setReciept(order.get("receipt"));

        this.myOrderRepository.save(myOrder);

        return order.toString();
    }

    @PostMapping("/updateOrder")
    public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){

        MyOrder myOrder = this.myOrderRepository.findByOrderId(data.get("orderId").toString());
        myOrder.setPaymentId(data.get("paymentId").toString());
        myOrder.setStatus(data.get("status").toString());

        this.myOrderRepository.save(myOrder);

        return ResponseEntity.ok(Map.of("msg","updated"));
    }

}
