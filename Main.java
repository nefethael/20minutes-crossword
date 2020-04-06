package seek20min;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Entry point.
 * 
 * @author gpercherancier
 *
 */
public class Main
{
    /** Logger. */
    private static final Log LOG = LogFactory.getLog(Main.class);

    /**
     * Main function.
     * 
     * @param args
     *            Arguments:<BR>
     *            - first is the number of copies<BR>
     *            - second is a boolean for getting solutions or not
     */
    public static void main(String args[])
    {
        // Get default printer
        PrintService printService = PrintServiceLookup.lookupDefaultPrintService();
        
        if (printService == null)
        {
            LOG.warn("No printer found!");
        }
        else
        {
        	LOG.info("Found \"" + printService.getName() + "\" as default printer");
        }
        TwentyMinutes twentyMin = new TwentyMinutes(printService);

        // Get number of copies
        int nbC = 1;
        try
        {
            nbC = Integer.valueOf(args[0]);
        }
        catch (Exception e)
        {
            nbC = 1;
            LOG.info("No number of copies set, default is 1");
        }

        // Get solution need
        boolean needSol = true;
        try
        {
            needSol = Boolean.valueOf(args[1]);
        }
        catch (Exception e)
        {
            needSol = true;
            LOG.info("Solutions are printed by default");
        }

        // Perform job
        twentyMin.run(nbC, needSol);
    }
}
