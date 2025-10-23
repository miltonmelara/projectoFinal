package com.mycompany.proyectofinalpoo.repo.servicios.dto;

public class ResumenReservasGlobal {
    private final int total;
    private final int programadas;
    private final int enProgreso;
    private final int finalizadas;
    private final int entregadas;

    public ResumenReservasGlobal(int total, int programadas, int enProgreso, int finalizadas, int entregadas) {
        this.total = total;
        this.programadas = programadas;
        this.enProgreso = enProgreso;
        this.finalizadas = finalizadas;
        this.entregadas = entregadas;
    }

    public int getTotal() { return total; }
    public int getProgramadas() { return programadas; }
    public int getEnProgreso() { return enProgreso; }
    public int getFinalizadas() { return finalizadas; }
    public int getEntregadas() { return entregadas; }
}
