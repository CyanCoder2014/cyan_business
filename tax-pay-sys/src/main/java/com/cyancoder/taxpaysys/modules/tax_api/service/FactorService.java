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
                                     String fromDateInput,String toDateInput,
                                     String companyId) throws Exception {



//        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(uniqueCode, companyId);


        List<FactorModel> factorModelList;
        if (basedOn.equals("factor_code"))
            factorModelList =
                    factorClientService.getFactors(codeFrom,codeTo)
                            .stream()
                            .filter(i->i.getId()>276225)
                            .filter(i->i.getStatus().toString()!= "removed")
                            .filter(i->i.getSeller().getId()== seller)
                            .toList();
        else{
            String fromDateStr = fromDateInput != "" ? fromDateInput : "2023-05-01";   // to consider
            SimpleDateFormat fromDateObj = new SimpleDateFormat("yyyy-MM-dd");
            Date fromDate = fromDateObj.parse(fromDateStr);

            String toDateStr = toDateInput != "" ? toDateInput : "2023-06-01";   // to consider
            SimpleDateFormat toDateObj = new SimpleDateFormat("yyyy-MM-dd");
            Date toDate = toDateObj.parse(toDateStr);

            factorModelList =
                    factorClientService.getFactors(fromDate,toDate)
                            .stream()
                            .filter(i->i.getId()>276225)
                            .filter(i->i.getStatus().toString()!= "removed")
                            .filter(i->i.getSeller().getId()== seller)
//                            .filter(i-> Long.parseLong(i.getCode()) >= (Long.parseLong(codeFrom)))
//                            .filter(i-> Long.parseLong(i.getCode()) <= (Long.parseLong(codeTo)))
                            .toList();
        }



        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorModelList.forEach(factorModel -> {

//            Long factorSerial = (10000_00000*factor.getSeller().getId())+(Long.valueOf(factor.getCode()));
            Long factorSerial = Long.valueOf(factorModel.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factorModel.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
//            header.setIrtaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factorModel.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factorModel.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factorModel.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factorModel.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factorModel.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factorModel.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مجموع تخفیفات
            header.setTadis(factorModel.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factorModel.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factorModel.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factorModel.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factorModel.getPayState()==PayState.credit?BigDecimal.ZERO: factorModel.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factorModel.getPayState()==PayState.credit? factorModel.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factorModel.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷


            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factorModel.getProduct()!=null? factorModel.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factorModel.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factorModel.getWeight().doubleValue()); // مقدار
            body.setFee(factorModel.getUnitPrice()); // مبلع واحد
            body.setPrdis(factorModel.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مبلغ تخفیف
            body.setAdis(factorModel.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); //نرخ مالیات بر ارزش افزوده ***************** /////////////////
            body.setVam(factorModel.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factorModel.getPricePlusTax()); // مبلغ کل

            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setBody(Collections.singletonList(body));
            invoiceDto.setHeader(header);

            invoiceList.add(invoiceDto);
        });

        AsyncResponseModel responseModel =
                taxApi.sendInvoices(invoiceList);



        log.info("responseModel: {}", responseModel);

        int length = 0;
        if (responseModel.getResult() != null){
            length = responseModel.getResult().size();
            IntStream.range(0, length).forEach(i-> {

                String FactorCode = invoiceList.get(i).getHeader().getInno();
                List<FactorModel> factorModels = factorClientService.getFactors(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                FactorTaxEntity factor = new FactorTaxEntity();//////////////////////factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
//                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState("1");
                factor.setTaxApiMessage(null);

                log.info("factor-u: {}", factor);

                FactorTaxEntity result = factorTaxRepository.save(factor);

//                log.warn(result.getCode());

            });
        }


        return responseModel;

    }



    private void setData(String uniqueCode, String companyId) throws Exception {


        CompanyModel companyModel = companyClientService.getCompany(companyId);


        if (companyModel.getUniqueCode() != Encrypt.hash(uniqueCode))
            throw new Exception("uniqueCode or companyId is not corrected!");

        if (companyModel.getPk() != null)
            throw new Exception("pKey is not corrected!");


        String privateKey = Encrypt.decrypt(companyModel.getPk(), uniqueCode);


        Random rnd = new Random();
        Header headerHttp = new Header("2023-cyanbusiness-fin-2320011"+ rnd.nextInt(10));
//        if (headerHttp.getString("Authorization") == "" || authService.getSeller() != seller)
            authService.setTokenInHeader(headerHttp, uniqueCode, privateKey);

        HeaderItems headerItems = HeaderItems.builder().build();
        BodyItems bodyItems = BodyItems.builder().build();
        InvoiceRequestDataModel data = new InvoiceRequestDataModel(headerItems,bodyItems);

        ServerInfoResponseModel response = serverInformationService.getServerInformation();
        ServerInfoPubKeyModel serverKey = response.successResponse != null ? response.successResponse.result.data.publicKeys[0] : null;

        InvoiceRequestModel bodyHttp = new InvoiceRequestModel(headerHttp, "INVOICE.V01", data, serverKey,privateKey);

        ApiConfig apiConfig = new ApiConfig()
                .baseUrl("https://tp.tax.gov.ir/req/api/self-tsp")
                .encrypter(new DefaultEncrypter(serverKey.key!= null?serverKey.key: KeyUtil.getOrgPublicKey(), serverKey.id!=null?serverKey.id:KeyUtil.getOrgKeyId()))
//                .signatory(new InMemorySignatory(KeyUtil.getStringPrivateKey("cyan"), null));
                .signatory(new InMemorySignatory(privateKey, null));
        TransferApi transferApi = new ObjectTransferApiImpl(apiConfig);
        this.taxApi = new DefaultTaxApiClient(transferApi, uniqueCode);


        this.taxApi.setAuthInfo(Token.getInstance().getToken());

    }


    private String getTaxId(Long factorId, Instant time,SellerUser seller){
        String taxId = TaxUtils.generateTaxId(KeyUtil.getClientId(seller), factorId, time);
        return taxId;
    }



    public Object factorCorrection(String uniqueCode, Long factorId,
                                     String companyId) throws Exception {

//        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(uniqueCode, companyId);

        List<FactorModel> factorModelList;
        factorModelList =
                factorClientService.getFactors(factorId.toString())
                        .stream()
                        .filter(i->i.getId()>276225)
                        .filter(i->i.getStatus().toString()!= "removed")
                        .filter(i->i.getSeller().getId()== seller)
                        .toList();

        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorModelList.forEach(factorModel -> {

            Long factorSerial = Long.valueOf(factorModel.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factorModel.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
            header.setIrtaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factorModel.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factorModel.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factorModel.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factorModel.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factorModel.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factorModel.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مجموع تخفیفات
            header.setTadis(factorModel.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factorModel.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factorModel.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factorModel.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factorModel.getPayState()==PayState.credit?BigDecimal.ZERO: factorModel.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factorModel.getPayState()==PayState.credit? factorModel.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factorModel.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷


            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factorModel.getProduct()!=null? factorModel.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factorModel.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factorModel.getWeight().doubleValue()); // مقدار
            body.setFee(factorModel.getUnitPrice()); // مبلع واحد
            body.setPrdis(factorModel.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مبلغ تخفیف
            body.setAdis(factorModel.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
            body.setVam(factorModel.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factorModel.getPricePlusTax()); // مبلغ کل

            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setBody(Collections.singletonList(body));
            invoiceDto.setHeader(header);

            invoiceList.add(invoiceDto);
        });

        AsyncResponseModel responseModel =
                taxApi.sendInvoices(invoiceList);



        log.info("responseModel: {}", responseModel);

        int length = 0;
        if (responseModel.getResult() != null){
            length = responseModel.getResult().size();
            IntStream.range(0, length).forEach(i-> {

                String FactorCode = invoiceList.get(i).getHeader().getInno();
                List<FactorModel> factorModels = factorClientService.getFactors(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                FactorTaxEntity factor = new FactorTaxEntity();//////////factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
//                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState("2"); // corrected
                factor.setTaxApiMessage("درخواست اصلاح فاکتور صادر شده است");

                log.info("factor-u: {}", factor);

                FactorTaxEntity result = factorTaxRepository.save(factor);

//                log.warn(result.getCode());

            });
        }


        return responseModel;

    }


    public Object factorCancellation(String uniqueCode, Long factorId,
                                     String companyId) throws Exception {

//        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(uniqueCode, companyId);

        List<FactorModel> factorModelList;
        factorModelList =
                factorClientService.getFactors(factorId.toString())
                        .stream()
                        .filter(i->i.getId()>276225)
                        .filter(i->i.getStatus().toString()!= "removed")
                        .filter(i->i.getSeller().getId()== seller)
                        .toList();

        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorModelList.forEach(factorModel -> {

            Long factorSerial = Long.valueOf(factorModel.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factorModel.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factorModel.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factorModel.getCode());  //  سریال صورتحساب   ****************
            header.setIrtaxid(getTaxId(factorSerial, factorModel.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factorModel.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factorModel.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factorModel.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factorModel.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factorModel.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factorModel.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مجموع تخفیفات
            header.setTadis(factorModel.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factorModel.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factorModel.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factorModel.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factorModel.getPayState()==PayState.credit?BigDecimal.ZERO: factorModel.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factorModel.getPayState()==PayState.credit? factorModel.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factorModel.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷
//
//
            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factorModel.getProduct()!=null? factorModel.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factorModel.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factorModel.getWeight().doubleValue()); // مقدار
            body.setFee(factorModel.getUnitPrice()); // مبلع واحد
            body.setPrdis(factorModel.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factorModel.getDiscount()==null?BigDecimal.ZERO: factorModel.getDiscount()); // مبلغ تخفیف
            body.setAdis(factorModel.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
            body.setVam(factorModel.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factorModel.getPricePlusTax()); // مبلغ کل

            InvoiceDto invoiceDto = new InvoiceDto();
            invoiceDto.setBody(Collections.singletonList(body));
            invoiceDto.setHeader(header);

            invoiceList.add(invoiceDto);
        });

//        return factorList;

        AsyncResponseModel responseModel =
                taxApi.sendInvoices(invoiceList);



        log.info("responseModel: {}", responseModel);

        int length = 0;
        if (responseModel.getResult() != null){
            length = responseModel.getResult().size();
            IntStream.range(0, length).forEach(i-> {

                String FactorCode = invoiceList.get(i).getHeader().getInno();
                List<FactorModel> factorModels = factorClientService.getFactors(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                FactorTaxEntity factor = new FactorTaxEntity();/////////factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
//                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState("3"); // cancelled
                factor.setTaxApiMessage("درخواست ابطال فاکتور صادر شده است");

                log.info("factor-u: {}", factor);

                FactorTaxEntity result = factorTaxRepository.save(factor);

//                log.warn(result.getCode());

            });
        }


        return responseModel;

    }



}
