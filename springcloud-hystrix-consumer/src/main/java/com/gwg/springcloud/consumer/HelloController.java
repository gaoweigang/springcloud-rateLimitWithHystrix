package com.gwg.springcloud.consumer;

import com.gwg.springcloud.common.Result;
import com.gwg.springcloud.remote.IHelloRemote;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @Value("${name:unknown}")
    private String name;

    @Autowired
    private IHelloRemote helloRemote;


    /**
     * 应用Consumer调用应用Provider的服务printServiceProvider
     * @return
     */
    @RequestMapping("/hello")
    public @ResponseBody Result hello() {
        System.out.println(Thread.currentThread().getId()+", 调用开始 start ****************");
        Result<String> result = helloRemote.printServiceProvider("gaoweigang", 11);
        System.out.println(Thread.currentThread().getId()+", 获取结果 "+result.getMessage());
        return result;
    }
}