package com.atguigu.gmall0925.dw.logger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DwLoggerApplication {

	public static void main(String[] args) {
		try {
			SpringApplication.run(DwLoggerApplication.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
