package org.apache.wicket.examples.stateless;

import java.util.Arrays;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.StatelessAjaxSubmitLink;
import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxButton;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxFallbackLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;


public class AjaxStatelessExample extends WicketExamplePage
{

	private static final String COUNTER_PARAM = "counter";

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 *
	 * @param parameters
	 *            Page parameters
	 */
	public AjaxStatelessExample(final PageParameters parameters)
	{
		super(parameters);
		setStatelessHint(true);
		
		add(new Label("message", new SessionModel()));
		add(new BookmarkablePageLink<>("indexLink", Index.class));
		
		final Label incrementLabel = new Label("incrementLabel", new AbstractReadOnlyModel<Integer>()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public Integer getObject()
			{
				final String counter = getParameter(parameters, COUNTER_PARAM);
				return counter != null ? Integer.parseInt(counter) : 0;
			}

		});
		final Link<?> incrementLink = new AjaxFallbackLink<Void>("incrementLink")
		{

			@Override
			public void onClick(final AjaxRequestTarget target)
			{				
				Integer counter = (Integer)incrementLabel.getDefaultModelObject();
				updateParams(getPageParameters(), counter);
				
				target.add(incrementLabel, this);
			}
			
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		};

		add(incrementLink);
		add(incrementLabel.setOutputMarkupId(true));

		final TextField<String> nameField = new TextField<String>("name", new Model<String>(""));
		final TextField<String> surnameField = new TextField<String>("surname", new Model<String>(""));

		final Form<String> form = new StatelessForm<String>("inputForm")
		{

			@Override
			protected void onSubmit()
			{

			}

		};
		final DropDownChoice<String> select = new DropDownChoice<String>("select",
			new Model<String>("2"), Arrays.asList(new String[] { "1", "2", "3" }));
		final Label selectedValue = new Label("selectedValue", "");
		add(selectedValue.setOutputMarkupId(true));

		select.add(new AjaxFormComponentUpdatingBehavior("change")
		{

			@Override
			protected void onUpdate(final AjaxRequestTarget target)
			{
				final String value = select.getModelObject();
				selectedValue.setDefaultModelObject("Selected value: " + value);
				target.add(selectedValue);
			}
			
			@Override
			public boolean getStatelessHint(Component component)
			{
				return true;
			}
		});

		form.add(nameField.setRequired(true));
		form.add(surnameField.setRequired(true));

		final Component feedback = new FeedbackPanel("feedback");
		final Label submittedValues = new Label("submittedValues", "");

		form.add(feedback.setOutputMarkupId(true));
		form.add(submittedValues.setOutputMarkupId(true));

		form.add(new StatelessAjaxSubmitLink("submit")
		{
			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				super.onError(target, form);
				target.add(feedback);
			}

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				super.onSubmit(target, form);
				String values = "Your name is: " + nameField.getModelObject() + " " + surnameField.getModelObject();
				submittedValues.setDefaultModelObject(values);
				target.add(feedback, submittedValues);
			}
		});

		add(form);

		add(select);


		add(new IndicatingAjaxFallbackLink("indicatingLink")
		{
			@Override
			public void onClick(AjaxRequestTarget target)
			{
				try
				{
					Thread.sleep(5000); // 1000 milliseconds is one second.
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
			
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		});

		StatelessForm indicatingForm = new StatelessForm("indicatingForm");

		add(indicatingForm);
		add(new IndicatingAjaxButton("indicatingButton", indicatingForm)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				try
				{
					Thread.sleep(5000); // 1000 milliseconds is one second.
				}
				catch (InterruptedException ex)
				{
					Thread.currentThread().interrupt();
				}
			}
			
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		});

	}

	private String getParameter(final PageParameters parameters, final String key)
	{
		final StringValue value = parameters.get(key);

		if (value.isNull() || value.isEmpty())
		{
			return null;
		}

		return value.toString();
	}

	protected final void updateParams(final PageParameters pageParameters, final int counter)
	{
		pageParameters.set(COUNTER_PARAM, Integer.toString(counter + 1));
	}
}