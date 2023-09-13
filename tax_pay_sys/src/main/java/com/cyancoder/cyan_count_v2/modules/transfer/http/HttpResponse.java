package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.http;

import java.util.Map;

public class HttpResponse {

    private Map<String, String> headers;
    private String body;
    private int status;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
