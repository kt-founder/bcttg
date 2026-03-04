package com.bcttg.module.note.service;

import java.util.Locale;
import java.util.Optional;

import com.bcttg.common.ApiException;
import com.bcttg.common.ErrorCode;
import com.bcttg.module.note.dto.CreatePersonalNoteRequest;
import com.bcttg.module.note.dto.UpdatePersonalNoteRequest;
import com.bcttg.module.note.entity.PersonalNote;
import com.bcttg.module.note.repository.PersonalNoteRepository;
import com.bcttg.module.user.entity.UserAccount;
import com.bcttg.module.user.repository.UserAccountRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonalNoteService {
    private final PersonalNoteRepository repository;
    private final UserAccountRepository userAccountRepository;

    public PersonalNoteService(PersonalNoteRepository repository, UserAccountRepository userAccountRepository) {
        this.repository = repository;
        this.userAccountRepository = userAccountRepository;
    }

    public Page<PersonalNote> findMine(String phone, String q, Boolean isArchived, Pageable pageable) {
        UserAccount owner = getOwnerByPhone(phone);
        Specification<PersonalNote> spec = Specification.<PersonalNote>where(
            (root, query, cb) -> cb.isNull(root.get("deletedAt"))
        ).and((root, query, cb) -> cb.equal(root.get("ownerUser").get("id"), owner.getId()));

        if (q != null && !q.isBlank()) {
            String like = "%" + q.toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("title")), like),
                cb.like(cb.lower(root.get("content")), like)
            ));
        }
        if (isArchived != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("isArchived"), isArchived));
        }
        return repository.findAll(spec, pageable);
    }

    public PersonalNote getMineById(String phone, Long id) {
        UserAccount owner = getOwnerByPhone(phone);
        PersonalNote note = repository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Personal note not found"));
        if (note.getDeletedAt() != null || note.getOwnerUser() == null || !note.getOwnerUser().getId().equals(owner.getId())) {
            throw new ApiException(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, "Personal note not found");
        }
        return note;
    }

    @Transactional
    public PersonalNote create(String phone, CreatePersonalNoteRequest request) {
        UserAccount owner = getOwnerByPhone(phone);
        PersonalNote note = new PersonalNote();
        note.setOwnerUser(owner);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setColorCode(request.getColorCode());
        note.setReminderAt(request.getReminderAt());
        note.setIsPinned(Optional.ofNullable(request.getIsPinned()).orElse(false));
        note.setIsArchived(false);
        return repository.save(note);
    }

    @Transactional
    public PersonalNote update(String phone, Long id, UpdatePersonalNoteRequest request) {
        PersonalNote note = getMineById(phone, id);
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setColorCode(request.getColorCode());
        note.setReminderAt(request.getReminderAt());
        note.setIsPinned(request.getIsPinned());
        note.setIsArchived(request.getIsArchived());
        return repository.save(note);
    }

    @Transactional
    public void delete(String phone, Long id) {
        PersonalNote note = getMineById(phone, id);
        repository.delete(note);
    }

    @Transactional
    public PersonalNote updatePinned(String phone, Long id, boolean value) {
        PersonalNote note = getMineById(phone, id);
        note.setIsPinned(value);
        return repository.save(note);
    }

    @Transactional
    public PersonalNote updateArchived(String phone, Long id, boolean value) {
        PersonalNote note = getMineById(phone, id);
        note.setIsArchived(value);
        return repository.save(note);
    }

    private UserAccount getOwnerByPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        return userAccountRepository.findByPhoneAndDeletedAtIsNull(phone)
            .orElseThrow(() -> new ApiException(ErrorCode.UNAUTHORIZED, HttpStatus.UNAUTHORIZED, "Unauthorized"));
    }
}
