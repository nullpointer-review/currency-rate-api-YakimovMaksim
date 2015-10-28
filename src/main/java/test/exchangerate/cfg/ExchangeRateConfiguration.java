package test.exchangerate.cfg;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = {"test.exchangerate.controller", "test.exchangerate.provider"})
public class ExchangeRateConfiguration extends WebMvcConfigurerAdapter {
}