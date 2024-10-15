package dev.johnmaluki.boardroom_booking_backend.core.service;

import dev.johnmaluki.boardroom_booking_backend.core.exception.FileUploadException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileService {
  private final String storageDir =
      System.getProperty("user.home") + "/project-uploads/kemri/uploads/boardroom-backend-files/";

  public String saveFile(MultipartFile file) {
    try {
      String fileName = file.getOriginalFilename();
      String uuid = UUID.randomUUID().toString();
      String fileNamePlusIdentifier = uuid + "-" + fileName;
      Path path = Paths.get(storageDir + fileNamePlusIdentifier);
      Files.createDirectories(path.getParent());
      Files.write(path, file.getBytes());
      return fileNamePlusIdentifier;
    } catch (IOException e) {
      throw new FileUploadException("Error occurred while uploading image");
    }
  }

  public byte[] getFile(String fileName) {
    try {
      Path path = Paths.get(storageDir + fileName);
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new ResourceNotFoundException("File image not found");
    }
  }
}
