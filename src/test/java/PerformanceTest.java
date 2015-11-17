import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import com.github.fge.jsonschema.main.JsonValidator;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class PerformanceTest {

    private static final int ITERATIONS = 1_000;

    public static void main(String[] args) throws Exception {
        long scan1Elapsed = validate1();
        long scan2Elapsed = validate2();

        System.out.printf("scan 1 took %dms, scan 2 took: %dms, scan1 - scan2: %d\n",
                TimeUnit.NANOSECONDS.toMillis(scan1Elapsed),
                TimeUnit.NANOSECONDS.toMillis(scan2Elapsed),
                scan1Elapsed - scan2Elapsed);

    }

    private static long validate1() throws Exception {
        JsonValidator validator = JsonSchemaFactory.byDefault().getValidator();
        JsonNode schema4 = new ObjectMapper().readTree(PerformanceTest.class.getResourceAsStream("json-schema-draft-04.json"));
        // warmup
        validator.validate(schema4, schema4);

        long startedAt = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            validator.validate(schema4, schema4);
        }
        return System.nanoTime() - startedAt;
    }

    private static long validate2() throws IOException {
        try (InputStream inputStream = PerformanceTest.class.getResourceAsStream("json-schema-draft-04.json")) {
            JSONObject rawSchema = new JSONObject(new JSONTokener(inputStream));
            Schema schema = SchemaLoader.load(rawSchema);
            //warmup
            schema.validate(rawSchema); // throws a ValidationException if this object is invalid

            long startedAt = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                schema.validate(rawSchema);
            }
            return System.nanoTime() - startedAt;
        }
    }
}
