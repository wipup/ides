package ports.soc.ides.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date and time utility class
 * @author WIPU
 *
 */
public class DateTimeUtils {

	public static final String FORMAT_PATTERN_DATETIME = "d MMM yyyy HH:mm";
	public static final String FORMAT_PATTERN_LONG_DATETIME = "d MMMM yyyy HH:mm";

	public static final DateTimeFormatter FORMATTER_DATETIME = DateTimeFormatter.ofPattern(FORMAT_PATTERN_DATETIME);
	public static final DateTimeFormatter FORMATTER_LONG_DATETIME = DateTimeFormatter.ofPattern(FORMAT_PATTERN_LONG_DATETIME);

	public static String formatDateTime(LocalDateTime dt) {
		if (dt == null) {
			return "";
		}
		return dt.format(FORMATTER_DATETIME);
	}
	
	public static String formatLongDateTime(LocalDateTime dt) {
		if (dt == null) {
			return "";
		}
		return dt.format(FORMATTER_LONG_DATETIME);
	}
}
