package com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.admin;

import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.forsoftwaredevelopers.audio_stream_api.application.command.ListAccessFilterCommand;
import com.forsoftwaredevelopers.audio_stream_api.application.command.PagedResponse;
import com.forsoftwaredevelopers.audio_stream_api.application.service.AccessFilterService;
import com.forsoftwaredevelopers.audio_stream_api.application.usecase.AccessFilterUseCases;
import com.forsoftwaredevelopers.audio_stream_api.domain.model.AccessFilter;
import com.forsoftwaredevelopers.audio_stream_api.infraestructure.web.result.ResultHttpMapper;

@RestController
@RequestMapping("api/admin/access-filters")
public class AccessFilterController {

    private final AccessFilterService accessFilterService;
    private final ResultHttpMapper resultHttpMapper;

    public AccessFilterController(AccessFilterService accessFilterService, ResultHttpMapper resultHttpMapper) {
        this.accessFilterService = accessFilterService;
        this.resultHttpMapper = resultHttpMapper;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<AccessFilter>> list(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String ip,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        
        var command = ListAccessFilterCommand.of(username, email, ip, status, startDate, endDate, page, size);
        return ResponseEntity.ok(accessFilterService.list(command));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccessFilter> getById(@PathVariable String id) {
        var accessFilter = accessFilterService.getById(id);
        if (accessFilter == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(accessFilter);
    }

    @PostMapping
    public ResponseEntity<?> create(AccessFilterUseCases.CreateAccessFilterCommand command) {
        var result = accessFilterService.create(command);
        return resultHttpMapper.toResponse(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable String id, AccessFilterUseCases.UpdateAccessFilterCommand command) {
        var result = accessFilterService.update(new AccessFilterUseCases.UpdateAccessFilterCommand(id, command.status()));
        return resultHttpMapper.toResponse(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id) {
        var result = accessFilterService.delete(new AccessFilterUseCases.DeleteAccessFilterCommand(id));
        return resultHttpMapper.toResponse(result);
    }
}