package dev.johnmaluki.boardroom_booking_backend.core.controller;

import dev.johnmaluki.boardroom_booking_backend.core.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Files")
public class FileController {
  private final FileService fileService;

  @PostMapping("/upload")
  @Operation(summary = "Upload image files")
  public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
    return ResponseEntity.ok(fileService.saveFile(file));
  }

  @GetMapping("/files/{fileName}")
  @Operation(summary = "Obtain file by name")
  public ResponseEntity<byte[]> getFiles(@PathVariable String fileName) {
    HttpHeaders headers = new HttpHeaders();
    String fileExtension = this.getFileExtension(fileName);
    headers.setContentType(MediaType.parseMediaType("image/" + fileExtension));
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return ResponseEntity.ok().headers(headers).body(fileService.getFile(fileName));
  }

  private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
      return ""; // No extension found
    }
    return fileName.substring(lastDotIndex + 1);
  }
}
