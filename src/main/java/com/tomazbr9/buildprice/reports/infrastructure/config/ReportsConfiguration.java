
package com.tomazbr9.buildprice.reports.infrastructure.config;

import com.tomazbr9.buildprice.reports.domain.service.AbcCurveCalculator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportsConfiguration {

    @Bean
    public AbcCurveCalculator abcCurveCalculator() {
        return new AbcCurveCalculator();
    }
}
