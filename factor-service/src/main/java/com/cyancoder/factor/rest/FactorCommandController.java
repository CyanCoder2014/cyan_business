package com.cyancoder.factor.rest;


import com.cyancoder.factor.command.CreateFactorCommand;
import com.cyancoder.factor.entity.FactorEntity;
import com.cyancoder.factor.entity.FactorItemEntity;
import com.cyancoder.factor.entity.ProductEntity;
import com.cyancoder.factor.entity.UnitEntity;
import com.cyancoder.factor.helper.ExcelHelper;
import com.cyancoder.factor.model.FactorItemModel;
import com.cyancoder.factor.model.FactorModel;
import com.cyancoder.factor.model.ProductModel;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import com.cyancoder.factor.model.request.UpdateFactorReqModel;
import com.cyancoder.factor.repository.FactorItemRepository;
import com.cyancoder.factor.repository.FactorRepository;
import com.cyancoder.factor.repository.ProductRepository;
import com.cyancoder.factor.repository.UnitRepository;
import com.cyancoder.factor.service.ExcelService;
import com.cyancoder.factor.service.FactorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/v2/api/factor-service/factors")
@RequiredArgsConstructor
@Slf4j
public class FactorCommandController {
    private  final Environment env;
    private  final CommandGateway commandGateway;

    private final FactorService factorService;
    private final ExcelService excelService;

    @PostMapping
    public FactorEntity createFactor(@RequestBody CreateFactorReqModel createFactorReqModel) throws ParseException {
        return factorService.addFactor(createFactorReqModel);
    }

    @PutMapping
    public FactorEntity createFactor(@RequestBody UpdateFactorReqModel updateFactorReqModel) throws Exception {
        return factorService.editFactor(updateFactorReqModel);
    }

    @DeleteMapping
    public Object createFactor(@RequestParam String factorId) throws Exception {
        return factorService.removeFactor(factorId);
    }


    @PostMapping("/import-excel")
    public Object createFactorsWithExcel(@RequestParam("file") MultipartFile file) {


        Object res = null;

        if (ExcelHelper.hasExcelFormat(file)) {
            try {
                List<CreateFactorReqModel> result = excelService.importExcel(file);

//                res = "Uploaded the file successfully: " + file.getOriginalFilename();

//                Object response_dto_list = result.stream().map(FactorModel::new).collect(Collectors.toList());

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(result);
            } catch (Exception e) {
                res = "Could not upload the file: " + file.getOriginalFilename() + "!";
                return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(res);
            }
        }

        res = "Please upload an excel file!";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);

    }


}
