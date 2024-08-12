package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataGenerator implements ApplicationRunner {
    private final AppAdminRepository appAdminRepository;
    private final UserUtil userUtil;

    private void createDefaultAdminEmails() {
        List<String> emails = userUtil.getDefaultSystemAdministrators();
        List<AppAdmin> appAdmins = emails.stream().map(AppAdmin::new).toList();
        appAdminRepository.saveAll(appAdmins);
    }
    @Override
    public void run(ApplicationArguments args) {
        this.createDefaultAdminEmails();
    }
}
