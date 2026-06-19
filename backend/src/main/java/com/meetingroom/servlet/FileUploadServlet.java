package com.meetingroom.servlet;

import com.meetingroom.util.JsonUtil;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@WebServlet("/api/upload/*")
@MultipartConfig(
    maxFileSize = 5 * 1024 * 1024,      // 5 MB per file
    maxRequestSize = 10 * 1024 * 1024    // 10 MB total request
)
public class FileUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIR =
            System.getProperty("user.home") + File.separator + "meeting-room-uploads" + File.separator + "rooms";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            Part filePart = req.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("No file uploaded"));
                return;
            }

            // Validate content type
            String contentType = filePart.getContentType();
            if (contentType == null ||
                (!contentType.startsWith("image/jpeg") &&
                 !contentType.startsWith("image/png") &&
                 !contentType.startsWith("image/gif") &&
                 !contentType.startsWith("image/webp"))) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write(JsonUtil.error("Only JPEG, PNG, GIF, and WebP images are allowed"));
                return;
            }

            // Generate unique filename, preserving extension
            String submitted = filePart.getSubmittedFileName();
            String ext = "";
            if (submitted != null) {
                int dot = submitted.lastIndexOf('.');
                if (dot >= 0) ext = submitted.substring(dot).toLowerCase();
            }
            String filename = UUID.randomUUID().toString() + ext;

            // Ensure upload directory exists
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            // Save file
            Path filePath = Paths.get(UPLOAD_DIR, filename);
            Files.copy(filePart.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "/api/uploads/rooms/" + filename;
            resp.getWriter().write(JsonUtil.success("Upload successful", url));

        } catch (IllegalStateException e) {
            resp.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            resp.getWriter().write(JsonUtil.error("File is too large. Maximum size is 5 MB"));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write(JsonUtil.error("Upload failed: " + e.getMessage()));
        }
    }
}
