package com.bharathandukuri.compilr.environment;

import com.bharathandukuri.compilr.compiler.dto.ExecuteResponse;
import com.bharathandukuri.compilr.compiler.enums.ExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MongoDB 8.0 Execution Environment Tests")
class MongoDbExecutionEnvironmentTest extends BaseEnvironmentTest {

    @Test
    @DisplayName("Executes Insert and Query in MongoDB")
    void fullWorkflow() {
        String script = """
                db.items.insertMany([
                    { item: "journal", qty: 25, status: "A" },
                    { item: "notebook", qty: 50, status: "A" }
                ]);
                printjson(db.items.find({ status: "A" }).toArray());
                """;
        ExecuteResponse response = execute("mongodb-8.0", script);
        assertThat(response.status()).isEqualTo(ExecutionStatus.SUCCESS);
        assertThat(response.stdout())
                .contains("journal")
                .contains("notebook");
    }
}
