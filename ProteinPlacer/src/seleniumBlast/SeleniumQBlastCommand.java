package seleniumBlast;

import org.concordia.cs.common.url.URLParameter;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author: Farzad
 * @created: Apr 4, 2006
 */
public abstract class SeleniumQBlastCommand implements Cloneable
{
    public SeleniumQBlastCommand()
    {
        setDefaultParamters();
    }

    protected abstract void setDefaultParamters();
    protected abstract String getCommandName();
    protected abstract void processResult(InputStream in) throws Exception;

    public String toString()
    {
        Set<Map.Entry<String,String>> entries = params.entrySet();
        URLParameter parameter = new URLParameter();
        
        try
        {
            parameter.addParameter("CMD", getCommandName());

            for (Map.Entry entry : entries)
            {
                if (entry.getValue() == null || entry.getValue().toString().length() == 0)
                {
                	System.out.println("Null entry = " + entry.getKey());
                    continue;
                }

                parameter.addParameter(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("Failed to form the url. nested exception is = " + e);
        }

        return parameter.getUrlText();
    }

    protected Map<String, String> params = new HashMap<String, String>();
}
