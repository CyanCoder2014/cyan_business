package com.cyancoder.taxpaysys.modules.home.service;

import com.cyancoder.taxpaysys.modules.home.entity.Ticket;
import com.cyancoder.taxpaysys.modules.home.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;


    public void addTicket(){
        Ticket ticket = Ticket.builder()
                .title("test title")
                .body("test body")
                .build();

        ticketRepository.save(ticket);
    }



}

