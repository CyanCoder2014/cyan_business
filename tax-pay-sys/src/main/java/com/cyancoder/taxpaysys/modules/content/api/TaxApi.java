package com.cyancoder.taxpaysys.modules.content.api;

import com.cyancoder.taxpaysys.modules.content.dto.*;
import com.cyancoder.taxpaysys.modules.transfer.dto.AsyncResponseModel;
import com.cyancoder.taxpaysys.modules.transfer.exception.TaxApiException;

import java.util.List;

//@Component
public interface TaxApi {

    AsyncResponseModel sendInvoices(List<InvoiceDto> invoices) throws TaxApiException;

    TokenModel requestToken() throws TaxApiException;

    ServerInformationModel getServerInformation() throws TaxApiException;

    @Deprecated
    List<InquiryResultModel> inquiryByUidAndFiscalId(List<UidAndFiscalId> uidAndFiscalIds) throws TaxApiException;

    List<InquiryResultModel> inquiryByTime(String persianTime) throws TaxApiException;

    List<InquiryResultModel> inquiryByTimeRange(String startDatePersian, String toDatePersian) throws TaxApiException;

    List<InquiryResultModel> inquiryByReferenceId(List<String> referenceIds) throws TaxApiException;

    FiscalInformationModel getFiscalInformation(String fiscalId) throws TaxApiException;

    SearchResultModel<ServiceStuffModel> getServiceStuffList(SearchDto searchDTO) throws TaxApiException;

    EconomicCodeModel getEconomicCodeInformation(String economicCode) throws TaxApiException;

    void setAuthInfo(String token);
}
