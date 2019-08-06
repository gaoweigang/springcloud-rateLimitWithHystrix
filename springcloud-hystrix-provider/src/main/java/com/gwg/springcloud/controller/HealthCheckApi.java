package com.gwg.springcloud.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

@RestController
@RequestMapping("/health")
public class HealthCheckApi {
	private static final Log logger = LogFactory.getLog(HealthCheckApi.class);


	/**
	 * 健康检查
	 * @return
	 */
	@RequestMapping(value = "/check",method= RequestMethod.GET)
	public Object checkHealth(HttpServletResponse response){
		HashMap<String,String> resultMap = new HashMap<String,String>();
		try {
			response.setStatus(HttpServletResponse.SC_OK);
			resultMap.put("result", "success");
		}catch (Exception e) {
			logger.error("健康检查异常: " , e);
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			resultMap.put("result", "error");
		}
		return resultMap;
	}
}
