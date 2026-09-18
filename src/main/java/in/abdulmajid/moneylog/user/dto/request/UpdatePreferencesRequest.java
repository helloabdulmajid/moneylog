package in.abdulmajid.moneylog.user.dto.request;

import in.abdulmajid.moneylog.user.model.DateFormat;
import in.abdulmajid.moneylog.user.model.ReminderTiming;
import in.abdulmajid.moneylog.user.model.ThemePreference;
import in.abdulmajid.moneylog.user.model.TimeFormat;
import lombok.Data;

@Data
public class UpdatePreferencesRequest {

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
