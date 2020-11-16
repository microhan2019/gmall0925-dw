package com.atguigu.gmall0925.dw.publisher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//注意不要选择org开头的包，要选tk.
import tk.mybatis.spring.annotation.MapperScan;


@SpringBootApplication
@MapperScan(basePackages = "com.atguigu.gmall0925.dw.publisher.mapper")
public class DwPublisherApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwPublisherApplication.class, args);
	}

}
