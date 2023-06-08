package xyz.anomatver.blps.auth.model;

import lombok.*;


import javax.persistence.*;

@Setter
@Getter
@Entity
@Builder
@Table(name = "roles")
@AllArgsConstructor
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 60)
    private String name;
}