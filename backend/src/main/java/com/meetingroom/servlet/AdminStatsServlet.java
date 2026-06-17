package com.meetingroom.servlet;

import com.meetingroom.dao.*;
import com.meetingroom.util.JsonUtil;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/admin/stats")
public class AdminStatsServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final CheckinDAO checkinDAO = new CheckinDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject stats = new JsonObject();
        stats.addProperty("total_rooms", roomDAO.count());
        stats.addProperty("reservations_today", reservationDAO.countToday());
        stats.addProperty("pending_count", reservationDAO.countByStatus("pending"));
        stats.addProperty("approved_count", reservationDAO.countByStatus("approved"));
        stats.addProperty("rejected_count", reservationDAO.countByStatus("rejected"));
        stats.addProperty("completed_count", reservationDAO.countByStatus("completed"));
        stats.addProperty("checkins_today", checkinDAO.countToday());

        resp.getWriter().write(JsonUtil.success("OK", stats));
    }
}
