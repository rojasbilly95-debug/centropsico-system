package com.centropsicologico.sistema.service;

import com.centropsicologico.sistema.entity.User;

import java.util.List;

public interface UserService {

    User save(User user);

    List<User> findAll();

    User findById(Long id);

    User update(Long id, User user);

    User toggleStatus(Long id); // 👈 NUEVO

    void delete(Long id);
}