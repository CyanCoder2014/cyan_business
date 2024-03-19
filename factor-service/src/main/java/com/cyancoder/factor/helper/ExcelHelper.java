package com.cyancoder.factor.helper;

import com.cyancoder.factor.model.BuyerModel;
import com.cyancoder.factor.model.request.CreateFactorReqModel;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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

            while (rows.hasNext()) {
                Row currentRow = rows.next();
                // skip header
                if (rowNumber < 1) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cellsInRow = currentRow.iterator();

//                if (currentRow.getCell(14) == null) {
//                    rowNumber++;
//                    continue;
//                }
//                if (currentRow.getCell(14).getCellType() != CellType.STRING) {
//                    ((XSSFCell) currentRow.getCell(14)).setCellType(CellType.STRING);
//                }
//
//                String num = currentRow.getCell(14).getStringCellValue();
//                if(num.equals("جمع") || num.equals("")) {
//                    rowNumber++;
//                    continue;
//                }

                T model = (T) new CreateFactorReqModel();
                BuyerModel buyer = new BuyerModel();

                int cellIdx = 0;
                while (cellsInRow.hasNext()) {
                    Cell currentCell = cellsInRow.next();

                    log.info("start while at index: {}", cellIdx);
//                    log.info("start while at currentCell: {}", currentCell.getStringCellValue());

                    switch (cellIdx) {
                        case 0:
                            ((XSSFCell) currentCell).setCellType(CellType.STRING);
//                            model.setCompanyId(currentCell.getStringCellValue().replaceAll(",", ""));
                            model.setCompanyId(currentCell.getStringCellValue());
                            break;

                        case 1:
                            if (!currentCell.getStringCellValue().equals("")) {
                                model.setCode(currentCell.getStringCellValue());
                            }
                            break;

                        case  2:
                            if (!currentCell.getStringCellValue().equals("")) {
                                model.setFactorDate(currentCell.getStringCellValue());////////////
                            }
                            break;

                        case 3:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                buyer.setNationalCode(Long.parseLong(String.valueOf(currentCell.getNumericCellValue())));/////////
                                model.setBuyer(buyer);
                            }
                            break;

                        case 4:
                            buyer.setBuyerType((String.valueOf(currentCell.getNumericCellValue())));/////////
                            model.setBuyer(buyer);
                            break;

                        case 5:
//                            buyer = model.getBuyer();//////////////////
                            buyer.setPostCode((String.valueOf(currentCell.getNumericCellValue())));/////////
                            model.setBuyer(buyer);
                            break;

                        case 6:
//                            model.setItems(currentCell.getStringCellValue());
                            break;

                        case 7:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                model.setNote(String.valueOf(currentCell.getNumericCellValue()));
                            } else {
                                model.setNote(currentCell.getStringCellValue());
                            }
                            break;

                        case 8:
                            if (currentCell.getCellType() == CellType.NUMERIC) {
                                model.setNote(String.valueOf(currentCell.getNumericCellValue()));
                            } else {
                                model.setNote(currentCell.getStringCellValue());
                            }
                            break;

                        case 9:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        case 10:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        case 11:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        case 12:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        case 13:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        case 14:
                            model.setNote(currentCell.getStringCellValue());
                            break;

                        default:
                            break;
                    }


                    cellIdx++;
                }

                models.add(model);
            }

            workbook.close();

            return models;
        } catch (IOException e) {
            throw new RuntimeException("fail to parse Excel file: " + e.getMessage());
        }
    }
}
