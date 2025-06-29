package com.example.clb.projecttracker.controller;

import com.example.clb.projecttracker.dto.DeveloperDto;
import com.example.clb.projecttracker.dto.DeveloperRequestDto;
import com.example.clb.projecttracker.dto.DeveloperPerformanceDto;
import com.example.clb.projecttracker.service.DeveloperService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/developers")
@RequiredArgsConstructor
public class DeveloperController {

    private final DeveloperService developerService;

    @PostMapping("/create")
    public ResponseEntity<DeveloperDto> createDeveloper(@Valid @RequestBody DeveloperRequestDto developerRequestDto) {
        DeveloperDto createdDeveloper = developerService.createDeveloper(developerRequestDto);
        return ResponseEntity.created(URI.create("/api/v1/developers/" + createdDeveloper.getId()))
                .body(createdDeveloper);
    }

    @GetMapping("/{developerId}")
    public ResponseEntity<DeveloperDto> getDeveloperById(@PathVariable Long developerId) {
        DeveloperDto developerDto = developerService.getDeveloperById(developerId);
        return ResponseEntity.ok(developerDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<DeveloperDto>> getAllDevelopers(
            @PageableDefault(size = 20, sort = "name,asc") Pageable pageable) {
        Page<DeveloperDto> developers = developerService.getAllDevelopers(pageable);
        return ResponseEntity.ok(developers);
    }

    @GetMapping("/top")
    public ResponseEntity<List<DeveloperPerformanceDto>> getTopDevelopers(
            @RequestParam(defaultValue = "5") int limit) {
        List<DeveloperPerformanceDto> topDevelopers = developerService.getTopDevelopersByCompletedTasks(limit);
        return ResponseEntity.ok(topDevelopers);
    }

    @PutMapping("/{developerId}")
    public ResponseEntity<DeveloperDto> updateDeveloper(@PathVariable Long developerId,
                                                      @Valid @RequestBody DeveloperRequestDto developerRequestDto) {
        DeveloperDto updatedDeveloper = developerService.updateDeveloper(developerId, developerRequestDto);
        return ResponseEntity.ok(updatedDeveloper);
    }

    @DeleteMapping("/{developerId}")
    public ResponseEntity<Void> deleteDeveloper(@PathVariable Long developerId) {
        developerService.deleteDeveloper(developerId);
        return ResponseEntity.noContent().build();
    }
}
