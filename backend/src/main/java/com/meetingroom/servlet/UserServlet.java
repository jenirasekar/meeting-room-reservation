package com.meetingroom.servlet;

import com.meetingroom.dao.UserDAO;
import com.meetingroom.model.User;
import com.meetingroom.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/admin/users/*")
public class UserServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/admin/users — list all
            List<User> users = userDAO.findAll();
            // Strip passwords
            for (User u : users) u.setPassword(null);
            resp.getWriter().write(JsonUtil.success("OK", users));
        } else {
            // GET /api/admin/users/{id}
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                User user = userDAO.findById(id);
                if (user == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("User not found"));
                    return;
                }
                user.setPassword(null);
                resp.getWriter().write(JsonUtil.success("OK", user));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Invalid user ID"));
            }
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("User ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            User existing = userDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("User not found"));
                return;
            }

            String body = JsonUtil.parseBody(req.getReader());
            JsonObject json = JsonUtil.parseJson(body);

            String email = json.has("email") ? json.get("email").getAsString() : null;
            String role = json.has("role") ? json.get("role").getAsString() : null;

            // Validate role
            if (role != null && !role.equals("admin") && !role.equals("user")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Role must be 'admin' or 'user'"));
                return;
            }

            // Build updated user
            existing.setPassword(null); // never expose
            if (email != null && !email.isEmpty()) existing.setEmail(email);
            if (role != null) existing.setRole(role);

            if (userDAO.update(existing)) {
                resp.getWriter().write(JsonUtil.success("User updated", existing));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Failed to update user"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid user ID"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("User ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));

            User existing = userDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("User not found"));
                return;
            }

            // Prevent self-deletion
            HttpSession session = req.getSession(false);
            User currentUser = (session != null) ? (User) session.getAttribute("user") : null;
            if (currentUser != null && currentUser.getId() == id) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(JsonUtil.error("You cannot delete your own account"));
                return;
            }

            if (userDAO.delete(id)) {
                resp.getWriter().write(JsonUtil.success("User deleted successfully"));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Failed to delete user"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid user ID"));
        }
    }
}
