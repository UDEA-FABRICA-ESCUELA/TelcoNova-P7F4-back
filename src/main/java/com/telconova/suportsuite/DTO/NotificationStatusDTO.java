package com.telconova.suportsuite.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class NotificationStatusDTO {

    private Long totalEnviado;
    private Long totalPendiente;
    private Long totalFallido;
    private Long totalProcesando;
    private Double tazaExito;





}
