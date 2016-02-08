package name.abhijitsarkar.java;

import rx.Observable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Abhijit Sarkar
 */
public class Transformer {
    public Observable<Map.Entry<String, ? extends Collection<String>>> transform(DirectoryStream<Path> dir, String text) {
        return Observable.from(dir).flatMap(p -> {
            try {
                return Observable.from((Iterable<Map.Entry<String, List<String>>>) Files.lines(p)
                        .filter(line -> line.contains(text))
                        .map(String::trim)
                        .collect(collectingAndThen(groupingBy(pp -> p.toAbsolutePath().toString()), Map::entrySet)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }
}
