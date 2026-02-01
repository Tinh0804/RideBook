
package com.project.BookCarOnline.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.util.Set;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table(name = "VAITRO")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID_VAITRO", nullable = false, unique = true, length = 36)
    private String roleId;

    @Column(name = "TENVAITRO",columnDefinition = "NVARCHAR(255)")
    private String roleName;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "roleNo")
    @JsonIgnore
    private Set<Account> accounts;

}
