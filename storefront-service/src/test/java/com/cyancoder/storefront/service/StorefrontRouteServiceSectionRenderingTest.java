package com.cyancoder.storefront.service;

import com.cyancoder.dynamiccore.runtime.DynamicRuntimeService;
import com.cyancoder.dynamiccore.runtime.DynamicScope;
import com.cyancoder.dynamiccore.store.mongo.DynamicEntityRecordDocument;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorefrontRouteServiceSectionRenderingTest {
    @Test void rendersRealSectionContentIntoHtmlAndSkipsHiddenSections() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        StorefrontRouteService service = new StorefrontRouteService(runtime, httpSupport);

        DynamicEntityRecordDocument route = new DynamicEntityRecordDocument();
        route.setRecordKey("home");
        route.setStatus("ACTIVE");
        route.setData(Map.of(
                "routeKey", "home",
                "path", "/",
                "publicationStatus", "PUBLISHED",
                "seo", Map.of("title", "Home"),
                "sections", List.of(
                        Map.of("sectionKey", "hero-1", "type", "hero", "order", 1, "visible", "true",
                                "content", Map.of("heading", "Build, automate, and launch", "primaryButtonLabel", "Get started", "primaryButtonHref", "/start")),
                        Map.of("sectionKey", "hero-2", "type", "hero", "order", 2, "visible", "false",
                                "content", Map.of("heading", "Hidden section should not render")),
                        Map.of("sectionKey", "faq-1", "type", "faq", "order", 3, "visible", "true",
                                "content", Map.of("items", List.of(Map.of("question", "Is this real?", "answer", "Yes."))))
                )
        ));
        when(runtime.listRecords(eq("site-route"), any(DynamicScope.class))).thenReturn(List.of(route));

        String html = service.renderHtml("/", new DynamicScope("tenant-demo", "site-demo"));

        assertTrue(html.contains("Build, automate, and launch"));
        assertTrue(html.contains("Get started"));
        assertTrue(html.contains("Is this real?"));
        assertFalse(html.contains("Hidden section should not render"));
    }

    @Test void fallsBackToSingleArticleWhenNoSectionsPresent() {
        DynamicRuntimeService runtime = mock(DynamicRuntimeService.class);
        InternalServiceHttpSupport httpSupport = mock(InternalServiceHttpSupport.class);
        StorefrontRouteService service = new StorefrontRouteService(runtime, httpSupport);

        DynamicEntityRecordDocument route = new DynamicEntityRecordDocument();
        route.setRecordKey("legacy");
        route.setStatus("ACTIVE");
        route.setData(Map.of(
                "routeKey", "legacy",
                "path", "/legacy",
                "publicationStatus", "PUBLISHED",
                "seo", Map.of("title", "Legacy page")
        ));
        when(runtime.listRecords(eq("site-route"), any(DynamicScope.class))).thenReturn(List.of(route));

        String html = service.renderHtml("/legacy", new DynamicScope("tenant-demo", "site-demo"));

        assertTrue(html.contains("<article>"));
        assertTrue(html.contains("Legacy page"));
    }
}
