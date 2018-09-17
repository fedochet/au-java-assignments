package ru.hse.spb.git;

import lombok.ToString;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

@Command(name = "log")
class GitLog {
}

@Command(name = "init")
class GitInit {
}

@Command(name = "commit")
@ToString
class GitCommit {

    @Parameters(index = "0", paramLabel = "FILE")
    Path file;

    @Parameters(index = "1", paramLabel = "MESSAGE")
    String message;
}

@Command(subcommands = {GitLog.class, GitInit.class, GitCommit.class})
class GitCommand {
}

public class Main {

    public static void main(String[] args) throws IOException {
        List<CommandLine> parse = new CommandLine(new GitCommand()).parse(args);

        executeGitCommand(parse.get(1), currentDir());
    }

    private static void executeGitCommand(CommandLine commandLine, Path repositoryDir) throws IOException {

        if (commandLine.getCommand() instanceof GitInit) {
            RepositoryManager.init(repositoryDir);
            System.out.println("Repository is initialized in " + repositoryDir);
            return;
        }

        Optional<RepositoryManager> possibleRepository = RepositoryManager.open(repositoryDir);

        if (!possibleRepository.isPresent()) {
            System.out.println("Git repository is not initialised properly in " + repositoryDir + "!");
            return;
        }

        RepositoryManager repository = possibleRepository.get();

        if (commandLine.getCommand() instanceof GitLog) {
            for (CommitInfo commitInfo : repository.getLog()) {
                System.out.println(commitInfo);
            }

            return;
        }

        if (commandLine.getCommand() instanceof GitCommit) {
            GitCommit commit = commandLine.getCommand();
            repository.commitFile(commit.file.toAbsolutePath(), commit.message);
            return;
        }
    }

    private static Path currentDir() {
        final String dir = System.getProperty("user.dir");
        return Paths.get(dir).toAbsolutePath();
    }
}
