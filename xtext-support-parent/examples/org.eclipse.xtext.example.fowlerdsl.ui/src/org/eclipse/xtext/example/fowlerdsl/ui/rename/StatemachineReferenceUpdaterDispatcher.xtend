package org.eclipse.xtext.example.fowlerdsl.ui.rename

import com.google.inject.Inject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.IFragmentProvider
import org.eclipse.xtext.resource.IResourceServiceProvider
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription
import org.eclipse.xtext.ui.refactoring.IRefactoringUpdateAcceptor
import org.eclipse.xtext.ui.refactoring.impl.ReferenceUpdaterDispatcher
import org.eclipse.xtext.ui.refactoring.impl.StatusWrapper

class StatemachineReferenceUpdaterDispatcher extends ReferenceUpdaterDispatcher {
	
	@Inject IResourceServiceProvider.Registry resourceServiceProviderRegistry;
	
	override protected createFindReferenceAcceptor(IRefactoringUpdateAcceptor updateAcceptor) {
		new StatemachineReferenceDescriptionAcceptor(resourceServiceProviderRegistry, updateAcceptor.getRefactoringStatus())
	}

	static class StatemachineReferenceDescriptionAcceptor extends ReferenceDescriptionAcceptor {
	
		new(Registry resourceServiceProviderRegistry, StatusWrapper status) {
			super(resourceServiceProviderRegistry, status)
		}
		
		override void accept(EObject source, URI sourceURI, EReference eReference, int index, EObject targetOrProxy, URI targetURI) {
			accept(new DefaultReferenceDescription(source.fallbackURI, targetURI, eReference, index, null)) 
		}	
		
		protected def URI getFallbackURI(EObject object) {
			val resource = object.eResource
			if(resource instanceof XtextResource) {
				val field = XtextResource.getDeclaredField('fragmentProviderFallback')
				field.accessible = true
				val fragmentProviderFallback = field.get(resource) as IFragmentProvider.Fallback
				val fragment = fragmentProviderFallback.getFragment(object)
				val resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(resource)
				return resourceURI.appendFragment(fragment)
			} else {
				return EcoreUtil2.getPlatformResourceOrNormalizedURI(object)
			}
		}
	}
}