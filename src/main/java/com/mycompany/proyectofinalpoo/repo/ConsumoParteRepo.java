package com.mycompany.proyectofinalpoo.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.mycompany.proyectofinalpoo.ConsumoParte;

public interface ConsumoParteRepo {
    List<ConsumoParte> findByFechaBetween(LocalDate inicio, LocalDate fin);
    List<ConsumoParte> findByReservaId(String reservaId);
    Optional<ConsumoParte> findById(String id);
    void save(ConsumoParte c);
    void saveAll(List<ConsumoParte> lista);
    void deleteByReservaId(String reservaId);
}
