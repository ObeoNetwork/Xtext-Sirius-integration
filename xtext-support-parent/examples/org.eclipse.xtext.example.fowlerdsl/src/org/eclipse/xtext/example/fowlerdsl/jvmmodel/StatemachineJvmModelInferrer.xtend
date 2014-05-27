package org.eclipse.xtext.example.fowlerdsl.jvmmodel

import com.google.inject.Inject
import com.google.inject.Provider
import org.eclipse.xtext.example.fowlerdsl.statemachine.Statemachine
import org.eclipse.xtext.xbase.jvmmodel.AbstractModelInferrer
import org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor
import org.eclipse.xtext.xbase.jvmmodel.JvmTypesBuilder
import static org.eclipse.xtext.common.types.JvmVisibility.*
/**
 * <p>Infers a JVM model from the source model.</p> 
 *
 * <p>The JVM model should contain all elements that would appear in the Java code 
 * which is generated from the source model. Other models link against the JVM model rather than the source model.</p>     
 */
class StatemachineJvmModelInferrer extends AbstractModelInferrer {

    /**
     * convenience API to build and initialize JVM types and their members.
     */
	@Inject extension JvmTypesBuilder

	/**
	 * The dispatch method {@code infer} is called for each instance of the
	 * given element's type that is contained in a resource.
	 * 
	 * @param element
	 *            the model to create one or more
	 *            {@link org.eclipse.xtext.common.types.JvmDeclaredType declared
	 *            types} from.
	 * @param acceptor
	 *            each created
	 *            {@link org.eclipse.xtext.common.types.JvmDeclaredType type}
	 *            without a container should be passed to the acceptor in order
	 *            get attached to the current resource. The acceptor's
	 *            {@link IJvmDeclaredTypeAcceptor#accept(org.eclipse.xtext.common.types.JvmDeclaredType)
	 *            accept(..)} method takes the constructed empty type for the
	 *            pre-indexing phase. This one is further initialized in the
	 *            indexing phase using the closure you pass to the returned
	 *            {@link org.eclipse.xtext.xbase.jvmmodel.IJvmDeclaredTypeAcceptor.IPostIndexingInitializing#initializeLater(org.eclipse.xtext.xbase.lib.Procedures.Procedure1)
	 *            initializeLater(..)}.
	 * @param isPreIndexingPhase
	 *            whether the method is called in a pre-indexing phase, i.e.
	 *            when the global index is not yet fully updated. You must not
	 *            rely on linking using the index if isPreIndexingPhase is
	 *            <code>true</code>.
	 */
   	def dispatch void infer(Statemachine stm, IJvmDeclaredTypeAcceptor acceptor, boolean isPreIndexingPhase) {
//   	
//   // create exactly one Java class per state machine
//   acceptor.accept(stm.toClass(stm.className)).initializeLater [
//     
//     // add a field for each service annotated with @Inject
//     members += stm.services.map[ service |
//       service.toField(service.name, service.type)
//     ]
//     
//     // generate a method for each state which has an action block
//     members += stm.states.filter[action!=null].map[state |
//       state.toMethod('do'+state.name.toFirstUpper, state.newTypeRef(Void::TYPE)) [
//         visibility = PROTECTED
//         
//         // Associate the expression with the body of this method.
//         body = state.action
//       ]
//     ]
//     
//     // generate a method containing the actual state machine code
//     members += stm.toMethod("run", newTypeRef(Void::TYPE)) [
//       
//       // the run method shall be provided with an event provider Provider<String> 
////       val eventProvider = stm.newTypeRef( stm.newTypeRef(typeof(String)))
////       parameters += stm.toParameter("eventSource", eventProvider)
//       
//       // generate the body
//       body = [append('''
//         //TODO         
//       ''')]
//     ]
//   ]
 }
     
     def className(Statemachine statemachine) {
       val lastSegment = statemachine.eResource.URI.lastSegment
       lastSegment.substring(0, lastSegment.indexOf('.')) 
     }
}

