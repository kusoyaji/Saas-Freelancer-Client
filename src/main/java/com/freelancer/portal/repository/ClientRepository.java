package com.freelancer.portal.repository;

import com.freelancer.portal.model.Client;
import com.freelancer.portal.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Page<Client> findByFreelancer(User freelancer, Pageable pageable);
    List<Client> findByFreelancer(User freelancer);
    Optional<Client> findByIdAndFreelancer(Long id, User freelancer);
    boolean existsByEmailAndFreelancer(String email, User freelancer);

    Page<Client> findAllByFreelancer(User currentUser, Pageable pageable);
}