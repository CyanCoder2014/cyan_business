package com.cyancoder.taxpaysys.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;


@Data
@AllArgsConstructor
public class ErrorMessage {

    private final Date timeStamp;
    private final String message;
}
