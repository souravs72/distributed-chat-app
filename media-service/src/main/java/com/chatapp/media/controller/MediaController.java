package com.chatapp.media.controller;

import com.chatapp.common.dto.BaseResponse;
import com.chatapp.media.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    @PostMapping("/upload")
    public ResponseEntity<BaseResponse<String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId) {
        BaseResponse<String> response = mediaService.uploadFile(file, userId);
        return ResponseEntity.status(response.isSuccess() ? 201 : 400).body(response);
    }

    @GetMapping("/files/{userId}/{filename}")
    public ResponseEntity<byte[]> downloadFile(
            @PathVariable String userId,
            @PathVariable String filename) {
        BaseResponse<byte[]> response = mediaService.downloadFile(userId, filename);
        
        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", filename);
        
        return new ResponseEntity<>(response.getData(), headers, HttpStatus.OK);
    }

    @GetMapping("/thumbnails/{userId}/{filename}")
    public ResponseEntity<byte[]> downloadThumbnail(
            @PathVariable String userId,
            @PathVariable String filename) {
        BaseResponse<byte[]> response = mediaService.downloadThumbnail(userId, filename);
        
        if (!response.isSuccess()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(response.getData(), headers, HttpStatus.OK);
    }
}
