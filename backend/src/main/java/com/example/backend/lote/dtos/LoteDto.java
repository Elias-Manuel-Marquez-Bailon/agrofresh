package com.example.backend.lote.dtos;

import com.example.backend.lote.model.MetodoCultivo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/** Datos que envía el productor para crear o editar un lote. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteDto {

    @NotNull(message = "El producto es obligatorio")
    private Long productoId;

    @NotNull(message = "La fecha de siembra es obligatoria")
    @PastOrPresent(message = "La fecha de siembra no puede ser futura")
    private LocalDate fechaSiembra;

    @PastOrPresent(message = "La fecha de cosecha no puede ser futura")
    private LocalDate fechaCosecha;

    @Size(max = 100, message = "La parcela no puede tener más de 100 caracteres")
    private String parcela;

    private MetodoCultivo metodoCultivo;

    @Size(max = 2000, message = "Las notas no pueden tener más de 2000 caracteres")
    private String notas;
}
