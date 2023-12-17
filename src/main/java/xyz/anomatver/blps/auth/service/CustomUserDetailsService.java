package xyz.anomatver.blps.auth.service;


import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;


    @Value("${users.file.path}")
    private String usersFilePath;
    File usersFile;
    ConcurrentHashMap<String, User> accounts;
    Lock fileLock;
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
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
        catch (UsernameNotFoundException ex) {
            logger.error("User not found with username: {}", username);
            throw ex;
        } catch (Exception e) {
            logger.error("An error occurred while loading user by username: {}", e.getMessage());
            throw e;
        }
    }

    public User getUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                String login = userDetails.getUsername();
                return userRepository.findByUsername(login).orElseThrow();
            }
            return null;
        }
        catch (Exception e) {
            logger.error("Error fetching user details: {}", e.getMessage());
        }
        return null;
    }

    @PostConstruct
    private void init() {
        try {
            this.accounts = new ConcurrentHashMap<>();
            this.fileLock = new ReentrantLock();
            initFile();
            loadFromXml();
        } catch (Exception e) {
            logger.error("Error occurred during initialization: {}", e.getMessage());
        }

    }

    private void initFile() {

        File file = new File(usersFilePath);
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
            logger.error("File not found while dumping to XML: {}", ex.getMessage());
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

            this.accounts = (ConcurrentHashMap<String, User>) decoder.readObject();


            decoder.close();
        } catch (FileNotFoundException ex) {
            logger.error("File not found while loading from XML: {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        catch (Exception e) {
            logger.error("Error occurred when loading from XML: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


}