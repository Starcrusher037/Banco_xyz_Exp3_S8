package cl.duoc.bancoxyz.core.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de solicitud para transferencias electronicas de fondos entre cuentas.
 * Valida que los datos obligatorios de origen, destino y monto cumplan con
 * las reglas minimas del negocio antes de pasar a la capa de servicio.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolicitudTransferenciaDto {

    /**
     * Identificador de la cuenta origen desde la cual se debitaran los fondos.
     */
    @NotNull(message = "El identificador de la cuenta origen es obligatorio")
    private Long cuentaOrigenId;

    /**
     * Identificador de la cuenta destino que recibira el abono.
     */
    @NotNull(message = "El identificador de la cuenta destino es obligatorio")
    private Long cuentaDestinoId;

    /**
     * Monto a transferir. Debe ser positivo y cumplir el valor minimo permitido.
     */
    @NotNull(message = "El monto a transferir es obligatorio")
    @Min(value = 100, message = "El monto minimo para transferencias es de $100")
    private Long monto;

    /**
     * Comentario opcional o glosa descriptiva de la transferencia.
     */
    private String comentario;
}
