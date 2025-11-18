package com.tlcn.sportsnet_backend.util;

import com.tlcn.sportsnet_backend.entity.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Permission;
import java.util.Collection;

import java.util.List;

public class AccountDetails implements UserDetails {

    private final Account account;

    public AccountDetails(Account account) {
        this.account = account;
    }

    public String getId() {
        return account.getId();
    }

    public Account getAccount() {
        return account;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return account.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .toList();
    }


    @Override
    public String getPassword() {
        return account.getPassword();
    }

    @Override
    public String getUsername() {
        return account.getEmail();
    }

    @Override
    public boolean isEnabled() {
        return account.isVerified() && account.isEnabled(); // ← dùng để kiểm tra có được phép login hay không
    }

    @Override
    public boolean isAccountNonLocked() {
        return account.isEnabled(); // ← hoặc return true nếu không quan tâm tới locked
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}