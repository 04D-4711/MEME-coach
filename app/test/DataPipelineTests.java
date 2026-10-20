import org.junit.Test;
import persistentdata.DataPipeline;

import static org.junit.Assert.assertNotNull;

public class DataPipelineTests {

    @Test
    public void constructorDoesNotRecursivelyCreatePipelines() {
        DataPipeline<String, String> pipeline =
                new DataPipeline<>(null, null, null, "test.csv");

        assertNotNull(pipeline);
    }
}
