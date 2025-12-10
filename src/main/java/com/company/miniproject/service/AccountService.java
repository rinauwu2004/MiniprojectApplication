package com.company.miniproject.service;

import com.company.miniproject.dto.AccountDto;
import com.company.miniproject.dto.ChangePasswordDto;
import com.company.miniproject.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface AccountService {
    
    Page<Account> findAll(Pageable pageable);
    
    List<Account> findAll();
    
    Page<Account> searchAccounts(String keyword, Integer roleId, com.company.miniproject.entity.AccountStatus status, Pageable pageable);
    
    Optional<Account> findById(Integer id);
    
    Account save(AccountDto dto);
    
    Account update(Integer id, AccountDto dto);
    
    void deleteById(Integer id);
    
    void changePassword(Integer accountId, ChangePasswordDto dto);
    
    void adminChangePassword(Integer accountId, com.company.miniproject.dto.AdminChangePasswordDto dto);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}

