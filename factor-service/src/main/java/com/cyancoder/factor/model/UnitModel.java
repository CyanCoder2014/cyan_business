package com.cyancoder.factor.model;


import com.cyancoder.factor.entity.UnitEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnitModel {

    public UnitModel(UnitEntity unitEntity){
        BeanUtils.copyProperties(unitEntity, this);
    }

    private String unitId;
    private String code;
    private String name;
    private String state;

}
