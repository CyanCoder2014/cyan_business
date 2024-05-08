package com.cyancoder.taxpaysys.modules.tax_api.service;


import org.springframework.cache.annotation.Cacheable;

public class TokenService {



    @Cacheable(cacheManager = "cacheManager", cacheNames = "default")
    public String getToken(){




        return null;

    }



}
