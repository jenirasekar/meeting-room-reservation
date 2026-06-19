package com.meetingroom.servlet;

import com.meetingroom.dao.ReservationDAO;
import com.meetingroom.dao.ReservationLogDAO;
import com.meetingroom.dao.RoomDAO;
import com.meetingroom.model.Reservation;
import com.meetingroom.model.Room;
import com.meetingroom.model.User;
import com.meetingroom.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/api/reservations/*")
public class ReservationServlet extends HttpServlet {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ReservationLogDAO logDAO = new ReservationLogDAO();
    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Authentication required"));
            return;
        }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            // GET /api/reservations
            Integer userId = null;
            if (!"admin".equals(user.getRole())) {
                userId = user.getId(); // Regular users only see their own
            }
            String status = req.getParameter("status");
            List<Reservation> list = reservationDAO.findAll(userId, status);
            resp.getWriter().write(JsonUtil.success("OK", list));
        } else {
            // GET /api/reservations/{id}
            try {
                int id = Integer.parseInt(pathInfo.substring(1));
                Reservation res = reservationDAO.findById(id);
                if (res == null) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write(JsonUtil.error("Reservation not found"));
                    return;
                }
                // Check ownership (regular users can only see their own)
                if (!"admin".equals(user.getRole()) && res.getUserId() != user.getId()) {
                    resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    resp.getWriter().write(JsonUtil.error("Access denied"));
                    return;
                }
                resp.getWriter().write(JsonUtil.success("OK", res));
            } catch (NumberFormatException e) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Invalid reservation ID"));
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Authentication required"));
            return;
        }

        String body = JsonUtil.parseBody(req.getReader());
        JsonObject json = JsonUtil.parseJson(body);

        int roomId = json.has("room_id") ? json.get("room_id").getAsInt() : 0;
        String title = json.has("title") ? json.get("title").getAsString() : "";
        String startTimeStr = json.has("start_time") ? json.get("start_time").getAsString() : null;
        String endTimeStr = json.has("end_time") ? json.get("end_time").getAsString() : null;

        if (roomId <= 0 || title.isEmpty() || startTimeStr == null || endTimeStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("room_id, title, start_time, and end_time are required"));
            return;
        }

        // Validate room exists and is available
        Room room = roomDAO.findById(roomId);
        if (room == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write(JsonUtil.error("Room not found"));
            return;
        }
        if ("unavailable".equals(room.getStatus())) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(JsonUtil.error("Room is currently unavailable"));
            return;
        }

        LocalDateTime startTime = JsonUtil.parseDateTime(startTimeStr);
        LocalDateTime endTime = JsonUtil.parseDateTime(endTimeStr);

        if (startTime == null || endTime == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid date format. Use ISO format: yyyy-MM-ddTHH:mm:ss"));
            return;
        }

        if (!endTime.isAfter(startTime)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("End time must be after start time"));
            return;
        }

        // Start time must be in the future
        if (startTime.isBefore(LocalDateTime.now())) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Cannot book a time that has already passed"));
            return;
        }

        // Check for conflicts
        List<Reservation> conflicts = reservationDAO.findConflicts(roomId, startTime, endTime, null);
        if (!conflicts.isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(JsonUtil.error("Time slot is already booked"));
            return;
        }

        Reservation res = new Reservation();
        res.setUserId(user.getId());
        res.setRoomId(roomId);
        res.setTitle(title);
        res.setStartTime(startTime);
        res.setEndTime(endTime);
        res.setStatus("pending");

        if (reservationDAO.create(res)) {
            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write(JsonUtil.success("Reservation created successfully", res));
        } else {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("Failed to create reservation"));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;
        if (user == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.error("Authentication required"));
            return;
        }

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Action is required"));
            return;
        }

        String[] segments = pathInfo.split("/");
        if (segments.length < 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid path"));
            return;
        }

        try {
            int reservationId = Integer.parseInt(segments[1]);
            String action = segments[2]; // approve, reject, cancel

            Reservation res = reservationDAO.findById(reservationId);
            if (res == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Reservation not found"));
                return;
            }

            String note = "";
            try {
                String body = JsonUtil.parseBody(req.getReader());
                JsonObject json = JsonUtil.parseJson(body);
                note = json.has("note") ? json.get("note").getAsString() : "";
            } catch (Exception ignored) {}

            switch (action) {
                case "approve":
                    if (!"admin".equals(user.getRole())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(JsonUtil.error("Only admins can approve"));
                        return;
                    }
                    if (!"pending".equals(res.getStatus())) {
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        resp.getWriter().write(JsonUtil.error("Can only approve pending reservations"));
                        return;
                    }
                    reservationDAO.updateStatus(reservationId, "approved");
                    logDAO.create(reservationId, user.getId(), "approved", note);
                    resp.getWriter().write(JsonUtil.success("Reservation approved"));
                    break;

                case "reject":
                    if (!"admin".equals(user.getRole())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(JsonUtil.error("Only admins can reject"));
                        return;
                    }
                    if (!"pending".equals(res.getStatus())) {
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        resp.getWriter().write(JsonUtil.error("Can only reject pending reservations"));
                        return;
                    }
                    reservationDAO.updateStatus(reservationId, "rejected");
                    logDAO.create(reservationId, user.getId(), "rejected", note);
                    resp.getWriter().write(JsonUtil.success("Reservation rejected"));
                    break;

                case "cancel":
                    if (res.getUserId() != user.getId() && !"admin".equals(user.getRole())) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        resp.getWriter().write(JsonUtil.error("You can only cancel your own reservations"));
                        return;
                    }
                    if (!"pending".equals(res.getStatus()) && !"approved".equals(res.getStatus())) {
                        resp.setStatus(HttpServletResponse.SC_CONFLICT);
                        resp.getWriter().write(JsonUtil.error("Can only cancel pending or approved reservations"));
                        return;
                    }
                    reservationDAO.updateStatus(reservationId, "cancelled");
                    if ("admin".equals(user.getRole())) {
                        logDAO.create(reservationId, user.getId(), "cancelled", note);
                    }
                    resp.getWriter().write(JsonUtil.success("Reservation cancelled"));
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write(JsonUtil.error("Unknown action: " + action));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid reservation ID"));
        }
    }
}
