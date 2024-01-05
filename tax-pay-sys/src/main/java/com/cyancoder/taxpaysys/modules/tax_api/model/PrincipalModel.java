package com.cyancoder.taxpaysys.modules.tax_api.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class PrincipalModel {

//    public PrincipalModel(Object principal){
//
//
//    }

    private String tokenValue;
    private String principal;

//    public PrincipalModel(Object principal) {
//
//        if (principal instanceof UserDetails) {
//            String username = ((UserDetails)principal).getUsername();
//        } else {
//            String username = principal.toString();
//        }
//
//        tokenValue = principal.toString();
//    }
}
