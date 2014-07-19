/*
 * ConverterFactory.java (Package: com.madphysicist.jform.ConverterFactory)
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

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Component;
import java.awt.Label;
import java.awt.TextComponent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.text.JTextComponent;
import com.madphysicist.jform.translate.Translator;
import com.madphysicist.tools.swing.JColorButton;
import com.madphysicist.tools.swing.JFileField;

/**
 * Creates {@code Converter}s suitable for specific types of {@code
 * Component}s. Configurations involve setting up a set of UI elements that will
 * have their background and foreground colors modified when errors occur. UI
 * elements are specified using the nested {@code MarkupConfiguration} interface. A
 * set of simple implementations of the interface are provided with this class.
 * The {@code getConverter()} methods allow a label to be included for the
 * main editor component. The label can be of any type (not just {@code Label}
 * or {@code JLabel}, and will be processed in the same way as the editor
 * component.
 * <p>
 * Search for mappings is done hierarchically. If the specific type of a
 * component has no mapping in either the current factory or in the default one,
 * the search continues with its superclass. The default factory is guaranteed
 * to contain a mapping for {@code java.awt.Component}, ensuring that the search
 * will end somewhere.
 * <p>
 * It is not necessary to create an instance of the factory if only the default
 * functionality is desired. The static {@code createConfig()} methods delegate
 * to a default instance which contains sensible mappings for the following
 * component types (both AWT and Swing):
 * <a name="supported" />
 * <ul>
 * <li>Component</li>
 * <li>Label/JLabel</li>
 * <li>Button/AbstractButton</li>
 * <li>JCheckbox</li>
 * <li>JSpinner</li>
 * <li>TextComponent</li>
 * <li>JComboBox</li>
 * <li>JColorButton</li>
 * <li>JFileField</li>
 * </ul>
 * The default instance can be retrieved and modified, but the lowest level set
 * of mappings can not.
 *
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 27 May 2013
 * @since 1.0.0
 */
public class ConverterFactory
{
    /**
     * This interface allows users to register their own set up for new
     * components or modify existing setups in a factory instance. Instances of
     * the interface may be registered with a factory using the {@code
     * registerConfiguration()} method.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 27 May 2013
     * @since 1.0.0
     */
    public static interface MarkupConfiguration
    {
        /**
         * Returns a list of components whose foregrounds will be set to the
         * error color in case of an error in the specified component. This list
         * is not required to contain the component for which it is generated.
         *
         * @param component the component on which error markup is being done.
         * @return a list of the components whose foreground color will be
         * modified in response to an error. May be {@code null} or empty.
         * @since 1.0.0
         */
        public List<Component> getForegrounds(Component component);

        /**
         * Returns a list of components whose backgrounds will be set to the
         * error color in case of an error in the specified component. This list
         * is not required to contain the component for which it is generated.
         *
         * @param component the component on which error markup is being done.
         * @return a list of the components whose background color will be
         * modified in response to an error. May be {@code null} or empty.
         * @since 1.0.0
         */
        public List<Component> getBackgrounds(Component component);
    }

    /**
     * A configuration that only changes the foreground of the input component.
     * This configuration is useful for components such as labels and buttons.
     *
     * @since 1.0.0
     */
    public static final MarkupConfiguration CONFIG_FOREGROUND = new MarkupConfiguration() {
        @Override public List<Component> getForegrounds(Component component) { return asList(component); }
        @Override public List<Component> getBackgrounds(Component component) { return null; }
    };

    /**
     * A configuration that only changes the background of the input component.
     * This configuration is useful for components such as text editors.
     *
     * @since 1.0.0
     */
    public static final MarkupConfiguration CONFIG_BACKGROUND = new MarkupConfiguration() {
        @Override public List<Component> getForegrounds(Component component) { return null; }
        @Override public List<Component> getBackgrounds(Component component) { return asList(component); }
    };

    /**
     * A configuration that changes both the foreground and background of the
     * input component. This configuration is used as a default.
     *
     * @since 1.0.0
     */
    public static final MarkupConfiguration CONFIG_BOTH = new MarkupConfiguration() {
        @Override public List<Component> getForegrounds(Component component) { return asList(component); }
        @Override public List<Component> getBackgrounds(Component component) { return asList(component); }
    };

    /**
     * The top-level map of the default values that come with this class. This
     * map is configured with the {@code populateDefaultConfigs()} method and
     * never altered after the class is loaded. Non-default instances will
     * search this map if their maps do not contain a valid mapping for a given
     * component type. The defaults should be adequate for most uses. This map
     * must contain a mapping for the raw type {@code Component.class}.
     *
     * @since 1.0.0
     */
    private static final HashMap<Class<? extends Component>, MarkupConfiguration> DEFAULT_CONFIGS = new HashMap<>();

    /**
     * The default factory instance. This instance is referred to by the static
     * {@code createConverter()} methods. The default instance is
     * publically accessible and mutable.
     *
     * @see #getDefaultInstance()
     * @since 1.0.0
     */
    private static final ConverterFactory DEFAULT_INSTANCE = new ConverterFactory();

    /**
     * The configuration map of the current instance. If a requested mapping is
     * not found in this map, it will be delegated to {@link #DEFAULT_CONFIGS}.
     *
     * @since 1.0.0
     */
    private final HashMap<Class<? extends Component>, MarkupConfiguration> configs;

    static {
        populateDefaultConfigs();
    }

    /**
     * Creates a new factory with no mappings.
     *
     * @since 1.0.0
     */
    public ConverterFactory()
    {
        configs = new HashMap<>();
    }

    /**
     * Constructs a {@code Converter} object with the specified name, and
     * translator, for the specified component with the specified label. If a
     * description of the error markup for the exact type of the component is
     * not found in this factory or in the default mappings, an attempt will be
     * made to retrieve a configuration for the component's supertype. A label
     * is processed in the same way as the component itself, if it is present.
     *
     * @param <T> The property type of the component config.
     * @param <U> The type of the editor component.
     * @param name the property name of the configuration.
     * @param component the component editor of the property.
     * @param translator a translator of appropriate type.
     * @param label a label associated with the component. The label may be
     * any type of {@code Component}, or {@code null}.
     * @return a configuration for the specified component and label that has
     * the error markup properly preconfigured.
     * @since 1.0.0
     */
    public <T, U extends Component> Converter<T, U> getConverter(String name, U component, Translator<T, U> translator, Component label)
    {
        Converter<T, U> componentConfig = new Converter<>(name, component, translator);
        return configureComponent(componentConfig, component, label);
    }

    /**
     * Constructs a {@code Converter} object with the specified name, and
     * translator, for the specified component with the specified label. The
     * property name for the configuration is taken from the name of the
     * component. If a description of the error markup for the exact type of the
     * component is not found in this factory or in the default mappings, an
     * attempt will be made to retrieve a configuration for the component's
     * supertype. A label is processed in the same way as the component itself,
     * if it is present.
     *
     * @param <T> The property type of the component config.
     * @param <U> The type of the editor component.
     * @param component the component editor of the property.
     * @param translator a translator of appropriate type.
     * @param label a label associated with the component. The label may be
     * any type of {@code Component}, or {@code null}.
     * @return a configuration for the specified component and label that has
     * the error markup properly preconfigured.
     * @see Component#getName()
     * @see Component#setName(String)
     * @since 1.0.0
     */
    public <T, U extends Component> Converter<T, U> getConverter(U component, Translator<T, U> translator, Component label)
    {
        Converter<T, U> componentConfig = new Converter<>(component, translator);
        return configureComponent(componentConfig, component, label);
    }

    /**
     * Constructs a {@code Converter} object with the specified name, and
     * translator, for the specified component with the specified label using
     * the default factory instance. If a description of the error markup for
     * the exact type of the component is not found in the default factory or in
     * the underlying default mappings, an attempt will be made to retrieve a
     * configuration for the component's supertype. A label is processed in the
     * same way as the component itself, if it is present.
     *
     * @param <T> The property type of the component config.
     * @param <U> The type of the editor component.
     * @param name the property name of the configuration.
     * @param component the component editor of the property.
     * @param translator a translator of appropriate type.
     * @param label a label associated with the component. The label may be
     * any type of {@code Component}, or {@code null}.
     * @return a configuration for the specified component and label that has
     * the error markup properly preconfigured.
     * @since 1.0.0
     */
    public static <T, U extends Component> Converter<T, U> createConverter(String name, U component, Translator<T, U> translator, Component label)
    {
        return DEFAULT_INSTANCE.getConverter(name, component, translator, label);
    }

    /**
     * Constructs a {@code Converter} object with the specified name, and
     * translator, for the specified component with the specified label using
     * the default factory instance. The property name for the configuration is
     * taken from the name of the component. If a description of the error
     * markup for the exact type of the component is not found in this factory
     * or in the underlying default mappings, an attempt will be made to
     * retrieve a configuration for the component's supertype. A label is
     * processed in the same way as the component itself, if it is present.
     *
     * @param <T> The property type of the component config.
     * @param <U> The type of the editor component.
     * @param component the component editor of the property.
     * @param translator a translator of appropriate type.
     * @param label a label associated with the component. The label may be
     * any type of {@code Component}, or {@code null}.
     * @return a configuration for the specified component and label that has
     * the error markup properly preconfigured.
     * @see Component#getName()
     * @see Component#setName(String)
     * @since 1.0.0
     */
    public static <T, U extends Component> Converter<T, U> createConverter(U component, Translator<T, U> translator, Component label)
    {
        return DEFAULT_INSTANCE.getConverter(component, translator, label);
    }

    /**
     * Adds an error markup configuration for components of the specified type.
     *
     * @param type the type of component to which the configuration applies. The
     * configuration will be applied to all of its subclasses as well unless a
     * more specific type is registered.
     * @param config the error markup configuration to register.
     * @since 1.0.0
     */
    public void registerConfiguration(Class<? extends Component> type, MarkupConfiguration config)
    {
        configs.put(type, config);
    }

    /**
     * Returns the default factory instance. This method allows the factory to
     * be modified with user-specific configurations on an application-wide
     * scale.
     *
     * @return the default factory.
     * @since 1.0.0
     */
    public static ConverterFactory getDefaultInstance()
    {
        return DEFAULT_INSTANCE;
    }

    /**
     * Adds error markup for a component and label to the specified {@code
     * Converter}. The mappings in this factory and in the defaults
     * dictate which components will be registered with foreground markup and
     * which with background markup.
     *
     * @param componentConfig the object to configure.
     * @param component the component that error markup is being applied to.
     * This parameter is passed in rather than using the underlying {@code
     * Component} of {@code componentConfig} so that the label can be processed
     * recursively.
     * @param label the label for the component. May be {@code null} if there is
     * no label.
     * @return the input component, with error markup set up.
     * @see Converter#addBackgroundMarkup(Component)
     * @see Converter#addForegroundMarkup(Component)
     * @since 1.0.0
     */
    private <T, U extends Component> Converter<T, U> configureComponent(Converter<T, U> componentConfig, Component component, Component label)
    {
        MarkupConfiguration config = findConfiguration(component.getClass());

        List<Component> componentArray;

        componentArray = config.getForegrounds(component);
        if(componentArray != null && !componentArray.isEmpty()) {
            for(Component markupComponent : componentArray)
                componentConfig.addForegroundMarkup(markupComponent);
        }

        componentArray = config.getBackgrounds(component);
        if(componentArray != null && !componentArray.isEmpty()) {
            for(Component markupComponent : componentArray)
                componentConfig.addBackgroundMarkup(markupComponent);
        }

        if(label != null)
            configureComponent(componentConfig, label, null);

        return componentConfig;
    }

    /**
     * Finds the error markup configuration for a given component type. If the
     * current factory does not have a mapping for the type, an attempt is made
     * to find it in {@link #DEFAULT_CONFIGS}. If neither if found, the same
     * procedure is applied recursively to the supertype of the class until a
     * match is found. {@code DEFAULT_CONFIGS} is guaranteed to contain a
     * mapping for the type {@code Component}.
     *
     * @param componentClass the class of the component to search for.
     * @return a configuration mapping to the specified component type.
     * @see #getValue(Class)
     * @since 1.0.0
     */
    @SuppressWarnings("unchecked")
    private MarkupConfiguration findConfiguration(Class<? extends Component> componentClass)
    {
        while(componentClass != Component.class) {
            MarkupConfiguration value = getValue(componentClass);
            if(value != null) {
                return value;
            }
            // This cast is pointless and generates an unchecked warning.
            // It appears here only for the purpose of eliminating compiler warnings.
            componentClass = (Class<? extends Component>)componentClass.getSuperclass();
        }
        return DEFAULT_CONFIGS.get(componentClass);
    }

    /**
     * Returns the configuration mapping for the specified class. If the current
     * instance has a mapping, it is returned. Otherwise, if the default map has
     * a mapping, it is returned. If neither mapping is found, the return value
     * is {@code null}.
     *
     * @param componentClass the exact component type to check for.
     * @return a mapping from the current factory instance, the defaults, or
     * {@code null}, in that order of preference.
     * @since 1.0.0
     */
    private MarkupConfiguration getValue(Class<? extends Component> componentClass)
    {
        if(configs.containsKey(componentClass))
            return configs.get(componentClass);
        return DEFAULT_CONFIGS.get(componentClass);
    }

    /**
     * Returns a single component as a list. This method is used in the three
     * basic implementations of {@code MarkupConfiguration} provided with this class.
     *
     * @param component the component to create a list view of.
     * @return a {@code List} containing the specified component and no other
     * elements.
     * @see #CONFIG_BACKGROUND
     * @see #CONFIG_FOREGROUND
     * @see #CONFIG_BOTH
     * @since 1.0.0
     */
    private static List<Component> asList(Component component)
    {
        return Arrays.asList(new Component[] {component});
    }

    /**
     * Populates the default configuration map. See the <a href="#supported">
     * class description</a> for a list of types supported by default. Most of
     * the mappings use the three default implementations of {@code
     * MarkupConfiguration} directly. Some, like the one for {@code JComboBox},
     * specify markup to sub-components of the target.
     *
     * @since 1.0.0
     */
    private static void populateDefaultConfigs()
    {
        DEFAULT_CONFIGS.put(Component.class, CONFIG_BOTH);
        DEFAULT_CONFIGS.put(Label.class, CONFIG_FOREGROUND);
        DEFAULT_CONFIGS.put(JLabel.class, CONFIG_FOREGROUND);
        DEFAULT_CONFIGS.put(Button.class, CONFIG_FOREGROUND);
        DEFAULT_CONFIGS.put(Checkbox.class, CONFIG_FOREGROUND);
        DEFAULT_CONFIGS.put(AbstractButton.class, CONFIG_FOREGROUND);
        DEFAULT_CONFIGS.put(TextComponent.class, CONFIG_BACKGROUND);
        DEFAULT_CONFIGS.put(JTextComponent.class, CONFIG_BACKGROUND);
        DEFAULT_CONFIGS.put(JColorButton.class, CONFIG_BACKGROUND);
        DEFAULT_CONFIGS.put(JComboBox.class, new MarkupConfiguration() {
            @Override public List<Component> getForegrounds(Component component) { return null; }
            @Override public List<Component> getBackgrounds(Component component) {
                return asList(((JComboBox<?>)component).getEditor().getEditorComponent()); }
        });
        DEFAULT_CONFIGS.put(JSpinner.class, new MarkupConfiguration() {
            @Override public List<Component> getForegrounds(Component component) { return null; }
            @Override public List<Component> getBackgrounds(Component component) {
                return asList(((JSpinner)component).getEditor()); }
        });
        DEFAULT_CONFIGS.put(JFileField.class, new MarkupConfiguration() {
            @Override public List<Component> getForegrounds(Component component) {
                return asList(((JFileField)component).getButton()); }
            @Override public List<Component> getBackgrounds(Component component) {
                return asList(((JFileField)component).getTextField()); }
        });        
    }
}
