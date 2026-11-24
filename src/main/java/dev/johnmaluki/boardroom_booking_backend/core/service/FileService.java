package dev.johnmaluki.boardroom_booking_backend.core.service;

import dev.johnmaluki.boardroom_booking_backend.core.exception.FileUploadException;
import dev.johnmaluki.boardroom_booking_backend.core.exception.ResourceNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * Image upload and download.
 *
 * <p>Uploads use {@link FilePart#transferTo}, which streams the request body to disk without ever
 * holding the whole file in memory — the servlet version read {@code MultipartFile.getBytes()} into
 * a single array.
 *
 * <p>Downloads stream the file back as {@link DataBuffer}s. {@code Files.createDirectories} and the
 * existence check are blocking filesystem calls, so they run on {@code boundedElastic}.
 */
@Component
public class FileService {
  private static final int BUFFER_SIZE = 8192;

  private final String storageDir =
      System.getProperty("user.home") + "/project-uploads/kemri/uploads/boardroom-backend-files/";

  /** Stores the upload under a UUID-prefixed name and returns that name. */
  public Mono<String> saveFile(FilePart file) {
    String fileNamePlusIdentifier = UUID.randomUUID() + "-" + file.filename();
    Path path = Paths.get(storageDir + fileNamePlusIdentifier);
    return Mono.fromCallable(
            () -> {
              Files.createDirectories(path.getParent());
              return path;
            })
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(file::transferTo)
        .thenReturn(fileNamePlusIdentifier)
        .onErrorMap(
            error -> !(error instanceof FileUploadException),
            error -> new FileUploadException("Error occurred while uploading image"));
  }

  public Flux<DataBuffer> getFile(String fileName) {
    Path path = Paths.get(storageDir).resolve(fileName).normalize();
    return Mono.fromCallable(() -> Files.isRegularFile(path))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMapMany(
            exists ->
                Boolean.TRUE.equals(exists)
                    ? DataBufferUtils.read(
                        path, new DefaultDataBufferFactory(), BUFFER_SIZE)
                    : Flux.error(new ResourceNotFoundException("File image not found")));
  }

  /** Guards against a traversal attempt such as {@code ../../etc/passwd} in the file name. */
  public boolean isWithinStorage(String fileName) {
    Path root = Paths.get(storageDir).normalize();
    return root.resolve(fileName).normalize().startsWith(root);
  }

}
