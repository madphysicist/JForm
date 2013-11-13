/*
 * CheckBoxTranslator.java
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

import javax.swing.JCheckBox;

/**
 * Translators for JCheckBoxes.
 *
 * @param <T> The type of object read from {@code JTextComponent}s translated by
 * extensions of this class.
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 26 May 2013
 * @since 1.0.0
 */
public class CheckBoxTranslator<T> implements Translator<T, JCheckBox>
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

    private static final CheckBoxTranslator<Boolean> defaultInstance = new CheckBoxTranslator<>(Boolean.TRUE, Boolean.FALSE);

    /**
     * @serial
     * @since 1.0.0.0
     */
    private final T trueValue;

    /**
     * @serial
     * @since 1.0.0.0
     */
    private final T falseValue;

    /**
     * @serial
     * @since 1.0.0.0
     */
    private final String trueString;

    /**
     * @serial
     * @since 1.0.0.0
     */
    private final String falseString;

    public CheckBoxTranslator(T trueValue, T falseValue)
    {
        this(trueValue, falseValue, trueValue.toString(), falseValue.toString());
    }

    public CheckBoxTranslator(T trueValue, T falseValue, String trueString, String falseString)
    {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.trueString = trueString;
        this.falseString = falseString;
    }

    public T getTrue()
    {
        return trueValue;
    }

    public String getTrueString()
    {
        return trueString;
    }

    public T getFalse()
    {
        return falseValue;
    }

    public String getFalseString()
    {
        return falseString;
    }

    @Override public void writeValue(Object value, JCheckBox uiComponent) throws TranslatorException
    {
        if(trueValue.equals(value) || trueString.equals(value))
            uiComponent.setSelected(true);
        else if(falseValue.equals(value) || falseString.equals(value))
            uiComponent.setSelected(false);
        else
            throw new TranslatorException("Incompatible value", this, uiComponent);
    }

    @Override public T readValue(JCheckBox uiComponent) throws TranslatorException
    {
        return uiComponent.isSelected() ? trueValue : falseValue;
    }

    @Override public String readString(JCheckBox uiComponent) throws TranslatorException
    {
        return uiComponent.isSelected() ? trueString : falseString;
    }

    public static synchronized CheckBoxTranslator<Boolean> getInstance()
    {
        return defaultInstance;
    }
}
