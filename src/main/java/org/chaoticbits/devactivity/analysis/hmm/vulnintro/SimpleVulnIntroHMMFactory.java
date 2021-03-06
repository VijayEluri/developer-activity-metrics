package org.chaoticbits.devactivity.analysis.hmm.vulnintro;

import static org.chaoticbits.devactivity.analysis.hmm.vulnintro.VulnerabilityState.NEUTRAL;
import static org.chaoticbits.devactivity.analysis.hmm.vulnintro.VulnerabilityState.VULNERABLE;

import java.util.Collection;

import org.chaoticbits.devactivity.analysis.hmm.Fraction;
import org.chaoticbits.devactivity.analysis.hmm.IHMMFactory;
import org.chaoticbits.devactivity.analysis.hmm.IHMMState;
import org.chaoticbits.devactivity.analysis.hmm.IHMMTransition;
import org.chaoticbits.devactivity.analysis.hmm.builtin.SimpleStartState;
import org.chaoticbits.devactivity.analysis.hmm.builtin.SimpleTransition;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class SimpleVulnIntroHMMFactory implements IHMMFactory<ChurnSignal> {

	private DirectedSparseGraph<IHMMState<ChurnSignal>, IHMMTransition<ChurnSignal>> graph;
	private IHMMState<ChurnSignal> starting;

	public SimpleVulnIntroHMMFactory() {
	}

	public DirectedGraph<IHMMState<ChurnSignal>, IHMMTransition<ChurnSignal>> getStateGraph() {
		init();
		return graph;
	}

	public IHMMState<ChurnSignal> getStarting() {
		init();
		return starting;
	}

	private void init() {
		if (graph != null)
			return;
		graph = new DirectedSparseGraph<IHMMState<ChurnSignal>, IHMMTransition<ChurnSignal>>();
		build();
		relaxTransitionProbs();
	}

	private void build() {
		starting = new SimpleStartState<ChurnSignal>();
		VulnerabilityHMMState v = new VulnerabilityHMMState(VULNERABLE);
		VulnerabilityHMMState n = new VulnerabilityHMMState(NEUTRAL);
		graph.addEdge(e("start-v"), starting, v);
		graph.addEdge(e("start-n"), starting, n);
		graph.addEdge(e("v-v"), v, v);
		graph.addEdge(e("v-n"), v, n);
		graph.addEdge(e("n-n"), n, n);
		graph.addEdge(e("n-v"), n, v);
	}

	/**
	 * Default transition probability is Laplace - 1/n where n=neighbors
	 */
	private void relaxTransitionProbs() {
		for (IHMMState<ChurnSignal> state : graph.getVertices()) {
			Collection<IHMMTransition<ChurnSignal>> edges = graph.getOutEdges(state);
			int total = edges.size();
			for (IHMMTransition<ChurnSignal> edge : edges) {
				edge.setProbability(new Fraction(1, total));
			}
		}
	}

	private SimpleTransition<ChurnSignal> e(String name) {
		return new SimpleTransition<ChurnSignal>(name, new Fraction(1, 2));
	}

}
