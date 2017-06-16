package org.eclipse.xtext.example.fowlerdsl.resource;

import java.lang.reflect.Field;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IFragmentProvider;
import org.eclipse.xtext.resource.IReferenceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class StatemachineResourceDescriptionStrategy extends DefaultResourceDescriptionStrategy {
  @Override
  protected IReferenceDescription createReferenceDescription(final EObject owner, final URI exportedContainerURI, final EReference eReference, final int indexInList, final EObject target) {
    final URI ownerURI = this.getFallbackURI(owner);
    final URI targetURI = this.getFallbackURI(target);
    return new DefaultReferenceDescription(ownerURI, targetURI, eReference, indexInList, exportedContainerURI);
  }
  
  protected URI getFallbackURI(final EObject object) {
    try {
      final Resource resource = object.eResource();
      if ((resource instanceof XtextResource)) {
        final Field field = XtextResource.class.getField("fragmentProviderFallback");
        field.setAccessible(true);
        Object _get = field.get(resource);
        final IFragmentProvider.Fallback fragmentProviderFallback = ((IFragmentProvider.Fallback) _get);
        final String fragment = fragmentProviderFallback.getFragment(object);
        final URI resourceURI = EcoreUtil2.getPlatformResourceOrNormalizedURI(resource);
        return resourceURI.appendFragment(fragment);
      } else {
        return EcoreUtil2.getPlatformResourceOrNormalizedURI(object);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
