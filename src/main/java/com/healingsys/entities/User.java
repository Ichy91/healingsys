package com.healingsys.entities;

import com.healingsys.entities.enums.BloodType;
import com.healingsys.entities.enums.Role;
import com.healingsys.entities.enums.UserStatus;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue
    @GenericGenerator(name = "postgres-uuid", strategy = "uuid")
    private UUID id;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp updatedDate;

    // personal info
    @NotNull
    private String name;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    private String SocialSecurityNumber;

    @NotNull
    private String IDNumber;

    @NotNull
    private String phoneNumber;

    @Email
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    // Health information
    private String weight;
    private String height;

    @Enumerated(EnumType.STRING)
    private BloodType bloodType;
}
