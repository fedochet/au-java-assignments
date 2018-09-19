package ru.hse.spb.git;

import picocli.CommandLine;
import ru.hse.spb.git.commit.CommitInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Parameters;

@Command(name = "log")
class GitLog {
    @Parameters(index = "0", arity = "0..1", description = "from commit")
    String hash;
}

@Command(name = "init")
class GitInit {
}

@Command(name = "commit")
class GitCommit {
    @Parameters(index = "0", paramLabel = "MESSAGE")
    String message;
    @Parameters(index = "1", paramLabel = "FILE")
    Path file;
}

@Command(name = "checkout")
class GitCheckout {
    @Parameters(index = "0", paramLabel = "HASH")
    String hash;
}

@Command(subcommands = {GitLog.class, GitInit.class, GitCommit.class, GitCheckout.class})
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
            for (CommitInfo commitInfo : log) {
                System.out.println(commitInfo);
            }
            return;
        }

        if (commandLine.getCommand() instanceof GitCommit) {
            GitCommit commit = commandLine.getCommand();
            repository.commitFile(commit.file.toAbsolutePath(), commit.message);
            return;
        }

        if (commandLine.getCommand() instanceof GitCheckout) {
            GitCheckout checkout = commandLine.getCommand();
            repository.checkoutTo(checkout.hash);
            return;
        }
    }

    private static Path currentDir() {
        final String dir = System.getProperty("user.dir");
        return Paths.get(dir).toAbsolutePath();
    }
}
