package org.example.application;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.EventRequestManager;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder;
import java.util.List;
import java.util.Map;

/**
 * A minimal example of a debugger implemented using Java Debug Interface (JDI).
 * <p>
 * This program:
 * 1. Creates a simple source file `MyMainClass.java`.
 * 2. Compiles it using `javac`.
 * 3. Runs it in debug mode using `-agentlib:jdwp` on port 5005.
 * 4. Attaches to the debuggee process and sets a breakpoint on line 15.
 * 5. Outputs a message when the breakpoint is hit and terminates.
 * <p>
 * Note: This example is for educational purposes and assumes the necessary Java
 * development tools (e.g., `javac`, `java`) are available on the system's PATH.
 *
 * @author [Blotor Raul]
 * @version 1.0
 */
public class MiniDebuggerOneFile {

    private static final String SOURCE_NAME = "MyMainClass.java";
    private static final String MAIN_CLASS = "MyMainClass";

    /**
     * Main method executing the debugging example.
     *
     * @param args Command-line arguments (not used in this program).
     */
    public static void main(String[] args) {
        try {
            // 1. Create source file
            createSourceFile();

            // 2. Compile source file
            compile();

            // 3. Launch debuggee with JDWP enabled
            Process debuggee = launchDebuggee();

            // Wait briefly for the debuggee to initialize
            Thread.sleep(800);

            // 4. Attach to the debuggee process(localhost:5005)
            VirtualMachine vm = attachToDebuggee("localhost", "5005");
            System.out.println("[INFO] Conectat la VM: " + vm.description());

            // Find the ReferenceType for MyMainClass
            ReferenceType rt = findClass(vm, MAIN_CLASS);
            if (rt == null) {
                System.out.println("[WARN] Clasa " + MAIN_CLASS + " nu a fost gasita in debuggee!");
            } else {
                setBreakpoint(vm, rt, 15);
            }

            // 5. Listen for events (e.g., breakpoints, VM disconnect)
            listenEvents(vm);

            // Cleanup
            vm.dispose();
            debuggee.waitFor();
            System.out.println("[INFO] Incheiat.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a simple source file `MyMainClass.java` with approximately 15 lines of code.
     *
     * @throws IOException If there is an error writing the source file.
     */
    private static void createSourceFile() throws IOException {
        String code = ""
                + "public class MyMainClass {\n"
                + "  public static void main(String[] args) {\n"
                + "    System.out.println(\"[COD] Start...\");\n"
                + "    int x = 10;\n"
                + "    for(int i=0; i<3; i++) {\n" // line 14
                + "      x += i;\n"               // line 15
                + "      System.out.println(\"[COD] i=\" + i + \", x=\"+x);\n"
                + "    }\n"
                + "    System.out.println(\"[COD] End: x=\"+x);\n"
                + "  }\n"
                + "}\n";

        try (PrintWriter pw = new PrintWriter(new FileWriter(SOURCE_NAME))) {
            pw.println(code);
        }
        System.out.println("[INFO] Created source file " + SOURCE_NAME);
    }

    /**
     * Compiles the source file `MyMainClass.java` using `javac`.
     *
     * @throws IOException          If an error occurs during compilation.
     * @throws InterruptedException If the process is interrupted while waiting for compilation.
     */
    private static void compile() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("javac", SOURCE_NAME);
        pb.inheritIO();
        Process p = pb.start();
        int exitVal = p.waitFor();
        if (exitVal == 0) {
            System.out.println("[INFO] Compilation successful!");
        } else {
            System.err.println("[ERR] Compilation error (exit code=" + exitVal + ")");
        }
    }

    /**
     * Launches the debuggee process in debug mode with JDWP enabled on port 5005.
     *
     * @return The launched `Process` instance.
     * @throws IOException If an error occurs while launching the process.
     */
    private static Process launchDebuggee() throws IOException {
        String javaCmd = "java";
        String agent = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005";

        ProcessBuilder pb = new ProcessBuilder(javaCmd, agent, MAIN_CLASS);
        pb.redirectErrorStream(true); // sa vedem tot outputul
        Process proc = pb.start();
        System.out.println("[INFO] Debuggee launched on port 5005 (pid=" + proc.pid() + ")");
        return proc;
    }

    /**
     * Attaches to the debuggee process running on the specified host and port.
     *
     * @param host The hostname or IP address of the debuggee.
     * @param port The port on which the debuggee is listening.
     * @return The attached {@link VirtualMachine}.
     * @throws Exception If the attachment fails.
     */
    private static VirtualMachine attachToDebuggee(String host, String port) throws Exception {
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

        System.out.println("[INFO] Connecting to " + host + ":" + port + " ...");
        VirtualMachine vm = socketConnector.attach(args);
        return vm;
    }

    /**
     * Finds the {@link ReferenceType} of the specified class in the debuggee.
     *
     * @param vm        The attached {@link VirtualMachine}.
     * @param className The fully qualified name of the class to find.
     * @return The {@link ReferenceType} if found, otherwise `null`.
     */
    private static ReferenceType findClass(VirtualMachine vm, String className) {
        for (ReferenceType rt : vm.allClasses()) {
            if (rt.name().equals(className)) {
                return rt;
            }
        }
        return null;
    }

    /**
     * Sets a breakpoint at the specified line in the given {@link ReferenceType}.
     *
     * @param vm   The attached {@link VirtualMachine}.
     * @param rt   The {@link ReferenceType} of the class.
     * @param line The line number to set the breakpoint.
     * @throws AbsentInformationException If line number information is unavailable.
     */
    private static void setBreakpoint(VirtualMachine vm, ReferenceType rt, int line)
            throws AbsentInformationException {
        List<Location> locs = rt.locationsOfLine(line);
        if (locs.isEmpty()) {
            System.out.println("[WARN] No location found for line " + line);
            return;
        }
        Location loc = locs.get(0);

        EventRequestManager erm = vm.eventRequestManager();
        BreakpointRequest bpr = erm.createBreakpointRequest(loc);
        bpr.enable();
        System.out.println("[INFO] Breakpoint set at line " + line
                + " in " + rt.name());
    }

    /**
     * Listens for events from the debuggee, such as breakpoints or VM disconnection.
     *
     * @param vm The attached {@link VirtualMachine}.
     * @throws InterruptedException If interrupted while waiting for events.
     */
    private static void listenEvents(VirtualMachine vm) throws InterruptedException {
        EventQueue eq = vm.eventQueue();
        boolean running = true;

        while (running) {
            EventSet es = eq.remove(); // blocant
            for (Event e : es) {
                if (e instanceof BreakpointEvent) {
                    BreakpointEvent be = (BreakpointEvent) e;
                    Location loc = be.location();
                    System.out.println("[BREAKPOINT] S-a atins la linia "
                            + loc.lineNumber() + " in " + loc.declaringType().name());

                    running = false; // iesim
                } else if (e instanceof VMDisconnectEvent) {
                    System.out.println("[INFO] Debuggee s-a inchis.");
                    running = false;
                }
            }
            es.resume();
        }
    }
}
