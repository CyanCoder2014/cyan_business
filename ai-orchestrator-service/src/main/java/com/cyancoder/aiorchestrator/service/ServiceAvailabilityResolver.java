package com.cyancoder.aiorchestrator.service;

import java.util.List;

public interface ServiceAvailabilityResolver {
    ServiceAvailabilitySnapshot resolve(List<String> requestedServiceKeys);
}
