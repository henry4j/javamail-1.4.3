/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
 * Copyright 2009 Jason Mehrens. All rights reserved.
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

package com.sun.mail.util.logging;

import java.io.ObjectStreamException;
import java.security.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * An adapter class to allow the Mail API to access the LogManager properties.
 * The LogManager properties are treated as the root of all properties.
 * First, the local properties and the parent properties are searched.
 * If no value is found, then, the LogManager is searched with prefix value
 * and finally, just the key itself is searched in the LogManager.
 *
 * <p>
 * This class also emulates the LogManager functions for creating new objects
 * from string class names.  This is to support initial setup of objects such as
 * log filters, formatters, error managers, etc.
 *
 * <p>
 * This class should never be exposed outside of this package.  Keep this
 * class package private (default access).
 * 
 * @author Jason Mehrens
 * @since JavaMail 1.4.3
 */
final class LogManagerProperties extends Properties {

    private static final long serialVersionUID = -2239983349056806252L;

    /**
     * Caches the LogManager so we only read the config once.
     */
    final static LogManager manager = LogManager.getLogManager();

    /**
     * This code is modifed from the LogManager, which explictly states
     * searching the system class loader first, then the context class loader.
     * There is resistance (compatiblity) to change this behavior to simply
     * searching the context class loader.
     * @param name full class name
     * @return the class.
     * @throws ClassNotFoundException if not found.
     */
    static final Class findClass(String name) throws ClassNotFoundException {
        ClassLoader[] loaders = getClassLoaders();
        Class clazz;
        if (loaders[0] != null) {
            try {
                clazz = Class.forName(name, false, loaders[0]);
            } catch (ClassNotFoundException tryContext) {
                clazz = tryLoad(name, loaders[1]);
            }
        } else {
            clazz = tryLoad(name, loaders[1]);
        }
        return clazz;
    }

    private static Class tryLoad(String name, ClassLoader l) throws ClassNotFoundException {
        if (l != null) {
            return Class.forName(name, false, l);
        } else {
            return Class.forName(name);
        }
    }

    private static ClassLoader[] getClassLoaders() {
        return (ClassLoader[]) AccessController.doPrivileged(new PrivilegedAction() {

            public Object run() {
                final ClassLoader[] loaders = new ClassLoader[2];
                try {
                    loaders[0] = ClassLoader.getSystemClassLoader();
                } catch (SecurityException ignore) {
                    loaders[0] = null;
                }

                try {
                    loaders[1] = Thread.currentThread().getContextClassLoader();
                } catch (SecurityException ignore) {
                    loaders[1] = null;
                }
                return loaders;
            }
        });
    }

    private final String prefix;

    /**
     * Creates a log manager properties object.
     * @param parent the parent properties.
     * @param prefix the namespace prefix.
     * @throws NullPointerException if <tt>prefix</tt> is <tt>null</tt>.
     */
    LogManagerProperties(final Properties parent, final String prefix) {
        super(parent);
        if(prefix == null) {
            throw new NullPointerException();
        }
        this.prefix = prefix;
    }

    /**
     * Performs the super action, then searches the log manager
     * by the prefix property, and then by the key itself.
     * @param key a non null key.
     * @return the value for that key.
     */
    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null && key.length() > 0) {
            value = manager.getProperty(prefix + '.' + key);
            if (value == null) {
                value = manager.getProperty(key);
            }
        }
        return value;
    }

    /**
     * It is assumed that this method will never be called.
     * No way to get the property names from LogManager.
     * @return the property names
     */
    public Enumeration propertyNames() {
        assert false;
        return super.propertyNames();
    }

    /**
     * It is assumed that this method will never be called.
     * The prefix value is not used for the equals method.
     * @param o any object or null.
     * @return true if equal, otherwise false.
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o instanceof Properties == false) {
            return false;
        }
        assert false;
        return super.equals(o);
    }

    /**
     * It is assumed that this method will never be called.  See equals.
     * @return the hash code.
     */
    public int hashCode() {
        assert false;
        return super.hashCode();
    }

    /**
     * It is assumed that this method will never be called.
     * @return a new properties object copied from this one.
     * @throws ObjectStreamException if there is a problem.
     */
    private synchronized Object writeReplace() throws ObjectStreamException {
        assert false;
        final Properties out = new Properties(defaults);
        if (!super.isEmpty()) { //should always be empty.
            out.putAll(this);
        }
        return out;
    }
}
