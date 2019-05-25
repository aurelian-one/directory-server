package one.aurelian.dirserver.raw;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class RawTransaction {

    URI sourceURI;

    String id;

    String description;

    Instant timeOccurred;

    @Singular
    Map<String, String> tags;

    @Singular
    List<RawTransactionEntry> entries;

}
