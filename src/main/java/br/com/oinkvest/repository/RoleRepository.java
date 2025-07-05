package br.com.oinkvest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.oinkvest.model.Role;


public interface RoleRepository extends JpaRepository<Role,Long>{
    Role findByName(String name);
    
}
