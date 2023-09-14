package com.cyancoder.tax_pay_sys_service.modules.tax_api.model;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class Header extends HashMap<String, Object> {


    public Header(String requestTraceId){

        long timestamp = System.currentTimeMillis();
        Map<String,Object> header = this;
//        header.put("Content-Type","application/json");
        header.put("requestTraceId",requestTraceId != null ? requestTraceId : "1231232");
        header.put("timestamp",new String(String.valueOf(timestamp)));
    }

    public String getString(Object key) {
        return (String) getOrDefault(key,"");
    }


    public String getContentType(){
        return "application/json";
    }
}
