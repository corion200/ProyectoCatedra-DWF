package sv.edu.udb.cfc.academic.dto;

import sv.edu.udb.cfc.academic.entity.Docente;

public record DocenteResponseDTO(
        Long id, String nombres, String apellidos, String dui, String correo,
        String tituloProfesional, String especialidad, Boolean activo) {

    public static DocenteResponseDTO from(Docente d) {
        return new DocenteResponseDTO(d.getId(), d.getNombres(), d.getApellidos(), d.getDui(), d.getCorreo(),
                d.getTituloProfesional(), d.getEspecialidad(), d.getActivo());
    }
}