package com.bestfriend.danjjak.analysis.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class UsageAnalysisMapperBindingTest {

    @Test
    void bindsEveryMapperMethod() throws Exception {
        String resource = "mapper/analysis/UsageAnalysisMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        for (var method : UsageAnalysisMapper.class.getDeclaredMethods()) {
            assertTrue(configuration.hasStatement(UsageAnalysisMapper.class.getName() + "." + method.getName()));
        }
    }
}
