package com.cyancoder.product.model;


import com.cyancoder.product.entity.ClientEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientModel {

    private String clientId;


    public ClientModel(ClientEntity clientEntity) {
        BeanUtils.copyProperties(clientEntity, this);
    }
}
