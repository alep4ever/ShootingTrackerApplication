package com.example.application.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller that serves video files with proper MIME types
 * This ensures videos are served correctly and can be played in the browser
 */
@RestController
public class VideoController {

    /**
     * Serves video files from the videos directory
     * The @GetMapping maps URLs like /api/videos/crossover.mp4 to this method
     *
     * @param filename The name of the video file to serve
     * @return The video file with proper headers set for browser playback
     */
    @GetMapping("/api/videos/{filename:.+}")
    public ResponseEntity<Resource> serveVideo(@PathVariable String filename) {
        try {
            // Build the path to the video file
            // This points to the videos folder in your resources/static directory
            Path videoPath = Paths.get("src/main/resources/static/videos").resolve(filename);

            // Create a resource from the file
            Resource video = new FileSystemResource(videoPath.toFile());

            // Check if the file actually exists
            if (!video.exists()) {
                // If file doesn't exist, return 404 Not Found
                return ResponseEntity.notFound().build();
            }

            // Return the video file with proper headers
            // The Content-Type header explicitly tells the browser this is an MP4 video
            // This prevents the MIME type error you were seeing
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "video/mp4")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(video);

        } catch (Exception e) {
            // If anything goes wrong, return 500 Internal Server Error
            return ResponseEntity.internalServerError().build();
        }
    }
}