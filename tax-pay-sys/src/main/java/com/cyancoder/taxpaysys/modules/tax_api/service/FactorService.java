package com.cyancoder.taxpaysys.modules.tax_api.service;


import com.cyancoder.taxpaysys.modules.content.api.DefaultTaxApiClient;
import com.cyancoder.taxpaysys.modules.content.api.TaxApi;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceBodyDto;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceDto;
import com.cyancoder.taxpaysys.modules.content.dto.InvoiceHeaderDto;
import com.cyancoder.taxpaysys.modules.tax_api.client.auth.Token;
import com.cyancoder.taxpaysys.modules.tax_api.client.service.AuthService;
import com.cyancoder.taxpaysys.modules.tax_api.client.service.ServerInformationService;
import com.cyancoder.taxpaysys.modules.tax_api.entity.factor.Factor;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.PayState;
import com.cyancoder.taxpaysys.modules.tax_api.entity.general.SellerUser;
import com.cyancoder.taxpaysys.modules.tax_api.model.Header;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.BodyItems;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.HeaderItems;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.InvoiceRequestDataModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.req.invoice.InvoiceRequestModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoPubKeyModel;
import com.cyancoder.taxpaysys.modules.tax_api.model.dto.res.server_info.ServerInfoResponseModel;
import com.cyancoder.taxpaysys.modules.tax_api.repository.FactorRepository;
import com.cyancoder.taxpaysys.modules.transfer.api.ObjectTransferApiImpl;
import com.cyancoder.taxpaysys.modules.transfer.api.TransferApi;
import com.cyancoder.taxpaysys.modules.transfer.config.ApiConfig;
import com.cyancoder.taxpaysys.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.taxpaysys.modules.transfer.impl.encrypter.DefaultEncrypter;
import com.cyancoder.taxpaysys.modules.transfer.impl.signatory.InMemorySignatory;
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

    private final FactorRepository factorRepository;

    private final AuthService authService;
    private final ServerInformationService serverInformationService;

    private TaxApi taxApi;

    public Object getFactorsToSubmit(String basedOn, String codeFrom, String codeTo,
                                     String fromDateInput,String toDateInput,
                                     int seller) throws Exception {



        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(sellerEnm);


        List<Factor> factorList;
        if (basedOn.equals("factor_code"))
            factorList =
                    factorRepository.findByCodeBetween(codeFrom,codeTo)
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

            factorList =
                    factorRepository.findByCreatedOnBetween(fromDate,toDate)
                            .stream()
                            .filter(i->i.getId()>276225)
                            .filter(i->i.getStatus().toString()!= "removed")
                            .filter(i->i.getSeller().getId()== seller)
//                            .filter(i-> Long.parseLong(i.getCode()) >= (Long.parseLong(codeFrom)))
//                            .filter(i-> Long.parseLong(i.getCode()) <= (Long.parseLong(codeTo)))
                            .toList();
        }



        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorList.forEach(factor -> {

//            Long factorSerial = (10000_00000*factor.getSeller().getId())+(Long.valueOf(factor.getCode()));
            Long factorSerial = Long.valueOf(factor.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factor.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factor.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factor.getCode());  //  سریال صورتحساب   ****************
//            header.setIrtaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factor.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factor.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factor.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factor.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factor.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factor.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مجموع تخفیفات
            header.setTadis(factor.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factor.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factor.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factor.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factor.getPayState()==PayState.credit?BigDecimal.ZERO:factor.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factor.getPayState()==PayState.credit?factor.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factor.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷


            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factor.getProduct()!=null?factor.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factor.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factor.getWeight().doubleValue()); // مقدار
            body.setFee(factor.getUnitPrice()); // مبلع واحد
            body.setPrdis(factor.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مبلغ تخفیف
            body.setAdis(factor.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
            body.setVam(factor.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factor.getPricePlusTax()); // مبلغ کل

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
                List<Factor> factors = factorRepository.findByCode(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                Factor factor = factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState(1);
                factor.setTaxApiMessage(null);

                log.info("factor-u: {}", factor);

                Factor result = factorRepository.save(factor);

                log.warn(result.getCode());

            });
        }


        return responseModel;

    }



    private void setData(SellerUser seller) throws Exception {

        Random rnd = new Random();
        Header headerHttp = new Header("2023-cyanbusiness-fin-2320011"+ rnd.nextInt(10));
        if (headerHttp.getString("Authorization") == "" || authService.getSeller() != seller)
            authService.setTokenInHeader(headerHttp, seller);

        HeaderItems headerItems = HeaderItems.builder().build();
        BodyItems bodyItems = BodyItems.builder().build();
        InvoiceRequestDataModel data = new InvoiceRequestDataModel(headerItems,bodyItems);

        ServerInfoResponseModel response = serverInformationService.getServerInformation();
        ServerInfoPubKeyModel serverKey = response.successResponse != null ? response.successResponse.result.data.publicKeys[0] : null;

        InvoiceRequestModel bodyHttp = new InvoiceRequestModel(headerHttp, "INVOICE.V01", data, serverKey,seller);

        ApiConfig apiConfig = new ApiConfig()
                .baseUrl("https://tp.tax.gov.ir/req/api/self-tsp")
                .encrypter(new DefaultEncrypter(serverKey.key!= null?serverKey.key: KeyUtil.getOrgPublicKey(), serverKey.id!=null?serverKey.id:KeyUtil.getOrgKeyId()))
//                .signatory(new InMemorySignatory(KeyUtil.getStringPrivateKey("cyan"), null));
                .signatory(new InMemorySignatory(KeyUtil.getStringPrivateKey(seller), null));
        TransferApi transferApi = new ObjectTransferApiImpl(apiConfig);
        this.taxApi = new DefaultTaxApiClient(transferApi, KeyUtil.getClientId(seller));


        this.taxApi.setAuthInfo(Token.getInstance().getToken());

    }


    private String getTaxId(Long factorId, Instant time,SellerUser seller){
        String taxId = TaxUtils.generateTaxId(KeyUtil.getClientId(seller), factorId, time);
        return taxId;
    }



    public Object factorCorrection(Long factorId,
                                     int seller) throws Exception {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(sellerEnm);

        List<Factor> factorList;
        factorList =
                factorRepository.findById(factorId)
                        .stream()
                        .filter(i->i.getId()>276225)
                        .filter(i->i.getStatus().toString()!= "removed")
                        .filter(i->i.getSeller().getId()== seller)
                        .toList();

        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorList.forEach(factor -> {

            Long factorSerial = Long.valueOf(factor.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factor.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factor.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factor.getCode());  //  سریال صورتحساب   ****************
            header.setIrtaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factor.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factor.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factor.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factor.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factor.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factor.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مجموع تخفیفات
            header.setTadis(factor.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factor.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factor.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factor.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factor.getPayState()==PayState.credit?BigDecimal.ZERO:factor.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factor.getPayState()==PayState.credit?factor.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factor.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷


            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factor.getProduct()!=null?factor.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factor.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factor.getWeight().doubleValue()); // مقدار
            body.setFee(factor.getUnitPrice()); // مبلع واحد
            body.setPrdis(factor.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مبلغ تخفیف
            body.setAdis(factor.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
            body.setVam(factor.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factor.getPricePlusTax()); // مبلغ کل

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
                List<Factor> factors = factorRepository.findByCode(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                Factor factor = factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState(2); // corrected
                factor.setTaxApiMessage("درخواست اصلاح فاکتور صادر شده است");

                log.info("factor-u: {}", factor);

                Factor result = factorRepository.save(factor);

                log.warn(result.getCode());

            });
        }


        return responseModel;

    }


    public Object factorCancellation(Long factorId,
                                     int seller) throws Exception {

        SellerUser sellerEnm = SellerUser.getSellerById(seller);
        setData(sellerEnm);

        List<Factor> factorList;
        factorList =
                factorRepository.findById(factorId)
                        .stream()
                        .filter(i->i.getId()>276225)
                        .filter(i->i.getStatus().toString()!= "removed")
                        .filter(i->i.getSeller().getId()== seller)
                        .toList();

        List<InvoiceDto> invoiceList = new ArrayList<>();

        factorList.forEach(factor -> {

            Long factorSerial = Long.valueOf(factor.getCode());

            InvoiceHeaderDto header = new InvoiceHeaderDto();
            header.setTaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی
            header.setIndatim(factor.getFactorDate().toInstant().toEpochMilli()); // تاریخ و زمان صدور
            header.setIndati2m(factor.getCreatedOn().toInstant().toEpochMilli()); //تاریخ و زمان ایجاد
            header.setInty(1); // نوع صورتحساب
            header.setInno(factor.getCode());  //  سریال صورتحساب   ****************
            header.setIrtaxid(getTaxId(factorSerial,factor.getFactorDate().toInstant(),sellerEnm)); // شماره منحصر به فرد مالیاتی صورتحساب مرجع
            header.setInp(1); // الگوی صورتحساب
            header.setIns(1); // موضوع صورتحساب ++++++++++++++++++
//            header.setTins(factor.getSeller().getEconomicCode().replace("-","")); // شماره اقتصادی فروشنده
            header.setTins(factor.getSeller().getNationalCode().replace("-","")); // شماره اقتصادی فروشنده

            header.setTob(factor.getPerson().toString().trim().equals("legal")?2:1);  // نوع شخص خریدار
            header.setBid(factor.getNationalCode());  // شناسه ملی خریدار
//            header.setTinb(factor.getEconomicCode());  //  شماره اقتصادی خریدار
            header.setTinb(factor.getNationalCode());  //  شماره اقتصادی خریدار
//            header.setSbc(null);  //    کد شعبه فروشنده
            header.setBpc(factor.getPostCode());  //  کدپستی خریدار
//            header.setBbc(null);  //    کد شعبه خریدار


            header.setTprdis(factor.getFinalPrice());  // need to consider ************ مجموع مبلغ قبل کسر تخفیف
            header.setTdis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مجموع تخفیفات
            header.setTadis(factor.getFinalPrice()); // need to consider ************  مجموع مبلغ بعد کسر تخفیف
            header.setTvam(factor.getTax()); // need to consider ************  مجموع مالیات
            header.setTodam(BigDecimal.ZERO); // مجوع سایر عوارض
            header.setTbill(factor.getPricePlusTax());  // need to consider ************ مجموع

            header.setSetm(factor.getPayState()==PayState.credit?2:1); // روش تسویه
            header.setCap(factor.getPayState()==PayState.credit?BigDecimal.ZERO:factor.getPricePlusTax()); // مبلع پرداختی نقدی
            header.setInsp(factor.getPayState()==PayState.credit?factor.getPricePlusTax():BigDecimal.ZERO); // مبلع پرداختی نسیه


            header.setTvop(factor.getTax()); // need to consider ************ مجموع سهم مالیات بر ارزش افزوده
//            header.setDpvb(1); //عدم پرداخت مالیات بر ارزش افزوده خریدار  need to consider ***********
            header.setTax17(BigDecimal.ZERO); //   مالیات موضوع ماده ۱۷
//
//
            InvoiceBodyDto body = new InvoiceBodyDto();
//            body.setSstid(factor.getProductType()!=null?factor.getProductType().getCode():null); // شناسه کالا
            body.setSstid(factor.getProduct()!=null?factor.getProduct().getCode():""); // شناسه کالا
            body.setSstt(factor.getProductName()); // شرح کالا
            body.setMu("164"); // واحد اندازه گیری - کیلو گرم
            body.setAm(factor.getWeight().doubleValue()); // مقدار
            body.setFee(factor.getUnitPrice()); // مبلع واحد
            body.setPrdis(factor.getFinalPrice()); // need to consider ************ // مبلغ قبل تخفیف
            body.setDis(factor.getDiscount()==null?BigDecimal.ZERO:factor.getDiscount()); // مبلغ تخفیف
            body.setAdis(factor.getFinalPrice()); // need to consider ************ مبلغ بعد تخفیف
            body.setVra(BigDecimal.ZERO); // نرم مالیات بر ارزش افزوده *****************
            body.setVam(factor.getTax()); // مبلع مالیات بر ارزش افزوده

            body.setTsstam(factor.getPricePlusTax()); // مبلغ کل

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
                List<Factor> factors = factorRepository.findByCode(FactorCode.toString())
                        .stream()
                        .filter(item->item.getStatus().toString()!= "removed")
                        .toList();
                Factor factor = factors.get(0);

                log.info("factor: {}", factor);

                factor.setTaxApiUid(responseModel.getResult().get(i).getUid());
                factor.setTaxApiRefrence(responseModel.getResult().get(i).getReferenceNumber());
                factor.setTaxApiState(3); // cancelled
                factor.setTaxApiMessage("درخواست ابطال فاکتور صادر شده است");

                log.info("factor-u: {}", factor);

                Factor result = factorRepository.save(factor);

                log.warn(result.getCode());

            });
        }


        return responseModel;

    }



}
