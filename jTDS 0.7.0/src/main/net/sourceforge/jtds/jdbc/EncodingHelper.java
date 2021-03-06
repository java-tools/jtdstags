package net.sourceforge.jtds.jdbc;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * Helper class to handle server character set conversion.
 *
 * @author Stefan Bodewig <a href="mailto:stefan.bodewig@megabit.net">stefan.bodewig@megabit.net</a>
 * @version  $Id: EncodingHelper.java,v 1.4 2003-12-16 19:08:48 alin_sinpalean Exp $
 */
public class EncodingHelper
{
    public static final String cvsVersion = "$Id: EncodingHelper.java,v 1.4 2003-12-16 19:08:48 alin_sinpalean Exp $";

    /**
     * Array containig the bytes 0x00 - 0xFF.
     */
    private static byte[] convArray;
    /**
     * Hashtable holding instances for all known encodings.
     */
    private static Hashtable knownEncodings;
    /**
     * Simple boolean to ensure we initialize once and only once.
     */
    private static boolean initialized;

    /**
     * The name of the encoding.
     */
    private String name;
    /**
     * Is this a DBCS charset (does it need more than one byte per character)?
     */
    private boolean wideChars;

    static
    {
        initialize();
    }

    /**
     * Initialize the static variables.
     * <p>
     * Will be called from the static block above, but some VMs (notably
     * Microsoft's) won't run this.
     */
    private static void initialize()
    {
        convArray = new byte[256];
        for( int i=0; i<256; i++ )
            convArray[i] = (byte)i;

        knownEncodings = new Hashtable();
        // SAfe: SQL Server returns iso_1, but it's actually Cp1252
        try
        {
            knownEncodings.put("iso_1", new EncodingHelper("Cp1252"));
        }
        catch( UnsupportedEncodingException ex )
        {}
        initialized = true;
    }

    /**
     * private so only the static accessor can be used.
     */
    private EncodingHelper(String name) throws UnsupportedEncodingException
    {
        this.name = name;
        // SAfe Hope this works in all cases.
        wideChars = new String(convArray, name).length()!=convArray.length;
    }

    /**
     * Translate the String into a byte[] in the server's encoding.
     */
    public byte[] getBytes(String value)
    {
        try
        {
            return value.getBytes(name);
        }
        catch( UnsupportedEncodingException uee )
        {
            return value.getBytes();
        }
    }

    /**
     * Translate the byte[] from the server's encoding to a Unicode String.
     */
    public String getString(byte[] value)
    {
        return getString(value, 0, value.length);
    }

    /**
     * Translate part of the byte[] from the server's encoding to a
     * Unicode String.
     *
     * The subarray starting at index off and extending to off+len-1
     * is translated.
     */
    public String getString(byte[] value, int off, int len)
    {
        try
        {
            return new String(value, off, len, name);
        }
        catch( UnsupportedEncodingException uee )
        {
            return new String(value, off, len);
        }
    }

    /**
     * Is this a DBCS charset (does it need more than one byte per character)?
     */
    public boolean isDBCS() {return wideChars;}

    /**
     * Can the given String be converted to the server's charset?
     */
    public boolean canBeConverted(String value)
    {
        try
        {
            // SAfe Not very efficient, since a String is always created, but it's the only way I
            //      could think of. Maybe some Reader/Writer combination would have been better
            //      but...
            return new String(value.getBytes(name), name).equals(value);
        }
        catch( UnsupportedEncodingException ex )
        {
            // SAfe No way.
            return false;
        }
    }

    /**
     * Return the helper object for the given encoding.
     */
    public static EncodingHelper getHelper(String encodingName)
    {
        if( !initialized )
            synchronized( net.sourceforge.jtds.jdbc.EncodingHelper.class )
            {
                if( !initialized )
                    initialize();
            }

        EncodingHelper res = (EncodingHelper)knownEncodings.get(encodingName);

        if( res == null )
        {
            try { res = new EncodingHelper(encodingName); }
            catch( UnsupportedEncodingException ex ) { }
        }
        // SAfe Try prepending Cp or MS to the character set. This should
        //      cover all the character sets known by SQL Server.
        if( res == null )
        {
            try { res = new EncodingHelper("Cp"+encodingName); }
            catch( UnsupportedEncodingException ex ) { }
        }
        if( res == null )
        {
            try { res = new EncodingHelper("MS"+encodingName); }
            catch( UnsupportedEncodingException ex ) { }
        }

        if( res != null )
            knownEncodings.put(encodingName, res);

        return res;
    }

    public String getName()
    {
        return name;
    }
}
