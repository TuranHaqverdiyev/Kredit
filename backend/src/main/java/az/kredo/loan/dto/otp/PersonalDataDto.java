package az.kredo.loan.dto.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Personal data fetched from ASAN/IAMAS systems")
public class PersonalDataDto {
    @Schema(example = "Turan")
    private String firstName;

    @Schema(example = "Aliyev")
    private String lastName;

    @Schema(example = "7ABC123")
    private String fin;

    @Schema(example = "1990-05-10")
    private LocalDate dateOfBirth;

    @Schema(example = "Baku, Nasimi district, apt 42")
    private String address;

    @Schema(example = "EMPLOYED")
    private String employmentStatus;

    @Schema(example = "3000.0")
    private Double monthlyIncome;

    @Schema(example = "100.0")
    private Double existingMonthlyDebt;
}
