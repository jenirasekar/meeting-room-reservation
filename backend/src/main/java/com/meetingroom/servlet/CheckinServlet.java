package com.meetingroom.servlet;

import com.meetingroom.dao.CheckinDAO;
import com.meetingroom.dao.ReservationDAO;
import com.meetingroom.model.Checkin;
import com.meetingroom.model.Reservation;
import com.meetingroom.model.User;
import com.meetingroom.util.JsonUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/checkin/*")
public class CheckinServlet extends HttpServlet {

    private final CheckinDAO checkinDAO = new CheckinDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        User user = (User) req.getSession().getAttribute("user");

        // Path: /api/checkin/{reservationId}
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Reservation ID is required"));
            return;
        }

        try {
            int reservationId = Integer.parseInt(pathInfo.substring(1));
            Reservation res = reservationDAO.findById(reservationId);

            if (res == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write(JsonUtil.error("Reservation not found"));
                return;
            }

            // Only the reservation owner or admin can check in
            if (res.getUserId() != user.getId() && !"admin".equals(user.getRole())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(JsonUtil.error("Access denied"));
                return;
            }

            // Only approved reservations can be checked in
            if (!"approved".equals(res.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(JsonUtil.error("Can only check in to approved reservations"));
                return;
            }

            // Check if already checked in
            Checkin existing = checkinDAO.findByReservationId(reservationId);
            if (existing != null) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write(JsonUtil.error("Already checked in"));
                return;
            }

            Checkin checkin = new Checkin();
            checkin.setReservationId(reservationId);
            checkin.setMethod("manual");

            if (checkinDAO.create(checkin)) {
                // Mark reservation as completed
                reservationDAO.updateStatus(reservationId, "completed");
                resp.getWriter().write(JsonUtil.success("Check-in successful", checkin));
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write(JsonUtil.error("Check-in failed"));
            }
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write(JsonUtil.error("Invalid reservation ID"));
        }
    }
}
