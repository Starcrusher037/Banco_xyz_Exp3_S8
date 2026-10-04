package cl.duoc.bancoxyz.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para operaciones de retiro bancario.
 * Expone exclusivamente la informacion relevante para el cliente,
 * resguardando las propiedades y estructuras internas de la entidad Cuenta.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RespuestaRetiroDto {

    /**
     * Identificador unico de la transaccion contable procesada.
     */
    private String transaccionId;

    /**
     * Identificador de la cuenta afectada.
     */
    private Long cuentaId;

    /**
     * Monto debitado de la cuenta.
     */
    private Long montoRetirado;

    /**
     * Saldo contable resultante tras la operacion.
     */
    private Long nuevoSaldoContable;

    /**
     * Saldo total disponible considerando linea de sobregiro.
     */
    private Long saldoDisponibleTotal;

    /**
     * Canal a traves del cual se processo el giro.
     */
    private String canal;

    /**
     * Estado del procesamiento de la operacion.
     */
    private String estado;

    /**
     * Mensaje descriptivo del resultado operacional.
     */
    private String mensaje;
}
