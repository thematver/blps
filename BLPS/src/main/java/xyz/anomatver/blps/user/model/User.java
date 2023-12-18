package xyz.anomatver.blps.user.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import xyz.anomatver.blps.auth.model.ERole;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true)
    private String username;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @JsonIgnore
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private List<ERole> roles = new ArrayList<>();

    public boolean hasRole(ERole role) { return roles.contains(role); }


}