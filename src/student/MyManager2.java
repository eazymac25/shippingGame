package student;

import game.Manager;
import game.Truck;
import game.Parcel;
import java.util.*;

public class MyManager2 extends Manager {

	boolean runner = true;
	ArrayList<Truck> trucklist = new ArrayList<Truck>();
	List<Parcel> parcellist = Collections.synchronizedList(new ArrayList<Parcel>());
	HashMap<Truck, Parcel> parcellookup = new HashMap<Truck, Parcel>();
	Set<Parcel> parcelset = new HashSet<Parcel>();


	@Override
	/** Only called once and sets up all of the trucks with the first assigned parcel*/
	public void run() {
		// TODO Auto-generated method stub

		parcelset = new HashSet<Parcel>(getParcels());

		// Assign parcels to an arrayList - only used to assign trucks parcels
		for(Parcel p : getGame().getBoard().getParcels()){
			parcellist.add(p);
		}

		// ArrayList of trucks in the game
		for(int k= 0; k<getTrucks().size(); k++){
			trucklist.add(getTrucks().get(k));
		}

		int size = Math.min(parcelset.size(), trucklist.size());

		// Assign every truck a random parcel and pcik it up
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
	/** A notification system that tells the truck what to do next*/
	public synchronized void truckNotification(Truck t, Notification message) {
		// TODO Auto-generated method stub

		
			if(runner){
				return;
			}
			
			synchronized(t){
			
			switch(message){
			case WAITING: // If the truck is waiting at location

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

						// If truck has a parcel deliver it
					} else if (t.getLoad() != null) {

						t.setTravelPath(Paths.dijkstra(t.getLocation(), t.getLoad().destination));
						if (t.getLocation().equals(t.getLoad().destination)){

							t.dropoffLoad();
						}
						// Return home otherwise
					} else {

						returntoDepot(t);
					}
				}
				
				break;

			case LOCATION_CHANGED: // Truck has moved
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
		
			// Just in case trucks haven't returned - go home
		if (getParcels()==null){
			
			returntoDepot(t);
		}
		
		// Set the game to finished when trucks are home
		if (getGame().getBoard().allTrucksAreAtDepot()){
			getGame().isFinished();
		}
	}



	/** Helper function for truckNotification that returns a truck to the depot */
	public void returntoDepot(Truck t){
		t.setTravelPath(Paths.dijkstra(t.getLocation(), getGame().getBoard().getTruckDepot()));
	}


	/** Helper function for the case: WAITING in truckNotification
	 * finds the nearest parcel to the truck with a priority on color
	 * and then a priority on the closest parcel.
	 * If same color cannot be found, it looks for the closest parcel*/
	public Parcel findNearest(Truck t){
		synchronized (parcelset){
			// Find parcels of the same color
			Set<Parcel> samecolor = new HashSet<Parcel>();
			for (Parcel p : parcelset){

				if (p.getColor().equals(t.getColor())){
					samecolor.add(p);
				}
			}
			Parcel shortest = null;
			int x = Integer.MAX_VALUE;

			// Find the closest parcel of same color
			if (!samecolor.isEmpty()){

				for (Parcel q : samecolor){

					int dij = Paths.pathLength(Paths.dijkstra(t.getLocation(), q.getLocation()));
					if (dij<x){
						x = dij;
						shortest = q;
					}
				}

			} else {

				// Look for the closest parcel to the truck
				for (Parcel d : parcelset){

					int dij = Paths.pathLength(Paths.dijkstra(t.getLocation(), d.getLocation()));
					if (dij<x){
						x = dij;
						shortest= d;
						if (t.getLocation().getNeighbors().containsKey(d.getLocation())){
							break;
						}
					}

				}

			}

			return shortest;
		}
	}


}
