package com.example.springbootapi.Service;

import com.example.springbootapi.Entity.Users;
import com.example.springbootapi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        // Tìm người dùng bằng userId
        Users users = userRepository.findById(Integer.parseInt(userId))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        return new User(users.getId().toString(), users.getPassword(),
                Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority(users.getRole().name())));
    }
}