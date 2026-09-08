package com.bestfriend.danjjak.user.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class UserMapperBindingTest {

    @Test
    void bindsEveryMapperMethodToXmlStatement() {
        String resource = "mapper/user/UserMapper.xml";
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

        String namespace = UserMapper.class.getName() + ".";
        List.of(
                        "findCurrentUser",
                        "findByKakaoUserId",
                        "findFirstUnlinkedUser",
                        "bindKakaoUserId",
                        "updateConsents",
                        "updateSettings")
                .forEach(name -> assertTrue(configuration.hasStatement(namespace + name)));
    }
}
