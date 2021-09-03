package vip.testops.gungnir.entities.vto;

import lombok.Data;

@Data
public class SourceLineData {
    private Integer lineNo;
    private String content;
    private int branchCount;
    private int branchCoveredCount;
    private int instructionCount;
    private int instructionCoveredCount;
}
