package com.example.packing_iso_service.config;

import org.jpos.iso.ISOException;
import org.jpos.iso.packager.GenericPackager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ISO8583PackagerConfig {
    @Bean
    public GenericPackager isoPackager() throws IOException, ISOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("iso8583-jpos-packager.xml");
        return new GenericPackager(is);
    }
}
