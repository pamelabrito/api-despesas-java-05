package com.github.progirls.despesas.api.despesas_api.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRedefinirSenhaDTO;
import com.github.progirls.despesas.api.despesas_api.dto.UsuarioRegisterDto;
import com.github.progirls.despesas.api.despesas_api.entities.Usuario;
import com.github.progirls.despesas.api.despesas_api.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
public class UsuarioService {

    private BCryptPasswordEncoder passwordEncoder;
    private UsuarioRepository usuarioRepository;

    // Método Construtor
    public UsuarioService(BCryptPasswordEncoder passwordEncoder, UsuarioRepository usuarioRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    // Método para a criação do usuário
    public void criarUsuario(UsuarioRegisterDto usuarioRegisterDto) {
        // If para verificar se o email já foi utilizado, evitando repetição
        var usuarioEmail = usuarioRepository.findByEmail(usuarioRegisterDto.email());
        if (usuarioEmail.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado, por favor tente outro.");
        }

        String encryptedPassword = passwordEncoder.encode(usuarioRegisterDto.senha());

        // Criando o novo usuário e salvando os parâmetros passados
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioRegisterDto.nome());
        usuario.setSenha(encryptedPassword);
        usuario.setEmail(usuarioRegisterDto.email());
        usuario.setDataCriacao(LocalDateTime.now());

        // Salvando no repositório e retornando o usuário
        usuarioRepository.save(usuario);

    }

    @Transactional
    public void atualizarSenha(UsuarioRedefinirSenhaDTO novaSenha, Authentication authentication) {

        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String novaSenhaCriptografada = passwordEncoder.encode(novaSenha.senha());

        usuario.setSenha(novaSenhaCriptografada);
        usuarioRepository.save(usuario);
    }
}
