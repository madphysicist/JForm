/*
 * TranslatorComponentException.java
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
 * An exception that indicates a mismatch between an {@code Translator} and
 * the {@code Component} that it is attempting to translate to or from.
 *
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 */
public class TranslatorComponentException extends ClassCastException
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
     * exception has no cause. A default message describing the exception will
     * be constructed from the component and translator.
     *
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorComponentException(Translator<?, ?> translator, Component component)
    {
        this(getMessage(translator, component), null, translator, component);
    }

    /**
     * Constructs an exception with the specified message, translator and
     * component. The exception has no cause. The message will not be modified.
     *
     * @param msg a message describing the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorComponentException(String msg, Translator<?, ?> translator, Component component)
    {
        this(msg, null, translator, component);
    }

    /**
     * Constructs an exception with the specified cause, translator and
     * component. A default message describing the exception will be constructed
     * from the component and translator.
     *
     * @param cause the cause of the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorComponentException(Throwable cause, Translator<?, ?> translator, Component component)
    {
        this(getMessage(translator, component), cause, translator, component);
    }

    /**
     * Constructs an exception with the specified message, cause, translator and
     * component. The message will not be modified.
     *
     * @param msg a message describing the exception.
     * @param cause the cause of the exception.
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the editor component in which the exception occurred.
     * May be {@code null}.
     * @since 1.0.0
     */
    public TranslatorComponentException(String msg, Throwable cause, Translator<?, ?> translator, Component component)
    {
        super((msg == null) ? getMessage(translator, component) : msg);
        if(cause != null)
            initCause(cause);
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

    /**
     * Creates a default exception message. The message assumes that the
     * exception is being thrown due to a type mismatch between the translator
     * and an input component. The message includes information about the types
     * of the translator and the component.
     *
     * @param translator the translator in which the exception occurred. May be
     * {@code null}.
     * @param component the component that did not match the requirement of the
     * translator. May be {@code null}.
     * @return a message describing the mismatch between the translator and the
     * component.
     * @since 1.0.0
     */
    private static String getMessage(Translator<?, ?> translator, Component component)
    {
        StringBuilder sb = new StringBuilder();

        if(translator == null)
            sb.append("Null Translator");
        else
            sb.append("Transslator of type ").append(translator.getClass().getName());

        sb.append(" unable to operate on ");

        if(component == null)
            sb.append("Null Component");
        else
            sb.append("Component of type ").append(component.getClass().getName());

        return sb.toString();
    }
}
