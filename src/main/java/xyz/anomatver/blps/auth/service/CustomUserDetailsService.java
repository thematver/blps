package xyz.anomatver.blps.auth.service;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import xyz.anomatver.blps.user.model.User;
import xyz.anomatver.blps.user.repository.UserRepository;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    String filePath = "users.xml";
    File usersFile;
    ConcurrentHashMap<String, User> accounts;
    Lock fileLock;
    Logger logger = Logger.getGlobal();
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        Set<GrantedAuthority> authorities = user
                .getRoles()
                .stream()
                .map((role) -> new SimpleGrantedAuthority("ROLE_" + role.toString())).collect(Collectors.toSet());

        return new org.springframework.security.core.userdetails.User(user.getUsername(),
                user.getPassword(),
                authorities);
    }

    public User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetails userDetails) {
            String login = userDetails.getUsername();
            User user = userRepository.findByUsername(login).orElseThrow();
            return user;
        }
        return null;
    }

    @PostConstruct
    private void init() {
        this.accounts = new ConcurrentHashMap<>();
        this.fileLock = new ReentrantLock();
        initFile();
        loadFromXml();

    }

    private void initFile() {

        File file = new File(filePath);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        this.usersFile = file;

    }


    public void addAccount(User account) {
        accounts.put(account.getUsername(), account);
        dumpToXml();
    }

    public void addAccount(String username, String password) {
        accounts.put(username, User.builder().username(username).password(password).build());
        dumpToXml();
    }

    public User findAccount(String username) {
        return accounts.get(username);
    }

    private void dumpToXml() {
        try {
            fileLock.lock();
            XMLEncoder encoder = new XMLEncoder(
                    new BufferedOutputStream(
                            new FileOutputStream(usersFile)
                    )
            );
            encoder.writeObject(this.accounts);
            encoder.close();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        } finally {
            fileLock.unlock();
        }


    }

    private void loadFromXml() {
        try {
            XMLDecoder decoder = new XMLDecoder(
                    new BufferedInputStream(
                            new FileInputStream(usersFile)
                    )
            );
            try {
                this.accounts = (ConcurrentHashMap<String, User>) decoder.readObject();
            } catch (Exception ex) {
                logger.warning("Users file corrupted, skipping");
            }

            decoder.close();
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }


}