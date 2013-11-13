/*
 * ColorButtonTranslator.java
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

import java.awt.Color;
import com.madphysicist.tools.swing.JColorButton;
import com.madphysicist.tools.util.ColorUtilities;

/**
 * Translator for {@code JColorButton}s. This class follows a singleton pattern
 * because it has no state.
 *
 * @see JColorButton
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 19 June 2013: Joseph Fox-Rabinovitz: Created
 */
public class ColorButtonTranslator implements Translator<Color, JColorButton>
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
    private static final ColorButtonTranslator instance = new ColorButtonTranslator();

    /**
     * An empty constructor. This constructor is not private to allow
     * subclassing.
     *
     * @since 1.0.0
     */
    protected ColorButtonTranslator() {}

    /**
     * Reads the current color from the specified button.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException this method does not throw this particular
     * exception. The {@code throws} clause is provided so that subclasses can
     * throw it.
     * @since 1.0.0
     */
    @Override public Color readValue(JColorButton uiComponent) throws TranslatorException
    {
        return uiComponent.getColor();
    }

    /**
     * Returns a string representation of the current color in the specified
     * button. The color is converted into a hex string by using {@code
     * ColorUtilities}. No attempt is made to find a text name for the color,
     * even if it is an exact match for an HTML color.
     *
     * @param uiComponent {@inheritDoc}
     * @return {@inheritDoc}
     * @throws TranslatorException this method does not throw this particular
     * exception. The {@code throws} clause is provided so that subclasses can
     * throw it.
     * @see ColorUtilities#toString(Color)
     * @since 1.0.0
     */
    @Override public String readString(JColorButton uiComponent) throws TranslatorException
    {
        return ColorUtilities.toString(readValue(uiComponent), true);
    }

    /**
     * Writes the specified value to the button. The value must be a {@code
     * Color} or a {@code String} that is parseable by {@code
     * ColorUtilities.parseColor()}.
     *
     * @param value {@inheritDoc}
     * @param uiComponent {@inheritDoc}
     * @throws TranslatorException {@inheritDoc}
     * @see ColorUtilities#parseColor(String)
     * @since 1.0.0
     */
    @Override public void writeValue(Object value, JColorButton uiComponent) throws TranslatorException
    {
        uiComponent.setColor(getValidValue(value, uiComponent));
    }

    /**
     * Computes a writeable value from the input. If the input is a {@code
     * Color}, it is returned directly. If it is a {@code String}, an attempt is
     * made to parse it. This method is used by {@code writeValue()} to generate
     * a valid color or to throw an exception.
     *
     * @param value the value, which may already be a {@code Color} or a {@code
     * String}.
     * @param uiComponent the component that the value will be written to. This
     * arcument is only used to generate exceptions if the value is unparseable.
     * @return the input value converted into a {@code Color} object.
     * @throws TranslatorException if the value is not a color and could not be
     * converted into one.
     * @see #writeValue(Object, JColorButton)
     * @see ColorUtilities#parseColor(String)
     * @since 1.0.0
     */
    private Color getValidValue(Object value, JColorButton uiComponent) throws TranslatorException
    {
        if(value instanceof Color)
            return (Color)value;
        if(value instanceof String) {
            try {
                return ColorUtilities.parseColor(value.toString());
            } catch(IllegalArgumentException iae) {
                throw new TranslatorException("Invalid color string \"" + value + "\"", iae, this, uiComponent);
            }
        }
        throw new TranslatorException("Incompatible value", this, uiComponent);
    }

    /**
     * Retrieves the singleton instance of this class.
     *
     * @return the singleton of this class.
     * @since 1.0.0
     */
    public static ColorButtonTranslator getInstance()
    {
        return instance;
    }
}
