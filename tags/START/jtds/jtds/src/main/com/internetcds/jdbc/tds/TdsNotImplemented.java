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



package com.internetcds.jdbc.tds;

import com.internetcds.jdbc.tds.TdsException;

/**
 * exception raised by methods that aren't implemented.
 *
 * This class will eventually go away
 *
 * @author Craig Spannring
 * @version  $Id: TdsNotImplemented.java,v 1.1.1.1 2001-08-10 01:44:34 skizz Exp $
 */
public class TdsNotImplemented extends TdsException
{
   public static final String cvsVersion = "$Id: TdsNotImplemented.java,v 1.1.1.1 2001-08-10 01:44:34 skizz Exp $";


   public TdsNotImplemented(String msg)
   {
      super("Not implemented.  " + msg);
   }
}
