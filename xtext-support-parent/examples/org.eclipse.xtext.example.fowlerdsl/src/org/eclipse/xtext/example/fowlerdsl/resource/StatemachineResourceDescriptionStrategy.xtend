package org.eclipse.xtext.example.fowlerdsl.resource

import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.IFragmentProvider
import org.eclipse.xtext.EcoreUtil2

class StatemachineResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
	
	override protected createReferenceDescription(EObject owner, URI exportedContainerURI, EReference eReference, int indexInList, EObject target) {
		val ownerURI = getFallbackURI(owner)
		val targetURI = getFallbackURI(target)
		return new DefaultReferenceDescription(ownerURI, targetURI, eReference, indexInList, exportedContainerURI)
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