package com.cyancoder.taxpaysys.modules.tax_api.service;


import com.cyancoder.taxpaysys.modules.content.api.DefaultTaxApiClient;
import com.cyancoder.taxpaysys.modules.content.api.TaxApi;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceBodyDto;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceDto;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceHeaderDto;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.auth.Token;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.AuthService;
import com.cyancoder.taxpaysys.modules.tax_api.client.out_api.service.ServerInformationService;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service.CompanyClientService;
import com.cyancoder.taxpaysys.modules.tax_api.client.services_api.service.FactorClientService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.FactorTaxEntity;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.PayState;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.CompanyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.FactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.RequestFactorModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.BodyItems;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.HeaderItems;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.InvoiceRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoPubKeyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.repository.FactorTaxRepository;
import com.cyancoder.taxpaysys.modules.transfer.api.ObjectTransferApiImpl;
import com.cyancoder.taxpaysys.modules.transfer.api.TransferApi;
import com.cyancoder.taxpaysys.modules.transfer.config.ApiConfig;
import com.cyancoder.taxpaysys.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.taxpaysys.modules.transfer.impl.encrypter.DefaultEncrypter;
import com.cyancoder.taxpaysys.modules.transfer.impl.signatory.InMemorySignatory;
import com.cyancoder.taxpaysys.util.Encrypt;
import com.cyancoder.taxpaysys.util.KeyUtil;
import com.cyancoder.taxpaysys.util.TaxUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FactorService {

    private final FactorTaxRepository factorTaxRepository;

    private final FactorClientService factorClientService;
    private final CompanyClientService companyClientService;

    private final AuthService authService;
    private final ServerInformationService serverInformationService;

    private TaxApi taxApi;

    public Object getFactorsToSubmit(String uniqueCode, String basedOn, String codeFrom, String codeTo,
                                     String fromDateInput, String toDateInput, String factorId,
                                     String companyId) throws Exception {


        CompanyModel companyModel = companyClientService.getCompany(companyId, uniqueCode);
        setData(uniqueCode, companyModel);


        List<FactorModel> factorModelList = new ArrayList<>();
        RequestFactorModel requestFactorModel = new RequestFactorModel(companyId);
//        if (basedOn.equals("factor_code")) {
            requestFactorModel.setCodeFrom(codeFrom);
            requestFactorModel.setCodeTo(codeTo);
//        } else {
            requestFactorModel.setFromDate(fromDateInput);
            requestFactorModel.setToDate(toDateInput);
//        }
        requestFactorModel.setFactorId(factorId);
        factorModelList = factorClientService.getFactors(requestFactorModel);


        log.info("factorModelList : {}", factorModelList);

        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorModelList.forEach(factorModel -> {

//            Long factorSerial = (10000_00000*factor.getSeller().getId())+(Long.valueOf(factor.getCode()));
            Long factorSerial = Long.valueOf(factorModel.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(), uniqueCode)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factorModel.getCreatedAt().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(factorModel.getState().trim().equals("type2") ? 2 : 1); // نوع صورتحساب
            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
//            header.setIrtaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(String.valueOf(companyModel.getNationalCode())); // شماره اقتصادی فروشنده


            //******** buyer **********//
            if (!factorModel.getState().trim().equals("type2")){
                header.setTob(factorModel.getBuyer().getBuyerType().trim().equals("legal") ? 2 : 1);  // نوع شخص خریدار
                header.setBid(String.valueOf(factorModel.getBuyer().getNationalCode()));  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
                header.setTinb(String.valueOf(factorModel.getBuyer().getNationalCode()));  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
                header.setBpc(factorModel.getBuyer().getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار
            }


            header.setTprdis(BigDecimal.ZERO);  // مجموع مبلغ قبل کسر تخفیف
            header.setTdis(BigDecimal.ZERO); // مجموع تخفیفات
            header.setTadis(BigDecimal.ZERO); //  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(BigDecimal.ZERO); //  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(BigDecimal.ZERO);  // مجموع


            List<InvoiceBodyDto> bodyList = new ArrayList<>();
            factorModel.getItems().forEach(factorItemModel -> {
                InvoiceBodyDto body = new InvoiceBodyDto();
                body.setSstid(factorItemModel.getProduct() != null ? factorItemModel.getProduct().getCode() : ""); // شناسه کالا
                body.setSstt(factorItemModel.getProduct().getName()); // شرح کالا
                body.setMu(factorItemModel.getProduct().getUnit().getCode()); // واحد اندازه گیری - کیلو گرم
                body.setAm(factorItemModel.getAmount()); // مقدار
                body.setFee(BigDecimal.valueOf(factorItemModel.getPrice())); // مبلع واحد
                body.setPrdis(BigDecimal.valueOf(Math.round(factorItemModel.getAmount()*factorItemModel.getPrice()))); // need to consider ************ // مبلغ قبل تخفیف
                body.setDis(factorItemModel.getDiscount() == null ? BigDecimal.ZERO : BigDecimal.valueOf(factorItemModel.getDiscount())); // مبلغ تخفیف
                body.setAdis(BigDecimal.valueOf(Math.round(body.getPrdis().doubleValue()-factorItemModel.getDiscount()))); // need to consider ************ مبلغ بعد تخفیف
                body.setVra(BigDecimal.valueOf(Math.round(factorItemModel.getTax()*100))); //نرخ مالیات بر ارزش افزوده ***************** /////////////////
                body.setVam(BigDecimal.valueOf(Math.floor(factorItemModel.getTax()*body.getAdis().doubleValue()))); // مبلع مالیات بر ارزش افزوده
                body.setTsstam(BigDecimal.valueOf(Math.round(body.getAdis().doubleValue()+body.getVam().doubleValue()))); // مبلغ کل
                bodyList.add(body);

                header.setTprdis(BigDecimal.valueOf(Math.round(header.getTprdis().doubleValue())+(Math.round(body.getPrdis().doubleValue()))));// need to consider ************ مجموع مبلغ قبل کسر تخفیف
                header.setTdis(BigDecimal.valueOf(Math.round(header.getTdis().doubleValue())+(Math.round(factorItemModel.getDiscount()))));// مجموع تخفیفات
                header.setTadis(BigDecimal.valueOf(Math.round(header.getTprdis().doubleValue()- header.getTdis().doubleValue()))); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
                header.setTvam(BigDecimal.valueOf(Math.round(header.getTvam().doubleValue()+body.getVam().doubleValue())));// need to consider ************  مجموع مالیات
                header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
                header.setTbill(BigDecimal.valueOf(Math.round(header.getTadis().doubleValue()+header.getTvam().doubleValue())));// need to consider ************ مجموع صورت حساب
            });


            header.setSetm(factorModel.getPayType() == "credit" ? 2 : 1); // روش تسویه
            header.setCap(factorModel.getPayType() == "credit" ? BigDecimal.ZERO : header.getTbill()); // مبلع پرداختی نقدی
            header.setInsp(factorModel.getPayType() == "credit" ? header.getTbill() : BigDecimal.ZERO); // مبلع پرداختی نسیه

            header.setTvop(header.getTvam()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷


            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setBody(bodyList);
            invoiceDto.setHeader(header);

            invoiceList.add(invoiceDto);
        });

        log.info("invoiceList: {}", invoiceList);

        AsyncResponseModel responseModel =
                taxApi.sendInvoices(invoiceList);

        log.info("responseModel: {}", responseModel);


        int length = 0;
        if (responseModel != null && responseModel.getResult() != null) {
            length = responseModel.getResult().size();
            IntStream.range(0, length).forEach(i -> {

                String FactorCode = invoiceList.get(i).getHeader().getInno();
                requestFactorModel.setCodeFrom(FactorCode);
                requestFactorModel.setCodeTo(FactorCode);
                List<FactorModel> factorModels = factorClientService.getFactors(requestFactorModel);
                FactorTaxEntity factor = new FactorTaxEntity();//////////////////////factors.get(0);

                factor.setFactorTaxId(UUID.randomUUID().toString());
                factor.setFactorId(factorModels.get(i).getFactorId());
                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
                factor.setTaxApiReference(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState("Sent");
                factor.setTaxApiMessage(null);
                factor.setTaxApiData(invoiceList.get(i).toString());

                log.info("factor-u: {}", factor);

                factorTaxRepository.save(factor);
            });
        }

        return responseModel;
    }


    private void setData(String uniqueCode, CompanyModel companyModel) throws Exception {


        String companyId = companyModel.getCompanyId();

        if (!companyModel.getUniqueCode().equals(Encrypt.hash(uniqueCode))){
            log.warn("companyModel.getUniqueCode() : {}",companyModel.getUniqueCode());
            log.warn("uniqueCode : {}",uniqueCode);
            log.warn("Encrypt.hash(uniqueCode) : {}",Encrypt.hash(uniqueCode));
            throw new Exception("uniqueCode or companyId is not corrected!");

        }

        if (companyModel.getPk() == null)
            throw new Exception("pKey is not corrected!");


        String privateKey = Encrypt.decrypt(companyModel.getPk(), uniqueCode);


        Random rnd = new Random();
        Header headerHttp = new Header("2023-cyanbusiness-fin-2320011" + rnd.nextInt(10));
//        if (headerHttp.getString("Authorization") == "" || authService.getSeller() != seller)
        authService.setTokenInHeader(headerHttp, uniqueCode, privateKey);

        HeaderItems headerItems = HeaderItems.builder().build();
        BodyItems bodyItems = BodyItems.builder().build();
        InvoiceRequestDataModel data = new InvoiceRequestDataModel(headerItems, bodyItems);

        ServerInfoResponseModel response = serverInformationService.getServerInformation(privateKey);
        ServerInfoPubKeyModel serverKey = response.successResponse != null ? response.successResponse.result.data.publicKeys[0] : null;

        InvoiceRequestModel bodyHttp = new InvoiceRequestModel(headerHttp, "INVOICE.V01", data, serverKey, privateKey);

        ApiConfig apiConfig = new ApiConfig()
                .baseUrl("https://tp.tax.gov.ir/req/api/self-tsp")
                .encrypter(new DefaultEncrypter(serverKey.key != null ? serverKey.key : KeyUtil.getOrgPublicKey(), serverKey.id != null ? serverKey.id : KeyUtil.getOrgKeyId()))
//                .signatory(new InMemorySignatory(KeyUtil.getStringPrivateKey("cyan"), null));
                .signatory(new InMemorySignatory(privateKey, null));
        TransferApi transferApi = new ObjectTransferApiImpl(apiConfig);
        this.taxApi = new DefaultTaxApiClient(transferApi, uniqueCode);


        this.taxApi.setAuthInfo(Token.getInstance().getToken());

    }


    private String getTaxId(Long factorId, Instant time, String uniqueCode) {
        String taxId = TaxUtils.generateTaxId(uniqueCode, factorId, time);
        return taxId;
    }

//
//    public Object factorCorrection(String uniqueCode, Long factorId,
//                                   String companyId) throws Exception {
//
////        SellerUser sellerEnm = SellerUser.getSellerById(seller);
//        setData(uniqueCode, companyId);
//
//        List<FactorModel> factorModelList;
//        factorModelList =
//                factorClientService.getFactors(factorId.toString())
//                        .stream()
//                        .filter(i -> i.getId() > 276225)
//                        .filter(i -> i.getStatus().toString() != "removed")
//                        .filter(i -> i.getSeller().getId() == seller)
//                        .toList();
//
//        List<InvoiceDto> invoiceList = new ArrayList<>();
//
//        factorModelList.forEach(factorModel -> {
//
//            Long factorSerial = Long.valueOf(factorModel.getCode());
//
//            InvoiceHeaderDto header = new InvoiceHeaderDto();
//            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(), sellerEnm)); // شماره منحصر به فرد مالیاتی
//            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
//            header.setIndati2m(factorModel.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
//            header.setInty(factorModel.getState().trim().equals("type2") ? 2 : 1); // نوع صورتحساب
//            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
//            header.setIrtaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(), sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
//            header.setInp(1); // الگوی صورتحساب
//            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
////            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
//            header.setTins(factorModel.getSeller().getNationalCode().replace("-", "")); // شماره اقتصادی فروشنده
//
//            header.setTob(factorModel.getPerson().toString().trim().equals("legal") ? 2 : 1);  // نوع شخص خریدار
//            header.setBid(factorModel.getNationalCode());  // شناسه ملی خریدار
////            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
//            header.setTinb(factorModel.getNationalCode());  //  شماره اقتصادی خریدار
////            header.setSbc(null);  //    کد شعبه فروشنده
//            header.setBpc(factorModel.getPostCode());  //  کدپستی خریدار
////            header.setBbc(null);  //    کد شعبه خریدار
//
//
//            header.setTprdis(factorModel.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
//            header.setTdis(factorModel.getDiscount() == null ? BigDecimal.ZERO : factorModel.getDiscount()); // مجموع تخفیفات
//            header.setTadis(factorModel.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
//            header.setTvam(factorModel.getTax()); // need to consider ************  مجموع مالیات
//            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
//            header.setTbill(factorModel.getPricePlusTax());  // need to consider ************ مجموع
//
//            header.setSetm(factorModel.getPayState() == PayState.credit ? 2 : 1); // روش تسویه
//            header.setCap(factorModel.getPayState() == PayState.credit ? BigDecimal.ZERO : factorModel.getPricePlusTax()); // مبلع پرداختی نقدی
//            header.setInsp(factorModel.getPayState() == PayState.credit ? factorModel.getPricePlusTax() : BigDecimal.ZERO); // مبلع پرداختی نسیه
//
//
//            header.setTvop(factorModel.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
////            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
//            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷
//
//
//            InvoiceBodyDto body = new InvoiceBodyDto();
////            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
//            body.setSstid(factorModel.getProduct() != null ? factorModel.getProduct().getCode() : ""); // شناسه کالا
//            body.setSstt(factorModel.getProductName()); // شرح کالا
//            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
//            body.setAm(factorModel.getWeight().doubleValue()); // مقدار
//            body.setFee(factorModel.getUnitPrice()); // مبلع واحد
//            body.setPrdis(factorModel.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
//            body.setDis(factorModel.getDiscount() == null ? BigDecimal.ZERO : factorModel.getDiscount()); // مبلغ تخفیف
//            body.setAdis(factorModel.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
//            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
//            body.setVam(factorModel.getTax()); // مبلع مالیات بر ارزش افزوده
//
//            body.setTsstam(factorModel.getPricePlusTax()); // مبلغ کل
//
//            InvoiceDto invoiceDto = new InvoiceDto();
//            invoiceDto.setBody(Collections.singletonList(body));
//            invoiceDto.setHeader(header);
//
//            invoiceList.add(invoiceDto);
//        });
//
//        AsyncResponseModel responseModel =
//                taxApi.sendInvoices(invoiceList);
//
//
//        log.info("responseModel: {}", responseModel);
//
//        int length = 0;
//        if (responseModel.getResult() != null) {
//            length = responseModel.getResult().size();
//            IntStream.range(0, length).forEach(i -> {
//
//                String FactorCode = invoiceList.get(i).getHeader().getInno();
//                List<FactorModel> factorModels = factorClientService.getFactors(FactorCode)
//                        .stream()
//                        .filter(item -> item.getStatus().toString() != "removed")
//                        .toList();
//                FactorTaxEntity factor = new FactorTaxEntity();//////////factors.get(0);
//
//                log.info("factor: {}", factor);
//
//                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
////                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
//                factor.setTaxApiState("2"); // corrected
//                factor.setTaxApiMessage("درخواست اصلاح فاکتور صادر شده است");
//
//                log.info("factor-u: {}", factor);
//
//                FactorTaxEntity result = factorTaxRepository.save(factor);
//
////                log.warn(result.getCode());
//
//            });
//        }
//
//
//        return responseModel;
//
//    }
//
//
//    public Object factorCancellation(String uniqueCode, Long factorId,
//                                     String companyId) throws Exception {
//
////        SellerUser sellerEnm = SellerUser.getSellerById(seller);
//        setData(uniqueCode, companyId);
//
//        List<FactorModel> factorModelList;
//        factorModelList =
//                factorClientService.getFactors(factorId.toString())
//                        .stream()
//                        .filter(i -> i.getId() > 276225)
//                        .filter(i -> i.getStatus().toString() != "removed")
//                        .filter(i -> i.getSeller().getId() == seller)
//                        .toList();
//
//        List<InvoiceDto> invoiceList = new ArrayList<>();
//
//        factorModelList.forEach(factorModel -> {
//
//            Long factorSerial = Long.valueOf(factorModel.getCode());
//
//            InvoiceHeaderDto header = new InvoiceHeaderDto();
//            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(), sellerEnm)); // شماره منحصر به فرد مالیاتی
//            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
//            header.setIndati2m(factorModel.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
//            header.setInty(factorModel.getState().trim().equals("type2") ? 2 : 1); // نوع صورتحساب
//            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
//            header.setIrtaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(), sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
//            header.setInp(1); // الگوی صورتحساب
//            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
////            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
//            header.setTins(factorModel.getSeller().getNationalCode().replace("-", "")); // شماره اقتصادی فروشنده
//
//            header.setTob(factorModel.getPerson().toString().trim().equals("legal") ? 2 : 1);  // نوع شخص خریدار
//            header.setBid(factorModel.getNationalCode());  // شناسه ملی خریدار
////            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
//            header.setTinb(factorModel.getNationalCode());  //  شماره اقتصادی خریدار
////            header.setSbc(null);  //    کد شعبه فروشنده
//            header.setBpc(factorModel.getPostCode());  //  کدپستی خریدار
////            header.setBbc(null);  //    کد شعبه خریدار
//
//
//            header.setTprdis(factorModel.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
//            header.setTdis(factorModel.getDiscount() == null ? BigDecimal.ZERO : factorModel.getDiscount()); // مجموع تخفیفات
//            header.setTadis(factorModel.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
//            header.setTvam(factorModel.getTax()); // need to consider ************  مجموع مالیات
//            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
//            header.setTbill(factorModel.getPricePlusTax());  // need to consider ************ مجموع
//
//            header.setSetm(factorModel.getPayState() == PayState.credit ? 2 : 1); // روش تسویه
//            header.setCap(factorModel.getPayState() == PayState.credit ? BigDecimal.ZERO : factorModel.getPricePlusTax()); // مبلع پرداختی نقدی
//            header.setInsp(factorModel.getPayState() == PayState.credit ? factorModel.getPricePlusTax() : BigDecimal.ZERO); // مبلع پرداختی نسیه
//
//
//            header.setTvop(factorModel.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
////            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
//            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷
////
////
//            InvoiceBodyDto body = new InvoiceBodyDto();
////            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
//            body.setSstid(factorModel.getProduct() != null ? factorModel.getProduct().getCode() : ""); // شناسه کالا
//            body.setSstt(factorModel.getProductName()); // شرح کالا
//            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
//            body.setAm(factorModel.getWeight().doubleValue()); // مقدار
//            body.setFee(factorModel.getUnitPrice()); // مبلع واحد
//            body.setPrdis(factorModel.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
//            body.setDis(factorModel.getDiscount() == null ? BigDecimal.ZERO : factorModel.getDiscount()); // مبلغ تخفیف
//            body.setAdis(factorModel.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
//            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
//            body.setVam(factorModel.getTax()); // مبلع مالیات بر ارزش افزوده
//
//            body.setTsstam(factorModel.getPricePlusTax()); // مبلغ کل
//
//            InvoiceDto invoiceDto = new InvoiceDto();
//            invoiceDto.setBody(Collections.singletonList(body));
//            invoiceDto.setHeader(header);
//
//            invoiceList.add(invoiceDto);
//        });
//
////        return factorList;
//
//        AsyncResponseModel responseModel =
//                taxApi.sendInvoices(invoiceList);
//
//
//        log.info("responseModel: {}", responseModel);
//
//        int length = 0;
//        if (responseModel.getResult() != null) {
//            length = responseModel.getResult().size();
//            IntStream.range(0, length).forEach(i -> {
//
//                String FactorCode = invoiceList.get(i).getHeader().getInno();
//                List<FactorModel> factorModels = factorClientService.getFactors(FactorCode)
//                        .stream()
//                        .filter(item -> item.getStatus().toString() != "removed")
//                        .toList();
//                FactorTaxEntity factor = new FactorTaxEntity();/////////factors.get(0);
//
//                log.info("factor: {}", factor);
//
//                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
////                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
//                factor.setTaxApiState("3"); // cancelled
//                factor.setTaxApiMessage("درخواست ابطال فاکتور صادر شده است");
//
//                log.info("factor-u: {}", factor);
//
//                FactorTaxEntity result = factorTaxRepository.save(factor);
//
////                log.warn(result.getCode());
//
//            });
//        }
//
//
//        return responseModel;
//
//    }


}
