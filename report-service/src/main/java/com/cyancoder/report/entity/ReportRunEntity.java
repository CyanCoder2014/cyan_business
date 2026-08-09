package com.cyancoder.report.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="report_run", uniqueConstraints=@UniqueConstraint(name="uk_report_run_scope_idempotency", columnNames={"tenant_key","idempotency_key"}), indexes={@Index(name="idx_report_run_scope_report", columnList="tenant_key,site_key,report_key,created_at")})
public class ReportRunEntity {
 @Id @Column(name="run_id",length=64) private String runId;
 @Column(name="tenant_key",nullable=false,length=100) private String tenantKey;
 @Column(name="site_key",length=100) private String siteKey;
 @Column(name="report_key",nullable=false,length=160) private String reportKey;
 @Column(name="status",nullable=false,length=24) private String status;
 @Column(name="actor",nullable=false,length=160) private String actor;
 @Column(name="idempotency_key",nullable=false,length=160) private String idempotencyKey;
 @Lob @Column(name="request_json") private String requestJson;
 @Lob @Column(name="result_json") private String resultJson;
 @Column(name="error_code",length=80) private String errorCode;
 @Lob @Column(name="error_message") private String errorMessage;
 @Column(name="created_at",nullable=false) private Instant createdAt;
 @Column(name="started_at") private Instant startedAt;
 @Column(name="completed_at") private Instant completedAt;
 public String getRunId(){return runId;} public void setRunId(String v){runId=v;} public String getTenantKey(){return tenantKey;} public void setTenantKey(String v){tenantKey=v;} public String getSiteKey(){return siteKey;} public void setSiteKey(String v){siteKey=v;} public String getReportKey(){return reportKey;} public void setReportKey(String v){reportKey=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public String getActor(){return actor;} public void setActor(String v){actor=v;} public String getIdempotencyKey(){return idempotencyKey;} public void setIdempotencyKey(String v){idempotencyKey=v;} public String getRequestJson(){return requestJson;} public void setRequestJson(String v){requestJson=v;} public String getResultJson(){return resultJson;} public void setResultJson(String v){resultJson=v;} public String getErrorCode(){return errorCode;} public void setErrorCode(String v){errorCode=v;} public String getErrorMessage(){return errorMessage;} public void setErrorMessage(String v){errorMessage=v;} public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;} public Instant getStartedAt(){return startedAt;} public void setStartedAt(Instant v){startedAt=v;} public Instant getCompletedAt(){return completedAt;} public void setCompletedAt(Instant v){completedAt=v;}
}
