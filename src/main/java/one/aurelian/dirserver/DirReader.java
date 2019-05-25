package one.aurelian.dirserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.java.Log;
import one.aurelian.dirserver.models.raw.RawTransaction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Value
@Builder
@Log
public class DirReader {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("json");

    @NonNull
    Path root;

    @NonNull @Builder.Default
    Integer maxDepth = 10;

    @NonNull @Builder.Default
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private static String getFileExtension(Path path) {
        final var fn = path.getFileName().toString();
        final var i = fn.lastIndexOf(".");
        return (i < 0) ? "" : fn.substring(i + 1);
    }

    Stream<Path> stream() throws IOException {
        if (maxDepth < 1) {
            throw new IllegalArgumentException("max depth must be >= 1");
        }
        return Files.find(
            root,
            10,
            (path, stat) -> stat.isRegularFile() && SUPPORTED_EXTENSIONS.contains(getFileExtension(path.getFileName()))
        );
    }

    Stream<RawTransaction> streamTransactionsFromPath(@NonNull Path path) {
        JsonFactory factory = new JsonFactory();
        factory = factory.setCodec(objectMapper);
        try {
            JsonParser parser = factory.createParser(path.toFile());
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IOException(String.format("Expected start of array, got %s (%s)", parser.getCurrentToken(), parser.getCurrentLocation()));
            }
            parser.nextToken();
            if (parser.getCurrentToken() == JsonToken.END_ARRAY) {
                return Stream.empty();
            }
            return Stream.generate((Supplier<RawTransaction>) () -> {
                try {
                    if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
                        throw new IOException(String.format("Expected start of object, got %s (%s)", parser.getCurrentToken(), parser.getCurrentLocation()));
                    }
                    return parser.readValueAs(new TypeReference<RawTransaction>() {});
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }).takeWhile(rawTransaction -> {
                try {
                    parser.nextToken();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
                    return true;
                }
                if (parser.getCurrentToken() == JsonToken.END_ARRAY) {
                    return false;
                }
                throw new UncheckedIOException(new IOException(String.format("Expected start of object or end of array, got %s (%s)", parser.getCurrentToken(), parser.getCurrentLocation())));
            }).onClose(() -> {
                try {
                    parser.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<RawTransaction> streamTransactions() throws IOException {
        return stream().flatMap(this::streamTransactionsFromPath);
    }
}
