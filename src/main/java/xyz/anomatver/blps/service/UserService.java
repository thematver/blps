package xyz.anomatver.blps.service;

import xyz.anomatver.blps.domain.User;

public interface UserService {

    User findByUsername(String username);
    User findById(Long id);

    User createUser(String username, String password);
}

