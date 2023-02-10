package org.hung.pojo;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "account")
public class Account {
    
    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    private double balance;
}
