package org.eclipse.xtext.example.fowlerdsl.ui.rename;

import com.google.common.collect.Iterables;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.example.fowlerdsl.statemachine.State;
import org.eclipse.xtext.example.fowlerdsl.statemachine.Transition;
import org.eclipse.xtext.ui.refactoring.impl.DefaultDependentElementsCalculator;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ListExtensions;

@SuppressWarnings("all")
public class StatemachineDependentElementsCalculator extends DefaultDependentElementsCalculator {
  @Override
  public List<URI> getDependentElementURIs(final EObject baseElement, final IProgressMonitor monitor) {
    final List<URI> dependentElementURIs = super.getDependentElementURIs(baseElement, monitor);
    if ((baseElement instanceof State)) {
      final Function1<Transition, URI> _function = (Transition it) -> {
        return EcoreUtil.getURI(it);
      };
      List<URI> _map = ListExtensions.<Transition, URI>map(((State)baseElement).getTransitions(), _function);
      Iterables.<URI>addAll(dependentElementURIs, _map);
    }
    return dependentElementURIs;
  }
}
