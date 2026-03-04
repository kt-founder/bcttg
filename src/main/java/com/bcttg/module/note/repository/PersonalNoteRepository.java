package com.bcttg.module.note.repository;

import java.util.Optional;

import com.bcttg.module.note.entity.PersonalNote;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PersonalNoteRepository extends JpaRepository<PersonalNote, Long>, JpaSpecificationExecutor<PersonalNote> {
    @EntityGraph(attributePaths = {"ownerUser"})
    Page<PersonalNote> findAll(Specification<PersonalNote> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"ownerUser"})
    Optional<PersonalNote> findById(Long id);
}
