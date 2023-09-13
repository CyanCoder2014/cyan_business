package main.java.com.cyancoder.tax_pay_sys_service.modules.home.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Table(name = "c_ticket")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class Ticket  {


    @jakarta.persistence.Id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long id;

    private String code;

    private String title;

    private String body;

//    @Enumerated(EnumType.STRING)
//    private Object priority;

    @Temporal(TemporalType.DATE)
    private Date date;

    @Transient
    private String persianDate;

    private String description;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_on")
    private Date createdOn;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "edited_on")
    private Date editedOn;

//    @Enumerated(EnumType.STRING)
    private String state;

//    @Enumerated(EnumType.STRING)
    private String status;


    }

