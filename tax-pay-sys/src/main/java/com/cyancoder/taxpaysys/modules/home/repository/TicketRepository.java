package com.cyancoder.taxpaysys.modules.home.repository;

import com.cyancoder.taxpaysys.modules.home.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Integer> {


}
