package com.centropsicologico.sistema.service.impl;

import com.centropsicologico.sistema.entity.User;
import com.centropsicologico.sistema.exception.BusinessRuleException;
import com.centropsicologico.sistema.exception.ResourceNotFoundException;
import com.centropsicologico.sistema.repository.UserRepository;
import com.centropsicologico.sistema.service.NotificationService;
import com.centropsicologico.sistema.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Override
    public User save(User user) {
        validateUserData(user, true);

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessRuleException("Ya existe un usuario con ese correo");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        User saved = userRepository.save(user);

        notificationService.createForRole(
                "Usuario registrado",
                "Se registró el usuario " + getFullName(saved) + " con rol " + saved.getRole() + ".",
                "USUARIO_CREADO",
                "ADMIN"
        );

        return saved;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    public User update(Long id, User user) {
        User currentUser = findById(id);

        validateUserData(user, false);

        if (!currentUser.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessRuleException("Ya existe otro usuario con ese correo");
        }

        currentUser.setFirstName(user.getFirstName());
        currentUser.setLastName(user.getLastName());
        currentUser.setEmail(user.getEmail());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        currentUser.setRole(user.getRole());

        if (user.getActive() != null) {
            currentUser.setActive(user.getActive());
        }

        User updated = userRepository.save(currentUser);

        notificationService.createForRole(
                "Usuario actualizado",
                "Se actualizaron los datos del usuario " + getFullName(updated) + ".",
                "USUARIO_EDITADO",
                "ADMIN"
        );

        return updated;
    }

    @Override
    public void delete(Long id) {
        User user = findById(id);
        user.setActive(false);

        userRepository.save(user);

        notificationService.createForRole(
                "Usuario desactivado",
                "El usuario " + getFullName(user) + " fue desactivado.",
                "USUARIO_ELIMINADO",
                "ADMIN"
        );
    }

    @Override
    public User toggleStatus(Long id) {
        User user = findById(id);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));

        User updated = userRepository.save(user);

        String status = Boolean.TRUE.equals(updated.getActive()) ? "activado" : "desactivado";

        notificationService.createForRole(
                "Estado de usuario modificado",
                "El usuario " + getFullName(updated) + " fue " + status + ".",
                "USUARIO_ESTADO",
                "ADMIN"
        );

        return updated;
    }

    private void validateUserData(User user, boolean requirePassword) {
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new BusinessRuleException("Los nombres del usuario son obligatorios");
        }

        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new BusinessRuleException("Los apellidos del usuario son obligatorios");
        }

        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new BusinessRuleException("El correo del usuario es obligatorio");
        }

        if (requirePassword && (user.getPassword() == null || user.getPassword().isBlank())) {
            throw new BusinessRuleException("La contraseña del usuario es obligatoria");
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new BusinessRuleException("El rol del usuario es obligatorio");
        }
    }

    private String getFullName(User user) {
        return (user.getFirstName() + " " + user.getLastName()).trim();
    }
}