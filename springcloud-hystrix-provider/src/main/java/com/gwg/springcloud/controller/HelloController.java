package com.gwg.springcloud.controller;

import com.gwg.springcloud.common.Result;
import com.gwg.springcloud.remote.IHelloRemote;
import com.gwg.springcloud.remote.fallback.IHelloFallback;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

/**
 * hystrix做限流，在这里我们设置了核心线程数和最大线程数，但是使用jmeter做压测时，吞吐量一样可以很高，
 * 因此hystrix只能做简单的线程而已，并不能做到精确的并发控制
 */
@RestController
public class HelloController implements IHelloRemote{

    @Autowired
    DiscoveryClient discoveryClient;

    @Value("${msg:unknown}")
    private String msg;

    @HystrixCommand(commandKey = "threadKey1",
            commandProperties = {
            @HystrixProperty(name="execution.isolation.strategy", value="THREAD"),
            @HystrixProperty(name="execution.isolation.thread.timeoutInMilliseconds" , value = "3000")},//设置超时时间
            threadPoolKey = "threadPoolKey1",
            threadPoolProperties = {
                    //核心线程数，最大并发线程数量
                    @HystrixProperty(name = "coreSize", value = "1"),
                    /*
                     * 设置线程池大小，默认为-1，创建的队列是SynchronousQueue,如果设置大于0则使用LinkedBlockingQueue。如果从-1换成其他值则需重启，
                     * 该值不能动态调整。如果需要动态调整，需要使用hystrix.threadpool.default.queueSizeRejectionThreshold配置
                     */
                    @HystrixProperty(name="maximumSize", value="1"),
                    /*
                     * 此属性允许maximumSize的配置生效。那么maximumSize可以等于或高于coreSize。 设置coreSize < maximumSize 会创建一个线程池，
                     * 该线程池可以支持 maximumSize 并发，但在相对不活动期间将向系统返回线程
                     */
                    @HystrixProperty(name="allowMaximumSizeToDivergeFromCoreSize", value="true"),
                    @HystrixProperty(name="queueSizeRejectionThreshold", value="1")
            },
            fallbackMethod = "printServiceProviderFallback") //服务降级，包含熔断降级，异常处理降级
    public @ResponseBody Result<String> printServiceProvider(@PathVariable("name") String name, @PathVariable("age") int age){
        System.out.println("调用服务开始 start ..........");

        //1.熔断超时测试
        try{
            int count =0;
            for(int i = 0; i < 100000000; i++){
                count = i;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //2测试熔断异常处理
        /*if("gaoweigang".equals(name)){
            throw new IllegalArgumentException();
        }*/
        ServiceInstance serviceInstance = discoveryClient.getLocalServiceInstance();
        return Result.success(serviceInstance.getServiceId() + " (" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + ")" + "===>Say " + msg);
    }


    /**
     * 当方法执行时间大于execution.isolation.thread.timeoutInMilliseconds指定的时间时会熔断，此时如果配置Fallback，则会执行fallback做降级处理
     * @return
     */
    public @ResponseBody Result<String> hello() {

        //1.熔断超时测试
        try{
            Thread.sleep(5000);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("hello ...");
        return Result.success();
    }

    public  Result<String> printServiceProviderFallback(String name, int age){
        return Result.error("hello fallback"+name+age);
    }
}