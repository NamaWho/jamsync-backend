package com.lsmsdb.jamsync.routine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
@Setter
@AllArgsConstructor
public class Neo4jOperation {
    private String query;
    private Map<String, Object> parameters;

    public Neo4jOperation() {
        this.query = "";
        this.parameters = new HashMap<>();
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = prepareParameters(parameters);
    }

    private Map<String, Object> prepareParameters(Map<String, Object> parameters) {
        Map<String, Object> preparedParameters = new HashMap<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Object[]) {
                // Convert array to array of strings
                Object[] arrayValue = (Object[]) value;
                String[] stringArray = convertArrayToStrings(arrayValue);

                // Add to prepared parameters
                preparedParameters.put(key, stringArray);
            } else {
                // Non-array value, add to prepared parameters
                preparedParameters.put(key, value);
            }
        }

        return preparedParameters;
    }

    private String[] convertArrayToStrings(Object[] arrayValue) {
        return IntStream.range(0, arrayValue.length)
                .mapToObj(i -> arrayValue[i].toString())
                .toArray(String[]::new);
    }
}

