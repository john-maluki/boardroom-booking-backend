package dev.johnmaluki.boardroom_booking_backend.core.controller;

import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import dev.johnmaluki.boardroom_booking_backend.core.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Files")
public class FileController {
  private final FileService fileService;

  @PostMapping("/upload")
  @Operation(summary = "Upload image files")
  public Mono<String> uploadFile(@RequestPart("file") FilePart file) {
    return fileService.saveFile(file);
  }

  /**
   * The body is a {@code Flux<DataBuffer>} so the file is streamed to the client rather than read
   * into a byte array first.
   */
  @GetMapping("/files/{fileName}")
  @Operation(summary = "Obtain file by name")
  public ResponseEntity<Flux<DataBuffer>> getFiles(@PathVariable String fileName) {
    if (!fileService.isWithinStorage(fileName)) {
      throw new ResourceNotFoundException("File image not found");
    }
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("image/" + getFileExtension(fileName)));
    headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
    return ResponseEntity.ok().headers(headers).body(fileService.getFile(fileName));
  }

  private String getFileExtension(String fileName) {
    int lastDotIndex = fileName.lastIndexOf('.');
    if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
      return "";
    }
    return fileName.substring(lastDotIndex + 1);
  }
}
