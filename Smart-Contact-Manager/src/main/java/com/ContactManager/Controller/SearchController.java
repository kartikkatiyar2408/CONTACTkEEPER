package com.ContactManager.Controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import com.ContactManager.Dao.ContactRepository;
import com.ContactManager.Dao.UserRepository;
import com.ContactManager.Entities.User;
import com.ContactManager.Entities.Contact;

@RestController
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @GetMapping("/search/{query}")
    public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal){

        System.out.println(query);

        User user = this.userRepository.getUserByUserName(principal.getName());

        List<Contact> contacts = this.contactRepository.findByNameContainingAndUser(query, user);

        return ResponseEntity.ok(contacts);
    }
}
