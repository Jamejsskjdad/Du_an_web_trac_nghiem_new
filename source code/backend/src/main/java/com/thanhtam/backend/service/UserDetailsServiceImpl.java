package com.thanhtam.backend.service;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.thanhtam.backend.entity.User;
import com.thanhtam.backend.repository.UserRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));
        System.out.println("Đăng nhập với username: " + username);
        System.out.println("Password mã hóa trong DB: " + user.getPassword());
        // Nếu muốn kiểm tra roles, profile v.v cũng log luôn ở đây
        return UserDetailsImpl.build(user);
    }


}
