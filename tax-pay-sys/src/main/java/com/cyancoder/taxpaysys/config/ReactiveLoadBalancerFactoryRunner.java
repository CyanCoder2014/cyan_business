//package com.cyancoder.taxpaysys.config;
//
//import lombok.extern.log4j.Log4j2;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.client.loadbalancer.Response;
//import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//
//@Log4j2
//@Component
//class ReactiveLoadBalancerFactoryRunner {
//
//    public static Flux<Object> call(WebClient http, String url) {
//        return http.get().uri(url).retrieve().bodyToFlux(Object.class);
//    }
//
//    ReactiveLoadBalancerFactoryRunner(ReactiveLoadBalancer.Factory<ServiceInstance> serviceInstanceFactory) {
//        var http = WebClient.builder().build();
//        ReactiveLoadBalancer<ServiceInstance> api = serviceInstanceFactory.getInstance("api");
//        Flux<Response<ServiceInstance>> chosen = Flux.from(api.choose());
//        chosen
//                .map(responseServiceInstance -> {
//                    ServiceInstance server = responseServiceInstance.getServer();
//                    var url = "http://" + server.getHost() + ':' + server.getPort() + "/greetings";
//                    log.info(url);
//                    return url;
//                })
//                .flatMap(url -> call(http, url))
//                .subscribe(greeting -> log.info("manual: " + greeting.toString()));
//
//    }
//
//
//
//}
