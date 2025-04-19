package com.shubham.Expense.Tracker.controllers;

import com.shubham.Expense.Tracker.entities.RefreshToken;
import com.shubham.Expense.Tracker.models.UserInfoDto;
import com.shubham.Expense.Tracker.models.request.AuthRequestDTO;
import com.shubham.Expense.Tracker.models.request.RefreshTokenRequestDTO;
import com.shubham.Expense.Tracker.models.response.JwtResponseDTO;
import com.shubham.Expense.Tracker.services.JwtService;
import com.shubham.Expense.Tracker.services.RefreshTokenService;
import com.shubham.Expense.Tracker.services.UserDetailsServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.shubham.Expense.Tracker.Utils.ValidationUtils.isValidEmail;
import static com.shubham.Expense.Tracker.Utils.ValidationUtils.isValidPassword;

@AllArgsConstructor
@RestController
public class AuthController
{

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("auth/v1/signup")
    public ResponseEntity SignUp(@RequestBody UserInfoDto userInfoDto){

        if (userInfoDto.getUsername() == null || userInfoDto.getUsername().isEmpty()) {
            return new ResponseEntity<>("Email cannot be empty", HttpStatus.BAD_REQUEST);
        }else if(!isValidEmail(userInfoDto.getUsername())){
            return new ResponseEntity<>("Invalid email", HttpStatus.BAD_REQUEST);
        }

        if (userInfoDto.getPassword() == null || userInfoDto.getPassword().isEmpty()) {
            return new ResponseEntity<>("Password cannot be empty", HttpStatus.BAD_REQUEST);
        }else if (!isValidPassword(userInfoDto.getPassword())){
            return new ResponseEntity<>("Password should have at least 8 characters, one digit, one lowercase, one uppercase, one special character", HttpStatus.BAD_REQUEST);
        }


        try{
            Boolean isSignUped = userDetailsService.signupUser(userInfoDto);
            if(Boolean.FALSE.equals(isSignUped)){
                return new ResponseEntity<>("Already Exist", HttpStatus.BAD_REQUEST);
            }
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(userInfoDto.getUsername());
            String jwtToken = jwtService.GenerateToken(userInfoDto.getUsername());
            return new ResponseEntity<>(new JwtResponseDTO(jwtToken, refreshToken.getToken()), HttpStatus.OK);
        }catch (Exception ex){
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @PostMapping("auth/v1/login")
    public ResponseEntity AuthenticateAndGetToken(@RequestBody AuthRequestDTO authRequestDTO){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if(authentication.isAuthenticated()){
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(authRequestDTO.getUsername());
            return new ResponseEntity<>(JwtResponseDTO.builder()
                    .accessToken(jwtService.GenerateToken(authRequestDTO.getUsername()))
                    .token(refreshToken.getToken())
                    .build(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Exception in User Service", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("auth/v1/refreshToken")
    public JwtResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO){
        return refreshTokenService.findByToken(refreshTokenRequestDTO.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUserInfo)
                .map(userInfo -> {
                    String accessToken = jwtService.GenerateToken(userInfo.getUsername());
                    return JwtResponseDTO.builder()
                            .accessToken(accessToken)
                            .token(refreshTokenRequestDTO.getToken()).build();
                }).orElseThrow(() ->new RuntimeException("Refresh Token is not in DB..!!"));
    }
}
