package org.hung.repo;

import java.util.Optional;
import java.util.UUID;

import org.hung.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account,UUID> {
    
    public Optional<Account> findByName(String name);
    
}
