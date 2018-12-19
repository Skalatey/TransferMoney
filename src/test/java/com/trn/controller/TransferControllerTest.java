package com.trn.controller;

import com.trn.dto.AccountDto;
import com.trn.dto.TransferRequestDto;
import com.trn.entity.Account;
import com.trn.exception.OverDraftException;
import com.trn.service.AccountService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(TransferController.class)
public class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService service;

    @Test
    public void whenGetWithdrawalIsOverDraftThenThrowException() throws Exception {
        when(service.withdraw(any(AccountDto.class))).thenThrow(new OverDraftException("Account does not have enough balance to transfer.", "CLIENT_ERROR"));

        this.mockMvc.perform(MockMvcRequestBuilders.put("/transaction/withdrawal")
                .content("{\"id\":1,\"amount\":300000}")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void whenGetDepositRequestIsCorrectThenUpdateSuccess() throws Exception {
        AccountDto accountDto = new AccountDto(1L, 300);

        Account result = new Account();
        result.setId(1L);
        result.setAmount(new BigDecimal("1300.00"));

        ArgumentCaptor<AccountDto> captor = ArgumentCaptor.forClass(AccountDto.class);

        when(service.deposit(any(AccountDto.class))).thenReturn(result);

        this.mockMvc.perform(MockMvcRequestBuilders.put("/transaction/deposit")
                .content("{\"id\":1,\"amount\":300}")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.amount", is(1300.00)))
                .andExpect(status().isOk());

        verify(service).deposit(captor.capture());
        AccountDto value = captor.getValue();
        Assert.assertEquals(accountDto.getId(), value.getId());
        Assert.assertEquals(accountDto.getAmount(), value.getAmount(), 0.1);
    }

    @Test
    public void whenGetTransferRequestIsCorrectThenUpdateSuccess() throws Exception {
        TransferRequestDto transfer = new TransferRequestDto();
        transfer.setIdToAccount(1L);
        transfer.setIdFromAccount(2L);
        transfer.setAmount(80);

        ArgumentCaptor<TransferRequestDto> valueCapture = ArgumentCaptor.forClass(TransferRequestDto.class);
        doNothing().when(service).transfer(any(TransferRequestDto.class));

        this.mockMvc.perform(MockMvcRequestBuilders.put("/transaction/transfer")
                .content("{\"idToAccount\":1,\"idFromAccount\":2,\"amount\":80}")
                .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andDo(print()).andExpect(status().isOk());

        verify(service).transfer(valueCapture.capture());
        TransferRequestDto captureValue = valueCapture.getValue();

        Assert.assertEquals(transfer.getIdToAccount(), captureValue.getIdToAccount());
        Assert.assertEquals(transfer.getIdFromAccount(), captureValue.getIdFromAccount());
        Assert.assertEquals(transfer.getAmount(), captureValue.getAmount(), 0.1);
    }
}
