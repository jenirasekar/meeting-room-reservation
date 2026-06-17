package com.meetingroom.servlet;

import com.meetingroom.dao.EquipmentDAO;
import com.meetingroom.dao.ReservationDAO;
import com.meetingroom.dao.RoomDAO;
import com.meetingroom.model.Equipment;
import com.meetingroom.model.Reservation;
import com.meetingroom.model.Room;
import com.meetingroom.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/rooms/*")
public class RoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();
    private final EquipmentDAO equipmentDAO = new EquipmentDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/rooms
            String capacity = req.getParameter("capacity");
            String status = req.getParameter("status");
            List<Room> rooms = roomDAO.findAll(capacity, status);
            resp.getWriter().write(JsonUtil.success("OK", rooms));

        } else if (pathInfo.matches("/\\d+/availability")) {
            // GET /api/rooms/{id}/availability?date=YYYY-MM-DD  (Bug 2 fix)
            String[] parts = pathInfo.split("/");
            int roomId;
            try {
                roomId = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Invalid room ID"));
                return;
            }

            String dateParam = req.getParameter("date");
            if (dateParam == null || dateParam.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Date parameter is required (YYYY-MM-DD)"));
                return;
            }

            try {
                LocalDate date = LocalDate.parse(dateParam);
                List<Reservation> slots = reservationDAO.findBookedSlots(roomId, date);
                resp.getWriter().write(JsonUtil.success("OK", slots));
            } catch (Exception e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Invalid date format. Use YYYY-MM-DD"));
            }

        } else {
            // GET /api/rooms/{id}
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Room room = roomDAO.findById(id);
                if (room == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Room not found"));
                    return;
                }
                List<Equipment> equipment = roomDAO.getEquipmentByRoomId(id);
                Map<String, Object> data = new HashMap<>();
                data.put("room", room);
                data.put("equipment", equipment);
                resp.getWriter().write(JsonUtil.success("OK", data));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Invalid room ID"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if (pathInfo != null && pathInfo.matches("/\\d+/equipment")) {
            handleAddEquipment(req, resp, pathInfo);
            return;
        }

        String body = JsonUtil.parseBody(req.getReader());
        Room room = JsonUtil.fromJson(body, Room.class);

        if (room.getName() == null || room.getName().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Room name is required"));
            return;
        }
        if (room.getCapacity() <= 0) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Capacity must be greater than 0"));
            return;
        }

        if (roomDAO.create(room)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.success("Room created successfully", room));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("Failed to create room"));
        }
    }

    private void handleAddEquipment(HttpServletRequest req, HttpServletResponse resp, String pathInfo)
            throws IOException {
        try {
            int roomId = Integer.parseInt(pathInfo.split("/")[1]);

            if (roomDAO.findById(roomId) == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Room not found"));
                return;
            }

            String body = JsonUtil.parseBody(req.getReader());
            Equipment equip = JsonUtil.fromJson(body, Equipment.class);
            equip.setRoomId(roomId);

            if (equip.getName() == null || equip.getName().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Equipment name is required"));
                return;
            }

            if (equipmentDAO.create(equip)) {
                resp.setStatus(HttpServletResponse.SC_CREATED);
                resp.getWriter().write(JsonUtil.success("Equipment added", equip));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Failed to add equipment"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid room ID"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Room ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            Room existing = roomDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Room not found"));
                return;
            }

            String body = JsonUtil.parseBody(req.getReader());
            Room input = JsonUtil.fromJson(body, Room.class);
            input.setId(id);
            if (input.getName() == null) input.setName(existing.getName());
            if (input.getStatus() == null) input.setStatus(existing.getStatus());
            if (input.getCapacity() == 0) input.setCapacity(existing.getCapacity());

            if (roomDAO.update(input)) {
                resp.getWriter().write(JsonUtil.success("Room updated successfully"));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Failed to update room"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid room ID"));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Room ID is required"));
            return;
        }

        try {
            int id = Integer.parseInt(pathInfo.substring(1));
            if (roomDAO.delete(id)) {
                resp.getWriter().write(JsonUtil.success("Room deleted successfully"));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Room not found"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid room ID"));
        }
    }
}
