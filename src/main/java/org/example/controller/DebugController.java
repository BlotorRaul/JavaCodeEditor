package org.example.controller;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder;
import java.util.List;
import java.util.Map;

/**
 * A controller class for managing a debugging session using the Java Debug Interface (JDI).
 * <p>
 * This class performs the following operations:
 * 1. Receives Java code as a string.
 * 2. Writes the code to a file (`MyMainClass.java`).
 * 3. Compiles the file using `javac`.
 * 4. Launches the compiled code in debug mode (using `-agentlib:jdwp` on port 5005).
 * 5. Attaches to the debuggee process via JDI.
 * 6. Sets a breakpoint at a specific line (e.g., line 15 by default).
 * 7. Listens for debug events, such as hitting the breakpoint or process disconnection.
 * <p>
 * Note: This example assumes that `javac` and `java` are available on the system PATH.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class DebugController {

    private static final String SOURCE_NAME = "MyMainClass.java";
    private static final String MAIN_CLASS = "MyMainClass";
    private Process debuggeeProcess;

    /**
     * Starts a debugging session for the given Java code.
     * <p>
     * The method performs the following steps:
     * 1. Writes the provided Java code to `MyMainClass.java`.
     * 2. Compiles the file.
     * 3. Launches the debuggee process in debug mode.
     * 4. Attaches to the debuggee process using JDI.
     * 5. Sets a breakpoint at line 15.
     * 6. Listens for events, such as hitting the breakpoint.
     *
     * @param code The Java source code to debug.
     */
    public void startDebugSession(String code) {
        try {
            createSourceFile(code);

            compileSource();

            debuggeeProcess = launchDebuggee();

            Thread.sleep(800);

            VirtualMachine vm = attachToDebuggee("localhost", "5005");
            System.out.println("[DEBUG] Connected to VM: " + vm.description());

            ReferenceType rt = findClass(vm, MAIN_CLASS);
            if (rt == null) {
                System.out.println("[WARN] Class " + MAIN_CLASS + " not found!");
            } else {
                setBreakpoint(vm, rt, 15);
            }

            listenEvents(vm);
            vm.dispose();
            debuggeeProcess.waitFor();
            System.out.println("[DEBUG] Debug session ended.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the given Java source code to a file named `MyMainClass.java`.
     *
     * @param code The Java source code to write.
     * @throws IOException If an error occurs while writing the file.
     */
    private void createSourceFile(String code) throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(SOURCE_NAME))) {
            pw.println(code);
        }
        System.out.println("[DEBUG] Created source file: " + SOURCE_NAME);
    }

    /**
     * Compiles the Java source file using `javac`.
     *
     * @throws IOException          If an error occurs while executing the compiler.
     * @throws InterruptedException If the compilation process is interrupted.
     */
    private void compileSource() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac", SOURCE_NAME);
        pb.inheritIO();
        Process p = pb.start();
        int exitVal = p.waitFor();
        if (exitVal == 0) {
            System.out.println("[DEBUG] Compilation successful!");
        } else {
            System.err.println("[DEBUG] Compilation failed (exit code=" + exitVal + ")");
        }
    }

    /**
     * Launches the debuggee process with JDWP enabled on port 5005.
     *
     * @return The `Process` instance representing the debuggee process.
     * @throws IOException If an error occurs while launching the process.
     */
    private Process launchDebuggee() throws IOException {
        String javaCmd = "java";
        // Parametrii de debug
        String agent = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005";

        ProcessBuilder pb = new ProcessBuilder(javaCmd, agent, MAIN_CLASS);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        System.out.println("[DEBUG] Debuggee launched on port 5005 (pid=" + proc.pid() + ")");
        return proc;
    }

    /**
     * Attaches to the debuggee process running on the specified host and port using JDI.
     *
     * @param host The hostname or IP address of the debuggee.
     * @param port The port number on which the debuggee is listening.
     * @return The attached {@link VirtualMachine}.
     * @throws Exception If an error occurs while attaching to the debuggee.
     */
    private VirtualMachine attachToDebuggee(String host, String port) throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        AttachingConnector socketConnector = null;

        for (AttachingConnector ac : vmm.attachingConnectors()) {
            if (ac.name().equals("com.sun.jdi.SocketAttach")) {
                socketConnector = ac;
                break;
            }
        }
        if (socketConnector == null) {
            throw new RuntimeException("SocketAttach connector not found!");
        }

        Map<String, Connector.Argument> args = socketConnector.defaultArguments();
        args.get("hostname").setValue(host);
        args.get("port").setValue(port);

        System.out.println("[DEBUG] Connecting to " + host + ":" + port + " ...");
        VirtualMachine vm = socketConnector.attach(args);
        return vm;
    }

    /**
     * Finds the {@link ReferenceType} of the specified class in the debuggee.
     *
     * @param vm        The attached {@link VirtualMachine}.
     * @param className The fully qualified name of the class to find.
     * @return The {@link ReferenceType} if found, or `null` if not found.
     */
    private ReferenceType findClass(VirtualMachine vm, String className) {
        for (ReferenceType rt : vm.allClasses()) {
            if (rt.name().equals(className)) {
                return rt;
            }
        }
        return null;
    }

    /**
     * Sets a breakpoint at the specified line in the given class.
     *
     * @param vm   The attached {@link VirtualMachine}.
     * @param rt   The {@link ReferenceType} of the class.
     * @param line The line number where the breakpoint should be set.
     * @throws AbsentInformationException If line information is unavailable.
     */
    private void setBreakpoint(VirtualMachine vm, ReferenceType rt, int line)
            throws AbsentInformationException {
        List<Location> locs = rt.locationsOfLine(line);
        if (locs.isEmpty()) {
            System.out.println("[DEBUG] No location found for line " + line);
            return;
        }
        Location loc = locs.get(0);

        EventRequestManager erm = vm.eventRequestManager();
        BreakpointRequest bpReq = erm.createBreakpointRequest(loc);
        bpReq.enable();
        System.out.println("[DEBUG] Breakpoint set at line " + line + " in " + rt.name());
    }

    /**
     * Listens for events from the debuggee process, such as hitting breakpoints or disconnections.
     *
     * @param vm The attached {@link VirtualMachine}.
     * @throws InterruptedException If interrupted while waiting for events.
     */
    private void listenEvents(VirtualMachine vm) throws InterruptedException {
        EventQueue eq = vm.eventQueue();
        boolean running = true;

        while (running) {
            EventSet es = eq.remove();
            for (Event e : es) {
                if (e instanceof BreakpointEvent) {
                    BreakpointEvent be = (BreakpointEvent) e;
                    Location loc = be.location();
                    System.out.println("[BREAKPOINT] Hit at line "
                            + loc.lineNumber() + " in " + loc.declaringType().name());
                    running = false;
                } else if (e instanceof VMDisconnectEvent) {
                    System.out.println("[DEBUG] Debuggee disconnected.");
                    running = false;
                }
            }
            es.resume();
        }
    }
}
