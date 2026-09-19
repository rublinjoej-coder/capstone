package com.rublin.rublinmart.service;

import com.rublin.rublinmart.dao.UserDAO;
import com.rublin.rublinmart.dto.UserResponseDTO;
import java.util.List;
import java.util.stream.Collectors;

public class AdminService {

    private final UserDAO userDAO;

    public AdminService() {
        this.userDAO = new UserDAO();
    }

    public AdminService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public List<UserResponseDTO> getAllUsers() {
        return userDAO.findAll().stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<UserResponseDTO> getUsersByRole(String role) {
        return userDAO.findByRole(role).stream()
                .map(UserResponseDTO::new)
                .collect(Collectors.toList());
    }
}
