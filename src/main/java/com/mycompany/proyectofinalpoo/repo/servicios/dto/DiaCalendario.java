/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.proyectofinalpoo.repo.servicios.dto;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.mycompany.proyectofinalpoo.Reserva;

public class DiaCalendario {
	private final LocalDate fecha;
	private final List<Reserva> reservas;
	private final int totalProgramadas;
	private final int totalEnProgreso;
	private final int totalFinalizadas;
	private final int totalEntregadas;

	public DiaCalendario(LocalDate fecha, List<Reserva> reservas, int totalProgramadas, int totalEnProgreso, int totalFinalizadas, int totalEntregadas) {
		this.fecha = fecha;
		this.reservas = reservas == null ? Collections.emptyList() : Collections.unmodifiableList(reservas);
		this.totalProgramadas = totalProgramadas;
		this.totalEnProgreso = totalEnProgreso;
		this.totalFinalizadas = totalFinalizadas;
		this.totalEntregadas = totalEntregadas;
	}

	public LocalDate obtenerFecha() { return fecha; }
	public List<Reserva> obtenerReservas() { return reservas; }
	public int obtenerTotalProgramadas() { return totalProgramadas; }
	public int obtenerTotalEnProgreso() { return totalEnProgreso; }
	public int obtenerTotalFinalizadas() { return totalFinalizadas; }
	public int obtenerTotalEntregadas() { return totalEntregadas; }
}
