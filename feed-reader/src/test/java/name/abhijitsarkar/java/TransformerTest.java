package name.abhijitsarkar.java;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

/**
 * @author Abhijit Sarkar
 */
public class TransformerTest {
    @Test
    public void testExtractsTemperature() throws IOException {
        String path = NoaaClient.currentConditionsPath(false);

        try (DirectoryStream<Path> files = Files.newDirectoryStream(Paths.get(path), "*.xml")) {
            Observable<Map.Entry<String, ? extends Collection<String>>> flow =
                    new Transformer().transform(files, "temp_f");

            Schedulers.immediate().createWorker().schedule(() ->
                    flow.toBlocking().forEach(e -> System.out.printf("%s -> %s.%n", e.getKey(), e.getValue())));
        }
    }
}
