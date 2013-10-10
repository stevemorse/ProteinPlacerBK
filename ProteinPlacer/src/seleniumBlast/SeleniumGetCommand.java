package seleniumBlast;

import java.io.*;

/**
 * @author: Farzad
 * @created: Apr 5, 2006
 */
public abstract class SeleniumGetCommand extends SeleniumQBlastCommand
{
    /**
     * Number of alignments
     *
     * @param value integer value
     */
    public void setAlignments(int value)
    {
        params.put("ALIGNMENTS", String.valueOf(value));
    }

    /**
     * Type of alignment view (FORMAT_OBJECT=Alignment only)
     *
     * @param value Pairwise, QueryAnchored, QueryAnchoredNoIdentities,
     *              FlatQueryAnchored, FlatQueryAnchoredNoIdentities, Tabular
     */
    public void setAlignmentView(String value)
    {
        params.put("ALIGNMENT_VIEW", value);
    }

    /**
     * Number of descriptions
     *
     * @param value integer value
     */
    public void setDescriptions(int value)
    {
        params.put("DESCRIPTIONS", String.valueOf(value));
    }

    /**
     * Add TARGET to Entrez links in formatted results
     *
     * @param value yes, no
     */
    public void setEntrezTarget(boolean value)
    {
        params.put("ENTREZ_LINKS_NEW_WINDOW", value ? "yes" : "no");
    }

    /**
     * Low expect value threshold for formatting
     *
     * @param value double type value
     */
    public void setLowExpect(double value)
    {
        params.put("EXPECT_LOW", String.valueOf(value));
    }

    /**
     * High expect value threshold for formatting
     *
     * @param value double type value
     */
    public void setHighExpect(double value)
    {
        params.put("EXPECT_HIGH", String.valueOf(value));
    }

    /**
     * Entrez query to limit formatting of Blast results
     *
     * @param value Entrez query format
     */
    public void setEntrezQueryFormat(String value)
    {
        params.put("FORMAT_ENTREZ_QUERY", value);
    }

    /**
     * Specifies object to get
     *
     * @param value Alignment, Neighbors, PSSM
     *              SearchInfo
     *              TaxBlast, TaxblastParent, TaxBlastMultiFrame
     */
    public void setObject(String value)
    {
        params.put("FORMAT_OBJECT", value);
    }

    /**
     * Type of formatting
     *
     * @param value HTML, Text, ASN.1, XML
     */
    public void setFormatType(String value)
    {
        params.put("FORMAT_TYPE", value);
    }

    /**
     * Show NCBI GI
     *
     * @param value yes, no
     */
    public void setShowGI(boolean value)
    {
        params.put("NCBI_GI", value ? "yes" : "no");
    }

    /**
     * Request ID
     *
     * @param value Valid request ID
     */
    public void setRequestID(String value)
    {
        params.put("RID", value);
    }

    /**
     * Allow to download megablast results as a gzip-compressed file
     *
     * @param value yes, no
     */
    public void setResultsFile(boolean value)
    {
        params.put("RESULTS_FILE", value ? "yes" : "no");
    }

    /**
     * Blast service which needs to be performed
     *
     * @param value plain, psi, phi, rpsblast, megablast
     */
    public void setService(String value)
    {
        params.put("SERVICE", value);
    }

    /**
     * Show graphical overview
     *
     * @param value yes, no
     */
    public void setShowOverview(boolean value)
    {
        params.put("SHOW_OVERVIEW", value ? "yes" : "no");
    }

    protected void setDefaultParamters()
    {
        setAlignments(500);
        setAlignmentView("pairwise");
        setDescriptions(500);
        setLowExpect(0);
        setHighExpect(0);
        setObject("Alignment");
        setFormatType("HTML");
        setShowGI(false);
        setResultsFile(false);
        setService("plain");
        setShowOverview(true);
    }

    protected String getCommandName()
    {
        return "Get";
    }

    protected void processResult(InputStream in) throws Exception
    {
        File temp_file = File.createTempFile("blst", "tmp");

        PrintWriter buffer = new PrintWriter(new FileWriter(temp_file));
        BufferedReader line_reader = new BufferedReader(new InputStreamReader(in));
        String line;
        status = null;

        while ((line = line_reader.readLine()) != null)
        {
            buffer.println(line);

            if (line.indexOf("Status=") > -1)
                status = getValue(line).toUpperCase();
        }

        buffer.flush();
        buffer.close();

        if (params.get("FORMAT_TYPE").equals("XML") && status==null)
            status = STATUS_READY;

        Reader buffered_data = new FileReader(temp_file);

        if (status.equals(STATUS_READY))
        {
            parseResults(buffered_data);
        }

        buffered_data.close();
        temp_file.delete();
    }

    public abstract void parseResults(Reader in) throws Exception;

    protected String getValue(String line)
    {
        return line.substring(line.indexOf("=") + 1).trim();
    }

    public String getStatus()
    {
        return status;
    }

    protected String status = STATUS_NONE;

    public static final String STATUS_READY = "READY";
    public static final String STATUS_WAITING = "WAITING";
    public static final String STATUS_NONE = "NONE";
}