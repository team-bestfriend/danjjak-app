package com.bestfriend.danjjak.transfer.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class TransferMapperBindingTest {

    @Test
    void bindsEveryMapperMethodToXmlStatement() {
        String resource = "mapper/transfer/TransferMapper.xml";
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

        String namespace = TransferMapper.class.getName() + ".";
        List.of(
                        "findSourceAccountForUpdate",
                        "findRegisteredRecipient",
                        "countAvailablePatternExecution",
                        "countRecentTransfers",
                        "debitAccount",
                        "insertTransaction",
                        "insertAnomaly",
                        "findPendingMatchingAnomalyForUpdate",
                        "findAnomalyForUpdate",
                        "resolveAnomalyAsCancelled",
                        "resolveAnomalyAsContinued",
                        "finishPatternExecution")
                .forEach(name -> assertTrue(configuration.hasStatement(namespace + name)));
    }
}
