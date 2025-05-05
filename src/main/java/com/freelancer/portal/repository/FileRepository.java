package com.freelancer.portal.repository;

import com.freelancer.portal.model.File;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for File entities.
 */
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    
    /**
     * Find all files by owner with pagination.
     *
     * @param owner the owner of the files
     * @param pageable pagination information
     * @return a page of files
     */
    Page<File> findAllByOwner(User owner, Pageable pageable);
    
    /**
     * Find all files by project with pagination.
     *
     * @param project the project
     * @param pageable pagination information
     * @return a page of files
     */
    Page<File> findAllByProject(Project project, Pageable pageable);
    
    /**
     * Find all files by project.
     *
     * @param project the project
     * @return a list of files
     */
    List<File> findAllByProject(Project project);
    
    /**
     * Find a file by id and project.
     *
     * @param id the id of the file
     * @param project the project
     * @return an optional file
     */
    Optional<File> findByIdAndProject(Long id, Project project);

    List<File> findByProjectId(Long id);
}