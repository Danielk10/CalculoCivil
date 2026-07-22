package com.diamon.calculo.terminal;

import com.diamon.calculo.engine.OpenSeesExecutor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * Parses and executes Linux-style terminal commands within the app sandbox.
 * Supports file operations, script execution, and built-in test commands.
 */
public class TerminalCommandParser {

    private File currentDir;
    private final File rootDir; // App sandbox root
    private OpenSeesExecutor executor;

    public static class CommandResult {
        public final String output;
        public final boolean success;
        public final boolean isClear;
        public final boolean isAsync; // For run-tcl/run-py that need async execution

        public CommandResult(String output, boolean success) {
            this(output, success, false, false);
        }

        public CommandResult(String output, boolean success, boolean isClear, boolean isAsync) {
            this.output = output;
            this.success = success;
            this.isClear = isClear;
            this.isAsync = isAsync;
        }
    }

    public TerminalCommandParser(File rootDir, OpenSeesExecutor executor) {
        this.rootDir = rootDir;
        this.currentDir = rootDir;
        this.executor = executor;
    }

    /**
     * Parse and execute a command string.
     */
    public CommandResult execute(String commandLine) {
        if (commandLine == null || commandLine.trim().isEmpty()) {
            return new CommandResult("", true);
        }

        String[] parts = commandLine.trim().split("\\s+");
        String cmd = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        switch (cmd) {
            case "help":
                return cmdHelp();
            case "clear":
                return new CommandResult("", true, true, false);
            case "ls":
                return cmdLs(args);
            case "pwd":
                return cmdPwd();
            case "mkdir":
                return cmdMkdir(args);
            case "rm":
                return cmdRm(args);
            case "cat":
                return cmdCat(args);
            case "cd":
                return cmdCd(args);
            case "run-tcl":
                return cmdRunTcl(args);
            case "run-py":
                return cmdRunPy(args);
            case "run-test-tcl":
                return cmdRunTestTcl();
            case "run-test-py":
                return cmdRunTestPy();
            case "echo":
                return new CommandResult(String.join(" ", args), true);
            case "whoami":
                return new CommandResult("opensees@structural-research", true);
            case "uname":
                return new CommandResult("OpenSees 3.8.0 ARM64/Android NDK", true);
            case "date":
                return new CommandResult(new java.util.Date().toString(), true);
            default:
                return new CommandResult(cmd + ": command not found. Type 'help' for available commands.", false);
        }
    }

    private CommandResult cmdHelp() {
        String help =
                "╔═══════════════════════════════════════════════════╗\n" +
                "║           AVAILABLE COMMANDS                     ║\n" +
                "╠═══════════════════════════════════════════════════╣\n" +
                "║  help              Show this help message        ║\n" +
                "║  clear             Clear terminal output         ║\n" +
                "║  ls [-a]           List files in directory       ║\n" +
                "║  pwd               Print working directory       ║\n" +
                "║  cd <dir>          Change directory               ║\n" +
                "║  mkdir <dir>       Create a new directory        ║\n" +
                "║  rm <file>         Remove a file                 ║\n" +
                "║  rm -rf <dir>      Remove directory recursively  ║\n" +
                "║  cat <file>        Display file contents         ║\n" +
                "║  echo <text>       Print text                    ║\n" +
                "║  run-tcl <script>  Execute OpenSees TCL script   ║\n" +
                "║  run-py <script>   Execute OpenSeesPy script     ║\n" +
                "║  run-test-tcl      Run cantilever beam TCL test  ║\n" +
                "║  run-test-py       Run portal frame Python test  ║\n" +
                "║  whoami            Show user info                ║\n" +
                "║  uname             Show system info              ║\n" +
                "║  date              Show current date/time        ║\n" +
                "╚═══════════════════════════════════════════════════╝";
        return new CommandResult(help, true);
    }

    private CommandResult cmdLs(String[] args) {
        boolean showHidden = args.length > 0 && args[0].equals("-a");
        File dir = currentDir;

        // Check if a path argument was given (non-flag)
        for (String arg : args) {
            if (!arg.startsWith("-")) {
                dir = resolveFile(arg);
                break;
            }
        }

        if (!dir.exists() || !dir.isDirectory()) {
            return new CommandResult("ls: cannot access '" + dir.getName() + "': No such directory", false);
        }

        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return new CommandResult("(empty directory)", true);
        }

        Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            if (!showHidden && f.getName().startsWith(".")) continue;
            String suffix = f.isDirectory() ? "/" : "";
            String size = f.isFile() ? String.format(" (%d bytes)", f.length()) : "";
            sb.append(f.getName()).append(suffix).append(size).append("\n");
        }
        return new CommandResult(sb.toString().trim(), true);
    }

    private CommandResult cmdPwd() {
        return new CommandResult(currentDir.getAbsolutePath(), true);
    }

    private CommandResult cmdCd(String[] args) {
        if (args.length == 0) {
            currentDir = rootDir;
            return new CommandResult("", true);
        }

        File target = resolveFile(args[0]);
        if (!target.exists()) {
            return new CommandResult("cd: no such directory: " + args[0], false);
        }
        if (!target.isDirectory()) {
            return new CommandResult("cd: not a directory: " + args[0], false);
        }

        // Security: don't escape sandbox
        try {
            if (!target.getCanonicalPath().startsWith(rootDir.getCanonicalPath())) {
                return new CommandResult("cd: permission denied (outside sandbox)", false);
            }
        } catch (IOException e) {
            return new CommandResult("cd: error: " + e.getMessage(), false);
        }

        currentDir = target;
        return new CommandResult("", true);
    }

    private CommandResult cmdMkdir(String[] args) {
        if (args.length == 0) {
            return new CommandResult("mkdir: missing operand", false);
        }
        File dir = resolveFile(args[0]);
        if (dir.exists()) {
            return new CommandResult("mkdir: cannot create '" + args[0] + "': File exists", false);
        }
        boolean ok = dir.mkdirs();
        return new CommandResult(ok ? "Directory created: " + args[0] : "mkdir: failed to create directory", ok);
    }

    private CommandResult cmdRm(String[] args) {
        if (args.length == 0) {
            return new CommandResult("rm: missing operand", false);
        }

        boolean recursive = false;
        String target = null;
        for (String arg : args) {
            if (arg.equals("-rf") || arg.equals("-r") || arg.equals("-f")) {
                recursive = true;
            } else {
                target = arg;
            }
        }

        if (target == null) {
            return new CommandResult("rm: missing file operand", false);
        }

        File file = resolveFile(target);
        if (!file.exists()) {
            return new CommandResult("rm: cannot remove '" + target + "': No such file or directory", false);
        }

        if (file.isDirectory() && !recursive) {
            return new CommandResult("rm: cannot remove '" + target + "': Is a directory (use rm -rf)", false);
        }

        boolean ok = deleteRecursive(file);
        return new CommandResult(ok ? "Removed: " + target : "rm: failed to remove " + target, ok);
    }

    private CommandResult cmdCat(String[] args) {
        if (args.length == 0) {
            return new CommandResult("cat: missing file operand", false);
        }
        File file = resolveFile(args[0]);
        if (!file.exists()) {
            return new CommandResult("cat: " + args[0] + ": No such file", false);
        }
        if (file.isDirectory()) {
            return new CommandResult("cat: " + args[0] + ": Is a directory", false);
        }

        try {
            byte[] bytes = readFileBytes(file);
            String content = new String(bytes);
            // Limit output size
            if (content.length() > 10000) {
                content = content.substring(0, 10000) + "\n... (truncated)";
            }
            return new CommandResult(content, true);
        } catch (IOException e) {
            return new CommandResult("cat: error reading " + args[0] + ": " + e.getMessage(), false);
        }
    }

    private CommandResult cmdRunTcl(String[] args) {
        if (args.length == 0) {
            return new CommandResult("run-tcl: missing script path", false);
        }
        if (executor == null) {
            return new CommandResult("Error: OpenSees executor not available", false);
        }

        File script = resolveFile(args[0]);
        if (!script.exists()) {
            return new CommandResult("run-tcl: script not found: " + args[0], false);
        }

        String result = executor.executeTclScript(script.getAbsolutePath());
        return new CommandResult(result, true);
    }

    private CommandResult cmdRunPy(String[] args) {
        if (args.length == 0) {
            return new CommandResult("run-py: missing script path", false);
        }
        if (executor == null) {
            return new CommandResult("Error: OpenSees executor not available", false);
        }

        File script = resolveFile(args[0]);
        if (!script.exists()) {
            return new CommandResult("run-py: script not found: " + args[0], false);
        }

        String result = executor.executePythonScript(script.getAbsolutePath());
        return new CommandResult(result, true);
    }

    private CommandResult cmdRunTestTcl() {
        if (executor == null) {
            return new CommandResult("Error: OpenSees executor not available", false);
        }

        String tclScript =
                "wipe\n" +
                "model BasicBuilder -ndm 2 -ndf 3\n" +
                "node 1 0.0 0.0\n" +
                "node 2 10.0 0.0\n" +
                "fix 1 1 1 1\n" +
                "geomTransf Linear 1\n" +
                "element elasticBeamColumn 1 1 2 0.01 2.0e8 0.0001 1\n" +
                "pattern Plain 1 Linear {\n" +
                "    load 2 0.0 -100.0 0.0\n" +
                "}\n" +
                "system BandGeneral\n" +
                "numberer RCM\n" +
                "constraints Plain\n" +
                "integrator LoadControl 1.0\n" +
                "algorithm Linear\n" +
                "analysis Static\n" +
                "analyze 1\n" +
                "puts \"=== Cantilever Beam Analysis ===\"\n" +
                "puts \"Node 2 Uy = [nodeDisp 2 2] m\"\n" +
                "puts \"Theoretical: -0.01666667 m\"\n" +
                "exit\n";

        String result = executor.executeTclScriptContent(tclScript);
        return new CommandResult("=== Running Cantilever Beam TCL Test ===\n" + result, true);
    }

    private CommandResult cmdRunTestPy() {
        if (executor == null) {
            return new CommandResult("Error: OpenSees executor not available", false);
        }

        String pyScript =
                "import opensees as ops\n" +
                "ops.wipe()\n" +
                "ops.model('basic', '-ndm', 2, '-ndf', 3)\n" +
                "ops.node(1, 0.0, 0.0)\n" +
                "ops.node(2, 5.0, 0.0)\n" +
                "ops.node(3, 5.0, 3.0)\n" +
                "ops.node(4, 0.0, 3.0)\n" +
                "ops.fix(1, 1, 1, 1)\n" +
                "ops.fix(2, 1, 1, 1)\n" +
                "ops.geomTransf('Linear', 1)\n" +
                "ops.element('elasticBeamColumn', 1, 1, 4, 0.04, 2.0e8, 5.33e-4, 1)\n" +
                "ops.element('elasticBeamColumn', 2, 2, 3, 0.04, 2.0e8, 5.33e-4, 1)\n" +
                "ops.element('elasticBeamColumn', 3, 4, 3, 0.04, 2.0e8, 5.33e-4, 1)\n" +
                "# Eigenvalue analysis\n" +
                "eigenValues = ops.eigen(3)\n" +
                "import math\n" +
                "print('=== Portal Frame Modal Analysis ===')\n" +
                "for i, ev in enumerate(eigenValues):\n" +
                "    T = 2 * math.pi / math.sqrt(ev)\n" +
                "    print(f'Mode {i+1}: T = {T:.4f} s, f = {1/T:.4f} Hz')\n" +
                "print('Analysis completed successfully.')\n";

        String result = executor.executePyScriptContent(pyScript);
        return new CommandResult("=== Running Portal Frame Python Test ===\n" + result, true);
    }

    // ==================== Helpers ====================

    private File resolveFile(String path) {
        if (path.startsWith("/")) {
            return new File(path);
        }
        if (path.equals("..")) {
            File parent = currentDir.getParentFile();
            return parent != null ? parent : currentDir;
        }
        if (path.equals(".")) {
            return currentDir;
        }
        return new File(currentDir, path);
    }

    private boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return file.delete();
    }

    private byte[] readFileBytes(File file) throws IOException {
        java.io.FileInputStream fis = new java.io.FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();
        return data;
    }

    public File getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(File dir) {
        this.currentDir = dir;
    }
}
