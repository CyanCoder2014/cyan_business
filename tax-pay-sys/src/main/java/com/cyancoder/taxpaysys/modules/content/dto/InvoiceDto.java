package com.cyancoder.taxpaysys.modules.content.dto;


import lombok.ToString;

import java.util.List;

@ToString
public class InvoiceDto {

    private InvoiceHeaderDto header;

    private List<InvoiceBodyDto> body;

    private List<PaymentDto> payments;

    private List<InvoiceExtension> extension;

    public InvoiceHeaderDto getHeader() {
        return header;
    }

    public void setHeader(InvoiceHeaderDto header) {
        this.header = header;
    }

    public List<InvoiceBodyDto> getBody() {
        return body;
    }

    public void setBody(List<InvoiceBodyDto> body) {
        this.body = body;
    }

    public List<PaymentDto> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentDto> payments) {
        this.payments = payments;
    }

    public List<InvoiceExtension> getExtension() {
        return extension;
    }

    public void setExtension(List<InvoiceExtension> extension) {
        this.extension = extension;
    }

}
