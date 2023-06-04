package com.example.demospringsercurity.auth;

import com.example.demospringsercurity.jwt.JwtService;
import com.example.demospringsercurity.jwt.JwtToken;
import com.example.demospringsercurity.jwt.TokenRepository;
import com.example.demospringsercurity.role.Role;
import com.example.demospringsercurity.role.RoleRepository;
import com.example.demospringsercurity.user.User;
import com.example.demospringsercurity.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final PasswordEncoder encoder;
    private final AuthenticationManager manager;

    private void saveUserToken(User user, String token) {
       var newToken = JwtToken.builder()
                .token(token)
                .expired(false)
                .revoked(false)
                .referenceUser(user)
                .build();
        tokenRepository.save(newToken);
    }

    private void revokeAllUserTokens(User user) {
        var tokens = tokenRepository.findAllValidTokenByUser(user.getUserId());
        if (tokens.isEmpty()) {return;}
        tokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(tokens);
    }
    public AuthenticationResponse register(RegisterRequest request) {
        var existedUser = userRepository.findByUserName(request.getUserName());
        if (existedUser.isPresent()) {
            throw new IllegalArgumentException("User name existed");
        }
        var role = roleRepository.findRoleByName(request.getRole());
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(role);
        User newUser = User.builder()
                .userName(request.getUserName())
                .password(encoder.encode(request.getPassword()))
                .dob(request.getDob())
                .roles(userRoles)
                .build();
        var savedUser = userRepository.save(newUser);
        String jwtToken = jwtService.generateToken(newUser);
        saveUserToken((User) savedUser, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );
        User user = userRepository.findByUserName(request.getUserName()).get();
        String jwtToken = jwtService.generateToken(user);
        revokeAllUserTokens(user);
        saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
