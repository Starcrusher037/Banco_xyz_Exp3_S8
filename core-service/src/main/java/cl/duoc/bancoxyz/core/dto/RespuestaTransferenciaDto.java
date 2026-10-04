package cl.duoc.bancoxyz.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de transferencia electronica de fondos.
 * Confirma el procesamiento de la transaccion sin exponer los detalles completos
 * de las entidades internas involucradas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaTransferenciaDto {

    /**
     * Identificador unico de la transaccion de transferencia.
     */
    private String transaccionId;

    /**
     * Identificador de la cuenta de origen.
     */
    private Long cuentaOrigenId;

    /**
     * Identificador de la cuenta de destino.
     */
    private Long cuentaDestinoId;

    /**
     * Monto total transferido.
     */
    private Long montoTransferido;

    /**
     * Estado del procesamiento.
     */
    private String estado;

    /**
     * Mensaje de confirmacion del movimiento.
     */
    private String mensaje;
}
