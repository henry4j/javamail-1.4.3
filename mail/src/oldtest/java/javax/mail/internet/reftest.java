/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

//package javax.mail.internet.tests;

import javax.mail.*;
import javax.mail.internet.*;

/**
 * Test setting of the References header.
 *
 * @author Bill Shannon
 */
public class reftest {
    private static Session session;
    private static int errors;

    public static void main(String[] argv) throws Exception {
	session = Session.getInstance(System.getProperties());
	/*
	 * Test cases:
	 * 
	 * Message-Id	References	In-Reply-To	Expected Result
	 */
	test(null,	null,		null,		null);
	test(null,	null,		"<1@a>",	"<1@a>");
	test(null,	"<2@b>",	null,		"<2@b>");
	test(null,	"<2@b>",	"<1@a>",	"<2@b>");
	test("<3@c>",	null,		null,		"<3@c>");
	test("<3@c>",	null,		"<1@a>",	"<1@a> <3@c>");
	test("<3@c>",	"<2@b>",	null,		"<2@b> <3@c>");
	test("<3@c>",	"<2@b>",	"<1@a>",	"<2@b> <3@c>");
	System.exit(errors);
    }

    private static void test(String msgid, String ref, String irt, String res)
				throws MessagingException {
	MimeMessage msg = new MimeMessage(session);
	msg.setFrom();
	msg.setRecipients(Message.RecipientType.TO, "you@example.com");
	msg.setSubject("test");
	if (msgid != null)
	    msg.setHeader("Message-Id", msgid);
	if (ref != null)
	    msg.setHeader("References", ref);
	if (irt != null)
	    msg.setHeader("In-Reply-To", irt);
	msg.setText("text");

	MimeMessage reply = (MimeMessage)msg.reply(false);
	String rref = reply.getHeader("References", " ");

	if ((rref == null && res == null) || (rref != null && rref.equals(res)))
	    ;	// success
	else {
	    System.out.println("FAILED: expected " + res + ", got " + rref);
	    errors++;
	}
    }
}
