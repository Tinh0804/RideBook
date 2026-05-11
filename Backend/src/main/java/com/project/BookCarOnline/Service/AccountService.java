package com.project.BookCarOnline.Service;

import com.project.BookCarOnline.DTO.Request.AccountRequest;
import com.project.BookCarOnline.DTO.Response.AccountResponse;
import com.project.BookCarOnline.Entity.Enum.PredefinedRole;
import com.project.BookCarOnline.Entity.Account;
import com.project.BookCarOnline.Exception.AppException;
import com.project.BookCarOnline.Exception.ErrorCode;
import com.project.BookCarOnline.Mapper.AccountMapper;
import com.project.BookCarOnline.Repository.AccountRepository;
import com.project.BookCarOnline.Repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    AccountResponse createTaiKhoan(AccountRequest request) {
        if(accountRepository.existsByUserName(request.getUserName()))
            throw new AppException(ErrorCode.USER_EXITED);
        Account account = accountMapper.toAccount(request);
        account.setPassWord(passwordEncoder.encode(request.getPassWord()));



       account.setRoleNo(
               roleRepository.findById(PredefinedRole.CUSTOMER.name()
               ).orElseThrow(()-> new AppException(ErrorCode.ROLE_NOT_FOUND)));

        try {
            account = accountRepository.save(account);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.USER_EXITED);
        }

        return accountMapper.toAccountResponse(account);
    }


}
