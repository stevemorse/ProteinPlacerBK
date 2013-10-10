package seleniumBlast;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author: Farzad
 * @created: Apr 4, 2006
 */
public class SeleniumQBlastRequest
{

    public static void runCommand(SeleniumQBlastCommand command) throws Exception
    {
        URL url = new URL(QBlastURL);

        URLConnection conn = url.openConnection();
        conn.setDoOutput(true);

        OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
        System.out.println("command = " + command);
        out.write(command.toString());
        out.flush();

        command.processResult(conn.getInputStream());
        
        out.close();
    }


    protected static String QBlastURL = "http://www.ncbi.nlm.nih.gov/blast/Blast.cgi";
}