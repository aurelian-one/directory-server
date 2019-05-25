package one.aurelian.dirserver.models.raw;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class RawTransaction {

    URI sourceURI;

    String id;

    @NotNull Instant timeOccurred;

    @NotNull String description;

    @NotNull
    @Singular
    Map<@Length(min = 1) @Pattern(regexp = "^[a-zA-Z0-9_\\-.]+$") String, @Length(min = 1) String> tags;

    @NotNull
    @Singular
    @Size(min = 1, max = 100)
    List<RawTransactionEntry> entries;

}
