package com.bcttg.module.note.controller;

import java.util.List;

import com.bcttg.common.ApiResponse;
import com.bcttg.common.PageMeta;
import com.bcttg.common.PageRequestUtil;
import com.bcttg.module.note.dto.CreatePersonalNoteRequest;
import com.bcttg.module.note.dto.NoteFlagRequest;
import com.bcttg.module.note.dto.PersonalNoteResponse;
import com.bcttg.module.note.dto.UpdatePersonalNoteRequest;
import com.bcttg.module.note.entity.PersonalNote;
import com.bcttg.module.note.service.PersonalNoteService;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notes")
@Tag(name = "Personal Notes", description = "Ghi chu ca nhan theo tung tai khoan")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER','USER')")
public class PersonalNoteController {
    private final PersonalNoteService service;

    public PersonalNoteController(PersonalNoteService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<List<PersonalNoteResponse>> list(
        @RequestParam(required = false) String q,
        @RequestParam(required = false, name = "is_archived") Boolean isArchived,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false, name = "page_size") Integer pageSize,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String order,
        Authentication authentication
    ) {
        Pageable pageable = PageRequestUtil.build(page, pageSize, sort, order, Sort.by("isPinned").descending().and(Sort.by("updatedAt").descending()));
        Page<PersonalNote> result = service.findMine(getPhone(authentication), q, isArchived, pageable);
        List<PersonalNoteResponse> data = result.map(PersonalNoteResponse::new).toList();
        return ApiResponse.success(data, PageMeta.from(result));
    }

    @PostMapping
    public ApiResponse<PersonalNoteResponse> create(@Valid @RequestBody CreatePersonalNoteRequest request, Authentication authentication) {
        return ApiResponse.success(new PersonalNoteResponse(service.create(getPhone(authentication), request)));
    }

    @GetMapping("/{id}")
    public ApiResponse<PersonalNoteResponse> get(@PathVariable Long id, Authentication authentication) {
        return ApiResponse.success(new PersonalNoteResponse(service.getMineById(getPhone(authentication), id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<PersonalNoteResponse> update(@PathVariable Long id, @Valid @RequestBody UpdatePersonalNoteRequest request, Authentication authentication) {
        return ApiResponse.success(new PersonalNoteResponse(service.update(getPhone(authentication), id, request)));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id, Authentication authentication) {
        service.delete(getPhone(authentication), id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/{id}/pin")
    public ApiResponse<PersonalNoteResponse> updatePin(@PathVariable Long id, @Valid @RequestBody NoteFlagRequest request, Authentication authentication) {
        return ApiResponse.success(new PersonalNoteResponse(service.updatePinned(getPhone(authentication), id, request.getValue())));
    }

    @PatchMapping("/{id}/archive")
    public ApiResponse<PersonalNoteResponse> updateArchive(@PathVariable Long id, @Valid @RequestBody NoteFlagRequest request, Authentication authentication) {
        return ApiResponse.success(new PersonalNoteResponse(service.updateArchived(getPhone(authentication), id, request.getValue())));
    }

    private String getPhone(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}
