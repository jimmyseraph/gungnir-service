package vip.testops.gungnir.entities.vto;

import lombok.Data;

import java.util.List;

@Data
public class SourceFileData {
    private String name;
    private int lineCount;
    private int lineCoveredCount;
    private int branchCount;
    private int branchCoveredCount;
    private int instructionCount;
    private int instructionCoveredCount;
    private List<SourceLineData> lines;
}
