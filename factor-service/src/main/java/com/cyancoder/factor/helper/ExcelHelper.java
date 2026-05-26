package com.cyancoder.factor.helper;

import com.cyancoder.factor.model.*;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
public class ExcelHelper<T extends CreateFactorReqModel> {

    T dataModel;
    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    static String[] HEADERs = { "Id", "Title", "Description", "Published" };
    static String SHEET = "models";


    public ExcelHelper(CreateFactorReqModel model){
        this.dataModel = (T) model;
    }


    public static boolean hasExcelFormat(MultipartFile file) {

        if (!TYPE.equals(file.getContentType())) {
            return false;
        }

        return true;
    }

    public List<T> excelToModel(InputStream is) {
        try {
            Workbook workbook = new XSSFWorkbook(is);
//            Sheet sheet = workbook.getSheet(SHEET);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            List<T> models = new ArrayList<T>();

            log.info("get sheet: {}", workbook);


            int rowNumber = 0;

            List<Map<String, Object>> resultList = new ArrayList<>();

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber < 1) {
                    rowNumber++;
                    continue;
                }

                Map<String, Object> record = new HashMap<>();
                Map<String, Object> buyer = new HashMap<>();
                Map<String, Object> item = new HashMap<>();
                Map<String, Object> product = new HashMap<>();
                Map<String, Object> unit = new HashMap<>();
                List<Map<String, Object>> items = new ArrayList<>();

                Iterator<Cell> cellsInRow = currentRow.iterator();
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();
                    int cellIdx = currentCell.getColumnIndex();
                    switch (cellIdx) {
                        case 0:
                            ((XSSFCell) currentCell).setCellType(CellType.STRING);
                            record.put("companyId", currentCell.getStringCellValue());
                            break;
                        case 1:
                            ((XSSFCell) currentCell).setCellType(CellType.STRING);
                            record.put("code", currentCell.getStringCellValue());
                            break;
                        case 2:
                            // Assuming the cell contains a date in the format "yyyy-MM-dd"
                            Date currentDate = currentCell.getDateCellValue();

                            if (currentDate != null) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                                String dateStr = dateFormat.format(currentDate);

                                record.put("factorDate", dateStr + "T00:00:00Z[UTC]");
                            }

                            break;
                        case 3:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                buyer.put("nationalCode", (long) currentCell.getNumericCellValue());
                            }
                            break;
                        case 4:
                            buyer.put("buyerType", currentCell.getStringCellValue());
                            break;
                        case 5:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                buyer.put("postCode", (String.valueOf(currentCell.getNumericCellValue())));
                            } else {
                                buyer.put("postCode", currentCell.getStringCellValue());
                            }
                            break;
                        case 6:
                            product.put("code", currentCell.getNumericCellValue());
                            break;
                        case 7:
                            product.put("name", currentCell.getStringCellValue());
                            break;
                        case 8:
                            unit.put("id", currentCell.getNumericCellValue());
                            break;
                        case 9:
                            unit.put("name", currentCell.getStringCellValue());
                            break;
                        case 10:
                            item.put("amount", currentCell.getNumericCellValue());
                            break;
                        case 11:
                            item.put("price", currentCell.getNumericCellValue());
                            break;
                        case 12:
                            item.put("discount", currentCell.getNumericCellValue());
                            break;
                        case 13:
                            item.put("tax", currentCell.getNumericCellValue());
                            break;
                        case 14:
                            item.put("otherCharges", currentCell.getNumericCellValue());
                            break;
                        case 15:
                            record.put("payType", currentCell.getStringCellValue());
                            break;
                        case 16:
                            record.put("payed", currentCell.getNumericCellValue());
                            break;
                        // Add other cases as needed
                    }
                }
                product.put("unit", unit);
                item.put("product", product);
                items.add(item);

                record.put("buyer", buyer);
                record.put("items", items);

                resultList.add(record);
            }

            // Assuming 'resultList' is the list of maps you obtained from the previous step
            Map<String, Map<String, Object>> mergedByCode = resultList.stream()
                    .filter(r -> r.get("code") != null)
                    .collect(Collectors.toMap(
                            r -> (String) r.get("code"), // Key Mapper
                            r -> new HashMap<>(r), // Value Mapper
                            (map1, map2) -> {
                                // Merge function: merge items lists and other values
                                List<Map<String, Object>> itemsList1 = (List<Map<String, Object>>) map1.get("items");
                                List<Map<String, Object>> itemsList2 = (List<Map<String, Object>>) map2.get("items");
                                List<Map<String, Object>> mergedItemsList = new ArrayList<>(itemsList1);
                                mergedItemsList.addAll(itemsList2);
                                map1.put("items", mergedItemsList); // Put merged items list back into the map
                                return map1;
                            }
                    ));


            // Now 'groupedByCode' contains a map where the key is the 'code', and the value is a list of items with that code.

            workbook.close();

            List<CreateFactorReqModel> factorReqModels = mergedByCode.entrySet().stream()
                    .map(entry -> {
                        Map<String, Object> mergedMap = entry.getValue(); // Now you get a single map per code

                        CreateFactorReqModel factorReqModel = new CreateFactorReqModel();
                        List<FactorItemModel> factorItems = new ArrayList<>();

                        // Convert the merged items to FactorItemModels
                        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) mergedMap.get("items");
                        for (Map<String, Object> itemMap : itemsList) {
                            FactorItemModel factorItem = mapToFactorItemModel(itemMap);
                            factorItems.add(factorItem);
                        }

                        // Set the details from the merged map
                        factorReqModel.setCode((String) mergedMap.get("code"));
                        factorReqModel.setCompanyId((String) mergedMap.get("companyId"));
                        factorReqModel.setBuyer(mapToBuyerModel((Map<String, Object>) mergedMap.get("buyer")));
                        factorReqModel.setFactorDate((String) mergedMap.get("factorDate"));
                        factorReqModel.setPayType((String) mergedMap.get("payType"));
                        factorReqModel.setPayed((Double) mergedMap.get("payed"));
                        // Set other properties as needed

                        factorReqModel.setItems(factorItems);
                        return factorReqModel;
                    })
                    .collect(Collectors.toList());

            return factorReqModels.stream().map(model -> (T) model)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }

    // Helper method to convert a map to FactorItemModel
    private FactorItemModel mapToFactorItemModel(Map<String, Object> itemMap) {
        FactorItemModel factorItemModel = new FactorItemModel();
        // For 'factor', you need to convert the associated map or id to a FactorModel, similarly to how you did with BuyerModel
        // factorItemModel.setFactor(convertToFactorModel(itemMap.get("factor")));

        // For 'product', you need to convert the associated map to a ProductModel
        // Assuming 'productMap' is a map representation of the product
        Map<String, Object> productMap = (Map<String, Object>) itemMap.get("product");
        ProductModel productModel = mapToProductModel(productMap);
        factorItemModel.setProduct(productModel);

        factorItemModel.setAmount((Double) itemMap.get("amount"));
        factorItemModel.setPrice((Double) itemMap.get("price"));
        factorItemModel.setDiscount((Double) itemMap.get("discount"));
        factorItemModel.setTax((Double) itemMap.get("tax"));
        factorItemModel.setOther_charge((Double) itemMap.get("otherCharges"));
        return factorItemModel;
    }


    // Helper method to convert a map to BuyerModel
    private BuyerModel mapToBuyerModel(Map<String, Object> buyerMap) {
        BuyerModel buyerModel = new BuyerModel();
        // Assuming buyerMap contains the keys used in your original map
        buyerModel.setNationalCode((Long) buyerMap.get("nationalCode"));
        buyerModel.setBuyerType((String) buyerMap.get("buyerType"));
        buyerModel.setPostCode((String) buyerMap.get("postCode"));
        return buyerModel;
    }

    // Helper method to convert a map to ProductModel
    private ProductModel mapToProductModel(Map<String, Object> productMap) {
        ProductModel productModel = new ProductModel();

        // Map the basic properties from productMap to productModel
        productModel.setCode(productMap.get("code").toString());
        productModel.setName((String) productMap.get("name"));

        // For 'unit', you need to convert the associated map to a UnitModel
        // Assuming 'unitMap' is a map representation of the unit
        Map<String, Object> unitMap = (Map<String, Object>) productMap.get("unit");
        UnitModel unitModel = mapToUnitModel(unitMap);
        productModel.setUnit(unitModel);

        return productModel;
    }

    // Helper method to convert a map to UnitModel
    private UnitModel mapToUnitModel(Map<String, Object> unitMap) {
        UnitModel unitModel = new UnitModel();

        unitModel.setUnitId(unitMap.get("id").toString());
        unitModel.setName((String) unitMap.get("name"));

        return unitModel;
    }

    // Helper method to convert a map to ProductTypeModel
    private ProductTypeModel mapToProductTypeModel(Map<String, Object> productTypeMap) {
        ProductTypeModel productTypeModel = new ProductTypeModel();

        productTypeModel.setProductTypeId((String) productTypeMap.get("productTypeId"));
        productTypeModel.setCode((String) productTypeMap.get("code"));
        productTypeModel.setName((String) productTypeMap.get("name"));
        productTypeModel.setNote((String) productTypeMap.get("note"));
        productTypeModel.setState((String) productTypeMap.get("state"));

        return productTypeModel;
    }
}
