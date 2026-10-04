package cl.duoc.bancoxyz.core.mensajeria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoTransaccion implements Serializable {
    private static final long serialVersionUID = 1L;

    private String transaccionId;
    private Long cuentaOrigenId;
    private Long cuentaDestinoId;
    private Long monto;
    private String tipoOperacion; // RETIRO, TRANSFERENCIA
    private String canal;         // MOVIL, WEB, ATM
    private String estado;        // EXITOSA, CONTINGENCIA
    private String fechaHora;
    private String detalle;
}
