package gyeonggi.gyeonggifesta.member.dto.request;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class UpdateFeatureReq {

	private String username;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthday;

	private String gender;

	private String email;
}
