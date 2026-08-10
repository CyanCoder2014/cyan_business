package com.cyancoder.report.model;
import java.time.Instant;
import java.util.List;
import java.util.Map;
public final class ReportContracts {private ReportContracts(){}
 public record SaveReportRequest(String reportKey,String title,String serviceKey,String entityKey,String defaultFilterField,String defaultSumField,String groupByField,List<ReportFilter> filters){}
 public record ReportSummary(String reportKey,String title,String serviceKey,String entityKey,String defaultSumField,String groupByField,Instant updatedAt){}
 public record ReportRunView(String runId,String reportKey,String status,Map<String,Object> request,ReportRunResponse result,String errorCode,String errorMessage,Instant createdAt,Instant startedAt,Instant completedAt){}
 public record ReportPage(List<ReportSummary> items,int page,int size,long total){}
 public record ReportRunPage(List<ReportRunView> items,int page,int size,long total){}
 public record CreateReportExportRequest(String format){}
 public record ReportExportView(String exportId,String reportKey,String runId,String format,String status,String assetKey,String deliveryUrl,String errorCode,String errorMessage,Instant createdAt,Instant completedAt){}
}
