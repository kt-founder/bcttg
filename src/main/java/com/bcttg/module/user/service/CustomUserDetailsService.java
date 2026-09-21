package com.bcttg.module.user.service;

import java.util.List;

import com.bcttg.module.user.entity.UserAccount;
import com.bcttg.module.user.repository.UserAccountRepository;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByPhoneAndDeletedAtIsNull(phone)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!Boolean.TRUE.equals(account.getIsActive())) {
            throw new DisabledException("User is inactive");
        }
        return new User(account.getPhone(), account.getPasswordHash(),
            List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())));
    }
}
