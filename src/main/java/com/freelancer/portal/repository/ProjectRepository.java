package com.freelancer.portal.repository;

import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.Project;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Page<Project> findByFreelancer(User freelancer, Pageable pageable);
    Page<Project> findByClient(Client client, Pageable pageable);
    Optional<Project> findByIdAndFreelancer(Long id, User freelancer);
    Optional<Project> findByIdAndClient(Long id, Client client);
    List<Project> findByClientAndFreelancer(Client client, User freelancer);
    long countByClient(Client client);
    
    // Added methods
    boolean existsByIdAndFreelancer(Long id, User freelancer);
    List<Project> findByFreelancer(User freelancer);

    List<Project> findByClient(Client client);
}