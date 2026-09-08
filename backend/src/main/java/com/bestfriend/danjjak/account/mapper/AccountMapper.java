package com.bestfriend.danjjak.account.mapper;

import com.bestfriend.danjjak.account.model.AccountRecord;
import com.bestfriend.danjjak.account.model.OwnedAccountImportCommand;
import com.bestfriend.danjjak.account.model.RecipientAccountCommand;
import com.bestfriend.danjjak.account.model.RegisteredPersonAccountRecord;
import com.bestfriend.danjjak.account.model.RegisteredPersonCommand;
import com.bestfriend.danjjak.account.model.RegisteredPersonRecord;
import com.bestfriend.danjjak.account.model.TransactionRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AccountMapper {

    List<AccountRecord> findOwnedAccounts(long userId);

    List<AccountRecord> findOwnedAccountCandidates(long userId);

    int importOwnedAccount(OwnedAccountImportCommand command);

    AccountRecord findOwnedAccount(
            @Param("userId") long userId, @Param("accountId") long accountId);

    List<RegisteredPersonRecord> findRegisteredPersons(long userId);

    RegisteredPersonRecord findRegisteredPerson(
            @Param("userId") long userId,
            @Param("registeredPersonId") long registeredPersonId);

    List<RegisteredPersonAccountRecord> findRegisteredPersonAccounts(
            @Param("userId") long userId,
            @Param("registeredPersonId") long registeredPersonId);

    int insertRegisteredPerson(RegisteredPersonCommand command);

    int insertRecipientAccount(RecipientAccountCommand command);

    int updateRegisteredPerson(RegisteredPersonCommand command);

    int updateRecipientAccount(RecipientAccountCommand command);

    List<TransactionRecord> findTransactions(
            @Param("userId") long userId,
            @Param("accountId") long accountId,
            @Param("category") String category);
}
