package in.abdulmajid.moneylog.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserPreferenceResponse {

    private String theme;
    private String currency;
    private String timezone;
    private String dateFormat;
    private String timeFormat;
    private String language;
    private Boolean billReminderEnabled;
    private Boolean mismatchAlertEnabled;
    private Boolean spendingSummaryEnabled;
    private String reminderDaysBefore;
}
