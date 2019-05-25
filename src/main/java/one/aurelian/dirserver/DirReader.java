package one.aurelian.dirserver;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import one.aurelian.dirserver.raw.RawTransaction;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Value
@Builder
public class DirReader {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("json", "yaml", "yml");

    @NonNull
    @Builder.Default
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

    @NonNull
    @Builder.Default
    Integer maxDepth = 10;

    private static String getFileExtension(Path path) {
        final var fn = path.getFileName().toString();
        final var i = fn.lastIndexOf(".");
        return (i < 0) ? "" : fn.substring(i + 1);
    }

    public Stream<Path> streamPathsFromDir(@NonNull Path root) throws IOException {
        if (getMaxDepth() < 1) {
            throw new IllegalArgumentException("max depth must be >= 1");
        }
        return Files.find(
            root,
            getMaxDepth(),
            (path, stat) -> stat.isRegularFile() && SUPPORTED_EXTENSIONS.contains(getFileExtension(path.getFileName()))
        );
    }

    static URI getLocationURI(Path path, JsonLocation location) {
        return URI.create(String.format("%s#%s:%s", path.toUri(), location.getLineNr(), location.getColumnNr()));
    }

    static void assertToken(JsonParser parser, JsonToken token) {
        if (parser.currentToken() != token) {
            throw new UncheckedIOException(new IOException(String.format(
                "Expected %s, got %s (%s)", token, parser.currentToken(), parser.getCurrentLocation()
            )));
        }
    }

    static RawTransaction readNextTransaction(Path path, JsonParser parser) {
        try {
            parser.nextToken();
            if (parser.currentToken() == JsonToken.END_ARRAY) {
                return null;
            }
            assertToken(parser, JsonToken.START_OBJECT);
            URI locationAsURI = getLocationURI(path, parser.getCurrentLocation());
            RawTransaction transaction = parser.readValueAs(new TypeReference<RawTransaction>() {});
            return transaction.toBuilder().sourceURI(locationAsURI).build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void uncheckedClose(Closeable subject) {
        try {
            subject.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<RawTransaction> streamTransactionsFromPath(@NonNull Path path) throws UncheckedIOException {
        try {
            JsonParser parser = objectMapper.getFactory().createParser(path.toFile());
            parser.nextToken();
            assertToken(parser, JsonToken.START_ARRAY);
            return Stream.generate(() -> readNextTransaction(path, parser))
                    .takeWhile(Objects::nonNull)
                    .onClose(() -> uncheckedClose(parser));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Stream<RawTransaction> streamTransactionsFromDir(
        @NonNull Path root
    ) throws IOException, UncheckedIOException {
        return streamPathsFromDir(root).flatMap(this::streamTransactionsFromPath);
    }
}
