package ru.hse.spb.git;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import ru.hse.spb.git.commit.CommitInfo;
import ru.hse.spb.git.status.Status;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

@Command(name = "init")
class GitInit {
}

@Command(name = "log")
class GitLog {
    @Parameters(index = "0", arity = "0..1", description = "from commit")
    String hash;
}

@Command(name = "status")
class GitStatusCommand {
    void invoke(Path repositoryDir) throws IOException {
        Optional<RepositoryManager> possibleRepository = RepositoryManager.open(repositoryDir);

        if (!possibleRepository.isPresent()) {
            System.out.println("Git repository is not initialised properly in " + repositoryDir + "!");
            return;
        }

        Status status = possibleRepository.get().getStatus();
        printStatus(possibleRepository.get().getRepositoryRoot(), status);
    }

    private void printStatus(Path root, Status status) {
        String stagedFiles =    formatFiles(root, status.getStagedFiles(), "modified: ");
        String notStagedFiles = formatFiles(root, status.getNotStagedFiles(), "modified: ");

        String deletedFiles = formatFiles(root, status.getDeletedFiles(), "deleted: ");
        String missingFiles = formatFiles(root, status.getMissingFiles(), "deleted: ");

        String notTrackedFiles = formatFiles(root, status.getNotTrackedFiles(), "");

        Arrays.asList(
            "On branch master",
            "Changes to be committed:",
            stagedFiles,
            deletedFiles,
            "",
            "Changes not staged for commit:",
            notStagedFiles,
            notTrackedFiles,
            missingFiles
        ).forEach(System.out::println);
    }

    private String formatFiles(Path relative, @NotNull Set<Path> files, String prefix) {
        return files.stream()
            .map(relative::relativize)
            .map(Path::toString)
            .map(s -> "\t" + prefix + s)
            .collect(Collectors.joining(System.lineSeparator()));
    }
}

@Command(name = "add")
class GitAdd {
    @Parameters(index = "0", paramLabel = "FILE")
    Path file;
}

@Command(name = "rm")
class GitRm {
    @Parameters(index = "0", paramLabel = "FILE")
    Path file;
}

@Command(name = "commit")
class GitCommit {
    @Parameters(index = "0", paramLabel = "MESSAGE")
    String message;
}

//TODO add option for `-- file` syntax
@Command(name = "checkout")
class GitCheckout {
    @Parameters(index = "0", paramLabel = "HASH")
    String hash;
}

@Command(name = "reset")
class GitReset {
    @Parameters(index = "0", paramLabel = "HASH")
    String hash;
}

@Command(subcommands = {
    GitLog.class,
    GitInit.class,
    GitCommit.class,
    GitCheckout.class,
    GitReset.class,
    GitAdd.class,
    GitRm.class,
    GitStatusCommand.class,
})
class GitCommand {
}

public class Main {

    public static void main(String[] args) throws IOException {
        CommandLine commandLine = new CommandLine(new GitCommand());
        List<CommandLine> parse = commandLine.parse(args);

        if (parse.size() == 2) {
            executeGitCommand(parse.get(1), currentDir());
        } else {
            commandLine.usage(System.out);
        }
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
            GitLog logCommand = commandLine.getCommand();
            List<CommitInfo> log = logCommand.hash != null ? repository.getLog(logCommand.hash) : repository.getLog();

            printCommitLog(log);

            return;
        }

        if (commandLine.getCommand() instanceof GitCommit) {
            GitCommit commit = commandLine.getCommand();
            repository.commit(commit.message);
            return;
        }

        if (commandLine.getCommand() instanceof GitAdd) {
            GitAdd gitAdd = commandLine.getCommand();
            repository.addFile(gitAdd.file.toAbsolutePath());
            return;
        }

        if (commandLine.getCommand() instanceof GitCheckout) {
            GitCheckout checkout = commandLine.getCommand();
            if (checkout.hash.equals("master")) {
                String masterHead = repository.getMasterHeadCommit().orElseThrow(() ->
                    new IllegalArgumentException("Cannot checkout to master, because there are no commits in it.")
                );
                repository.checkoutToCommit(masterHead);
            } else {
                repository.checkoutToCommit(checkout.hash);
            }
            return;
        }

        if (commandLine.getCommand() instanceof GitReset) {
            GitReset reset = commandLine.getCommand();
            repository.hardResetTo(reset.hash);
            return;
        }

        GitStatusCommand statusCommand = commandLine.getCommand();
        statusCommand.invoke(repositoryDir);
    }

    private static void printCommitLog(List<CommitInfo> log) {
        if (log.isEmpty()) {
            System.out.println("No commits yet.");
        } else {
            for (CommitInfo commitInfo : log) {
                System.out.printf("commit %s\n", commitInfo.getHash());
                System.out.println();
                System.out.printf("%s\n", commitInfo.getMessage());
            }
        }
    }

    private static Path currentDir() {
        final String dir = System.getProperty("user.dir");
        return Paths.get(dir).toAbsolutePath();
    }
}
