package one.aurelian.dirserver;

import lombok.extern.java.Log;
import one.aurelian.dirserver.raw.RawTransaction;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


@Log
class DirReaderTest {

    @Test
    void streamTransactions() throws IOException {
        DirReader reader = DirReader.builder().build();
        List<RawTransaction> transactions = reader.streamTransactionsFromDir(
            Path.of(Objects.requireNonNull(getClass().getClassLoader().getResource("datasetA")).getPath())
        ).collect(Collectors.toList());
        log.info(transactions.toString());
        assertThat(transactions).hasSize(3);
    }
}