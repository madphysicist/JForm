/*
 * JFormManager.java (Class: com.madphysicist.jform.JFormManager)
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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import com.madphysicist.jform.translate.TranslatorComponentException;
import com.madphysicist.jform.translate.TranslatorException;
import com.madphysicist.jform.translate.Translator;

/**
 * A manager class that allows users to register properties and components that
 * edit them.
 * <p>
 * Communication between components and the properties they edit is enabled
 * by registering both with the manager using one of the {@code register()}
 * methods. Note that any component can be registered, regardless of whether it
 * is active, visible, enabled, etc. Components can only be registered once per
 * manager instance. Further registrations will overwrite previous ones.
 * Components can be deregistered using the {@code deregister()} methods.
 * <p>
 * Registration consists of providing the manager with a {@code Converter}
 * that lets it know how to manipulate the graphics and data in response to
 * user input. The {@code Converter} object contains information that
 * allows the UI component's data to be translated into a property value. It
 * also has information about how to graphically mark up errors in the user
 * input. {@code Converter} objects can be created manually or through a
 * {@code ConverterFactory}. Factories are flexible and can be configured
 * to create different setups for different types of components.
 * <p>
 * This class allows listeners to be registered to respond to successful changes
 * to a property. Changes are monitored by the underlying {@code Converter}.
 * Methods that read the form fields such as {@code get()} and {@code
 * exportConfig()} always trigger a property change on each field that can be
 * read and has changed if requested. To trigger a non-invasive check of all
 * components that will not throw exceptions or apply markup to any fields
 * unless requested, use the {@code check} method. {@code check} may be invoked
 * in response to any user defined condition, anywhere from a click on an "OK"
 * button to a successfully committed edit in a text field.
 * <p>
 * Configuration properties can be set and retrieved individually using the
 * {@code get()} and {@code set()} methods. Both types of method throw {@code
 * TranslatorException}s. The {@code get()} methods also perform error markup if
 * they fail, as do the {@code exportConfig()} methods. Note that while
 * programatically retrieving bound properties always triggers a {@code
 * PropertyChangeEvent} to the appropriate listeners, the user selects whether
 * the export operations trigger an event. Events are triggered in the export
 * only after the entire form has been successfully processed.
 * <p>
 * Properties not bound to an editor component may be added to the manager. Such
 * "unbound" properties can be created by calling {@code set(String, Object)} or
 * {@code addListener(String, PropertyChangeListener)}. The latter case will
 * associate a {@code null} value with the property. Unbound properties can have
 * a value and attached listeners. They should also be provided with a string
 * value if it is not the same as {@code value.toString()}. When a component is
 * registered for an unbound property, it receives all the listeners and an
 * attempt is made to write the current value to the component. When components
 * are deregistered, the underlying property is removed entirely. To delete a
 * component without deleting the property value and its listeners, use the
 * {@code unbind()} methods.
 * <p>
 * This class provides a number of simple built in input and output formats for
 * the data. The most basic format is an in-memory {@code Map} of property names
 * to objects. There are also convenience methods for importing and exporting
 * {@code ResourceBundle}s, properties files in plain text and and XML formats.
 *
 * @see Converter
 * @see ConverterFactory
 * @author Joseph Fox-Rabinovitz
 * @version 1.0.0, 25 May 2013: Joseph Fox-Rabinovitz: Created.
 * @version 1.0.0.1, 25 June 2013: Joseph Fox-Rabinovitz: Updated with changes
 * to the {@code Converter} API.
 */
public class JFormManager implements Serializable
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
     * A map of registered {@code Component}s to their configurations. This map
     * represents the user interface side of the interaction. It contains the
     * same configuration objects as {@link #namedConfigs}. It references none
     * of the properties in {@link #unboundProperties}.
     *
     * @see #register(Converter)
     * @see #register(Component, Translator)
     * @see #deregister(Component)
     * @see #deregister(Converter)
     * @serial
     * @since 1.0.0
     */
    private HashMap<Component, Converter<?, ? extends Component>> componentConfigs;

    /**
     * A map of property names to their configurations. This map represents the
     * model side of the interaction it contains the same configuration objects
     * as {@link #componentConfigs}. It contains none of the properties in
     * {@link #unboundProperties}.
     *
     * @see #register(Converter)
     * @see #register(Component, Translator)
     * @see #deregister(String)
     * @see #deregister(Converter)
     * @serial
     * @since 1.0.0
     */
    private HashMap<String, Converter<?, ? extends Component>> namedConfigs;

    /**
     * A map of the properties that are not bound to any component. These
     * properties have no restrictions in terms of the values that they can
     * hold. Unbound properties are created by setting a property that is not
     * registered by name, adding a listener to a property that is not
     * registered, or explicitly unbinding a bound property. This map contains
     * none of the properties registered in {@link #namedConfigs} and {@link
     * #componentConfigs}.
     *
     * @see #set(String, Object, String)
     * @see #addListener(String, PropertyChangeListener)
     * @see #unbind(String)
     * @see #unbind(Component)
     * @see #unbind(Converter)
     * @serial
     * @since 1.0.0
     */
    private HashMap<String, UnboundNode> unboundProperties;

    /**
     * Constructs a new manager with no properties or components.
     *
     * @since 1.0.0
     */
    public JFormManager()
    {
        super();
        componentConfigs = new HashMap<>();
        namedConfigs = new HashMap<>();
        unboundProperties = new HashMap<>();
    }

    /**
     * Retrieves the configuration for the specified {@code Component}.
     *
     * @param component the component to search for.
     * @return the configuration for that component, or {@code null} if it is
     * not registered.
     * @since 1.0.0
     */
    public Converter<?, ? extends Component> getRegistration(Component component)
    {
        return componentConfigs.get(component);
    }

    /**
     * Retrieves the configuration for the specified configuration property. A
     * property may be registered but not bound to a component. In this case,
     * the return value will be {@code null}. Use {@link #isProperty(String)}
     * and {@link #isBound(String)} to determine if the property exists and is
     * bound to an editor component.
     *
     * @param propertyName the name to search for.
     * @return the configuration for that name, or {@code null} if it is not
     * bound to a component.
     * @since 1.0.0
     */
    public Converter<?, ? extends Component> getRegistration(String propertyName)
    {
        return namedConfigs.get(propertyName);
    }

    /**
     * Returns the component that is responsible for user interactions for the
     * specified configuration property. If the specified property is not bound
     * to a component, the return value will be {@code null}, even if it is
     * managed by this instance. To determine if a property is managed, use
     * {@link #isProperty(String) isProperty()}. To determine if it is bound to
     * a component, use {@link #isBound(String) isBound()}.
     *
     * @param propertyName the property name to search for.
     * @return the component that edits the specified property, or {@code null}
     * if the specified property is not bound to a component.
     * @since 1.0.0
     */
    public Component getComponent(String propertyName)
    {
        if(namedConfigs.containsKey(propertyName))
            return namedConfigs.get(propertyName).getComponent();
        return null;
    }

    /**
     * Returns the name of the property edited by the specified component.
     *
     * @param component the component to search for.
     * @return the name of the property edited by the component, or {@code null}
     * if the component is not registered.
     * @since 1.0.0
     */
    public String getName(Component component)
    {
        if(componentConfigs.containsKey(component))
            return componentConfigs.get(component).getName();
        return null;
    }

    /**
     * Determines if a property with the specified name is managed by this
     * class. The property may be bound to an editor component or not. To
     * determine if it is, use {@link #isBound(String)}.
     *
     * @param propertyName the name of the property to check.
     * @return {@code true} if a property with the specified name is managed
     * by this class, regardless of whether it is bound to a component, {@code
     * false} otherwise.
     * @since 1.0.0
     */
    public boolean isProperty(String propertyName)
    {
        return namedConfigs.containsKey(propertyName) || unboundProperties.containsKey(propertyName);
    }

    /**
     * Determines if the specified property is bound to an editor component.
     * This method returns {@code false} for both unbound and unregistered
     * properties. Use {@link #isProperty(String)} to determine if the property
     * is registered at all.
     *
     * @param propertyName the name of the property to check.
     * @return {@code true} if the property is bound to a component editor,
     * {@code false} if it is not.
     * @since 1.0.0
     */
    public boolean isBound(String propertyName)
    {
        return namedConfigs.containsKey(propertyName);
    }

    /**
     * Determines if the specified component is registered as a property editor.
     *
     * @param component the component to check.
     * @return {@code true} if the component is registered, {@code false}
     * otherwise.
     * @since 1.0.0
     */
    public boolean isBound(Component component)
    {
        return componentConfigs.containsKey(component);
    }

    /**
     * Registers the specified component with this manager. This method will
     * register a plain {@code Converter}, without markup and all values
     * besides the translator set to defaults. The configuration can be manually
     * modified by retrieving it with the {@link #getRegistration(Component)
     * getRegistration()} method, or by using the {@link
     * #register(Converter)} version of this method in conjunction with a
     * {@link Converter}. The component's name will be the name of the property
     * edited by the component.
     * <p>
     * If an unbound property with the specified name is already managed by this
     * manager, it will be bound to the component with all its listeners. An
     * attempt will be made to set the component's value to the current value of
     * the property. This attempt will ignore any exceptions and invalid values
     * will be silently skipped.
     *
     * @param <U> The type of the component.
     * @param component the component to register. Any previous registrations
     * will be completely removed.
     * @param translator the translator that will convert between the
     * component's user-edited state and the configuration properties managed
     * by this class.
     * @see Converter#Converter(Component, Translator)
     * @see Component#getName()
     * @since 1.0.0
     */
    public <U extends Component> void register(U component, Translator<?, U> translator)
    {
        register(new Converter<>(component, translator));
    }

    /**
     * Registers the specified component with this manager. This method will
     * register a plain {@code Converter}, without markup and all values
     * besides the property name and translator set to defaults. The
     * configuration can be manually modified by retrieving it with the {@link
     * #getRegistration(String) getRegistration()} method, or by using the
     * {@link #register(Converter)} version of this method in conjunction
     * with a {@link ConverterFactory}.
     * <p>
     * If an unbound property with the specified name is already managed by this
     * manager, it will be bound to the component with all its listeners. An
     * attempt will be made to set the component's value to the current value of
     * the property. This attempt will ignore any exceptions and invalid values
     * will be skipped.
     *
     * @param <U> The type of the component.
     * @param propertyName the name to assign to the new property.
     * @param component the component to register. Any previous registrations
     * will be completely removed.
     * @param translator the translator that will convert between the
     * component's user-edited state and the configuration properties managed
     * by this class.
     * @see Converter#Converter(String, Component, Translator)
     * @since 1.0.0
     */
    public <U extends Component> void register(String propertyName, U component, Translator<?, U> translator)
    {
        register(new Converter<>(propertyName, component, translator));
    }

    /**
     * Registers the specified configuration with this manager. Configurations
     * can be created by manually instantiating and setting up a {@code
     * Converter} or by acquiring a standard one from a {@link
     * ConverterFactory}.
     * <p>
     * If an unbound property with the same name as the specified configuration
     * is already managed by this manager, it will be bound to the configuration
     * with all its listeners. An attempt will be made to write the current
     * value of the property to the unerlying component. This attempt will
     * ignore any exceptions and invalid values will be skipped.
     *
     * @param componentConfig the configuration to register. Any previous
     * registrations of this configuration will be completely removed.
     * @since 1.0.0
     */
    public void register(Converter<?, ?> componentConfig)
    {
        // Bind any existing unbound properties
        UnboundNode previous = unboundProperties.remove(componentConfig.getName());
        if(previous != null) previous.bind(componentConfig);

        // Insert the new configuration
        Converter<?, ?> previous1 = componentConfigs.put(componentConfig.getComponent(), componentConfig);
        Converter<?, ?> previous2 = namedConfigs.put(componentConfig.getName(), componentConfig);

        // do a (possibly superfluous) reference sanity check
        if(previous1 != previous2)
            throw new InternalError("Configuration retrieved by name and configuration retrieved by component are diffrent references.");
    }

    /**
     * Removes the registration for the specified component from the manager.
     * Since a component can only be registered once to a manager, this method
     * removes all registrations for the component. User interaction with the
     * component will no longer affect the underlying property. The property
     * itself will be deleted. To preserve the property while removing its
     * editor component, use {@link #unbind(Component)}.
     *
     * @param component the component to deregister.
     * @since 1.0.0
     */
    public void deregister(Component component)
    {
        deregister(componentConfigs.get(component));
    }

    /**
     * Removes the registration for the specified property from the manager.
     * Since a property can only be registered once to a manager, this method
     * removes all registrations for the property, whether or not they are bound
     * to an editor component. The property itself will be deleted. To preserve
     * the property while removing its associated editor component, use {@link
     * #unbind(String)}.
     *
     * @param propertyName the property to deregister.
     * @since 1.0.0
     */
    public void deregister(String propertyName)
    {
        if(namedConfigs.containsKey(propertyName))
            deregister(namedConfigs.get(propertyName));
        else
            unboundProperties.remove(propertyName);
    }

    /**
     * Removes the registration with the specified configuration. User
     * interaction with the component will no longer affect the underlying
     * property.  The property itself will be deleted, along with the editor
     * component. To preserve the property while removing the editor component,
     * use {@link #unbind(Converter)}.
     *
     * @param componentConfig the configuration to deregister.
     * @since 1.0.0
     */
    public void deregister(Converter<?, ? extends Component> componentConfig)
    {
        if(componentConfig != null) {
            namedConfigs.remove(componentConfig.getName());
            componentConfigs.remove(componentConfig.getComponent());
        }
    }

    /**
     * Removes a property's binding to its editor component. The component and
     * all of its configuration are deleted. Hovewer, the property and its
     * listeners persist in an unbound state. An attempt is made to read the
     * current value from the component. If the value can not be read, the
     * property will start out uninitialized. Unbinding properties that are not
     * registered with the manager or not bound to a component is a no-op. Use
     * {@link #isBound(String)} to check if a property is bound to a component.
     *
     * @param propertyName the name of the property to unbind.
     * @since 1.0.0
     */
    public void unbind(String propertyName)
    {
        unbind(namedConfigs.get(propertyName));
    }

    /**
     * Removes a property's binding to its editor component. The component and
     * all of its configuration are deleted. Hovewer, the property it edits and
     * its listeners persist in an unbound state. An attempt is made to read the
     * current value from the component. If the value can not be read, the
     * property will start out uninitialized. Unbinding components that are not
     * registered with the manager is a no-op. Use {@link #isBound(Component)}
     * to check if a component is registered as a property editor.
     *
     * @param component component to unbind. This component will be deleted but
     * the property which it edits will persist.
     * @since 1.0.0
     */
    public void unbind(Component component)
    {
        unbind(componentConfigs.get(component));
    }

    /**
     * Removes a property's binding to its editor component. The component and
     * all of its configuration are deleted. Hovewer, the property and its
     * listeners persist in an unbound state. An attempt is made to read the
     * current value from the component. If the value can not be read, the
     * property will start out uninitialized. Unbinding configurations that do
     * not represent registered property-editor pairs is a no-op.
     *
     * @param componentConfig the component configuration to unbind. The
     * underlying component will be deleted while the property it edits will
     * persist.
     * @since 1.0.0
     */
    public void unbind(Converter<?, ? extends Component> componentConfig)
    {
        if(componentConfig != null) {
            deregister(componentConfig);
            unboundProperties.put(componentConfig.getName(), new UnboundNode(componentConfig));
        }
    }

    /**
     * Adds a listener for changes to the specified property. Bound and unbound
     * properties notify their listeners differently:
     * <dl>
     * <dt>Bound Properties:</dt>
     * <dd>Listeners are notified when the property is read successfully, and
     * the new value is different from the last recorded value. Listeners are
     * not notified when a property is set because the {@link Translator}
     * interface does not guarantee that a value written to a component editor
     * can be read back. A check of all or some of the bound properties (with or
     * without error markup) can be induced with any of the {@code check()}
     * methods.</dd>
     * <dt>Unbound Properties:</dt>
     * <dd>Listeners are notified when a property is set to a new value.
     * Listeners are not notified when the property is read. Calling {@code
     * check()} on unbound properties is a no-op.</dd>
     * </dl>
     * <p>
     * Adding a listener to a property that is not yet registered creates an
     * unbound property. The property will have a default value of {@code null}
     * until it is set explicitly. The property and its listeners will be bound
     * to an editor component at a later time if one is added to the specified
     * property.
     *
     * @param propertyName the name of the property to listen to.
     * @param listener the listener to add to the property.
     * @see #check(String, boolean)
     * @see #check(Component, boolean)
     * @see #check(boolean)
     * @since 1.0.0
     */
    public void addListener(String propertyName, PropertyChangeListener listener)
    {
        if(namedConfigs.containsKey(propertyName))
            namedConfigs.get(propertyName).addListener(listener);
        else if(unboundProperties.containsKey(propertyName))
            unboundProperties.get(propertyName).addListener(listener);
        else
            unboundProperties.put(propertyName, new UnboundNode(propertyName, listener));
    }

    /**
     * Adds a listener for changes to properties edited by the specified
     * component. The listener will be notified whenever a new value is read
     * from the component. Listeners can also be triggered through the {@code
     * check()} methods.
     * <p>
     * Note that listeners can only be added once and only for existing
     * components. Requesting to add a listener to a component that is not
     * registered is a no-op, even if the component is to be registered later.
     *
     * @param component the editor component of the property to listen to.
     * @param listener the listener to add to the property.
     * @since 1.0.0
     */
    public void addListener(Component component, PropertyChangeListener listener)
    {
        if(componentConfigs.containsKey(component))
            componentConfigs.get(component).addListener(listener);
    }

    /**
     * Removes a listener for changes to the specified property. The listener
     * will no longer be notified when a new value is read from the editor
     * component for the property. Removing a non-existent listener or a
     * listener from a non-existent property is a no-op. This method removes
     * listeners from both bound and unbound properties.
     *
     * @param propertyName the name of the property to remove the listener from.
     * @param listener the listener to remove from the property. The listener
     * may still be active for other properties.
     * @since 1.0.0
     */
    public void removeListener(String propertyName, PropertyChangeListener listener)
    {
        if(namedConfigs.containsKey(propertyName))
            namedConfigs.get(propertyName).removeListener(listener);
        else if(unboundProperties.containsKey(propertyName))
            unboundProperties.get(propertyName).removeListener(listener);
    }

    /**
     * Removes a listener for changes to the specified property editor
     * component. The listener will no longer be notified when a new value is
     * read from the component. Removing a non-existent listener or a listener
     * from a non-existent component is a no-op.
     *
     * @param component the editor component of the property to remove the
     * listener from.
     * @param listener the listener to remove from the property. The listener
     * may still be active for other properties and components.
     * @since 1.0.0
     */
    public void removeListener(Component component, PropertyChangeListener listener)
    {
        if(componentConfigs.containsKey(component))
            componentConfigs.get(component).removeListener(listener);
    }

    /**
     * Retrieves the value of the specified property. Behavior is different
     * depending on whether the property is bound to an editor component or not:
     * <dl>
     * <dt>Bound Properties:</dt>
     * <dd>If the user input to the requested field is invalid, the error markup
     * is activated and an exception is thrown. The markup can be cleared with
     * {@link #clearErrors()}. An event will be sent to all listeners for the
     * specified property if a new value is read from it successfully.</dd>
     * <dt>Unbound Properties:</dt>
     * <dd>The value of the property will always be returned, although it may be
     * {@code null} if it has not been initialized. No listeners will be
     * notified.</dd>
     * </dl>
     *
     * @param propertyName the name of the property to retrieve.
     * @return the current value of the property, or {@code null} if there is no
     * such property or if the property is disabled.
     * @throws TranslatorException if the value could not be read from the
     * editor component.
     * @see #isBound(String)
     * @since 1.0.0
     */
    public Object get(String propertyName) throws TranslatorException
    {
        if(namedConfigs.containsKey(propertyName)) {
            Converter<?, ? extends Component> componentConfig = namedConfigs.get(propertyName);
            if(componentConfig.isEnabled())
                return componentConfig.readValue();
        } else if(unboundProperties.containsKey(propertyName)) {
            return unboundProperties.get(propertyName).getValue();
        }
        return null;
    }

    /**
     * Retrieves the property value from the specified component. If the user
     * input to the requested field is invalid, the error markup is activated
     * and an exception is thrown. The markup can be cleared with {@link
     * #clearErrors()}. An event will be sent to all listeners for the specified
     * property if a new value is read from it successfully.
     *
     * @param component the component to retrieve the value from.
     * @return the current value of the property, or {@code null} if there is no
     * such property or if the property is disabled.
     * @throws TranslatorException if the value could not be read from the
     * editor component.
     * @since 1.0.0
     */
    public Object get(Component component) throws TranslatorException
    {
        if(componentConfigs.containsKey(component)) {
            Converter<?, ? extends Component> componentConfig = componentConfigs.get(component);
            if(componentConfig.isEnabled())
                return componentConfig.readValue();
        }
        return null;
    }

    /**
     * Retrieves the string value of the specified property. This method is
     * provided because some export formats can not handle objects of arbitrary
     * type. Behavior is different depending on whether the property is bound to
     * an editor component or not:
     * <dl>
     * <dt>Bound Properties:</dt>
     * <dd>If the user input to the requested field is invalid, the error markup
     * is activated and an exception is thrown. The markup can be cleared with
     * {@link #clearErrors()}. An event will be sent to all listeners for the
     * specified property if a new string value is read from it
     * successfully.</dd>
     * <dt>Unbound Properties:</dt>
     * <dd>The string value of the property will always be returned, although it
     * may be {@code null} if it has not been initialized. No listeners will be
     * notified.</dd>
     * </dl>
     *
     * @param propertyName the name of the property to retrieve.
     * @return the current string value of the property, or {@code null} if
     * there is no such property or if the property is disabled.
     * @throws TranslatorException if the string value could not be read from
     * the editor component.
     * @see #isBound(String)
     * @since 1.0.0
     */
    public String getString(String propertyName) throws TranslatorException
    {
        if(namedConfigs.containsKey(propertyName)) {
            Converter<?, ? extends Component> componentConfig = namedConfigs.get(propertyName);
            if(componentConfig.isEnabled())
                return componentConfig.readString();
        } else if(unboundProperties.containsKey(propertyName)) {
            return unboundProperties.get(propertyName).getString();
        }
        return null;
    }

    /**
     * Retrieves the string form of the property value from the specified
     * component. This method is provided because some export formats can not
     * handle objects of arbitrary type. If the user input to the requested
     * field is invalid, the error markup is activated and an exception is
     * thrown. The markup can be cleared with {@link #clearErrors()}. An event
     * will be sent to all listeners for the specified property if a new value
     * is read from it successfully.
     *
     * @param component the component to retrieve the string value from.
     * @return the current string value of the property, or {@code null} if
     * there is no such property or if the property is disabled.
     * @throws TranslatorException if the string value could not be read from
     * the editor component.
     * @since 1.0.0
     */
    public String getString(Component component) throws TranslatorException
    {
        if(componentConfigs.containsKey(component)) {
            Converter<?, ? extends Component> componentConfig = componentConfigs.get(component);
            if(componentConfig.isEnabled())
                return componentConfig.readString();
        }
        return null;
    }

    /**
     * Sets the value of a property. Behavior is different depending on whether
     * the property is bound to an editor component or not:
     * <dl>
     * <dt>Bound Properties:</dt>
     * <dd>If the user input to the requested field is invalid, an exception is
     * thrown. An event will not be sent to listeners because the value that is
     * set is not necessarily valid to read back. To check if it is and notify
     * listeners, use one of the {@code check()} methods. Bound properties
     * completely ignore the {@code string} parameter, which may be set to
     * {@code null}. Disabled properties can be set as well as enabled
     * ones.</dd>
     * <dt>Unbound Properties:</dt>
     * <dd>Always sets the value. Listeners are notified immediately if the new
     * value is not the same as the previous one. The string value of the
     * property will be set as well. If it is {@code null}, it will be set to
     * {@code value.toString()}, unless {@code value} is {@code null} as
     * well.</dd>
     * </dl>
     *
     * @param propertyName the name of the property to set.
     * @param value the value to set.
     * @param string the string version of the value. Ignored completely for
     * bound properties.
     * @throws TranslatorException if the string value could not be written to
     * the editor component of a bound property.
     * @see #isBound(String)
     * @see Converter#isEnabled()
     * @since 1.0.0
     */
    public void set(String propertyName, Object value, String string) throws TranslatorException
    {
        if(namedConfigs.containsKey(propertyName))
            namedConfigs.get(propertyName).writeValue(value);
        else if(unboundProperties.containsKey(propertyName))
            unboundProperties.get(propertyName).setValue(value, string);
        else
            unboundProperties.put(propertyName, new UnboundNode(propertyName, value, string));
    }

    /**
     * Sets the value of the property edited by the specified component. If the
     * value can not be written to the component, an exception is thrown.
     * Listeners will not be notified since the property may not be valid to
     * read even if it is valid to write to the editor. Disabled properties can
     * be set as well as enabled ones.
     *
     * @param component the editor component to set the value to.
     * @param value the value to write to the component.
     * @throws TranslatorException if the string value could not be written to
     * the editor component.
     * @see Converter#isEnabled()
     * @since 1.0.0
     */
    public void set(Component component, Object value) throws TranslatorException
    {
        if(componentConfigs.containsKey(component))
            componentConfigs.get(component).writeValue(value);
    }

    /**
     * Checks if a property value has been updated, and notifies listeners if it
     * has. This method is only necessary for properties bound to an editor. It
     * is a no-op for unbound properties and disabled properties. This method
     * does not throw an exception, but it can optionally activate error markup
     * on a component if it contains invalid data.
     *
     * @param propertyName the name of the property to check.
     * @param mark indicates whether or not to apply error markup if a value
     * can not be read from the editor component.
     * @return {@code true} it the property has changed and listeners have been
     * notified, {@code false} otherwise. A return value of {@code false} may
     * mean that the editor does not contain a value that can be read, or that
     * the value has not been updated since last time it was read.
     * @since 1.0.0
     */
    public boolean check(String propertyName, boolean mark)
    {
        if(namedConfigs.containsKey(propertyName)) {
            Converter<?, ? extends Component> componentConfig = namedConfigs.get(propertyName);
            if(componentConfig.isEnabled())
                return componentConfig.checkValue(mark);
        }
        return false;
    }

    /**
     * Checks if a property value has been updated, and notifies listeners if it
     * has. This method is a no-op for disabled components. It does not throw an
     * exception, but it can optionally activate error markup on a component if
     * it contains invalid data.
     *
     * @param component the editor component to check.
     * @param mark indicates whether or not to apply error markup if a value
     * can not be read from the component.
     * @return {@code true} it the property has changed and listeners have been
     * notified, {@code false} otherwise. A return value of {@code false} may
     * mean that the editor does not contain a value that can be read, or that
     * the value has not been updated since last time it was read.
     * @since 1.0.0
     */
    public boolean check(Component component, boolean mark)
    {
        if(componentConfigs.containsKey(component)) {
            Converter<?, ? extends Component> componentConfig = componentConfigs.get(component);
            if(componentConfig.isEnabled())
                return componentConfig.checkValue(mark);
        }
        return false;
    }

    /**
     * Checks all registered component editors for property updates, and
     * notifies listeners of any updates. This method only checks properties
     * bound to a non-disabled editor. This method does not throw any
     * exceptions, but it can optionally activate error markup on components
     * that contain invalid data.
     *
     * @param mark indicates whether or not to apply error markup if a value
     * can not be read from an editor component.
     * @return {@code true} it any property has changed and listeners have been
     * notified, {@code false} otherwise. Editors that are disabled or contain
     * invalid values are considered not to have been updated.
     * @since 1.0.0
     */
    public boolean check(boolean mark)
    {
        boolean any = false;
        for(Converter<?, ? extends Component> componentConfig : namedConfigs.values()) {
            if(componentConfig.isEnabled())
                any |= componentConfig.checkValue(mark);
        }
        return any;
    }

    /**
     * Clears all of the error markings that are currently set for the editor of
     * the specified property. Error markings can be set by retrieving
     * individual property via {@code get()} or {@code getString()} or exporting
     * the form data via {@link #exportConfig()} or {@link #exportProperties()}
     * when the field contains an invalid value. Markings can also be set
     * manually by manipulating the property's {@code Converter}. This
     * operation only affects the UI. It does not modify the managed data in any
     * way. This method is a no-op for unbound properties.
     *
     * @param propertyName the name of the property for which errors are to be
     * cleared.
     * @since 1.0.0
     */
    public void clearErrors(String propertyName)
    {
        if(namedConfigs.containsKey(propertyName))
            namedConfigs.get(propertyName).clearError();
    }

    /**
     * Clears all of the error markings that are currently set for a registered
     * component. Error markings can be set by retrieving individual property
     * via {@code get()} or {@code getString()} or exporting the form data via
     * {@link #exportConfig()} or {@link #exportProperties()} when the field
     * contains an invalid value. Markings can also be set manually by
     * manipulating the property's {@code Converter}. This operation only
     * affects the UI. It does not modify the managed data in any way.
     *
     * @param component the component for which errors are to be cleared.
     * @since 1.0.0
     */
    public void clearErrors(Component component)
    {
        if(componentConfigs.containsKey(component))
            componentConfigs.get(component).clearError();
    }

    /**
     * Clears all of the error markings that are currently set in registered
     * components. Error markings can be set by retrieving individual properties
     * via {@code get()} or {@code getString()} or exporting the form data via
     * {@link #exportConfig()} or {@link #exportProperties()} when a form field
     * contains an invalid value. Markings can also be set individually or
     * manually by manipulating {@code Converter}s. This operation only
     * affects the UI. It does not modify the managed data in any way.
     *
     * @since 1.0.0
     */
    public void clearErrors()
    {
        for(Converter<?, ? extends Component> componentConfig : componentConfigs.values())
            componentConfig.clearError();
    }

    /**
     * Applies preprocessing to the input map before it is used to set form
     * fields. This method is invoked immediately before the import operation
     * begins. The default implementation of this method is a no-op. It is
     * provided to allow extending classes to modify the functionality of {@link
     * #importConfig(Map, boolean) importConfig()} through a hook without
     * redefining the entire method.
     *
     * @param properties a map of names to the objects that will be imported.
     * @throws TranslatorException if the preprocessing does not succeed for any
     * reason.
     * @since 1.0.0
     */
    protected void preImport(Map<String, Object> properties) throws TranslatorException {}

    /**
     * Applies postprocessing to the input map after it is used to set form
     * fields. This method is invoked just after the import operation ends. This
     * method is not invoked if either the preprocessing or import operation
     * threw an exception. The default implementation of this method is a no-op.
     * It is provided to allow extending classes to modify the functionality of
     * {@link #importConfig(Map, boolean) importConfig()} through a hook without
     * redefining the entire method.
     *
     * @param properties a map of names to the objects that have been imported.
     * @throws TranslatorException if the postprocessing does not succeed for
     * any reason.
     * @since 1.0.0
     */
    protected void postImport(Map<String, Object> properties) throws TranslatorException {}

    /**
     * Imports named properties into the registered components. Invalid values
     * will cause the import to fail with an exception if it is done in strict
     * mode. Otherwise, invalid values are skipped. Validity is determined by
     * the {@link Converter#writeValue(Object) Converter.writeValue()} method of
     * the configuration corresponding to each form field. The first exception
     * encountered will be thrown, but only after loading of all map values have
     * been attempted, even in strict mode. Note that a value that is valid when
     * written to a field may not be valid on output. Properties that are in the
     * input map but not registered to components will be added as unbound
     * properties. Properties with disabled configurations will be loaded in the
     * same way as enabled ones.
     * <p>
     * This methods provides two overridable hooks. {@link #preImport(Map)
     * preImport()} is invoked immediately before any values are retrieved from
     * the input map. {@link #postImport(Map) postImport()} is invoked
     * immediately after the entire map has been traversed and immediately
     * before this method returns. An exception either from either {@code
     * preImport()} or from this method will prevent {@code postImport()} from
     * executing.
     * <p>
     * All of the other import methods in this class provide different sources
     * for the properties they load, but delegate to this method to actually
     * import the data into the form.
     *
     * @param properties a map of names to values that will be loaded into the
     * form.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * input map, or that the input map contained an invalid value in non-strict
     * mode.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importConfig(Map<String, Object> properties, boolean strict) throws TranslatorException
    {
        preImport(properties);

        int failed = 0;
        TranslatorException exception = null;

        int fieldCount = 0;

        for(Map.Entry<String, Object> entry : properties.entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();

            try {
                set(name, value, null);
                if(isBound(name))
                    fieldCount++;
            } catch(TranslatorException te) {
                if(exception == null)
                    exception = te;
                failed++;
            }
        }

        if(strict && failed > 0) {
            String msg = "Failed with " + failed + " error";
            if(failed != 1)
                msg += "s";
            throw new TranslatorException(msg, exception, exception.getTranslator(), exception.getComponent());
        }

        postImport(properties);

        // since a each of the maps can have keys listed only once, the number
        // of bound properties that was set correctly must be the total number
        // of bound properties.
        // Note that disabled configs are not treated specially.
        return fieldCount == namedConfigs.size();
    }

    /**
     * Imports a property resource bundle into the form manager. This method
     * uses the interface provided by {@link ResourceBundle}. The named
     * resource must exist on the class path and contain string properties. This
     * method is a frontend for {@link #importConfig(Map, boolean)
     * importConfig()}, to which it delegates the process of actually loading
     * data into the form.
     *
     * @param resourceName the name of the resource to load. The name is
     * interpreted by {@link ResourceBundle#getBundle(String)}.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * resource bundle, or that the resource contained an invalid value in
     * non-strict mode.
     * @throws MissingResourceException if a resource with the specified name
     * could not be found.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importResource(String resourceName, boolean strict) throws MissingResourceException, TranslatorException
    {
        ResourceBundle bundle = ResourceBundle.getBundle(resourceName);
        Map<String, Object> properties = new HashMap<>();
        Enumeration<String> keys = bundle.getKeys();
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            properties.put(key, bundle.getObject(key));
        }
        return importConfig(properties, strict);
    }

    /**
     * Imports an XML properties file. This method uses the interface provided
     * by {@link Properties}. The named resource must be a properties file on
     * the class path. This method is a frontend for {@link
     * #importConfig(Map, boolean) importConfig()}, to which it delegates the
     * process of actually loading data into the form.
     *
     * @param resourceName the name of the XML file to load. The file is parsed
     * by {@link Properties#loadFromXML(InputStream)}.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * XML, or that the XML contained an invalid value in non-strict mode.
     * @throws IOException if there is an error reading the resource, the file
     * does not exist, can not be opened or parsed.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importXML(String resourceName, boolean strict) throws IOException, TranslatorException
    {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
            if(inputStream == null)
                throw new FileNotFoundException("Unable to find " + resourceName);
            return importXML(inputStream, strict);
        }
    }

    /**
     * Imports XML properties from an input stream. This method uses the
     * interface provided by {@link Properties}. The input stream must contain
     * valid XML property data. This method is a frontend for {@link
     * #importConfig(Map, boolean) importConfig()}, to which it delegates the
     * process of actually loading data into the form.
     *
     * @param inputStream the input stream from which data will be acquired. The
     * file is parsed by {@link Properties#loadFromXML(InputStream)}.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * XML, or that the XML contained an invalid value in non-strict mode.
     * @throws IOException if there is an error reading or parsing the stream.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importXML(InputStream inputStream, boolean strict) throws IOException, TranslatorException
    {
        Properties properties = new Properties();
        properties.loadFromXML(inputStream);            
        return importProperties(properties, strict);
    }

    /**
     * Imports a property set into the form manager. The property set must not
     * contain any non-{@code String} keys. This method is a frontend for {@link
     * #importConfig(Map, boolean) importConfig()}, to which it delegates the
     * process of actually loading data into the form.
     *
     * @param properties the property set to load. The parent's keys will not be
     * imported.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * property set, or that the property set contained an invalid value in
     * non-strict mode.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @throws ClassCastException if the property set contains non-{@code
     * String} keys.
     * @since 1.0.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean importProperties(Properties properties, boolean strict) throws TranslatorException, ClassCastException
    {
        // properties will complain about HashMap<String, Object>
        // importConfig will complain about HashMap<Object, Object>
        return importConfig(new HashMap(properties), strict);
    }

    /**
     * Imports a properties file. This method uses the interface provided by
     * {@link Properties}. The named resource must be a properties file on the
     * class path. This method is a frontend for {@link
     * #importConfig(Map, boolean) importConfig()}, which it delegates the
     * process of actually loading data into the form.
     *
     * @param resourceName the name of the properties file to load. The file is
     * parsed by {@link Properties#load(InputStream)}.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * input properties, or that the input properties contained an invalid value
     * in non-strict mode.
     * @throws IOException if there is an error reading the resource, the file
     * does not exist, can not be opened or parsed.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importProperties(String resourceName, boolean strict) throws IOException, TranslatorException
    {
        try(InputStream inputStream = ClassLoader.getSystemResourceAsStream(resourceName)) {
            if(inputStream == null)
                throw new FileNotFoundException("Unable to find " + resourceName);
            return importProperties(inputStream, strict);
        }
    }

    /**
     * Imports properties from an input stream. This method uses the interface
     * provided by {@link Properties}. The input stream must contain valid
     * property keys and values. This method is a frontend for {@link
     * #importConfig(Map, boolean) importConfig()}, to which it delegates the
     * process of actually loading data into the form.
     *
     * @param inputStream the input stream from which data will be acquired. The
     * file is parsed by {@link Properties#load(InputStream)}.
     * @param strict a boolean flag indicating the behavior when a property
     * value can not be written to a field due to a {@code TranslatorException}.
     * If this flag is {@code true}, the exception will be propagated. If {@code
     * false}, it will be ignored.
     * @return {@code true} if all of the bound fields in the form have been
     * updated successfully, {@code false} otherwise. A return value of {@code
     * false} may indicate that a property from the form is not present in the
     * input properties, or that the input properties contained an invalid value
     * in non-strict mode.
     * @throws IOException if there is an error reading or parsing the stream.
     * @throws TranslatorException if {@code strict} is {@code true} and any
     * of the bound properties could not be written to their respective editors.
     * @since 1.0.0
     */
    public boolean importProperties(InputStream inputStream, boolean strict) throws IOException, TranslatorException
    {
        Properties properties = new Properties();
        properties.load(inputStream);            
        return importProperties(properties, strict);
    }

    /**
     * Exports the form data into a map containing raw objects and notifies
     * listeners on bound properties of changes. This method is a shorthand for
     * {@code exportConfig(true)}.
     *
     * @return a map of all the enabled properties managed by this instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @see #exportConfig(boolean)
     * @since 1.0.0
     */
    public Map<String, Object> exportConfig() throws TranslatorException
    {
        return exportConfig(true);
    }

    /**
     * Exports the form data into a map containing raw objects. Bound and
     * unbound properties are exported. Disabled properties are not exported.
     * This method is a frontend for {@link #export(Map, boolean, boolean)
     * export()}, to which it delegates the process of exporting the data. All
     * keys in the exported data will be {@code String}s, but values may be of
     * any type.
     *
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a map of all the enabled properties managed by this instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @since 1.0.0
     */
    public Map<String, Object> exportConfig(boolean notify) throws TranslatorException
    {
        HashMap<String, Object> result = new HashMap<>();
        export(result, false, notify);
        return result;
    }

    /**
     * Exports the form data into a map containing only strings and notifies
     * listeners on bound properties of changes. This method is a shorthand for
     * {@code exportStrings(true)}.
     *
     * @return a map of all the enabled properties managed by this instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @see #exportStrings(boolean)
     * @since 1.0.0
     */
    public Map<String, String> exportStrings() throws TranslatorException
    {
        return exportStrings(true);
    }

    /**
     * Exports the form data into a map containing only strings. Bound and
     * unbound properties are exported. Disabled properties are not exported.
     * This method is a frontend for {@link #export(Map, boolean, boolean)
     * export()}, to which it delegates the process of exporting the data. All
     * keys and values in the exported data will be {@code String}s.
     *
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a map of all the enabled properties managed by this instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @since 1.0.0
     */
    public Map<String, String> exportStrings(boolean notify) throws TranslatorException
    {
        Map<String, String> result = new HashMap<>();
        export(result, true, notify);
        return result;
    }

    /**
     * Exports the form data into a property set and notifies listeners on bound
     * properties of changes. This method is a shorthand for {@code
     * exportProperties(true)}.
     *
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @see #exportProperties(boolean)
     * @since 1.0.0
     */
    public Properties exportProperties() throws TranslatorException
    {
        return exportProperties(true);
    }

    /**
     * Exports the form data into a property set. Bound and unbound properties
     * are exported. Disabled properties are not exported. This method is a
     * frontend for {@link #export(Map, boolean, boolean) export()}, to which it
     * delegates the process of exporting the data. All values are exported as
     * {@code String}s.
     *
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @since 1.0.0
     */
    public Properties exportProperties(boolean notify) throws TranslatorException
    {
        Properties result = new Properties();
        export(result, true, notify);
        return result;
    }

    /**
     * Exports the form data into a property set and writes it to a properties
     * file after notifying listeners on bound properties of any changes. This
     * method is a shorthand for {@code exportProperties(fileName, true)}.
     *
     * @param fileName the name of the properties file to write to. Any
     * previously existing file will be overwritten.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties file could not be opened or written
     * for any reason.
     * @see #exportProperties(String, boolean)
     * @since 1.0.0
     */
    public Properties exportProperties(String fileName) throws IOException, TranslatorException
    {
        return exportProperties(fileName, true);
    }

    /**
     * Exports the form data into a property set and writes it to a properties
     * file. Bound and unbound properties are exported and written. Disabled
     * properties are neither exported nor written out. This method uses the
     * {@link Properties#store(OutputStream, String)} API to write the
     * properties file. This method is a frontend for {@link
     * #export(Map, boolean, boolean) export()}, to which it delegates the
     * process of exporting the data. All values are exported as {@code
     * String}s.
     *
     * @param fileName the name of the properties file to write to. Any
     * previously existing file will be overwritten.
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties file could not be opened or written
     * for any reason.
     * @since 1.0.0
     */
    public Properties exportProperties(String fileName, boolean notify) throws IOException, TranslatorException
    {
        try(OutputStream outputStream = new FileOutputStream(fileName)) {
            return exportProperties(outputStream, notify);
        }
    }

    /**
     * Exports the form data into a property set and writes it to a character
     * stream after notifying listeners on bound properties of any changes. This
     * method is a shorthand for {@code exportProperties(outputStream, true)}.
     *
     * @param outputStream the stream to write the property set to.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties file could not be opened or written
     * for any reason.
     * @see #exportProperties(OutputStream, boolean)
     * @since 1.0.0
     */
    public Properties exportProperties(OutputStream outputStream) throws IOException, TranslatorException
    {
        return exportProperties(outputStream, true);
    }

    /**
     * Exports the form data into a property set and writes it to a character
     * stream. Bound and unbound properties are exported and written. Disabled
     * properties are neither exported nor written out. This method uses the
     * {@link Properties#store(OutputStream, String)} API to write the properties
     * to a stream. This method is a frontend for {@link
     * #export(Map, boolean, boolean) export()}, to which it delegates the
     * process of exporting the data. All values are exported as {@code
     * String}s.
     *
     * @param outputStream the stream to write the property set to.
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties could not be written to the stream
     * for any reason.
     * @since 1.0.0
     */
    public Properties exportProperties(OutputStream outputStream, boolean notify) throws IOException, TranslatorException
    {
        Properties result = exportProperties(notify);
        result.store(outputStream, null);
        return result;
    }

    /**
     * Exports the form data into a property set and writes it to an XML file
     * after notifying listeners on bound properties of any changes. This method
     * is a shorthand for {@code exportXML(fileName, true)}.
     *
     * @param fileName the name of the XML file to write to. Any previously
     * existing file will be overwritten.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the XML file could not be opened or written for
     * any reason.
     * @see #exportXML(String, boolean)
     * @since 1.0.0
     */
    public Properties exportXML(String fileName) throws IOException, TranslatorException
    {
        return exportXML(fileName, true);
    }

    /**
     * Exports the form data into a property set and writes it to an XML file.
     * Bound and unbound properties are exported and written. Disabled
     * properties are neither exported nor written out. This method uses the
     * {@link Properties#storeToXML(OutputStream, String)} API to write the XML
     * file. This method is a frontend for {@link #export(Map, boolean, boolean)
     * export()}, to which it delegates the process of exporting the data. All
     * values are exported as {@code String}s.
     *
     * @param fileName the name of the XML file to write to. Any previously
     * existing file will be overwritten.
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the XML file could not be opened or written for
     * any reason.
     * @since 1.0.0
     */
    public Properties exportXML(String fileName, boolean notify) throws IOException, TranslatorException
    {
        try(OutputStream outputStream = new FileOutputStream(fileName)) {
            return exportXML(outputStream, notify);
        }
    }

    /**
     * Exports the form data into a property set and writes it to a character
     * stream as XML after notifying listeners on bound properties of any
     * changes. This method is a shorthand for {@code
     * exportXML(outputStream, true)}.
     *
     * @param outputStream the stream to write the property set to.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties could not be written to the stream
     * for any reason.
     * @see #exportXML(OutputStream, boolean)
     * @since 1.0.0
     */
    public Properties exportXML(OutputStream outputStream) throws IOException, TranslatorException
    {
        return exportXML(outputStream, true);
    }

    /**
     * Exports the form data into a property set and writes it to a character
     * stream as XML. Bound and unbound properties are exported and written.
     * Disabled properties are neither exported nor written out. This method
     * uses the {@link Properties#storeToXML(OutputStream, String)} API to write
     * the properties to a stream. This method is a frontend for {@link
     * #export(Map, boolean, boolean) export()}, to which it delegates the
     * process of exporting the data. All values are exported as {@code
     * String}s.
     *
     * @param outputStream the stream to write the property set to.
     * @param notify specifies whether or not property change listeners on bound
     * properties should be notified when the data is exported successfully.
     * @return a property set with all the enabled properties managed by this
     * instance.
     * @throws TranslatorException if any of the component editors could not be
     * read.
     * @throws IOException if the properties could not be written to the stream
     * for any reason.
     * @since 1.0.0
     */
    public Properties exportXML(OutputStream outputStream, boolean notify) throws IOException, TranslatorException
    {
        Properties result = exportProperties(notify);
        result.storeToXML(outputStream, null);
        return result;
    }

    /**
     * Applies preprocessing to the output map before it is filled with form
     * field values and unbound properties. This method is invoked immediately
     * before the export operation begins, but after all errors have been
     * cleared in the form. The default implementation of this method is a
     * no-op. It is provided to allow extending classes to modify the
     * functionality of {@link #export(Map, boolean, boolean) export()} through
     * a hook without redefining the entire method.
     *
     * @param properties an empty map of the objects that will be exported.
     * @param strings a boolean indicating whether the data should be exported
     * as objects or as {@code String}s. If {@code true}, the map should not
     * contain any non-{@code String} objects.
     * @throws TranslatorException if the preprocessing does not succeed for any
     * reason.
     * @since 1.0.0
     */
    protected void preExport(Map<String, Object> properties, boolean strings) throws TranslatorException {}

    /**
     * Applies postprocessing to the output map after it has been filled with
     * form field data and unbound properties. This method is invoked just after
     * the export operation ends. This method is not invoked if either the
     * preprocessing or export operation threw an exception. The default
     * implementation of this method is a no-op. It is provided to allow
     * extending classes to modify the functionality of {@link
     * #export(Map, boolean, boolean) export()} through a hook without
     * redefining the entire method.
     *
     * @param properties a map of names to the objects that have been exported.
     * This includes unbound properties.
     * @param strings a boolean indicating whether the data should be exported
     * as objects or as {@code String}s. If {@code true}, the map should not
     * contain any non-{@code String} objects.
     * @throws TranslatorException if the postprocessing does not succeed for
     * any reason.
     * @since 1.0.0
     */
    protected void postExport(Map<String, Object> properties, boolean strings) throws TranslatorException {}

    /**
     * Exports the form data to the specified map. This method can export either
     * the raw object data or just strings to the map. This method clears all
     * errors before attempting to export data. The first exception caused by an
     * invalid read will be thrown, but only after the entire form has been
     * processed and all additional errors have been marked. If this no
     * exceptions are thrown, all unbound properties will be added to the
     * result. Listeners are notified only after all bound fields have been
     * exported successfully. Note that the exported map may have {@code null}
     * values.
     * <p>
     * This method honors the {@code useableWhenDisabled} property of {@code
     * Converter}. Disabled components are not exported unless their
     * configuration allows it. An editor component is enabled for export if
     * either the underlying component is enabled ({@code
     * Component.isEnabled()}), or its configuration allows it to be used when
     * disabled. This property must be set manually in the configuration itself.
     * <p>
     * This methods provides two overridable hooks. {@code preExport()} is
     * invoked immediately after clearing all error markers and before
     * retrieving any values from the form. {@code postExport()} is invoked
     * immediately after the entire form, including the unbound properties, has
     * been added to the map. An exception either from either {@code
     * preExport()} or from this method will prevent {@code postExport()} from
     * executing.
     * <p>
     * All of the other export methods in this class provide different outputs
     * formats for the exported properties, but delegate to this method to
     * actually export the data from the form. The lack of type arguments in the
     * input map allows the form to be exported directly to a {@code Properties}
     * object, which is a map of generic type {@code <Object, Object>} as well
     * as to a map of generic type {@code <String, Object>} or {@code
     * <String, String>}.

     * @param result the map to export to.
     * @param strings specifies whether the data should be exported as raw
     * objects or as {@code String}s.
     * @param notify specifies whether listeners should be notified of changed
     * bound properties.
     * @throws TranslatorException if the contents of any of the enabled editor
     * components could not be read. The first encountered exception is thrown
     * upwards, after error markup has been applied to all invalid components.
     * @see Converter#isUseableWhenDisabled()
     * @see #preExport(Map, boolean)
     * @see #postExport(Map, boolean)
     * @since 1.0.0
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void export(Map result, boolean strings, boolean notify) throws TranslatorException
    {
        clearErrors();

        preExport(result, strings);

        int failed = 0;
        TranslatorException exception = null;

        for(Converter<?, ? extends Component> componentConfig : componentConfigs.values()) {
            if(!componentConfig.isEnabled())
                continue;
            try {
                Object value;
                if(strings)
                    value = componentConfig.readString(false);
                else
                    value = componentConfig.readValue(false);
                result.put(componentConfig.getName(), value);
            } catch(ClassCastException cce) {
                if(!(cce instanceof TranslatorComponentException))
                    throw new TranslatorComponentException(
                            cce, componentConfig.getTranslator(), componentConfig.getComponent());
            } catch(TranslatorException te) {
                failed++;
                if(exception == null)
                    exception = te;
            }
        }

        if(failed > 0) {
            String msg = "Failed with " + failed + " error";
            if(failed != 1)
                msg += "s";
            throw new TranslatorException(msg, exception, exception.getTranslator(), exception.getComponent());
        } else {
            if(notify)
                check(false);

            for(Map.Entry<String, UnboundNode> entry : unboundProperties.entrySet()) {
                Object value;
                if(strings)
                    value = entry.getValue().getString();
                else
                    value = entry.getValue().getValue();
                result.put(entry.getKey(), value);
            }
        }

        postExport(result, strings);
    }

    /**
     * Maintains the value and listeners of unbound properties. Listeners are
     * notified whenever a value is changed. This class also provides methods
     * for binding and unbinding properties.
     *
     * @author Joseph Fox-Rabinovitz
     * @version 1.0.0, 06 June 2013
     * @since 1.0.0
     */
    private static class UnboundNode implements Serializable
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
         * The name of the property. May be retrieved by {@link #getName()}.
         *
         * @since 1.0.0
         */
        private final String name;

        /**
         * The current value of the property. May be retrieved by {@link
         * #getValue()}. Set by {@link #updateValue(Object, String)
         * updateValue()} along with {@link #string}.
         *
         * @since 1.0.0
         */
        private Object value;

        /**
         * The current string value of the property. In most cases, this will
         * just be {@code value.toString()}. However, some properties will have
         * diffrent conversion to strings. This value will only be different
         * from {@code value.toString()} if the property is unbound from an
         * editor and the {@code readValue()} and {@code readString()} methods
         * perform special parsing. This field may be retrieved by {@link
         * #getString()}. It is set by {@link #updateValue(Object, String)
         * updateValue()} along with {@link #value}.
         *
         * @since 1.0.0
         */
        private String string;

        /**
         * A set of listeners for changes to the property. Listeners will be
         * triggered whenever the property is set. This set is transferred to a
         * {@code Converter} when the property is bound. It is copied from
         * a confiuration if the property is unbound. This field must never be
         * {@code null}.
         *
         * @since 1.0.0
         */
        private final Set<PropertyChangeListener> listeners;

        /**
         * Constructs an unbound property from a new value. The node will not
         * have any listeners. This constructor is a convenience for {@link
         * JFormManager.UnboundNode#JFormManager.UnboundNode(String, Object,
         * String, PropertyChangeListener)} that is used by {@link #set(String,
         * Object, String)} when a registered property with the specified name
         * does not exist.
         *
         * @param name the name of the property.
         * @param value the value of the property, may be {@code null}.
         * @param string the string value of the property. If this parameter is
         * {@code null}, the string value will be set to {@code
         * value.toString()} if {@code value} is non-{@code null}.
         * @since 1.0.0
         */
        public UnboundNode(String name, Object value, String string)
        {
            this(name, value, string, null);
        }

        /**
         * Constructs an unbound property from a listener. The node's value and
         * string value will both be {@code null}. This constructor is a
         * convenience for {@link
         * JFormManager.UnboundNode#JFormManager.UnboundNode(String, Object,
         * String, PropertyChangeListener)} that is used by {@link
         * #addListener(String, PropertyChangeListener)} when a registered
         * property with the specified name does not exist.
         *
         * @param name the name of the property.
         * @param listener the listener to add. May be {@code null}. This
         * constructor doesn not send an event to the listner.
         * @since 1.0.0
         */
        public UnboundNode(String name, PropertyChangeListener listener)
        {
            this(name, null, null, listener);
        }

        /**
         * Constructs an unbound property from a new value and listener.
         *
         * @param name the name of the property.
         * @param value the value of the property, may be {@code null}.
         * @param string the string value of the property. If this parameter is
         * {@code null}, the string value will be set to {@code
         * value.toString()} if {@code value} is non-{@code null}.
         * @param listener the listener to add. May be {@code null}. This
         * constructor does not send an event to the listner.
         * @throws NullPointerException if {@code name} is {@code null}.
         * @since 1.0.0
         */
        public UnboundNode(String name, Object value, String string, PropertyChangeListener listener)
        {
            if(name == null)
                throw new NullPointerException("Unbound property name");
            this.name = name;
            this.value = value;
            this.listeners = new HashSet<>();

            updateValue(value, string);

            if(listener != null)
                listeners.add(listener);
        }

        /**
         * Constructs an unbound property from a bound one. The name and
         * listeners are transferred directly from the bound configuration. An
         * attempt is made to read the value and the string value from the
         * component. If it is impossible to do so due to a {@code
         * TranslatorException}, the name and value will be set to {@code null}.
         * This consuructor does not invoke {@code remove()} on the
         * configuration.
         *
         * @param componentConfig the configuration to create an unbound
         * property from.
         * @since 1.0.0
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public UnboundNode(Converter componentConfig)
        {
            this.name = componentConfig.getName();
            this.string = null;
            try {
                this.string = componentConfig.readString();
                this.value = componentConfig.readValue();
            } catch(TranslatorException te) {
                // if readString fails, so will readValue
                this.value = this.string;
            }
            this.listeners = new HashSet<>(componentConfig.getListeners());
        }

        /**
         * Returns the name of the property. Not that this method is not used internally by {@code JFormManager} because
         * it always stores nodes in a map keyed by name.
         *
         * @return the name of the property.
         * @since 1.0.0
         */
        @SuppressWarnings("unused")
        public String getName()
        {
            return this.name;
        }

        /**
         * Returns the current value of the property.
         *
         * @return the value of the property. May be {@code null}.
         * @since 1.0.0
         */
        public Object getValue()
        {
            return this.value;
        }

        /**
         * Returns the current string value of the property. This is usually the
         * same as {@code getValue().toString()}, but may be different if the
         * property is unbound from an editor with a custom translator.
         *
         * @return the string value of the property.
         * @since 1.0.0
         */
        public String getString()
        {
            return this.string;
        }

        /**
         * Changes the value of the property and notifies listeners. The new
         * value may be {@code null}. If the new string value is {@code null},
         * the property's string value will be set to {@code
         * newValue.toString()}, unless {@code newValue} is {@code null} as
         * well. This method is a no-op if the new value and the old value are
         * both {@code null} or if the new value is equal to the old value.
         *
         * @param newValue the value to set.
         * @param newString the string value to set. The string may not be
         * {@code null} if the value is {@code null}.
         * @since 1.0.0
         */
        public void setValue(Object newValue, String newString)
        {
            if((newValue == null && value != null) || !newValue.equals(value)) {
                PropertyChangeEvent event = new PropertyChangeEvent(null, name, value, newValue);
                for(PropertyChangeListener listener : listeners)
                    listener.propertyChange(event);
                updateValue(newValue, newString);
            }
        }

        /**
         * Adds a listener to the property. Listeners can only be added once to
         * a property. Adding a listener that has already been added is a no-op.
         *
         * @param listener the listener to add.
         * @since 1.0.0
         */
        public void addListener(PropertyChangeListener listener)
        {
            listeners.add(listener);
        }

        /**
         * Removes a listener from the property. Removing a non-existent
         * listener is a no-op.
         *
         * @param listener the listener to remove.
         * @since 1.0.0
         */
        public void removeListener(PropertyChangeListener listener)
        {
            listeners.remove(listener);
        }

        /**
         * Binds the property represented by this node to the specified
         * configuration. Any listeners that the property has are transferred
         * directly. An attempt is made to write the value to the newly assigned
         * editor component. If that fails, an attempt is made to write the
         * string value to the editor. Further exceptions are ignored.
         *
         * @param componentConfig the component configuration to bind the
         * property to.
         * @since 1.0.0
         */
        public void bind(Converter<?, ? extends Component> componentConfig)
        {
            componentConfig.addListeners(this.listeners);
            if(this.value != null) {
                try {
                    componentConfig.writeValue(this.value);
                } catch(TranslatorException te1) {
                    try {
                        if(this.string != null) {
                            componentConfig.writeValue(this.string);
                        }
                    } catch(TranslatorException te2) {}
                }
            }
        }

        /**
         * Changes the value of the property without notifying listeners. The
         * new value may be {@code null}. If the new string value is {@code
         * null}, the property's string value will be set to {@code
         * newValue.toString()}, unless {@code newValue} is {@code null} as
         * well.
         *
         * @param newValue the value to set.
         * @param newString the string value to set. If {@code null}, an attempt
         * will be made to set it to {@code newValue.toString()}.
         * @since 1.0.0
         */
        private void updateValue(Object newValue, String newString)
        {
            this.value = newValue;
            if(newString != null)
                string = newString;
            else if(newValue == null)
                this.string = null;
            else
                this.string = newValue.toString();
        }
    }
}
