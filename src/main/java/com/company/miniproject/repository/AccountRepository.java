package com.company.miniproject.repository;

import com.company.miniproject.entity.Account;
import com.company.miniproject.entity.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    
    @Query("SELECT a FROM Account a LEFT JOIN FETCH a.roles WHERE a.username = :username")
    Optional<Account> findByUsername(@Param("username") String username);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT COUNT(a) > 0 FROM Account a WHERE LOWER(a.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
    
    @Query("SELECT a FROM Account a WHERE LOWER(a.email) = LOWER(:email)")
    Optional<Account> findByEmailIgnoreCase(@Param("email") String email);
    
    @Query("SELECT DISTINCT a FROM Account a JOIN a.roles r WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:roleId IS NULL OR r.id = :roleId) AND " +
           "(:status IS NULL OR a.status = :status)")
    Page<Account> searchAccounts(@Param("keyword") String keyword,
                                @Param("roleId") Integer roleId,
                                @Param("status") AccountStatus status,
                                Pageable pageable);
    
    @Query("SELECT COUNT(a) FROM Account a JOIN a.roles r WHERE r.name IN ('EMPLOYEE', 'MANAGER') AND a.status = :status")
    long countEmployeesAndManagersByStatus(@Param("status") AccountStatus status);
}
