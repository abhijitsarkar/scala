package name.abhijitsarkar.java;

import rx.Observable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Abhijit Sarkar
 */
public class Transformer {
    public static boolean run(String path, String text, String fileFilter) {
        DirectoryStream<Path> files = files(path, fileFilter);

        Observable.from(files).flatMap(p -> {
            try {
                return Observable.from((Iterable<Map.Entry<String, List<String>>>) Files.lines(p)
                        .filter(line -> line.contains(text))
                        .map(String::trim)
                        .collect(collectingAndThen(groupingBy(pp -> p.toAbsolutePath().toString()), Map::entrySet)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        })
                .toBlocking()
                .forEach(e -> System.out.printf("%s -> %s.%n", e.getKey(), e.getValue()));

        try {
            files.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return true;
    }

    private static DirectoryStream<Path> files(String path, String fileFilter) {
        try {
            return Files.newDirectoryStream(Paths.get(path), fileFilter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
