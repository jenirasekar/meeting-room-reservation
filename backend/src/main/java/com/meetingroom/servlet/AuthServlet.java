package com.meetingroom.servlet;

import com.meetingroom.dao.UserDAO;
import com.meetingroom.model.User;
import com.meetingroom.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        if (path == null) path = "/";

        switch (path) {
            case "/register":
                handleRegister(req, resp);
                break;
            case "/login":
                handleLogin(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            default:
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Endpoint not found"));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        if ("/me".equals(path)) {
            handleMe(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.error("Endpoint not found"));
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = JsonUtil.parseBody(req.getReader());
        User input = JsonUtil.fromJson(body, User.class);

        if (input.getUsername() == null || input.getUsername().isEmpty() ||
            input.getPassword() == null || input.getPassword().isEmpty() ||
            input.getEmail() == null || input.getEmail().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Username, password, and email are required"));
            return;
        }

        // Check if username or email already exists
        if (userDAO.findByUsername(input.getUsername()) != null) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(JsonUtil.error("Username already taken"));
            return;
        }
        if (userDAO.findByEmail(input.getEmail()) != null) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(JsonUtil.error("Email already registered"));
            return;
        }

        // Hash password
        String hashed = BCrypt.hashpw(input.getPassword(), BCrypt.gensalt(12));
        input.setPassword(hashed);
        input.setRole("user"); // Default role

        if (userDAO.create(input)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.success("Registration successful"));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("Registration failed"));
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = JsonUtil.parseBody(req.getReader());
        User input = JsonUtil.fromJson(body, User.class);

        if (input.getUsername() == null || input.getPassword() == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Username and password are required"));
            return;
        }

        User user = userDAO.findByUsername(input.getUsername());
        if (user == null || !BCrypt.checkpw(input.getPassword(), user.getPassword())) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Invalid username or password"));
            return;
        }

        // Create session
        HttpSession session = req.getSession(true);
        session.setAttribute("user", user);
        session.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Return user info (without password)
        user.setPassword(null);
        resp.getWriter().write(JsonUtil.success("Login successful", user));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        resp.getWriter().write(JsonUtil.success("Logged out successfully"));
    }

    private void handleMe(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Not authenticated"));
            return;
        }

        User user = (User) session.getAttribute("user");
        // Re-fetch from DB to get fresh data
        User fresh = userDAO.findById(user.getId());
        if (fresh != null) {
            fresh.setPassword(null);
            resp.getWriter().write(JsonUtil.success("OK", fresh));
        } else {
            resp.getWriter().write(JsonUtil.success("OK", user));
        }
    }
}
