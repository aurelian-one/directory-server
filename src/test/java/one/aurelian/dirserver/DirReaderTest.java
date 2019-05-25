package one.aurelian.dirserver;

import lombok.extern.java.Log;
import one.aurelian.dirserver.models.raw.RawTransaction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Log
class DirReaderTest {

    @Test
    void streamTransactions() throws IOException {
        DirReader reader = DirReader.builder()
                .root(Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("datasetA")).getPath()))
                .build();
        log.info(reader.stream().collect(Collectors.toList()).toString());
        Collection<RawTransaction> transactions = reader.streamTransactions().collect(Collectors.toList());
        log.info(transactions.toString());
    }


}