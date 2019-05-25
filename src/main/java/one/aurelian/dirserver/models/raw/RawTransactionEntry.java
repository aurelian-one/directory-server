package one.aurelian.dirserver.models.raw;

import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Value
public class RawTransactionEntry {

    @NotNull @Length(min = 1) @Pattern(regexp = "^[\\p{Alnum}_\\-]+(?:\\.[\\p{Alnum}_\\-]+)*$") String accountId;

    @NotNull @Length(min = 1) @Pattern(regexp = "^-?[1-9][0-9]*(?:\\.[0-9]+)?$") String value;

    @NotNull @Length(min = 1) @Pattern(regexp = "^[\\p{L}\\p{Sc}]+$") String currency;

    public List<String> getAccountIdAsList() {
        return Arrays.asList(getAccountId().split("\\."));
    }

    public BigDecimal getValueAsDecimal() {
        return new BigDecimal(getValue());
    }

}
