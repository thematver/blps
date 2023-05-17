package xyz.anomatver.blps.domain;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column()
    @JsonProperty("role")
    private String role;

    @Column(unique = true)
    @JsonProperty("username")
    private String username;

    @JsonProperty("email")
    @Column(name = "email")
    private String email;

    @JsonProperty("password")
    @Column(name = "password")
    private String password;


    @OneToMany
    private List<Review> postedReviews;


    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User() {

    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }





    public void setRole(String role) {
        this.role = role;
    }
}