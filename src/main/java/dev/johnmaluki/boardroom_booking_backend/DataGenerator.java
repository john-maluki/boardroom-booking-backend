package dev.johnmaluki.boardroom_booking_backend;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppAdmin;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataGenerator implements ApplicationRunner {
    private final AppAdminRepository appAdminRepository;
    private final UserUtil userUtil;

    private void createDefaultAdminEmails() {
        List<String> emails = userUtil.getDefaultSystemAdministrators();

        // Check existing emails in the database
        List<AppAdmin> existingAdmins = appAdminRepository.findAllByEmailIn(emails);
        Set<String> existingEmails = existingAdmins.stream()
                .map(AppAdmin::getEmail)
                .collect(Collectors.toSet());

        // Filter to get only the new emails
        List<AppAdmin> appAdminsToCreate = emails.stream()
                .filter(email -> !existingEmails.contains(email))
                .map(AppAdmin::new)
                .toList();

        // Save new AppAdmin entries
        if (!appAdminsToCreate.isEmpty()) {
            appAdminRepository.saveAll(appAdminsToCreate);
        }
    }
    @Override
    public void run(ApplicationArguments args) {
        this.createDefaultAdminEmails();
    }
}
