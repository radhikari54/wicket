/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.ajax;

import java.util.HashMap;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.util.lang.Args;

/**
 * An ajax behavior that is attached to a certain client-side (usually javascript) event, such as
 * click, change, keydown, etc.
 * <p>
 * Example:
 * 
 * <pre>
 *         WebMarkupContainer div=new WebMarkupContainer(...);
 *         div.setOutputMarkupId(true);
 *         div.add(new AjaxEventBehavior(&quot;click&quot;) {
 *             protected void onEvent(AjaxRequestTarget target) {
 *                 System.out.println(&quot;ajax here!&quot;);
 *             }
 *         }
 * </pre>
 * 
 * This behavior will be linked to the <em>click</em> javascript event of the div WebMarkupContainer
 * represents, and so anytime a user clicks this div the {@link #onEvent(AjaxRequestTarget)} of the
 * behavior is invoked.
 * 
 * @since 1.2
 *
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AjaxEventBehavior extends AbstractDefaultAjaxBehavior
{
	private static final long serialVersionUID = 1L;

	/**
	 * Meta data that indicates that there is EventDelegatingBehavior somewhere in the page hierarchy
	 * The key is the event name and the value is a boolean flag.
	 */
	protected static final MetaDataKey<HashMap<String, Integer>> EVENT_NAME_PAGE_KEY = new MetaDataKey<HashMap<String, Integer>>() {};

	private final String event;

	/**
	 * Construct.
	 * 
	 * @param event
	 *      the event this behavior will be attached to
	 */
	public AjaxEventBehavior(String event)
	{
		Args.notEmpty(event, "event");

		onCheckEvent(event);

		event = event.toLowerCase();
		if (event.startsWith("on"))
		{
			event = event.substring(2);
		}

		this.event = event;
	}

	@Override
	public void renderHead(final Component component, final IHeaderResponse response)
	{
		super.renderHead(component, response);

		if (component.isEnabledInHierarchy())
		{
			boolean found = false;
			HashMap<String, Integer> enabledEvents = component.getPage().getMetaData(EVENT_NAME_PAGE_KEY);
			if (enabledEvents != null && enabledEvents.containsKey(getEvent()))
			{
				Component cursor = component.getParent();
				while (cursor != null && cursor instanceof Page == false)
				{
					List<EventDelegatingBehavior> behaviors = cursor.getBehaviors(EventDelegatingBehavior.class);
					for (EventDelegatingBehavior behavior : behaviors)
					{
						if (getEvent().equalsIgnoreCase(behavior.getEvent()))
						{
							CharSequence attributes = renderAjaxAttributes(component);
							behavior.contributeComponentAttributes(component.getMarkupId(), attributes);
							found = true;
							break;
						}
					}
					cursor = cursor.getParent();
				}
			}

			if (found == false)
			{
				CharSequence js = getCallbackScript(component);

				response.render(OnDomReadyHeaderItem.forScript(js.toString()));
			}
		}
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
	{
		super.updateAjaxAttributes(attributes);

		attributes.setEventNames(event);
	}

	/**
	 * 
	 * @param event
	 *      the event this behavior will be attached to
	 */
	protected void onCheckEvent(final String event)
	{
	}

	/**
	 * 
	 * @return event
	 *      the event this behavior is attached to
	 */
	public final String getEvent()
	{
		return event;
	}

	/**
	 * 
	 * @see org.apache.wicket.ajax.AbstractDefaultAjaxBehavior#respond(AjaxRequestTarget)
	 */
	@Override
	protected final void respond(final AjaxRequestTarget target)
	{
		onEvent(target);
	}

	/**
	 * Listener method for the ajax event
	 * 
	 * @param target
	 *      the current request handler
	 */
	protected abstract void onEvent(final AjaxRequestTarget target);
}
