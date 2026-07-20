package FYP.project_backend.notification;

import FYP.project_backend.notification.dto.NotificationResponse;
import FYP.project_backend.user.User;
import FYP.project_backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin("*")
public class NotificationController {

    private final NotificationRepository repository;

    private final UserRepository userRepository;

    //=========================================
    // MY NOTIFICATIONS
    //=========================================

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public List<NotificationResponse> myNotifications(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElseThrow();

        return repository

                .findByUserOrderByCreatedAtDesc(user)

                .stream()

                .map(notification ->

                        NotificationResponse.builder()

                                .id(notification.getId())

                                .title(notification.getTitle())

                                .message(notification.getMessage())

                                .type(notification.getType())

                                .read(notification.isRead())

                                .createdAt(notification.getCreatedAt())

                                .build()

                )

                .toList();

    }

    //=========================================
    // GET UNREAD COUNT
    //=========================================

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public long unreadCount(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElseThrow();

        return repository.countByUserAndIsReadFalse(user);

    }

    //=========================================
    // MARK AS READ
    //=========================================

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> markRead(
            @PathVariable Long id){

        Notification notification = repository

                .findById(id)

                .orElse(null);

        if(notification == null){

            return ResponseEntity.notFound().build();

        }

        notification.setRead(true);

        repository.save(notification);

        return ResponseEntity.ok(notification);

    }

    //=========================================
    // MARK ALL AS READ
    //=========================================

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> readAll(){

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        User user = userRepository

                .findByEmail(authentication.getName())

                .orElseThrow();

        List<Notification> notifications =

                repository.findByUserOrderByCreatedAtDesc(user);

        notifications.forEach(n -> n.setRead(true));

        repository.saveAll(notifications);

        return ResponseEntity.ok(

                Map.of(

                        "message","All notifications marked as read"

                )

        );

    }

    //=========================================
    // ADMIN GET ALL
    //=========================================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Notification> getAll(){

        return repository.findAll();

    }

    //=========================================
    // DELETE
    //=========================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(
            @PathVariable Long id){

        if(!repository.existsById(id)){

            return ResponseEntity.notFound().build();

        }

        repository.deleteById(id);

        return ResponseEntity.ok("Notification deleted.");

    }

}