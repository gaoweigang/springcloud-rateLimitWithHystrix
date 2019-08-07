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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * hystrix做限流，在这里我们设置了核心线程数和最大线程数，但是使用jmeter做压测时，吞吐量一样可以很高，
 * 因此hystrix只能做简单的线程而已，并不能做到精确的并发控制
 */
@RestController
public class HelloController implements IHelloRemote{

    private volatile int count = 0;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Value("${msg:unknown}")
    private String msg;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 服务端 限流 与 服务降级使用@HystrixCommand。yml配置必须配合@HystrixCommand才起作用
     * @param name
     * @param age
     * @return
     */
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

        //1.服务降级
        try{
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }
        //2测试服务降级——异常处理
        /*if("gaoweigang".equals(name)){
            throw new IllegalArgumentException();
        }*/
        ServiceInstance serviceInstance = discoveryClient.getLocalServiceInstance();
        return Result.success(serviceInstance.getServiceId() + " (" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + ")" + "===>Say " + msg);
    }


    /**
     * 注意：在服务端做熔断和服务降级必须要配置@HystrixCommand才会生效, 仅在yml中配置 熔断和 服务降级无法生效
     *
     * 当方法执行时间大于execution.isolation.thread.timeoutInMilliseconds指定的时间时会熔断，此时如果配置Fallback，则会执行fallback做降级处理
     *
     */
    @HystrixCommand(fallbackMethod = "helloFallback")
    public @ResponseBody Result<String> hello() {

        //1.服务降级
        /*try{
            Thread.sleep(10);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        System.out.println("hello ...");
        return Result.success();
    }


    /**
     * 用于测试 Eureka服务端 熔断：
     * 测试结果：当在10秒内feign调用失败次数达到服务端配置的command.default.circuitBreaker.requestVolumeThreshold次数时候，feign接口会短路。
     * 短路之后在Eureka服务端配置的command.default.circuitBreaker.sleepWindowInMilliseconds时间段之后尝试恢复正常，在这期间所有对该feign接口的调用
     * 全部熔断处理
     * @return
     */
    @HystrixCommand(fallbackMethod = "printUserFallback")
    public @ResponseBody Result<String> printUser(@PathVariable("name") String name) {
        count++;
        String message = "date:"+dateFormat.format(new Date())+"   , name = "+name+", count = "+count;
        System.out.println(message);
        int a = count / 0;
        /*try{
            Thread.sleep(6000);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        return Result.success(message);
    }



    ///////////////////////////////////////////eureka client test start///////////////////////////////////////////////////////////

    /**
     * 用于测试 Eureka客户端 熔断：
     * 测试结果：当在10秒内feign调用失败次数达到客户端配置的command.default.circuitBreaker.requestVolumeThreshold次数时候，feign接口会短路。
     * 短路之后在Eureka客户端配置的command.default.circuitBreaker.sleepWindowInMilliseconds时间段之后尝试恢复正常，在这期间所有对该feign接口的调用
     * 全部熔断处理
     * @return
     */
    public @ResponseBody Result<String> queryUserByName(@PathVariable("name") String name) {
        count++;
        String message = "date:"+dateFormat.format(new Date())+"   , name = "+name+", count = "+count;
        System.out.println(message);
        int a = count / 0;
        /*try{
            Thread.sleep(6000);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        return Result.success(message);
    }

    /**
     * 结果说明：如果Eureka客户端与Eureka服务端都配置了熔断且都开启了，则服务端的配置会覆盖客户端的配置
     * @param name
     * @return
     */
    @HystrixCommand(fallbackMethod = "printUserInfoFallback")
    public @ResponseBody Result<String> printUserInfo(@PathVariable("name") String name) {
        count++;
        String message = "date:"+dateFormat.format(new Date())+"   , name = "+name+", count = "+count;
        System.out.println(message);
        int a = count / 0;
        /*try{
            Thread.sleep(6000);
        }catch (Exception e){
            e.printStackTrace();
        }*/
        return Result.success(message);
    }


    private  Result<String> printUserInfoFallback(String name){
        return Result.error("printUser fallback");
    }


    ///////////////////////////////////////////eureka client test end///////////////////////////////////////////////////////////


    private  Result<String> printServiceProviderFallback(String name, int age){
        return Result.error("fallback ,name = "+name+", age = "+age);
    }

    private  Result<String> helloFallback(){
        return Result.error("hello fallback");
    }

    private  Result<String> printUserFallback(String name){
        return Result.error("printUser fallback");
    }
}