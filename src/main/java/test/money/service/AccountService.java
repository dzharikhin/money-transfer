package test.money.service;

import test.money.model.Account;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by dzharikhin on 17.05.2016.
 */
public interface AccountService {

    Account createAccount(@NotNull UUID id, String label);
    Collection<Account> getAccounts(Collection<String> labelFilter);
    Optional<Account> getAccount(@NotNull UUID id);
    Account updateAccount(@NotNull Account account, String label);
    void deleteAccount(@NotNull Account account);
}
