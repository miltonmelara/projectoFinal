/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class CalendarioReservas {
	private final LocalDate fechaInicio;
	private final LocalDate fechaFin;
	private final List<DiaCalendario> dias;
	private final int totalReservas;
	private final int totalProgramadas;
	private final int totalEnProgreso;
	private final int totalFinalizadas;
	private final int totalEntregadas;

	public CalendarioReservas(LocalDate fechaInicio, LocalDate fechaFin, List<DiaCalendario> dias, int totalReservas, int totalProgramadas, int totalEnProgreso, int totalFinalizadas, int totalEntregadas) {
		this.fechaInicio = fechaInicio;
		this.fechaFin = fechaFin;
		this.dias = dias == null ? Collections.emptyList() : Collections.unmodifiableList(dias);
		this.totalReservas = totalReservas;
		this.totalProgramadas = totalProgramadas;
		this.totalEnProgreso = totalEnProgreso;
		this.totalFinalizadas = totalFinalizadas;
		this.totalEntregadas = totalEntregadas;
	}

	public LocalDate obtenerFechaInicio() { return fechaInicio; }
	public LocalDate obtenerFechaFin() { return fechaFin; }
	public List<DiaCalendario> obtenerDias() { return dias; }
	public int obtenerTotalReservas() { return totalReservas; }
	public int obtenerTotalProgramadas() { return totalProgramadas; }
	public int obtenerTotalEnProgreso() { return totalEnProgreso; }
	public int obtenerTotalFinalizadas() { return totalFinalizadas; }
	public int obtenerTotalEntregadas() { return totalEntregadas; }
}
