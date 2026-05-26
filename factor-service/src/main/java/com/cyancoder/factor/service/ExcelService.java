package com.cyancoder.factor.service;

import com.cyancoder.factor.helper.ExcelHelper;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExcelService {

    private final FactorService factorService;

    public List<CreateFactorReqModel> importExcel(MultipartFile file) {
        try {
            CreateFactorReqModel model = new CreateFactorReqModel();
            ExcelHelper excelHelper = new ExcelHelper(model);

            List<CreateFactorReqModel> data = excelHelper.excelToModel(file.getInputStream());

            // group factor item and add to item list of a factor .............

            data.forEach(item -> {
                try {
                    factorService.removeFactorByCode(item.getCode());
                    factorService.addFactor(item);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            });
            return data;
        } catch (IOException e) {
            throw new RuntimeException("fail to store excel data: " + e.getMessage());
        }
    }











}
