package com.dpalaznik.mockito.example.loginservice;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Dmitry Palaznik <dmitry.palaznik@viaden.com>
 */
public class LoginServiceTest {

    private static final String ACCOUNT_ID_1 = "dmitry";
    private static final String PASSWORD = "password";
    private static final String ACCOUNT_ID_2 = "alex";

    private IAccount account;
    private IAccountRepository accountRepository;
    private LoginService service;

    @Before
    public void init() {
        account = mock(IAccount.class);
        when(account.getId()).thenReturn(ACCOUNT_ID_1);
        accountRepository = mock(IAccountRepository.class);
        when(accountRepository.find(anyString())).thenReturn(account);
        service = new LoginService(accountRepository);
    }

    @Test
    public void isShouldSetAccountToLoggedInWhenPasswordMatches() {
        willPasswordMatch(true);
        service.login(ACCOUNT_ID_1, PASSWORD);
        verify(account, times(1)).login();
    }

    @Test
    public void itShouldSetAccountToRevokedAfterThreeFailedLoginAttempts() {
        willPasswordMatch(false);
        for (int i = 0; i < 3; i++) {
            service.login(ACCOUNT_ID_1, PASSWORD);
        }
        verify(account, times(1)).setRevoked(true);
    }

    @Test
    public void itShouldNotSetAccountLoggedInIfPasswordDoesNotMatch() {
        willPasswordMatch(false);
        service.login(ACCOUNT_ID_1, PASSWORD);
        verify(account, never()).setLoggedIn(true);
    }

    @Test
    public void itShouldNotRevokeSecondAccountAfterTwoFailedAttemptsFirstAccount() {
        willPasswordMatch(false);

        IAccount secondAccount = mock(IAccount.class);
        when(secondAccount.passwordMatches(anyString())).thenReturn(false);
        when(accountRepository.find(ACCOUNT_ID_2)).thenReturn(secondAccount);

        service.login(ACCOUNT_ID_1, PASSWORD);
        service.login(ACCOUNT_ID_1, PASSWORD);
        service.login(ACCOUNT_ID_2, PASSWORD);

        verify(secondAccount, never()).setRevoked(true);
    }

    @Test(expected = AccountNotFoundException.class)
    public void itShouldThrowExceptionIfAccountNotFound() {
        when(accountRepository.find(ACCOUNT_ID_2)).thenReturn(null);
        service.login(ACCOUNT_ID_2, PASSWORD);
    }

    @Test
    public void itShouldResetBackToInitialStateAfterSuccessfulLogin() {
        willPasswordMatch(false);
        service.login("brett", "password");
        service.login("brett", "password");
        willPasswordMatch(true);
        service.login("brett", "password");
        willPasswordMatch(false);
        service.login("brett", "password");
        verify(account, never()).setRevoked(true);
    }

    private void willPasswordMatch(boolean value) {
        when(account.passwordMatches(anyString())).thenReturn(value);
    }
}
