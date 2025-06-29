package com.example.clb.projecttracker.mapper;

import java.util.List;

/**
 * Base mapper interface for entity to DTO mapping
 *
 * @param <D> - DTO type
 * @param <E> - Entity type
 */
public interface EntityMapper<D, E> {

    E toEntity(D dto);
    D toDto(E entity);
    List<E> toEntity(List<D> dtoList);
    List<D> toDto(List<E> entityList);
} 