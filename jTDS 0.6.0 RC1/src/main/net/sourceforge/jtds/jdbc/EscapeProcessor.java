//
// Copyright 1998 CDS Networks, Inc., Medford Oregon
//
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. All advertising materials mentioning features or use of this software
//    must display the following acknowledgement:
//      This product includes software developed by CDS Networks, Inc.
// 4. The name of CDS Networks, Inc.  may not be used to endorse or promote
//    products derived from this software without specific prior
//    written permission.
//
// THIS SOFTWARE IS PROVIDED BY CDS NETWORKS, INC. ``AS IS'' AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED.  IN NO EVENT SHALL CDS NETWORKS, INC. BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
// OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
// HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
// LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
// OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE.
//

package net.sourceforge.jtds.jdbc;

import java.sql.*;

abstract public class EscapeProcessor
{
    public static final String cvsVersion = "$Id: EscapeProcessor.java,v 1.4 2002-10-22 09:50:10 alin_sinpalean Exp $";

    String   input;
    private static final String ESCAPE_PREFIX_DATE = "d ";
    private static final String ESCAPE_PREFIX_TIME = "t ";
    private static final String ESCAPE_PREFIX_TIMESTAMP = "ts ";
    private static final String ESCAPE_PREFIX_OUTER_JOIN = "oj ";
    private static final String ESCAPE_PREFIX_FUNCTION = "fn ";
    private static final String ESCAPE_PREFIX_CALL = "call ";
    private static final String ESCAPE_PREFIX_ESCAPE_CHAR = "escape ";

    public EscapeProcessor(String sql)
    {
        input = sql;
    }

    abstract public String expandDBSpecificFunction(String escapeSequence) throws SQLException;

    /**
     * Is the string made up only of digits?
     * <p>
     * <b>Note:</b>  Leading/trailing spaces or signs are not considered digits.
     *
     * @return true if the string has only digits, false otherwise.
     */
    private static boolean validDigits(String str)
    {
        for( int i=0; i<str.length(); i++ )
            if( !Character.isDigit(str.charAt(i)) )
                return false;

        return true;
    }

    /**
     * Given a string and an index into that string return the index
     * of the next non-whitespace character.
     *
     * @return index of next non-whitespace character.
     */
    private static int skipWhitespace(String str, int i)
    {
        while( i<str.length() && Character.isWhitespace(str.charAt(i)) )
           i++;

        return i;
    }

    /**
     * Given a string and an index into that string, advanvance the index
     * iff it is on a quote character.
     *
     * @return the next position in the string iff the current character is a
     *         quote
     */
    private static int skipQuote(String str, int i)
    {
        // skip over the leading quote if it exists
        if( i<str.length() && (str.charAt(i)=='\'' || str.charAt(i)=='"') )
            // XXX Note-  The spec appears to prohibit the quote character,
            // but many drivers allow it.  We should probably control this
            // with a flag.
            i++;

        return i;
    }

    /**
     * Convert a JDBC SQL escape date sequence into a datestring recognized by SQLServer.
     */
    private static String getDate(String str) throws SQLException
    {
        int   i;

        // skip over the "d "
        i = 2;

        // skip any additional spaces
        i = skipWhitespace(str, i);

        i = skipQuote(str, i);

        // i is now up to the point where the date had better start.
        if( str.length()-i<10 || str.charAt(i+4)!='-' || str.charAt(i+7)!='-' )
            throw new SQLException("Malformed date");

        String year  = str.substring(i, i+4);
        String month = str.substring(i+5, i+5+2);
        String day   = str.substring(i+5+3, i+5+3+2);

        // Make sure the year, month, and day are numeric
        if( !validDigits(year) || !validDigits(month) || !validDigits(day) )
            throw new SQLException("Malformed date");

        // Make sure there isn't any garbage after the date
        i = i+10;
        i = skipWhitespace(str, i);
        i = skipQuote(str, i);
        i = skipWhitespace(str, i);

        if( i<str.length() )
            throw new SQLException("Malformed date");

        return "'" + year + month + day + "'";
    }

    /**
     * Convert a JDBC SQL escape time sequence into a time string recognized by SQLServer.
     */
    private static String getTime(String str) throws SQLException
    {
        int   i;

        // skip over the "t "
        i = 2;

        // skip any additional spaces
        i = skipWhitespace(str, i);

        i = skipQuote(str, i);

        // i is now up to the point where the date had better start.
        if( str.length()-i<8 || str.charAt(i+2)!=':' || str.charAt(i+5)!=':' )
            throw new SQLException("Malformed time");

        String hour   = str.substring(i, i+2);
        String minute = str.substring(i+3, i+3+2);
        String second = str.substring(i+3+3, i+3+3+2);

        // Make sure the year, month, and day are numeric
        if( !validDigits(hour) || !validDigits(minute) || !validDigits(second) )
            throw new SQLException("Malformed time");

        // Make sure there isn't any garbage after the time
        i = i+8;
        i = skipWhitespace(str, i);
        i = skipQuote(str, i);
        i = skipWhitespace(str, i);

        if( i<str.length() )
            throw new SQLException("Malformed time");

        return "'" + hour + ":" + minute + ":" + second + "'";
    }

    /**
     * Convert a JDBC SQL escape timestamp sequence into a date-time string recognized by SQLServer.
     */
    private static String getTimestamp(String str) throws SQLException
    {
        int   i;

        // skip over the "d "
        i = 2;

        // skip any additional spaces
        i = skipWhitespace(str, i);

        i = skipQuote(str, i);

        // i is now at the point where the date had better start.
        if( (str.length()-i)<19 || str.charAt(i+4)!='-' || str.charAt(i+7)!='-' )
            throw new SQLException("Malformed date");

        String year  = str.substring(i, i+4);
        String month = str.substring(i+5, i+5+2);
        String day   = str.substring(i+5+3, i+5+3+2);

        // Make sure the year, month, and day are numeric
        if( !validDigits(year) || !validDigits(month) || !validDigits(day) )
            throw new SQLException("Malformed date");

        // Make sure there is at least one space between date and time
        i = i+10;
        if( !Character.isWhitespace(str.charAt(i)) )
            throw new SQLException("Malformed date");

        // skip the whitespace
        i = skipWhitespace(str, i);

        // see if it could be a time
        if( str.length()-i<8 || str.charAt(i+2)!=':' || str.charAt(i+5)!=':' )
            throw new SQLException("Malformed time");

        String hour     = str.substring(i, i+2);
        String minute   = str.substring(i+3, i+3+2);
        String second   = str.substring(i+3+3, i+3+3+2);
        String fraction = "000";
        i = i+8;
        if( str.length()>i && str.charAt(i)=='.' )
        {
            fraction = "";
            i++;
            while( str.length()>i && validDigits(str.substring(i,i+1)) )
                fraction = fraction + str.substring(i,++i);

            if( fraction.length() > 3 )
                fraction = fraction.substring(0,3);
            else
                while( fraction.length()<3 )
                    fraction = fraction + "0";
        }


        // Make sure there isn't any garbage after the time
        i = skipWhitespace(str, i);
        i = skipQuote(str, i);
        i = skipWhitespace(str, i);

        if (i<str.length())
        {
           throw new SQLException("Malformed date");
        }

        return "'" + year + month + day + " "
                + hour + ":" + minute + ":" + second + "." + fraction + "'";
    }

    public String expandEscape(String escapeSequence) throws SQLException
    {
        String str    = new String(escapeSequence);
        String result = null;

        if( startsWithIgnoreCase(str, ESCAPE_PREFIX_FUNCTION) )
        {
            str = str.substring(ESCAPE_PREFIX_FUNCTION.length());
            result = expandCommonFunction(str);

            if( result == null )
                result = expandDBSpecificFunction(str);

            if( result == null )
                result = str;
        }
        else if( startsWithIgnoreCase(str, ESCAPE_PREFIX_CALL) ||
            (str.startsWith("?=") && startsWithIgnoreCase(str.substring(2).trim(), ESCAPE_PREFIX_CALL)) )
        {
            boolean returnsVal = str.startsWith("?=");
            str = str.substring(str.indexOf("call")+4).trim();
            int pPos = str.indexOf('(');
            if( pPos >= 0 )
            {
                if( str.charAt(str.length()-1) != ')' )
                    throw new SQLException("Malformed procedure call: "+str);
                result = "exec "+(returnsVal ? "?=" : "")+str.substring(0, pPos)+
                    " "+str.substring(pPos+1, str.length()-1);
            }
            else
                result = "exec "+(returnsVal ? "?=" : "")+str;
        }
        else if( startsWithIgnoreCase(str, ESCAPE_PREFIX_DATE) )
            result = getDate(str);
        else if( startsWithIgnoreCase(str, ESCAPE_PREFIX_TIME) )
            result = getTime(str);
        else if( startsWithIgnoreCase(str, ESCAPE_PREFIX_TIMESTAMP) )
            result = getTimestamp(str);
        else if( startsWithIgnoreCase(str, ESCAPE_PREFIX_OUTER_JOIN) )
            result = str.substring(ESCAPE_PREFIX_OUTER_JOIN.length()).trim();
        else
            throw new SQLException("Unrecognized escape sequence: " + escapeSequence);

        return result;
    }

    /**
     * Expand functions that are common to both SQLServer and Sybase
     */
    public String expandCommonFunction(String str) throws SQLException
    {
        String result = null;
        int pPos = str.indexOf('(');

        if( pPos < 0 )
            throw new SQLException("Malformed function escape: "+str);
        String fName = str.substring(0, pPos).trim();

        // @todo Implement this in a smarter way
        if( fName.equalsIgnoreCase("user") )
            result = "user_name" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("database") )
            result = "db_name" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("ifnull") )
            result = "isnull" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("now") )
            result = "getdate" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("atan2") )
            result = "atn2" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("length") )
            result = "len" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("locate") )
            result = "charindex" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("repeat") )
            result = "replicate" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("insert") )
            result = "stuff" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("lcase") )
            result = "lower" + str.substring(pPos);
        else if( fName.equalsIgnoreCase("ucase") )
            result = "upper" + str.substring(pPos);

        return result;
    }

    public String nativeString() throws SQLException
    {
        return nativeString(input, '\\');
    }

    private String nativeString(String sql, char escapeCharacter) throws SQLException
    {
        StringBuffer result = new StringBuffer(sql.length());

        String escape = "";
        int    i;

        // Simple finite state machine.  Bonehead, but it works.
        final int   normal                        = 0;

        final int   inString                      = 1;
        final int   inStringWithBackquote         = 2;

        final int   inEscape                      = 3;
        final int   inEscapeInString              = 4;
        final int   inEscapeInStringWithBackquote = 5;

        int         state = normal;
        char        ch;

        int escapeStartedAt = -1;
        i = 0;
        while( i < sql.length() )
        {
            ch = sql.charAt(i);
            switch( state )
            {
                case normal:
                {
                    if( ch == '{' )
                    {
                        escapeStartedAt = i;
                        state = inEscape;
                        escape = "";
                    }
                    else
                    {
                       result.append(ch);
                       if( ch == '\'' )
                           state = inString;
                    }
                    break;
                }
                case inString:
                case inStringWithBackquote:
                {
                    if( (i+1)<sql.length() && ch==escapeCharacter &&
                        (sql.charAt(i+1)=='_' || sql.charAt(i+1)=='%') )
                    {
                        i++;
                        ch = sql.charAt(i);
                        result.append('\\');
                        result.append(ch);
                    }
                    else
                    {
                        result.append(ch);
                        if( state == inStringWithBackquote )
                        {
                            state = inString;
                        }
                        else
                        {
                            if( ch == '\\' ) state = inStringWithBackquote;
                            if( ch == '\'' ) state = normal;
                        }
                    }
                    break;
                }
                case inEscape:
                {
                    if( ch == '}' )
                    {
                        // XXX Is it always okay to trim leading and trailing blanks?
                        escape = escape.trim();

                        // At this point there are a couple of things to
                        // consider.  First, if the escape is of the form
                        // "{escape 'c'} but it is not at the end of the SQL
                        // we consider that a malformed SQL string.  If it
                        // is the "{escape 'c'}" clause and it is at the end
                        // of the string then we have to go through and
                        // reparse this whole thing again, this time with an
                        // escape character.  Any other escape is handled in
                        // the expandEscape method()

                        if( startsWithIgnoreCase(escape, ESCAPE_PREFIX_ESCAPE_CHAR) )
                        {
                            char c;

                            // make sure it is the last thing in the sql
                            if( i+1 != sql.length() )
                                throw new SQLException("Malformed statement. Escape clause must " +
                                    "be at the end of the query.");

                            /** @todo Avoid reparsing the query with the escape character */
                            // parse the sql again, this time without the ending string but with the
                            // escape character set

                            c = findEscapeCharacter(escape);

                            result.delete(0,result.length());
                            result.append(nativeString(sql.substring(0, escapeStartedAt), c));
                            state = normal;
                        }
                        else
                        {
                            state = normal;
                            result.append(expandEscape(escape));
                            escapeStartedAt = -1;
                        }
                    }
                    else
                    {
                        escape = escape + ch;
                        if( ch == '\'' ) state = inEscapeInString;
                    }
                    break;
                }
                case inEscapeInString:
                case inEscapeInStringWithBackquote:
                {
                    escape = escape + ch;

                    if( state == inEscapeInStringWithBackquote )
                        state = inEscapeInString;
                    else
                    {
                        // SAfe This backslash in escape string is wrong. Someone might want to use backslash as escape
                        //      character. Then it would look like: {escape '\'} <- no backslash escaping.
//                        if( ch == '\\' ) state = inEscapeInStringWithBackquote;
                        if( ch == '\'' ) state = inEscape;
                    }
                    break;
                }
                default:
                    throw new SQLException("Internal error.  Unknown state in FSM");
            }
            i++;
        }

        if( state!=normal && state!=inString )
           throw new SQLException("Syntax error in SQL escape syntax");

        return result.toString();
    } // nativeString()

    static char findEscapeCharacter(String escape)
        throws SQLException
    {
        String str = escape.trim();

        if( !str.substring(0, 6).equalsIgnoreCase("escape") )
            throw new SQLException("Internal Error");

        str = str.substring(6).trim();
        if( str.length()!=3 || str.charAt(0)!='\'' || str.charAt(2)!='\'' )
            throw new SQLException("Malformed escape clause: |" + escape + "|");

        return str.charAt(1);
    }

    public static boolean startsWithIgnoreCase(String s, String prefix)
    {
        if( s.length() < prefix.length() )
            return false;

        for( int i=prefix.length()-1; i>=0; i-- )
            if( Character.toLowerCase(s.charAt(i)) != Character.toLowerCase(prefix.charAt(i)) )
                return false;

        return true;
    }
}
