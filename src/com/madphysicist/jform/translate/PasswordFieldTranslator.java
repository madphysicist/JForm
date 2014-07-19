/*
 * PasswordFieldTranslator.java (Class: com.madphysicist.jform.translate.PasswordFieldTranslator)
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

import javax.swing.JPasswordField;

/**
 * A simple translator for {@link JPasswordField}s. The values returned by this
 * class should be zeroed out and garbage collected as soon as possible when
 * they are no longer needed.
 *
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 26 Nov 2013: Joseph Fox-Rabinovitz: Created
 * @since 1.0.1
 */
public class PasswordFieldTranslator implements Translator<char[], JPasswordField>
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
     * The singleton instance of this class.
     *
     * @see #getInstance()
     * @since 1.0.0
     */
    private static final PasswordFieldTranslator INSTANCE = new PasswordFieldTranslator();

    /**
     * An empty constructor. This constructor is not private to allow
     * subclassing.
     *
     * @since 1.0.0
     */
    protected PasswordFieldTranslator() {}

    /**
     * Writes a {@code String} representation of the specified value into the
     * specified {@code JPasswordField}. This method handles {@code char[]}
     * values specially, converting them to a string before writing them to the
     * field.
     *
     * @param value {@inheritDoc}. If the value is an array of type {@code
     * char[]}, it will be converted to a {@code String}. All other types will
     * be written in their default {@code String} form.
     * @param uiComponent {@inheritDoc}
     * @throws TranslatorException if the value is incompatible with the
     * specified component. This implementation does not throw any exceptions,
     * but allows subclasses to do so.
     * @since 1.0.0
     */
    @Override public void writeValue(Object value, JPasswordField uiComponent) throws TranslatorException
    {
        if(value instanceof char[]) {
            uiComponent.setText(new String((char[])value));
        } else {
            uiComponent.setText(value.toString());
        }
    }

    /**
     * Returns a string representation of the text component if and only if it
     * could be read with {@code readValue()}. The raw {@code char[]} returned
     * by {@code readValue()} is converted into a {@code String}. Use of this
     * method is allowed but not recommended. The resulting value can not be
     * cleared, unlike a {@code char[]}, which is therefore more secure.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException if the value can not be translated. This
     * implementation passes through any exceptions thrown by {@code
     * readValue()}.
     * @see #readValue(JPasswordField)
     * @since 1.0.0
     */
    @Override public String readString(JPasswordField uiComponent) throws TranslatorException
    {
        char[] value = readValue(uiComponent);
        return (value == null) ? null : new String(value);
    }

    /**
     * 
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException {@inheritDoc}
     * @see #readString(JPasswordField)
     * @since 1.0.0
     */
    @Override public char[] readValue(JPasswordField uiComponent) throws TranslatorException
    {
        return uiComponent.getPassword();
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return the singleton of this class.
     * @since 1.0.0
     */
    public static PasswordFieldTranslator getInstance()
    {
        return INSTANCE;
    }
}
