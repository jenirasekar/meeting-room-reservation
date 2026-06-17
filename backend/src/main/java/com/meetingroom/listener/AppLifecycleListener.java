package com.meetingroom.listener;

import com.meetingroom.dao.ReservationDAO;
import com.meetingroom.util.DBConnection;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppLifecycleListener implements ServletContextListener {

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=== Meeting Room Reservation System Started ===");

        // Initialize DB pool
        try {
            DBConnection.getDataSource();
            System.out.println("Database connection pool initialized successfully.");
        } catch (Exception e) {
            System.err.println("WARNING: Could not initialize DB pool at startup: " + e.getMessage());
        }

        // Start auto-cancellation scheduler — runs every 5 minutes
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                autoCancelNoShows();
            } catch (Exception e) {
                System.err.println("Auto-cancellation error: " + e.getMessage());
            }
        }, 1, 5, TimeUnit.MINUTES);

        System.out.println("Auto-cancellation scheduler started (runs every 5 minutes).");
    }

    /**
     * Cancels approved reservations whose start_time has passed by more than
     * 15 minutes and have no check-in record.
     */
    private void autoCancelNoShows() {
        ReservationDAO reservationDAO = new ReservationDAO();
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Integer> noShowIds = reservationDAO.findNoShows(cutoff);

        if (!noShowIds.isEmpty()) {
            for (int id : noShowIds) {
                reservationDAO.updateStatus(id, "cancelled");
                System.out.println("Auto-cancelled reservation #" + id + " (no check-in after 15 minutes)");
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("=== Meeting Room Reservation System Shutting Down ===");

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            System.out.println("Auto-cancellation scheduler stopped.");
        }

        try {
            DBConnection.close();
            System.out.println("Connection pool closed.");
        } catch (Exception e) {
            System.err.println("Error closing connection pool: " + e.getMessage());
        }
    }
}
