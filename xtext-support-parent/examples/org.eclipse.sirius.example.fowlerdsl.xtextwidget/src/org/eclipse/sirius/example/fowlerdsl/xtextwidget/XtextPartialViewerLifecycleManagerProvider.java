package org.eclipse.sirius.example.fowlerdsl.xtextwidget;

import org.eclipse.eef.EEFControlDescription;
import org.eclipse.eef.EEFCustomWidgetDescription;
import org.eclipse.eef.EEFGroupDescription;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager;
import org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManagerProvider;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;

public class XtextPartialViewerLifecycleManagerProvider implements IEEFLifecycleManagerProvider {

	private static final String SUPPORTED_ID = "XtextGroup";

	@Override
	public boolean canHandle(EEFControlDescription controlDescription) {
		/*
		 * it seems there is a bug in EEF right now which prevents me from relying on
		 * the identifier directly.
		 */
		return controlDescription.eContainer() instanceof EEFGroupDescription
				&& SUPPORTED_ID.equals(((EEFGroupDescription) controlDescription.eContainer()).getIdentifier())
				&& controlDescription instanceof EEFCustomWidgetDescription;
	}

	@Override
	public IEEFLifecycleManager getLifecycleManager(EEFControlDescription controlDescription,
			IVariableManager variableManager, IInterpreter interpreter, EditingContextAdapter contextAdapter) {
		if (controlDescription instanceof EEFCustomWidgetDescription) {
			return new XtextPartialViewerLifecycleManager((EEFCustomWidgetDescription) controlDescription,
					variableManager, interpreter, contextAdapter);
		}
		throw new IllegalArgumentException();
	}

}
