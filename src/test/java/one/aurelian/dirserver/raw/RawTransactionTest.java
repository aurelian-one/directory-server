package one.aurelian.dirserver.raw;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


class RawTransactionTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testDeserialize() throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.findAndRegisterModules();
        RawTransaction t = om.readValue(
            getClass().getClassLoader().getResource("transaction.json"),
            RawTransaction.class
        );
        assertThat(validator.validate(t)).isEmpty();
        assertThat(t.getId()).isNull();
        assertThat(t.getDescription()).isEqualTo("Some example transaction");
        assertThat(t.getTags()).hasSize(1).containsEntry("merchant", "CO-OP");
        assertThat(t.getTimeOccurred()).isEqualTo(Instant.EPOCH);
        assertThat(t.getEntries()).hasSize(2);
        assertThat(t.getEntries().get(0)).satisfies(e -> {
            assertThat(e.getAccountId()).isEqualTo("expenses.food_&_drink.groceries");
            assertThat(e.getCurrency()).isEqualTo("£");
            assertThat(e.getValueAsDecimal()).isEqualTo(new BigDecimal("10.45"));
        });
        assertThat(t.getEntries().get(1)).satisfies(e -> {
            assertThat(e.getAccountId()).isEqualTo("assets.monzo.current_account");
            assertThat(e.getCurrency()).isEqualTo("£");
            assertThat(e.getValueAsDecimal()).isEqualTo(new BigDecimal("-10.45"));
        });
        assertThat(t.getEntries().get(0).getValueAsDecimal().add(t.getEntries().get(1).getValueAsDecimal())).isZero();
    }

}