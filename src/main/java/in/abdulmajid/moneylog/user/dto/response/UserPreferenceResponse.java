package in.abdulmajid.moneylog.user.dto.response;

import in.abdulmajid.moneylog.user.model.DateFormat;
import in.abdulmajid.moneylog.user.model.ReminderTiming;
import in.abdulmajid.moneylog.user.model.ThemePreference;
import in.abdulmajid.moneylog.user.model.TimeFormat;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPreferenceResponse {
    private ThemePreference theme;
    private String currency;
    private String timezone;
    private DateFormat dateFormat;
    private TimeFormat timeFormat;
    private String language;
    private Boolean billReminderEnabled;
    private Boolean mismatchAlertEnabled;
    private Boolean spendingSummaryEnabled;
    private ReminderTiming reminderDaysBefore;
}
