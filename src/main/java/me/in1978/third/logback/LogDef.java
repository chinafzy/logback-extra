package me.in1978.third.logback;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogDef {
    private String ori;
    private String target;

    @Override
    public String toString() {
        return ori;
    }
}
