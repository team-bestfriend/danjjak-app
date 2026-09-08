package com.bestfriend.danjjak.pattern.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class PatternMapperBindingTest {

    @Test
    void bindsEveryMapperMethod() throws Exception {
        String resource = "mapper/pattern/PatternMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        for (var method : PatternMapper.class.getDeclaredMethods()) {
            assertTrue(configuration.hasStatement(PatternMapper.class.getName() + "." + method.getName()));
        }
    }
}
