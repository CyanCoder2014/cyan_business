package com.cyancoder.tax_pay_sys_service.modules.home.repository;

import com.cyancoder.tax_pay_sys_service.modules.home.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {


}
