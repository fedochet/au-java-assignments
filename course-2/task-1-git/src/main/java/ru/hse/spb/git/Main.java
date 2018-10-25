package ru.hse.spb.git;

import org.jetbrains.annotations.NotNull;
import picocli.CommandLine;
import ru.hse.spb.git.commit.CommitInfo;
import ru.hse.spb.git.status.Status;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

interface RepositoryAction {
    void invoke(@NotNull RepositoryManager manager) throws IOException;
}

@Command(name = "init")
class GitInit {
}

@Command(name = "log")
class GitLog implements RepositoryAction {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Parameters(index = "0", arity = "0..1", description = "from commit")
    private String hash;

    @Override
    public void invoke(@NotNull RepositoryManager repository) throws IOException {
        List<CommitInfo> log = hash != null ? repository.getLog(hash) : repository.getLog();

        printCommitLog(log);
    }

    private void printCommitLog(List<CommitInfo> log) {
        if (log.isEmpty()) {
            System.out.println("No commits yet.");
        } else {
            for (CommitInfo commitInfo : log) {
                System.out.printf("commit %s\n", commitInfo.getHash());
                System.out.printf("Date: %s\n", DATE_TIME_FORMATTER.format(commitInfo.getTimestamp()));
                System.out.println();
                System.out.printf("%s\n", commitInfo.getMessage());
            }
        }
    }

}

@Command(name = "status")
class GitStatusCommand implements RepositoryAction {
    @Override
    public void invoke(@NotNull RepositoryManager repositoryManager) throws IOException {
        Status status = repositoryManager.getStatus();
        printStatus(repositoryManager.getRepositoryRoot(), status);
    }

    private void printStatus(Path root, Status status) {
        String stagedFiles = formatFiles(root, status.getStagedFiles(), "modified: ");
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
class GitAdd implements RepositoryAction {
    @Parameters(index = "0", paramLabel = "FILE")
    private Path file;


    @Override
    public void invoke(@NotNull RepositoryManager manager) throws IOException {
        manager.addFile(file.toAbsolutePath());
    }
}

@Command(name = "rm")
class GitRm implements RepositoryAction {
    @Parameters(index = "0", paramLabel = "FILE")
    private Path file;

    @Override
    public void invoke(@NotNull RepositoryManager manager) throws IOException {
        manager.remove(file.toAbsolutePath());
    }
}

@Command(name = "commit")
class GitCommit implements RepositoryAction {
    @Parameters(index = "0", paramLabel = "MESSAGE")
    private String message;

    @Override
    public void invoke(@NotNull RepositoryManager repositoryManager) throws IOException {
        repositoryManager.commit(message);
    }
}

//TODO add option for `-- file` syntax
@Command(name = "checkout")
class GitCheckout implements RepositoryAction {
    @Parameters(index = "0", paramLabel = "HASH")
    private String hash;

    @Override
    public void invoke(@NotNull RepositoryManager repository) throws IOException {
        if (hash.equals("master")) {
            String masterHead = repository.getMasterHeadCommit().orElseThrow(() ->
                new IllegalArgumentException("Cannot checkout to master, because there are no commits in it.")
            );
            repository.checkoutToCommit(masterHead);
        } else {
            repository.checkoutToCommit(hash);
        }
    }
}

@Command(name = "reset")
class GitReset implements RepositoryAction {
    @Parameters(index = "0", paramLabel = "HASH")
    private String hash;

    @Override
    public void invoke(@NotNull RepositoryManager manager) throws IOException {
        manager.hardResetTo(hash);
    }
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
        RepositoryAction command = commandLine.getCommand();
        command.invoke(repository);
    }

    private static Path currentDir() {
        final String dir = System.getProperty("user.dir");
        return Paths.get(dir).toAbsolutePath();
    }
}
