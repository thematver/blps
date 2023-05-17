package xyz.anomatver.blps.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import xyz.anomatver.blps.domain.User;
import xyz.anomatver.blps.domain.UserRole;
import xyz.anomatver.blps.exception.NotFoundException;
import xyz.anomatver.blps.repository.UserRepository;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;



    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + id));
    }

    @Override
    public User createUser(String username, String password) {
        User user = new User(username, password);
        return userRepository.save(user);
    }


}
