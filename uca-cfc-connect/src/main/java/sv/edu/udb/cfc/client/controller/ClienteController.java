package sv.edu.udb.cfc.client.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sv.edu.udb.cfc.client.dto.ClienteCreateDTO;
import sv.edu.udb.cfc.client.dto.ClienteResponseDTO;
import sv.edu.udb.cfc.client.dto.ClienteUpdateDTO;
import sv.edu.udb.cfc.client.enums.TipoCliente;
import sv.edu.udb.cfc.client.service.ClienteService;

@Tag(name = "Clientes", description = "Personas naturales y empresas del CFC")
@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Registra un cliente (PERSONA requiere DUI; EMPRESA usa razón social)")
    @ApiResponse(responseCode = "201", description = "Cliente creado")
    @ApiResponse(responseCode = "409", description = "DUI/NIT ya registrado")
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> crear(@Valid @RequestBody ClienteCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(dto));
    }

    @Operation(summary = "Lista clientes con filtros combinables y paginación")
    @GetMapping
    public Page<ClienteResponseDTO> listar(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) TipoCliente tipo,
            @RequestParam(required = false) Boolean activo,
            @PageableDefault(size = 10, sort = "nombre", direction = Sort.Direction.ASC) Pageable pageable) {
        return clienteService.listar(nombre, tipo, activo, pageable);
    }

    @Operation(summary = "Obtiene un cliente por id")
    @GetMapping("/{id}")
    public ClienteResponseDTO obtenerPorId(@PathVariable Long id) {
        return clienteService.obtenerPorId(id);
    }

    @Operation(summary = "Actualiza los datos de un cliente")
    @PutMapping("/{id}")
    public ClienteResponseDTO actualizar(@PathVariable Long id,
                                         @Valid @RequestBody ClienteUpdateDTO dto) {
        return clienteService.actualizar(id, dto);
    }

    @Operation(summary = "Desactiva un cliente (soft delete)")
    @ApiResponse(responseCode = "204", description = "Cliente desactivado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}