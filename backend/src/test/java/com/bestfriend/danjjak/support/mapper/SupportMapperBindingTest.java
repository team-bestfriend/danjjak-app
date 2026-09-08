package com.bestfriend.danjjak.support.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class SupportMapperBindingTest {

    @Test
    void bindsEveryMapperMethodToXmlStatement() {
        String resource = "mapper/support/SupportMapper.xml";
        Configuration configuration = new Configuration();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(input);
            new XMLMapperBuilder(
                            input,
                            configuration,
                            resource,
                            configuration.getSqlFragments())
                    .parse();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }

        String namespace = SupportMapper.class.getName() + ".";
        List.of("findGuardian", "upsertGuardian", "findHighAnomaly", "markGuardianNotified")
                .forEach(name -> assertTrue(configuration.hasStatement(namespace + name)));
    }
}
