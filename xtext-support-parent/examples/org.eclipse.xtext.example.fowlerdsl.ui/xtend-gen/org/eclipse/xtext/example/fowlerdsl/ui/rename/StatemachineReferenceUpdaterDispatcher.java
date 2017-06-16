package org.eclipse.xtext.example.fowlerdsl.ui.rename;

import com.google.inject.Inject;
import java.lang.reflect.Field;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.resource.IFragmentProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.DefaultReferenceDescription;
import org.eclipse.xtext.ui.refactoring.IRefactoringUpdateAcceptor;
import org.eclipse.xtext.ui.refactoring.impl.ReferenceUpdaterDispatcher;
import org.eclipse.xtext.ui.refactoring.impl.StatusWrapper;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class StatemachineReferenceUpdaterDispatcher extends ReferenceUpdaterDispatcher {
  public static class StatemachineReferenceDescriptionAcceptor extends ReferenceUpdaterDispatcher.ReferenceDescriptionAcceptor {
    public StatemachineReferenceDescriptionAcceptor(final IResourceServiceProvider.Registry resourceServiceProviderRegistry, final StatusWrapper status) {
      super(resourceServiceProviderRegistry, status);
    }
    
    @Override
    public void accept(final EObject source, final URI sourceURI, final EReference eReference, final int index, final EObject targetOrProxy, final URI targetURI) {
      URI _fallbackURI = this.getFallbackURI(source);
      DefaultReferenceDescription _defaultReferenceDescription = new DefaultReferenceDescription(_fallbackURI, targetURI, eReference, index, null);
      this.accept(_defaultReferenceDescription);
    }
    
    protected URI getFallbackURI(final EObject object) {
      try {
        final Resource resource = object.eResource();
        if ((resource instanceof XtextResource)) {
          final Field field = XtextResource.class.getDeclaredField("fragmentProviderFallback");
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
  
  @Inject
  private IResourceServiceProvider.Registry resourceServiceProviderRegistry;
  
  @Override
  protected ReferenceUpdaterDispatcher.ReferenceDescriptionAcceptor createFindReferenceAcceptor(final IRefactoringUpdateAcceptor updateAcceptor) {
    StatusWrapper _refactoringStatus = updateAcceptor.getRefactoringStatus();
    return new StatemachineReferenceUpdaterDispatcher.StatemachineReferenceDescriptionAcceptor(this.resourceServiceProviderRegistry, _refactoringStatus);
  }
}
