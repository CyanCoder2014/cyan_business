package come.cyancoder.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicEntityAliasRouteConfigurationTest {

    private static final String ROUTE_PREFIX = "spring.cloud.gateway.server.webflux.routes[";

    @Test
    void everyDynamicServiceHasAQualifiedEndpointAliasAndRewrite() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadAllProperties("application.properties");

        List<RouteAlias> aliases = List.of(
                new RouteAlias(2, "tax-pay-sys", "tax-pay"),
                new RouteAlias(3, "factor-service", "factor"),
                new RouteAlias(5, "buyer-service", "buyer"),
                new RouteAlias(7, "product-service", "product"),
                new RouteAlias(9, "client-service", "client"),
                new RouteAlias(17, "content-service", "content"),
                new RouteAlias(18, "catalog-service", "catalog"),
                new RouteAlias(19, "crm-service", "crm"),
                new RouteAlias(20, "commerce-service", "commerce"),
                new RouteAlias(21, "finance-service", "finance"),
                new RouteAlias(22, "inventory-service", "inventory"),
                new RouteAlias(23, "report-service", "report"),
                new RouteAlias(30, "payment-service", "payment"),
                new RouteAlias(31, "storefront-service", "storefront"),
                new RouteAlias(32, "media-service", "media"),
                new RouteAlias(33, "cart-service", "cart"),
                new RouteAlias(34, "checkout-service", "checkout"),
                new RouteAlias(35, "payment-orchestrator-service", "payment-orchestrator"),
                new RouteAlias(36, "automation-orchestrator-service", "automation-orchestrator"),
                new RouteAlias(37, "pricing-promotion-service", "pricing-promotion"),
                new RouteAlias(38, "search-index-service", "search-index"),
                new RouteAlias(39, "notification-service", "notification"),
                new RouteAlias(40, "bpm-service", "bpm"),
                new RouteAlias(41, "ai-orchestrator-service", "ai-orchestrator"),
                new RouteAlias(42, "bot-adapter-service", "bot-adapter")
        );

        for (RouteAlias route : aliases) {
            String prefix = ROUTE_PREFIX + route.index() + "].";
            String externalPath = "/endpoint/" + route.alias() + "/entities/**";
            String broaderServicePath = "/endpoint/" + route.alias() + "/**";
            String rewrite = "RewritePath=/endpoint/" + route.alias()
                    + "/entities/(?<segment>.*), /endpoint/entities/${segment}";
            String predicate = properties.getProperty(prefix + "predicates[0]");

            assertEquals(route.serviceId(), properties.getProperty(prefix + "id"));
            assertTrue(predicate.contains(externalPath) || predicate.contains(broaderServicePath),
                    () -> route.serviceId() + " is missing " + externalPath);
            assertEquals(rewrite, properties.getProperty(prefix + "filters[0]"));
        }
    }

    private record RouteAlias(int index, String serviceId, String alias) {
    }
}
