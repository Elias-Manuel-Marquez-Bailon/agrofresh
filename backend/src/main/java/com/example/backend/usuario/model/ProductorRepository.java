package com.example.backend.usuario.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductorRepository extends JpaRepository<Productor, Long> {

    Optional<Productor> findByUsuarioId(Long usuarioId);
}
