package com.gwg.springcloud;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 注解@EnableDiscoveryClient，@EnableEurekaClient的区别
 SpringCLoud中的“Discovery Service”有多种实现，比如：eureka, consul, zookeeper。
 1，@EnableDiscoveryClient注解是基于spring-cloud-commons依赖，并且在classpath中实现；
 2，@EnableEurekaClient注解是基于spring-cloud-netflix依赖，只能为eureka作用；
 如果你的classpath中添加了eureka，则它们的作用是一样的。


 因为我在classpath中添加了Eureka，所以在这里这两个注解的作用一样

 Feign+Hystrix熔断机制，降级
 FallbackFactory：表示调用服务出现问题时可以打印出相应日志
 如果需要访问产生回退触发器的原因，可以使用@feignclient中的fallbackFactory属性
 */
@SpringBootApplication
//@EnableDiscoveryClient
@EnableEurekaClient
//@ComponentScan(value = {"com.gwg.springcloud.*"})
@EnableHystrix //等价于yml中feign.hystrix.enabled = true
@EnableCircuitBreaker //默认是开启的，即引入了相应的jar会自动开启
@EnableHystrixDashboard
public class ProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class);
    }


    @Bean
    public ServletRegistrationBean getServlet(){
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(streamServlet);
        servletRegistrationBean.setLoadOnStartup(1);
        servletRegistrationBean.addUrlMappings("/hystrix.stream");
        servletRegistrationBean.setName("HystrixMetricsStreamServlet");
        return servletRegistrationBean;
    }
}