package com.bestfriend.danjjak.account.mapper;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.List;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

class AccountMapperBindingTest {

    @Test
    void bindsEveryMapperMethodToXmlStatement() {
        String resource = "mapper/account/AccountMapper.xml";
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

        String namespace = AccountMapper.class.getName() + ".";
        List.of(
                        "findOwnedAccounts",
                        "findOwnedAccountCandidates",
                        "importOwnedAccount",
                        "findOwnedAccount",
                        "findRegisteredPersons",
                        "findRegisteredPerson",
                        "findRegisteredPersonAccounts",
                        "insertRegisteredPerson",
                        "insertRecipientAccount",
                        "updateRegisteredPerson",
                        "updateRecipientAccount",
                        "findTransactions")
                .forEach(name -> assertTrue(configuration.hasStatement(namespace + name)));
    }
}
