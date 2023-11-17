package com.cyancoder.taxpaysys.modules.home.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

@Service
public class ExternalService {


    @Autowired
    private ExternalFeignClientAsync externalFeignClientAsync;

    public List<Object> getAllRecords(){

        return IntStream.range(0, 2)
                .parallel()
                .mapToObj(i ->
                        {
                            try {
                                return externalFeignClientAsync.getRecordsAsync();
                            } catch (ExecutionException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .map(this::mapToProductResponse)
                .toList();
    }

    private Object mapToProductResponse(Object object) {
        return object;
    }

}
