package de.fu_berlin.inf.focustracker.repository;

import junit.framework.TestCase;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.core.ResolvedSourceField;

import de.fu_berlin.inf.focustracker.interaction.Action;
import de.fu_berlin.inf.focustracker.interaction.JavaInteraction;
import de.fu_berlin.inf.focustracker.interaction.Origin;
import de.fu_berlin.inf.focustracker.repository.InteractionRepository;

public class TestInteractionRepository extends TestCase {

	public void testGetAll() throws Exception {
		
		InteractionRepository rep = InteractionRepository.getInstance();
		rep.add(new JavaInteraction(Action.COLLAPSED, new ResolvedSourceField(null, "name", "key"), 1d, Origin.CONSOLE));
		
		System.err.println(rep.getAll(IMember.class));
		
	}
}
