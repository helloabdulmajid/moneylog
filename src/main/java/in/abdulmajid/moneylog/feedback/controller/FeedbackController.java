package in.abdulmajid.moneylog.feedback.controller;

import in.abdulmajid.moneylog.auth.model.User;
import in.abdulmajid.moneylog.common.CurrentUserHelper;
import in.abdulmajid.moneylog.feedback.dto.request.FeedbackRequest;
import in.abdulmajid.moneylog.feedback.dto.response.FeedbackResponse;
import in.abdulmajid.moneylog.feedback.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CurrentUserHelper currentUserHelper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FeedbackResponse> submit(
            @RequestPart("data") @Valid FeedbackRequest request,
            @RequestPart(value = "screenshot", required = false) MultipartFile screenshot,
            HttpServletRequest servletRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.submit(
                        request,
                        screenshot,
                        currentUserOrNull(),
                        clientIp(servletRequest)));
    }

    private User currentUserOrNull() {
        try {
            return currentUserHelper.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}