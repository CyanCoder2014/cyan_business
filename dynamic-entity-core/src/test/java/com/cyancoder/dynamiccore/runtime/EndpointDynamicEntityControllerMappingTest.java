package com.cyancoder.dynamiccore.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EndpointDynamicEntityControllerMappingTest {

    @Test
    void exposesDirectAndFixedServiceKeyPaths() {
        RequestMapping mapping = EndpointDynamicEntityController.class.getAnnotation(RequestMapping.class);

        assertNotNull(mapping);
        assertArrayEquals(new String[]{
                "/endpoint/entities",
                "/api/${dynamic.runtime.service-key}/endpoint/entities"
        }, mapping.value());
    }
}
