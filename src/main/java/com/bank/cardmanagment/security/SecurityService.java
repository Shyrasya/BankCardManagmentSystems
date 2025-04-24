package com.bank.cardmanagment.security;

//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//
//@Component
//public class SecurityService {
//    public boolean hasRole(String role) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null) {
//            for (GrantedAuthority authority : authentication.getAuthorities()) {
//                if (authority.getAuthority().equals("ROLE_" + role)) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//}
