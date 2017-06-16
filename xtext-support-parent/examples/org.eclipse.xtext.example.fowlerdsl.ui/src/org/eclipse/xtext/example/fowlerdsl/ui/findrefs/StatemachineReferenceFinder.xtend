package 
org.eclipse.xtext.example.fowlerdsl.ui.findrefs

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.resource.IFragmentProvider
import org.eclipse.xtext.resource.IReferenceDescription
import org.eclipse.xtext.resource.IResourceServiceProvider.Registry
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ui.editor.findrefs.DelegatingReferenceFinder
import org.eclipse.xtext.ui.editor.findrefs.ReferenceAcceptor
import org.eclipse.xtext.util.IAcceptor

class StatemachineReferenceFinder extends DelegatingReferenceFinder {
		
	override protected toAcceptor(IAcceptor<IReferenceDescription> acceptor) {
		new StatemachineReferenceAcceptor(acceptor, resourceServiceProviderRegistry)
	}
	
	static class StatemachineReferenceAcceptor extends ReferenceAcceptor {
	
		protected new(IAcceptor<IReferenceDescription> delegate, Registry resourceServiceProviderRegistry) {
			super(delegate, resourceServiceProviderRegistry)
		}
	
		override void accept(EObject source, URI sourceURI, EReference eReference, int index, EObject targetOrProxy, URI targetURI) {
			super.accept(source, sourceURI, eReference, index, targetOrProxy, targetURI)
			this.accept(this.createReferenceDescription(source.fallbackURI, targetURI, eReference, index, this.findExportedContainer(source))) 
		}	
		
		protected def URI getFallbackURI(EObject object) {
			val resource = object.eResource
			if(resource instanceof XtextResource) {
				val field = XtextResource.getField('fragmentProviderFallback')
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
