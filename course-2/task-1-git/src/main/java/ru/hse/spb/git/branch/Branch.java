package ru.hse.spb.git.branch;

import lombok.Data;

@Data
public class Branch {
    private final String name;
    private final String headCommit;
}
