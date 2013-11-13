/*
 * Translator.java
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
import java.io.Serializable;

/**
 * The fundamental interface for translating user input in an UI field into an
 * object. Some translators must be bound to a specific field. Others can be
 * used on any field of a given type. See {@code ComboBoxTranslator} and
 * {@code TextComponentTranslator} for examples of each case, respectively.
 * <p>
 * The contract of this interface is that any {@code Object} or {@code String}
 * that can be read from a component with {@code readValue()} or {@code
 * readString()} must be allowed to be written back to it. However, not every
 * object which can be written to a component with {@code writeValue()} is
 * required to be valid when read back. As a consequence, the {@code
 * writeValue()} method takes arguments of type {@code Object} rather than of
 * generic type {@code T}. Translators must be able to handle at least {@code
 * String} objects in addition the their usual output types when writing to a
 * field. Other types may be premitted as well, depending on the individual
 * translator. Additionally, {@code readValue()} and {@code readString()} should
 * both either throw an exception or return a valid value. There should never be
 * a case where {@code readValue()} can parse the component's contents while
 * {@code readString()} can not, or vice versa.
 *
 * @param <T> The raw type of the field. This can be anything at all. Popular
 * choices include {@code Number}, {@code String}, or array types.
 * @param <U> The type of UI component that this translator applies to. The type
 * must extend {@code java.awt.Component}.
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 */
public interface Translator<T, U extends Component> extends Serializable
{
    /**
     * Converts a value in the specified form field into an Object.
     *
     * @param uiComponent the component to read from.
     * @return the value stored in the component.
     * @throws TranslatorException if the value could not be extracted due to
     * a formatting error.
     * @since 1.0.0
     */
    public T readValue(U uiComponent) throws TranslatorException;

    /**
     * Converts a value in the specified form field into a string instread of
     * the default return type. This method is particularly useful for exporting
     * fields for which the default type does not convert well with {@code
     * toString()}.
     *
     * @param uiComponent the component to read from.
     * @return the value stored in the component.
     * @throws TranslatorException if the value could not be extracted due to
     * a formatting error.
     * @since 1.0.0
     */
    public String readString(U uiComponent) throws TranslatorException;

    /**
     * Writes a value to the component. This value does not necessarily have to
     * be legible with {@code readValue()}. On the other hand, any value read by
     * either {@code readValue()} or {@code readString()} should be able to be
     * written back without causing an exception.
     *
     * @param value the value to write to the component. This value does not
     * necessarily have to be of a type that can be read back from the
     * component.
     * @param uiComponent the component to write to.
     * @throws TranslatorException if the value being written is incompatible
     * with the component.
     * @since 1.0.0
     */
    public void writeValue(Object value, U uiComponent) throws TranslatorException;
}
