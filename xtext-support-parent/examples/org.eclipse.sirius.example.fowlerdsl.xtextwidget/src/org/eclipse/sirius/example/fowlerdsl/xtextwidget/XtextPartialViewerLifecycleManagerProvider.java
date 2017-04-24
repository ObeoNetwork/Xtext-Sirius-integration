package org.eclipse.sirius.example.fowlerdsl.xtextwidget;

import org.eclipse.eef.EEFControlDescription;
import org.eclipse.eef.EEFCustomWidgetDescription;
import org.eclipse.eef.core.api.EditingContextAdapter;
import org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManager;
import org.eclipse.eef.ide.ui.api.widgets.IEEFLifecycleManagerProvider;
import org.eclipse.sirius.common.interpreter.api.IInterpreter;
import org.eclipse.sirius.common.interpreter.api.IVariableManager;

public class XtextPartialViewerLifecycleManagerProvider implements IEEFLifecycleManagerProvider {

	private static final String SUPPORTED_ID = "org.eclipse.sirius.example.fowlerdsl.xtextwidget.XtextPartialViewer";
	
	@Override
	public boolean canHandle(EEFControlDescription controlDescription) {
		return SUPPORTED_ID.equals(controlDescription.getIdentifier()) && 
				controlDescription instanceof EEFCustomWidgetDescription;
	}

	@Override
	public IEEFLifecycleManager getLifecycleManager(
			EEFControlDescription controlDescription, 
			IVariableManager variableManager,
			IInterpreter interpreter, 
			EditingContextAdapter contextAdapter) {
		if (controlDescription instanceof EEFCustomWidgetDescription) {
			return new XtextPartialViewerLifecycleManager((EEFCustomWidgetDescription) controlDescription, variableManager, interpreter, contextAdapter);
		}
		throw new IllegalArgumentException();
	}
	

}
