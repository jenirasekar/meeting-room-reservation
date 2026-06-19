package com.meetingroom.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

@WebServlet("/api/uploads/*")
public class FileServeServlet extends HttpServlet {

    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator + "meeting-room-uploads";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Prevent directory traversal
        String normalized = Paths.get(pathInfo).normalize().toString();
        if (normalized.contains("..")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File file = Paths.get(UPLOAD_DIR, normalized).toFile();
        if (!file.exists() || !file.isFile()) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Determine content type
        String name = file.getName().toLowerCase();
        String mime;
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) mime = "image/jpeg";
        else if (name.endsWith(".png"))  mime = "image/png";
        else if (name.endsWith(".gif"))  mime = "image/gif";
        else if (name.endsWith(".webp")) mime = "image/webp";
        else                             mime = "application/octet-stream";

        resp.setContentType(mime);
        resp.setContentLengthLong(file.length());

        // Cache for 1 hour
        resp.setHeader("Cache-Control", "public, max-age=3600");

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = resp.getOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) >= 0) {
                os.write(buf, 0, n);
            }
        }
    }
}
