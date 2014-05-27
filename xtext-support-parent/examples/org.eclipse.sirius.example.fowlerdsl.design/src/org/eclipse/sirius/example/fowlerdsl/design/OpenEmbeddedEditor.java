package org.eclipse.sirius.example.fowlerdsl.design;

import org.eclipse.xtext.example.fowlerdsl.ui.internal.StatemachineActivator;
import org.obeonetwork.dsl.viewpoint.xtext.support.action.OpenXtextEmbeddedEditor;

import com.google.inject.Injector;

public class OpenEmbeddedEditor extends OpenXtextEmbeddedEditor {

	@Override
	protected Injector getInjector() {
		 return  StatemachineActivator.getInstance().getInjector("org.eclipse.xtext.example.fowlerdsl.Statemachine");
	}

}
