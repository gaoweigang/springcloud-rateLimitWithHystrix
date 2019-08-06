package com.gwg.springcloud.remote.fallback;

import com.gwg.springcloud.common.Result;
import com.gwg.springcloud.remote.IHelloRemote;
import com.netflix.hystrix.exception.HystrixTimeoutException;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 熔断处理
 */
@Component
@Slf4j
public class IHelloFallback implements FallbackFactory<IHelloRemote> {

    /**
     * 在springcloud项目启动的时候,我们会调用FallbackFactory的create[create的参数是 RuntimeException]获取一个fallback样本，
     * 以检查返回的fallback样本的类型是否与@FeignClient注解的feign接口的fallback类型是否兼容。
     * @param throwable
     * @return
     */
    public IHelloRemote create(Throwable throwable) {
        final StringBuffer message = new StringBuffer("IHelloRemote fallback");

        //最好不要在这里打印出异常。因为在springcloud项目启动的时候,我们会调用FallbackFactory的create[create的参数是 RuntimeException]获取一个fallback样本，
        // 以检查返回的fallback样本的类型是否与@FeignClient注解的feign接口的fallback类型是否兼容。所以使用throwable.printStackTrace()会打印出RuntimeException异常。
        //throwable.printStackTrace();

        if(throwable != null && throwable instanceof HystrixTimeoutException){
            message.append(" timeout ");
            log.error(message.toString(), throwable);

        }
        return new IHelloRemote(){
            private String msg = message.toString();

            public Result<String> printServiceProvider(String name, int age) {
                System.out.println("printServiceProvider fallback ....");
                return  Result.error(msg);
            }

            @Override
            public Result<String> hello() {
                System.out.println("hello fallback ....");
                return Result.error(msg);
            }
        };
    }
}
