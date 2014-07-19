/*
 * TranslatorException.java (Class: com.madphysicist.jform.translate.TranslatorException)
 *
 * Mad Physicist JForm Project
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 by Joseph Fox-Rabinovitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.madphysicist.jform.translate;

import java.awt.Component;

/**
 * An exception that indicates failure to read or write to a Translator. The
 * exception may contain more detailed information about its cause, than just a
 * message. {@code TranslatorException}s indicate an error reading from or
 * writing to a valid editor component. To indicate that the component itself is
 * invalid for the given {@code Translator}, use the runtime exception type
 * {@code TranslatorComponentException}.
 *
 * @see Translator
 * @see TranslatorComponentException
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 */
public class TranslatorException extends Exception
{
    /**
     * The version ID for serialization.
     *
     * @serial Increment the least significant three digits when compatibility
     * is not compromised by a structural change (e.g. adding a new field with
     * a sensible default value), and the upper digits when the change makes
     * serialized versions of of the class incompatible with previous releases.
     * @since 1.0.0
     */
    private static final long serialVersionUID = 1000L;

    /**
     * The translator in which the exception occurred.
     *
     * @see #getTranslator()
     * @serial
     * @since 1.0.0
     */
    private Translator<?, ?> translator;

    /**
     * The editor component in which the exception ocurred. This should be the
     * component being passed in to the translator's {@code readValue()}, {@code
     * readString()} or {@code writeValue()} methods.
     *
     * @see #getComponent()
     * @serial
     * @since 1.0.0
     */
    private Component component;

    /**
     * Constructs an exception with the specified translator and component. The
     * exception has no message and no cause. Note that the generic type of the
     * translator does not have to match the type of the component.
     *
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorException(Translator<?, ?> translator, Component component)
    {
        this(null, null, translator, component);
    }

    /**
     * Constructs an exception with the specified message, translator and
     * component. The exception has no cause. Note that the generic type of the
     * translator does not have to match the type of the component.
     *
     * @param msg a message describing the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorException(String msg, Translator<?, ?> translator, Component component)
    {
        this(msg, null, translator, component);
    }

    /**
     * Constructs an exception with the specified cause, translator and
     * component. The exception has no message. Note that the generic type of
     * the translator does not have to match the type of the component.
     *
     * @param cause the cause of the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorException(Throwable cause, Translator<?, ?> translator, Component component)
    {
        this(null, cause, translator, component);
    }

    /**
     * Constructs an exception with the specified message, cause, translator and
     * component. Note that the generic type of the translator does not have to
     * match the type of the component.
     *
     * @param msg a message describing the exception.
     * @param cause the cause of the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorException(String msg, Throwable cause, Translator<?, ?> translator, Component component)
    {
        super(msg, cause);
        this.translator = translator;
        this.component = component;
    }

    /**
     * Returns the translator in which the exception occurred.
     *
     * @return the translator in which the exception occurred. May be {@code
     * null} if a translator was not initialized for this exception.
     * @since 1.0.0
     */
    public Translator<?, ?> getTranslator()
    {
        return translator;
    }

    /**
     * Returns the editor component in which the exception ocurred. This should
     * be the component being passed in to the translator's {@code readValue()},
     * {@code readString()} or {@code writeValue()} methods.
     *
     * @return the editor component in which this exception occurred. May be
     * {@code null} if a component was not initialized for this exception.
     * @since 1.0.0
     */
    public Component getComponent()
    {
        return component;
    }
}
