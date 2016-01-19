/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.felix.gogo.runtime;

import java.io.EOFException;

/*
 * Test features of the new parser/tokenizer, many of which are not supported
 * by the original parser.
 */
public class TestParser2 extends BaseTestCase
{
    public void testComment() throws Exception
    {
        m_ctx.addCommand("echo", this);

        assertEquals("file://wibble#tag", m_ctx.execute("echo file://wibble#tag"));
        assertEquals("file:", m_ctx.execute("echo file: //wibble#tag"));

        assertEquals("PWD/*.java", m_ctx.execute("echo PWD/*.java"));
        try
        {
            m_ctx.execute("echo PWD /*.java");
            fail("expected EOFException");
        }
        catch (EOFException e)
        {
            // expected
        }

        assertEquals("ok", m_ctx.execute("// can't quote\necho ok\n"));

        // quote in comment in closure
        assertEquals("ok", m_ctx.execute("x = { // can't quote\necho ok\n}; x"));
        assertEquals("ok", m_ctx.execute("x = {\n// can't quote\necho ok\n}; x"));
        assertEquals("ok", m_ctx.execute("x = {// can't quote\necho ok\n}; x"));
    }

    public void testCoercion() throws Exception
    {
        m_ctx.addCommand("echo", this);
        // FELIX-2432
        assertEquals("null x", m_ctx.execute("echo $expandsToNull x"));
    }

    public void testStringExecution() throws Exception
    {
        m_ctx.addCommand("echo", this);
        m_ctx.addCommand("new", this);
        
        // FELIX-2433
        assertEquals("helloworld", m_ctx.execute("echo \"$(echo hello)world\""));
        
         // FELIX-1473 - allow method calls on String objects
        assertEquals("hello", m_ctx.execute("cmd = echo; eval $cmd hello"));
        assertEquals(4, m_ctx.execute("'four' length"));
        try {
            m_ctx.execute("four length");
            fail("expected: command not found: four");
        } catch (IllegalArgumentException e) {
        }
        
        // check CharSequence types are preserved
        Object b = m_ctx.execute("b = new java.lang.StringBuilder");
        assertTrue(b instanceof StringBuilder);
        assertEquals(b, m_ctx.execute("c = $b"));
    }

    public CharSequence echo(Object args[])
    {
        if (args == null)
        {
            return "null args!";
        }

        StringBuilder sb = new StringBuilder();
        for (Object arg : args)
        {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(String.valueOf(arg));
        }
        return sb.toString();
    }
    
    public Object _new(String className) throws Exception {
        return Class.forName(className).newInstance();
    }

}
