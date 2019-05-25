package one.aurelian.dirserver.raw;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
@JsonDeserialize(builder = RawTransactionEntry.RawTransactionEntryBuilder.class)
public class RawTransactionEntry {

    String accountId;
    String value;
    String currency;

    public BigDecimal getValueAsDecimal() {
        return new BigDecimal(getValue());
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class RawTransactionEntryBuilder {
        public RawTransactionEntryBuilder value(String s) {
            final var i = s.lastIndexOf(" ");
            if (i < 0) {
                value = s;
                return this;
            }
            value = s.substring(0, i);
            return currency(s.substring(i + 1));
        }
    }

}
