package main.java.com.cyancoder.tax_pay_sys_service.modules.transfer.http;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface HttpRequestSender {

    HttpResponse sendPostRequest(@NotNull String url, Object requestBody, @Nullable Map<String, String> headers);

    HttpResponse sendGetRequest(@NotNull String url, @Nullable Map<String, String> headers);
}
