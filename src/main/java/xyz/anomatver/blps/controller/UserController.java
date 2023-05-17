package xyz.anomatver.blps.controller;

import xyz.anomatver.blps.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.service.ModeratorService;
import xyz.anomatver.blps.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ModeratorService moderatorService;


    @PostMapping("/permissions/{username}")
    public ResponseEntity<Boolean> perms(@PathVariable String username) {
        User user = userService.findByUsername(username);
        moderatorService.grantRole(user);
        return ResponseEntity.ok(true);
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestParam String username, @RequestParam String password) {
        try {
            if (userService.findByUsername(username) != null) {
                throw new NotFoundException("Не нашлось");
            }

            User user = userService.createUser(username, password);
            return ResponseEntity.ok(user);
        } catch (NotFoundException e){
            return ResponseEntity.ok("Такой уже есть");
        }
    }
}
