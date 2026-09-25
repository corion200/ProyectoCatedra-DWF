package sv.edu.udb.cfc.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import sv.edu.udb.cfc.security.entity.Rol;
import sv.edu.udb.cfc.security.entity.Usuario;
import sv.edu.udb.cfc.security.repository.RolRepository;
import sv.edu.udb.cfc.security.repository.UsuarioRepository;

import java.util.List;

/** Crea los 4 roles del enunciado y usuarios demo la primera vez que arranca. */
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    @Bean
    CommandLineRunner initSeguridad(RolRepository rolRepository,
                                    UsuarioRepository usuarioRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            for (String nombre : List.of("ADMIN", "RECEPCIONISTA", "CLIENTE", "CONTABILIDAD")) {
                rolRepository.findByNombre(nombre)
                        .orElseGet(() -> rolRepository.save(Rol.builder().nombre(nombre).build()));
            }
            if (usuarioRepository.count() == 0) {
                usuarioRepository.save(Usuario.builder()
                        .nombre("Administrador del CFC").correo("admin@udb.edu.sv")
                        .password(passwordEncoder.encode("Admin123!"))
                        .rol(rolRepository.findByNombre("ADMIN").orElseThrow()).build());
                usuarioRepository.save(Usuario.builder()
                        .nombre("Recepcionista del CFC").correo("recep@udb.edu.sv")
                        .password(passwordEncoder.encode("Recep123!"))
                        .rol(rolRepository.findByNombre("RECEPCIONISTA").orElseThrow()).build());
                usuarioRepository.save(Usuario.builder()
                        .nombre("Contabilidad del CFC").correo("contab@udb.edu.sv")
                        .password(passwordEncoder.encode("Conta123!"))
                        .rol(rolRepository.findByNombre("CONTABILIDAD").orElseThrow()).build());
                usuarioRepository.save(Usuario.builder()
                        .nombre("Cliente Demo").correo("cliente@gmail.com")
                        .password(passwordEncoder.encode("Cliente123!"))
                        .rol(rolRepository.findByNombre("CLIENTE").orElseThrow()).build());
            }
        };
    }
}
