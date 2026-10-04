package cl.duoc.bancoxyz.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para la ejecucion de giros o retiros bancarios.
 * Encapsula y valida los parametros de entrada requeridos por la operacion,
 * evitando la exposicion directa de entidades del modelo de dominio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudRetiroDto {

    /**
     * Identificador unico de la cuenta sobre la cual se efectuara el cargo.
     */
    @NotNull(message = "El identificador de la cuenta es obligatorio")
    private Long cuentaId;

    /**
     * Monto monetario solicitado para el retiro.
     * Debe ser mayor o igual al limite minimo operacional de $1.000.
     */
    @NotNull(message = "El monto del retiro es obligatorio")
    @Min(value = 1000, message = "El monto minimo para retiros es de $1.000")
    private Long monto;

    /**
     * Canal electronico o presencial por el cual se inicia la operacion.
     * Valores esperados: ATM, WEB, MOVIL.
     */
    private String canal;
}
