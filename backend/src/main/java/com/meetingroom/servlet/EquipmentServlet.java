package com.meetingroom.servlet;

import com.meetingroom.dao.EquipmentDAO;
import com.meetingroom.dao.RoomDAO;
import com.meetingroom.model.Equipment;
import com.meetingroom.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/equipment/*")
public class EquipmentServlet extends HttpServlet {

    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // POST /api/equipment — body must contain room_id
        String body = JsonUtil.parseBody(req.getReader());
        Equipment equip = JsonUtil.fromJson(body, Equipment.class);

        if (equip.getRoomId() <= 0 || equip.getName() == null || equip.getName().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("room_id and name are required"));
            return;
        }

        // Verify room exists
        if (roomDAO.findById(equip.getRoomId()) == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.error("Room not found"));
            return;
        }

        if (equipmentDAO.create(equip)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.success("Equipment added", equip));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("Failed to add equipment"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // PUT /api/equipment/{id}
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Equipment ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Equipment existing = equipmentDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Equipment not found"));
                return;
            }

            String body = JsonUtil.parseBody(req.getReader());
            Equipment input = JsonUtil.fromJson(body, Equipment.class);
            input.setId(id);

            if (equipmentDAO.update(input)) {
                resp.getWriter().write(JsonUtil.success("Equipment updated"));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Failed to update equipment"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid equipment ID"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        // DELETE /api/equipment/{id}
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Equipment ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            if (equipmentDAO.delete(id)) {
                resp.getWriter().write(JsonUtil.success("Equipment deleted"));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Equipment not found"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid equipment ID"));
        }
    }
}
