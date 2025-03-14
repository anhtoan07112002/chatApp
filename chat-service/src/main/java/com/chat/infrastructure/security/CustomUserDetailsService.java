package com.chat.infrastructure.security;

import com.chat.domain.entity.user.User;
import com.chat.domain.service.userservice.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final IUserService userService;
//
//    @Autowired
//    public CustomUserDetailsService(@Lazy IUserService userService) {
//        this.userService = userService;
//    }
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        try {
//            User user = userService.getUserByEmail(email);
//            log.debug("Loading user details for email: {}", email);
//
//            var authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
//
//            var userDetails = org.springframework.security.core.userdetails.User
//                    .withUsername(user.getEmail())
//                    .password(user.getPassword())
//                    .authorities(authorities)
//                    .build();
//
//            log.debug("User details loaded with authorities: {}", authorities);
//            return userDetails;
//        } catch (Exception e) {
//            log.error("Error loading user by email: {}", email, e);
//            throw new UsernameNotFoundException("User not found with email: " + email);
//        }
//    }
//}
public class CustomUserDetailsService implements UserDetailsService {

    private final IUserService userService;

    @Autowired
    public CustomUserDetailsService(@Lazy IUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            User user = userService.getUserByEmail(email);
            log.debug("Loading user details for email: {}", email);

            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .authorities(Collections.emptyList())
                    .build();
        } catch (Exception e) {
            log.error("Error loading user by email: {}", email, e);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
    }
}