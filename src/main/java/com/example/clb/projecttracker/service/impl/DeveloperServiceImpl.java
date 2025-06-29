package com.example.clb.projecttracker.service.impl;

import com.example.clb.projecttracker.document.enums.ActionType;
import com.example.clb.projecttracker.dto.DeveloperDto;
import com.example.clb.projecttracker.dto.DeveloperRequestDto;
import com.example.clb.projecttracker.dto.DeveloperPerformanceDto;
import com.example.clb.projecttracker.exception.DuplicateResourceException;
import com.example.clb.projecttracker.exception.ResourceNotFoundException;
import com.example.clb.projecttracker.model.Developer;
import com.example.clb.projecttracker.repository.DeveloperRepository;
import com.example.clb.projecttracker.service.AuditLogService;
import com.example.clb.projecttracker.service.DeveloperService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeveloperServiceImpl implements DeveloperService {

    private final DeveloperRepository developerRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    @CacheEvict(value = "developersPage", allEntries = true)
    public DeveloperDto createDeveloper(DeveloperRequestDto developerRequestDto) {
        developerRepository.findByEmail(developerRequestDto.getEmail()).ifPresent(d -> {
            throw new DuplicateResourceException("Developer", "email", developerRequestDto.getEmail());
        });

        Developer developer = mapToEntity(developerRequestDto);
        Developer savedDeveloper = developerRepository.save(developer);
        // Log action
        auditLogService.logAction("Developer", savedDeveloper.getId(), ActionType.CREATED, "SYSTEM", "Developer created: " + savedDeveloper.getName());
        return mapToDto(savedDeveloper);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developers", key = "#developerId")
    public DeveloperDto getDeveloperById(Long developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", developerId));
        return mapToDto(developer);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "developersPage") // Key will be generated based on Pageable
    public Page<DeveloperDto> getAllDevelopers(Pageable pageable) {
        Page<Developer> developers = developerRepository.findAll(pageable);
        return developers.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "topDevelopers", key = "#limit")
    public List<DeveloperPerformanceDto> getTopDevelopersByCompletedTasks(int limit) {
        if (limit <= 0) {
            return List.of(); // Or throw an IllegalArgumentException
        }
        Pageable pageable = PageRequest.of(0, limit);
        return developerRepository.findTopDevelopersByCompletedTasks(pageable);
    }

    @Override
    @Transactional
    @Caching(put = {
        @CachePut(value = "developers", key = "#developerId")
    }, evict = {
        @CacheEvict(value = "developersPage", allEntries = true),
        @CacheEvict(value = "topDevelopers", allEntries = true)
    })
    public DeveloperDto updateDeveloper(Long developerId, DeveloperRequestDto developerRequestDto) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", developerId));

        // Check if the new email conflicts with another existing developer
        developerRepository.findByEmail(developerRequestDto.getEmail()).ifPresent(existingDeveloper -> {
            if (!existingDeveloper.getId().equals(developerId)) {
                throw new DuplicateResourceException("Developer", "email", developerRequestDto.getEmail());
            }
        });

        developer.setName(developerRequestDto.getName());
        developer.setEmail(developerRequestDto.getEmail());
        developer.setSkills(developerRequestDto.getSkills());

        Developer updatedDeveloper = developerRepository.save(developer);
        // Log action
        auditLogService.logAction("Developer", updatedDeveloper.getId(), ActionType.UPDATED, "SYSTEM", "Developer updated: " + updatedDeveloper.getName());
        return mapToDto(updatedDeveloper);
    }

    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "developers", key = "#developerId"),
        @CacheEvict(value = "developersPage", allEntries = true),
        @CacheEvict(value = "topDevelopers", allEntries = true)
    })
    public void deleteDeveloper(Long developerId) {
        Developer developer = developerRepository.findById(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer", "id", developerId));
        
        // Disassociate tasks before deletion
        developer.getAssignedTasks().forEach(task -> task.setDeveloper(null));
        developerRepository.save(developer); // Save changes to tasks (disassociation)

        // Log action before deletion
        auditLogService.logAction("Developer", developerId, ActionType.DELETED, "SYSTEM", "Developer deleted: " + developer.getName());
        developerRepository.deleteById(developerId);
    }

    // --- Helper Mapper Methods ---
    private DeveloperDto mapToDto(Developer developer) {
        DeveloperDto dto = new DeveloperDto();
        dto.setId(developer.getId());
        dto.setName(developer.getName());
        dto.setEmail(developer.getEmail());
        dto.setSkills(developer.getSkills());
        return dto;
    }

    private Developer mapToEntity(DeveloperRequestDto dto) {
        Developer developer = new Developer();
        developer.setName(dto.getName());
        developer.setEmail(dto.getEmail());
        developer.setSkills(dto.getSkills());
        return developer;
    }
}
