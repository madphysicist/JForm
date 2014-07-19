/*
 * Converter.java (Package: com.madphysicist.jform.Converter)
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
package com.madphysicist.jform;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.madphysicist.jform.translate.TranslatorException;
import com.madphysicist.jform.translate.Translator;

/**
 * Manages a bridge between user input and an underlying property value. This
 * class contains a translator to read and write the property from and to a form
 * field. It has error markup information to apply to the editor component if
 * the user enters invalid data. The translation can be disabled if the
 * component is disabled.
 * <p>
 * This class allows {@code PropertyChangeListener}s to be registered to it.
 * Calls to {@code readValue()} or {@code readString()} can be requested to
 * notify the listeners when they succeed and find a new value in the field.
 * To trigger a check for updates without throwing an exception or marking up
 * the component, use {@code checkValue()}. The reason that writing to a
 * component does not cause a notification to property listeners is that the
 * written object can not necessarily be read back from the component. This
 * means that writing to a component does not constitute a committed edit.
 * However, the {@code writeChecked()} method provides a convenience method for
 * combining {@code writeValue()} and {@code checkValue()}. Note that invoking
 * {@code readValue()} or {@code readString()} directly on the {@code
 * Translator} will not trigger a check or listener notification.
 * <p>
 * Error markup can be applied to components other than the property editor
 * itself. For example, labels and various subcomponents should often be marked
 * up as well. Instances of this class require setup for the markup which will
 * mostly be standard across all editor components of a given type. To eliminate
 * manual configuration and provide a standard behavior, instances should be
 * acquired from a {@code ConverterFactory} rather than being set up manually
 * every time. The factories themselves are highly customizable and allow the
 * registration of various elements as error markup for each editor.
 *
 * @param <T> The type of raw object that can be read from this configuration.
 * @param <U> The type of the component editor for this configuration.
 * @see ConverterFactory
 * @see PropertyChangeListener
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created
 * @version 1.0.1, 25 June 2013: Joseph Fox-Rabinovitz: Added class-level
 * generics and removed the {@code remove()} methods.
 */
public class Converter<T, U extends Component> implements Serializable
{
    /**
     * The version ID for serialization.
     *
     * @serial Increment the least significant three digits when compatibility
     * is not compromised by a structural change (e.g. adding a new field with a
     * sensible default value), and the upper digits when the change makes
     * serialized versions of of the class incompatible with previous releases.
     * @since 1.0.0
     */
    private static final long serialVersionUID = 1000L;

    /**
     * The default foreground color to use for error markup of components. Note
     * that this field if of the internal type {@code ErrorColor}.
     *
     * @see #foregroundErrorColor
     * @since 1.0.0
     */
    private static final ErrorColor FOREGROUND_ERROR_COLOR = new ErrorColor(Color.RED);

    /**
     * The default background color to use for error markup of components. Note
     * that this field if of the internal type {@code ErrorColor}.
     *
     * @see #backgroundErrorColor
     * @since 1.0.0
     */
    private static final ErrorColor BACKGROUND_ERROR_COLOR = new ErrorColor(Color.PINK);

    /**
     * The component that edits the property managed by this instance.
     *
     * @see #getComponent()
     * @serial
     * @since 1.0.0
     */
    private final U component;

    /**
     * The translator between user input through the component and the value of
     * the property.
     *
     * @see #getTranslator()
     * @serial
     * @since 1.0.0
     */
    private final Translator<T, U> translator;

    /**
     * The name of the property. This value can be set explicitly or obtained
     * from the name of the component.
     *
     * @see #getName()
     * @serial
     * @since 1.0.0
     */
    private final String name;

    /**
     * A marker that indicates whether or not forms should read the property
     * value when the component editor is disabled. This flag does not prevent
     * the the component from being read or in any way disable the translator.
     * It is just a marker that can be honored or disregarded by choice.
     *
     * @see #setUseableWhenDisabled(boolean)
     * @see #isUseableWhenDisabled()
     * @serial
     * @since 1.0.0
     */
    private boolean useWhenDisabled;

    /**
     * Determines if this instance is in an error state. An error state occurrs
     * when an unparseable object is read from the editor component. Reading an
     * invalid value always sets the error state, but reading a valid one does
     * not clear it. The state must be cleared manually, preferably before
     * another read is attempted. This flag is synonymous with error markup
     * being turned on.
     *
     * @see #setError()
     * @see #clearError()
     * @see #isError()
     * @serial
     * @since 1.0.0
     */
    private boolean error;

    /**
     * The color to apply to components that have foreground error markup. This
     * color is of the internal type {@code ErrorColor} so that it can be
     * distinguished from user-defined colors when reverting the markup. If the
     * foreground color of a marked up component is changed outside of this
     * class, it will be left untouched when removing the markup.
     *
     * @see #setErrorForeground(Color)
     * @see #getErrorForeground()
     * @see #setError()
     * @see #clearError()
     * @serial
     * @since 1.0.0
     */
    private ErrorColor foregroundErrorColor;

    /**
     * The color to apply to components that have background error markup. This
     * color is of the internal type {@code ErrorColor} so that it can be
     * distinguished from user-defined colors when reverting the markup. If the
     * background color of a marked up component is changed outside of this
     * class, it will be left untouched when removing the markup.
     *
     * @see #setErrorBackground(Color)
     * @see #getErrorBackground()
     * @see #setError()
     * @see #clearError()
     * @serial
     * @since 1.0.0
     */
    private ErrorColor backgroundErrorColor;

    /**
     * A map of all markup elements. The map contains all of the components that
     * will be marked up on error as keys and {@code ComponentNode} objects
     * with instructions on how to perform and revert the markup.
     *
     * @see #addForegroundMarkup(Component)
     * @see #addBackgroundMarkup(Component)
     * @see #removeForegroundMarkup(Component)
     * @see #removeBackgroundMarkup(Component)
     * @serial
     * @since 1.0.0
     */
    private Map<Component, MarkupNode> markup;

    /**
     * A set of listeners that will be notified when the value of the underlying
     * property changes. The value can only be changed by a read from the
     * editor, since writes can not necessarily be read back without further
     * editing. Checking for a new value can be forced with the {@code
     * checkValue()} method. Conversely, reading can be done without notifying
     * listeners using {@code readValue(false)}.
     *
     * @see #addListener(PropertyChangeListener)
     * @see #addListeners(Collection)
     * @see #removeListener(PropertyChangeListener)
     * @see #removeListeners(Collection)
     * @see #removeListeners()
     * @serial
     * @since 1.0.0
     */
    private Set<PropertyChangeListener> listeners;

    /**
     * The last valid value read from the editor. This reference is used to
     * determine when listeners should be notified. If a read is performed
     * without notifying listeners, this value is not updated, so that
     * subsequent checks will properly trigger property change notifications.
     * Only the protected method {@code changeValue()} should ever directly
     * modify this field.
     *
     * @see #changeValue(Object)
     * @serial
     * @since 1.0.0
     */
    private T previousValue;

    /**
     * Constructs a configuration from the specified component and translator.
     * The property name is set to the component's name. No error markup
     * setup is done. It is recommended that the {@code ConverterFactory}
     * class be used instead of calling this constructor directly.
     *
     * @param component the component to which this configuration applies. The
     * name of the underlying property is set to the name of the component. This
     * component is passed in to the read and write methods of the translator.
     * @param translator the translator to assign to the component.
     * @throws NullPointerException if the component, its name or the translator
     * are {@code null}.
     * @see Component#getName()
     * @see ConverterFactory
     * @since 1.0.0
     */
    public Converter(U component, Translator<T, U> translator)
    {
        this(component.getName(), component, translator);
    }

    /**
     * Constructs a configuration from the specified property name, component
     * and translator. No error markup setup is done. It is recommended that the
     * {@code ConverterFactory} class be used instead of calling this
     * constructor directly.
     *
     * @param name the name of the underlying property.
     * @param component the component to which this configuration applies. This
     * component is passed in to the read and write methods of the translator.
     * @param translator the translator to assign to the component.
     * @throws NullPointerException if the name, component, or the translator
     * are {@code null}.
     * @see ConverterFactory
     * @since 1.0.0
     */
    public Converter(String name, U component, Translator<T, U> translator)
    {
        if(name == null)
            throw new NullPointerException("name");
        if(component == null)
            throw new NullPointerException("component");
        if(translator == null)
            throw new NullPointerException("translator");

        this.component = component;
        this.translator = translator;
        this.name = name;
        this.useWhenDisabled = false;
        this.error = false;
        this.foregroundErrorColor = FOREGROUND_ERROR_COLOR;
        this.backgroundErrorColor = BACKGROUND_ERROR_COLOR;
        this.markup = new HashMap<>();
        this.listeners = new HashSet<>();
        this.previousValue = null;
    }

    /**
     * Returns the editor component. This component allows the user to edit the
     * property managed by this configuration.
     *
     * @return the editor component.
     * @since 1.0.0
     */
    public U getComponent()
    {
        return this.component;
    }

    /**
     * Returns the translator used to convert between the component and the
     * value of the property.
     *
     * @return the translator.
     * @since 1.0.0
     */
    public Translator<T, U> getTranslator()
    {
        return translator;
    }

    /**
     * Returns the name of the underlying property.
     *
     * @return the property name.
     * @since 1.0.0
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * A marker that indicates whether or not forms should read the property
     * value when the component editor is disabled. This flag does not prevent
     * the the component from being read or in any way disable the translator.
     * {@code JFormManager} honors this marker when exporting forms. However,
     * it disregards it on import. This property is {@code false} by default.
     *
     * @return a flag indicating whether or not the property should be read
     * when the editor component is disabled.
     * @see #setUseableWhenDisabled(boolean)
     * @see #isEnabled()
     * @since 1.0.0
     */
    public boolean isUseableWhenDisabled()
    {
        return useWhenDisabled;
    }

    /**
     * Determines whether the underlying property should be read when the
     * editor component is disabled. This flag is a marker that does not
     * actually prevent input or output to the editor. Whether or not the flag
     * is honored (and if so, how) is up to the classes that read and write to
     * this instance.
     *
     * @param b {@code false} if the property should be disregarded when the
     * editor component is disabled, {@code true} otherwise.
     * @see #isUseableWhenDisabled()
     * @see Component#setEnabled(boolean)
     * @since 1.0.0
     */
    public void setUseableWhenDisabled(boolean b)
    {
        this.useWhenDisabled = b;
    }

    /**
     * Determines if this instance is enabled. Being enabled means that either
     * the editor component is enabled or the configuration is useable when the
     * component is disabled. {@code JFormManager} uses this flag to determine
     * which configurations to export, but not which ones to import. Other
     * classes are free to honor or ignore this flag.
     *
     * @return {@code true} if the configuration is enabled, {@code false}
     * otherwise.
     * @see #isUseableWhenDisabled()
     * @see Component#isEnabled()
     * @since 1.0.0
     */
    public boolean isEnabled()
    {
        return useWhenDisabled || component.isEnabled();
    }

    public void clearError()
    {
        if(isError()) {
            for(MarkupNode node : markup.values())
                node.clearError();
            error = false;
        }
    }

    public void setError()
    {
        if(!isError()) {
            for(MarkupNode node : markup.values())
                node.setError();
            error = true;
        }
    }

    public boolean isError()
    {
        return error;
    }

    public Color getErrorForeground()
    {
        return foregroundErrorColor;
    }

    public Color getErrorBackground()
    {
        return backgroundErrorColor;
    }

    public void setErrorForeground(Color color)
    {
        this.foregroundErrorColor = new ErrorColor(color);
    }

    public void setErrorBackground(Color color)
    {
        this.backgroundErrorColor = new ErrorColor(color);
    }

    public void addForegroundMarkup(Component markupComponent)
    {
        MarkupNode node;
        if(markup.containsKey(markupComponent)) {
            node = markup.get(markupComponent);
        } else {
            node = new MarkupNode(markupComponent);
            markup.put(markupComponent, node);
        }
        node.setForeground(true);
    }

    public void removeForegroundMarkup(Component markupComponent)
    {
        if(markup.containsKey(markupComponent)) {
            MarkupNode node = markup.get(markupComponent);
            /*
             * If the node is going to be neither foreground nor background,
             * it should be removed entirely.
             */
            if(node.isBackground()) {
                node.setForeground(false);
            } else {
                markup.remove(markupComponent);
            }
        }
    }

    public void addBackgroundMarkup(Component markupComponent)
    {
        MarkupNode node;
        if(markup.containsKey(markupComponent)) {
            node = markup.get(markupComponent);
        } else {
            node = new MarkupNode(markupComponent);
            markup.put(markupComponent, node);
        }
        node.setBackground(true);
    }

    public void removeBackgroundMarkup(Component markupComponent)
    {
        if(markup.containsKey(markupComponent)) {
            MarkupNode node = markup.get(markupComponent);
            /*
             * If the node is going to be neither foreground nor background,
             * it should be removed entirely.
             */
            if(node.isForeground()) {
                node.setBackground(false);
            } else {
                markup.remove(markupComponent);
            }
        }
    }

    public void addListener(PropertyChangeListener listener)
    {
        this.listeners.add(listener);
    }

    public void addListeners(Collection<PropertyChangeListener> listeners)
    {
        this.listeners.addAll(listeners);
    }

    public void removeListener(PropertyChangeListener listener)
    {
        listeners.remove(listener);
    }

    public void removeListeners(Collection<PropertyChangeListener> listeners)
    {
        listeners.removeAll(listeners);
    }

    public void removeListeners()
    {
        listeners.clear();
    }

    public T readValue() throws TranslatorException
    {
        return readValue(true);
    }

    public T readValue(boolean notify) throws TranslatorException
    {
        try {
            T newValue = translator.readValue(component);
            if(notify)
                changeValue(newValue);
            return newValue;
        } catch(TranslatorException te) {
            setError();
            throw te;
        }
    }

    public String readString() throws TranslatorException
    {
        return readString(true);
    }

    public String readString(boolean notify) throws TranslatorException
    {
        try {
            String newString = translator.readString(component);
            if(notify)
                checkValue(false);
            return newString;
        } catch(TranslatorException te) {
            setError();
            throw te;
        }
    }

    public void writeValue(Object value) throws TranslatorException
    {
        translator.writeValue(value, component);
    }

    public boolean checkValue(boolean mark)
    {
        try {
            T newValue = translator.readValue(component);
            return changeValue(newValue);
        } catch(TranslatorException te) {
            if(mark)
                setError();
            return false;
        }
    }

    public void writeChecked(Object value, boolean mark) throws TranslatorException
    {
        writeValue(value);
        checkValue(mark);
    }

    protected Collection<PropertyChangeListener> getListeners()
    {
        return Collections.unmodifiableSet(listeners);
    }

    protected boolean changeValue(T newValue)
    {
        if((newValue == null && previousValue != null) || (newValue != null && !newValue.equals(previousValue))) {
            PropertyChangeEvent event = new PropertyChangeEvent(component, name, previousValue, newValue);
            for(PropertyChangeListener listener : listeners)
                listener.propertyChange(event);
            previousValue = newValue;
            return true;
        }
        return false;
    }

    /**
     * A tag class to indicate colors set by error markup rather than by the
     * user.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 25 May 2013
     * @since 1.0.0
     */
    private static class ErrorColor extends Color
    {
        /**
         * The version ID for serialization.
         *
         * @serial Increment the least significant three digits when
         * compatibility is not compromised by a structural change (e.g. adding
         * a new field with a sensible default value), and the upper digits when
         * the change makes serialized versions of of the class incompatible
         * with previous releases.
         * @since 1.0.0
         */
        private static final long serialVersionUID = 1000L;

        /**
         * Copy constructor for converting a regular color into an error color.
         * This constructor preserves the alpha value.
         *
         * @param color the color to copy.
         * @since 1.0.0
         */
        public ErrorColor(Color color)
        {
            super(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        }
    }

    /**
     * A set of instructions for applying and clearing markup on a component
     * when an error occurs in the editor.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 25 May 2013
     * @since 1.0.0
     */
    private class MarkupNode implements Serializable
    {
        /**
         * The version ID for serialization.
         *
         * @serial Increment the least significant three digits when
         * compatibility is not compromised by a structural change (e.g. adding
         * a new field with a sensible default value), and the upper digits when
         * the change makes serialized versions of of the class incompatible
         * with previous releases.
         * @since 1.0.0
         */
        private static final long serialVersionUID = 1000L;

        /**
         * The component that will be marked up when an error occurs in the
         * editor component.
         *
         * @since 1.0.0
         */
        public final Component component;

        /**
         * The previous foreground color of the component, before error markup
         * was applied. This color will be reinstated when the markup is
         * removed.
         *
         * @since 1.0.0
         */
        private Color foregroundColor;

        /**
         * The previous background color of the component, before error markup
         * was applied. This color will be reinstated when the markup is
         * removed.
         *
         * @since 1.0.0
         */
        private Color backgroundColor;

        /**
         * Determines if this node contains foreground markup instructions. If
         * not, {@link #foregroundColor} will be ignored.
         *
         * @since 1.0.0
         */
        private boolean foreground;

        /**
         * Determines if this node contains background markup instructions. If
         * not, {@link #backgroundColor} will be ignored.
         *
         * @since 1.0.0
         */
        private boolean background;

        /**
         * Constructs a new node for the specified markup component. The new
         * node will initially perform neither foreground nor background markup.
         *
         * @param component the component that will display the error markup
         * configured in this node.
         * @since 1.0.0
         */
        public MarkupNode(Component component)
        {
            this.component = component;
            this.background = false;
            this.foreground = false;
        }

        public void setError()
        {
            if(foreground) {
                foregroundColor = component.getForeground();
                component.setForeground(foregroundErrorColor);
            }
            if(background) {
                backgroundColor = component.getBackground();
                component.setBackground(backgroundErrorColor);
            }
        }

        public void clearError()
        {
            if(foreground && component.getForeground() instanceof ErrorColor)
                component.setForeground(foregroundColor);
            if(background && component.getBackground() instanceof ErrorColor)
                component.setBackground(backgroundColor);
        }

        public void setForeground(boolean foreground)
        {
            this.foreground = foreground;
        }

        public void setBackground(boolean background)
        {
            this.background = background;
        }

        public boolean isForeground()
        {
            return foreground;
        }

        public boolean isBackground()
        {
            return background;
        }

        @Override@SuppressWarnings("unchecked")
        public boolean equals(Object o)
        {
            return (o instanceof Converter.MarkupNode) && (((MarkupNode)o).component.equals(this.component));
        }

        @Override public int hashCode()
        {
            return component.hashCode();
        }
    }
}

