package com.gasagency.dsc.service;

import com.gasagency.dsc.dto.AuthResponse;
import com.gasagency.dsc.dto.LoginRequest;
import com.gasagency.dsc.dto.SignupRequest;
import com.gasagency.dsc.entity.Agency;
import com.gasagency.dsc.repository.AgencyRepository;
import com.gasagency.dsc.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService {

    private final AgencyRepository agencyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AgencyRepository agencyRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.agencyRepository = agencyRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse signup(SignupRequest request) {
        if (agencyRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        Agency agency = Agency.builder()
                .name(request.agencyName())
                .ownerName(request.ownerName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .city(request.city())
                .role("AGENCY")
                .build();

        agency = agencyRepository.save(agency);
        log.info("New agency registered: {} (id={})", agency.getName(), agency.getId());

        String token = jwtService.generateToken(agency.getId(), agency.getEmail(), agency.getRole());

        return new AuthResponse(
                token,
                agency.getId(),
                agency.getName(),
                agency.getEmail(),
                agency.getRole(),
                agency.getSetupCompleted(),
                agency.getElevenLabsAgentId()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Agency agency = agencyRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), agency.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!agency.getActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }

        log.info("Agency logged in: {} (id={})", agency.getName(), agency.getId());

        String token = jwtService.generateToken(agency.getId(), agency.getEmail(), agency.getRole());

        return new AuthResponse(
                token,
                agency.getId(),
                agency.getName(),
                agency.getEmail(),
                agency.getRole(),
                agency.getSetupCompleted(),
                agency.getElevenLabsAgentId()
        );
    }
}
