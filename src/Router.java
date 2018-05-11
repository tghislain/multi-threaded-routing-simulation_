import java.util.LinkedList;

/*
 * This runnable routes packets as they traverse the network.
 */
class Router implements Runnable {
	private LinkedList<Packet> list = new LinkedList<Packet>();
	private int routes[];
	private Router routers[];
	private int routerNum;
	private boolean end = false;
	private boolean networkEmpty = false;

	Router(int rts[], Router rtrs[], int num) {
		routes = rts;
		routers = rtrs;
		routerNum = num;
	}

	/*
	 * Add a packet to this router. Add some details on how this works.
	 */
	public void addWork(Packet p) {
		// save packet in list to be processed and notify our thread that there
		// is something to process
		networkEmpty = false;
		synchronized (list) {
			list.add(p);
			list.notify();
		}

	}

	/*
	 * End the thread, once no more packets are outstanding.
	 */
	public synchronized void end() {
		// tell the router that it should shutdown once there is no more packets
		// in the network
		end = true;
		synchronized (list) {
			list.notify();
		}

	}

	public synchronized void networkEmpty() {
		networkEmpty = true;
		synchronized (list) {
			list.notify();
		}
	}

	/*
	 * Process packets. Add some details on how this works.
	 */
	public void run() {
		// while end has not been called keep running
		while (!end) {

			// if the network is empty but end has not been called wait
			if (networkEmpty) {
				try {
					synchronized (list) {
						list.wait();
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			// while there are packets in the network do not exit
			while (!networkEmpty) {
				// wait if there is no packet in our list

				if (list.isEmpty()) {

					try {
						synchronized (list) {
							if (list.isEmpty() && !networkEmpty)
								list.wait();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				// get a packet from our list to process
				Packet tempPacket;
				synchronized (list) {
					tempPacket = list.peek();
					if (tempPacket != null)
						list.removeFirst();

				}

				// record this router on the
				// packet's path
				if (tempPacket != null) {

					tempPacket.Record(routerNum);

					// check if the packet's destination is not this router
					// and
					// forward it
					if (tempPacket.getDestination() != routerNum) {
						// find the next router for this packet
						int nextRouter = routes[tempPacket.getDestination()];
						routers[nextRouter].addWork(tempPacket);

					} else {
						// remove the packet from network because it is at the destination
						routing.decPacketCount();

						if (routing.getPacketCount() == 0)
							break;

					}
				}

			}

		}

	}
}