package com.cyancoder.client.model;


import com.cyancoder.client.entity.BuyerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BuyerModel {

    private String buyerId;

    private Long nationalCode;
    private String economicCode;
    private String buyerType;
    private String tell;
    private String address;
    private String postCode;
    private String cityId;
    private String note;
    private boolean addNew;
    public BuyerModel(BuyerEntity buyerEntity) {
        BeanUtils.copyProperties(buyerEntity, this);
    }
}
