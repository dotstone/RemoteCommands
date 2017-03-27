package task.client.dispatcher;

import java.util.Collection;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import io.vertx.core.net.NetSocket;

public class MostMatchesDispatcher implements TaskDispatcher {
	
	private int outstanding;
	
	private Multiset<String> results;

	public void sendMessages(Collection<NetSocket> netSockets, String msg) {
		results = HashMultiset.create();
		outstanding = netSockets.size();
		netSockets.forEach(socket -> socket.write(msg));
	}

	@Override
	public String messageReceived(String msg) {
		outstanding--;
		results.add(msg);
		if(outstanding < results.size()) {
			return getResult();
		}
		return null;
	}

	private String getResult() {
		String curResult = null;
		int maxMatches = 0;
		int distanceToNextBest = 0;			// track the count distance to the next best result
		for(String result : results.elementSet()) {
			if(result == null) {
				continue;
			}
			int curCount = results.count(result);
			if(curResult == null || curCount > maxMatches) {
				distanceToNextBest = curCount - maxMatches;
				curResult = result;
				maxMatches = curCount;
			} else if(curCount > maxMatches - distanceToNextBest) {
				distanceToNextBest = maxMatches - curCount;
			}
		}
		if(outstanding == 0 || distanceToNextBest > outstanding) {
			// Only take the result if there is no chance that another result can catch up
			return curResult;
		}
		return null;
	}
	
	
}
