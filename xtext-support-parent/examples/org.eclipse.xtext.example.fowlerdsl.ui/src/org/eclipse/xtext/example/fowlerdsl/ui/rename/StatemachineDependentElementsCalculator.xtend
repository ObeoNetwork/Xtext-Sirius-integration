package org.eclipse.xtext.example.fowlerdsl.ui.rename

import org.eclipse.core.runtime.IProgressMonitor
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.example.fowlerdsl.statemachine.State
import org.eclipse.xtext.ui.refactoring.impl.DefaultDependentElementsCalculator

import static extension org.eclipse.emf.ecore.util.EcoreUtil.*

class StatemachineDependentElementsCalculator extends DefaultDependentElementsCalculator {
	
	override getDependentElementURIs(EObject baseElement, IProgressMonitor monitor) {
		val dependentElementURIs = super.getDependentElementURIs(baseElement, monitor)
		if(baseElement instanceof State) 
			dependentElementURIs += baseElement.transitions.map[URI]		
		return dependentElementURIs
	}
	
}