
package com.project.BookCarOnline.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.project.BookCarOnline.Entity.Enum.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.UUID;

import java.util.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, unique = true, length = 36)
     String accountId;

    @Column(nullable = false, unique = true, length = 100)
     String userName;

    @JsonIgnore
    @Column(nullable = false, length = 100)
     String passWord;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
     Role roleNo;


    @Column
     Date createdAt = new Date();

    @Column
     Boolean accountStatus;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(length = 100)
    Provider provider = Provider.LOCAL;

    @Column(length = 255)
     String providerId;

}
