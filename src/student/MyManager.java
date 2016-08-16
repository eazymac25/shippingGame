package student;

import game.Manager;
import game.Truck;
import game.Parcel;
import java.util.*;

public class MyManager extends Manager {

	boolean runner = true;
	ArrayList<Truck> trucklist = new ArrayList<Truck>();
	ArrayList<Parcel> parcellist = new ArrayList<Parcel>();
	HashMap<Truck, ArrayList<Parcel>> map = new HashMap<Truck, ArrayList<Parcel>>();


	@Override
	public void run() {
		// TODO Auto-generated method stub

		createParcelList(parcellist);

		createTruckList(trucklist);
		

		if(parcellist.size()<trucklist.size()){
			synchronized (parcellist) {
				
				for (int d = 0; d<parcellist.size(); d++){
					Truck t = trucklist.get(d);
					Parcel par = parcellist.get(d);
					t.setUserData(par);
					t.setTravelPath(Paths.dijkstra(t.getLocation(), par.getLocation()));
					if (t.getLocation() == par.start){
						t.pickupLoad(par);
					}
				}
			}
			

		} else {
			int div = (parcellist.size()/trucklist.size());

			synchronized (parcellist){
				
				for (int x = 0; x< trucklist.size(); x++){

					ArrayList<Parcel> remaining = new ArrayList<Parcel>();

					for(int k = 0; k<=div && !parcellist.isEmpty(); k++){

						remaining.add(parcellist.get(0));
						parcellist.remove(0);

					}

					map.put(trucklist.get(x), remaining);
				}
			}
			
		}

		runner = false;

	}
	
	/** Helper Function 
	 * Loops through all parcels to create an ArrayList of parcels*/
	public void createParcelList(ArrayList<Parcel> plist){
		
		for(Parcel p : getGame().getBoard().getParcels()){
			plist.add(p);
		}
	}
	
	/** Helper Function 
	 * Loops through all trucks to create an ArrayList of trucks*/
	public void createTruckList(ArrayList<Truck> tlist){
		
		for(int k= 0; k<getTrucks().size(); k++){
			tlist.add(getTrucks().get(k));
		}
	}

	@Override
	public void truckNotification(Truck t, Notification message) {
		// TODO Auto-generated method stub

		if (message.equals(Manager.Notification.WAITING)){

			if(runner){
				return;
			}

		}
		

		if (!getParcels().isEmpty()){

			if (t.getLoad() != null){
				
				dropOffAtDestination(t);


			} else {
				
				pickupNextParcel(t);
			}


		} else {

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
	
	
	/** Helper function for truckNotification
	 *  It drops off a package via the shortest path */
	public void dropOffAtDestination(Truck t){
		
		
		if (t.getLocation() == t.getLoad().destination){

			if (map.get(t) == null){
				t.dropoffLoad();
				returntoDepot(t);
			}else {

				map.get(t).remove(t.getLoad());
				t.dropoffLoad();
			}

		} else {

			t.setTravelPath(Paths.dijkstra(t.getLocation(), t.getLoad().destination));
		}
		
	}
	
	
    /** Helper function for truckNotification 
     *  It picks up the trucks next parcel*/
	public void pickupNextParcel(Truck t){

		if(map.get(t) != null || t.getUserData() == null){

			if(map.get(t)!= null && !map.get(t).isEmpty()) {

				int store = 0;
				
				// If the parcel is at the trucks current location it picks it up
				HashSet<Parcel> phold = new HashSet<Parcel>(t.getLocation().getParcels());
				synchronized (phold) {
					
					for (Parcel p: t.getLocation().getParcels()) {
						if(map.get(t).contains(p)){

							t.pickupLoad(p);
							store = 1;
							break;
						}
					}

					if(store == 0){

						// Picks up the next parcel in the list of parcels
						Parcel p = map.get(t).get(0);
						t.setTravelPath(Paths.dijkstra(t.getLocation(), p.start)); 
						if (t.getLocation() == p.start)
							t.pickupLoad (p); 	

					}
				}


			} else	

				returntoDepot(t);

		} else{
			HashSet<Parcel> phold = new HashSet<Parcel>(t.getLocation().getParcels());
			synchronized (phold) {
				//If more trucks than packages look through user data to find parcel
				if(t.getLocation().getParcels().contains(t.getUserData())){
					for(Parcel pr : t.getLocation().getParcels()){
						if(pr.equals(t.getUserData())){
							t.pickupLoad(pr);
						}
					}
				}
			}

		}

	}

}
