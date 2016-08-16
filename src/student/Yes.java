package student;

import game.Manager;
import game.Truck;
import game.Parcel;
import game.Node;
import java.util.*;

public class Yes extends Manager {

	private boolean runner = true;
	ArrayList<Truck> trucklist = new ArrayList<Truck>();
	List<Parcel> parcellist = Collections.synchronizedList(new ArrayList<Parcel>());
	HashMap<Truck, Parcel> parcellookup = new HashMap<Truck, Parcel>();
	Set<Parcel> parcelset = Collections.synchronizedSet(new HashSet<Parcel>());


	@Override
	public void run() {
		// TODO Auto-generated method stub

		parcelset = new HashSet<Parcel>(getParcels());

		for(Parcel p : getGame().getBoard().getParcels()){
			parcellist.add(p);
		}

		for(int k= 0; k<getTrucks().size(); k++){
			trucklist.add(getTrucks().get(k));
		}

		int size = Math.min(parcelset.size(), trucklist.size());


		synchronized (parcelset){
			int c = 0;
			for (Parcel p : parcellist){
				if (c>=size) break;
				Truck t = trucklist.get(c);

				parcellookup.put(t, p);
				t.setUserData(p);

				t.setTravelPath(Paths.dijkstra(t.getLocation(), p.start));
				if (t.getLocation().equals(p.start)){
					t.pickupLoad(p);
					parcelset.remove(p);
					parcellookup.remove(t);
				}
				c++;
			}

		}


		runner = false;

	}


	@Override
	public synchronized void truckNotification(Truck t, Notification message) {
		// TODO Auto-generated method stub
		
		if(runner){

			return;
		}

		synchronized(t){

			switch(message){
			case WAITING:

				synchronized(parcellookup){
					// Assign myself a parcel
					if(!parcelset.isEmpty() && t.getLoad() == null){


						Parcel near = findNearest(t);
						parcellookup.put(t, near);
						t.setTravelPath(Paths.dijkstra(t.getLocation(), near.start));
						parcelset.remove(near);

						if (t.getLocation().equals(parcellookup.get(t).start)){

							t.pickupLoad(parcellookup.get(t));
							parcellookup.remove(t);

						}

					} else if (t.getLoad() != null) {

						t.setTravelPath(Paths.dijkstra(t.getLocation(), t.getLoad().destination));
						if (t.getLocation().equals(t.getLoad().destination)){

							t.dropoffLoad();
						}
					} else {

						returntoDepot(t);
					}
				}

			case LOCATION_CHANGED:
				synchronized(parcellookup){
					if(t.getLoad()!=null){
						if(t.getLocation().equals(t.getLoad().destination)){

							// Drop Off Load
							t.dropoffLoad();;
						}


					} else if (t.getLoad()==null && parcellookup.get(t)!=null){

						if (t.getLocation().equals(parcellookup.get(t).start)){

							t.pickupLoad(parcellookup.get(t));
						}
					}
				}

				break;

			default:
				break;
			}
		}

		if (getParcels()==null){

			returntoDepot(t);
		}

		if (getGame().getBoard().allTrucksAreAtDepot()){
			getGame().isFinished();
		}
	}



	/** Helper function for truckNotification that returns a truck to the depot */
	public void returntoDepot(Truck t){
		t.setTravelPath(Paths.dijkstra(t.getLocation(), getGame().getBoard().getTruckDepot()));
	}


	public Parcel findNearest(Truck t){
		synchronized (parcelset){
			Set<Parcel> samecolor = new HashSet<Parcel>();
			for (Parcel p : parcelset){

				if (p.getColor().equals(t.getColor())){
					samecolor.add(p);
				}
			}
			Parcel shortest = null;
			int x = Integer.MAX_VALUE;

			if (!samecolor.isEmpty()){

				for (Parcel q : samecolor){

					int dij = Paths.pathLength(Paths.dijkstra(t.getLocation(), q.getLocation()));
					if (dij<x){
						x = dij;
						shortest = q;
					}
				}

			} else {
				
				HashMap<Node, java.lang.Integer> neighbors = t.getLocation().getNeighbors();
				Set<Map.Entry<Node, java.lang.Integer>> hep = Collections.synchronizedSet(neighbors.entrySet());
				boolean b = false;
				Map.Entry<Node, java.lang.Integer> h = null;
				synchronized (hep){
					
					for (Map.Entry<Node, java.lang.Integer> p : hep){
						
						if (parcelset.contains(p.getKey().getRandomParcel())){
							b = true;
							h = p;
							//shortest= p.getKey().getRandomParcel();
							break;
						}
						
					}
					
					if (b){
						shortest = h.getKey().getRandomParcel();
					} else {
						
						for (Parcel d : parcelset){

							int dij = Paths.pathLength(Paths.dijkstra(t.getLocation(), d.getLocation()));
							if (dij<x){
								x = dij;
								shortest= d;
							}

						}
					}
				}


/*				for (Parcel d : parcelset){

					int dij = Paths.pathLength(Paths.dijkstra(t.getLocation(), d.getLocation()));
					if (dij<x){
						x = dij;
						shortest= d;
					}

				}*/

			}

			return shortest;
		}
	}


}
