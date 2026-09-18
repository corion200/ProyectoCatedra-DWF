package sv.edu.udb.cfc.client.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sv.edu.udb.cfc.client.dto.ClienteCreateDTO;
import sv.edu.udb.cfc.client.dto.ClienteResponseDTO;
import sv.edu.udb.cfc.client.dto.ClienteUpdateDTO;
import sv.edu.udb.cfc.client.entity.Cliente;
import sv.edu.udb.cfc.client.enums.TipoCliente;
import sv.edu.udb.cfc.client.repository.ClienteRepository;
import sv.edu.udb.cfc.shared.exception.BusinessException;
import sv.edu.udb.cfc.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public ClienteResponseDTO crear(ClienteCreateDTO dto) {
        validarDuiSegunTipo(dto.tipoCliente(), dto.dui());
        String nit = dto.nit().trim();
        String dui = normalizarOpcional(dto.dui());
        validarUnicidad(nit, dui, null);

        Cliente cliente = Cliente.builder()
                .tipoCliente(dto.tipoCliente())
                .nombre(dto.nombre().trim())
                .dui(dui)
                .nit(nit)
                .correo(dto.correo().trim().toLowerCase())
                .telefono(dto.telefono().trim())
                .direccion(dto.direccion())
                .contactoNombre(dto.contactoNombre())
                .build();
        return ClienteResponseDTO.from(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponseDTO actualizar(Long id, ClienteUpdateDTO dto) {
        Cliente cliente = buscarEntidad(id);
        validarDuiSegunTipo(dto.tipoCliente(), dto.dui());
        String nit = dto.nit().trim();
        String dui = normalizarOpcional(dto.dui());
        validarUnicidad(nit, dui, id);

        cliente.setTipoCliente(dto.tipoCliente());
        cliente.setNombre(dto.nombre().trim());
        cliente.setDui(dui);
        cliente.setNit(nit);
        cliente.setCorreo(dto.correo().trim().toLowerCase());
        cliente.setTelefono(dto.telefono().trim());
        cliente.setDireccion(dto.direccion());
        cliente.setContactoNombre(dto.contactoNombre());
        if (dto.activo() != null) {
            cliente.setActivo(dto.activo());
        }
        return ClienteResponseDTO.from(cliente);
    }

    @Override
    public ClienteResponseDTO obtenerPorId(Long id) {
        return ClienteResponseDTO.from(buscarEntidad(id));
    }

    @Override
    public Page<ClienteResponseDTO> listar(String nombre, TipoCliente tipo, Boolean activo, Pageable pageable) {
        return clienteRepository.findAll(conFiltros(nombre, tipo, activo), pageable)
                .map(ClienteResponseDTO::from);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        // Soft delete: las inscripciones/pagos históricos conservan la referencia al cliente
        buscarEntidad(id).setActivo(false);
    }

    // ─────────────── reglas de negocio ───────────────

    /** REGLA: las PERSONAS naturales se identifican con DUI; las EMPRESAS no lo tienen. */
    private void validarDuiSegunTipo(TipoCliente tipo, String dui) {
        if (tipo == TipoCliente.PERSONA && (dui == null || dui.isBlank())) {
            throw new BusinessException("El DUI es obligatorio para clientes de tipo PERSONA",
                    HttpStatus.BAD_REQUEST);
        }
    }

    private void validarUnicidad(String nit, String dui, Long idExcluido) {
        boolean nitExiste = (idExcluido == null)
                ? clienteRepository.existsByNit(nit)
                : clienteRepository.existsByNitAndIdNot(nit, idExcluido);
        if (nitExiste) {
            throw new BusinessException("Ya existe un cliente con el NIT: " + nit);
        }
        if (dui != null) {
            boolean duiExiste = (idExcluido == null)
                    ? clienteRepository.existsByDui(dui)
                    : clienteRepository.existsByDuiAndIdNot(dui, idExcluido);
            if (duiExiste) {
                throw new BusinessException("Ya existe un cliente con el DUI: " + dui);
            }
        }
    }

    // ─────────────── helpers ───────────────

    private Specification<Cliente> conFiltros(String nombre, TipoCliente tipo, Boolean activo) {
        return (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (nombre != null && !nombre.isBlank()) {
                condiciones.add(cb.like(cb.lower(root.get("nombre")),
                        "%" + nombre.trim().toLowerCase() + "%"));
            }
            if (tipo != null) {
                condiciones.add(cb.equal(root.get("tipoCliente"), tipo));
            }
            if (activo != null) {
                condiciones.add(cb.equal(root.get("activo"), activo));
            }
            return cb.and(condiciones.toArray(new Predicate[0]));
        };
    }

    private String normalizarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    private Cliente buscarEntidad(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", id));
    }
}