package com.github.progirls.despesas.api.despesas_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.progirls.despesas.api.despesas_api.dto.NovaDespesaDTO;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DespesaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {

        Usuario user = new Usuario(
                null,
                "Teste",
                "natalia@teste.com",
                passwordEncoder.encode("12345"),
                LocalDateTime.now());
        usuarioRepository.save(user);
    }

    @Test
    void deveCriarDespesaComSucesso() throws Exception {
        var response = mockMvc.perform(post("/api/v1/login")
                        .with(httpBasic("natalia@teste.com", "12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");

        NovaDespesaDTO dto = new NovaDespesaDTO(
                100.0,
                "Shopping",
                2,
                LocalDate.now(),
                null,
                false
        );

        mockMvc.perform(post("/api/v1/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated());
    }

    @Test
    void deveRetornarErro400() throws Exception{
        var response = mockMvc.perform(post("/api/v1/login")
                        .with(httpBasic("natalia@teste.com", "12345")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String json = response.getResponse().getContentAsString();
        String token = JsonPath.read(json, "$.token");

        String despesaInvalida = """
                {
                    "valor" = 230.0,
                    "descricao" = "Despesa Inválida"
                }
                """;

        mockMvc.perform(post("/api/v1/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(despesaInvalida)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornarErro401() throws Exception{
        NovaDespesaDTO dto = new NovaDespesaDTO(
                100.0,
                "Shopping",
                2,
                LocalDate.now(),
                null,
                false
        );

        mockMvc.perform(post("/api/v1/despesas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header("Authorization", "Bearer TOKEN_INVALIDO"))
                .andExpect(status().isUnauthorized());
    }
}
